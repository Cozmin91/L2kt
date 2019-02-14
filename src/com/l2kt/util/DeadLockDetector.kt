package com.l2kt.util

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.Shutdown
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/**
 * Thread to check for deadlocked threads.
 */
class DeadLockDetector : Thread("DeadLockDetector") {

    private val tmx: ThreadMXBean

    init {
        tmx = ManagementFactory.getThreadMXBean()
    }

    override fun run() {
        var deadlock = false
        while (!deadlock) {
            try {
                val ids = tmx.findDeadlockedThreads()

                if (ids != null) {
                    deadlock = true
                    val tis = tmx.getThreadInfo(ids, true, true)
                    val info = StringBuilder()
                    info.append("DeadLock Found!\n")

                    for (ti in tis)
                        info.append(ti.toString())

                    for (ti in tis) {
                        val locks = ti.lockedSynchronizers
                        val monitors = ti.lockedMonitors

                        if (locks.size == 0 && monitors.size == 0)
                            continue

                        var dl = ti
                        info.append("Java-level deadlock:\n")
                        info.append("\t")
                        info.append(dl.threadName)
                        info.append(" is waiting to lock ")
                        info.append(dl.lockInfo.toString())
                        info.append(" which is held by ")
                        info.append(dl.lockOwnerName)
                        info.append("\n")

                        while (true){
                            dl = tmx.getThreadInfo(longArrayOf(dl.lockOwnerId), true, true)[0]
                            if(dl.threadId != ti.threadId){
                                info.append("\t")
                                info.append(dl.threadName)
                                info.append(" is waiting to lock ")
                                info.append(dl.lockInfo.toString())
                                info.append(" which is held by ")
                                info.append(dl.lockOwnerName)
                                info.append("\n")
                                continue
                            }
                            break
                        }
                    }
                    LOGGER.warn(info.toString())

                    if (Config.RESTART_ON_DEADLOCK) {
                        "Server has stability issues - restarting now.".announceToOnlinePlayers()
                        Shutdown.instance.startShutdown(null, "DeadLockDetector - Auto Restart", 60, true)
                    }
                }
                Thread.sleep(SLEEP_TIME.toLong())
            } catch (e: Exception) {
                LOGGER.warn("The DeadLockDetector encountered a problem.", e)
            }

        }
    }

    companion object {
        private val LOGGER = CLogger(DeadLockDetector::class.java.name)

        /** Interval to check for deadlocked threads  */
        private val SLEEP_TIME = Config.DEADLOCK_CHECK_INTERVAL * 1000
    }
}