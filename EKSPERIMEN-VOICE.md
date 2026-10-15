**Percobaan latensi Small — 15 Oktober 2026**

Pengguna menyukai hasil Small, tetapi ingin waktu dari Selesai ke draft sekitar 3–4 detik untuk ucapan pendek. Log HP Infinix GT 10 Pro pada APK 1.1.0 menunjukkan 12,814 dan 14,312 detik; rekaman kedua 7,2 detik. Pemetaan native pada proses HP serta checksum APK memastikan `libcatatvoice_fast.so` aktif. Angka ini adalah pengukuran HP, berbeda dari hasil emulator 4 detik sebelumnya.

APK eksperimen memakai **Notra Uji**, ID `id.catatcatat.voicetest`, penyimpanan/database sendiri, serta model Small yang sama. Notra utama `id.catatcatat` tidak diganti. Versi sumber 1.1.1 menggunakan versionCode 7; APK 1.1.0 di D dipertahankan sebagai arsip. Build utama tetap memakai profil QUALITY; Gradle menolak profil cepat tanpa flag eksperimen.

| Profil | Konteks encoder | Decoder |
|---|---|---|
| `quality` | Penuh, 1.500 frame / 30 detik | Beam 3 |
| `context512` | Minimum 512 frame / 10,24 detik | Beam 3 |
| `context384` | Minimum 384 frame / 7,68 detik | Beam 3 |
| `fast384` | Minimum 384 frame / 7,68 detik | Greedy 1 |

Konteks eksperimen bertambah mengikuti **seluruh durasi audio yang sudah diproses + padding 2 detik**, dibulatkan ke kelipatan 128 frame. Audio lebih panjang tidak dipotong agar selalu muat di jendela pendek. Saat mencapai ukuran model, gunakan konteks penuh kembali. Ini tetap eksperimen: perubahan attention dapat menurunkan akurasi walaupun semua audio masuk. Tidak ada koreksi paksa nama/nominal.

Setelah pengukuran awal, thread komputasi terlihat dijadwalkan juga pada inti berkapasitas 227, sedangkan cluster besar HP melaporkan kapasitas 876–1.024. Kandidat berikutnya memakai `InferenceCpuScope` pada profil eksperimen: baca kapasitas CPU yang diizinkan, pilih maksimal empat inti berkapasitas tinggi, dan sesuaikan jumlah thread. Mask diwariskan thread native yang dibuat selama komputasi, lalu affinity worker pemanggil dipulihkan sebelum kembali ke coroutine pool. Bila antarmuka tidak tersedia, akses ditolak, atau CPU homogen, penjadwalan semula tetap digunakan. QUALITY tetap menggunakan kebijakan scheduler semula. Karena ini perubahan tambahan, hasil putaran tanpa affinity dan putaran dengan affinity dicatat terpisah.

`VoiceProfileBenchmarkTest` hanya berjalan pada aplikasi eksperimen. Aktivitas uji berada di depan untuk mendekati kebijakan CPU saat dipakai pengguna. Model dipanaskan sekali; delapan fixture sintetis kemudian dijalankan dengan urutan profil diputar: cuci normal/cepat/pelan, parkir, ChatGPT, charger/speaker, empat pengeluaran, dan makan 15 ribu berdurasi enam detik. Nama, nominal, teks/error, dan waktu setiap kombinasi dicatat. Tes selesai bukan berarti semua pengenalan benar; laporan harus menghitung kesalahan.

Pengukuran native baru mencatat total waktu komputasi, rata-rata encoder, rata-rata per token decoder, serta rata-rata batch prompt. Field upstream adalah rata-rata per run, sehingga `decoder_token_avg_ms` tidak boleh disebut sebagai total waktu decoder. Log produksi tidak menyimpan teks atau audio pengguna. Fixture tes hanya berada di APK pengujian.

```powershell
# Seluruh file build/output tetap di D.
.\build.ps1 -Experiment -VoiceProfile context512 -Tasks @('testDebugUnitTest','assembleDebug','assembleDebugAndroidTest','lintDebug')

# Jalankan benchmark pada perangkat yang dipilih (ID dari adb devices):
.\test-device.ps1 -Serial <serial> -Experiment -VoiceProfile context512 -TestClass id.catatcatat.VoiceProfileBenchmarkTest

# Jalankan sambil menangkap log NotraProfileBench/CatatVoice ke D.
# Ringkasan harus mempertahankan semua hasil salah dan kegagalan:
python .\tools\summarize_voice_profiles.py D:\CatatCatat-tools\profile-phone-live.log --output D:\CatatCatat-tools\dist\Notra-profil-voice.json
```

Pemilihan kandidat mempertimbangkan waktu dan ketepatan pada rekaman yang sama, bukan satu hasil tercepat. Hasil sintetis di HP belum membuktikan akurasi suara pengguna. Kandidat tetap harus diuji pengguna sebelum menggantikan Notra utama atau dibagikan sebagai versi stabil.

**Hasil dan kandidat yang dipilih**

Seluruh pengukuran berikut dilakukan pada HP Infinix pengguna, ARM64 dengan `catatvoice_fast`, melalui wireless ADB. APK tidak memiliki izin internet; Wi-Fi tetap aktif untuk ADB. Ini bukan pengujian mode pesawat. Tidak ada rekaman mikrofon dalam fixture; suara sintetis dibuat sebelumnya di PC.

