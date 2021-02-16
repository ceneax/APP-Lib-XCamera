package ceneax.app.camera

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ceneax.lib.tensorflow.TFLite
import ceneax.lib.tensorflow.runner.DetectorRunner
import ceneax.lib.xcamera.XCamera
import ceneax.lib.xcamera.interfaces.IAnalysisCallback
import ceneax.lib.xcamera.util.ImageUtil
import ceneax.lib.xcamera.widget.drawview.IBaseDrawer
import ceneax.lib.xcamera.widget.drawview.InfoDrawer
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min

class MainActivity : AppCompatActivity(), IAnalysisCallback {

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private val REQUEST_CODE_PERMISSIONS = 10

    private lateinit var mCamera: XCamera

    private val label = arrayOf("人类", "自行车", "汽车", "摩托车", "飞机", "公交车",
        "轮船", "卡车", "船", "交通信号灯", "消防栓", "???", "停止标志", "停车收费表",
        "长椅", "鸟", "猫", "狗", "马", "羊", "牛", "大象", "熊", "斑马", "长颈鹿",
        "???", "背包", "雨伞", "???", "???", "手提包", "茶", "手提箱", "飞盘", "滑雪板",
        "单板滑雪", "运动球", "风筝", "篮球", "棒球手套", "滑板", "冲浪板",
        "网球拍", "瓶子", "???", "红酒杯s", "杯子", "叉", "刀", "勺子", "碗", "香蕉",
        "苹果", "三明治", "橘子", "西兰花", "萝卜", "热狗", "披萨", "甜甜圈", "蛋糕",
        "椅子", "长椅", "盆栽植物", "床", "???", "餐桌", "???", "???", "洗手间", "???", "电视",
        "笔记本电脑", "鼠标", "remote", "键盘", "手机", "微波炉", "烤箱", "烤面包机", "水池",
        "冰箱", "???", "书", "时钟", "花瓶", "剪刀", "玩具熊", "吹风机", "牙刷", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // 闪光灯
        tgFlash.setOnCheckedChangeListener { buttonView, isChecked ->
            mCamera.setTorch(isChecked)
        }
        tgCameraId.setOnCheckedChangeListener { buttonView, isChecked ->
            mCamera.cameraId = if (isChecked) XCamera.CameraId.CAMERA_ID_FRONT else XCamera.CameraId.CAMERA_ID_BACK
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 不要忘记释放资源
        mCamera.release()
        TFLite.getInstance().release()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        TFLite.getInstance().loadModel(assets, "detect.tflite")

        mCamera = XCamera.Builder(this)
            .setPreviewView(pvMain)
            // 可选
            .setMode(XCamera.Mode.CAPTURE_MODE or XCamera.Mode.ANALYSIS_MODE)
            // 可选
            .setAnalysisCallback(this)
            .build()
    }

    private val runner = DetectorRunner.Builder().build()

    override fun onAnalyze(imageProxy: ImageProxy, data: ByteArray, bitmap: Bitmap, matrix: Matrix, drawer: MutableList<IBaseDrawer>) {
        val res = TFLite.getInstance().run(runner.setBitmap(bitmap)
            .setMatrix(matrix)
            .setRotationDegrees(imageProxy.imageInfo.rotationDegrees))

        res.forEach {
            drawer.add(InfoDrawer(it.location, label[it.id] + " " +
                    (it.confidence * 100).toInt() + "%", if (it.confidence < 0.6f) Color.YELLOW else Color.BLUE))
        }
    }

}