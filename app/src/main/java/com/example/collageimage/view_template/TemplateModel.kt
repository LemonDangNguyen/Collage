package com.example.collageimage.view_template

data class TemplateModel(
    val id: Int, // ID của template
    val backgroundImageResId: Int, // ID ảnh nền
    val stringPaths: List<String> // Danh sách các path dưới dạng chuỗi
)




/*
val template = TemplateModel(
    id = 1,
    backgroundImageResId = R.drawable.template_01, // Ảnh nền
    stringPaths = listOf(
        "M109 108.5h502v277H109v-277Z",  // Path 1
        "M109 420h502v277H109V420Z",    // Path 2
        "M109 727h502v277H109V727Z"     // Path 3
    )
)
*/