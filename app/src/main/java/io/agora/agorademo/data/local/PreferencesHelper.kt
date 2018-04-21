package io.agora.agorademo.data.local

import com.chibatching.kotpref.KotprefModel


object UserPrefs : KotprefModel() {
    var name by stringPref()
    var id by stringPref()
}

