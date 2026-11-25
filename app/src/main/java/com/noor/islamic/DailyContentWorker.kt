package com.noor.islamic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class DailyContentWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        return try {
            val today = LocalDate.now(ZoneId.of("Asia/Samarkand"))
            val day = today.toEpochDay()
            val ayahNumber = ((day * 1103515245L + 12345L).mod(6236L) + 1L).toInt()
            val duaRefs = listOf("2:201","2:250","2:286","3:8","3:16","3:53","3:147","7:23","7:126","10:85","11:47","12:101","14:40","14:41","18:10","20:25","20:114","21:83","21:87","23:97","23:98","23:109","25:65","25:74","26:83","27:19","28:24","40:7","40:8","59:10","60:4","66:8")
            val duaRef = duaRefs[Math.floorMod(day.toInt(), duaRefs.size)]
            val ayah = fetchPair(ayahNumber.toString())
            val dua = fetchPair(duaRef)
            val p = applicationContext.getSharedPreferences("noor_daily", Context.MODE_PRIVATE)
            p.edit()
                .putString("date", today.toString())
                .putString("ayah_ar", ayah.arabic).putString("ayah_ru", ayah.russian).putString("ayah_ref", ayah.reference)
                .putString("dua_ar", dua.arabic).putString("dua_ru", dua.russian).putString("dua_ref", dua.reference)
                .apply()
            DailyNotifications.show(applicationContext)
            Result.success()
        } catch (_: Exception) {
            DailyNotifications.show(applicationContext) // keep last cached content offline
            Result.retry()
        }
    }

    private data class PairText(val arabic:String, val russian:String, val reference:String)
    private fun fetchPair(ref:String): PairText {
        val endpoint = "https://api.alquran.cloud/v1/ayah/$ref/editions/quran-uthmani,ru.kuliev"
        val c = URL(endpoint).openConnection() as HttpURLConnection
        c.connectTimeout = 10000; c.readTimeout = 10000; c.setRequestProperty("Accept", "application/json")
        val root = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
        val arr = root.getJSONArray("data")
        var ar = ""; var ru = ""; var reference = ref
        for (i in 0 until arr.length()) {
            val x = arr.getJSONObject(i)
            val lang = x.getJSONObject("edition").optString("language")
            if (lang == "ar") ar = x.optString("text")
            if (lang == "ru") ru = x.optString("text")
            val s = x.optJSONObject("surah")?.optInt("number",0) ?: 0
            val a = x.optInt("numberInSurah",0)
            if (s > 0 && a > 0) reference = "$s:$a"
        }
        if (ar.isBlank()) throw IllegalStateException("Arabic ayah unavailable")
        return PairText(ar, ru.ifBlank { "Перевод временно недоступен" }, reference)
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request = PeriodicWorkRequestBuilder<DailyContentWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("noor_daily_content", ExistingPeriodicWorkPolicy.UPDATE, request)
            WorkManager.getInstance(context).enqueueUniqueWork("noor_daily_now", ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<DailyContentWorker>().setConstraints(constraints).build())
        }
    }
}

object DailyNotifications {
    private const val CHANNEL = "noor_daily_lock"
    fun show(context: Context) {
        val p = context.getSharedPreferences("noor_daily", Context.MODE_PRIVATE)
        val ar = p.getString("ayah_ar", "") ?: ""
        val ru = p.getString("ayah_ru", "") ?: ""
        val ref = p.getString("ayah_ref", "") ?: ""
        val duaAr = p.getString("dua_ar", "") ?: ""
        val duaRu = p.getString("dua_ru", "") ?: ""
        val duaRef = p.getString("dua_ref", "") ?: ""
        if (ar.isBlank() && duaAr.isBlank()) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel(CHANNEL,"NOOR · Аят и дуа",NotificationManager.IMPORTANCE_LOW).apply { description="Ежедневный аят и дуа на экране блокировки"; setSound(null,null); enableVibration(false) })
        val pi = PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        fun post(id:Int,title:String,a:String,r:String,reference:String) {
            val text = listOf(a,r,"Коран $reference").filter { it.isNotBlank() }.joinToString("\n")
            val n = NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_noor_notification).setContentTitle(title)
                .setContentText(r.ifBlank { a }).setStyle(NotificationCompat.BigTextStyle().bigText(text)).setContentIntent(pi)
                .setSilent(true).setOngoing(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setPriority(NotificationCompat.PRIORITY_LOW).build()
            nm.notify(id,n)
        }
        if (ar.isNotBlank()) post(601,"NOOR · Аят дня",ar,ru,ref)
        if (duaAr.isNotBlank()) post(602,"NOOR · Дуа дня",duaAr,duaRu,duaRef)
    }
}
