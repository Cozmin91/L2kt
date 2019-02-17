package com.l2kt.gameserver.model.group

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.instance.Player

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * @return the leader of this group.
 */
/**
 * Change the leader of this group to the specified player.
 * @param leader : the player to set as the new leader of this group.
 */
abstract class AbstractGroup(var leader: Player) {
    /**
     * @return the level of this group.
     */
    /**
     * Change the level of this group. **Used only when the group is created.**
     * @param level : the level to set.
     */
    var level: Int = 0

    /**
     * @return a list of all members of this group.
     */
    abstract val members: List<Player>

    /**
     * @return the count of all players in this group.
     */
    abstract val membersCount: Int

    /**
     * @return the leader objectId.
     */
    val leaderObjectId: Int
        get() = leader.objectId

    /**
     * @return a random member of this group.
     */
    val randomPlayer: Player?
        get() = Rnd[members]

    /**
     * Check if this group contains a given player.
     * @param player : the player to check.
     * @return `true` if this group contains the specified player, `false` otherwise.
     */
    abstract fun containsPlayer(player: WorldObject): Boolean

    /**
     * Broadcast a packet to every member of this group.
     * @param packet : the packet to broadcast.
     */
    abstract fun broadcastPacket(packet: L2GameServerPacket)

    /**
     * Broadcast a CreatureSay packet to every member of this group. Similar to broadcastPacket, but with an embbed BlockList check.
     * @param msg : the msg to broadcast.
     * @param broadcaster : the player who broadcasts the message.
     */
    abstract fun broadcastCreatureSay(msg: CreatureSay, broadcaster: Player)

    /**
     * Recalculate the group level.
     */
    abstract fun recalculateLevel()

    /**
     * Destroy that group, resetting all possible values, leading to that group object destruction.
     */
    abstract fun disband()

    /**
     * Check if a given player is the leader of this group.
     * @param player : the player to check.
     * @return `true` if the specified player is the leader of this group, `false` otherwise.
     */
    fun isLeader(player: Player): Boolean {
        return leader.objectId == player.objectId
    }

    /**
     * Broadcast a system message to this group.
     * @param message : the system message to broadcast.
     */
    fun broadcastMessage(message: SystemMessageId) {
        broadcastPacket(SystemMessage.getSystemMessage(message))
    }

    /**
     * Broadcast a custom text message to this group.
     * @param text : the custom string to broadcast.
     */
    fun broadcastString(text: String) {
        broadcastPacket(SystemMessage.sendString(text))
    }
}