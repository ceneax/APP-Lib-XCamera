package ceneax.lib.xcamera.interfaces;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.camera.core.ImageProxy;

import java.util.List;

import ceneax.lib.xcamera.widget.drawview.IBaseDrawer;

public interface IAnalysisCallback {

    /**
     * @param imageProxy 图像代理
     * @param data NV21 数据
     * @param bitmap 转换后的Bitmap
     * @param matrix 坐标转换
     * @param drawer 绘制集合
     */
    void onAnalyze(ImageProxy imageProxy, byte[] data, Bitmap bitmap, Matrix matrix, List<IBaseDrawer> drawer);

}