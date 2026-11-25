package com.noor.islamic

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> manager.updateAppWidget(id, views(context)) }
    }
    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PrayerWidgetProvider::class.java))
            ids.forEach { manager.updateAppWidget(it, views(context)) }
        }
        private fun views(context: Context): RemoteViews {
            val p = context.getSharedPreferences("noor_settings", Context.MODE_PRIVATE)
            val names = listOf("Фаджр", "Восход", "Зухр", "Аср", "Магриб", "Иша")
            val times = PrayerTimes.times(context)
            return RemoteViews(context.packageName, R.layout.widget_prayer).apply {
                setTextViewText(R.id.widget_city, "Самарканд · ${PrayerTimes.date()}")
                setTextViewText(R.id.widget_times, names.zip(times).joinToString("   ") { "${it.first} ${it.second}" })
            }
        }
    }
}
