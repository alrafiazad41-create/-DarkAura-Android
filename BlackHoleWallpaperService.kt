package com.rafi.blackholetheme

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.*
import kotlin.random.Random

class BlackHoleWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = BlackHoleEngine()

    inner class BlackHoleEngine : Engine(), SensorEventListener {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var frame = 0
        private var offsetX = 0f
        private var offsetY = 0f
        private var gyroX = 0f
        private var gyroY = 0f
        private val sensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
        private val gyro by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private data class Particle(var angle: Float, var radius: Float, val speed: Float, val size: Float, val alpha: Int)
        private val particles = List(150) {
            Particle(Random.nextFloat() * (2f * PI.toFloat()), 90f + Random.nextFloat() * 520f,
                0.004f + Random.nextFloat() * 0.018f, 0.7f + Random.nextFloat() * 2.4f,
                70 + Random.nextInt(160))
        }
        private val drawFrame = object : Runnable {
            override fun run() {
                if (!visible) return
                draw()
                handler.postDelayed(this, 33L)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (visible) {
                gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
                handler.removeCallbacks(drawFrame)
                handler.post(drawFrame)
            } else {
                handler.removeCallbacks(drawFrame)
                sensorManager.unregisterListener(this)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(drawFrame)
            sensorManager.unregisterListener(this)
            super.onSurfaceDestroyed(holder)
        }

        override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
            offsetX = (xOffset - 0.5f) * 24f
            offsetY = (yOffset - 0.5f) * 20f
        }

        override fun onTouchEvent(event: MotionEvent) {
            // Touch interaction intentionally subtle; motion parallax comes from the gyroscope.
            super.onTouchEvent(event)
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroX = (gyroX + event.values[1] * 0.7f).coerceIn(-18f, 18f)
                gyroY = (gyroY + event.values[0] * 0.7f).coerceIn(-18f, 18f)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) render(canvas)
            } catch (_: Exception) {
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun render(c: Canvas) {
            val w = c.width.toFloat()
            val h = c.height.toFloat()
            c.drawColor(Color.rgb(2, 3, 6))
            val cx = w * 0.5f + offsetX + gyroX
            val cy = h * 0.49f + offsetY + gyroY
            // Faint atmospheric haze, kept low-luminance for AMOLED screens.
            for (i in 0..5) {
                fill.color = Color.argb(7 + i, 18 + i * 3, 18 + i * 3, 18 + i * 3)
                c.drawCircle(cx - w * 0.12f, cy + h * 0.05f, w * (0.18f + i * 0.075f), fill)
            }
            // Background stars.
            for (i in 0 until 95) {
                val sx = ((i * 137.3f) % w + frame * (if (i % 2 == 0) 0.05f else -0.03f) + w) % w
                val sy = ((i * 241.7f) % h)
                fill.color = Color.argb(if (i % 7 == 0) 170 else 65, 235, 235, 235)
                c.drawCircle(sx, sy, if (i % 7 == 0) 1.5f else 0.65f, fill)
            }
            // Futuristic monochrome orbital telemetry HUD rings.
            c.save()
            c.rotate(frame * 0.035f, cx, cy)
            for (i in 0..2) {
                paint.color = Color.argb(22 + i * 8, 235, 235, 235)
                paint.strokeWidth = if (i == 1) 1.5f else 0.8f
                val rr = w * (0.285f + i * 0.035f)
                c.drawCircle(cx, cy, rr, paint)
                for (tick in 0..35) {
                    val a = tick * (2f * PI.toFloat() / 36f)
                    val inner = rr - if (tick % 3 == 0) w * 0.012f else w * 0.004f
                    val outer = rr
                    c.drawLine(cx + cos(a) * inner, cy + sin(a) * inner * 0.48f,
                        cx + cos(a) * outer, cy + sin(a) * outer * 0.48f, paint)
                }
            }
            c.restore()
            // Event horizon and gravitational lens glow.
            fill.color = Color.BLACK
            c.drawCircle(cx, cy, w * 0.205f, fill)
            val pulse = 0.78f + 0.22f * (0.5f + 0.5f * sin(frame * 0.055f))
            for (i in 0..9) {
                paint.color = Color.argb((18 + i * 9), 220, 220, 220)
                paint.strokeWidth = (w * (0.004f + i * 0.0012f)) * pulse
                val rx = w * (0.215f + i * 0.006f)
                val ry = w * (0.095f + i * 0.003f)
                val oval = RectF(cx - rx, cy - ry, cx + rx, cy + ry)
                c.save()
                c.rotate(-13f + sin(frame * 0.012f) * 2.5f, cx, cy)
                c.drawOval(oval, paint)
                c.restore()
            }
            // Thin white edge highlights give the scene a premium sci-fi look.
            paint.color = Color.argb(48, 245, 245, 245)
            paint.strokeWidth = w * 0.002f
            c.drawOval(RectF(cx - w * 0.31f, cy - w * 0.15f, cx + w * 0.31f, cy + w * 0.15f), paint)
            // Bright, warped accretion arc.
            paint.color = Color.argb((205 * pulse).toInt(), 255, 255, 255)
            paint.strokeWidth = w * 0.008f
            val arcRect = RectF(cx - w * 0.27f, cy - w * 0.13f, cx + w * 0.27f, cy + w * 0.13f)
            c.save()
            c.rotate(-13f + sin(frame * 0.012f) * 2.5f, cx, cy)
            c.drawArc(arcRect, 190f + frame * 0.18f, 150f, false, paint)
            paint.color = Color.argb((90 * pulse).toInt(), 210, 210, 210)
            paint.strokeWidth = w * 0.003f
            c.drawArc(arcRect, 12f - frame * 0.18f, 125f, false, paint)
            c.restore()
            // Infalling particles, looping around the horizon.
            particles.forEach { p ->
                p.angle += p.speed
                p.radius -= 0.22f + p.speed * 12f
                if (p.radius < w * 0.19f) {
                    p.radius = w * (0.36f + Random.nextFloat() * 0.30f)
                    p.angle = Random.nextFloat() * 2f * PI.toFloat()
                }
                val x = cx + cos(p.angle) * p.radius * 1.25f
                val y = cy + sin(p.angle) * p.radius * 0.53f
                fill.color = Color.argb(p.alpha, 245, 245, 245)
                c.drawCircle(x, y, p.size, fill)
                if (p.size > 2.1f) {
                    paint.color = Color.argb(p.alpha / 2, 220, 220, 220)
                    paint.strokeWidth = 1f
                    c.drawLine(x, y, x - cos(p.angle) * 10f, y - sin(p.angle) * 5f, paint)
                }
            }
            // Occasional lens-flare glints along the outer orbit.
            val flareAngle = frame * 0.017f
            val fx = cx + cos(flareAngle) * w * 0.31f
            val fy = cy + sin(flareAngle) * w * 0.145f
            fill.color = Color.argb(115, 255, 255, 255)
            c.drawCircle(fx, fy, 2.2f, fill)
            paint.color = Color.argb(70, 230, 230, 230)
            paint.strokeWidth = 1f
            c.drawLine(fx - 8f, fy, fx + 8f, fy, paint)
            c.drawLine(fx, fy - 8f, fx, fy + 8f, paint)
            frame++
        }
    }
}
