package com.l2kt.gameserver.model.memo

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger

/**
 * An implementation of [AbstractMemo] used for Player. There is a restore/save system.
 */
class PlayerMemo(private val _objectId: Int) : AbstractMemo() {

    init {
        restoreMe()
    }

    public override fun restoreMe(): Boolean {
        // Restore previous variables.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SELECT_QUERY).use { ps ->
                    ps.setInt(1, _objectId)

                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            set(rs.getString("var"), rs.getString("val"))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore variables for player id {}.", e, _objectId)
            return false
        } finally {
            compareAndSetChanges(true, false)
        }
        return true
    }

    public override fun storeMe(): Boolean {
        // No changes, nothing to store.
        if (!hasChanges())
            return false

        try {
            L2DatabaseFactory.connection.use { con ->
                // Clear previous entries.
                con.prepareStatement(DELETE_QUERY).use { ps ->
                    ps.setInt(1, _objectId)
                    ps.execute()
                }

                // Insert all variables.
                con.prepareStatement(INSERT_QUERY).use { ps ->
                    ps.setInt(1, _objectId)
                    for ((key, value) in entries) {
                        ps.setString(2, key)
                        ps.setString(3, value.toString())
                        ps.addBatch()
                    }
                    ps.executeBatch()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update variables for player id {}.", e, _objectId)
            return false
        } finally {
            compareAndSetChanges(true, false)
        }
        return true
    }

    companion object {
        private val LOGGER = CLogger(PlayerMemo::class.java.name)

        private val SELECT_QUERY = "SELECT * FROM character_memo WHERE charId = ?"
        private val DELETE_QUERY = "DELETE FROM character_memo WHERE charId = ?"
        private val INSERT_QUERY = "INSERT INTO character_memo (charId, var, val) VALUES (?, ?, ?)"
    }
}