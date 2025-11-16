
package com.meticha.jetpackboilerplate.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val lot: String,
    val lang: String,
    val device: String
)
