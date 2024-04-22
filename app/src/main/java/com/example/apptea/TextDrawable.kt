package com.example.apptea

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class TextDrawable(private val text: String, private val textColor: Int, private val backgroundColor: Int) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = textColor
        paint.textSize = 48f
        paint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val width = bounds.right - bounds.left
        val height = bounds.bottom - bounds.top

        // Draw the background
        paint.color = backgroundColor
        canvas.drawRect(bounds, paint)

        // Draw the text
        paint.color = textColor
        val xCoord = width / 2f
        val yCoord = height / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, xCoord, yCoord, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}