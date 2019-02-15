package com.l2kt.gameserver.data.manager

import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Duel
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads and stores [Duel]s for easier management.
 */
object DuelManager {
    private val _duels = ConcurrentHashMap<Int, Duel>()

    fun getDuel(duelId: Int): Duel? {
        return _duels[duelId]
    }

    /**
     * Add a Duel on the _duels Map. Both players must exist.
     * @param playerA : The first player to use.
     * @param playerB : The second player to use.
     * @param isPartyDuel : True if the duel is a party duel.
     */
    fun addDuel(playerA: Player?, playerB: Player?, isPartyDuel: Boolean) {
        if (playerA == null || playerB == null)
            return

        // Compute a new id.
        val duelId = IdFactory.getInstance().nextId

        // Feed the Map.
        _duels[duelId] = Duel(playerA, playerB, isPartyDuel, duelId)
    }

    /**
     * Remove the duel from the Map, and release the id.
     * @param duelId : The id to remove.
     */
    fun removeDuel(duelId: Int) {
        // Release the id.
        IdFactory.getInstance().releaseId(duelId)

        // Delete from the Map.
        _duels.remove(duelId)
    }

    /**
     * Ends the duel by a surrender action.
     * @param player : The player used to retrieve the duelId. The player is then used as surrendered opponent.
     */
    fun doSurrender(player: Player?) {
        if (player == null || !player.isInDuel)
            return

        val duel = getDuel(player.duelId)
        duel?.doSurrender(player)
    }

    /**
     * Ends the duel by a defeat action.
     * @param player : The player used to retrieve the duelId. The player is then used as defeated opponent.
     */
    fun onPlayerDefeat(player: Player?) {
        if (player == null || !player.isInDuel)
            return

        val duel = getDuel(player.duelId)
        duel?.onPlayerDefeat(player)
    }

    /**
     * Registers a buff which will be removed if the duel ends.
     * @param player : The player to buff.
     * @param buff : The effect to cast.
     */
    fun onBuff(player: Player?, buff: L2Effect?) {
        if (player == null || !player.isInDuel || buff == null)
            return

        val duel = getDuel(player.duelId)
        duel?.onBuff(player, buff)
    }

    /**
     * Removes player from duel, enforcing duel cancellation.
     * @param player : The player to check.
     */
    fun onPartyEdit(player: Player?) {
        if (player == null || !player.isInDuel)
            return

        val duel = getDuel(player.duelId)
        duel?.onPartyEdit()
    }

    /**
     * Broadcasts a packet to the team (or the player) opposing the given player.
     * @param player : The player used to find the opponent.
     * @param packet : The packet to send.
     */
    fun broadcastToOppositeTeam(player: Player?, packet: L2GameServerPacket) {
        if (player == null || !player.isInDuel)
            return

        val duel = getDuel(player.duelId) ?: return

        if (duel.playerA == player)
            duel.broadcastToTeam2(packet)
        else if (duel.playerB == player)
            duel.broadcastToTeam1(packet)
        else if (duel.isPartyDuel) {
            if (duel.playerA.party != null && duel.playerA.party!!.containsPlayer(player))
                duel.broadcastToTeam2(packet)
            else if (duel.playerB.party != null && duel.playerB.party!!.containsPlayer(player))
                duel.broadcastToTeam1(packet)
        }
    }
}