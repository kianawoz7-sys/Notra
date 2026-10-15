#pragma once

#include <sched.h>
#include <algorithm>
#include <fstream>
#include <string>
#include <vector>
#include <utility>

// Best-effort, per-inference affinity. No device clock, thermal, or global policy
// is changed. Child compute threads inherit the mask; restore the caller before
// returning its coroutine worker to the pool. Homogeneous/unsupported CPUs keep
// their existing scheduler policy.
class InferenceCpuScope {
    cpu_set_t previous_{};
    bool changed_ = false;
    int threads_;
    unsigned long long mask_ = 0;

    static int read_number(const std::string &path) {
        int value = 0;
        std::ifstream input(path);
        input >> value;
        return value;
    }

public:
    InferenceCpuScope(bool prefer_fast, int default_threads) : threads_(default_threads) {
        if (!prefer_fast || sched_getaffinity(0, sizeof(previous_), &previous_) != 0) return;
        std::vector<std::pair<int, int>> cores;
        for (int cpu = 0; cpu < CPU_SETSIZE; ++cpu) {
            if (!CPU_ISSET(cpu, &previous_)) continue;
            const auto root = "/sys/devices/system/cpu/cpu" + std::to_string(cpu);
            const int capacity = read_number(root + "/cpu_capacity");
            if (capacity > 0) cores.emplace_back(capacity, cpu);
        }
        // Frequency is a fallback only when the capacity interface is absent.
        if (cores.empty()) for (int cpu = 0; cpu < CPU_SETSIZE; ++cpu) {
            if (!CPU_ISSET(cpu, &previous_)) continue;
            const int frequency = read_number("/sys/devices/system/cpu/cpu" + std::to_string(cpu) + "/cpufreq/cpuinfo_max_freq");
            if (frequency > 0) cores.emplace_back(frequency, cpu);
        }
        if (cores.size() < 2) return;
        std::sort(cores.begin(), cores.end(), [](auto a, auto b) { return a.first > b.first || (a.first == b.first && a.second < b.second); });
        if (cores.front().first == cores.back().first) return;
        const int threshold = cores.front().first / 2;
        cpu_set_t selected;
        CPU_ZERO(&selected);
        int count = 0;
        for (auto [capacity, cpu] : cores) {
            if (capacity < threshold || count == default_threads) break;
            CPU_SET(cpu, &selected);
            if (cpu < 64) mask_ |= 1ULL << cpu;
            ++count;
        }
        if (count > 0 && sched_setaffinity(0, sizeof(selected), &selected) == 0) {
            changed_ = true;
            threads_ = count;
        } else mask_ = 0;
    }

    ~InferenceCpuScope() { if (changed_) sched_setaffinity(0, sizeof(previous_), &previous_); }
    InferenceCpuScope(const InferenceCpuScope &) = delete;
    InferenceCpuScope &operator=(const InferenceCpuScope &) = delete;
    int threads() const { return threads_; }
    unsigned long long mask() const { return mask_; }
};
