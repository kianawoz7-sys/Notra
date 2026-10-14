**Notra — catatan pengeluaran lewat suara, offline di Android**

Aplikasi untuk menyebut beberapa pengeluaran sekaligus, memeriksa hasil, lalu menyimpan semuanya. Mendukung semua nama pengeluaran: makanan, minuman, kost, paket data, bensin, ngeprint, dan lainnya. Model Whisper base multilingual q5_1 disertakan dalam APK. Tidak ada izin internet pada aplikasi.

Perangkat sasaran pertama: Infinix GT 10 Pro, RAM fisik 8 GB. Android minimum: Android 8.0. Paket menyertakan arm64 untuk HP dan x86_64 untuk pengujian emulator.

**Cara menggunakan**

1. Buka aplikasi, lalu ketuk bola hitam-putih untuk mulai mencatat. Izinkan mikrofon saat Android meminta izin.
2. Sebutkan nama dan harga masing-masing pengeluaran, misalnya: “Nasi ayam lima belas ribu, es teh lima ribu.” Variasi “rebu”, “rb”, dan nominal juta dipahami oleh pengurai jika transkripsinya terbaca.
3. Tekan ikon persegi di bawah bola atau ketuk bola besar untuk selesai bicara. Rekaman dibatasi 60 detik dan berhenti otomatis bila batas tercapai.
4. Periksa nama, nominal, dan tanggal. Perbaiki baris yang keliru, hapus baris yang tidak diperlukan, atau tambah pengeluaran manual.
5. Tekan **Simpan semua**. Pengeluaran dengan nama/nominal belum lengkap harus dikoreksi dahulu.
6. Ikon kiri atas membuka **Laporan**. Catatan tersimpan dapat diketuk untuk diedit. Menu titik tiga memiliki pilihan hapus dengan konfirmasi.
7. **Laporan** dimulai dari hari ini dan menampilkan total serta rincian per hari, minggu Senin–Minggu, atau bulan kalender. Gunakan panah atau pemilih tanggal untuk pindah periode.

UI versi 1.0.2 mengikuti referensi pengguna: halaman utama tanpa judul merek, slogan, petunjuk, label offline, total, atau daftar catatan. Tanggal tetap ada. Bola kecil membesar dengan animasi saat merekam; kontrol rekaman berupa ikon. Tombol Isi manual menggunakan pill kaca gelap selebar halaman. Total dan catatan hanya ada di Laporan. Perubahan ini tidak mengubah transkripsi, pengurai, database, maupun aturan backup.

Versi 1.0.3 memakai nama **Notra** dan gambar logo catatan bertumpuk yang diberikan pengguna. **Pengaturan → Tema tampilan** menyediakan **Terang**, **Gelap**, dan **Sistem**. Pilihan tersimpan di HP; Sistem menjadi pilihan awal dan mengikuti tema Android. Warna halaman, formulir, laporan, dialog, serta status bar menyesuaikan tema. Saat aplikasi dibuka, animasi singkat menampilkan tiga titik berderet yang membentuk lingkaran lalu berputar. Halaman utama tetap minimal.

Versi 1.0.4 mengganti logo dengan gambar catatan bertumpuk terbaru dari pengguna, dengan area di luar objek menjadi transparan. Ikon memakai PNG RGBA langsung agar tidak menambahkan latar pada resource aplikasi. Launcher Android dapat memberi bingkai sendiri; Pixel pada emulator memberi bingkai putih otomatis. Hasil di launcher Infinix perlu diperiksa di HP. PNG dan catatan proses edit ada di [ICON.md](ICON.md).

Nama folder proyek, ID aplikasi `id.catatcatat`, nama database, format cadangan, dan sertifikat tetap kompatibel dengan Catat-Catat. Pasang APK Notra sebagai pembaruan tanpa uninstall agar data lama tetap ada. Cadangan dari versi sebelumnya tetap dapat dipulihkan. File cadangan baru memakai awalan `notra-cadangan`.

