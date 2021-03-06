package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.Timestamp

class SkillCoolTime(cha: Player) : L2GameServerPacket() {
    private var _reuseTimeStamps: List<Timestamp> = cha.reuseTimeStamps.filter { r -> r.hasNotPassed() }

    override fun writeImpl() {
        writeC(0xc1)
        writeD(_reuseTimeStamps.size) // list size
        for (ts in _reuseTimeStamps) {
            writeD(ts.id)
            writeD(ts.value)
            writeD(ts.reuse.toInt() / 1000)
            writeD(ts.remaining.toInt() / 1000)
        }
    }
}