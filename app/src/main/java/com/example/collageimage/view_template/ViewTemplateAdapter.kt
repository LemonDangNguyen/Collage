package com.example.collageimage.view_template

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.example.collageimage.R

class ViewTemplateAdapter(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private lateinit var backgroundBitmap: Bitmap
    private val pathObjects = mutableListOf<Path>()
    private val scaledPaths = mutableListOf<Path>()
    private val selectedBitmapsAndMatrices = mutableListOf<Pair<Bitmap?, Matrix?>>()
    private var onPathClickListener: ((Int) -> Unit)? = null
    private var matrixGestureDetector: MatrixGestureDetector? = null

    fun setBackgroundDrawable(imageResId: Int) {
        backgroundBitmap = BitmapFactory.decodeResource(context.resources, imageResId)
        invalidate()
    }

    fun setPath(index: Int, pathData: String) {
        val pathObj = PathParser.createPathFromPathData(pathData)
        pathObjects.add(pathObj)

        if (index >= selectedBitmapsAndMatrices.size) {
            selectedBitmapsAndMatrices.add(Pair(null, Matrix()))
        }
    }

    fun setSelectedImage(bitmap: Bitmap, pathIndex: Int) {
        selectedBitmapsAndMatrices[pathIndex] =
            Pair(bitmap, Matrix())  // Thêm ảnh và khởi tạo Matrix mới cho ảnh
        invalidate()
    }

    fun setOnPathClickListener(listener: (Int) -> Unit) {
        onPathClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        scaledPaths.clear()
        pathObjects.forEachIndexed { index, path ->
            val scaledPath = Path(path)
            val matrix = Matrix()
            val scaleX = width / 720f
            val scaleY = height / 1280f
            matrix.setScale(scaleX, scaleY)
            scaledPath.transform(matrix)
            scaledPaths.add(scaledPath)
            val paint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.fill_color)
            }
            canvas.drawPath(scaledPath, paint)
            selectedBitmapsAndMatrices[index]?.let { (bitmap, matrix) ->
                bitmap?.let {
                    val bounds = RectF()
                    scaledPath.computeBounds(bounds, true)
                    canvas.save()
                    canvas.clipPath(scaledPath)  // Cắt vùng theo path
                    canvas.concat(matrix!!)  // Áp dụng Matrix cho canvas
                    canvas.drawBitmap(it, null, bounds, null)
                    canvas.restore()
                }
            }
        }
        super.onDraw(canvas)
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(backgroundBitmap, null, rectF, Paint())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scaledPaths.forEachIndexed { index, path ->
                    if (isPointInPath(path, x, y)) {
                        if (selectedBitmapsAndMatrices[index].first != null) {
                            // Khởi tạo MatrixGestureDetector
                            matrixGestureDetector = selectedBitmapsAndMatrices[index].second?.let {
                                MatrixGestureDetector(
                                    it,
                                    object : MatrixGestureDetector.OnMatrixChangeListener {
                                        override fun onChange(matrix: Matrix?) {
                                            selectedBitmapsAndMatrices[index] =
                                                selectedBitmapsAndMatrices[index].copy(second = matrix!!)
                                            invalidate()
                                        }
                                    })
                            }
                            matrixGestureDetector?.onTouchEvent(event)
                        } else {
                            onPathClickListener?.invoke(index)
                        }
                        return@forEachIndexed
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                matrixGestureDetector?.onTouchEvent(event)
            }

            MotionEvent.ACTION_POINTER_UP -> {}

            MotionEvent.ACTION_UP -> {
                matrixGestureDetector?.onTouchEvent(event)
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
