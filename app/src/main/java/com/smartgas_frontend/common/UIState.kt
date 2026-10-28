package com.smartgas_frontend.common

class UIState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val message: String = ""
)
