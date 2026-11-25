package com.noor.islamic

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val names = listOf("Фаджр", "Восход", "Зухр", "Аср", "Магриб", "Иша")
    private val icons = listOf("☾", "☀", "◒", "◐", "◑", "✦")
    private val prefs by lazy { getSharedPreferences("noor_settings", Context.MODE_PRIVATE) }

    private val bg = Color.rgb(4, 20, 17)
    private val card = Color.rgb(12, 39, 32)
    private val card2 = Color.rgb(15, 49, 40)
    private val gold = Color.rgb(221, 194, 125)
    private val cream = Color.rgb(244, 239, 224)
    private val muted = Color.rgb(171, 190, 181)
    private val emerald = Color.rgb(31, 117, 88)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        DailyContentWorker.schedule(this)
        render()
    }

    override fun onResume() { super.onResume(); if (::class.java != null) render() }

    private fun render() {
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val scroll = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(bg); overScrollMode = View.OVER_SCROLL_NEVER }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(34)) }
        scroll.addView(content, ViewGroup.LayoutParams(-1, -2))

        // Header
        content.addView(LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = "نُور"; textSize = 29f; setTextColor(gold); typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(pill("САМАРКАНД"))
        })
        content.addView(text("NOOR  ·  ${prettyDate()}", 13, muted).apply { letterSpacing = .08f })
        content.addView(gap(18))

        // Hero / next prayer
        val times = PrayerTimes.times(this)
        val next = nextPrayer(times)
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(22), dp(22), dp(20)); background = gradientCard()
        }
        hero.addView(text("СЛЕДУЮЩИЙ НАМАЗ", 11, gold, true).apply { letterSpacing = .14f })
        hero.addView(text(next.first, 29, cream, true))
        hero.addView(text(next.second, 48, gold, true).apply { setPadding(0, dp(2), 0, 0) })
        hero.addView(text(next.third, 14, muted))
        hero.addView(gap(12))
        hero.addView(TextView(this).apply {
            text = "﷽"; textSize = 28f; gravity = Gravity.END; setTextColor(Color.argb(155, 221, 194, 125)); typeface = Typeface.SERIF
        })
        content.addView(hero, lpBottom(16))

        // Prayer strip
        sectionTitle(content, "Сегодня")
        val prayerScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER }
        val prayerRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        names.forEachIndexed { i, name ->
            val offset = prefs.getInt("offset_$i", 0)
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(15), dp(14), dp(15), dp(14)); background = round(card, 20)
                addView(text(icons[i], 18, gold, true).apply { gravity = Gravity.CENTER })
                addView(text(name, 12, muted).apply { gravity = Gravity.CENTER })
                addView(text(times.getOrElse(i) { "--:--" }, 18, cream, true).apply { gravity = Gravity.CENTER })
                if (offset != 0) addView(text(if (offset > 0) "+$offset мин" else "$offset мин", 10, gold).apply { gravity = Gravity.CENTER })
                setOnClickListener { editOffset(i) }
            }
            prayerRow.addView(tile, LinearLayout.LayoutParams(dp(104), -2).apply { marginEnd = dp(9) })
        }
        prayerScroll.addView(prayerRow); content.addView(prayerScroll, lpBottom(18))

        // Daily ayah
        val daily = getSharedPreferences("noor_daily", Context.MODE_PRIVATE)
        sectionTitle(content, "Для души")
        content.addView(dailyCard(
            "АЯТ ДНЯ", "❦",
            daily.getString("ayah_ar", "Подключитесь к интернету для первого обновления") ?: "",
            daily.getString("ayah_ru", "") ?: "",
            "Коран ${daily.getString("ayah_ref", "")} · перевод Э. Кулиева"
        ), lpBottom(12))
        content.addView(dailyCard(
            "ДУА ДНЯ", "☾",
            daily.getString("dua_ar", "После обновления здесь появится дуа") ?: "",
            daily.getString("dua_ru", "") ?: "",
            "Коран ${daily.getString("dua_ref", "")} · обновляется ежедневно"
        ), lpBottom(18))

        // Tasbih
        sectionTitle(content, "Тасбих")
        val count = prefs.getInt("tasbih", 0)
        val tasbih = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(18), dp(20), dp(18), dp(20)); background = round(card, 24)
            addView(text("سُبْحَانَ ٱللَّٰهِ", 22, gold, true).apply { gravity = Gravity.CENTER; typeface = Typeface.SERIF })
            addView(text("$count", 42, cream, true).apply { gravity = Gravity.CENTER })
            addView(primaryButton("НАЖАТЬ  +1") { prefs.edit().putInt("tasbih", count + 1).apply(); render() }, LinearLayout.LayoutParams(-1, dp(52)))
            addView(textButton("Сбросить") { prefs.edit().putInt("tasbih", 0).apply(); render() })
        }
        content.addView(tasbih, lpBottom(18))

        // Tools
        sectionTitle(content, "NOOR")
        content.addView(toolButton("☾", "Живые обои", "Мечеть, звёзды и следующий намаз") {
            try { startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this, NoorWallpaperService::class.java))) }
            catch (_: Exception) { Toast.makeText(this, "Откройте настройки обоев телефона", Toast.LENGTH_LONG).show() }
        }, lpBottom(9))
        content.addView(toolButton("◷", "Время намаза", "Расписание и корректировка ±60 минут") { editSchedule() }, lpBottom(9))
        content.addView(toolButton("↻", "Аят и дуа дня", "Обновить содержимое через интернет") {
            DailyContentWorker.schedule(this); Toast.makeText(this, "Обновление запущено", Toast.LENGTH_SHORT).show()
        }, lpBottom(9))
        content.addView(toolButton("▦", "Виджет намазов", "Синхронизировать время на главном экране") {
            PrayerWidgetProvider.updateAll(this); Toast.makeText(this, "Виджет обновлён", Toast.LENGTH_SHORT).show()
        })
        content.addView(gap(9))
        content.addView(toolButton("◉", "Экран блокировки", "Аят и дуа в тихих уведомлениях") {
            DailyNotifications.show(this); Toast.makeText(this, "Уведомления NOOR обновлены", Toast.LENGTH_SHORT).show()
        })

        setContentView(scroll)
    }

    private fun dailyCard(title: String, icon: String, arabic: String, translation: String, ref: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(18), dp(20), dp(18)); background = round(card, 24)
        addView(LinearLayout(this@MainActivity).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(text(icon, 18, gold, true))
            addView(text("  $title", 11, gold, true).apply { letterSpacing = .12f })
        })
        addView(text(arabic, 25, cream, true).apply { gravity = Gravity.END; typeface = Typeface.SERIF; setLineSpacing(dp(5).toFloat(), 1f); setPadding(0, dp(14), 0, dp(8)) })
        if (translation.isNotBlank()) addView(text(translation, 14, cream).apply { setLineSpacing(dp(3).toFloat(), 1f) })
        addView(text(ref, 11, muted).apply { setPadding(0, dp(10), 0, 0) })
    }

    private fun toolButton(icon: String, title: String, subtitle: String, click: () -> Unit): View = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(13), dp(16), dp(13)); background = round(card, 18); isClickable = true; setOnClickListener { click() }
        addView(text(icon, 22, gold, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(42), -2))
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL; addView(text(title, 15, cream, true)); addView(text(subtitle, 11, muted))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(text("›", 27, gold))
    }

    private fun nextPrayer(times: List<String>): Triple<String,String,String> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Samarkand")); val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        for (i in listOf(0,2,3,4,5)) {
            val p = times.getOrNull(i)?.split(":") ?: continue; if (p.size != 2) continue
            val mins = p[0].toIntOrNull()?.times(60)?.plus(p[1].toIntOrNull() ?: 0) ?: continue
            if (mins > now) return Triple(names[i], times[i], "через ${formatDelta(mins-now)}")
        }
        return Triple("Фаджр", times.getOrElse(0){"--:--"}, "следующий намаз завтра")
    }
    private fun formatDelta(m: Int) = if (m < 60) "$m мин" else "${m/60} ч ${m%60} мин"
    private fun prettyDate(): String = SimpleDateFormat("d MMMM · EEEE", Locale("ru")).format(Date()).replaceFirstChar { it.uppercase() }

    private fun editSchedule() {
        val day = PrayerTimes.date(); val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24),dp(8),dp(24),dp(8)) }
        val fields = names.mapIndexed { i, name -> EditText(this).apply { hint="$name (ЧЧ:ММ)"; setText(prefs.getString("time_${day}_$i", "")); form.addView(this) } }
        val dialog = AlertDialog.Builder(this).setTitle("Расписание · $day").setMessage("Введите точное время Самарканда.").setView(form).setPositiveButton("Сохранить", null).setNegativeButton("Отмена", null).create()
        dialog.setOnShowListener { dialog.getButton(-1).setOnClickListener {
            val values=fields.map{it.text.toString().trim()}; if(values.any{!it.matches(Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))}) Toast.makeText(this,"Формат ЧЧ:ММ",Toast.LENGTH_LONG).show()
            else { val e=prefs.edit(); values.forEachIndexed{i,t->e.putString("time_${day}_$i",t)}; e.apply(); PrayerWidgetProvider.updateAll(this); dialog.dismiss(); render() }
        }}; dialog.show()
    }
    private fun editOffset(index:Int){
        val input=EditText(this).apply{inputType=4098;setText(prefs.getInt("offset_$index",0).toString())}
        AlertDialog.Builder(this).setTitle("${names[index]} · корректировка").setMessage("От −60 до +60 минут").setView(input).setPositiveButton("Сохранить"){_,_->
            val v=input.text.toString().toIntOrNull(); if(v==null||v !in -60..60) Toast.makeText(this,"Введите число от −60 до +60",Toast.LENGTH_LONG).show() else {prefs.edit().putInt("offset_$index",v).apply();PrayerWidgetProvider.updateAll(this);render()}
        }.setNegativeButton("Отмена",null).show()
    }

    private fun sectionTitle(parent: LinearLayout, title:String){ parent.addView(text(title,17,cream,true).apply{setPadding(dp(2),0,0,dp(9))}) }
    private fun text(s:String,size:Int,color:Int,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size.toFloat();setTextColor(color);if(bold)setTypeface(typeface,Typeface.BOLD)}
    private fun pill(s:String)=TextView(this).apply{text=s;textSize=10f;setTextColor(gold);setPadding(dp(12),dp(7),dp(12),dp(7));background=strokeRound(Color.TRANSPARENT,gold,18,1);letterSpacing=.1f}
    private fun primaryButton(s:String, click:()->Unit)=TextView(this).apply{text=s;textSize=13f;setTextColor(bg);gravity=Gravity.CENTER;setTypeface(typeface,Typeface.BOLD);background=round(gold,18);setOnClickListener{click()};letterSpacing=.08f}
    private fun textButton(s:String,click:()->Unit)=TextView(this).apply{text=s;textSize=12f;setTextColor(muted);gravity=Gravity.CENTER;setPadding(0,dp(12),0,0);setOnClickListener{click()}}
    private fun gap(h:Int)=Space(this).apply{layoutParams=ViewGroup.LayoutParams(1,dp(h))}
    private fun lpBottom(m:Int)=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(m)}
    private fun round(color:Int,r:Int)=GradientDrawable().apply{setColor(color);cornerRadius=dp(r).toFloat()}
    private fun strokeRound(color:Int,stroke:Int,r:Int,w:Int)=GradientDrawable().apply{setColor(color);cornerRadius=dp(r).toFloat();setStroke(dp(w),stroke)}
    private fun gradientCard()=GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(22,82,63),Color.rgb(8,39,31),card)).apply{cornerRadius=dp(28).toFloat();setStroke(dp(1),Color.argb(70,221,194,125))}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
