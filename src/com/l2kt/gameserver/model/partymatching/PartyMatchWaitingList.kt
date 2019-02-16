package com.l2kt.gameserver.model.partymatching

import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

/**
 * @author Gnacik
 */
object PartyMatchWaitingList {
    private val _members: MutableList<Player>

    val players: List<Player>
        get() = _members

    init {
        _members = ArrayList()
    }

    fun addPlayer(player: Player) {
        if (!_members.contains(player))
            _members.add(player)
    }

    fun removePlayer(player: Player) {
        if (_members.contains(player))
            _members.remove(player)
    }
}