package com.dito.app.core.data.shop

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)
