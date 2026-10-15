**Notra Uji 1.1.1 — percobaan latensi, 15 Oktober 2026, Asia/Jakarta**

Kandidat dipasang sebagai `id.catatcatat.voicetest` dengan data terpisah. Notra utama pada HP tetap 1.1.0 / versionCode 6. Small tetap digunakan; profil terpilih context512 / beam3 menambah pemilihan inti cepat sementara serta mengurangi frekuensi gambar orb. Model, ikon, tema, skema database, dan format backup tetap sama.

- **65 tes unit lulus; 12 tes integrasi Android lulus pada Infinix pengguna**. Batal/timeout, pemakaian ulang native, pengeluaran satu/dua/empat item, rebu, kasus enam detik, hening, transaksi, persistensi, dan backup lulus.
- Benchmark final memakai delapan audio sintetis yang sama pada HP, dengan aktivitas foreground dan panel pencegah rekaman manual bersamaan. **8/8 benar** untuk nama dan nominal; median **5,966 detik**, rentang **5,467–11,841 detik**. Kasus lambat tetap dihitung. **Target 3 detik belum tercapai secara konsisten.** Ini bukan bukti akurasi suara asli semua orang.
- Setelah benchmark, uji MIC pengguna pada Notra Uji menghasilkan waktu **3,846 detik** untuk rekaman 3,0 detik, termasuk pemuatan model 266 ms. Ini bukti waktu satu ucapan pendek pada HP; log tidak berisi teks sehingga ketepatan hasil memerlukan pemeriksaan pengguna.
- Profil 384 dan greedy384 benar 7/8 karena “cuci” pelan menjadi “cici”; keduanya tidak dipilih. Uji pemanasan awal yang timeout dan putaran yang bercampur rekaman manual juga disimpan terpisah. Semua rincian serta tabel waktu ada di `EKSPERIMEN-VOICE.md`.
- Lint: **0 error, 7 warning**. Satu warning baru adalah string nama aplikasi lama yang tidak dipakai oleh label launcher eksperimen. Signature v2, sertifikat lama dan alignment 16 KB valid. Tanpa izin internet; Wi-Fi aktif hanya untuk koneksi wireless ADB. Tidak diklaim sebagai tes mode pesawat.
- APK `D:\CatatCatat-tools\dist\Notra-v1.1.1-Uji-context512.apk`: **211.856.571 byte**, SHA-256 `0e3738fcb7bbec51218b98b80d7e6f12804d3ae15902d04295c87f13d2e0bbbe`. Model Small di dalamnya memiliki hash pinned semula, tidak ada fixture audio dalam APK pengguna. Panduan: `PANDUAN-UJI-CEPAT.md`.

**Notra 1.1.0 — upgrade voice, 15 Oktober 2026, Asia/Jakarta**

Default voice menjadi Whisper Small multilingual Q5_1 (190.085.487 byte). Rekaman memakai jalur MIC, pemangkasan hening hanya di tepi dengan padding 400 ms, dan gain terbatas maksimal 8 kali. Decoder memakai beam search 3, konteks penuh, serta prompt kosakata tanpa contoh harga. Model diperiksa ukuran/SHA-256 sebelum dipakai. Small membutuhkan lebih banyak penyimpanan dan komputasi; tidak ada optimasi pengecilan APK.

- **59 tes unit lulus**: 28 parser/periode, 16 backup, 4 pertahanan transkripsi, 11 pemrosesan audio. Kasus tambahan mencakup parkir Rp2.000, cuci Rp12.000, langganan ChatGPT Rp165.000, charger/speaker, audio pelan, clipping, hening, klik, NaN, dan jeda internal.
- **Small: 12/12 tes Android lulus**, termasuk ucapan satu/dua/empat pengeluaran, rebu, kasus enam detik tanpa duplikasi, penolakan hening, penghentian native pada 700 ms dan pembatalan, transkripsi berikutnya setelah penghentian, Room, transaksi, dan pemulihan backup.
- **Base pembanding: 11/12 tes Android lulus**. Kasus dua pengeluaran salah ditranskripsikan menjadi `Nasi ayam 15 ribu, ST-55 ribu.` Nominal kedua ditandai tidak valid. Kegagalan pengenalan ini tetap dicatat; tes tidak dilonggarkan dan nominal tidak ditebak.
- Lint akhir: **0 error, 6 warning** (target API/dependensi, vektor splash, dan saran aturan backup Android). API/database/backup, tema, splash, serta ikon tidak diubah oleh upgrade voice.
- Uji memakai emulator Android 36.1 x86_64, RAM 4 GB, 4 core, port 5556. Mode pesawat aktif dan Wi-Fi mati. ABI ARM64 dibangun dengan pustaka baseline/FP16/dot-product; jalur runtime ARM dan mikrofon Infinix belum diuji.

