package io.agora.agorademo.data

import io.agora.agorademo.data.model.Brand
import io.reactivex.Observable
import io.reactivex.Single


interface DataManager {
    fun listOfBrands(): Single<List<Brand>>
}