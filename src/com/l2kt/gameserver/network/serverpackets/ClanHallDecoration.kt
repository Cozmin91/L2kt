package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.entity.ClanHall

/**
 * @author Steuf
 */
class ClanHallDecoration(private val _clanHall: ClanHall) : L2GameServerPacket() {
    private var _function: ClanHall.ClanHallFunction? = null

    override fun writeImpl() {
        writeC(0xf7)
        writeD(_clanHall.id) // clanhall id

        // FUNC_RESTORE_HP
        _function = _clanHall.getFunction(ClanHall.FUNC_RESTORE_HP)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_clanHall.grade == 0 && _function!!.lvl < 220 || _clanHall.grade == 1 && _function!!.lvl < 160 || _clanHall.grade == 2 && _function!!.lvl < 260 || _clanHall.grade == 3 && _function!!.lvl < 300)
            writeC(1)
        else
            writeC(2)

        // FUNC_RESTORE_MP
        _function = _clanHall.getFunction(ClanHall.FUNC_RESTORE_MP)
        if (_function == null || _function!!.lvl == 0) {
            writeC(0)
            writeC(0)
        } else if ((_clanHall.grade == 0 || _clanHall.grade == 1) && _function!!.lvl < 25 || _clanHall.grade == 2 && _function!!.lvl < 30 || _clanHall.grade == 3 && _function!!.lvl < 40) {
            writeC(1)
            writeC(1)
        } else {
            writeC(2)
            writeC(2)
        }

        // FUNC_RESTORE_EXP
        _function = _clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_clanHall.grade == 0 && _function!!.lvl < 25 || _clanHall.grade == 1 && _function!!.lvl < 30 || _clanHall.grade == 2 && _function!!.lvl < 40 || _clanHall.grade == 3 && _function!!.lvl < 50)
            writeC(1)
        else
            writeC(2)

        // FUNC_TELEPORT
        _function = _clanHall.getFunction(ClanHall.FUNC_TELEPORT)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_function!!.lvl < 2)
            writeC(1)
        else
            writeC(2)

        writeC(0)

        // CURTAINS
        _function = _clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_function!!.lvl <= 1)
            writeC(1)
        else
            writeC(2)

        // FUNC_ITEM_CREATE
        _function = _clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_clanHall.grade == 0 && _function!!.lvl < 2 || _function!!.lvl < 3)
            writeC(1)
        else
            writeC(2)

        // FUNC_SUPPORT
        _function = _clanHall.getFunction(ClanHall.FUNC_SUPPORT)
        if (_function == null || _function!!.lvl == 0) {
            writeC(0)
            writeC(0)
        } else if (_clanHall.grade == 0 && _function!!.lvl < 2 || _clanHall.grade == 1 && _function!!.lvl < 4 || _clanHall.grade == 2 && _function!!.lvl < 5 || _clanHall.grade == 3 && _function!!.lvl < 8) {
            writeC(1)
            writeC(1)
        } else {
            writeC(2)
            writeC(2)
        }

        // Front Plateform
        _function = _clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_function!!.lvl <= 1)
            writeC(1)
        else
            writeC(2)

        // FUNC_ITEM_CREATE
        _function = _clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE)
        if (_function == null || _function!!.lvl == 0)
            writeC(0)
        else if (_clanHall.grade == 0 && _function!!.lvl < 2 || _function!!.lvl < 3)
            writeC(1)
        else
            writeC(2)

        writeD(0)
        writeD(0)
    }
}