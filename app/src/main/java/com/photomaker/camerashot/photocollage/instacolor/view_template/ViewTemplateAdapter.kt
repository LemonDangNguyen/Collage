package com.photomaker.camerashot.photocollage.instacolor.view_template

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.photomaker.camerashot.photocollage.instacolor.R

class ViewTemplateAdapter(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var backgroundBitmap: Bitmap? = null
    private val pathObjects = mutableListOf<Path>()
    private val scaledPaths = mutableListOf<Path>()
    private val selectedBitmapsAndMatrices = mutableListOf<Pair<Bitmap?, Matrix?>>()

    private var selectedPathIndex: Int = -1
    private var onPathClickListener: ((Int) -> Unit)? = null
    private var onBitmapClickListener: ((Int) -> Unit)? = null

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

    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private val rotationGestureDetector: RotationGestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        rotationGestureDetector = RotationGestureDetector(RotationListener())
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
        if (pathIndex < selectedBitmapsAndMatrices.size) {
            val currentPair = selectedBitmapsAndMatrices[pathIndex]
            selectedBitmapsAndMatrices[pathIndex] = Pair(bitmap, currentPair.second ?: Matrix())
        } else {
            while (selectedBitmapsAndMatrices.size <= pathIndex) {
                selectedBitmapsAndMatrices.add(Pair(null, Matrix()))
            }
            selectedBitmapsAndMatrices[pathIndex] = Pair(bitmap, Matrix())
        }
        invalidate()
    }

    fun setOnPathClickListener(listener: (Int) -> Unit) {
        onPathClickListener = listener
    }

    fun setOnBitmapClickListener(listener: (Int) -> Unit) {
        onBitmapClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        backgroundBitmap?.let {
            val rectFBackground = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawBitmap(it, null, rectFBackground, Paint())
        }

        scaledPaths.clear()
        pathObjects.forEachIndexed { index, path ->
            val scaledPath = Path(path)
            val scaleMatrix = Matrix()
            val scaleX = width / 720f
            val scaleY = height / 1280f
            scaleMatrix.setScale(scaleX, scaleY)
            scaledPath.transform(scaleMatrix)
            scaledPaths.add(scaledPath)
            canvas.drawPath(scaledPath, fillPaint)

            if (index == selectedPathIndex) {
                canvas.drawPath(scaledPath, strokePaint)
            }

            selectedBitmapsAndMatrices.getOrNull(index)?.let { (bitmap, matrix) ->
                bitmap?.let {
                    val bounds = RectF()
                    scaledPath.computeBounds(bounds, true)
                    canvas.save()
                    canvas.clipPath(scaledPath)
                    matrix?.let { m ->
                        canvas.concat(m)
                    }
                    canvas.drawBitmap(it, null, bounds, null)
                    canvas.restore()
                }
            }
        }
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        rotationGestureDetector.onTouchEvent(event)

        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            scaledPaths.forEachIndexed { index, path ->
                if (isPointInPath(path, x, y)) {
                    if (selectedBitmapsAndMatrices[index].first != null) {
                        selectedPathIndex = index
                        onBitmapClickListener?.invoke(index)
                    } else {
                        selectedPathIndex = index
                        onPathClickListener?.invoke(index)
                    }
                    invalidate()
                    return true
                }
            }
            selectedPathIndex = -1
            invalidate()
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

    fun isPathEmpty(pathIndex: Int): Boolean {
        return selectedBitmapsAndMatrices.getOrNull(pathIndex)?.first == null
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (selectedPathIndex != -1) {
                val pair = selectedBitmapsAndMatrices[selectedPathIndex]
                pair.second?.postTranslate(-distanceX, -distanceY)
                invalidate()
                return true
            }
            return false
        }

    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (selectedPathIndex != -1) {
                val scaleFactor = detector.scaleFactor
                val pair = selectedBitmapsAndMatrices[selectedPathIndex]
                pair.second?.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                invalidate()
                return true
            }
            return false
        }
    }

    inner class RotationListener : RotationGestureDetector.OnRotationGestureListener {
        override fun onRotation(rotationDetector: RotationGestureDetector) {
            if (selectedPathIndex != -1) {
                val rotationDegrees = rotationDetector.rotationDegrees
                val pair = selectedBitmapsAndMatrices[selectedPathIndex]
                if (pair.second != null) {
                    val path = scaledPaths[selectedPathIndex]
                    val bounds = RectF()
                    path.computeBounds(bounds, true)
                    val pivotX = bounds.centerX()
                    val pivotY = bounds.centerY()

                    pair.second?.postRotate(rotationDegrees, pivotX, pivotY)
                    invalidate()
                }
            }
        }
    }
}
