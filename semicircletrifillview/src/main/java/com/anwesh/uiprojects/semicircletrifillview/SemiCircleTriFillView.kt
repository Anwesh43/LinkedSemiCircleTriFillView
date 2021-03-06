package com.anwesh.uiprojects.semicircletrifillview

/**
 * Created by anweshmishra on 18/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF

val parts : Int = 6
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 3.7f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val colors : Array<Int> = arrayOf(
        "#F44336",
        "#03A9F4",
        "#FF9800",
        "#4CAF50",
        "#00BCD4"
).map {
    Color.parseColor(it)
}.toTypedArray()
val rot : Float = 90f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Path.semiCircleTriangle(r : Float, sf : Float) {

    moveTo(r, 0f)
    arcTo(RectF(-r, -r, r, r), 0f, 180f)
    lineTo(0f, -r)
    lineTo(r, 0f)

}

fun Canvas.drawSemiCircleTriFill(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    val sf5 : Float = sf.divideScale(4, parts)
    val r : Float = Math.min(w, h) / (sizeFactor * 2)
    save()
    translate(w / 2, h / 2)
    rotate(rot * sf5)
    paint.style = Paint.Style.STROKE
    drawArc(RectF(-r, -r, r, r), 0f, 180f * sf1, false, paint)
    drawLine(-r, 0f, -r * (1 - sf2), -r * sf2, paint)
    drawLine(0f, -r, r * sf3, -r * (1 - sf3), paint)
    val path : Path = Path()
    path.semiCircleTriangle(r, sf)
    clipPath(path)
    paint.style = Paint.Style.FILL
    drawRect(RectF(-r, r - 2 * r * sf4, r, r), paint)
    restore()
}

fun Canvas.drawSCTFNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawSemiCircleTriFill(scale, w, h, paint)
}

class SemiCircleTriFillView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb: (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb: () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }

    }
    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
    data class SCTFNode(private var i : Int, val state : State = State()) {

        private var next : SCTFNode? = null
        private var prev : SCTFNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SCTFNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSCTFNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SCTFNode {
            var curr : SCTFNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SemiCircleTriFill(var i : Int) {

        private var curr : SCTFNode = SCTFNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SemiCircleTriFillView) {

        private val animator : Animator = Animator(view)
        private val sctf : SemiCircleTriFill = SemiCircleTriFill(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            sctf.draw(canvas, paint)
            animator.animate {
                sctf.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sctf.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : SemiCircleTriFillView {
            val view : SemiCircleTriFillView = SemiCircleTriFillView(activity)
            activity.setContentView(view)
            return view
        }
    }
}