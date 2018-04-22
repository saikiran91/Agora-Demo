package io.agora.agorademo.injection.component

import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.injection.PerActivity
import io.agora.agorademo.injection.module.ActivityModule
import dagger.Subcomponent
import io.agora.agorademo.features.brands.BrandsActivity
import io.agora.agorademo.features.login.LoginActivity

@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {
    fun inject(baseActivity: MvpBaseActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(brandsActivity: BrandsActivity)
}
