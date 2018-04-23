package io.agora.agorademo.features.broadcast

import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast

class LaunchBroadCastEvent(val broadcast: Broadcast, val brand: Brand)
class EndLiveBroadcastEvent(val role: Int)

