package com.example.smartair.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SimpleLineChart extends View {

    private List<DataPoint> dataPoints = new ArrayList<>();
    private Paint linePaint;
    private Paint pointPaint;
    private Paint axisPaint;
    private Paint textPaint;

    public SimpleLineChart(Context context) {
        super(context);
        init();
    }

    public SimpleLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#2196F3")); // Blue
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#1976D2")); // Darker Blue
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        axisPaint = new Paint();
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStrokeWidth(2f);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
    }

    public void setData(List<DataPoint> points) {
        this.dataPoints = points;
        invalidate(); // Redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.isEmpty()) {
            canvas.drawText("No data available", getWidth() / 2f - 100, getHeight() / 2f, textPaint);
            return;
        }

        float padding = 50f;
        float width = getWidth() - 2 * padding;
        float height = getHeight() - 2 * padding;

        // Draw Axes
        canvas.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding, axisPaint); // X
        canvas.drawLine(padding, padding, padding, getHeight() - padding, axisPaint); // Y

        // Find Min/Max
        long minX = Long.MAX_VALUE, maxX = Long.MIN_VALUE;
        float maxY = 0;

        for (DataPoint p : dataPoints) {
            if (p.timestamp < minX) minX = p.timestamp;
            if (p.timestamp > maxX) maxX = p.timestamp;
            if (p.value > maxY) maxY = p.value;
        }

        // Avoid divide by zero
        if (maxY == 0) maxY = 5; // Default scale
        long timeRange = maxX - minX;
        if (timeRange == 0) timeRange = 1; // Avoid divide by zero

        Path path = new Path();
        boolean first = true;

        for (DataPoint p : dataPoints) {
            float x = padding + ((p.timestamp - minX) / (float) timeRange) * width;
            // Invert Y (canvas 0 is top)
            float y = (getHeight() - padding) - (p.value / maxY) * height;

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }

            // Draw point
            canvas.drawCircle(x, y, 8f, pointPaint);
        }

        canvas.drawPath(path, linePaint);
    }

    public static class DataPoint implements Comparable<DataPoint> {
        public long x;
        public long y;
        long timestamp;
        float value;

        public DataPoint(long timestamp, float value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        @Override
        public int compareTo(DataPoint o) {
            return Long.compare(this.timestamp, o.timestamp);
        }
    }
}