Saat mikrofon aktif, rekaman dibatalkan bila aplikasi ditinggalkan. Rekaman tidak disimpan sebagai bagian dari riwayat atau backup. Pengeluaran baru masuk ke database setelah pengguna menekan tombol simpan.

**Backup**

- Buka ikon pengaturan, lalu **Cadangkan data**. Pilih folder yang berada di luar penyimpanan internal aplikasi melalui pemilih file Android.
- **Bagikan backup** membuat file sementara untuk disalin lewat menu berbagi Android. Membuka menu berbagi belum berarti salinan telah sampai ke tempat lain.
- Simpan salinan di luar HP, misalnya laptop, flashdisk, atau cloud saat ada internet, untuk menghadapi HP hilang/rusak.
- **Pulihkan data dari file** memeriksa format terlebih dahulu, lalu menampilkan ringkasan pemulihan. Catatan identik dilewati. Untuk ID yang sama dengan isi/status berbeda, pengguna memilih versi yang digunakan. Pengeluaran berbeda dengan nama/harga sama tetap dihitung terpisah.
- Sebelum pemulihan, aplikasi menyimpan snapshot pengaman lokal `before-restore.json` dalam penyimpanan privat. Perubahan database diterapkan dalam satu transaksi.
- Backup berisi catatan, ID, tanggal, waktu perubahan, dan penanda penghapusan. Model suara tidak ikut dicadangkan.

**Build di komputer ini**

Sumber aplikasi berada di folder proyek ini. Semua unduhan, alat, cache, file sementara, hasil native build, emulator, dan APK berada di drive D sesuai permintaan pengguna.

Setelah clone dari GitHub, salin `local.properties.example` menjadi `local.properties`, lalu sesuaikan jalur SDK, sumber whisper.cpp 1.8.2, dan model pada komputer tersebut. Model, SDK, cache, APK, dan kunci penandatanganan tidak disimpan dalam repositori. Folder model harus menyediakan `ggml-base-q5_1.bin` dan file lisensi yang disebutkan di bawah. Untuk memperbarui instalasi Catat-Catat/Notra yang sudah ada, gunakan kunci penandatanganan yang sama dengan APK sebelumnya.

| Lokasi | Isi |
|---|---|
| `D:\Android studio\jbr` | Java yang sudah terpasang |
| `D:\Backup Android Studio\.gradle` | Gradle dan cache dependensi yang sudah tersedia; unduhan tambahan tetap di D |
| `D:\Backup Android Studio\Android-SDK\Sdk` | Android SDK/NDK/CMake yang sudah tersedia |
| `D:\CatatCatat-tools\sdk` | Junction menuju SDK di D untuk menghindari spasi pada jalur native build |
| `D:\CatatCatat-tools\whisper.cpp-1.8.2` | Sumber whisper.cpp versi 1.8.2 |
| `D:\CatatCatat-tools\models` | Model offline serta lisensi pihak ketiga |
| `D:\CatatCatat-tools\packaged-model` | Assets terpilih untuk APK: model Base dan lisensi; model percobaan tidak ikut |
| `D:\CatatCatat-tools\temp` | File sementara proses build dan unduhan |
| `D:\CatatCatat-tools\build` | Hasil build dan laporan pemeriksaan |
| `D:\CatatCatat-tools\source` | Salinan sumber untuk build; menghindari masalah KSP Windows lintas drive |
| `D:\CatatCatat-tools\avd` | Perangkat emulator pengujian tersendiri |
| `D:\CatatCatat-tools\dist` | Paket APK dan panduan yang disiapkan untuk pengguna |

Jalankan PowerShell dari folder proyek:

```powershell
.\build.ps1
# Pengujian pada emulator yang sudah berjalan:
.\test-device.ps1
```

`local.properties` menetapkan lokasi SDK, sumber whisper.cpp, dan assets model. `build.ps1` menyalin sumber terbaru ke D sebelum menjalankan Gradle, karena KSP Windows mengharuskan sumber dan hasil generasi berada di drive yang sama. Folder `app/src` disinkronkan agar resource yang dihapus tidak tertinggal pada salinan build. Sumber yang diedit tetap di proyek C. Gradle mengarahkan output serta project cache ke D. Pengembangan menggunakan AGP 8.7.0, Kotlin 2.0.21, Gradle 8.14, NDK 28.2.13676358, dan CMake 3.22.1.

