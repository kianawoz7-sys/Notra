**Rencana aplikasi Notra — Android offline**

Disusun: 14 Oktober 2026. Nama aplikasi ditetapkan pengguna menjadi **Notra** pada versi 1.0.3, dengan logo catatan bertumpuk dari gambar yang diberikan. Folder proyek dan ID aplikasi lama dipertahankan untuk kompatibilitas pembaruan. Dokumen ini menjadi acuan bersama untuk pengembangan. Pengujian HP sudah dimulai: versi 1.0 memerlukan lebih dari satu menit untuk ucapan enam detik dan mengulang satu pengeluaran Rp15.000 menjadi banyak draft. Pembaruan 1.0.1 menangani pengulangan, batas waktu, pembatalan, dan informasi proses. Rincian pengujian ada di `HASIL-UJI.md`; hasil suara asli dan jalur CPU cepat pada HP masih perlu diuji ulang.

**1. Tujuan dan kebutuhan yang sudah disepakati**

Mencatat semua jenis pengeluaran dengan sedikit mengetik: makanan, minuman, kost, paket data, ngeprint, bensin, dan pengeluaran lainnya. Semua pencatatan dan laporan harus dapat digunakan offline, termasuk pengenalan suara setelah aplikasi dan model terpasang.

- Satu rekaman dapat memuat beberapa pengeluaran; kebiasaan pengguna adalah 1–4 pengeluaran. Empat bukan batas wajib.
- Hasil suara selalu diperiksa sebelum disimpan. Pengguna dapat mengubah nama, nominal, dan tanggal, serta menghapus baris draft.
- Satu tombol **Simpan semua** menyimpan seluruh draft yang valid.
- Input manual tersedia. Catatan tersimpan dapat diedit atau dihapus dengan konfirmasi.
- Laporan menampilkan total harian, mingguan, dan bulanan beserta rincian pengeluaran. Batas anggaran dan pemasukan berada di luar lingkup versi pertama.
- Backup dan pemulihan termasuk fitur versi pertama.

**2. Perangkat dan pilihan teknologi**