Perbandingan memakai **audio yang sama dan pipeline yang sama**, hanya model yang berbeda. Audio sintetis `id-ID-ArdiNeural` dibuat di PC lalu PCM 16 kHz diproses offline pada Android. “Cepat” berarti rate TTS +20%; “pelan” berarti amplitudo PCM dikalikan 0,03. Ini bukan sampel suara pengguna atau ukuran akurasi umum.

| Contoh | Small: hasil dan waktu putaran akhir | Base pembanding: hasil dan waktu |
|---|---|---|
| Cuci 12 ribu, normal | `Cuci 12.000` — benar, 4,53 dtk | `cuci dua belas ribu.` — benar, 1,42 dtk |
| Cuci 12 ribu, +20% | `Cuci Rp12.000` — benar, 4,08 dtk | `Cuci dua belas ribu.` — benar, 1,15 dtk |
| Cuci 12 ribu, amplitudo 3% | `Cuci Rp12.000` — benar, 4,16 dtk | `cuci dua belas ribu.` — benar, 1,18 dtk |
| Parkir 2 ribu | `Parkir 2000` — benar, 4,03 dtk | `Parkir dwari bu.` — nominal hilang, 1,25 dtk |
| Langganan ChatGPT 165 ribu | `Langganan ChatGPT 165.000` — benar, 4,22 dtk | `Langganan ChatGPT 165.` — nominal ambigu, 1,22 dtk |
| Charger 30 ribu, speaker 120 ribu | `Charger 30.000, speaker 120.000.` — benar, 4,27 dtk | `Charger 30 ribu, speaker 120 ribu.` — benar, 1,25 dtk |

Small benar untuk nama dan nominal **6/6** contoh tersebut; Base benar untuk **4/6**. Nama ChatGPT diperiksa lengkap pada putaran akhir. Metode benchmark melaporkan semua teks/error dan waktu; keberhasilan menjalankan metode benchmark sendiri bukan bukti bahwa semua ucapan benar. Salah satu putaran awal Small memerlukan 22,84 detik untuk contoh pertama; putaran akhir 4,03–4,53 detik. Beban/warm-up emulator memengaruhi hasil. Angka ini tidak memprediksi GT 10 Pro. Contoh lama “macha ... es teh ...” masih menghasilkan nama `Maca`/`ST` walaupun nominal benar, sehingga koreksi nama sebelum menyimpan tetap diperlukan.

Uji pertama menemukan scheduler upstream v1.8.2 tidak meneruskan callback abort ke CPU; encoder besar terus berjalan setelah deadline. `WhisperCancellation.cmake` kini memeriksa hash source lalu menghasilkan salinan yang meneruskan callback pada conv/encoder/cross/decoder. Patch disimpan di Git, sehingga pembersihan unduhan D tidak menghapus perbaikannya. Penghentian dan pemakaian ulang konteks lulus sesudah patch. Jalur x86_64 AVX2/FMA/F16C memiliki pemeriksaan fitur dan baseline tetap tersedia; pengujian di atas memakai jalur cepat emulator.

