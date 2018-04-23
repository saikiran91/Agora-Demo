package io.agora.agorademo.features.broadcast

import io.agora.agorademo.features.base.MvpView


interface BroadcastMvpView : MvpView {
    fun showProgress(show: Boolean)
    fun showError(message: String)
    fun updateQuestionCount()
    fun clearQuestionCount()
    fun hideQuestionList()
    fun askQuestionSuccess(message: String)
}