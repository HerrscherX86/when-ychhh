<p align="center">
  <img src="docs/assets/logo.png" alt="Logo When Yah" width="140" />
</p>

<h1 align="center">When Yah</h1>

<p align="center"><strong>Wishlist dan tracker tabungan offline — cuma IDR.</strong></p>

<p align="center">When Yah membantu kamu mencatat barang yang ingin dibeli, menyisihkan uang untuk tiap barang, membuat rencana nabung, dan melihat progresnya. Semuanya tanpa akun, tanpa internet, dan tanpa menghubungkan rekening bank.</p>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&amp;logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/min%20SDK-31-3DDC84" alt="Min SDK 31" />
  <img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&amp;logoColor=white" alt="Kotlin 2.2.10" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4" alt="Jetpack Compose Material 3" />
  <img src="https://img.shields.io/badge/status-initial%20release-orange" alt="Status initial release" />
</p>

> When Yah **tidak** menahan, memindahkan, atau memverifikasi uang sungguhan. Aplikasi ini cuma mencatat rencana dan riwayat tabunganmu.

---

## 🧭 Alur singkat

1. **Tambah barang** — isi nama, harga target, dan opsional gambar, URL, catatan, serta kategori.
2. **Catat setoran** — simpan uang yang benar-benar kamu sisihkan untuk barang itu.
3. **Buat rencana** — pilih nominal dan frekuensi harian/mingguan/bulanan untuk melihat estimasi kapan target tercapai.
4. **Pantau progres** — lihat ringkasan di Home, aktivitas terbaru, dan barang yang paling dekat dengan target.
5. **Tandai dibeli** — barang pindah ke arsip, riwayat tabungan tetap tersimpan.
6. **Ekspor backup** — simpan ZIP berisi data dan gambar ke lokasi pilihanmu.

---

## 📸 Screenshot

<!-- Ganti file di docs/screenshots/ dengan screenshot asli. Rasio 9:20, lebar 540 px. -->

<table align="center">
  <tr>
    <td align="center" width="50%">
      <img src="docs/screenshots/home.png" alt="Home" width="280" /><br />
      <sub><b>Home</b></sub>
    </td>
    <td align="center" width="50%">
      <img src="docs/screenshots/item-detail.png" alt="Detail item" width="280" /><br />
      <sub><b>Detail item</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <img src="docs/screenshots/savings-plan.png" alt="Rencana nabung" width="280" /><br />
      <sub><b>Rencana nabung</b></sub>
    </td>
    <td align="center" width="50%">
      <img src="docs/screenshots/settings.png" alt="Tampilan dan bahasa" width="280" /><br />
      <sub><b>Tampilan &amp; bahasa</b></sub>
    </td>
  </tr>
</table>

---

## ✨ Fitur

### Wishlist dan koleksi
- Tambah barang dengan nama, harga target, gambar lokal, URL pembelian, catatan, dan kategori.
- Kelompokkan barang ke koleksi buatan sendiri. Satu barang maksimal masuk ke satu koleksi.
- Cari dan urutkan barang di Home.
- Halaman detail barang dengan progres tabungan, rencana, dan riwayat transaksi.

### Tabungan per barang
- Catat setoran dan penarikan manual per barang.
- Saldo selalu dihitung dari ledger transaksi, bukan disimpan sebagai angka terpisah.
- Penarikan biasa tidak bisa membuat saldo minus.
- Progres dibatasi 100% untuk tampilan, tapi kelebihan saldo tetap disimpan.

### Rencana nabung
- Kontribusi tetap harian, mingguan, atau bulanan.
- Estimasi jumlah kontribusi dan tanggal tercapainya target.
- Rencana hanya berupa proyeksi — tidak pernah otomatis membuat transaksi atau memotong saldo.
- Recurrence bulanan tetap memakai tanggal awal dan otomatis menyesuaikan akhir bulan.

### Dashboard
- Ringkasan total target, total tersimpan, dan jumlah barang yang sudah tercapai.
- Aktivitas terbaru, rencana terdekat, dan barang yang paling dekat dengan target.
- Daftar semua barang aktif dengan pencarian dan filter.

