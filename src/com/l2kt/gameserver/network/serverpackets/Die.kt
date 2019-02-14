package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.pledge.Clan

class Die(private val _activeChar: Creature) : L2GameServerPacket() {
    private val _charObjId: Int = _activeChar.objectId
    private val _fake: Boolean = !_activeChar.isDead

    private var _sweepable: Boolean = false
    private var _allowFixedRes: Boolean = false
    private var _clan: Clan? = null

    init {

        if (_activeChar is Player) {
            _allowFixedRes = _activeChar.accessLevel.allowFixedRes
            _clan = _activeChar.clan

        } else if (_activeChar is Attackable)
            _sweepable = _activeChar.isSpoiled
    }

    override fun writeImpl() {
        if (_fake)
            return

        writeC(0x06)
        writeD(_charObjId)
        writeD(0x01) // to nearest village

        if (_clan != null) {
            var side: Siege.SiegeSide? = null

            val siege = CastleManager.getInstance().getActiveSiege(_activeChar)
            if (siege != null)
                side = siege.getSide(_clan)

            writeD(if (_clan!!.hasHideout()) 0x01 else 0x00) // to clanhall
            writeD(if (_clan!!.hasCastle() || side == Siege.SiegeSide.OWNER || side == Siege.SiegeSide.DEFENDER) 0x01 else 0x00) // to castle
            writeD(if (side == Siege.SiegeSide.ATTACKER && _clan!!.flag != null) 0x01 else 0x00) // to siege HQ
        } else {
            writeD(0x00) // to clanhall
            writeD(0x00) // to castle
            writeD(0x00) // to siege HQ
        }

        writeD(if (_sweepable) 0x01 else 0x00) // sweepable (blue glow)
        writeD(if (_allowFixedRes) 0x01 else 0x00) // FIXED
    }
}