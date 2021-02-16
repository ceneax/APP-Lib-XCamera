package ceneax.lib.xcamera.widget.drawview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawView extends View {

    private Paint mPaint;
    private IBaseDrawer[] mBaseDrawer;

    // 是否正在绘制过程 标志位
    private boolean mIsDrawing = false;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mIsDrawing = true;
        mPaint.reset();

        super.onDraw(canvas);

        if (mBaseDrawer != null && mBaseDrawer.length > 0) {
            for (IBaseDrawer iBaseDrawer : mBaseDrawer) {
                iBaseDrawer.onDraw(canvas, mPaint);
            }
        }

        mIsDrawing = false;
    }

    public void draw(IBaseDrawer... baseDrawer) {
        if (mIsDrawing) {
            return;
        }

        mBaseDrawer = baseDrawer;
        postInvalidate();
    }

}
