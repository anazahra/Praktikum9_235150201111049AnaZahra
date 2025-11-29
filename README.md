# CameraX – Izin, Penyimpanan MediaStore, dan Rotasi 

Proyek ini merupakan implementasi sederhana CameraX yang menampilkan alur lengkap pengambilan foto, mulai dari permintaan izin kamera, pengelolaan penyimpanan melalui MediaStore, hingga penanganan rotasi gambar menggunakan metadata EXIF.

---

## 1. Alur Izin Kamera

Aplikasi menggunakan izin **CAMERA** untuk menampilkan preview dan mengambil foto.
Jika izin ditolak, preview tidak dapat ditampilkan sehingga aplikasi menampilkan UI fallback berupa pesan yang menjelaskan bahwa akses kamera diperlukan.

* Aplikasi menangani tiga kondisi:
  * **Belum diberikan izin** → tampil dialog sistem.
  * **Ditolak sementara** → bisa meminta ulang.

Pendekatan ini memastikan UX tetap ramah dan transparan saat izin kamera tidak diberikan oleh pengguna.

---

## 2. Penyimpanan Foto Menggunakan MediaStore

Aplikasi menyimpan foto ke folder:

```
Pictures/KameraKu/
```

Penyimpanan menggunakan **Android MediaStore**.

Keuntungan menggunakan:
* Aplikasi bisa menulis file media tanpa meminta akses ke seluruh penyimpanan pengguna.
* Foto otomatis muncul di Galeri.
* Aman dan sesuai standar praktik Android modern.

---

## 3. Penanganan Rotasi Gambar (EXIF Orientation)

Sensor kamera tidak selalu mengikuti orientasi perangkat saat pengambilan gambar.
Karena itu, aplikasi menambahkan metadata **EXIF orientation** agar gambar ditampilkan dengan rotasi yang benar di Galeri maupun aplikasi lain.

Peran EXIF:
* Menyimpan informasi rotasi 0°, 90°, 180°, atau 270°.
* Viewer membaca metadata ini lalu menampilkan gambar dengan orientasi yang sesuai.
* Mencegah kasus foto tampak terbalik atau miring meskipun saat memotret terlihat normal.