| Putaran awal, penjadwalan CPU semula | Nama + nominal benar | Median | Rentang |
|---|---|---|---|
| QUALITY | 8/8 | 33,45 dtk | 28,04–36,77 dtk |
| context512 | 8/8 | 8,46 dtk | 7,63–13,30 dtk |
| context384 | 7/8 | 7,86 dtk | 5,82–10,48 dtk |
| fast384 | 7/8 | 6,78 dtk | 5,78–12,35 dtk |

Kedua profil 384 salah membaca `cuci-quiet` sebagai **Cici** meskipun nominal tetap Rp12.000. Profil 512 dipilih untuk kandidat. QUALITY pada tes sintetis ini lebih lambat daripada log penggunaan nyata 1.1.0 (12,8–14,3 detik); keduanya berasal dari sesi, input, dan kondisi penjadwalan/beban yang berbeda. Jangan mengganti patokan penggunaan nyata dengan angka 33 detik atau mengklaim rasio percepatan pasti dari kedua sesi.

Satu pemanasan awal memakai QUALITY menyentuh batas 45 detik sebelum perbandingan dimulai. Pemanasan kemudian memakai context384 terlebih dahulu; kegagalan awal disimpan dalam `profile-phone-cold-failed*`. Putaran pertama dengan affinity sempat bercampur dengan rekaman manual; hasilnya disimpan sebagai `profile-phone-cores-mixed*` dan tidak dipakai untuk kesimpulan latensi.

Kandidat akhir menambahkan pemilihan inti cepat dan mengurangi pembaruan gambar kabut orb menjadi sekitar 20/s saat aktif dan 8/s saat idle. Bentuk, warna, periode gerak, respons level, dan pengaturan durasi animasi tetap menggunakan mekanisme animasi semula. Panel yang menyerap sentuhan hanya ada dalam APK tes agar mikrofon manual tidak ikut mengantre selama benchmark.

| Kandidat context512, putaran akhir | Waktu sampai hasil terurai | Nama + nominal |
|---|---|---|
| Cuci normal | 5,728 dtk | Benar |
| Cuci +20% | 11,841 dtk | Benar |
| Cuci amplitudo 3% | 6,345 dtk | Benar |
| Parkir Rp2.000 | 5,467 dtk | Benar |
| Langganan ChatGPT Rp165.000 | 5,804 dtk | Benar |
| Charger + speaker | 6,109 dtk | Benar |
| Empat pengeluaran campuran | 6,732 dtk | Benar |
| Makan Rp15.000, PCM enam detik | 5,822 dtk | Benar |

Median akhir **5,966 detik**, rentang **5,467–11,841 detik**, benar **8/8** contoh. Kasus 11,841 detik tetap dihitung; log mencatat encoder 10,136 detik pada kasus tersebut. Penyebab variasi durasi belum dipastikan. Mask CPU `f0` dan empat thread terverifikasi pada profil cepat. Pemanasan terakhir 8,094 detik (termasuk pemuatan konteks native), sehingga awal pemakaian juga dapat lebih lama. **Target 3 detik belum tercapai secara konsisten.**

Kandidat akhir lulus **65 tes unit**, **12 tes integrasi Android pada HP**, serta lint **0 error / 7 warning**. Tes Android mencakup satu/dua/empat pengeluaran, rebu, enam detik tanpa duplikasi, hening, penghentian native saat timeout/batal dan transkripsi sesudahnya, transaksi, penyimpanan, serta backup. Satu warning tambahan hanya resource `app_name` yang tidak dipakai karena label launcher eksperimen terpisah.

APK akhir: `D:\CatatCatat-tools\dist\Notra-v1.1.1-Uji-context512.apk`, **211.856.571 byte (~202,04 MiB)**, SHA-256 `0e3738fcb7bbec51218b98b80d7e6f12804d3ae15902d04295c87f13d2e0bbbe`. Model tetap Small Q5_1 dengan SHA-256 semula, satu model dan dua ABI. Signature v2, sertifikat lama, dan alignment 16 KB valid; fixture tidak masuk APK pengguna. Metadata serta laporan lengkap ada di `Notra-v1.1.1-Uji-verifikasi.json` dalam dist.

Notra utama pada HP terverifikasi masih versionCode 6 / versi 1.1.0 dengan waktu pembaruan semula. Hasil pengeluaran uji berada di penyimpanan Notra Uji sendiri. Kandidat sudah dipasang terpisah dan dibuka untuk uji suara pengguna setelah semua tes otomatis selesai.

**Observasi suara pengguna setelah benchmark**

Log Notra Uji 12:37:00–12:37:03 merekam input MIC 3,0 detik, profile context512 / beam3 / mask f0. Pemrosesan tercatat **3.846 ms**, termasuk pemuatan konteks native **266 ms**; komputasi native **3.499 ms**, rata-rata encoder **2.592 ms**. Ini observasi suara nyata pada HP, bukan fixture sintetis, dan menunjukkan sekitar empat detik mungkin untuk satu ucapan pendek pada kondisi tersebut. Isi teks tidak dicatat oleh log produksi, sehingga ketepatan nama/nominal harus dinilai pengguna. Satu observasi ini tidak menghapus variasi 5,5–11,8 detik pada pengujian sintetis atau menjamin tiga detik pada semua ucapan/HP.
