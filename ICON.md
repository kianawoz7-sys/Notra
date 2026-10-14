**Ikon Notra 1.0.4**

Pengguna meminta gambar catatan bertumpuk terbaru dipakai sebagai ikon, dengan semua area di luar objek menjadi transparan. Pengeditan memakai **imagegen bawaan**, mode background-extraction, dengan `transparent_background=true`; bukan CLI/API fallback. Hasil kedua dipilih setelah pemeriksaan alpha dan uji launcher Android.

Aset aplikasi: [notra_logo.png](app/src/main/res/drawable-nodpi/notra_logo.png). Salinan hasil edit: `D:\CatatCatat-tools\icon-v1.0.4\notra-logo-transparent.png`. Salinan untuk pengguna: `D:\CatatCatat-tools\dist\notra-logo-transparent-v1.0.4.png`. PNG RGBA 1254×1254; SHA-256 `c4ef099c037fc467c03d055ba0af3660b473a68987f8663f34dd3d09f5cc9c48`.

Resource launcher memakai PNG langsung. Tidak ada latar kotak/lingkaran yang dipasang oleh aplikasi. Launcher dapat membungkus bitmap dengan bingkai sistemnya; Pixel pada emulator menambahkan bingkai putih. Tampilan launcher Infinix belum diperiksa di HP fisik. [Sumber renderer Android](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/graphics/java/android/graphics/drawable/AdaptiveIconDrawable.java) memperlihatkan bahwa format adaptif menggambar latar hitam sebelum kedua lapisan, sehingga alpha transparan pada lapisan background tidak menghasilkan ikon yang mengambang.

**Prompt edit pertama (verbatim)**

```text
Use case: background-extraction. Asset type: transparent PNG foreground for the Notra Android launcher icon. Image 1 is the exact edit target. Remove ONLY the solid black background outside the stacked-notes object and make all exterior pixels genuinely transparent using alpha. Preserve the original object faithfully: the front white note with its three solid dark charcoal bars, the bent lower-right paper corner, the silver/gray middle notes and the very dark charcoal rear note; retain the original proportions, perspective, soft highlights, internal contact shadows, colors, and smooth rounded edges. The dark rear note and the dark bars are parts of the object and MUST remain opaque, not cut away. No outer black square or circular plate, no new background, no checkerboard baked into pixels, no extra outline, no text, no watermark. Keep the complete object centered on a square transparent canvas with comfortable transparent margins. Do not redesign or stylize the notes. All space outside the physical note stack must be transparent. This is clean background removal from the supplied image.
```

**Prompt perapian final (verbatim)**

```text
Use case: background-extraction. Image 1 is the edit target: a cutout stacked-notes logo with transparency. Perform ONE correction only: clean the alpha boundary and remove EVERY detached white speck, fragment, halo, fringe, stray pixel or faint leftover outside the smooth outline of the physical note stack. Preserve the complete stacked-note object itself, its three dark bars, folded lower-right corner, silver middle sheets, charcoal rear sheet, shading, proportions, and placement unchanged. Output a perfectly clean, continuous anti-aliased silhouette on real transparent alpha. The logo interior should be fully opaque; the front sheet, the dark bars, and dark rear sheet must not become semi-transparent. Keep ALL exterior areas, including the entire surrounding margins, entirely transparent, with no visible debris, no white border, no diffuse outer glow, no square, no backdrop, no added text. Do not make a checkerboard image. This is a precise cleanup of the existing transparent cutout, not a redesign.
```
