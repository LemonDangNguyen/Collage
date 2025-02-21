package com.nmh.base.project.model

import java.util.Locale

data class LanguageModel(
    var name: String = "",
    var uri: String = "",
    var nativeName: String = "",
    var locale: Locale = Locale.ENGLISH,
    var isCheck: Boolean
)