package ceneax.lib.xcamera.widget.drawview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PointDrawer implements IBaseDrawer {

    // 点的半径
    private static final float CIRCLE_RADIUS = 8f;

    private final float cx;
    private final float cy;
    private final int pointColor;

    public PointDrawer(float cx, float cy) {
        this(cx, cy, Color.GREEN);
    }

    public PointDrawer(float cx, float cy, int pointColor) {
        this.cx = cx;
        this.cy = cy;
        this.pointColor = pointColor;
    }

    @Override
    public void onDraw(Canvas canvas, Paint paint) {
        // 设置点的颜色
        paint.setColor(pointColor);

        // 绘制点
        canvas.drawCircle(cx, cy, CIRCLE_RADIUS, paint);
    }

}
