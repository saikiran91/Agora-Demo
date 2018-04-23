package io.agora.agorademo.data

import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.data.model.Question
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single


interface DataManager {
    fun listOfBrands(): Single<List<Brand>>
    fun loadAndListenToBroadCasts(brand: Brand): Observable<List<Broadcast>>

    fun listenToNewQuestion(broadcast: Broadcast): Observable<List<Question>>
    fun answerQuestionListenToChanges(question: Question): Observable<Question>

    fun askQuestion(question: Question): Completable
    fun createBroadcast(broadcast: Broadcast): Completable
    fun updateBroadcastPeople(broadcastId: String, count: Int)
    fun listenToQuestionUpdate(question: Question): Observable<Question>

}