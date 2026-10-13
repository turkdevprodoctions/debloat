package com.centerteam.shell

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var listContainer: LinearLayout
    private lateinit var searchInput: EditText
    private var allPackages: List<PackageItem> = emptyList()
    private var filtered: List<PackageItem> = emptyList()

    data class PackageItem(
        val pkg: String,
        val name: String,
        val icon: Drawable?,
        val isSystem: Boolean,
        val isEnabled: Boolean,
        val isHidden: Boolean
    )

    private val shizukuListener =
        Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == ShizukuHelper.PERMISSION_REQUEST_CODE) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    toast("✓ Shizuku izni verildi")
                    loadAll()
                } else {
                    toast("✗ Reddedildi")
                }
                updateStatus()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Shizuku.addRequestPermissionResultListener(shizukuListener)
        } catch (_: Exception) {}

        val scroll = ScrollView(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#0B1120"), Color.parseColor("#1E1B4B"))
            )
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 80, 24, 80)
        }
        scroll.addView(root)

        // BAŞLIK
        root.addView(TextView(this).apply {
            text = "🗑 Debloat Pro"
            textSize = 30f
            setTextColor(Color.WHITE)
        })
        root.addView(TextView(this).apply {
            text = "Shizuku ile Güçlü Sistem Uygulama Yöneticisi"
            textSize = 13f
            setTextColor(Color.parseColor("#A78BFA"))
            setPadding(0, 6, 0, 30)
        })

        // DURUM
        statusText = TextView(this).apply {
            text = "Kontrol ediliyor..."
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(30, 25, 30, 25)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 30f
            }
        }
        root.addView(statusText)

        // İZİN BUTONU
        root.addView(button("🔓 Shizuku İzni Al", "#3B82F6") {
            if (!ShizukuHelper.isAvailable()) {
                toast("Shizuku çalışmıyor")
                return@button
            }
            if (ShizukuHelper.hasPermission()) {
                toast("Zaten izinli")
                return@button
            }
            ShizukuHelper.requestPermission()
        })

        // ARAMA
        searchInput = EditText(this).apply {
            hint = "🔍 Paket ara..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#64748B"))
            textSize = 15f
            setBackgroundColor(Color.parseColor("#1E293B"))
            setPadding(40, 30, 40, 30)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 20) }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                    filter(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        root.addView(searchInput)

        // FİLTRE
        val filterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 20)
        }
        filterRow.addView(smallButton("📱 Tümü") { loadAll() })
        filterRow.addView(smallButton("⚙️ Sistem") { loadSystem() })
        filterRow.addView(smallButton("🚫 Devre Dışı") { loadDisabled() })
        root.addView(filterRow)

        // LİSTE
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(listContainer)

        setContentView(scroll)
        updateStatus()
    }

    private fun updateStatus() {
        when {
            !ShizukuHelper.isAvailable() -> {
                statusText.text = "❌ Shizuku kurulu değil"
                statusText.setTextColor(Color.parseColor("#EF4444"))
            }
            !ShizukuHelper.hasPermission() -> {
                statusText.text = "⚠ Shizuku hazır, izin lazım"
                statusText.setTextColor(Color.parseColor("#F59E0B"))
            }
            else -> {
                statusText.text = "✓ Shizuku hazır — tüm işlemler aktif"
                statusText.setTextColor(Color.parseColor("#10B981"))
                loadAll()
            }
        }
    }

    private fun loadAll() {
        if (!ShizukuHelper.hasPermission()) return
        val pm = packageManager
        val packages = pm.getInstalledPackages(
            PackageManager.MATCH_DISABLED_COMPONENTS or
            PackageManager.MATCH_UNINSTALLED_PACKAGES or
            PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS
        )
        allPackages = packages.mapNotNull { info ->
            try {
                val app: ApplicationInfo = info.applicationInfo ?: return@mapNotNull null
                val isSys = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                        (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                PackageItem(
                    pkg = info.packageName,
                    name = pm.getApplicationLabel(app).toString(),
                    icon = try { pm.getApplicationIcon(app) } catch (e: Exception) { null },
                    isSystem = isSys,
                    isEnabled = app.enabled,
                    isHidden = false
                )
            } catch (e: Exception) { null }
        }.distinctBy { it.pkg }.sortedBy { it.name.lowercase() }
        filtered = allPackages
        render()
    }

    private fun loadSystem() {
        loadAll()
        filtered = allPackages.filter { it.isSystem }
        render()
    }

    private fun loadDisabled() {
        loadAll()
        filtered = allPackages.filter { !it.isEnabled }
        render()
    }

    private fun filter(q: String) {
        filtered = if (q.isBlank()) allPackages
        else allPackages.filter {
            it.pkg.contains(q, ignoreCase = true) ||
            it.name.contains(q, ignoreCase = true)
        }
        render()
    }

    private fun render() {
        listContainer.removeAllViews()
        if (filtered.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = "Paket bulunamadı"
                textSize = 14f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, 20, 0, 20)
            })
            return
        }

        listContainer.addView(TextView(this).apply {
            text = "${filtered.size} paket"
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 0, 0, 15)
        })

        filtered.take(200).forEach { item ->
            val risk = CriticalApps.check(item.pkg)
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(20, 20, 20, 20)
                background = GradientDrawable().apply {
                    setColor(when {
                        risk.level == "critical" -> Color.parseColor("#4A1010")
                        risk.level == "warning" -> Color.parseColor("#4A3A10")
                        risk.level == "safe" -> Color.parseColor("#103A1F")
                        !item.isEnabled -> Color.parseColor("#2A1E1E")
                        else -> Color.parseColor("#1E293B")
                    })
                    cornerRadius = 20f
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 5, 0, 5) }
                isClickable = true
                setOnClickListener { openDetail(item.pkg) }
            }

            // İKON
            val iconView = ImageView(this).apply {
                setImageDrawable(item.icon)
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    setMargins(0, 0, 20, 0)
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            card.addView(iconView)

            // METİNLER
            val texts = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val prefix = when {
                risk.level == "critical" -> "🚨 "
                risk.level == "warning" -> "⚠️ "
                risk.level == "safe" -> "✓ "
                item.isSystem -> "⚙️ "
                else -> "📱 "
            }
            texts.addView(TextView(this).apply {
                text = prefix + item.name
                textSize = 15f
                setTextColor(Color.WHITE)
            })
            texts.addView(TextView(this).apply {
                text = item.pkg
                textSize = 10f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(0, 4, 0, 0)
            })

            // ROZETLER
            val badges = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 6, 0, 0)
            }
            if (item.isSystem) {
                badges.addView(badge("SİSTEM", "#F59E0B"))
            } else {
                badges.addView(badge("KULLANICI", "#10B981"))
            }
            if (!item.isEnabled) {
                badges.addView(badge("DEVRE DIŞI", "#EF4444"))
            }
            texts.addView(badges)

            card.addView(texts)

            card.addView(TextView(this).apply {
                text = "›"
                textSize = 24f
                setTextColor(Color.parseColor("#64748B"))
            })

            listContainer.addView(card)
        }
    }

    private fun badge(text: String, color: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 9f
            setTextColor(Color.WHITE)
            setPadding(15, 5, 15, 5)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(color))
                cornerRadius = 10f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 8, 0) }
        }

    private fun openDetail(pkg: String) {
        val i = Intent(this, AppDetailActivity::class.java)
        i.putExtra("package", pkg)
        startActivity(i)
    }

    private fun button(label: String, color: String, action: () -> Unit): TextView =
        TextView(this).apply {
            this.text = label
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(40, 35, 40, 35)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(color))
                cornerRadius = 40f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 10) }
            isClickable = true
            setOnClickListener { action() }
        }

    private fun smallButton(label: String, action: () -> Unit): TextView =
        TextView(this).apply {
            this.text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#334155"))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ).apply { setMargins(5, 0, 5, 0) }
            isClickable = true
            setOnClickListener { action() }
        }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.removeRequestPermissionResultListener(shizukuListener)
        } catch (_: Exception) {}
    }
}
