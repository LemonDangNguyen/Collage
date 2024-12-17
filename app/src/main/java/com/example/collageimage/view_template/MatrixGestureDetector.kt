package com.example.collageimage.view_template

import android.graphics.Matrix
import android.view.MotionEvent

class MatrixGestureDetector(matrix: Matrix, listener: OnMatrixChangeListener) {
    interface OnMatrixChangeListener {
        fun onChange(matrix: Matrix?)
    }

    private var ptpIdx = 0
    private val mTempMatrix = Matrix()
    private val mMatrix: Matrix = matrix
    private val mListener: OnMatrixChangeListener = listener
    private val mSrc = FloatArray(4)
    private val mDst = FloatArray(4)
    private var mCount = 0
    private var previousDistance = 0f
    private var scaleFactor = 1f
    fun onTouchEvent(event: MotionEvent) {
        if (event.pointerCount > 2) {
            return
        }
        val action = event.actionMasked
        val index = event.actionIndex
        var idx = index * 2
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                mSrc[idx] = event.getX(index)
                mSrc[idx + 1] = event.getY(index)
                mCount++
                ptpIdx = 0
            }
            MotionEvent.ACTION_MOVE -> {
                var i = 0
                while (i < mCount) {
                    idx = ptpIdx + i * 2
                    mDst[idx] = event.getX(i)
                    mDst[idx + 1] = event.getY(i)
                    i++
                }
                mTempMatrix.setPolyToPoly(mSrc, ptpIdx, mDst, ptpIdx, mCount)
                mMatrix.postConcat(mTempMatrix)
                mListener.onChange(mMatrix)
                System.arraycopy(mDst, 0, mSrc, 0, mDst.size)
                if (event.pointerCount == 2) {
                    val distance = calculateDistance(event)
                    if (previousDistance > 0) {
                        val scale = distance / previousDistance
                        scaleImage(scale)
                    }
                    previousDistance = distance
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(index) == 0) ptpIdx = 2
                mCount--
                previousDistance = 0f
            }
        }
    }
    private fun calculateDistance(event: MotionEvent): Float {
        val x1 = event.getX(0)
        val y1 = event.getY(0)
        val x2 = event.getX(1)
        val y2 = event.getY(1)
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat()
    }
    private fun scaleImage(scale: Float) {
        scaleFactor *= scale
        val values = FloatArray(9)
        mMatrix.getValues(values)
        mMatrix.postScale(scale, scale, values[Matrix.MSCALE_X] / 2f, values[Matrix.MSCALE_Y] / 2f)
        mListener.onChange(mMatrix)
    }
}