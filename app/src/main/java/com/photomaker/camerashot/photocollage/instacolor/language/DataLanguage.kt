package com.photomaker.camerashot.photocollage.instacolor.language

import android.content.Context
import com.photomaker.camerashot.photocollage.instacolor.NMHApp
import com.photomaker.camerashot.photocollage.instacolor.helpers.CURRENT_LANGUAGE
import com.photomaker.camerashot.photocollage.instacolor.helpers.FINISH_LANGUAGE
import com.photomaker.camerashot.photocollage.instacolor.model.LanguageModel
import com.photomaker.camerashot.photocollage.instacolor.sharepref.DataLocalManager
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.helpers.IS_SHOW_BACK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

object DataLanguage {

    fun getListLanguage(): Flow<MutableList<LanguageModel>> {

        var currentLang = ""
        DataLocalManager.getLanguage(CURRENT_LANGUAGE)?.let { currentLang = it.name }
        if (!DataLocalManager.getBoolean(IS_SHOW_BACK, false)) currentLang = ""

        val lstLang = mutableListOf<LanguageModel>()
        val f = NMHApp.ctx.assets.list("flag_language")
        if (f != null) {
            for (s in f) {
                val name =
                    s.replace(".webp", "").replaceFirst(s.substring(0, 1), s.substring(0, 1).uppercase())
                if (name == currentLang || name.lowercase().contains("english")) continue

                lstLang.add(LanguageModel(name, "flag_language", getTextLang(NMHApp.ctx, name), checkLocale(s), false))
            }
        }

        if (currentLang != "") {
            if (currentLang == "english")
                lstLang.add(1, LanguageModel(currentLang, "flag_language", getTextLang(NMHApp.ctx, currentLang), checkLocale(currentLang), true))
            else lstLang.add(LanguageModel(currentLang, "flag_language", getTextLang(NMHApp.ctx, currentLang), checkLocale(currentLang), true))
        }

        if (lstLang.none { it.locale == Locale.ENGLISH })
            lstLang.add(1, LanguageModel("English", "flag_language", getTextLang(NMHApp.ctx, "english"), checkLocale("english"), false))

        return flow { emit(lstLang) }
    }

    private fun checkLocale(name: String?): Locale {
        return when {
            name?.lowercase()?.contains("french") == true -> Locale.FRANCE
            name?.lowercase()?.contains("hindi") == true -> Locale("hi", "IN")
            name?.lowercase()?.contains("portuguese") == true -> Locale("pt", "PT")
            name?.lowercase()?.contains("spanish") == true -> Locale("es", "ES")
            name?.lowercase()?.contains("arabic") == true -> Locale("ar", "AE")
            name?.lowercase()?.contains("turkish") == true -> Locale("tr", "TR")
            name?.lowercase()?.contains("china_simplified") == true -> Locale.SIMPLIFIED_CHINESE
            name?.lowercase()?.contains("china_traditional") == true -> Locale.TRADITIONAL_CHINESE
            name?.lowercase()?.contains("bengal") == true -> Locale("bn", "IN")
            name?.lowercase()?.contains("german") == true -> Locale.GERMANY
            name?.lowercase()?.contains("japan") == true -> Locale("ja", "JP")
            name?.lowercase()?.contains("south_korea") == true -> Locale("ko", "KR")
            name?.lowercase()?.contains("indonesia") == true -> Locale("id", "ID")
            name?.lowercase()?.contains("brazil") == true -> Locale("pt", "BR")
            name?.lowercase()?.contains("russia") == true -> Locale("ru", "RU")
            name?.lowercase()?.contains("turkey") == true -> Locale("tr", "TR")
            else -> Locale.ENGLISH
        }
    }

    private fun getTextLang(context: Context, name: String): String = when(name.lowercase()) {
        "arabic" -> context.getString(R.string.arabic)
        "bengal" -> context.getString(R.string.bengal)
        "brazil" -> context.getString(R.string.portuguese_brazil)
        "china_simplified" -> context.getString(R.string.chinese_simplified)
        "china_traditional" -> context.getString(R.string.chinese_traditional)
        "english" -> context.getString(R.string.english)
        "french" -> context.getString(R.string.french)
        "german" -> context.getString(R.string.german)
        "hindi" -> context.getString(R.string.hindi)
        "indonesia" -> context.getString(R.string.indonesia)
        "japan" -> context.getString(R.string.japanese)
        "portuguese" -> context.getString(R.string.portuguese)
        "russia" -> context.getString(R.string.russian)
        "south_korea" -> context.getString(R.string.korean)
        "spanish" -> context.getString(R.string.spanish)
        "turkey" -> context.getString(R.string.turkish)
        else -> ""
    }
}
