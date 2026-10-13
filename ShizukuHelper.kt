package com.centerteam.shell

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuHelper {

    const val PERMISSION_REQUEST_CODE = 1001

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) { false }
    }

    fun hasPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
        } catch (e: Exception) { e.printStackTrace() }
    }

    /**
     * Yansıma ile komut çalıştır
     */
    fun exec(cmd: Array<String>): String {
        return try {
            val cls = Class.forName("rikka.shizuku.Shizuku")
            val method = cls.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, cmd, null, null) as Process
            val output = process.inputStream.bufferedReader().readText()
            val errOutput = process.errorStream.bufferedReader().readText()
            process.waitFor()
            (output + "\n" + errOutput).trim()
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    fun listAllPackages(): List<String> {
        val output = exec(arrayOf("pm", "list", "packages", "-u"))
        return output.lines()
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:").trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * ⚡ KESİN SİLME — 4 KATMANLI
     * 1. pm uninstall --user 0 --force
     * 2. pm disable-user --user 0
     * 3. pm hide --user 0
     * 4. Doğrulama: pm list packages -u | grep
     *
     * @return Pair<SonuçKodu, Mesaj>
     */
    fun forceRemove(pkg: String): Pair<String, String> {

        // === KATMAN 1: Uninstall (zorla) ===
        val uninstall1 = exec(arrayOf("pm", "uninstall", "--user", "0", "--force", pkg))
        if (uninstall1.contains("Success", ignoreCase = true)) {
            return Pair("uninstalled", "✓ Tamamen silindi")
        }

        // === KATMAN 2: Standart uninstall ===
        val uninstall2 = exec(arrayOf("pm", "uninstall", "--user", "0", pkg))
        if (uninstall2.contains("Success", ignoreCase = true)) {
            return Pair("uninstalled", "✓ Silindi")
        }

        // === KATMAN 3: Disable + Hide ===
        exec(arrayOf("pm", "disable-user", "--user", "0", pkg))
        exec(arrayOf("pm", "hide", "--user", "0", pkg))

        // Doğrulama
        val check = exec(arrayOf("pm", "list", "packages", "-u"))
        val stillThere = check.lines().any { it.contains(pkg) }

        return if (!stillThere) {
            Pair("hidden", "✓ Tamamen gizlendi ve devre dışı bırakıldı")
        } else {
            // Son çare: sadece disable
            val disableCheck = exec(arrayOf("pm", "disable-user", "--user", "0", pkg))
            if (!disableCheck.contains("Error", ignoreCase = true)) {
                Pair("disabled", "⚠ Devre dışı bırakıldı (tamamen silinemedi)")
            } else {
                Pair("failed", "✗ Başarısız — Samsung KNOX koruması olabilir")
            }
        }
    }

    /**
     * Silme (basit uninstall)
     */
    fun uninstall(pkg: String): Boolean {
        val out = exec(arrayOf("pm", "uninstall", "--user", "0", pkg))
        return out.contains("Success", ignoreCase = true)
    }

    fun disable(pkg: String): Boolean {
        val out = exec(arrayOf("pm", "disable-user", "--user", "0", pkg))
        return !out.contains("Error", ignoreCase = true) && !out.contains("ERROR")
    }

    fun enable(pkg: String): Boolean {
        val out = exec(arrayOf("pm", "enable", pkg))
        return !out.contains("Error", ignoreCase = true) && !out.contains("ERROR")
    }

    fun unhide(pkg: String): Boolean {
        val out = exec(arrayOf("pm", "unhide", "--user", "0", pkg))
        return !out.contains("Error", ignoreCase = true)
    }

    fun forceStop(pkg: String): Boolean {
        val out = exec(arrayOf("am", "force-stop", pkg))
        return !out.contains("Error", ignoreCase = true)
    }

    fun clearData(pkg: String): Boolean {
        val out = exec(arrayOf("pm", "clear", pkg))
        return out.contains("Success", ignoreCase = true)
    }

    fun launchApp(pkg: String): Boolean {
        val monkey = exec(arrayOf("monkey", "-p", pkg,
            "-c", "android.intent.category.LAUNCHER", "1"))
        if (monkey.contains("Events injected")) return true

        val dumpsys = exec(arrayOf("dumpsys", "package", pkg))
        val regex = Regex("$pkg/([a-zA-Z0-9_.]+Activity)")
        val match = regex.find(dumpsys)?.groupValues?.get(1)
        if (match != null) {
            val result = exec(arrayOf("am", "start", "-n", "$pkg/$match"))
            return !result.contains("Error", ignoreCase = true)
        }
        return false
    }

    fun getAllActivities(pkg: String): List<String> {
        val dumpsys = exec(arrayOf("dumpsys", "package", pkg))
        val regex = Regex("$pkg/([a-zA-Z0-9_.]+)")
        return regex.findAll(dumpsys)
            .map { it.value }
            .toList()
            .distinct()
    }

    fun launchActivity(component: String): Boolean {
        val result = exec(arrayOf("am", "start", "-n", component))
        return !result.contains("Error", ignoreCase = true)
    }

    /**
     * Uygulamanın detaylarını döndürür
     */
    fun getAppDetail(pkg: String): AppDetail {
        val dumpsys = exec(arrayOf("dumpsys", "package", pkg))

        val versionName = Regex("versionName=([^\\s]+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val versionCode = Regex("versionCode=(\\d+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val targetSdk = Regex("targetSdk=(\\d+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val minSdk = Regex("minSdk=(\\d+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val dataDir = Regex("dataDir=([^\\s]+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val codePath = Regex("codePath=([^\\s]+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val firstInstallTime = Regex("firstInstallTime=([^\\s]+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"
        val lastUpdateTime = Regex("lastUpdateTime=([^\\s]+)").find(dumpsys)
            ?.groupValues?.get(1) ?: "?"

        // İzinler
        val permissions = Regex("android\\.permission\\.[A-Z_]+")
            .findAll(dumpsys)
            .map { it.value }
            .distinct()
            .toList()

        // İstatistik
        val activityCount = Regex("Activity Resolver Table|Activities:")
            .findAll(dumpsys).count()
        val serviceCount = Regex("Service Resolver Table|Services:")
            .findAll(dumpsys).count()
        val receiverCount = Regex("Receiver Resolver Table|Receivers:")
            .findAll(dumpsys).count()

        return AppDetail(
            versionName = versionName,
            versionCode = versionCode,
            targetSdk = targetSdk,
            minSdk = minSdk,
            dataDir = dataDir,
            codePath = codePath,
            firstInstall = firstInstallTime,
            lastUpdate = lastUpdateTime,
            permissions = permissions,
            activityCount = activityCount,
            serviceCount = serviceCount,
            receiverCount = receiverCount,
            rawDumpsys = dumpsys
        )
    }
}

data class AppDetail(
    val versionName: String,
    val versionCode: String,
    val targetSdk: String,
    val minSdk: String,
    val dataDir: String,
    val codePath: String,
    val firstInstall: String,
    val lastUpdate: String,
    val permissions: List<String>,
    val activityCount: Int,
    val serviceCount: Int,
    val receiverCount: Int,
    val rawDumpsys: String
)
