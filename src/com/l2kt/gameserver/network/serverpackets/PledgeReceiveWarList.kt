package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.sql.ClanTable

class PledgeReceiveWarList(private val _clanList: Set<Int>, private val _tab: Int, private val _page: Int) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x3e)
        writeD(_tab)
        writeD(_page)
        writeD(if (_tab == 0) _clanList.size else if (_page == 0) if (_clanList.size >= 13) 13 else _clanList.size else _clanList.size % (13 * _page))

        var index = 0
        for (clanId in _clanList) {
            val clan = ClanTable.getInstance().getClan(clanId) ?: continue

            if (_tab != 0) {
                if (index < _page * 13) {
                    index++
                    continue
                }

                if (index == (_page + 1) * 13)
                    break

                index++
            }

            writeS(clan.name)
            writeD(_tab)
            writeD(_page)
        }
    }
}