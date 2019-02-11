package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Npc

class MonRaceInfo(
    private val _unknown1: Int,
    private val _unknown2: Int,
    private val _monsters: List<Npc>,
    private val _speeds: Array<IntArray>
): L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xdd)

        writeD(_unknown1)
        writeD(_unknown2)
        writeD(8)

        for (i in 0..7) {
            val npc = _monsters[i]

            writeD(npc.objectId)
            writeD(npc.template.npcId + 1000000)
            writeD(14107) // origin X
            writeD(181875 + 58 * (7 - i)) // origin Y
            writeD(-3566) // origin Z
            writeD(12080) // end X
            writeD(181875 + 58 * (7 - i)) // end Y
            writeD(-3566) // end Z
            writeF(npc.collisionHeight)
            writeF(npc.collisionRadius)
            writeD(120) // ?? unknown

            for (j in 0..19) {
                if (_unknown1 == 0)
                    writeC(_speeds[i][j])
                else
                    writeC(0)
            }
            writeD(0)
        }
    }
}