Catatan UI `Uji Small Base` Rp12.345 dibuat di Small, lalu APK diganti ke Base dan kembali ke Small tanpa uninstall. Catatan dan total tetap Rp12.345 pada kedua pergantian. Paket memakai ID `id.catatcatat`, versionCode **6**, sertifikat SHA-256 tetap `755400dbccf91a70bb87bf41188720b315d3b22818fa7bd797260d5cc26864f7`. Signature v2 dan alignment 16 KB valid. Manifest hanya meminta mikrofon serta permission internal AndroidX, tanpa izin internet. Masing-masing APK memuat satu model, kedua ABI, dan lisensi; audio tes tidak masuk APK pengguna.

| APK di `D:\CatatCatat-tools\dist` | Ukuran | SHA-256 |
|---|---|---|
| `Notra-v1.1.0-Small.apk` | 214.319.357 byte (~204,4 MiB) | `d60261a62306dbe0748baa159fdc6cfbf5c3893c7e025692102ab09c96447e99` |
| `Notra-v1.1.0-Base-pembanding.apk` | 81.173.793 byte (~77,4 MiB) | `934e93dd1296c3f47c2942d7cddb251ec4e1554618c58a3b401eae9bdc04de67` |

Base adalah paket eksperimen pembanding, dengan kegagalan yang dicatat di atas. Kedua APK bisa dipasang bergantian sebagai pembaruan; APK arsip 1.0.4 memakai versionCode 5 sehingga installer biasa dapat menolak downgrade langsung. Semua unduhan, dependensi tambahan, native/cache build, fixture, emulator, log, dan paket baru berada di D. Source yang diedit tetap di C. Log lengkap serta `Notra-v1.1.0-verifikasi.json` berada di D; suara asli, jarak/tempo nyaman, waktu proses, dan konsumsi baterai pada HP masih perlu dicoba pengguna.

Pemeriksaan tambahan lewat tombol rekam tidak selesai: proses host `qemu-system-x86_64-headless.exe` berhenti dua kali dengan Windows Application Error `0xc0000005`, sehingga ADB offline. Ini terjadi setelah seluruh uji transkripsi fixture dan pergantian APK di atas selesai. Tidak ada hasil lulus untuk capture MIC dari pemeriksaan tersebut; keberhasilan tes fixture tidak memvalidasi jalur mikrofon nyata. Log host emulator disimpan di D. Penyebab spesifik crash QEMU belum dipastikan; perubahan UI tidak dilakukan untuk mengakalinya. Uji capture MIC dan akurasi suara tetap menunggu HP fisik.

**Ikon Notra 1.0.4 — 14 Oktober 2026, Asia/Jakarta**

Logo diganti dari gambar terbaru pengguna. Penghapusan latar memakai tool imagegen bawaan, kemudian alpha hasil diperiksa. PNG sumber dan PNG di APK sama-sama RGBA 1254×1254, alpha 0 pada empat sudut, dengan 851.667 piksel sepenuhnya transparan. Pemeriksaan alpha ≥128 menemukan satu komponen objek utama dan tidak menemukan komponen terpisah di luarnya. Detail objek gelap dan lipatan catatan tetap ada. Prompt lengkap dan lokasi aset tercatat di `ICON.md`.

- Build APK dan lint berhasil: **0 error, 13 warning** yang sama dengan pemeriksaan terakhir 1.0.3. Perubahan hanya resource ikon, nomor versi, dokumentasi, dan pengabaian folder lampiran chat pada Git.
- APK dipasang sebagai pembaruan di emulator proyek; aplikasi tetap terbuka dan Laporan masih menampilkan Kost Rp800.000 dari data uji sebelumnya. Model suara, parser, Room, backup, tema, dan animasi pembuka tidak diubah. Tes inti tidak diulang untuk penggantian gambar ini.
- Ikon PNG baru diperiksa pada launcher emulator. Pengujian format adaptif dengan latar transparan menghasilkan lingkaran hitam; format final memakai bitmap RGBA langsung. Launcher Pixel tetap menambahkan bingkai putih dan menyesuaikan ukuran objek. PNG dan resource aplikasi tidak menyertakan bingkai tersebut. Tampilan launcher Infinix belum diuji secara langsung, sehingga belum diklaim bebas bingkai di HP.
- Signature dan alignment 16 KB valid. Sertifikat tetap `755400dbccf91a70bb87bf41188720b315d3b22818fa7bd797260d5cc26864f7`; ID `id.catatcatat`, versionCode 5, versionName 1.0.4. Pembaruan dapat dipasang tanpa uninstall.
- APK `D:\CatatCatat-tools\dist\Notra-v1.0.4.apk`: **78.796.444 byte**, SHA-256 `f9dbf00d031d2375d243147c72b6f2db82d17e91328d89fe17db1dfab06dc83d`. PNG transparan: `D:\CatatCatat-tools\dist\notra-logo-transparent-v1.0.4.png`, SHA-256 `c4ef099c037fc467c03d055ba0af3660b473a68987f8663f34dd3d09f5cc9c48`.

