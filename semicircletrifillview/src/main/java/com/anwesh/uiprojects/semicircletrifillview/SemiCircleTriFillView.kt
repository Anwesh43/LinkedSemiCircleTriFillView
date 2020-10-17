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

val parts : Int = 7
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