### Pembelian dan arsip
- Tandai barang sebagai dibeli: seluruh saldo tersedia dicatat sebagai `PURCHASE_SPEND`, lalu barang dipindahkan ke arsip.
- Riwayat transaksi tetap utuh setelah dibeli.
- Barang yang sudah dibeli tidak lagi muncul di total aktif dan tidak lagi punya reminder.

### Backup dan restore
- Ekspor backup ke file ZIP versioned berisi `manifest.json`, `data.json`, dan folder `images/`.
- Manifest menyimpan versi format/schema, ukuran, dan checksum SHA-256.
- Restore memvalidasi manifest, schema, checksum, relasi data, dan ukuran file sebelum mengganti data.
- Proteksi zip-slip dan batas ukuran entri.
- Restore bersifat **full replacement**, bukan merge.

### Notifikasi (opsional)
- Reminder kontribusi per rencana sesuai jam lokal.
- Notifikasi saat target benar-benar tercapai dari transaksi nyata.
- Ringkasan progres mingguan atau bulanan.
- Memakai `AlarmManager`: exact alarm kalau izin “Alarms & reminders” diberikan, fallback inexact yang tetap aman saat Doze kalau tidak.
- Reminder didaftarkan ulang otomatis setelah reboot, update aplikasi, dan saat izin exact alarm diberikan.

### Tampilan dan bahasa
- Material 3 dengan dynamic color.
- Mode terang, gelap, atau mengikuti sistem.
- Pilihan warna aksen.
- Bahasa Indonesia (register Gaul) dan English, bisa diganti kapan saja.
- Onboarding tiga halaman saat pertama kali membuka aplikasi.

### Yang sengaja tidak ada
- Integrasi bank, pembayaran, atau verifikasi transaksi.
- Akun, sinkronisasi lintas perangkat, atau backend.
- Scraping harga atau notifikasi harga otomatis.
- Dukungan multi-currency atau konversi FX.

---

## 💰 Aturan uang yang dijaga

When Yah memakai aturan akuntansi yang ketat supaya angka tabungan tetap konsisten:

| Aturan | Implementasi |
|---|---|
| Mata uang | IDR saja, tanpa kurs atau konversi |
| Tipe angka | Rupiah bulat disimpan sebagai `Long` / SQLite `INTEGER`, bukan `Double`/`Float` |
| Saldo | Selalu diturunkan dari ledger transaksi |
| Ledger | Append-only: `DEPOSIT` (+), `WITHDRAWAL` (−), `PURCHASE_SPEND` (−) |
| Rencana | Proyeksi saja; tidak pernah menulis transaksi |
| Pembelian | Satu `PURCHASE_SPEND` untuk seluruh saldo tersedia; riwayat tetap disimpan |
| Koleksi | Satu barang maksimal satu koleksi; hapus koleksi hanya melepas barangnya |
| Progres | Ditampilkan maksimal 100%, surplus tetap dipertahankan |

---

## 🧱 Tech stack

| Bagian | Teknologi |
|---|---|
| Bahasa | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Navigasi | Navigation Compose |
| Arsitektur | MVVM + repository + domain calculators |
| Database | Room |
| Dependency injection | Hilt |
| Preferensi | Preferences DataStore |
| Gambar | Coil |
| Backup | kotlinx.serialization + ZIP + SHA-256 |
| Notifikasi | AlarmManager + BroadcastReceiver |
| Build | Gradle 9.4.1, AGP 9.2.1, KSP |
| Minimum | Android 12 (API 31) |
| Target | Android 16 (API 36) |

---

## 📱 Requirement

- Android 12 (API 31) atau lebih baru di perangkat.
- JDK 17 atau lebih baru untuk build.
- Android SDK Platform 37 (`compileSdk = 37`).
- Android Studio versi terbaru yang mendukung AGP 9.2.1, atau build lewat CLI.

`local.properties` berisi path SDK dan bersifat machine-specific (tidak ikut di-commit). Android Studio akan membuatnya otomatis.

---

## 🚀 Build dari source

```bash
git clone https://github.com/HerrscherX86/when-ychhh.git
cd when-ychhh
./gradlew :app:assembleDebug
```

Di Windows, ganti `./gradlew` dengan `gradlew.bat`.

APK debug ada di:

```
app/build/outputs/apk/debug/app-debug.apk
```

Untuk memasang ke perangkat yang terhubung:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Test dan pemeriksaan

