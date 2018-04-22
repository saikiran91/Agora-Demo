package io.agora.agorademo.data

import com.google.firebase.firestore.FirebaseFirestore
import io.agora.agorademo.data.model.Brand
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
}