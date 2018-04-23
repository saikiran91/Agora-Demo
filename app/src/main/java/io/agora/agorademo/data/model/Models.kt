package io.agora.agorademo.data.model

import java.util.*

data class Broadcast(var id: String = "",
                     var brand_id: String = "",
                     val start: Date = Date(),
                     val end: Date = Date(),
                     val live: Boolean = false,
                     var broadcast_channel: String = "",
                     val user_id: String = "",
                     val user_name: String = "",
                     val user_image: String = "",
                     val user_email: String = "",
                     val people: Int = 0)

data class Brand(val id: String = "",
                 var name: String = "",
                 var image: String = "",
                 val order: Int = 0,
                 val description: String = "")