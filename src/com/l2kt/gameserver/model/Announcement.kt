package com.l2kt.gameserver.model

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import java.util.concurrent.ScheduledFuture

/**
 * A datatype used to retain informations for announcements. It notably holds a [ScheduledFuture].
 */
class Announcement : Runnable {
    val message: String

    var isCritical: Boolean = false
        private set
    var isAuto: Boolean = false
        private set
    private var _unlimited: Boolean = false

    var initialDelay: Int = 0
        private set
    var delay: Int = 0
        private set
    var limit: Int = 0
        private set
    private var _tempLimit: Int = 0 // Temporary limit, used by current timer.

    private var _task: ScheduledFuture<*>? = null

    constructor(message: String, critical: Boolean) {
        this.message = message
        isCritical = critical
    }

    constructor(message: String, critical: Boolean, auto: Boolean, initialDelay: Int, delay: Int, limit: Int) {
        this.message = message
        isCritical = critical
        isAuto = auto
        this.initialDelay = initialDelay
        this.delay = delay
        this.limit = limit

        if (isAuto) {
            when (this.limit) {
                0 // unlimited
                -> {
                    _task = ThreadPool.scheduleAtFixedRate(
                        this,
                        (this.initialDelay * 1000).toLong(),
                        (this.delay * 1000).toLong()
                    ) // self schedule at fixed rate
                    _unlimited = true
                }

                else -> {
                    _task = ThreadPool.schedule(this, (this.initialDelay * 1000).toLong()) // self schedule (initial)
                    _tempLimit = this.limit
                }
            }
        }
    }

    override fun run() {
        if (!_unlimited) {
            if (_tempLimit == 0)
                return

            _task = ThreadPool.schedule(this, (delay * 1000).toLong()) // self schedule (worker)
            _tempLimit--
        }
        message.announceToOnlinePlayers(isCritical)
    }

    fun stopTask() {
        if (_task != null) {
            _task!!.cancel(true)
            _task = null
        }
    }

    fun reloadTask() {
        stopTask()

        if (isAuto) {
            when (limit) {
                0 // unlimited
                -> {
                    _task = ThreadPool.scheduleAtFixedRate(
                        this,
                        (initialDelay * 1000).toLong(),
                        (delay * 1000).toLong()
                    ) // self schedule at fixed rate
                    _unlimited = true
                }

                else -> {
                    _task = ThreadPool.schedule(this, (initialDelay * 1000).toLong()) // self schedule (initial)
                    _tempLimit = limit
                }
            }
        }
    }
}