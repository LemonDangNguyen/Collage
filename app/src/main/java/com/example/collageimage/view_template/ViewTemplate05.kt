package com.example.collageimage.view_template

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.collageimage.R
import com.otaliastudios.zoom.ZoomLayout

@SuppressLint("ResourceType")
class ViewTemplate05: RelativeLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var w = 0f
    private var h = 0f

    var iv1: ImageView
    lateinit var zl1: ZoomLayout
    lateinit var iv2: ImageView
    lateinit var iv3: ImageView
    lateinit var ivTemp: ImageView

    init {
        w = resources.displayMetrics.widthPixels / 100f
        h = resources.displayMetrics.heightPixels / 100f

        ivTemp = ImageView(context).apply {
            id = 1
            scaleType = ImageView.ScaleType.FIT_XY
            setImageResource(R.drawable.template_05)
        }
        addView(ivTemp, LayoutParams(-1, -1))

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
        }

        zl1.addView(iv1, LayoutParams(-1, -1))
//        val width = (39.21f * w).toInt()
  //      val height = (21.64f * w).toInt()
        val width = (69.72f * w).toInt()
        val height = (38.472f * w).toInt()
        addView(zl1, LayoutParams(width, height).apply {
            topMargin = (15.1389f * w).toInt()
            leftMargin = (15.1389f * w).toInt()
            rightMargin = (15.1389f * w).toInt()
        })

        iv2 = ImageView(context).apply {
            id = 3
            setBackgroundColor(Color.RED)
        }
        addView(iv2, LayoutParams(width, height).apply {
            addRule(BELOW, zl1.id)
            leftMargin = (15.1389f * w).toInt()
            rightMargin = (15.1389f * w).toInt()
            topMargin = (4.3056f * w).toInt()
        })

        iv2 = ImageView(context).apply {
            id = 5
            setBackgroundColor(Color.GREEN)
        }

        addView(iv2, LayoutParams(width / 2, height).apply {
            addRule(BELOW, zl1.id)
            addRule(CENTER_HORIZONTAL, TRUE)
            topMargin = (4.3056f * w).toInt()
        })
    }

}