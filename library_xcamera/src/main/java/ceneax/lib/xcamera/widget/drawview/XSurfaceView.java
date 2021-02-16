package ceneax.lib.xcamera.widget.drawview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * @Description: XSurfaceView
 * @Date: 2020/11/12 12:06
 * @Author: ceneax
 */
public class XSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable, View.OnTouchListener {

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private final Paint mPaint = new Paint();

    private IBaseDrawer mBaseDrawer;

    // 是否正在绘制过程 标志位
    private boolean mIsDrawing = false;

    // 是否需要手指滑动功能
    private boolean mTouch = false;
    // 记录最后一次手指滑动的坐标
    private final PointF lastPoint = new PointF();

    public XSurfaceView(Context context) {
        this(context, null);
    }

    public XSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        mIsDrawing = true;

        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            mPaint.reset();

            mBaseDrawer.onDraw(mCanvas, mPaint);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }

        mIsDrawing = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTouch) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastPoint.set(event.getRawX(), event.getRawY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX() - lastPoint.x;
                    float y = event.getRawY() - lastPoint.y;
                    v.setX(v.getX() + x);
                    v.setY(v.getY() + y);
                    lastPoint.set(event.getRawX(), event.getRawY());
                    break;
            }
        }
        return mTouch;
    }

    /**
     * 绘制
     */
    public void drawIt(IBaseDrawer baseDrawer) {
        if (mIsDrawing) {
            return;
        }

        mBaseDrawer = baseDrawer;
        new Thread(this).start();
    }

    /**
     * 可否跟随手指移动
     * @param touch 标志位
     */
    public void touch(boolean touch) {
        mTouch = touch;
    }

}
