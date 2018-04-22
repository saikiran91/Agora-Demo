package io.agora.agorademo.data.model

import com.google.firebase.firestore.DocumentReference
import java.util.*

data class Broadcast(val user: DocumentReference? = null,
                     var broadcast_id: String = "",
                     var name: String = "",
                     val startDate: Date = Date())

data class Brand(val id: String = "",
                 var name: String = "",
                 var image: String = "",
                 val order: Int = 0,
                 val description: String = "")