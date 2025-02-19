package com.example.collageimage.language



import com.example.collageimage.NMHApp
import com.nmh.base.project.helpers.CURRENT_LANGUAGE
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.nmh.base.project.model.LanguageModel
import com.example.collageimage.sharepref.DataLocalManager
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
        for (s in f!!) {
            val name =
                s.replace(".webp", "").replaceFirst(s.substring(0, 1), s.substring(0, 1).uppercase())
            if (name != currentLang)
                lstLang.add(LanguageModel(name, "flag_language", checkLocale(s),false))
        }

        if (currentLang != "")
            lstLang.add(LanguageModel(currentLang, "flag_language", checkLocale(currentLang),true))

        return flow { emit(lstLang) }
    }

    private fun checkLocale(name: String): Locale {
        return if (name.lowercase().contains("french")) Locale.FRANCE
        else if (name.lowercase().contains("hindi")) Locale("hi", "IN")
        else if (name.lowercase().contains("spanish")) Locale("es", "ES")
        else if (name.lowercase().contains("portuguese")) Locale("pt", "PT")
        else Locale.ENGLISH
    }
}