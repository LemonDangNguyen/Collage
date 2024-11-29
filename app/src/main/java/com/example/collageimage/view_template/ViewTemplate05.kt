package com.example.collageimage.view_template

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.collageimage.R
import com.otaliastudios.zoom.ZoomLayout

@SuppressLint("ResourceType")
class ViewTemplate05 : RelativeLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var w = 0f

    var iv1: ImageView
    lateinit var zl1 : ZoomLayout
    lateinit var zl2 : ZoomLayout
    lateinit var zl3 : ZoomLayout
    lateinit var iv2: ImageView
    lateinit var iv3: ImageView
    lateinit var ivTemp: ImageView

    init {
        w = resources.displayMetrics.widthPixels / 100f

        iv1 = ImageView(context).apply {
            id = 2
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.anhtest)
            setOnClickListener {
                Toast.makeText(context, "ehe", Toast.LENGTH_SHORT).show()
            }
        }
        zl1 = ZoomLayout(context).apply {
            id = 4
            setZoomEnabled(true)
        }

        zl1.addView(iv1, LayoutParams(-1, -1))

        val width = (84f * w).toInt()
        val height = (40f * w).toInt()

        addView(zl1, LayoutParams(width, height).apply {
            topMargin = (14.167f * w).toInt()
            leftMargin = (14.167f * w).toInt()
            rightMargin = (14.167f * w).toInt()
        })

        iv2 = ImageView(context).apply {
            id = 3
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.anhtest)
            setOnClickListener {
                Toast.makeText(context, "ehe", Toast.LENGTH_SHORT).show()
            }
        }

        zl2 = ZoomLayout(context).apply {
            id = 5
            setZoomEnabled(true)
        }
        zl2.addView(iv2, LayoutParams(-1, -1))

        addView(zl2, LayoutParams(width, height).apply {
            addRule(BELOW, zl1.id)
            leftMargin = (14.167f * w).toInt()
            rightMargin = (14.167f * w).toInt()
            topMargin = (3.60f * w).toInt()
        })

        iv3 = ImageView(context).apply {
            id = 6
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.anhtest)
            setOnClickListener {
                Toast.makeText(context, "ehe", Toast.LENGTH_SHORT).show()
            }
        }
        zl3 = ZoomLayout(context).apply {
            id = 7
            setZoomEnabled(true)
        }
        zl3.addView(iv3, LayoutParams(-1, -1))
        addView(zl3, LayoutParams(width, height).apply {
            addRule(BELOW, zl2.id)
            leftMargin = (14.167f * w).toInt()
            rightMargin = (14.167f * w).toInt()
            topMargin = (3.05f * w).toInt()

        })

        ivTemp = ImageView(context).apply {
            id = 1
            scaleType = ImageView.ScaleType.FIT_XY
            setImageResource(R.drawable.template_05)
        }
        addView(ivTemp, LayoutParams(-1, -1))
    }

}