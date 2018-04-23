package io.agora.agorademo.features.broadcast

import android.databinding.ObservableArrayList
import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.BR

import io.agora.agorademo.R
import io.agora.agorademo.data.DataManager
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.data.model.Question
import io.agora.agorademo.databinding.ItemQuestionBinding
import io.agora.agorademo.features.base.BasePresenter
import io.agora.agorademo.injection.ConfigPersistent
import io.agora.agorademo.util.clearAndAddAll
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_question.view.*
import timber.log.Timber
import javax.inject.Inject


@ConfigPersistent
class BroadcastPresenter @Inject
constructor(private val dataManager: DataManager) : BasePresenter<BroadcastMvpView>() {
    lateinit var broadCast: Broadcast
    val lastAdapter: LastAdapter by lazy { initLastAdapter() }
    private val questions = ObservableArrayList<Question>()

    private fun initLastAdapter() = LastAdapter(questions, BR.question)
            .map<Question, ItemQuestionBinding>(R.layout.item_question) {
                onBind {
                    val view = it.itemView
                    val question = it.binding.question!!
                    //Update actions according to isBroadcasting
                    view.answer_bt.setOnClickListener {
                        answerQuestion(question)
                        mvpView?.hideQuestionList()
                    }
                }
            }

    private fun answerQuestion(question: Question) {
        dataManager.answerQuestionListenToChanges(question.copy(answered = true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { ques ->
                            if (ques.answered)
                                mvpView?.showError("Connecting to user")
                            else
                                mvpView?.showError("Call disconnected")
                        },
                        { throwable ->
                            Timber.e(throwable, "onFetchQuestionsError")
                            mvpView?.showError(throwable.message.toString())
                        }
                )
    }

    fun listenToNewQuestions() {
        fun onListenToNewQuestionsUpdate(list: List<Question>) {
            mvpView?.updateQuestionCount(list.size)
            questions.clearAndAddAll(list)
        }


        fun onListenToNewQuestionsError(throwable: Throwable) {
            Timber.e(throwable, "onFetchQuestionsError")
            mvpView?.showError(throwable.message.toString())
        }


        dataManager.listenToNewQuestion(broadCast).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onListenToNewQuestionsUpdate(it) },
                        { onListenToNewQuestionsError(it) })
    }
}