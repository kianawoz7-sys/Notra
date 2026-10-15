# whisper.cpp 1.8.2 accepts an abort callback but its scheduler helper does not
# forward it to CPU backends. A large encoder graph can therefore keep running
# after the UI deadline. Compile a checked, generated copy; never alter the
# downloaded upstream tree. The generated file lives in the native build on D.
set(whisper_upstream "${WHISPER_SOURCE_DIR}/src/whisper.cpp")
file(SHA256 "${whisper_upstream}" whisper_upstream_sha)
if(NOT whisper_upstream_sha STREQUAL "02566a39137a14e0798ec0d88e78bd626f21e7549bc27d1cb5e4a30b101a0f7e")
    message(FATAL_ERROR "Cancellation patch requires the pinned whisper.cpp v1.8.2 source")
endif()
file(READ "${whisper_upstream}" whisper_patched)
string(REPLACE
    "                      bool   sched_reset = true) {"
    "         ggml_abort_callback   abort_callback,\n                       void * abort_callback_data,\n                      bool   sched_reset = true) {"
    whisper_patched "${whisper_patched}")
string(REPLACE
    "        auto * fn_set_n_threads ="
    "        auto * fn_set_abort = (ggml_backend_set_abort_callback_t) ggml_backend_reg_get_proc_address(reg, \"ggml_backend_set_abort_callback\");\n        if (fn_set_abort) fn_set_abort(backend, abort_callback, abort_callback_data);\n\n        auto * fn_set_n_threads ="
    whisper_patched "${whisper_patched}")
string(REGEX MATCHALL "ggml_graph_compute_helper\\(sched, gf, n_threads\\)" whisper_calls "${whisper_patched}")
list(LENGTH whisper_calls whisper_call_count)
if(NOT whisper_call_count EQUAL 4)
    message(FATAL_ERROR "Unexpected Whisper scheduler call sites; inspect cancellation patch")
endif()
string(REPLACE "ggml_graph_compute_helper(sched, gf, n_threads)"
    "ggml_graph_compute_helper(sched, gf, n_threads, abort_callback, abort_callback_data)"
    whisper_patched "${whisper_patched}")
# The optional VAD API has no task callback; retain its non-reset behavior.
string(REPLACE "ggml_graph_compute_helper(sched, gf, vctx->n_threads, false)"
    "ggml_graph_compute_helper(sched, gf, vctx->n_threads, nullptr, nullptr, false)"
    whisper_patched "${whisper_patched}")
set(whisper_generated "${CMAKE_CURRENT_BINARY_DIR}/whisper-cancellable.cpp")
file(CONFIGURE OUTPUT "${whisper_generated}" CONTENT "${whisper_patched}" @ONLY)
get_target_property(whisper_sources whisper SOURCES)
list(REMOVE_ITEM whisper_sources whisper.cpp)
list(APPEND whisper_sources "${whisper_generated}")
set_property(TARGET whisper PROPERTY SOURCES "${whisper_sources}")
