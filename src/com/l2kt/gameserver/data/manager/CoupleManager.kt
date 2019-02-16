package com.l2kt.gameserver.data.manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.IntIntHolder
import java.util.concurrent.ConcurrentHashMap

/**
 * Custom wedding system implementation.<br></br>
 * <br></br>
 * Loads and stores couples using [IntIntHolder], id being requesterId and value being partnerId.
 */
object CoupleManager {

    private val _couples = ConcurrentHashMap<Int, IntIntHolder>()
    private val LOGGER = CLogger(CoupleManager::class.java.name)

    private const val LOAD_COUPLES = "SELECT * FROM mods_wedding"
    private const val DELETE_COUPLES = "DELETE FROM mods_wedding"
    private const val ADD_COUPLE = "INSERT INTO mods_wedding (id, requesterId, partnerId) VALUES (?,?,?)"

    val couples: Map<Int, IntIntHolder>
        get() = _couples

    init {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_COUPLES).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            _couples[rs.getInt("id")] = IntIntHolder(rs.getInt("requesterId"), rs.getInt("partnerId"))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load couples.", e)
        }

        LOGGER.info("Loaded {} couples.", _couples.size)
    }

    fun getCouple(coupleId: Int): IntIntHolder? {
        return _couples[coupleId]
    }

    /**
     * Add a couple to the couples map. Both players must be logged.
     * @param requester : The wedding requester.
     * @param partner : The wedding partner.
     */
    fun addCouple(requester: Player?, partner: Player?) {
        if (requester == null || partner == null)
            return

        val coupleId = IdFactory.getInstance().nextId

        _couples[coupleId] = IntIntHolder(requester.objectId, partner.objectId)

        requester.coupleId = coupleId
        partner.coupleId = coupleId
    }

    /**
     * Delete the couple. If players are logged, reset wedding variables.
     * @param coupleId : The couple id to delete.
     */
    fun deleteCouple(coupleId: Int) {
        val couple = _couples.remove(coupleId) ?: return

        // Inform and reset the couple id of requester.
        val requester = World.getPlayer(couple.id)
        if (requester != null) {
            requester.coupleId = 0
            requester.sendMessage("You are now divorced.")
        }

        // Inform and reset the couple id of partner.
        val partner = World.getPlayer(couple.value)
        if (partner != null) {
            partner.coupleId = 0
            partner.sendMessage("You are now divorced.")
        }

        // Release the couple id.
        IdFactory.getInstance().releaseId(coupleId)
    }

    /**
     * Save all couples on shutdown. Delete previous SQL infos.
     */
    fun save() {
        try {
            L2DatabaseFactory.connection.use { con ->
                var ps = con.prepareStatement(DELETE_COUPLES)
                ps.execute()
                ps.close()

                ps = con.prepareStatement(ADD_COUPLE)
                for ((key, couple) in _couples) {

                    ps.setInt(1, key)
                    ps.setInt(2, couple.id)
                    ps.setInt(3, couple.value)
                    ps.addBatch()
                }
                ps.executeBatch()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't add a couple.", e)
        }

    }

    /**
     * @param coupleId : The couple id to check.
     * @param objectId : The player objectId to check.
     * @return the partner objectId, or 0 if not found.
     */
    fun getPartnerId(coupleId: Int, objectId: Int): Int {
        val couple = _couples[coupleId] ?: return 0

        return if (couple.id == objectId) couple.value else couple.id
    }
}