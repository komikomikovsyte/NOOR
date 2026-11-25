package com.noor.islamic

import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class NoorWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = NoorEngine()
    inner class NoorEngine : Engine() {
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG); private val handler=Handler(Looper.getMainLooper()); private var visible=false; private var phase=0f
        private val draw=object:Runnable{override fun run(){drawFrame(); if(visible) handler.postDelayed(this,80)}}
        override fun onVisibilityChanged(v:Boolean){visible=v;handler.removeCallbacks(draw);if(v)handler.post(draw)}
        override fun onSurfaceChanged(h:SurfaceHolder,f:Int,w:Int,hh:Int){super.onSurfaceChanged(h,f,w,hh);drawFrame()}
        override fun onSurfaceDestroyed(h:SurfaceHolder){visible=false;handler.removeCallbacks(draw);super.onSurfaceDestroyed(h)}
        private fun drawFrame(){if(!visible)return; var c:Canvas?=null;try{c=surfaceHolder.lockCanvas();if(c!=null)scene(c)}finally{if(c!=null)surfaceHolder.unlockCanvasAndPost(c)}}
        private fun scene(c:Canvas){
            val w=c.width.toFloat();val h=c.height.toFloat(); phase+=.015f
            paint.shader=LinearGradient(0f,0f,0f,h,intArrayOf(0xFF041713.toInt(),0xFF0A3027.toInt(),0xFF061D18.toInt()),null,Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,paint);paint.shader=null
            // soft moon glow
            paint.shader=RadialGradient(w*.76f,h*.20f,w*.36f,intArrayOf(0x42E8D59B,0x140F6A50,0x00000000),null,Shader.TileMode.CLAMP);c.drawCircle(w*.76f,h*.20f,w*.36f,paint);paint.shader=null
            // crescent
            paint.color=0xFFE0C47E.toInt();c.drawCircle(w*.76f,h*.18f,w*.085f,paint);paint.color=0xFF092A22.toInt();c.drawCircle(w*.795f,h*.155f,w*.075f,paint)
            // stars / particles
            for(i in 0 until 28){val x=((i*97)%100)/100f*w;val y=((i*53)%55)/100f*h;val a=(55+35*sin(phase+i)).toInt().coerceIn(15,90);paint.color=Color.argb(a,235,216,159);c.drawCircle(x,y,if(i%5==0)2.2f else 1.2f,paint)}
            // mosque silhouette near bottom
            paint.color=0xFF03110E.toInt();val base=h*.84f;c.drawRect(0f,base,w,h,paint);c.drawRect(w*.18f,base-h*.11f,w*.82f,base,paint)
            c.drawCircle(w*.50f,base-h*.11f,w*.18f,paint);c.drawRect(w*.32f,base-h*.11f,w*.68f,base,paint)
            c.drawRect(w*.12f,base-h*.22f,w*.18f,base,paint);c.drawCircle(w*.15f,base-h*.22f,w*.03f,paint)
            c.drawRect(w*.82f,base-h*.22f,w*.88f,base,paint);c.drawCircle(w*.85f,base-h*.22f,w*.03f,paint)
            // brand + time
            paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create(Typeface.SERIF,Typeface.BOLD);paint.color=0xFFE0C47E.toInt();paint.textSize=w*.095f;c.drawText("نُور",w*.5f,h*.42f,paint)
            paint.typeface=Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL);paint.textSize=w*.028f;paint.letterSpacing=.18f;paint.color=0xFFB8C9C1.toInt();c.drawText("NOOR",w*.5f,h*.465f,paint);paint.letterSpacing=0f
            val time=SimpleDateFormat("HH:mm",Locale.US).format(Date());paint.typeface=Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD);paint.textSize=w*.12f;paint.color=0xFFF1EBDD.toInt();c.drawText(time,w*.5f,h*.60f,paint)
            paint.textSize=w*.032f;paint.typeface=Typeface.DEFAULT;paint.color=0xFFB8C9C1.toInt();c.drawText(SimpleDateFormat("EEEE, d MMMM",Locale("ru")).format(Date()),w*.5f,h*.645f,paint)
        }
    }
}
