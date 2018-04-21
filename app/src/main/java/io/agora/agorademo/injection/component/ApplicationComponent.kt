package io.agora.agorademo.injection.component

import android.app.Application
import android.content.Context
import io.agora.agorademo.data.DataManager
import io.agora.agorademo.injection.ApplicationContext
import io.agora.agorademo.injection.module.ApplicationModule
import io.agora.agorademo.injection.module.Bindings
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(ApplicationModule::class), (Bindings::class)])
interface ApplicationComponent {

    @ApplicationContext
    fun context(): Context

    fun application(): Application

    fun dataManager(): DataManager

}
