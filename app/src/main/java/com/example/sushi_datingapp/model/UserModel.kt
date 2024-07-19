package com.example.sushi_datingapp.model

data class UserModel(

    val number: String? = "",
    val name: String? = "",
    val city: String? = "",
    val email: String?="",
    val gender: String? = "",
    val relationship:String?="",
    val zodiac: String?="",
    val image: String?="",
    val age: String?="",
    val userStatus :String?="",
    val liked: Boolean? = false,
    val birthdate: String? = ""
)
