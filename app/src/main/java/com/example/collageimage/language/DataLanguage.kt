package com.example.collageimage.language



import android.content.Context
import com.example.collageimage.NMHApp
import com.example.collageimage.R
import com.nmh.base.project.helpers.CURRENT_LANGUAGE
import com.nmh.base.project.helpers.FINISH_LANGUAGE
import com.nmh.base.project.helpers.FIRST_INSTALL
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.nmh.base.project.model.LanguageModel
import com.nmh.base.project.sharepref.DataLocalManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

object DataLanguage {

    fun getListLanguage(): Flow<MutableList<LanguageModel>> {

        var currentLang = ""
        DataLocalManager.getLanguage(CURRENT_LANGUAGE)?.let { currentLang = it.name }
        if (!DataLocalManager.getBoolean(FINISH_LANGUAGE, false)) currentLang = ""

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
        return if (name?.lowercase()?.contains("french") == true) Locale.FRANCE
        else if (name?.lowercase()?.contains("hindi") == true) Locale("hi", "IN")
        else if (name?.lowercase()?.contains("portuguese") == true) Locale("pt", "PT")
        else if (name?.lowercase()?.contains("spanish") == true) Locale("es", "ES")
        else if (name?.lowercase()?.contains("arabic") == true) Locale("ar", "AE")
        else if (name?.lowercase()?.contains("turkish") == true) Locale("tr", "TR")
        else Locale.ENGLISH
    }

    private fun getTextLang(context: Context, name: String): String = when(name.lowercase()) {
        "english" -> context.getString(R.string.english)
        "spanish" -> context.getString(R.string.spanish)
        "french" -> context.getString(R.string.french)
        "hindi" -> context.getString(R.string.hindi)
        "portuguese" -> context.getString(R.string.portuguese)
        "turkish" -> context.getString(R.string.turkish)
        "arabic" -> context.getString(R.string.arabic)
        else -> ""
    }
}