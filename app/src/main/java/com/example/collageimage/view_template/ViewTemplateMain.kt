package com.example.collageimage.view_template

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.collageimage.R
import androidx.core.graphics.PathParser
import com.example.collageimage.SelectImageTemplate

class ViewTemplateMain(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private lateinit var backgroundBitmap: Bitmap
    var path1: String = "M109 108.5h502v277H109v-277Z"
    var path2: String = "M109 420h502v277H109V420Z"
    var path3: String = "M109 727h502v277H109V727Z"
    private val pathObjects = mutableListOf<Path>()
    private val scaledPaths = mutableListOf<Path>()
    private val selectedBitmaps = mutableListOf<Bitmap?>() // Danh sách ảnh cho từng path
    private var selectedPathIndex: Int? = null
    private var onPathClickListener: ((Int) -> Unit)? = null

    init {
        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.template_05)
        backgroundBitmap = backgroundDrawable?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        initializePaths()

        // Khởi tạo danh sách ảnh cho từng path (mặc định không có ảnh)
        selectedBitmaps.add(null) // Path 1
        selectedBitmaps.add(null) // Path 2
        selectedBitmaps.add(null) // Path 3
    }

    private fun initializePaths() {
        pathObjects.clear()
        scaledPaths.clear()
        val path1Obj = PathParser.createPathFromPathData(path1)
        val path2Obj = PathParser.createPathFromPathData(path2)
        val path3Obj = PathParser.createPathFromPathData(path3)
        pathObjects.add(path1Obj)
        pathObjects.add(path2Obj)
        pathObjects.add(path3Obj)
    }

    fun setBackgroundDrawable(imageResId: Int) {
        val backgroundDrawable = ContextCompat.getDrawable(context, imageResId)
        backgroundBitmap = backgroundDrawable?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val rectF = RectF(0f, 0f, width, height)
        canvas.drawBitmap(backgroundBitmap, null, rectF, Paint())

        scaledPaths.clear()
        pathObjects.forEachIndexed { index, path ->
            val scaledPath = Path(path)
            val matrix = android.graphics.Matrix()
            val scaleX = width / 720f
            val scaleY = height / 1280f
            matrix.setScale(scaleX, scaleY)
            scaledPath.transform(matrix)
            scaledPaths.add(scaledPath)

            val paint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.colorPrimary)
            }
            canvas.drawPath(scaledPath, paint)

            selectedBitmaps[index]?.let { bitmap ->
                val bounds = RectF()
                scaledPath.computeBounds(bounds, true)
                canvas.drawBitmap(bitmap, null, bounds, null)
            }
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scaledPaths.forEachIndexed { index, path ->
                    if (isPointInPath(path, x, y)) {
                        onPathClickListener?.invoke(index)
                        return@forEachIndexed
                    }
                }
            }
        }
        return true
    }

    private fun isPointInPath(path: Path, x: Float, y: Float): Boolean {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return x >= bounds.left && x <= bounds.right && y >= bounds.top && y <= bounds.bottom
    }
    fun setSelectedImage(bitmap: Bitmap, pathIndex: Int) {
        selectedBitmaps[pathIndex] = bitmap
        invalidate()
    }
    fun setOnPathClickListener(listener: (Int) -> Unit) {
        onPathClickListener = listener
    }
}
