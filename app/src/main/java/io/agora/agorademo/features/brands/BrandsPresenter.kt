package io.agora.agorademo.features.brands

import io.agora.agorademo.data.DataManager
import io.agora.agorademo.features.base.BasePresenter
import io.agora.agorademo.injection.ConfigPersistent
import javax.inject.Inject

/**
 * Created by saiki on 22-04-2018.
 **/
@ConfigPersistent
class BrandsPresenter @Inject
constructor(private val mDataManager: DataManager) : BasePresenter<BrandsMvpView>() {

}