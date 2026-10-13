package com.centerteam.shell

object CriticalApps {

    val BRICK_LEVEL = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.providers.settings",
        "com.android.providers.media",
        "com.android.providers.contacts",
        "com.android.providers.telephony",
        "com.android.providers.downloads",
        "com.android.providers.calendar",
        "com.android.server.telecom",
        "com.android.phone",
        "com.android.shell",
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.samsung.android.launcher",
        "com.samsung.android.app.launcher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.huawei.android.launcher"
    )

    val WARNING_LEVEL = setOf(
        "com.android.vending",
        "com.google.android.gms.location",
        "com.google.android.gms.auth",
        "com.android.bluetooth",
        "com.android.nfc",
        "com.android.camera",
        "com.android.camera2",
        "com.android.gallery3d",
        "com.android.mms",
        "com.android.email",
        "com.android.calendar",
        "com.android.deskclock",
        "com.android.contacts",
        "com.android.dialer",
        "com.android.inputmethod.latin",
        "com.google.android.inputmethod.latin",
        "com.android.keychain",
        "com.android.certinstaller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.wallpaper",
        "com.android.wallpaperpicker",
        "com.android.wallpapercropper",
        "com.android.thememanager",
        "com.samsung.android.app.contacts",
        "com.samsung.android.dialer",
        "com.samsung.android.messaging",
        "com.sec.android.app.camera",
        "com.sec.android.gallery3d",
        "com.samsung.android.game.gamehome"
    )

    data class RiskLevel(
        val level: String,
        val message: String,
        val reason: String
    )

    fun check(packageName: String): RiskLevel {
        return when {
            packageName in BRICK_LEVEL -> RiskLevel(
                "critical",
                "🚨 BU UYGULAMA SİLİNİRSE CİHAZ ÇÖKEBİLİR!",
                "Bu sistem uygulaması cihazın temel işlevleri için gerekli.\n\n" +
                        "Silinmesi durumunda:\n" +
                        "• Telefon açılmaz (bootloop)\n" +
                        "• Ayarlar çalışmaz\n" +
                        "• Arama yapılamaz\n" +
                        "• WiFi/Bluetooth bozulabilir\n\n" +
                        "Geri almak için fabrika ayarlarına dönmen gerekir."
            )
            packageName in WARNING_LEVEL -> RiskLevel(
                "warning",
                "⚠️ Bu uygulama önemli olabilir",
                "Kritik değil ama bazı özellikleri bozabilir."
            )
            packageName.startsWith("com.android.") ||
            packageName.startsWith("android") -> RiskLevel(
                "warning",
                "⚠️ Sistem uygulaması",
                "Emin değilsen silme. Fabrika ayarlarına dönerek geri gelir."
            )
            packageName.startsWith("com.samsung.") ||
            packageName.startsWith("com.sec.") -> RiskLevel(
                "warning",
                "⚠️ Samsung uygulaması",
                "Sistem uygulaması olabilir. Dikkatli ol."
            )
            packageName.startsWith("com.google.") -> RiskLevel(
                "warning",
                "⚠️ Google uygulaması",
                "Bazı Google servisleri etkilenebilir."
            )
            else -> RiskLevel(
                "safe",
                "✓ Güvenli",
                "Kullanıcı uygulaması. Silinebilir."
            )
        }
    }
}
