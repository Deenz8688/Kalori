package com.deenzstudios.kalori

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Ring bulat progres air minuman.
 * - Bulatan penuh berwarna MERAH (tanda belum minum).
 * - Arc BIRU menutupi bahagian yang telah diminum (naik ikut peratus).
 */
class WaterRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var ratio = 0f
    private var consumedMl = 0
    private var targetMl = 3000

    private val density = resources.displayMetrics.density
    private val stroke = 20f * density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#E53935") // merah = belum minum
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#2196F3") // biru = sudah minum
    }

    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A1A")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 36f * density
    }

    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textAlign = Paint.Align.CENTER
        textSize = 15f * density
    }

    private val arcRect = RectF()

    /** Kemas kini ring dgn jumlah diminum & sasaran (ml). */
    fun setProgress(consumedMl: Int, targetMl: Int) {
        this.consumedMl = consumedMl
        this.targetMl = if (targetMl <= 0) 3000 else targetMl
        this.ratio = (consumedMl.toFloat() / this.targetMl).coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - stroke / 2f - 2f * density
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Bulatan penuh merah (asas — tanda belum cukup minum)
        canvas.drawCircle(cx, cy, radius, trackPaint)

        // Arc biru = jumlah yang telah diminum (mula di atas, ikut arah jam)
        if (ratio > 0f) {
            canvas.drawArc(arcRect, -90f, 360f * ratio, false, progressPaint)
        }

        // Teks tengah: peratus + jumlah liter
        val percentText = "%.0f%%".format(ratio * 100)
        val centerY = cy + percentPaint.textSize / 3f
        canvas.drawText(percentText, cx, centerY, percentPaint)
        canvas.drawText(
            "%.2fL / %.2fL".format(consumedMl / 1000f, targetMl / 1000f),
            cx,
            centerY + subPaint.textSize + 6f * density,
            subPaint
        )
    }
}
