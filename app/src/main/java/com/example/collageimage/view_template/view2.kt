package com.example.collageimage.view_template

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.example.collageimage.R

class ViewTemplateAdapter22(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private lateinit var backgroundBitmap: Bitmap
    private val pathObjects = mutableListOf<Path>()
    private val scaledPaths = mutableListOf<Path>()
    private val selectedBitmapsAndMatrices = mutableListOf<Pair<Bitmap?, Matrix?>>()
    private var onPathClickListener: ((Int) -> Unit)? = null
    private var matrixGestureDetector: MatrixGestureDetector? = null

    private var selectedPathIndex: Int = -1

    private val fillPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.fill_color)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val strokePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

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
            Pair(bitmap, Matrix())
        invalidate()
    }

    fun setOnPathClickListener(listener: (Int) -> Unit) {
        onPathClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        if (::backgroundBitmap.isInitialized) {
            val rectFBackground = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawBitmap(backgroundBitmap, null, rectFBackground, Paint())
        }

        scaledPaths.clear()
        pathObjects.forEachIndexed { index, path ->
            val scaledPath = Path(path)
            val matrix = Matrix()
            val scaleX = width / 720f
            val scaleY = height / 1280f
            matrix.setScale(scaleX, scaleY)
            scaledPath.transform(matrix)
            scaledPaths.add(scaledPath)

            canvas.drawPath(scaledPath, fillPaint)

            if (index == selectedPathIndex) {
                canvas.drawPath(scaledPath, strokePaint)
            }

            selectedBitmapsAndMatrices[index]?.let { (bitmap, matrix) ->
                bitmap?.let {
                    val bounds = RectF()
                    scaledPath.computeBounds(bounds, true)
                    canvas.save()
                    canvas.clipPath(scaledPath)
                    canvas.concat(matrix!!)
                    canvas.drawBitmap(it, null, bounds, null)
                    canvas.restore()
                }
            }
        }
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                var pathSelected = false
                scaledPaths.forEachIndexed { index, path ->
                    if (isPointInPath(path, x, y)) {
                        if (selectedPathIndex == index) {
                            selectedPathIndex = -1
                        } else {
                            selectedPathIndex = index
                        }
                        pathSelected = true
                        invalidate()

                        if (selectedBitmapsAndMatrices[index].first != null) {
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

                if (!pathSelected) {
                    selectedPathIndex = -1
                    invalidate()
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
        val region = Region()
        val bounds = RectF()
        path.computeBounds(bounds, true)
        region.setPath(path, Region(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt()))
        return region.contains(x.toInt(), y.toInt())
    }

}