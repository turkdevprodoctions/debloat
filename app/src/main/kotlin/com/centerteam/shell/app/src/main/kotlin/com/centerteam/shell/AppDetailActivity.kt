package com.centerteam.shell

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class AppDetailActivity : Activity() {

    private lateinit var pkg: String
    private var isSystemApp = false
    private var appName = ""
    private var appIcon: android.graphics.drawable.Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pkg = intent.getStringExtra("package") ?: run { finish(); return }

        isSystemApp = try {
            val info = packageManager.getApplicationInfo(pkg, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        } catch (e: Exception) { false }

        try {
            val info = packageManager.getApplicationInfo(pkg, 0)
            appName = packageManager.getApplicationLabel(info).toString()
            appIcon = packageManager.getApplicationIcon(info)
        } catch (e: Exception) {
            appName = pkg.substringAfterLast(".")
        }

        render()
    }

    private fun render() {
        val scroll = ScrollView(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#0B1120"), Color.parseColor("#1E1B4B"))
            )
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 80, 30, 80)
        }
        scroll.addView(root)

        // İKON + İSİM
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 20)
        }
        header.addView(ImageView(this).apply {
            setImageDrawable(appIcon)
            layoutParams = LinearLayout.LayoutParams(140, 140).apply {
                setMargins(0, 0, 25, 0)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        })

        val headerTexts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        headerTexts.addView(TextView(this).apply {
            text = appName
            textSize = 24f
            setTextColor(Color.WHITE)
        })
        headerTexts.addView(TextView(this).apply {
            text = pkg
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 6, 0, 0)
        })
        header.addView(headerTexts)
        root.addView(header)

        // TÜR ROZETİ
        val typeBadge = if (isSystemApp) "⚙️ SİSTEM UYGULAMASI" else "📱 KULLANICI UYGULAMASI"
        val typeColor = if (isSystemApp) "#F59E0B" else "#10B981"
        root.addView(TextView(this).apply {
            text = typeBadge
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(typeColor))
                cornerRadius = 15f
            }
        })

        // RİSK KARTI
        val risk = CriticalApps.check(pkg)
        val riskColor = when (risk.level) {
            "critical" -> "#DC2626"
            "warning" -> "#F59E0B"
            "safe" -> "#10B981"
            else -> "#64748B"
        }
        val riskCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 25, 30, 25)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(riskColor))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 20) }
        }
        riskCard.addView(TextView(this).apply {
            text = risk.message
            textSize = 15f
            setTextColor(Color.WHITE)
        })
        riskCard.addView(TextView(this).apply {
            text = risk.reason
            textSize = 12f
            setTextColor(Color.parseColor("#F0F0F0"))
            setPadding(0, 10, 0, 0)
            setLineSpacing(0f, 1.4f)
        })
        root.addView(riskCard)

        // DETAY BİLGİLERİ
        root.addView(section("📊 UYGULAMA BİLGİLERİ"))

        val detail = ShizukuHelper.getAppDetail(pkg)
        root.addView(infoRow("Sürüm", detail.versionName))
        root.addView(infoRow("Version Code", detail.versionCode))
        root.addView(infoRow("Target SDK", detail.targetSdk))
        root.addView(infoRow("Min SDK", detail.minSdk))
        root.addView(infoRow("Activity", detail.activityCount.toString()))
        root.addView(infoRow("Service", detail.serviceCount.toString()))
        root.addView(infoRow("İzin Sayısı", detail.permissions.size.toString()))
        root.addView(infoRow("Kurulum", detail.firstInstall.take(19)))
        root.addView(infoRow("Son Güncelleme", detail.lastUpdate.take(19)))

        // İZİNLER
        if (detail.permissions.isNotEmpty()) {
            root.addView(section("🔐 İZİNLER (${detail.permissions.size})"))
            detail.permissions.take(20).forEach { perm ->
                root.addView(TextView(this).apply {
                    text = "• ${perm.removePrefix("android.permission.")}"
                    textSize = 11f
                    setTextColor(Color.parseColor("#E2E8F0"))
                    setPadding(20, 6, 20, 6)
                })
            }
        }

        // AKSİYONLAR
        root.addView(section("⚡ AKSİYONLAR"))

        val hasShizuku = ShizukuHelper.isAvailable() && ShizukuHelper.hasPermission()
        root.addView(TextView(this).apply {
            text = if (hasShizuku) "✓ Shizuku AKTİF" else "⚠ Shizuku izni yok"
            textSize = 13f
            setTextColor(Color.parseColor(if (hasShizuku) "#10B981" else "#F59E0B"))
            setPadding(30, 15, 30, 15)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 15f
            }
        })

        root.addView(button("🚀 Uygulamayı Aç", "#3B82F6") {
            if (!hasShizuku) { toast("Shizuku izni gerekli"); return@button }
            if (ShizukuHelper.launchApp(pkg)) toast("Açılıyor...")
            else toast("Launcher activity yok")
        })

        root.addView(button("📋 Paket Adını Kopyala", "#8B5CF6") {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("pkg", pkg))
            toast("Kopyalandı")
        })

        if (hasShizuku) {
            root.addView(button("⛔ Zorla Durdur", "#F59E0B") {
                if (ShizukuHelper.forceStop(pkg)) toast("Durduruldu")
                else toast("Başarısız")
            })

            root.addView(button("🧹 Veri Temizle", "#06B6D4") {
                confirmDelete(risk, "verileri temizle") {
                    if (ShizukuHelper.clearData(pkg)) toast("Temizlendi")
                    else toast("Başarısız")
                }
            })

            root.addView(button("🧊 Devre Dışı Bırak (Dondur)", "#F97316") {
                confirmDelete(risk, "devre dışı bırak") {
                    if (ShizukuHelper.disable(pkg)) {
                        toast("Devre dışı ✓")
                        finish()
                    } else toast("Başarısız")
                }
            })

            root.addView(button("✓ Etkinleştir", "#10B981") {
                if (ShizukuHelper.enable(pkg)) {
                    ShizukuHelper.unhide(pkg)
                    toast("Etkinleştirildi")
                } else toast("Başarısız")
            })

            // ⚡ KESİN SİL
            root.addView(button("🗑️ KESİN SİL (Force)", "#DC2626") {
                confirmDelete(risk, "TAMAMEN SİL") {
                    val (code, msg) = ShizukuHelper.forceRemove(pkg)
                    toast(msg)
                    if (code == "uninstalled" || code == "hidden") {
                        finish()
                    }
                }
            })
        }

        setContentView(scroll)
    }

    private fun infoRow(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(25, 18, 25, 18)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 15f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }

            addView(TextView(this@AppDetailActivity).apply {
                text = label
                textSize = 13f
                setTextColor(Color.parseColor("#94A3B8"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(this@AppDetailActivity).apply {
                text = value
                textSize = 13f
                setTextColor(Color.WHITE)
            })
        }
    }

    private fun confirmDelete(risk: CriticalApps.RiskLevel, action: String, onYes: () -> Unit) {
        when (risk.level) {
            "critical" -> {
                AlertDialog.Builder(this)
                    .setTitle("🚨 ÇOK TEHLİKELİ!")
                    .setMessage(
                        "Bu uygulama cihaz için KRİTİK!\n\n" +
                        "${risk.reason}\n\n" +
                        "YİNE DE ${action.uppercase()} İSTİYOR MUSUN?"
                    )
                    .setPositiveButton("⚠ EVET, RİSKİ KABUL EDİYORUM") { _, _ ->
                        AlertDialog.Builder(this)
                            .setTitle("⚠️ SON UYARI")
                            .setMessage(
                                "GERÇEKTEN EMİN MİSİN?\n\n" +
                                "Cihaz açılmayabilir.\n" +
                                "Tüm verilerini kaybedebilirsin."
                            )
                            .setPositiveButton("🔥 EVET, YAP") { _, _ -> onYes() }
                            .setNegativeButton("VAZGEÇ", null)
                            .show()
                    }
                    .setNegativeButton("HAYIR", null)
                    .show()
            }
            "warning" -> {
                AlertDialog.Builder(this)
                    .setTitle("⚠️ Dikkat")
                    .setMessage("${risk.message}\n\n${risk.reason}\n\n$action istiyor musun?")
                    .setPositiveButton("Devam Et") { _, _ -> onYes() }
                    .setNegativeButton("Vazgeç", null)
                    .show()
            }
            else -> {
                AlertDialog.Builder(this)
                    .setTitle("Onayla")
                    .setMessage("$action istediğine emin misin?\n\n$pkg")
                    .setPositiveButton("Evet") { _, _ -> onYes() }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        }
    }

    private fun section(t: String) = TextView(this).apply {
        text = t
        textSize = 14f
        setTextColor(Color.parseColor("#A78BFA"))
        setPadding(0, 40, 0, 10)
    }

    private fun button(label: String, color: String, action: () -> Unit): TextView =
        TextView(this).apply {
            this.text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(40, 35, 40, 35)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(color))
                cornerRadius = 30f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            isClickable = true
            setOnClickListener { action() }
        }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
