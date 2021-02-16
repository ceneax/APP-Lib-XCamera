package ceneax.lib.xcamera.widget.drawview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class InfoDrawer implements IBaseDrawer {

    // 字体大小
    private static final float TEXT_SIZE = 40f;
    // 圆角大小
    private static final float ROUND_CORNER = 20f;
    // 线条宽度
    private static final float STROKE_WIDTH = 5f;

    private final float left;
    private final float top;
    private final float right;
    private final float bottom;
    private final String text;
    private final int strokeColor;

    public InfoDrawer(RectF rectF, String text) {
        this(rectF.left, rectF.top, rectF.right, rectF.bottom, text);
    }

    public InfoDrawer(RectF rectF, String text, int strokeColor) {
        this(rectF.left, rectF.top, rectF.right, rectF.bottom, text, strokeColor);
    }

    public InfoDrawer(float left, float top, float right, float bottom, String text) {
        this(left, top, right, bottom, text, Color.BLUE);
    }

    public InfoDrawer(float left, float top, float right, float bottom, String text, int strokeColor) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.text = text == null ? "" : text;
        this.strokeColor = strokeColor;
    }

    @Override
    public void onDraw(Canvas canvas, Paint paint) {
        // 字体大小
        paint.setTextSize(TEXT_SIZE);

        // 线条颜色
        paint.setColor(strokeColor);
        // 线条宽度
        paint.setStrokeWidth(STROKE_WIDTH);
        // 样式: 不填充
        paint.setStyle(Paint.Style.STROKE);
        // 画圆角矩形
        canvas.drawRoundRect(left, top, right, bottom, ROUND_CORNER, ROUND_CORNER, paint);

        // 样式: 填充
        paint.setStyle(Paint.Style.FILL);
        // 填充颜色
        paint.setColor(strokeColor);
        // 文本宽度
        float textWidth = paint.measureText(text);
        // 画文字背景 圆角矩形
        canvas.drawRoundRect(left + STROKE_WIDTH, top + STROKE_WIDTH, left + STROKE_WIDTH + textWidth,
                top + TEXT_SIZE + STROKE_WIDTH, ROUND_CORNER, ROUND_CORNER, paint);

        // 文本颜色
        paint.setColor(Color.WHITE);
        // 画文本
        canvas.drawText(text, left + STROKE_WIDTH, top + TEXT_SIZE, paint);
    }

}
