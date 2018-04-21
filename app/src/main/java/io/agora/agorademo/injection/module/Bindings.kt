package io.agora.agorademo.injection.module

import io.agora.agorademo.data.DataManager
import io.agora.agorademo.data.DataManagerImpl
import dagger.Binds
import dagger.Module

@Module
abstract class Bindings {

  @Binds
  internal abstract fun bindDataManger(manager: DataManagerImpl): DataManager

}