**Notra 1.0.3 — 14 Oktober 2026, Asia/Jakarta**

Nama aplikasi menjadi **Notra**, memakai gambar catatan bertumpuk dari pengguna. Pengaturan menambahkan satu kartu **Tema tampilan** dengan pilihan **Terang / Gelap / Sistem**. Pilihan tersimpan di SharedPreferences dan Sistem menjadi pilihan awal. Splash native menampilkan tiga titik berderet yang membentuk lingkaran, berputar, lalu menghilang sebelum halaman utama muncul. Animasi Compose berdurasi sekitar 1,65 detik dan mengikuti pengaturan durasi animasi Android.

- Build APK berhasil; **44 tes unit lulus, 0 failure/error**. Lint: **0 error, 13 warning**. Dua belas warning terkait dependensi/platform yang sudah ada; satu saran VectorRaster muncul karena ukuran kanvas ikon splash Android 288dp. Vektor tersebut hanya menggambar tiga titik dan ukurannya menyamakan titik pembuka sistem dengan titik Compose.
- UI diperiksa pada emulator proyek Android 36.1, x86_64, port 5556, dengan mode pesawat aktif. Tema Gelap tampil pada halaman utama, pengaturan, laporan, dan input manual. Warna teks, panel, kolom, tombol, status bar, serta navigation bar ikut menyesuaikan.
- Tema Gelap bertahan setelah force-stop dan pembukaan ulang. Pilihan Terang tetap terang ketika sistem Android disetel gelap. Pilihan Sistem mengikuti perubahan tema Android dari gelap ke terang. Kartu cadangan dan seluruh tombolnya tetap dapat diakses.
- Video startup memperlihatkan urutan titik berderet, berubah posisi ke lingkaran, berputar, lalu masuk ke halaman utama. Untuk verifikasi gerakan, skala animasi emulator diaktifkan sementara dan kemudian dikembalikan. Saat animasi Android dimatikan, aplikasi tetap dapat membuka halaman utama.
- Launcher emulator memperlihatkan ikon baru; metadata launcher dan APK menyebut **Notra**. PNG sumber sama dengan file pengguna, SHA-256 `abd07f218024bcf7dd856a48e53f868696c13864e6e2c6ca0b5bf46e0fe89207`. Lapisan monokrom tersedia untuk launcher yang memakai ikon bertema.
- Catatan emulator dari versi sebelumnya, Kost Rp800.000, tetap ada setelah instalasi pembaruan. ID aplikasi `id.catatcatat`, database, skema Room, format JSON cadangan versi 1, dan sertifikat dipertahankan. Nama file cadangan baru memakai awalan `notra-cadangan`; cadangan Catat-Catat tetap kompatibel.
- Signature v2 dan alignment 16 KB valid. Sertifikat SHA-256 tetap `755400dbccf91a70bb87bf41188720b315d3b22818fa7bd797260d5cc26864f7`. APK versionCode 4, versionName 1.0.3, ABI arm64-v8a/x86_64, tanpa izin internet.
- Salinan build sekarang menyinkronkan `app/src` agar resource yang dihapus di sumber tidak tertinggal di D. Semua cache, file sementara, emulator, APK, screenshot, dan video berada di D. Model suara, JNI, parser, database, serta aturan pemulihan tidak diubah.

