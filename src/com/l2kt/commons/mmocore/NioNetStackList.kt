package com.l2kt.commons.mmocore

class NioNetStackList<E> {
    private val _start = NioNetStackNode()

    private val _buf = NioNetStackNodeBuf()

    private var _end = NioNetStackNode()

    val isEmpty: Boolean
        get() = _start.next == _end

    init {
        clear()
    }

    fun addLast(elem: E) {
        val newEndNode = _buf.removeFirst()
        _end.value = elem
        _end.next = newEndNode
        _end = newEndNode
    }

    fun removeFirst(): E? {
        val old = _start.next
        val value = old!!.value
        _start.next = old.next
        _buf.addLast(old)
        return value
    }

    fun clear() {
        _start.next = _end
    }

    private inner class NioNetStackNode {
        var next: NioNetStackNode? = null

        var value: E? = null
    }

    private inner class NioNetStackNodeBuf internal constructor() {
        private val _start = NioNetStackNode()

        private var _end = NioNetStackNode()

        init {
            _start.next = _end
        }

        internal fun addLast(node: NioNetStackNode) {
            node.next = null
            node.value = null
            _end.next = node
            _end = node
        }

        internal fun removeFirst(): NioNetStackNode {
            if (_start.next == _end)
                return NioNetStackNode()

            val old = _start.next
            _start.next = old!!.next
            return old
        }
    }
}