package io.agora.agorademo

import android.content.Context
import io.agora.agorabase.openlive.AGApplication
import io.agora.agorademo.injection.component.ApplicationComponent
import io.agora.agorademo.injection.component.DaggerApplicationComponent
import io.agora.agorademo.injection.module.ApplicationModule
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

class AgoraApp : AGApplication() {
    private var mApplicationComponent: ApplicationComponent? = null

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        instance = this
    }

    // Needed to replace the component with a test specific one
    var component: ApplicationComponent
        get() {
            if (mApplicationComponent == null) {
                mApplicationComponent = DaggerApplicationComponent.builder()
                        .applicationModule(ApplicationModule(this))
                        .build()
            }
            return mApplicationComponent as ApplicationComponent
        }
        set(applicationComponent) {
            mApplicationComponent = applicationComponent
        }

    companion object {
        lateinit var instance: AgoraApp


        operator fun get(context: Context): AgoraApp {
            return context.applicationContext as AgoraApp
        }
    }
}
