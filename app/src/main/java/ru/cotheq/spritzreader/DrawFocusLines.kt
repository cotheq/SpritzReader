package ru.cotheq.spritzreader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * Created by Kot on 28.11.2017.
 */
internal class DrawFocusLines(context: Context, spritzFocus: Float) : View(context) {
    private val _spritzFocus = spritzFocus
    override fun onDraw(canvas: Canvas) {
        var paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 3F

        canvas.drawLine(0F, 10F, canvas.width.toFloat(), 10F, paint)
        canvas.drawLine(0F, (canvas.height - 10).toFloat(), canvas.width.toFloat(), canvas.height - 10.toFloat(), paint)
        canvas.drawLine(canvas.width * _spritzFocus , 10F, canvas.width * _spritzFocus, 20F, paint)
        canvas.drawLine(canvas.width * _spritzFocus, (canvas.height - 10).toFloat(), canvas.width * _spritzFocus, (canvas.height - 20).toFloat(), paint)

    }
}