package com.example.pistask.presentation.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.pow
import kotlin.random.Random

/**
 * Vue overlay qui anime des particules (eau + pistaches) en trajectoire de Bézier
 * depuis la checkbox cochée jusqu'à l'arrosoir.
 */
class WaterFlowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        // La vue ne doit pas intercepter les touches
        isClickable = false
        isFocusable = false
    }

    private val particles = mutableListOf<FlowParticle>()
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG) // prealloué hors onDraw

    private val waterColors = intArrayOf(0xFF3B82F6.toInt(), 0xFF60A5FA.toInt(), 0xFF93C5FD.toInt())
    private val pistachioColors = intArrayOf(0xFF93C572.toInt(), 0xFFD4E09B.toInt(), 0xFF6B9E3D.toInt())

    fun startFlow(startX: Float, startY: Float, endX: Float, endY: Float) {
        val count = 18
        val burst = mutableListOf<FlowParticle>()

        repeat(count) {
            val isPistachio = Random.nextFloat() > 0.65f
            burst.add(
                FlowParticle(
                    startX = startX,
                    startY = startY,
                    endX = endX,
                    endY = endY,
                    type = if (isPistachio) ParticleType.PISTACHIO else ParticleType.WATER,
                    curve = (Random.nextFloat() - 0.5f) * 320f,
                    size = Random.nextFloat() * 9f + 5f,
                    delay = Random.nextFloat() * 0.25f   // décalage léger entre particules
                )
            )
        }

        particles.addAll(burst)

        val animator = ValueAnimator.ofFloat(0f, 1.25f) // 1.25 pour couvrir les délais
        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { anim ->
            val raw = anim.animatedValue as Float
            burst.forEach { it.rawProgress = raw }
            invalidate()
            if (raw >= 1.25f) {
                particles.removeAll(burst.toSet())
                invalidate()
            }
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (particles.isEmpty()) return

        particles.forEach { p ->
            val t = ((p.rawProgress - p.delay) / 1f).coerceIn(0f, 1f)
            if (t <= 0f || t >= 1f) return@forEach

            val cpX = (p.startX + p.endX) / 2f + p.curve
            val cpY = minOf(p.startY, p.endY) - 280f

            val invT = 1f - t
            val x = invT.pow(2) * p.startX + 2f * invT * t * cpX + t.pow(2) * p.endX
            val y = invT.pow(2) * p.startY + 2f * invT * t * cpY + t.pow(2) * p.endY

            drawPaint.color = when (p.type) {
                ParticleType.WATER -> waterColors[Random.nextInt(waterColors.size)]
                ParticleType.PISTACHIO -> pistachioColors[Random.nextInt(pistachioColors.size)]
            }
            drawPaint.alpha = ((1f - t.pow(2)) * 230).toInt()

            canvas.drawCircle(x, y, p.size, drawPaint)
        }
    }

    enum class ParticleType { WATER, PISTACHIO }

    private data class FlowParticle(
        val startX: Float, val startY: Float,
        val endX: Float, val endY: Float,
        val type: ParticleType,
        val curve: Float,
        val size: Float,
        val delay: Float,
        var rawProgress: Float = 0f
    )
}
