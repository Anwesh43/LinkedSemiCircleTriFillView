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
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    moveTo(r, 0f)
    arcTo(RectF(-r, -r, r, r), 0f, 180f * sf1)
    lineTo(-r * (1 - sf2), -r * sf2)
    lineTo(r * sf3, -r + r * sf3)
}

fun Canvas.drawSemiCircleTriFill(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf4 : Float = sf.divideScale(3, parts)
    val sf5 : Float = sf.divideScale(4, parts)
    val r : Float = Math.min(w, h) / (sizeFactor * 2)
    save()
    translate(w / 2, h / 2)
    rotate(rot * sf5)
    val path : Path = Path()
    path.semiCircleTriangle(r, sf)
    paint.style = Paint.Style.STROKE
    drawPath(path, paint)
    clipPath(path)
    paint.style = Paint.Style.FILL
    drawRect(RectF(-r, r - 2 * r * sf, r, r), paint)
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

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}