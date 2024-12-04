package com.example.collageimage.view_template

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.collageimage.R

class TemplateViewModel : ViewModel() {
    private val _templates = MutableLiveData<List<TemplateModel>>()
    val templates: LiveData<List<TemplateModel>> get() = _templates

    private val _selectedBitmap = MutableLiveData<Bitmap?>()
    val selectedBitmap: LiveData<Bitmap?> get() = _selectedBitmap

    fun loadTemplates() {

        val templatesList = listOf(
            TemplateModel(
                id = 1,
                backgroundImageResId = R.drawable.template_01,
                stringPaths = listOf(
                    "M109 108.5h502v277H109v-277Z",
                    "M109 420h502v277H109V420Z",
                    "M109 727h502v277H109V727Z"
                )
            ),

            TemplateModel(
                id = 3,
                backgroundImageResId = R.drawable.template_03,
                stringPaths = listOf(
                    "M525.22 221.12l-198.1-27.84-29.82 198.6 145.57 21.01-21.36-105.92 94.44-18.82 9.27-67.03Z",
                    "M624.05 278.73L431.18 315.9l37.98 197.14 192.88-37.17-37.99-197.14Z",
                    "M438 843l7.5 33.5c3.33-2.67 14.5-10.5 32.5-20.5 22.5-12.5 80.96-55.3 108.5-111 29.18-59.01 37.8-107.5 23.5-172.5-7.04-32.01-23.17-63.67-31.5-74.5l-117 22.5L443 418l-153.5-19.5 4.5-31c-15.33 4.5-50.68 15.44-79 33.5-46.62 29.74-72.07 55.14-97 104.5-26.25 51.96-32.92 89.64-26.5 147.5 4.82 43.48 24.97 86.6 34.47 106.95L126 760l211 29.5-8.5 72.5L438 843Z",
                    "M431.88 848L239 885.16l37.99 197.14 192.87-37.16L431.87 848Z",
                    "M332 794.5L134 766l-30.5 198.5L249 986l-21-106 94.5-18.5 9.5-67Z"
                )
            ),

            TemplateModel(
                id = 5,
                backgroundImageResId = R.drawable.template_05,
                stringPaths = listOf(
                    "M109 108.5h502v277H109v-277Z",
                    "M109 420h502v277H109V420Z",
                    "M109 727h502v277H109V727Z"
                )
            ),

            TemplateModel(
                id = 13,
                backgroundImageResId = R.drawable.template_13,
                stringPaths = listOf(
                    "M73 505H273V832H73z",
                    "M336 272H643V613H336z"
                )
            ),

            TemplateModel(
                id = 25,
                backgroundImageResId = R.drawable.template_25,
                stringPaths = listOf(
                    "M302.88 113.31L65.91 143.12l29.92 237.5L332.8 350.8l-29.92-237.5Z",
                    "M652.34 174H408v244.15h244.34V174Z",
                    "M339.69 489.73L105.73 448 64 681.96l233.96 41.73 41.73-233.96Z",
                    "M635.7 548H382v253.7h253.7V548Z",
                    "M301.63 819L56 849.82l30.82 245.63 245.63-30.82L301.63 819Z",
                    "M666.73 892.86L414.87 877 399 1129.1l251.85 15.86 15.88-252.1Z"
                )
            ),

        )
        _templates.value = templatesList
    }

    fun setSelectedImage(bitmap: Bitmap) {
        _selectedBitmap.value = bitmap
    }
}
