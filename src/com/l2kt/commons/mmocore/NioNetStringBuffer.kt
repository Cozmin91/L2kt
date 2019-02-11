package com.l2kt.commons.mmocore

import java.nio.BufferOverflowException

class NioNetStringBuffer(private val _size: Int) {
    private val _buf: CharArray = CharArray(_size)

    private var _len: Int = 0

    init {
        _len = 0
    }

    fun clear() {
        _len = 0
    }

    fun append(c: Char) {
        if (_len < _size)
            _buf[_len++] = c
        else
            throw BufferOverflowException()
    }

    override fun toString(): String {
        return String(_buf, 0, _len)
    }
}