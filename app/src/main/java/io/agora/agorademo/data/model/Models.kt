package io.agora.agorademo.data.model

data class Broadcast(var id: String = "",
                     var brand_id: String = "",
                     val start: Long? = null,
                     val end: Long? = null,
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

data class Question(val id: String = "",
                    val answered: Boolean = false,
                    val brand_id: String = "",
                    val question: String = "",
                    val user_id: String = "",
                    val user_name: String = "",
                    val broadcast_id: String = "",
                    val isRead: Boolean = false)
