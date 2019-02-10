package com.l2kt.loginserver.model

import java.util.*

data class AccountInfo(var login: String, private val _passHash: String, val accessLevel: Int, val lastServer: Int) {

    init {
        Objects.requireNonNull(login, "login")
        Objects.requireNonNull(_passHash, "passHash")

        if (login.isEmpty())
            throw IllegalArgumentException("login")

        if (_passHash.isEmpty())
            throw IllegalArgumentException("passHash")

        login = login.toLowerCase()
    }

    fun checkPassHash(passHash: String): Boolean {
        return _passHash == passHash
    }
}