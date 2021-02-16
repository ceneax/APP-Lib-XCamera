package ceneax.lib.xcamera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.core.impl.CameraInternal;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ComponentActivity;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ceneax.lib.xcamera.interfaces.IAnalysisCallback;
import ceneax.lib.xcamera.util.ImageUtil;
import ceneax.lib.xcamera.widget.XPreviewView;
import ceneax.lib.xcamera.widget.drawview.IBaseDrawer;

/**
 * @Description: CameraX
 * @Date: 2021/2/14 16:58
 * @Author: ceneax
 */
public class XCamera {

    private final Builder mBuilder;

    private ExecutorService mCameraExecutor;

    private ProcessCameraProvider mCameraProvider;

    // useCases
    private Preview mPreview;
    private ImageCapture mImageCapture;
    private ImageAnalysis mImageAnalyzer;

    // 按需开启的功能集合
    private final List<UseCase> mUseCaseList = new ArrayList<>();

    private XCamera(Builder builder) {
        mBuilder = builder;
        initCamera();
    }

    /**
     * 初始化CameraX
     */
    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mBuilder.context);

        cameraProviderFuture.addListener(() -> {
            try {
                // 将Camera的lifecycle与lifecycle owner进行绑定
                mCameraProvider = cameraProviderFuture.get();

                mUseCaseList.clear();

                // 开启预览功能
                mPreview = new Preview.Builder().build();
                mPreview.setSurfaceProvider(mBuilder.xPreviewView.getSurfaceProvider());
                mUseCaseList.add(mPreview);

                // 按需开启图像捕获功能
                if (Mode.contains(mBuilder.mode, Mode.CAPTURE_MODE)) {
                    mImageCapture = new ImageCapture.Builder().build();
                    mUseCaseList.add(mImageCapture);
                }
                // 按需开启图像分析功能
                if (Mode.contains(mBuilder.mode, Mode.ANALYSIS_MODE)) {
                    mImageAnalyzer = new ImageAnalysis.Builder().build();
                    mCameraExecutor = Executors.newSingleThreadExecutor();
                    // 坐标转换矩阵
                    Matrix matrix = new Matrix();
                    // 绘制集合
                    List<IBaseDrawer> drawerList = new ArrayList<>();
                    // 分析回调
                    mImageAnalyzer.setAnalyzer(mCameraExecutor, imageProxy -> {
                        if (mBuilder.analysis == null) {
                            // 直接返回，不执行close；那么，就不会有下一帧数据传过来
                            return;
                        }

                        // 清空绘制
                        drawerList.clear();

                        byte[] data = ImageUtil.yuv420ToNV21(imageProxy);
                        Bitmap bitmap = ImageUtil.nv21ToBitmap(data, imageProxy.getWidth(), imageProxy.getHeight());
                        mBuilder.analysis.onAnalyze(imageProxy, data, bitmap, ImageUtil.getCorrectionMatrix(matrix, imageProxy,
                                mBuilder.xPreviewView.getPreviewView()), drawerList);

                        // 绘制
                        mBuilder.xPreviewView.draw(drawerList.toArray(new IBaseDrawer[] {}));

                        // 只有执行close方法之后，才会继续接收下一帧数据
                        imageProxy.close();
                    });
                    mUseCaseList.add(mImageAnalyzer);
                }

                bindAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(mBuilder.context));
    }

    /**
     * 绑定
     */
    private void bindAll() {
        // 绑定之前需要取消绑定全部
        mCameraProvider.unbindAll();
        mCameraProvider.bindToLifecycle(mBuilder.context, new CameraSelector.Builder()
                .requireLensFacing(mBuilder.cameraId)
                .build(), mUseCaseList.toArray(new UseCase[0]));
    }

    /**
     * 获取相机对象
     */
    @SuppressLint("RestrictedApi")
    public CameraInternal getCamera() {
        return mPreview.getCamera();
    }

    /**
     * 获取相机ID
     */
    public int getCameraId() {
        return mBuilder.cameraId;
    }

    /**
     * 设置相机ID
     */
    public void setCameraId(int cameraId) {
        mBuilder.setCameraId(cameraId);
        bindAll();
    }

    /**
     * 控制闪光灯
     * @param enable 是否启用
     */
    public void setTorch(boolean enable) {
        getCamera().getCameraControl().enableTorch(enable);
    }

    /**
     * 通过系统方法进行拍照
     */
    public void takePhotoBySystem() {
        if (mImageCapture == null) {
            return;
        }
    }

    /**
     * 通过预览截取方法进行拍照
     */
    public Bitmap takePhotoByPreview() {
        return mBuilder.xPreviewView.getBitmap();
    }

    /**
     * 设置拍照的闪光灯模式
     */
    public void setFlashMode(int flashMode) {
        if (mImageCapture == null) {
            return;
        }
        mImageCapture.setFlashMode(flashMode);
    }

    /**
     * 获取当前拍照时的闪光灯模式
     */
    public int getFlashMode() {
        if (mImageCapture == null) {
            return FlashMode.FLASH_MODE_AUTO;
        }
        return mImageCapture.getFlashMode();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mCameraExecutor != null) {
            mCameraExecutor.shutdown();
        }
        mUseCaseList.clear();
    }

    /**
     * 建造者模式
     */
    public static class Builder {
        private final ComponentActivity context;
        // 相机模式
        private int mode;
        // 相机预览View
        private XPreviewView xPreviewView;
        // 相机ID，默认后置
        private int cameraId = CameraId.CAMERA_ID_BACK;
        // 图像分析回调
        private IAnalysisCallback analysis;

        public Builder(ComponentActivity context) {
            this.context = context;
            xPreviewView = new XPreviewView(context);
        }

        public Builder setMode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder setPreviewView(XPreviewView xPreviewView) {
            this.xPreviewView = xPreviewView;
            return this;
        }

        public Builder setCameraId(int cameraId) {
            this.cameraId = cameraId;
            return this;
        }

        public Builder setAnalysisCallback(IAnalysisCallback analysis) {
            this.analysis = analysis;
            return this;
        }

        public XCamera build() {
            return new XCamera(this);
        }
    }

    /**
     * 相机模式
     */
    public static class Mode {
        // 图像捕获模式
        public static final int CAPTURE_MODE = 1 << 0;
        // 图像分析模式
        public static final int ANALYSIS_MODE = 1 << 1;

        /**
         * 判断是否包含某个模式
         */
        public static boolean contains(int allMode, int mode) {
            return (allMode & mode) == mode;
        }

        /**
         * 判断是否仅仅只包含某个模式
         */
        public static boolean equals(int allMode, int mode) {
            return allMode == mode;
        }
    }

    /**
     * 相机ID
     */
    public static class CameraId {
        // 后置
        public static final int CAMERA_ID_BACK = CameraSelector.LENS_FACING_BACK;
        // 前置
        public static final int CAMERA_ID_FRONT = CameraSelector.LENS_FACING_FRONT;
    }

    /**
     * 闪光灯模式
     */
    public static class FlashMode {
        // 开
        public static final int FLASH_MODE_ON = ImageCapture.FLASH_MODE_ON;
        // 关
        public static final int FLASH_MODE_OFF = ImageCapture.FLASH_MODE_OFF;
        // 自动
        public static final int FLASH_MODE_AUTO = ImageCapture.FLASH_MODE_AUTO;
    }

}