APK: `D:\CatatCatat-tools\dist\Notra-v1.0.3.apk`, **78.983.347 byte**, SHA-256 `d4b3c285549db9b13917da02049ffb07537894d54ea34fc71fe5be65732e8dc3`. Pratinjau: `notra-tema-gelap-v1.0.3.png`, `notra-tema-terang-v1.0.3.png`, dan `notra-splash-v1.0.3.mp4` di folder dist. Ini merupakan pemeriksaan emulator; suara dan performa HP fisik tidak diuji ulang untuk perubahan tampilan ini.

**Pembaruan UI 1.0.2 — 14 Oktober 2026, Asia/Jakarta**

Perubahan mengikuti referensi pengguna: halaman utama hanya menampilkan tanggal, bola kecil, ikon laporan/pengaturan, serta tombol Isi manual kaca gelap selebar halaman. Judul merek, slogan, petunjuk, label offline, total harian, daftar catatan, dan navigasi teks bawah dihapus dari halaman utama. Bola membesar dengan animasi saat merekam; selesai/batal memakai ikon. Laporan dibuka dari kiri atas dan dimulai pada hari ini.

Build APK dan lint berhasil dengan 0 error. Tampilan utama dan rekaman diperiksa dari screenshot emulator; metadata UI halaman utama hanya berisi tanggal dan Isi manual sebagai teks terlihat. Diperiksa pula pembukaan input manual, pembatalan rekaman, ikon selesai merekam, pesan bila audio tidak terdengar, Laporan harian berisi catatan uji Kost Rp800.000, serta akses tombol backup di Pengaturan. Model, JNI, parser, database, dan aturan backup tidak diubah. Tes inti 44 unit/12 Android dari 1.0.1 tetap tercatat di bawah; tidak dijalankan ulang untuk perubahan tampilan ini.

APK: `D:\CatatCatat-tools\dist\Catat-Catat-v1.0.2.apk`. Screenshot: `tampilan-utama-v1.0.2.png` dan `tampilan-voice-v1.0.2.png` dalam folder dist. ID aplikasi dan sertifikat sama dengan versi sebelumnya sehingga bisa dipasang sebagai pembaruan tanpa uninstall. Semua output dan cache build tetap di D.

**Pembaruan 1.0.1 — 14 Oktober 2026, Asia/Jakarta**

Pemicu perbaikan: pengguna merekam sekitar enam detik di GT 10 Pro, menunggu lebih dari satu menit, lalu melihat “makan lima belas ribu” diulang menjadi 28 draft dengan total Rp405.000. Ini adalah keluaran pengenalan suara yang keliru, bukan 28 pembelian. Error unduhan Gradle 9.0 di Android Studio adalah masalah terpisah dari runtime APK.

