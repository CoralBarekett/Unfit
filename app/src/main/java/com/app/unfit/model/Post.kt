package com.app.unfit.model

data class Post(
    val id: String = "",
    val userName: String = "",
    val imageUrl: String = "",
    val description: String = "",
    var isLiked: Boolean = false,
    var isSaved: Boolean = false
)
