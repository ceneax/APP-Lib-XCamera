package ceneax.lib.xcamera.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;

import ceneax.lib.xcamera.widget.drawview.DrawView;
import ceneax.lib.xcamera.widget.drawview.IBaseDrawer;
import ceneax.lib.xcamera.widget.drawview.XSurfaceView;

/**
 * @Description: XPreviewView
 * @Date: 2020/11/12 12:06
 * @Author: ceneax
 */
public class XPreviewView extends FrameLayout {

    private final Context context;

    // CameraX的预览组件
    private PreviewView mPreviewView;
    // 自定义的DrawView
    private DrawView mDrawView;

    public XPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public XPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public XPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this.context = context;
        initView();
    }

    private void initView() {
        mPreviewView = new PreviewView(context);
        mDrawView = new DrawView(context);

        addView(mPreviewView);
        addView(mDrawView);
    }

    public PreviewView getPreviewView() {
        return mPreviewView;
    }

    public Preview.SurfaceProvider getSurfaceProvider() {
        return mPreviewView.getSurfaceProvider();
    }

    public Bitmap getBitmap() {
        return mPreviewView.getBitmap();
    }

    public DrawView getDrawView() {
        return mDrawView;
    }

    public void draw(IBaseDrawer... baseDrawer) {
        mDrawView.draw(baseDrawer);
    }

}
