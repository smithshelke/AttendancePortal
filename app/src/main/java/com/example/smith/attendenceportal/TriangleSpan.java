package com.example.smith.attendenceportal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.style.LineBackgroundSpan;

public class TriangleSpan implements LineBackgroundSpan {
    private int color;


    TriangleSpan(int color) {
        this.color = color;
    }

    @Override
    public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom, CharSequence charSequence, int start, int end, int lineNum) {
        int oldColor = paint.getColor();
        if (this.color != 0) {
            paint.setColor(this.color);
        }
        drawTriangle(canvas, paint, (left+right)/2, 60, 28);
        paint.setColor(oldColor);
    }
    private
    void drawTriangle(Canvas canvas, Paint paint, int x, int y, int width) {
        int halfWidth = width / 2;
        Path path = new Path();
        path.moveTo(x, y - halfWidth); // Top
        path.lineTo(x - halfWidth, y + halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y + halfWidth); // Bottom right
        path.lineTo(x, y - halfWidth); // Back to Top
        path.close();

        canvas.drawPath(path, paint);
    }
}
