package com.biglifts.workouttracker.ui.workout.timer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.biglifts.workouttracker.R

class TimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.parseColor("#E0E0E0")
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = context.getColor(R.color.primary)
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 80f
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
        isFakeBoldText = true
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 36f
        textAlign = Paint.Align.CENTER
        color = Color.GRAY
    }

    private val rect = RectF()
    private var progress = 1f
    private var totalTime = 90
    private var currentTime = 90

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) - 30f

        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Background circle
        canvas.drawArc(rect, 0f, 360f, false, circlePaint)

        // Progress arc
        val sweepAngle = progress * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Time text
        val minutes = currentTime / 60
        val seconds = currentTime % 60
        val timeText = String.format("%d:%02d", minutes, seconds)
        canvas.drawText(timeText, centerX, centerY + 20f, textPaint)

        // Label
        canvas.drawText("REST", centerX, centerY + 70f, labelPaint)
    }

    fun setProgress(current: Int, total: Int) {
        currentTime = current
        totalTime = total
        progress = if (total > 0) current.toFloat() / total else 0f

        // Color based on progress
        progressPaint.color = when {
            progress > 0.5f -> context.getColor(R.color.primary)
            progress > 0.2f -> context.getColor(R.color.warning)
            else -> context.getColor(R.color.error)
        }

        invalidate()
    }
}