- Build Gradle 8.14 di D berhasil; **44 tes unit lulus, 0 failure/error**. Empat tes baru menolak transkripsi yang terlalu panjang atau berulang, sekaligus mempertahankan empat pembelian identik yang mungkin memang berbeda.
- **12 tes Android lulus** dalam mode pesawat. Selain penyimpanan dan contoh satu/dua/empat pengeluaran serta “rebu”, tes mencakup ucapan “makan lima belas ribu” pada PCM berdurasi enam detik, penolakan audio tanpa suara, pemangkasan hening tanpa menghapus jeda internal, penghentian JNI saat dibatalkan/timeout, serta transkripsi kembali setelah pembatalan.
- Kasus enam detik menghasilkan **satu pengeluaran Rp15.000**. Audio adalah suara sintetis Ardi dengan hening di tepi, bukan rekaman asli pengguna. Dua pengukuran emulator menghasilkan sekitar 6,1 dan 1,7 detik pemrosesan; ini **bukan prediksi waktu GT 10 Pro**. Log pengujian final: `D:\CatatCatat-tools\voicefix-final-device.log`.
- Lint: **0 error, 12 warning**. Signature v2 dan alignment 16 KB valid. Sertifikat sama dengan APK v1.0: SHA-256 `755400dbccf91a70bb87bf41188720b315d3b22818fa7bd797260d5cc26864f7`. ID aplikasi tetap `id.catatcatat`, versionCode naik ke 2; pembaruan tidak memerlukan uninstall.
- Model Base tetap dipaketkan. Percobaan Tiny lebih cepat tetapi salah membaca sejumlah nominal; model Tiny disimpan di D untuk eksperimen dan tidak ada dalam APK pembaruan.
- Model native digunakan kembali, konteks minimum dinaikkan, dan jumlah token decoder dibatasi. Hasil yang terpotong oleh batas token, terlalu panjang, atau mengulang pola berkali-kali ditolak sebelum menjadi draft. Pembelian yang mungkin berbeda tidak digabung diam-diam.
- Layar proses memiliki tahap, timer, indikator, batal, dan input manual. Setelah 45 detik UI menawarkan pemulihan dan membatalkan komputasi native. ID sesi mencegah hasil lama membuka draft setelah pengguna membatalkan/beralih manual.
- Pustaka ARM64 cepat dengan FP16/dot-product dan pustaka baseline berhasil dibangun. Disassembly menunjukkan instruksi `sdot` pada pustaka cepat dan tidak pada baseline. Pemilihan cepat memerlukan fitur CPU `asimdhp` dan `asimddp`; pengujian Android di atas memakai x86_64 baseline, **bukan validasi runtime jalur ARM di HP**.
- UI emulator: rekaman tanpa audio menampilkan pesan jelas serta tombol Rekam ulang/Isi manual. Input manual dapat dibuka dan dibatalkan; catatan uji Kost Rp800.000 tetap utuh. Tangkapan layar pemulihan: `D:\CatatCatat-tools\ui-check\voicefix-recovery.png`.
- Android Studio memilih Gradle lokal 8.14 melalui `.idea/gradle.xml`. Java lokal menunjuk JBR yang sudah ada di D. Petunjuk distribusi lokal, cache D, build mirror, dan membaca log HP tersedia di README/PANDUAN.

APK pembaruan: `D:\CatatCatat-tools\dist\Catat-Catat-v1.0.1.apk`. HP fisik belum terhubung melalui ADB selama perbaikan ini. Akurasi suara asli, durasi di GT 10 Pro, serta pemilihan kernel cepat perlu diuji pengguna pada APK pembaruan. Log produksi mencatat durasi/model/jalur CPU/error tanpa isi ucapan.

**Arsip pemeriksaan versi 1.0 — 14 Oktober 2026, Asia/Jakarta**

Build APK, pemeriksaan Android lint, 40 tes unit, serta delapan kasus pengujian Android berhasil. Pengujian Android dijalankan langsung melalui AndroidJUnitRunner/ADB pada emulator tersendiri di D. Tujuh kasus dijalankan sebagai satu kelompok, lalu satu kasus ucapan “rebu” dijalankan terpisah setelah ditambahkan. Pemeriksaan lint terakhir: **0 error, 12 warning**. Warning mencakup saran pembaruan dependensi dan konfigurasi platform; versi dependensi dipatok agar build dapat direproduksi.

**Lingkungan**

- Windows; sumber yang diedit di folder proyek C, salinan build di `D:\CatatCatat-tools\source`.
- Seluruh unduhan, alat tambahan, cache build, output, dan perangkat emulator di D. Java, SDK, NDK, CMake, dan Gradle yang sudah tersedia di D digunakan kembali.
- Emulator Android 36.1, x86_64, RAM 2 GB, port 5556, Windows Hypervisor Platform.
- Mode pesawat aktif, Wi-Fi dan data seluler dimatikan selama pengujian Android dan alur UI.
- APK mencakup ABI arm64-v8a untuk HP sasaran dan x86_64 untuk emulator. Model base multilingual q5_1 ada dalam assets, checksum sumber diverifikasi.

**40 tes unit**

- 24 tes pengurai dan periode laporan: rebu/ribu/rb/k, angka tertulis/digit, juta, pecahan ribuan, gabungan ribuan–ratusan, beberapa pengeluaran, nominal ambigu/negatif/berlebihan, pengeluaran tanpa nominal, batas minggu/tahun dan bulan kabisat, serta total yang mengabaikan catatan dihapus.
- 16 tes backup: round-trip nama/tanggal/nominal/penghapusan, signature, pemulihan berulang, ID berbeda dengan nama/harga sama, konflik edit/hapus, ID ganda/tidak valid, tanggal mustahil, nominal pecahan/negatif, file terpotong, status tidak valid, versi dan mata uang tidak didukung.
- Semua tes: 0 failure, 0 error.

