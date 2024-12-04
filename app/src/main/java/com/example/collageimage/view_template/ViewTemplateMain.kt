package com.example.collageimage.view_template

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.example.collageimage.R

class ViewTemplateMain(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private lateinit var backgroundBitmap: Bitmap
    private val pathObjects = mutableListOf<Path>()
    private val scaledPaths = mutableListOf<Path>()
    private val selectedBitmaps = mutableListOf<Bitmap?>()
    private var onPathClickListener: ((Int) -> Unit)? = null

    init {
        selectedBitmaps.add(null)
        selectedBitmaps.add(null)
        selectedBitmaps.add(null)
    }

    fun setBackgroundDrawable(imageResId: Int) {
        backgroundBitmap = BitmapFactory.decodeResource(context.resources, imageResId)  // Khởi tạo backgroundBitmap
        invalidate()  // Sau khi khởi tạo, vẽ lại view
    }


    fun setPath(index: Int, pathData: String) {
        val pathObj = PathParser.createPathFromPathData(pathData)
        pathObjects.add(pathObj)
    }

    fun setSelectedImage(bitmap: Bitmap, pathIndex: Int) {
        selectedBitmaps[pathIndex] = bitmap
        invalidate()
    }

    // Thêm phương thức setOnPathClickListener
    fun setOnPathClickListener(listener: (Int) -> Unit) {
        onPathClickListener = listener
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
}
