package com.l2kt.commons.concurrent

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger

import java.util.concurrent.*

/**
 * This class handles thread pooling system. It relies on two ThreadPoolExecutor arrays, which poolers number is generated using config.
 *
 *
 * Those arrays hold following pools :
 *
 *
 *  * Scheduled pool keeps a track about incoming, future events.
 *  * Instant pool handles short-life events.
 *
 */
object ThreadPool {
    internal val LOGGER = CLogger(ThreadPool::class.java.name)

    private val MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(java.lang.Long.MAX_VALUE - System.nanoTime()) / 2

    private var threadPoolRandomizer: Int = 0

    private var scheduledPools: MutableList<ScheduledThreadPoolExecutor> = mutableListOf()
    private var instantPools: MutableList<ThreadPoolExecutor> = mutableListOf()

    /**
     * Init the different pools, based on Config. It is launched only once, on Gameserver instance.
     */
    fun init() {
        // Feed scheduled pool.
        var poolCount = Config.SCHEDULED_THREAD_POOL_COUNT
        if (poolCount == -1)
            poolCount = Runtime.getRuntime().availableProcessors()

        for (i in 0 until poolCount)
            scheduledPools.add(ScheduledThreadPoolExecutor(Config.THREADS_PER_SCHEDULED_THREAD_POOL))

        // Feed instant pool.
        poolCount = Config.INSTANT_THREAD_POOL_COUNT
        if (poolCount == -1)
            poolCount = Runtime.getRuntime().availableProcessors()

        for (i in 0 until poolCount)
            instantPools.add(ThreadPoolExecutor(
                Config.THREADS_PER_INSTANT_THREAD_POOL,
                Config.THREADS_PER_INSTANT_THREAD_POOL,
                0,
                TimeUnit.SECONDS,
                ArrayBlockingQueue(100000)
            ))

        // Prestart core threads.
        for (threadPool in scheduledPools)
            threadPool.prestartAllCoreThreads()

        for (threadPool in instantPools)
            threadPool.prestartAllCoreThreads()

        // Launch purge task.
        scheduleAtFixedRate(Runnable {
            for (threadPool in scheduledPools)
                threadPool.purge()

            for (threadPool in instantPools)
                threadPool.purge()
        }, 600000, 600000)

        LOGGER.info("Initializing ThreadPool.")
    }

    /**
     * Schedules a one-shot action that becomes enabled after a delay. The pool is chosen based on pools activity.
     * @param r : the task to execute.
     * @param delay : the time from now to delay execution.
     * @return a ScheduledFuture representing pending completion of the task and whose get() method will return null upon completion.
     */
    fun schedule(r: Runnable, delay: Long): ScheduledFuture<*>? {
        try {
            return getPool(scheduledPools).schedule(TaskWrapper(r), validate(delay), TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            return null
        }

    }

    /**
     * Schedules a periodic action that becomes enabled after a delay. The pool is chosen based on pools activity.
     * @param r : the task to execute.
     * @param delay : the time from now to delay execution.
     * @param period : the period between successive executions.
     * @return a ScheduledFuture representing pending completion of the task and whose get() method will throw an exception upon cancellation.
     */
    fun scheduleAtFixedRate(r: Runnable, delay: Long, period: Long): ScheduledFuture<*>? {
        return try {
            getPool(scheduledPools).scheduleAtFixedRate(
                TaskWrapper(r),
                validate(delay),
                validate(period),
                TimeUnit.MILLISECONDS
            )
        } catch (e: Exception) {
            null
        }

    }

    /**
     * Executes the given task sometime in the future.
     * @param r : the task to execute.
     */
    fun execute(r: Runnable) {
        try {
            getPool(instantPools).execute(TaskWrapper(r))
        } catch (e: Exception) {
        }

    }

    /**
     * Retrieve stats of current running thread pools.
     */
    fun getStats() {
        for (i in scheduledPools.indices) {
            val threadPool = scheduledPools[i]

            LOGGER.info("=================================================")
            LOGGER.info("Scheduled pool #$i:")
            LOGGER.info("\tgetActiveCount: ...... " + threadPool.activeCount)
            LOGGER.info("\tgetCorePoolSize: ..... " + threadPool.corePoolSize)
            LOGGER.info("\tgetPoolSize: ......... " + threadPool.poolSize)
            LOGGER.info("\tgetLargestPoolSize: .. " + threadPool.largestPoolSize)
            LOGGER.info("\tgetMaximumPoolSize: .. " + threadPool.maximumPoolSize)
            LOGGER.info("\tgetCompletedTaskCount: " + threadPool.completedTaskCount)
            LOGGER.info("\tgetQueuedTaskCount: .. " + threadPool.queue.size)
            LOGGER.info("\tgetTaskCount: ........ " + threadPool.taskCount)
        }

        for (i in instantPools.indices) {
            val threadPool = instantPools[i]

            LOGGER.info("=================================================")
            LOGGER.info("Instant pool #$i:")
            LOGGER.info("\tgetActiveCount: ...... " + threadPool.activeCount)
            LOGGER.info("\tgetCorePoolSize: ..... " + threadPool.corePoolSize)
            LOGGER.info("\tgetPoolSize: ......... " + threadPool.poolSize)
            LOGGER.info("\tgetLargestPoolSize: .. " + threadPool.largestPoolSize)
            LOGGER.info("\tgetMaximumPoolSize: .. " + threadPool.maximumPoolSize)
            LOGGER.info("\tgetCompletedTaskCount: " + threadPool.completedTaskCount)
            LOGGER.info("\tgetQueuedTaskCount: .. " + threadPool.queue.size)
            LOGGER.info("\tgetTaskCount: ........ " + threadPool.taskCount)
        }
    }

    /**
     * Shutdown thread pooling system correctly. Send different informations.
     */
    fun shutdown() {
        try {
            println("ThreadPool: Shutting down.")

            for (threadPool in scheduledPools)
                threadPool.shutdownNow()

            for (threadPool in instantPools)
                threadPool.shutdownNow()
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    /**
     * @param <T> : The pool type.
     * @param threadPools : The pool array to check.
     * @return the less fed pool.
    </T> */
    private fun <T> getPool(threadPools: MutableList<T>): T {
        return threadPools[threadPoolRandomizer++ % threadPools.size]
    }

    /**
     * @param delay : The delay to validate.
     * @return a secured value, from 0 to MAX_DELAY.
     */
    private fun validate(delay: Long): Long {
        return Math.max(0, Math.min(MAX_DELAY, delay))
    }

    class TaskWrapper(private val _runnable: Runnable) : Runnable {

        override fun run() {
            try {
                _runnable.run()
            } catch (e: RuntimeException) {
                LOGGER.error("Exception in a ThreadPool task execution.", e)
            }

        }
    }
}