Perangkat sasaran pertama: Infinix GT 10 Pro dengan RAM fisik 8 GB. Perencanaan kapasitas memakai RAM fisik tersebut. Dimensity 8050 tercantum pada [spesifikasi resmi Infinix](https://www.infinixmobility.com/GT-10-Pro).

| Bagian | Pilihan awal | Peran |
|---|---|---|
| Aplikasi | Kotlin, Jetpack Compose, Material 3 | Tampilan native Android |
| Penyimpanan | Room di atas SQLite | Catatan pengeluaran lokal |
| Voice | whisper.cpp melalui integrasi native Android | Mengubah rekaman menjadi teks di HP |
| Model suara | Base multilingual sebagai kandidat awal | Mendukung pengujian ucapan bahasa Indonesia |
| Pengurai pengeluaran | Logika Kotlin lokal | Memisahkan nama dan nominal tanpa layanan online |
| Backup | JSON dengan versi format | Menyimpan data yang dapat dipulihkan |
| Pemilihan file | Storage Access Framework Android | Memilih lokasi backup dan file pemulihan |

Referensi teknologi: [Compose](https://developer.android.com/compose), [Room](https://developer.android.com/training/data-storage/room), [contoh Android whisper.cpp](https://github.com/ggml-org/whisper.cpp/tree/master/examples/whisper.android), [model whisper.cpp](https://github.com/ggml-org/whisper.cpp/blob/master/models/README.md), dan [pemilihan file Android](https://developer.android.com/training/data-storage/shared/documents-files).

Pilihan model suara bersifat sementara sampai diuji di HP pengguna. Mulai dengan base multilingual, bandingkan dengan tiny bila waktu tunggu terlalu panjang, dan pertimbangkan model yang lebih akurat bila pengenalan bahasa Indonesia belum memadai. Model yang dipilih disertakan dalam paket instalasi. Ukuran, waktu proses, dan akurasi APK final ditentukan dari pengujian, bukan dari perkiraan RAM saja.

**3. Halaman dan tombol**

Ada dua halaman utama dan pengaturan yang dibuka lewat ikon roda gigi.

| Halaman | Isi dan tombol |
|---|---|
| Catat | Tanggal, bola suara kecil, tombol Isi manual lebar, ikon Laporan dan Pengaturan; total serta catatan dipindah ke Laporan |
| Periksa hasil | Daftar draft nama–nominal, tanggal yang dapat diubah, **Ubah**, **Hapus baris**, **Rekam ulang**, **Batal**, **Simpan semua** |
| Laporan | Pilihan **Hari / Minggu / Bulan**, pemilih periode, total dan rincian pengeluaran |
| Pengaturan | Satu pengaturan **Tema tampilan: Terang / Gelap / Sistem**, **Cadangkan data**, **Pulihkan data**, **Bagikan backup**, waktu backup terakhir dan status perubahan yang belum dicadangkan |

Tema awal mengikuti Sistem. Pilihan tersimpan secara lokal dan menyesuaikan semua halaman. Splash screen berupa animasi native sekitar 1,65 detik: tiga titik berderet, berubah menjadi susunan lingkaran, lalu berputar. Animasi tidak diulang ketika pengguna mengganti tema pada halaman pengaturan.

Catat lewat suara: tekan mikrofon untuk mulai, tekan **Selesai** untuk menghentikan rekaman, tunggu pemrosesan, lalu periksa hasil. Saat memproses, tombol simpan belum tersedia. Jika rekaman gagal atau tidak menghasilkan pengeluaran yang dapat dibaca, pengguna dapat merekam ulang atau beralih ke input manual.

**4. Aturan membaca suara dan menyimpan data**

Contoh dua pengeluaran:

> “Nasi ayam lima belas rebu, es teh lima rebu.”

Hasil draft: Nasi ayam Rp15.000; Es teh Rp5.000; total Rp20.000.

Contoh campuran empat pengeluaran:

> “Bayar kost delapan ratus rebu, paket data lima puluh rebu, ngeprint lima rebu, bensin tiga puluh rebu.”

Hasil draft: Rp800.000, Rp50.000, Rp5.000, dan Rp30.000; total Rp885.000.

- Nama pengeluaran bebas dan tidak bergantung pada daftar nama makanan atau barang.
- Normalisasi mendukung angka tertulis maupun angka hasil transkripsi, termasuk variasi ribu/rebu/rb dan juta.
- Nominal atau pemisahan pengeluaran yang ambigu ditandai untuk dikoreksi. Tidak otomatis menganggap angka tanpa satuan sebagai ribuan.
- Draft hanya dapat disimpan jika nama terisi dan nominal valid lebih dari nol. Seluruh hasil tetap diperiksa pengguna, karena kesalahan transkripsi bisa menghasilkan teks yang terlihat valid.
- Tanggal default adalah hari ini menurut waktu lokal HP dan dapat diubah, misalnya untuk pengeluaran kemarin.
- Setiap pengeluaran memiliki ID unik, nama, nominal bilangan bulat dalam rupiah, tanggal pengeluaran, waktu dibuat/diubah, dan penanda penghapusan untuk kebutuhan pemulihan. Rekaman audio bersifat sementara dan tidak termasuk backup.
- Simpan semua dilakukan dalam satu transaksi penyimpanan. Tombol dinonaktifkan saat menyimpan untuk mencegah pengeluaran ganda akibat ketukan berulang.

**5. Aturan laporan**

- Harian: satu tanggal yang dipilih.
- Mingguan: Senin sampai Minggu.
- Bulanan: bulan kalender, misalnya 1–31 Oktober.
- Total dihitung dari tanggal pengeluaran, bukan waktu pembuatan catatan. Bayar kost dihitung pada tanggal pembayaran.
- Perubahan nominal, tanggal, dan penghapusan langsung memperbarui total serta rincian. Catatan yang dihapus tidak dihitung.
- Semua nilai ditampilkan dalam rupiah. Pilihan tanggal tetap memakai tanggal lokal yang dicatat supaya catatan lama tidak bergeser akibat perubahan zona waktu HP.

**6. Backup dan pemulihan**

Cadangkan data membuat file seperti `catat-catat-backup-2026-10-14-203000.json`. File berisi versi format, waktu pembuatan backup, seluruh catatan beserta ID dan statusnya, serta pengaturan yang diperlukan. Model suara tidak ikut dicadangkan.

- Pengguna memilih lokasi file melalui pemilih file Android. File backup lokal diletakkan di luar direktori internal aplikasi agar tetap tersedia setelah aplikasi dihapus.
- Setelah file berhasil ditulis, tampilkan jumlah catatan, lokasi, dan pilihan **Bagikan backup**. Pembatalan atau kegagalan penulisan tidak ditandai sebagai backup berhasil.
- Status **Backup terakhir dibuat** hanya menyatakan keberhasilan pembuatan file. Aplikasi tidak mengklaim file sudah tersalin ke Drive hanya karena menu berbagi dibuka.
- Tampilkan penanda bila ada penambahan, pengeditan, atau penghapusan setelah backup terakhir.
- Untuk menghadapi HP hilang atau rusak, pengguna perlu menyalin backup ke luar HP: laptop, flashdisk, atau penyimpanan cloud saat ada internet. Backup lokal dan pemulihan lokal tetap berjalan offline.

Pulihkan data membuka pemilih file, memvalidasi format dan isi, lalu menampilkan tanggal backup, jumlah catatan, dan perubahan yang akan diterapkan sebelum konfirmasi pengguna.

- Pemulihan menggabungkan berdasarkan ID unik, bukan kesamaan nama/harga. Dua pembelian yang memang berbeda tetap menjadi dua catatan.
- ID dan isi yang identik dilewati agar pemulihan berulang tidak menggandakan data.
- Bila ID sama tetapi isi atau status penghapusan berbeda, tampilkan konflik dan minta pilihan mempertahankan versi sekarang atau memakai versi backup. Catatan yang dihapus tidak diam-diam dimunculkan kembali.
- Buat cadangan pengaman data sekarang sebelum menerapkan pemulihan. Terapkan hasil pemulihan dalam satu transaksi.
- File rusak, format tidak didukung, atau kegagalan proses tidak boleh meninggalkan perubahan sebagian pada database.

**7. Tahapan pengerjaan dan hasil tiap tahap**

1. **Pemeriksaan lingkungan dan percobaan voice.** Periksa Android SDK/NDK dan alat build, lalu siapkan APK percobaan rekam–transkripsi offline. Pengguna memasang APK di GT 10 Pro untuk membandingkan ketepatan nama/nominal dan waktu proses dengan ucapan sehari-hari. Hasil: pilihan model dan catatan hasil pengujian perangkat.
2. **Pencatatan dan penyimpanan.** Buat halaman Catat, input manual, draft pemeriksaan, edit/hapus, pengurai nominal, dan database. Hasil: seluruh alur pencatatan dapat digunakan dan catatan bertahan setelah aplikasi ditutup.
3. **Integrasi suara dan laporan.** Hubungkan transkripsi ke beberapa draft pengeluaran, tambahkan status rekaman/pemrosesan dan laporan periode. Hasil: alur suara sampai laporan berjalan lengkap.
4. **Backup dan pemulihan.** Tambahkan ekspor, bagikan, impor tervalidasi, penanganan konflik, dan status backup. Hasil: data dapat dipindahkan ke instalasi baru tanpa duplikasi atau perubahan diam-diam.
5. **Pengujian akhir dan APK.** Uji dalam mode pesawat, periksa ketepatan total, dan coba pemulihan pada instalasi terpisah tanpa menghapus data utama pengguna. Hasil: APK versi pertama beserta petunjuk instalasi, penggunaan, dan backup.

**8. Patokan selesai untuk versi pertama**

- Suara, input manual, catatan, laporan, backup lokal, dan pemulihan lokal dapat digunakan dalam mode pesawat setelah paket lengkap terpasang.
- Pengguna dapat menyebut 1–4 pengeluaran campuran dalam satu rekaman dan selalu melihat hasil sebelum menyimpan.
- Uji pengurai memeriksa variasi nominal, pemisahan beberapa pengeluaran, dan input ambigu. Kecepatan serta kualitas pengenalan suara dinilai langsung di GT 10 Pro.
- Simpan, edit, hapus, dan pindah tanggal menghasilkan total yang benar, termasuk batas minggu, pergantian bulan, dan pergantian tahun.
- Catatan bertahan setelah aplikasi ditutup dan HP di-restart.
- Backup dapat dipulihkan pada instalasi baru; ID, nominal, tanggal, dan status catatan tetap benar. Pemulihan berulang tidak menggandakan data.
- Uji backup mencakup file rusak, versi tidak didukung, konflik edit/hapus, pembatalan, dan kegagalan proses tanpa merusak data sekarang.

Keputusan lanjutan yang mengikuti hasil uji: model voice final, ukuran APK, dan waktu pemrosesan yang nyaman. Pengujian langsung di HP memerlukan pengguna memasang APK; hasil perangkat tersebut belum tersedia pada tahap perencanaan.

**Pembaruan 14 Oktober 2026: pengguna meminta implementasi sampai APK siap diuji.**

- Semua unduhan, instalasi tambahan, cache, dan output build harus ditempatkan di drive D. Sumber aplikasi tetap di folder proyek ini.
- Desain voice mengikuti dua referensi pengguna: latar terang, bola hitam-putih dengan batas seperti kabut, lebih besar dan bergerak saat merekam atau memproses, serta kontrol selesai/batal yang mudah dijangkau.
- Pengguna sedang mengerjakan tugas lain; pengembangan tidak menunggu pengujian HP. Siapkan fitur lengkap, pengujian yang dapat dilakukan di komputer, APK, dan panduan. Pengujian suara serta waktu proses di GT 10 Pro dilakukan pengguna setelah kembali.
- Kandidat model awal yang dipaketkan: base multilingual q5_1, untuk mengurangi ukuran paket sambil tetap memakai model base. Hasil uji perangkat menjadi dasar penyesuaian berikutnya.

**Pembaruan UI 1.0.2 — 14 Oktober 2026.** Halaman utama dan layar voice dibuat bersih mengikuti referensi bola kecil yang membesar saat ditekan. Hilangkan branding, slogan, petunjuk, label offline, total, dan daftar catatan dari halaman utama. Tanggal dipertahankan; Isi manual menjadi tombol kaca gelap selebar halaman. Laporan dibuka melalui ikon kiri atas dan dimulai dari hari ini. Aturan suara, penyimpanan, laporan, serta backup tidak berubah.
