package com.l2kt.commons.mmocore

interface IClientFactory<T : MMOClient<*>> {
    fun create(con: MMOConnection<T>): T
}