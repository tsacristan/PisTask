package com.example.pistask.presentation.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Arrosoir custom avec niveau d'eau animé.
 */
class WateringCanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var waterLevel: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    private var displayedLevel: Float = waterLevel.toFloat()

    private val paintCoque = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E9DCC9")
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val paintWater = Paint(Paint.ANTI_ALIAS_FLAG)

    private val bodyRect = RectF()
    private val pathArrosoir = Path()

    fun animateTo(targetLevel: Int, onEnd: (() -> Unit)? = null) {
        val start = displayedLevel
        val end = targetLevel.toFloat().coerceIn(0f, 100f)
        ValueAnimator.ofFloat(start, end).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                displayedLevel = it.animatedValue as Float
                waterLevel = displayedLevel.toInt()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onEnd?.invoke()
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val centerY = h / 2f

        val bodyWidth = w * 0.48f
        val bodyHeight = h * 0.58f
        bodyRect.set(
            centerX - bodyWidth / 2,
            centerY - bodyHeight / 2 + 8f,
            centerX + bodyWidth / 2,
            centerY + bodyHeight / 2 + 8f
        )

        // Shader eau (recalculé à chaque draw)
        paintWater.shader = LinearGradient(
            0f, bodyRect.top, 0f, bodyRect.bottom,
            Color.parseColor("#60A5FA"),
            Color.parseColor("#3B82F6"),
            Shader.TileMode.CLAMP
        )

        // 1. Eau à l'intérieur (clippée)
        val waterHeight = (bodyHeight - 8f) * (displayedLevel / 100f)
        canvas.save()
        val clipPath = Path().apply {
            addRoundRect(bodyRect, 30f, 30f, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawRect(
            bodyRect.left,
            bodyRect.bottom - waterHeight,
            bodyRect.right,
            bodyRect.bottom,
            paintWater
        )
        canvas.restore()

        // 2. Corps contour
        paintCoque.style = Paint.Style.STROKE
        canvas.drawRoundRect(bodyRect, 30f, 30f, paintCoque)

        // 3. Anse (droite)
        pathArrosoir.reset()
        pathArrosoir.moveTo(bodyRect.right, bodyRect.top + 16f)
        pathArrosoir.cubicTo(
            bodyRect.right + 50f, bodyRect.top - 4f,
            bodyRect.right + 50f, bodyRect.bottom + 4f,
            bodyRect.right, bodyRect.bottom - 16f
        )
        canvas.drawPath(pathArrosoir, paintCoque)

        // 4. Bec verseur (gauche)
        pathArrosoir.reset()
        pathArrosoir.moveTo(bodyRect.left, centerY + 8f)
        pathArrosoir.lineTo(bodyRect.left - 52f, centerY - 36f)
        canvas.drawPath(pathArrosoir, paintCoque)

        // 5. Pomme d'arrosage
        paintCoque.style = Paint.Style.FILL
        canvas.drawOval(
            bodyRect.left - 66f, centerY - 50f,
            bodyRect.left - 38f, centerY - 22f,
            paintCoque
        )
        paintCoque.style = Paint.Style.STROKE
    }
}
