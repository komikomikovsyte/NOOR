package com.noor.islamic

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private val names = listOf("Фаджр", "Восход", "Зухр", "Аср", "Магриб", "Иша")
    private val base = listOf("05:00", "06:30", "12:00", "15:30", "17:30", "19:00")
    private val prefs by lazy { getSharedPreferences("noor_settings", Context.MODE_PRIVATE) }
    private val green = Color.rgb(7, 29, 24)
    private val panel = Color.rgb(16, 43, 35)
    private val gold = Color.rgb(215, 189, 123)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 24, 20, 24); setBackgroundColor(green) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, ViewGroup.LayoutParams(-1, -1))
        content.addView(label("☾  NOOR — نور", 28, gold, true))
        content.addView(label("Твой ежедневный исламский помощник", 14, Color.LTGRAY))
        content.addView(space(18))
        card(content, "ВРЕМЕНА НАМАЗОВ · УЗБЕКИСТАН") {
            addView(label("Город: ${prefs.getString("city", "Самарканд")}", 15, Color.WHITE))
            addView(label("Расчётный профиль: Узбекистан (настраиваемый)", 12, gold))
            names.forEachIndexed { i, name ->
                val offset = prefs.getInt("offset_$i", 0)
                val time = adjustedTime(base[i], offset)
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    addView(TextView(this@MainActivity).apply { text = name; textSize = 15f; setTextColor(Color.WHITE) }, LinearLayout.LayoutParams(0, -2, 1f))
                    addView(TextView(this@MainActivity).apply { text = "$time   (${signed(offset)} мин)"; textSize = 14f; setTextColor(gold) })
                    addView(Button(this@MainActivity).apply { text = "±"; setOnClickListener { editOffset(i) } })
                })
            }
            addView(Button(this@MainActivity).apply { text = "Настроить город и расписание"; setOnClickListener { editCity() } })
            addView(Button(this@MainActivity).apply { text = "Сбросить все корректировки"; setOnClickListener { names.indices.forEach { prefs.edit().remove("offset_$it").apply() }; PrayerWidgetProvider.updateAll(this@MainActivity); render() } })
        }
        card(content, "ТАСБИХ") {
            val count = prefs.getInt("tasbih", 0)
            addView(label("Субханаллах", 14, gold))
            addView(label("$count", 34, Color.WHITE, true))
            addView(Button(this@MainActivity).apply { text = "＋  Считать"; setOnClickListener { prefs.edit().putInt("tasbih", count + 1).apply(); render() } })
            addView(Button(this@MainActivity).apply { text = "Сбросить счётчик"; setOnClickListener { prefs.edit().putInt("tasbih", 0).apply(); render() } })
        }
        card(content, "АЯТ ДНЯ") {
            addView(label("إِنَّ مَعَ الْعُسْرِ يُسْرًا", 22, gold, true))
            addView(label("Воистину, за каждой тягостью наступает облегчение.", 14, Color.WHITE))
            addView(label("Коран, 94:6 · перевод смыслов", 12, Color.LTGRAY))
        }
        card(content, "ЕЖЕДНЕВНЫЙ ЗИКР") {
            addView(label("أَسْتَغْفِرُ اللَّهَ", 24, gold, true))
            addView(label("Астагфируллах — Прошу прощения у Аллаха", 14, Color.WHITE))
            addView(label("Утренние и вечерние поминания будут доступны в каталоге.", 12, Color.LTGRAY))
        }
        card(content, "КОМПАС РАЗРАБОТКИ") {
            addView(label("Коран с переводом • Дуа • уведомления и азан • живые обои • виджеты", 14, Color.WHITE))
            addView(Button(this@MainActivity).apply { text = "Установить живые обои NOOR"; setOnClickListener { try { startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this@MainActivity, NoorWallpaperService::class.java))) } catch (_: Exception) { Toast.makeText(this@MainActivity, "Откройте настройки обоев телефона", Toast.LENGTH_LONG).show() } } })
            addView(Button(this@MainActivity).apply { text = "Обновить виджет"; setOnClickListener { PrayerWidgetProvider.updateAll(this@MainActivity); Toast.makeText(this@MainActivity, "Виджет обновлён", Toast.LENGTH_SHORT).show() } })
        }
        setContentView(root)
    }

    private fun editOffset(index: Int) {
        val input = EditText(this).apply { inputType = 2; setText(prefs.getInt("offset_$index", 0).toString()); hint = "Например: -5 или 7" }
        AlertDialog.Builder(this).setTitle("${names[index]}: корректировка")
            .setMessage("Укажите смещение в минутах. Минус — раньше, плюс — позже.")
            .setView(input).setPositiveButton("Сохранить") { _, _ ->
                val value = input.text.toString().toIntOrNull()
                if (value == null || value !in -60..60) Toast.makeText(this, "Введите число от −60 до +60", Toast.LENGTH_LONG).show()
                else { prefs.edit().putInt("offset_$index", value).apply(); PrayerWidgetProvider.updateAll(this); render() }
            }.setNegativeButton("Отмена", null).show()
    }

    private fun editCity() {
        val input = EditText(this).apply { setText(prefs.getString("city", "Самарканд")); hint = "Например, Самарканд" }
        AlertDialog.Builder(this).setTitle("Город для расписания").setView(input)
            .setPositiveButton("Сохранить") { _, _ -> prefs.edit().putString("city", input.text.toString()).apply(); PrayerWidgetProvider.updateAll(this); render() }
            .setNegativeButton("Отмена", null).show()
    }

    private fun adjustedTime(value: String, offset: Int): String = try {
        val parts = value.split(":"); val total = (parts[0].toInt() * 60 + parts[1].toInt() + offset + 1440) % 1440
        String.format(Locale.US, "%02d:%02d", total / 60, total % 60)
    } catch (_: Exception) { value }
    private fun signed(n: Int) = if (n > 0) "+$n" else n.toString()
    private fun label(s: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply { text = s; textSize = size.toFloat(); setTextColor(color); if (bold) setTypeface(null, 1); setPadding(0, 5, 0, 5) }
    private fun space(h: Int) = TextView(this).apply { height = h }
    private fun card(parent: LinearLayout, title: String, body: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 14, 16, 14); setBackgroundColor(panel) }
        box.addView(label(title, 12, gold, true)); body(box)
        val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 14; parent.addView(box, lp)
    }
}
