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
