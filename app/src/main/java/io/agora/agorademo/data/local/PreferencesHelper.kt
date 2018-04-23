package io.agora.agorademo.data.local

import com.chibatching.kotpref.KotprefModel


object UserPrefs : KotprefModel() {
    var id by stringPref()
    var name by stringPref()
    var email by stringPref()
    var photoUrl by stringPref()
    var broadcastChannel by stringPref()
    var broadcastId by stringPref()
}