Jika Android Studio mencoba mengunduh `gradle-9.0.0-bin.zip` lalu mengalami `SocketTimeoutException`, pilih **Settings → Build, Execution, Deployment → Build Tools → Gradle → Distribution: Local installation**. Gunakan folder `D:\Backup Android Studio\.gradle\wrapper\dists\gradle-8.14-all\c2qonpi39x1mddn7hk5gh9iqj\gradle-8.14`, Gradle user home `D:\Backup Android Studio\.gradle`, dan Gradle JDK `D:\Android studio\jbr`. Konfigurasi proyek `.idea/gradle.xml` sudah memilih distribusi lokal tersebut. Build yang diverifikasi tetap melalui `build.ps1`, yang memakai salinan D untuk menghindari masalah KSP lintas drive. Membaca Logcat HP tidak memerlukan unduhan Gradle baru.

Model: `ggml-base-q5_1.bin`, 59.707.625 byte, SHA-256 `422f1ae452ade6f30a004d7e5c6a43195e4433bc370bf23fac9cc591f01a8898`. Sumber unduhan dikunci pada revisi `5359861c739e955e79d9a303bcbc70fb988958b1` dari [ggerganov/whisper.cpp](https://huggingface.co/ggerganov/whisper.cpp). [whisper.cpp](https://github.com/ggml-org/whisper.cpp) dan [model Whisper](https://github.com/openai/whisper) memakai lisensi MIT; salinan lisensi disertakan dalam assets APK.

**Pengujian dan batas bukti**

Pengujian otomatis mencakup parsing nominal bahasa Indonesia, beberapa pengeluaran, satuan ambigu, jumlah negatif/berlebihan, batas minggu/bulan, total yang mengabaikan penghapusan, round-trip backup, ID ganda, konflik edit/hapus, file rusak, dan pemulihan berulang. Pengujian Android terpisah memeriksa Room setelah database dibuka kembali, rollback transaksi, pemulihan ke database kosong, dan pemuatan model native offline.

Hasil versi awal dan pembaruan ada di `HASIL-UJI.md`. Versi 1.0.1 memiliki 44 tes unit dan 12 kasus Android, termasuk kasus ucapan enam detik, penolakan audio tanpa suara, penghentian native saat batal/timeout, dan penggunaan ulang model setelah pembatalan. Pengujian Android menggunakan AndroidJUnitRunner langsung melalui ADB agar tidak bergantung pada unduhan tambahan pengelola tes Gradle. Akurasi terhadap suara pengguna dan waktu proses di GT 10 Pro memerlukan uji ulang langsung pada HP tersebut.

Paket pembaruan: `D:\CatatCatat-tools\dist\Notra-v1.0.4.apk`. Panduan singkat ada di `PANDUAN.md`. Model Base tetap dipakai setelah model Tiny menurunkan ketepatan nominal dalam percobaan. Model yang sudah dimuat digunakan kembali; konteks minimum dinaikkan menjadi 512 frame dan keluaran decoder dibatasi. Transkripsi terlalu panjang/berulang ditolak sebelum diparsing. Timer 45 detik membebaskan UI dan memicu penghentian native melalui abort callback. Hasil terlambat dari proses yang dibatalkan tidak boleh membuka draft atau mengganti input manual.

ARM64 memiliki pustaka baseline dan pustaka dengan kernel FP16/dot-product. Pustaka cepat hanya dipilih jika fitur `asimdhp` dan `asimddp` dilaporkan CPU; x86_64 serta ARM tanpa fitur tersebut memakai baseline. Build kedua pustaka dan instruksi SIMD pada pustaka cepat telah diperiksa; pengukuran jalur cepat di HP fisik masih perlu dilakukan. `read-phone-log.ps1` membaca log khusus `CatatVoice` dari HP USB dan menulis hasilnya ke D. Isi ucapan tidak dicatat dalam log produksi.
