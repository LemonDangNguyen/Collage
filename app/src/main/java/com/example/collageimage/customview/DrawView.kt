package com.example.collageimage.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.collageimage.R
import com.example.collageimage.customview.model.DrawInfo
import com.example.collageimage.customview.model.PaintPath

import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList

class DrawView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mPath: Path? = null
    private var pathListHistory = mutableListOf<PaintPath?>()
    private var pathListUndo = mutableListOf<PaintPath?>()
    private var currentX: Float? = null
    private var currentY: Float? = null
    private var touchTolerance = 4f
    private var color = Color.BLACK
    private var penWidth = 10f
    private var isEraserMode = false
    var isColoringMode = false
    private var cachedBitmap: Bitmap? = null

    private val savedMatrix = Matrix()
    private val zoomMatrix = Matrix()
    private val zoomMatrixInverse = Matrix()

    private var mode = NONE
    private var startPoint = PointF()
    private var oldDist = 1f
     var isInteractable: Boolean = true
    private var isFirstDraw = true
    private var onDrawChange: OnDrawChange? = null
    var backgroundBitmap: Bitmap? = null

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = penWidth
        style = Paint.Style.STROKE
        color = this@DrawView.color
    }

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.RED // Màu mặc định cho tô màu
    }

    companion object {
        private const val NONE = 0
        private const val ZOOM = 2
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DrawView,
            0, 0
        ).apply {
            try {
                color = getColor(R.styleable.DrawView_penColor, Color.BLACK)
                penWidth = getFloat(R.styleable.DrawView_penWidth, 10f)
                setBackgroundColor(Color.TRANSPARENT)
            } finally {
                recycle()
            }
        }
    }
    fun setInteractionEnabled(enabled: Boolean) {
        isInteractable = enabled
        // Thay đổi độ trong suốt hoặc các thuộc tính khác nếu cần
        alpha = if (enabled) 1.0f else 0.5f
        invalidate()
    }
    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(zoomMatrix)

        canvas.drawColor(Color.TRANSPARENT)

        // Vẽ nền nếu có
        backgroundBitmap?.let {
            val paint = Paint().apply {
                alpha = (0.3f * 255).toInt()
            }
            val srcRect = Rect(0, 0, it.width, it.height)
            val destRect = Rect(0, 0, width, height)
            canvas.drawBitmap(it, srcRect, destRect, paint)
        }

        for (paintPath in pathListHistory) {
            if (paintPath != null) {
                canvas.drawPath(paintPath.path, paintPath.paint)
            }
        }

        canvas.restore()
    }

    interface OnDrawChange {
        fun onDrawChange()
    }

    fun setOnDrawChange(listener: OnDrawChange) {
        this.onDrawChange = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInteractable){
            return false
        }
        val x = event.x
        val y = event.y

        if (isColoringMode) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                CoroutineScope(Dispatchers.Main).launch {
                    handleColoringMode(x, y)
                }
            }
            return true
        } else {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    zoomMatrixInverse.reset()
                    zoomMatrix.invert(zoomMatrixInverse)
                    val transformedPoint = floatArrayOf(x, y)
                    zoomMatrixInverse.mapPoints(transformedPoint)
                    touchStart(transformedPoint[0], transformedPoint[1])
                    invalidate()
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(zoomMatrix)
                        midPoint(startPoint, event)
                        mode = ZOOM
                    }
                    if (event.pointerCount > 1) {
                        touchUp()
                        if (pathListHistory.size > 0) {
                            pathListHistory.removeAt(pathListHistory.size - 1)
                            invalidate()
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    touchUp()
                    invalidate()
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == ZOOM && event.pointerCount > 1) {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            zoomMatrix.set(savedMatrix)

                            val endPoint = PointF()
                            midPoint(endPoint, event)

                            val scale = newDist / oldDist
                            zoomMatrix.postScale(scale, scale, startPoint.x, startPoint.y)
                            zoomMatrix.postTranslate(
                                endPoint.x - startPoint.x,
                                endPoint.y - startPoint.y
                            )
                        }
                    } else {
                        zoomMatrixInverse.reset()
                        zoomMatrix.invert(zoomMatrixInverse)
                        val transformedPoint = floatArrayOf(x, y)
                        zoomMatrixInverse.mapPoints(transformedPoint)
                        touchMove(transformedPoint[0], transformedPoint[1])
                    }
                    invalidate()
                }
            }
        }
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    private fun touchStart(xPos: Float, yPos: Float) {
        mPath = Path()
        val paintPath = PaintPath(mPath!!, getPaint())
        pathListHistory.add(paintPath)
        pathListUndo.clear()
        mPath!!.reset()
        mPath!!.moveTo(xPos, yPos)

        currentX = xPos
        currentY = yPos

        //playSoundLooping()
    }


    private fun touchMove(xPos: Float, yPos: Float) {
        val dx = abs(xPos - currentX!!)
        val dy = abs(yPos - currentY!!)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            mPath!!.quadTo(currentX!!, currentY!!, (xPos + currentX!!) / 2, (yPos + currentY!!) / 2)
            currentX = xPos
            currentY = yPos
        }
    }

    private fun touchUp() {
        mPath!!.lineTo(currentX!!, currentY!!)
        onDrawChange?.onDrawChange()
        if (pathListHistory.size == 1 && isFirstDraw) {
            isFirstDraw = false
        }
    //    stopSound()
    }

    private suspend fun handleColoringMode(x: Float, y: Float) {
        withContext(Dispatchers.Default) {
            val targetRegionPath = getRegionForFill(x.toInt(), y.toInt())
            withContext(Dispatchers.Main) {
                if (targetRegionPath != null) {
                    // Tạo một Paint mới với màu hiện tại của fillPaint
                    val newFillPaint = Paint(fillPaint).apply {
                        color = fillPaint.color // Sử dụng màu hiện tại của fillPaint
                    }
                    // Lưu path và paint riêng cho vùng tô màu này
                    pathListHistory.add(PaintPath(targetRegionPath, newFillPaint, true))
                    updateCachedBitmap()
                    invalidate()
                }
            }
        }
    }


    private fun updateCachedBitmap() {
        if (cachedBitmap == null) {
            cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(cachedBitmap!!)
        canvas.drawColor(Color.TRANSPARENT) // Reset màu nền
        for (paintPath in pathListHistory) {
            if (paintPath != null) {
                canvas.drawPath(paintPath.path, paint)
            }
        }
    }

    private fun getRegionForFill(x: Int, y: Int): Path? {
        val bitmap = cachedBitmap ?: createBitmapFromPaths()
        if (bitmap.getPixel(x, y) == fillPaint.color) return null // Kiểm tra nếu điểm đã tô màu

        val region = Region()
        val path = Path()

        if (!fillRegion(bitmap, x, y, region)) return null

        region.getBoundaryPath(path)
        path.fillType = Path.FillType.EVEN_ODD
        return path
    }

    private fun fillRegion(bitmap: Bitmap, startX: Int, startY: Int, region: Region): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        val queue = LinkedList<Point>()
        val visited = Array(height) { BooleanArray(width) }
        queue.add(Point(startX, startY))

        while (queue.isNotEmpty()) {
            val point = queue.remove()

            if (point.x < 0 || point.x >= width || point.y < 0 || point.y >= height || visited[point.y][point.x]) continue

            visited[point.y][point.x] = true

            if (bitmap.getPixel(point.x, point.y) != Color.WHITE) continue

            region.union(Rect(point.x, point.y, point.x + 1, point.y + 1))

            queue.add(Point(point.x + 1, point.y))
            queue.add(Point(point.x - 1, point.y))
            queue.add(Point(point.x, point.y + 1))
            queue.add(Point(point.x, point.y - 1))
        }
        return true
    }

    private fun createBitmapFromPaths(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        for (paintPath in pathListHistory) {
            if (paintPath != null) {
                canvas.drawPath(paintPath.path, paint)
            }
        }
        return bitmap
    }

    fun toggleColoringMode() {
        isColoringMode = !isColoringMode
    }
    fun setFillColor(color: Int) {
        fillPaint.color = color

    }



    fun setUndo() {
        val size = pathListHistory.size
        if (size > 0) {
            pathListUndo.add(pathListHistory[size - 1])
            pathListHistory.removeAt(size - 1)
            onDrawChange?.onDrawChange()
            invalidate()
        }
    }

    fun setRedo() {
        val size = pathListUndo.size
        if (size > 0) {
            pathListHistory.add(pathListUndo[size - 1])
            pathListUndo.removeAt(size - 1)
            onDrawChange?.onDrawChange()
            invalidate()
        }
    }

    fun setPenColor(color: Int) {
        this.color = color
    }

    fun setPenWidth(width: Float) {
        penWidth = width
    }

    private fun getPaint(): Paint {
        val paint = Paint()
        if (isEraserMode) {
            paint.color = -1
        } else {
            paint.color = color
        }
        paint.strokeWidth = penWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
        return paint
    }

    private fun flipPath(path: Path, canvasWidth: Float): Path {
        val matrix = Matrix()
        matrix.setScale(-1f, 1f, canvasWidth / 2, 0f)
        val mirroredPath = Path(path)
        mirroredPath.transform(matrix)
        return mirroredPath
    }

    fun flip() {
        for (paintPath in pathListHistory) {
            if (paintPath != null) {
                paintPath.path = flipPath(paintPath.path, width.toFloat())
            }
        }
        invalidate()
    }

    fun setEraserMode(isEraserMode: Boolean) {
        this.isEraserMode = isEraserMode
    }
    fun getEraserMode():Boolean {
        return isEraserMode
    }

    fun clearDraw() {
        // Lưu trạng thái hiện tại vào pathListUndo để có thể hoàn tác việc xóa
        pathListUndo.addAll(pathListHistory) // Thêm tất cả đường vẽ hiện tại vào danh sách Undo
        pathListHistory.clear() // Xóa tất cả đường vẽ và vùng tô màu khỏi pathListHistory

        onDrawChange?.onDrawChange() // Gọi callback nếu có thay đổi
        invalidate() // Yêu cầu vẽ lại màn hình để hiển thị trạng thái trống
    }

    fun getHistoryPaint(): MutableList<PaintPath?> {
        return pathListHistory
    }

    fun getHistoryUndo(): MutableList<PaintPath?> {
        return pathListUndo
    }

    fun setHistory(drawInfo: DrawInfo) {
        pathListHistory.clear()
        pathListHistory.addAll(drawInfo.listHistory)
        pathListUndo.clear()
        pathListUndo.addAll(drawInfo.listUndo)
        onDrawChange?.onDrawChange()
        invalidate()
    }

    fun createNewDraw() {
        pathListHistory.clear()
        pathListUndo.clear()
        onDrawChange?.onDrawChange()
        invalidate()
        zoomMatrix.reset()
        savedMatrix.reset()
        zoomMatrixInverse.reset()
        oldDist = 1f
    }

    fun resetPos() {
        zoomMatrix.reset()
        savedMatrix.reset()
        zoomMatrixInverse.reset()
        oldDist = 1f
    }

//    private fun playSoundLooping() {
//        stopSound()
//        if (context == null) {
//            return
//        }
//        mediaPlayer = MediaPlayer.create(context, R.raw.draw_sound)
//        mediaPlayer.isLooping = true
//        mediaPlayer.start()
//        isPlay = true
//    }

//    private fun stopSound() {
//        if (isPlay) {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.stop()
//                mediaPlayer.reset()
//            }
//        }
//        isPlay = false
//    }

}