package com.noor.islamic
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
object PrayerTimes {
 fun date(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Samarkand") }.format(Date())
 fun times(c: Context): List<String> {
 val p = c.getSharedPreferences("noor_settings", 0)
 return (0..5).map { i ->
 val t = p.getString("time_${date()}_$i", null)
 if (t == null) "—" else { val x=t.split(":"); val m=(x[0].toInt()*60+x[1].toInt()+p.getInt("offset_$i",0)+1440)%1440; String.format(Locale.US,"%02d:%02d",m/60,m%60) }
 }
 }
}
