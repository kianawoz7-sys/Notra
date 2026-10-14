**Notra siap dicoba di Android**

APK pembaruan: `D:\CatatCatat-tools\dist\Notra-v1.0.3.apk`. Model suara sudah ada di dalam APK; tidak perlu mengunduh model lagi di HP. Pasang sebagai pembaruan di atas Catat-Catat, tanpa mencopot aplikasi, agar catatan tetap ada. Sertifikat penandatanganan dan ID aplikasi tetap sama.

**Memasang**

1. Pindahkan APK ke Infinix GT 10 Pro, misalnya melalui kabel USB.
2. Buka file APK di HP dan pilih pasang. Jika Android meminta izin memasang dari aplikasi pembuka file, aktifkan izin tersebut lalu lanjutkan.
3. Buka **Notra** dengan ikon catatan bertumpuk. Tiga titik akan membentuk lingkaran dan berputar sebelum halaman utama muncul. Ketika pertama kali memakai voice, izinkan mikrofon. Setelah pemasangan, kamu dapat mencoba dalam mode pesawat.

**Mencatat**

- Ketuk bola hitam-putih, lalu sebutkan nama dan harga setiap pengeluaran. Contoh: “Macha dua belas rebu, es teh lima rebu.”
- Bola kecil membesar saat ditekan. Untuk selesai bicara, tekan ikon persegi di bawah bola atau ketuk bola besar. Tunggu hasilnya, periksa nama/nominal/tanggal, lalu tekan **Simpan semua**.
- Saat menunggu, indikator berputar menunjukkan pemrosesan. Tombol silang membatalkan proses; **Isi manual** langsung membuka input manual. Batas waktu 45 detik tetap berlaku. Saat gagal, ikon mikrofon merekam ulang dan tombol Isi manual tetap tersedia.
- Hasil yang terlalu panjang atau mengulang kalimat berkali-kali ditolak sebelum menjadi draft. Jangan menyimpan hasil lama yang mengulang satu pembelian menjadi banyak pengeluaran.
- Untuk membatalkan rekaman, tekan tanda silang. Rekaman yang belum disimpan tidak masuk ke laporan.
- **Isi manual** berupa tombol kaca gelap selebar halaman utama. Halaman utama hanya berisi tanggal, bola suara, dan kontrol; tidak menampilkan daftar atau total pengeluaran.
- Ikon menu di kiri atas membuka **Laporan**, dimulai dari hari ini. Halaman ini menampilkan catatan serta total harian, minggu Senin–Minggu, dan bulan kalender. Ketuk catatan untuk mengubahnya; menu titik tiga menyediakan hapus dengan konfirmasi.

**Mengatur tema**

- Buka ikon pengaturan → **Tema tampilan**.
- Pilih **Terang** untuk latar cerah, **Gelap** untuk latar gelap, atau **Sistem** agar mengikuti pengaturan Android.
- Pilihan langsung diterapkan dan tetap tersimpan setelah aplikasi ditutup. Pilihan awal adalah Sistem.

**Mencadangkan dan memulihkan**

- Buka pengaturan → **Cadangkan data** → pilih folder, misalnya Downloads atau Documents.
- Gunakan **Bagikan backup** untuk menyalin file ke luar HP. Pilih tujuan sendiri dan pastikan salinannya benar-benar tersimpan.
- Salinan di laptop, flashdisk, atau cloud saat ada internet membantu jika HP hilang/rusak. Salinan yang hanya berada di HP ikut hilang bila HP hilang.
- Untuk pemulihan, pilih **Pulihkan data dari file**, periksa ringkasan, lalu konfirmasi. Catatan identik tidak digandakan. Versi berbeda hanya diganti bila kamu memilih **Gunakan versi backup**.
- Cadangan Catat-Catat dari versi sebelumnya tetap dapat dipakai di Notra. Nama file cadangan baru diawali `notra-cadangan`.

**Uji saat kamu kembali**

1. Coba satu pengeluaran, kemudian dua dan empat pengeluaran campuran.
2. Periksa nama serta harga sebelum simpan. Pengenalan suara dapat salah; kolom hasil memang disediakan untuk koreksi.
3. Perhatikan waktu tunggu setelah selesai bicara. Pengujian pada GT 10 Pro dengan suara kamu belum dilakukan.
4. Coba mode pesawat, tutup/buka kembali aplikasi, dan buat satu backup lalu pulihkan.
5. Coba ketiga pilihan tema. Saat memilih Sistem, ubah tema Android dan periksa apakah Notra ikut berubah.

Jika ingin membaca error melalui komputer: hubungkan HP lewat USB, aktifkan USB debugging, izinkan komputer pada layar HP, kemudian jalankan `read-phone-log.ps1` dari folder proyek. Log khusus voice disimpan di `D:\CatatCatat-tools\phone-voice.log`. Versi ini mencatat durasi audio, jalur CPU, lama pemrosesan, dan error; tidak mencatat isi ucapan pengguna.

APK belum berisi catatan contoh. Contoh dalam tangkapan layar adalah data pengujian emulator.