**Delapan kasus Android**

1. Kegagalan transaksi mempertahankan data semula.
2. Ucapan bahasa Indonesia dipisahkan menjadi dua pengeluaran Rp15.000 dan Rp5.000.
3. Satu pengeluaran dihasilkan sebagai Rp12.000.
4. Model native yang dipaketkan dapat dimuat dan dijalankan offline.
5. Empat jenis pengeluaran dihasilkan sebagai Rp800.000, Rp50.000, Rp5.000, dan Rp30.000.
6. Catatan bertahan ketika database ditutup lalu dibuka kembali.
7. Backup dipulihkan ke database kosong tanpa duplikasi saat diulang.
8. Ucapan “macha dua belas rebu, es teh lima rebu” menghasilkan dua nominal Rp12.000 dan Rp5.000.

Audio pengujian adalah suara sintetis bahasa Indonesia, bukan suara pengguna. Fixture audio hanya berada di APK pengujian dan tidak ikut dalam APK pengguna.

Optimasi native menyesuaikan konteks audio dengan panjang rekaman dan memberi padding dua detik. Rekaman panjang tetap memakai batas konteks model. Pada emulator, kasus dua pengeluaran berubah dari sekitar 93,7 detik sebelum optimasi menjadi 24,4 detik sesudah optimasi. Kasus satu pengeluaran sekitar 16,8 detik, empat pengeluaran 30,1 detik, dan contoh “rebu” sekitar 25,3 detik. Angka ini adalah hasil emulator RAM 2 GB dan **bukan perkiraan kecepatan GT 10 Pro**.

Pada contoh “rebu”, model membaca nama “es teh” sebagai “estih”; nominalnya tetap benar. Ini menunjukkan alasan hasil suara harus diperiksa dan nama dapat dikoreksi sebelum simpan. Akurasi untuk suara, aksen, dan lingkungan pengguna tetap perlu diuji pada HP fisik.

**Pemeriksaan UI dan pemilih file Android**

- Halaman utama, laporan, pemeriksaan draft, pengaturan, dan layar voice dibuka serta tangkapan layarnya diperiksa.
- Input manual Kost Rp800.000 tersimpan; total tetap Rp800.000 setelah aplikasi dihentikan dan dibuka lagi.
- Edit ke Rp850.000 langsung memperbarui total.
- Hapus melalui menu dan konfirmasi mengubah total menjadi Rp0.
- Backup berhasil disimpan sebagai JSON melalui pemilih file Android di folder Downloads.
- Impor file yang sama menampilkan satu catatan identik untuk dilewati; jumlah catatan tetap satu.
- Impor versi lama setelah edit menampilkan Rp850.000 sekarang dan Rp800.000 pada backup. Dengan pilihan default, Rp850.000 dipertahankan.
- Impor setelah penghapusan menampilkan status dihapus sebagai konflik. Catatan kembali Rp800.000 setelah pengguna secara eksplisit memilih versi backup.
- Tombol berbagi membuka menu berbagi Android dengan file JSON. Pengujian tidak memilih penerima atau mengirim/mengunggah file.
- Izin mikrofon, tampilan rekaman, timer, bola voice, dan tombol batal diperiksa pada emulator. Akurasi mikrofon nyata belum diuji karena perangkat fisik pengguna belum dihubungkan.

**Paket**

APK ditandatangani untuk pemasangan lokal. Signature scheme v2 dan pemeriksaan ZIP alignment 16 KB diverifikasi. Manifest hanya meminta izin mikrofon serta permission internal receiver AndroidX; tidak meminta izin internet. Lisensi whisper.cpp, Whisper, GGML, dan Apache 2.0 disertakan dalam assets.

Paket siap untuk pengujian pengguna; pengujian suara nyata, waktu tunggu, dan penggunaan baterai pada Infinix GT 10 Pro masih menunggu pengguna kembali.
