package com.l2kt.gameserver.data.sql

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.model.memo.AbstractMemo

/**
 * A global, server-size, container for variables of any type, which can be then saved/restored upon server restart. It extends [AbstractMemo].
 */
object ServerMemoTable : AbstractMemo() {

    private val LOGGER = CLogger(ServerMemoTable::class.java.name)

    private const val SELECT_QUERY = "SELECT * FROM server_memo"
    private const val DELETE_QUERY = "DELETE FROM server_memo"
    private const val INSERT_QUERY = "INSERT INTO server_memo (var, value) VALUES (?, ?)"

    init {
        restoreMe()
    }

    public override fun restoreMe(): Boolean {
        // Restore previous variables.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SELECT_QUERY).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            set(rs.getString("var"), rs.getString("value"))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore server variables.", e)
            return false
        } finally {
            compareAndSetChanges(true, false)
        }
        LOGGER.info("Loaded {} server variables.", size)
        return true
    }

    public override fun storeMe(): Boolean {
        // No changes, nothing to store.
        if (!hasChanges())
            return false

        try {
            L2DatabaseFactory.connection.use { con ->
                // Clear previous entries.
                var ps = con.prepareStatement(DELETE_QUERY)
                ps.execute()
                ps.close()

                // Insert all variables.
                ps = con.prepareStatement(INSERT_QUERY)
                for ((key, value) in entries) {
                    ps.setString(1, key)
                    ps.setString(2, value.toString())
                    ps.addBatch()
                }
                ps.executeBatch()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't save server variables to database.", e)
            return false
        } finally {
            compareAndSetChanges(true, false)
        }
        LOGGER.info("Stored {} server variables.", size)
        return true
    }
}