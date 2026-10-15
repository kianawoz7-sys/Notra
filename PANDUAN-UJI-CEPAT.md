**Notra Uji — percobaan Small lebih cepat**

APK: `D:\CatatCatat-tools\dist\Notra-v1.1.1-Uji-context512.apk` (~202 MiB). Aplikasi ini sudah terpasang pada HP pengguna sebagai **Notra Uji**. Notra utama versi 1.1.0 tetap ada. Catatan kedua aplikasi terpisah; gunakan Notra Uji untuk percobaan dahulu.

Kandidat tetap memakai Small Q5_1 dan beam 3, dengan konteks audio mengikuti durasi ucapan, pemilihan inti CPU cepat, serta animasi orb yang lebih hemat gambar. Semua pengenalan tetap offline. Hasil suara tetap diperiksa sebelum disimpan.

1. Buka **Notra Uji**. Izinkan mikrofon bila diminta.
2. Coba tempo normal: “cuci dua belas rebu”, “parkir dua ribu”, “langganan ChatGPT seratus enam puluh lima ribu”, lalu “charger tiga puluh ribu, speaker seratus dua puluh ribu”.
3. Tekan Selesai. Periksa kata, nominal, dan waktu tunggunya. Batalkan draft bila hanya untuk menguji.
4. Bandingkan dengan Notra utama pada jarak, suasana, dan jumlah barang yang sama. Jangan menjalankan tes otomatis bersamaan dengan merekam manual.
5. Beri tahu setelah tes selesai agar log dapat dibaca lewat koneksi ADB yang sudah tersedia. Log produksi tidak mencatat teks atau audio pengguna.

Pada delapan contoh sintetis di HP ini, nama dan nominal benar 8/8, median sekitar **6 detik**, rentang **5,5–11,8 detik**. Awal pemakaian bisa lebih lama. **Target 3 detik belum tercapai secara konsisten**, dan hasil sintetis belum membuktikan ketepatan suara/aksen semua orang.

Uji manual pengguna sesudah benchmark sudah teramati: rekaman 3 detik diproses **3,846 detik**, termasuk pemuatan model. Ini mendekati empat detik untuk satu ucapan pendek; ketepatan hasil tetap perlu diperiksa pada layar.

Profil 384 yang lebih agresif tidak dipilih karena salah membaca “cuci” pelan menjadi “cici”. Rincian semua hasil, termasuk kegagalan dan variasi waktu, ada di `EKSPERIMEN-VOICE.md`. Versi utama belum diganti dengan kandidat ini.
