package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.templates.skills.L2EffectFlag

class EtcStatusUpdate(private val _player: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xF3)
        writeD(_player.charges)
        writeD(_player.weightPenalty)
        writeD(if (_player.isInRefusalMode || _player.isChatBanned) 1 else 0)
        writeD(if (_player.isInsideZone(ZoneId.DANGER_AREA)) 1 else 0)
        writeD(if (_player.expertiseWeaponPenalty || _player.expertiseArmorPenalty > 0) 1 else 0)
        writeD(if (_player.isAffected(L2EffectFlag.CHARM_OF_COURAGE)) 1 else 0)
        writeD(_player.deathPenaltyBuffLevel)
    }
}