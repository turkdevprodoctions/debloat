<div align="center">

<img src="https://turkdev-photourl-n45o.vercel.app/i/de9319fe7ed7404f.png" alt="Debloat" width="120" height="120" style="border-radius: 24px;">

# Debloat

**Android sistem uygulamalarını Shizuku ile güvenle kaldır**

[![VirusTotal](https://img.shields.io/badge/VirusTotal-Clean-brightgreen)](https://www.virustotal.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-1.0.0-purple.svg)]()

[🇹🇷 Türkçe](#-türkçe) • [🇬🇧 English](#-english)

</div>

---

## 🇹🇷 Türkçe

### 📱 Debloat Nedir?

**Debloat**, Android cihazınızdaki gereksiz sistem uygulamalarını ve önceden yüklenmiş bloatware'i **Shizuku** altyapısıyla **root olmadan** güvenle kaldırmanızı sağlayan açık kaynak bir araçtır.

**Geliştirici:** DevSoft Inc.
**Lisans:** MIT (Açık Kaynak)

---

### ✨ Özellikler

- ⬇️ **Uygulama Sil** — Sistem ve kullanıcı uygulamalarını kalıcı olarak kaldır
- ❄️ **Dondur** — Silmeden devre dışı bırak, istediğin zaman geri aç
- ⚠️ **Kritik Uyarı** — Kritik sistem uygulamalarında çift onay ister
- ⓘ **Detaylı Bilgi** — Sürüm, izinler, aktivite sayısı, boyut, tarihler
- ⌕ **Hızlı Arama** — Yüzlerce uygulama arasında anında filtre
- ⚡ **Zorla Durdur** — Çalışan uygulamayı anında kapat
- 🧹 **Veri Temizle** — Uygulamanın tüm verisini sil
- 🔍 **Manifest Okuyucu** — Uygulama izinlerini detaylı incele
- 🎨 **Modern Tema** — Koyu tema, akıcı animasyonlar
- 🌐 **Türkçe Arayüz** — Tamamen Türkçe

---

### 🛡️ Güvenlik ve Gizlilik

- ✅ **Sadece 2 izin** (QUERY_ALL_PACKAGES + INTERNET)
- ✅ **Veri toplamaz** — Hiçbir kişisel veri toplanmaz
- ✅ **Sunucu yok** — Tüm işlemler cihazda yapılır
- ✅ **Açık kaynak** — Kodun tamamı incelenebilir
- ✅ **VirusTotal: Temiz** — 70+ antivirüs motoru tarafından taranmıştır
- ✅ **Çift onay sistemi** — Kritik uygulamalarda iki aşamalı onay
- ✅ **Reklamsız** — Hiçbir reklam yok

---

### 📥 İndir

| Kanal | Link |
|-------|------|
| **APK (Ana)** | [dosya.co](https://dosya.co/5698iipqmmza/Debloat.apk.html) |
| **APK (Yedek)** | [dosya.co](https://dosya.co/wpm910nc8pfv/Debloat.apk.html) |
| **Uptodown** | [Uptodown'da Gör](https://uptodown.dev) |
| **Yayıncı Mağazası** | [center-store-silk.vercel.app](https://center-store-silk.vercel.app/) |

---

### 📖 Kurulum

#### 1. Shizuku'yu Kur

1. Play Store'dan **Shizuku**'yu indirin
2. **Ayarlar → Telefon Hakkında → Build numarasına 7 kere basın**
3. **Geliştirici seçenekleri → Kablosuz hata ayıklama** AÇ
4. Shizuku'yu açın ve **Pairing** yapın
5. **Servisi başlatın**

#### 2. Debloat'ı Kur

1. Yukarıdaki indirme linklerinden **APK'yı indirin**
2. APK'ya tıklayın → **Kur**
3. **Bilinmeyen kaynaklardan yüklemeye izin verin**
4. Debloat'ı açın → **Shizuku izni verin**
5. Uygulama listesinden silmek istediğinizi seçin

---

### 🎯 Kritik Uyarı Sistemi

Debloat, cihazınızı korumak için **renkli risk seviyeleri** kullanır:

| Seviye | Renk | Anlam |
|--------|------|-------|
| 🚨 **Kritik** | Kırmızı | Silinirse cihaz çöker (SystemUI, Settings, Launcher) |
| ⚠️ **Önemli** | Turuncu | Bazı özellikler bozulabilir (Google servisleri) |
| ✓ **Güvenli** | Yeşil | Bloatware — güvenle silinebilir |

**Kritik uygulamalarda çift onay istenir.**

---

### ⚙️ Teknik Detaylar

- **Platform:** Android 8.0+ (API 26+)
- **Dil:** Kotlin
- **Altyapı:** Shizuku API
- **Kullanılan İzinler:**
  - `QUERY_ALL_PACKAGES` — Uygulamaları listelemek için
  - `INTERNET` — Shizuku ile yerel iletişim için
- **Boyut:** Birkaç MB
- **RAM Kullanımı:** Minimum

---

### 🔧 Kaynak Koddan Derleme

```bash
# Repoyu klonla
git clone https://github.com/kullanici/debloat.git

# Android Studio'da aç
# Build → Make Project

# APK'yı derle
# Build → Build Bundle(s) / APK(s) → Build APK(s)
