package io.agora.agorademo.injection.component

import io.agora.agorademo.injection.PerFragment
import io.agora.agorademo.injection.module.FragmentModule
import dagger.Subcomponent

/**
 * This component inject dependencies to all Fragments across the application
 */
@PerFragment
@Subcomponent(modules = arrayOf(FragmentModule::class))
interface FragmentComponent