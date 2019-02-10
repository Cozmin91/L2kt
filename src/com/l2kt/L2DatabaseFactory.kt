package com.l2kt

import com.mchange.v2.c3p0.ComboPooledDataSource

import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger

object L2DatabaseFactory {

    private var source: ComboPooledDataSource
    private var log = Logger.getLogger(L2DatabaseFactory::class.java.name)

    val connection: Connection
        get() {
            return source.connection
        }

    init {
        try {
            source = ComboPooledDataSource()
            source.isAutoCommitOnClose = true

            source.initialPoolSize = 10
            source.minPoolSize = 10
            source.maxPoolSize = Math.max(10, Config.DATABASE_MAX_CONNECTIONS)

            source.acquireRetryAttempts = 0 // try to obtain connections indefinitely (0 = never quit)
            source.acquireRetryDelay = 500 // 500 miliseconds wait before try to acquire connection again
            source.checkoutTimeout = 0 // 0 = wait indefinitely for new connection
            source.acquireIncrement = 5 // if pool is exhausted, get 5 more connections at a time
            // cause there is a "long" delay on acquire connection
            // so taking more than one connection at once will make connection pooling
            // more effective.

            source.automaticTestTable = "connection_test_table"
            source.isTestConnectionOnCheckin = false

            source.idleConnectionTestPeriod = 3600 // test idle connection every 60 sec
            source.maxIdleTime = 0 // idle connections never expire

            // enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
            source.maxStatementsPerConnection = 100

            source.isBreakAfterAcquireFailure = false
            source.driverClass = "com.mysql.jdbc.Driver"
            source.jdbcUrl = Config.DATABASE_URL
            source.user = Config.DATABASE_LOGIN
            source.password = Config.DATABASE_PASSWORD

            source.connection.close()
        } catch (x: SQLException) {
            throw x
        } catch (e: Exception) {
            throw SQLException("could not init DB connection:$e")
        }
    }

    fun shutdown() {
        try {
            source.close()
        } catch (e: Exception) {
            log.log(Level.INFO, "", e)
        }
    }
}