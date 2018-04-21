package io.agora.agorademo.injection.component

import io.agora.agorademo.injection.ConfigPersistent
import io.agora.agorademo.injection.module.ActivityModule
import io.agora.agorademo.injection.module.FragmentModule
import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.features.base.BaseFragment
import dagger.Component

/**
 * A dagger component that will live during the lifecycle of an Activity or Fragment but it won't
 * be destroy during configuration changes. Check [MvpBaseActivity] and [BaseFragment] to
 * see how this components survives configuration changes.
 * Use the [ConfigPersistent] scope to annotate dependencies that need to survive
 * configuration changes (for example Presenters).
 */
@ConfigPersistent
@Component(dependencies = arrayOf(ApplicationComponent::class))
interface ConfigPersistentComponent {

    fun activityComponent(activityModule: ActivityModule): ActivityComponent

    fun fragmentComponent(fragmentModule: FragmentModule): FragmentComponent

}
