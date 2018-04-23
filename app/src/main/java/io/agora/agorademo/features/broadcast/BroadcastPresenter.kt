package io.agora.agorademo.features.broadcast

import android.databinding.ObservableArrayList
import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.BR
import io.agora.agorademo.R
import io.agora.agorademo.data.DataManager
import io.agora.agorademo.data.local.UserPrefs
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.data.model.Question
import io.agora.agorademo.databinding.ItemQuestionBinding
import io.agora.agorademo.features.base.BasePresenter
import io.agora.agorademo.injection.ConfigPersistent
import io.agora.agorademo.util.clearAndAddAll
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_question.view.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@ConfigPersistent
class BroadcastPresenter @Inject
constructor(private val dataManager: DataManager) : BasePresenter<BroadcastMvpView>() {
    lateinit var broadCast: Broadcast
    val lastAdapter: LastAdapter by lazy { initLastAdapter() }
    private val listOfQuestions = ObservableArrayList<Question>()

    private fun initLastAdapter() = LastAdapter(listOfQuestions, BR.question)
            .map<Question, ItemQuestionBinding>(R.layout.item_question) {
                onBind {
                    val view = it.itemView
                    val question = it.binding.question!!
                    //Update actions according to isBroadcasting
                    view.answer_bt.setOnClickListener {
                        answerQuestion(question)
                        mvpView?.hideQuestionList()
                        mvpView?.clearQuestionCount()
                    }
                }
            }

    private fun answerQuestion(question: Question) {
        dataManager.answerQuestionListenToChanges(question.copy(answered = true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { ques ->
                            if (ques.answered)
                                mvpView?.showError("Answering to user")
                            else
                                mvpView?.showError("Cant answer now")
                        },
                        { throwable ->
                            Timber.e(throwable, "onFetchQuestionsError")
                            mvpView?.showError(throwable.message.toString())
                        }
                )
    }

    fun listenToNewQuestions() {
        fun onListenToNewQuestionsUpdate(list: List<Question>) {
            if (listOfQuestions.size != list.size) mvpView?.updateQuestionCount()
            listOfQuestions.clearAndAddAll(list)
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

    fun askQuestion(message: String) {
        if (message.isNotEmpty()) {
            mvpView?.showProgress(true)
            val question = Question(id = UUID.randomUUID().toString(),
                    question = message,
                    user_id = UserPrefs.id,
                    user_name = UserPrefs.name,
                    brand_id = broadCast.brand_id,
                    answered = false,
                    broadcast_id = broadCast.id)

            dataManager.askQuestion(question)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                mvpView?.showProgress(false)
                                mvpView?.askQuestionSuccess("Your question sent")
                                listenToQuestionUpdate(question)
                            },
                            { throwable ->
                                mvpView?.showProgress(false)
                                Timber.e(throwable, "onAskQuestionError")
                                mvpView?.showError(throwable.message.toString())
                            })
        } else
            mvpView?.showError("Question cant be empty")
    }

    private fun listenToQuestionUpdate(question: Question?) {
        question?.let {
            dataManager.listenToQuestionUpdate(question = question)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ question ->
                        if (question.answered) {
                            //Connect to astrologer
                            Timber.d("Question is answered")
                            mvpView?.askQuestionSuccess("Your question is being answered")
                        }
                    }, { t ->
                        Timber.e(t, "listenToQuestionUpdate Error")
                    })

            return
        }

        Timber.e("listenToQuestionUpdate Failed")
    }
}