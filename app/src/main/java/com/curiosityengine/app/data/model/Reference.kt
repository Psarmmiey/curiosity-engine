package com.curiosityengine.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Reference(
    val title: String,
    val url: String,
    val description: String? = null
)
