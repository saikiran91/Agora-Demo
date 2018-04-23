package io.agora.agorademo.features.broadcast

import io.agora.agorademo.features.base.MvpView


interface BroadcastMvpView : MvpView {
    fun showProgress(show: Boolean)
    fun showError(message: String)
    fun updateQuestionCount(size: Int)
    fun hideQuestionList()
    fun askQuestionSuccess(message: String)
}