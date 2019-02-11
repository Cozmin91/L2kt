package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

class ChangeMoveType(val character: Creature) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x2e)
        writeD(character.objectId)
        writeD(if (character.isRunning) RUN else WALK)
        writeD(0) // c2
    }

    companion object {
        const val WALK = 0
        const val RUN = 1
    }
}