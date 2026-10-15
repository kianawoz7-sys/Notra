**Notra siap dicoba di Android**

APK upgrade: `D:\CatatCatat-tools\dist\Notra-v1.1.0-Small.apk`. Model suara ditingkatkan dari Base ke **Small multilingual Q5_1**, dengan decoder lebih teliti dan penyesuaian volume rekaman pelan. Model sudah ada di APK; HP tetap dapat dipakai offline. Pasang sebagai pembaruan tanpa mencopot Notra, agar catatan tetap ada. Sertifikat dan ID aplikasi tetap sama. Ikon, tema, laporan, dan cadangan dipertahankan.

Model Small sendiri sekitar 190 MB; APK lengkap sekitar 204 MiB (214 MB), dan kebutuhan penyimpanan/RAM bertambah. Pemakaian pertama menyalin serta memeriksa model, sehingga dapat lebih lama. Model lebih besar masih dapat salah mengenali ucapan dan belum menjamin tempo tertentu di GT 10 Pro.

**Membandingkan Small dengan Base**

1. Buat cadangan catatan dahulu melalui Pengaturan.
2. Pasang APK Small sebagai pembaruan. Ucapkan dengan tempo normal: “cuci dua belas ribu”, “parkir dua ribu”, “langganan ChatGPT seratus enam puluh lima ribu”, dan “charger tiga puluh ribu, speaker seratus dua puluh ribu”. Periksa nama, nominal, dan waktu proses; batalkan draft uji bila tidak ingin menyimpannya.
3. Ulangi di tempat/jarak HP yang sama memakai `Notra-v1.1.0-Base-pembanding.apk`. Paket ini memakai pemrosesan rekaman dan decoder yang sama dengan Small agar model dapat dibandingkan.
4. Kedua APK memakai versionCode 6 dan kunci yang sama, sehingga bisa dipasang bergantian tanpa uninstall. Versi lama 77 MB tetap bisa disimpan sebagai arsip; pemasangan langsung versi 1.0.4 di atas 1.1.0 dapat ditolak Android sebagai downgrade. Gunakan APK Base pembanding bila ingin kembali ke model ringan.
5. Catat kata/nominal yang salah beserta lama proses. Uji sintetis di emulator tidak menggantikan pengujian suara asli kamu.

Pada enam contoh sintetis yang sama, Small benar 6/6 dan Base 4/6. Base juga gagal satu dari 12 tes Android karena salah membaca nominal kedua. Paket Base disediakan untuk eksperimen perbandingan. Hasil lengkap, termasuk kesalahan, ada di `HASIL-UJI.md`.

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