```bash
./gradlew :app:testDebugUnitTest     # unit test host-side
./gradlew :app:lintDebug             # Android Lint
./gradlew :app:connectedDebugAndroidTest   # butuh device/emulator
```

Status saat ini:

- **64 unit test** lulus.
- **0 error** di Android Lint.
- `:app:assembleDebug`, `:app:testDebugUnitTest`, dan `:app:lintDebug` semuanya hijau.

Logika saldo, proyeksi rencana, pembelian, agregasi dashboard, validasi backup, dan jadwal notifikasi sengaja ditaruh di `domain/` supaya bisa diuji tanpa UI.

---

## 🗂️ Struktur proyek

```
app/src/main/java/id/my/f1zz/whenyah/
├── data/
│   ├── local/          # Room: entity, DAO, database, converter
│   ├── repository/     # wishlist, tabungan, rencana, pembelian
│   ├── notification/   # AlarmManager scheduler, receiver, NotificationService
│   ├── backup/         # export/import ZIP + validasi
│   └── settings/       # DataStore
├── domain/
│   ├── savings/        # kalkulasi saldo dan progres
│   ├── plan/           # proyeksi rencana nabung
│   ├── dashboard/      # agregasi dashboard
│   ├── notifications/  # perhitungan jadwal berikutnya
│   ├── validation/     # validasi input dan URL
│   └── i18n/           # AppStrings (English + Indonesia)
├── ui/
│   ├── home/ collections/ item/ archive/ settings/ onboarding/
│   ├── navigation/
│   ├── components/
│   └── theme/
└── di/                 # modul Hilt
```

---

## 🏗️ Arsitektur singkat

- Single-module Android app (`:app`) dengan Jetpack Compose.
- UI memanggil ViewModel; ViewModel memakai repository; repository memakai Room/DataStore.
- Semua perhitungan uang dan jadwal ada di `domain/` dan diuji lewat unit test.
- Saldo tidak pernah disimpan sebagai field mutable — selalu hasil turunan dari ledger.
- Backup/restore memvalidasi seluruh arsip sebelum menyentuh database, lalu mengganti semua tabel dalam satu transaksi.
- Notifikasi memakai `AlarmManager` supaya reminder tetap jalan saat aplikasi ditutup. Kebijakan battery saver OEM tetap bisa menunda pengiriman.

Dokumen sumber kebenaran produk ada di [`wishlist_savings_tracker_PRD_v1.2.md`](wishlist_savings_tracker_PRD_v1.2.md). Baca §2 (keputusan terkunci), §6 (invariant akuntansi), dan §15 (open decisions) sebelum mengubah perilaku.

---

## 🔐 Privasi

- Tidak ada akun, login, backend, analytics, atau iklan.
- Aplikasi **tidak meminta izin internet**.
- Semua data tersimpan di penyimpanan privat aplikasi.
- Backup hanya keluar kalau kamu memilih lokasinya sendiri lewat file picker Android.
- File backup tidak dienkripsi, jadi simpan di lokasi yang aman.
- Gambar disalin ke penyimpanan privat aplikasi; database hanya menyimpan path relatifnya.

---

## 📌 Status

Versi **1.0** — initial release untuk pemakaian pribadi. Belum tersedia di Google Play.

Yang masih mungkin dikerjakan:

- Publikasi ke Google Play.
- Pengujian notifikasi di lebih banyak perangkat/ROM, terutama vendor dengan battery saver agresif.
- Panduan tambahan di dalam aplikasi untuk auto-start/background activity per vendor.
- Penyempurnaan animasi dan aksesibilitas.

---

## 🤝 Kontribusi

Issue dan pull request dipersilakan. Sebelum mengubah perilaku produk:

1. Baca PRD, terutama keputusan terkunci dan invariant akuntansi.
2. Jaga uang tetap `Long`/`INTEGER` dalam IDR — jangan pakai `Double`/`Float`.
3. Jalankan `./gradlew :app:testDebugUnitTest` dan `./gradlew :app:lintDebug`.
4. Tambahkan unit test untuk logika domain baru.
5. Jangan menambahkan dependency baru tanpa alasan yang jelas.

---

## 📄 Lisensi

Lisensi open-source untuk repositori ini belum ditetapkan.
