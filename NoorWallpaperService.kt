package com.noor.islamic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.sin

class NoorWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = NoorEngine()
    inner class NoorEngine : Engine() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var visible = false
        private var frame = 0
        override fun onVisibilityChanged(visibleNow: Boolean) { visible = visibleNow; if (visible) drawFrame() }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { super.onSurfaceChanged(holder, format, width, height); drawFrame() }
        override fun onSurfaceDestroyed(holder: SurfaceHolder) { visible = false; super.onSurfaceDestroyed(holder) }
        private fun drawFrame() {
            if (!visible) return
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(0xFF061B16.toInt())
                    paint.shader = RadialGradient(canvas.width * .5f, canvas.height * .42f, canvas.width * .8f,
                        intArrayOf(0xFF174C3A.toInt(), 0xFF0B2B22.toInt(), 0xFF061B16.toInt()), null, Shader.TileMode.CLAMP)
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                    paint.shader = null; paint.color = 0xFFC5A45B.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = canvas.width * .09f
                    canvas.drawText("نُور", canvas.width / 2f, canvas.height * .42f, paint)
                    paint.textSize = canvas.width * .035f; paint.color = 0xFFE4D8B5.toInt()
                    canvas.drawText("NOOR  •  نور", canvas.width / 2f, canvas.height * .47f, paint)
                    paint.color = 0x55D8C58A; paint.style = Paint.Style.STROKE; paint.strokeWidth = 2f
                    val r = canvas.width * (.28f + .006f * sin(frame++ / 8.0).toFloat())
                    canvas.drawCircle(canvas.width / 2f, canvas.height * .44f, r, paint); paint.style = Paint.Style.FILL
                }
            } finally { if (canvas != null) holder.unlockCanvasAndPost(canvas) }
        }
    }
}
