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
 * Ring bulat progres kalori harian.
 * - Bulatan asas kelabu (sasaran TDEE).
 * - Arc berwarna menaik ikut peratus pengambilan:
 *   🟢 HIJAU  = bawah sasaran (< 80%)
 *   🟠 OREN   = hampir sasaran (80% - 100%)
 *   🔴 MERAH  = melebihi sasaran (>= 100%)
 */
class CalorieRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var ratio = 0f
    private var consumed = 0.0
    private var target = 0.0

    private val density = resources.displayMetrics.density
    private val stroke = 20f * density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#ECEFF1") // kelabu = sasaran
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#4CAF50")
    }

    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A1A")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 34f * density
    }

    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textAlign = Paint.Align.CENTER
        textSize = 14f * density
    }

    private val arcRect = RectF()

    /** Kemas kini ring dgn jumlah kalori dimakan & sasaran TDEE (kcal). */
    fun setProgress(consumedKcal: Double, targetKcal: Double) {
        this.consumed = consumedKcal
        this.target = if (targetKcal <= 0.0) 2000.0 else targetKcal
        this.ratio = (consumedKcal / this.target).toFloat().coerceIn(0f, 1f)
        progressPaint.color = when {
            this.ratio >= 1f -> Color.parseColor("#E53935") // melebihi sasaran
            this.ratio >= 0.8f -> Color.parseColor("#FB8C00") // hampir sasaran
            else -> Color.parseColor("#4CAF50") // masih dalam sasaran
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - stroke / 2f - 2f * density
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Bulatan asas kelabu
        canvas.drawCircle(cx, cy, radius, trackPaint)

        // Arc berwarna = peratus kalori dimakan (mula di atas, ikut arah jam)
        if (ratio > 0f) {
            canvas.drawArc(arcRect, -90f, 360f * ratio, false, progressPaint)
        }

        // Teks tengah: peratus + jumlah kcal
        val percentText = "%.0f%%".format(ratio * 100)
        val centerY = cy + percentPaint.textSize / 3f
        canvas.drawText(percentText, cx, centerY, percentPaint)
        canvas.drawText(
            "%.0f / %.0f kcal".format(consumed, target),
            cx,
            centerY + subPaint.textSize + 6f * density,
            subPaint
        )
    }
}
