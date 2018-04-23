package io.agora.agorademo.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.data.model.Question
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DataManagerImpl @Inject constructor() : DataManager {


    override fun listOfBrands(): Single<List<Brand>> {
        return Single.create<List<Brand>> { emitter ->
            FirebaseFirestore.getInstance().collection("brands")
                    .addSnapshotListener { result, exception ->
                        when {
                            exception != null -> emitter.onError(exception)
                        //TODO use try cache because if result fails to convert to Brand it crashes
                            result != null -> emitter.onSuccess(result.toObjects(Brand::class.java))
                            else -> emitter.onError(Exception("listOfBrands result is null or empty"))
                        }
                    }
        }.subscribeOn(Schedulers.io())
    }


    override fun loadAndListenToBroadCasts(brand: Brand): Observable<List<Broadcast>> {
        return Observable.create<List<Broadcast>> { emitter ->
            FirebaseFirestore.getInstance().collection("broadcasts")
                    .whereEqualTo("brand_id", brand.id)
                    .whereEqualTo("live", true)
                    .addSnapshotListener { result, exception ->
                        when {
                            exception != null -> emitter.onError(exception)
                        //TODO use try cache because if result fails to convert to Broadcast it crashes
                            result != null -> emitter.onNext(result.toObjects(Broadcast::class.java))
                            else -> emitter.onError(Exception("listOfBrands result is null or empty"))
                        }
                    }
        }.subscribeOn(Schedulers.io())
    }

    override fun listenToNewQuestion(schedule: Broadcast): Observable<List<Question>> {
        return Observable.create<List<Question>> { emitter ->
            FirebaseFirestore.getInstance().collection("Questions")
                    .whereEqualTo("schedule_id", schedule.id)
                    .addSnapshotListener { result, exception ->
                        when {
                            exception != null -> emitter.onError(exception)
                            result != null -> emitter.onNext(result.toObjects(Question::class.java))
                            else -> emitter.onError(Exception("listenToNewQuestion result is null or empty"))
                        }

                    }
        }.subscribeOn(Schedulers.io())
    }

    override fun answerQuestionListenToChanges(question: Question): Observable<Question> {
        return Observable.create<Question> { emitter ->

            FirebaseFirestore.getInstance()
                    .collection("Questions")
                    .document(question.id)
                    .set(question, SetOptions.merge())

            FirebaseFirestore.getInstance().collection("Questions")
                    .whereEqualTo("id", question.id)
                    .addSnapshotListener { result, exception ->
                        when {
                            exception != null -> emitter.onError(exception)
                            result != null -> result.toObjects(Question::class.java).let { listOfQuestions ->
                                if (listOfQuestions.isNotEmpty()) {
                                    emitter.onNext(listOfQuestions.first())
                                }
                            }
                            else -> emitter.onError(Exception("answerQuestionListenToChanges result is null or empty"))
                        }
                    }
        }.subscribeOn(Schedulers.io())
    }


    override fun askQuestion(question: Question): Completable {
        return Completable.create { emitter ->
            FirebaseFirestore.getInstance()
                    .collection("Questions")
                    .document(question.id)
                    .set(question)
                    .addOnSuccessListener { emitter.onComplete() }
                    .addOnFailureListener { emitter.onError(it.fillInStackTrace()) }
        }.subscribeOn(Schedulers.io())
    }


    override fun createBroadcast(broadcast: Broadcast): Completable {
        return Completable.create { emitter ->
            FirebaseFirestore.getInstance()
                    .collection("broadcasts")
                    .document(broadcast.id)
                    .set(broadcast)
                    .addOnSuccessListener { emitter.onComplete() }
                    .addOnFailureListener { emitter.onError(it.fillInStackTrace()) }
        }.subscribeOn(Schedulers.io())
    }

}