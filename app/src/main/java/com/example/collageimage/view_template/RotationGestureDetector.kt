package com.example.collageimage.view_template

import android.view.MotionEvent

class RotationGestureDetector(private val listener: OnRotationGestureListener) {

    interface OnRotationGestureListener {
        fun onRotation(rotationDetector: RotationGestureDetector)
    }

    private var fX: Float = 0.0f
    private var fY: Float = 0.0f
    private var sX: Float = 0.0f
    private var sY: Float = 0.0f
    private var angle: Float = 0.0f

    val rotationDegrees: Float
        get() = angle

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    fX = event.getX(0)
                    fY = event.getY(0)
                    sX = event.getX(1)
                    sY = event.getY(1)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) {
                    val nfX = event.getX(0)
                    val nfY = event.getY(0)
                    val nsX = event.getX(1)
                    val nsY = event.getY(1)

                    val angle1 = Math.atan2((fY - sY).toDouble(), (fX - sX).toDouble()) * 180 / Math.PI
                    val angle2 = Math.atan2((nfY - nsY).toDouble(), (nfX - nsX).toDouble()) * 180 / Math.PI

                    angle = (angle2 - angle1).toFloat()

                    listener.onRotation(this)

                    fX = nfX
                    fY = nfY
                    sX = nsX
                    sY = nsY
                }
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                angle = 0.0f
            }
        }
        return true
    }
}
