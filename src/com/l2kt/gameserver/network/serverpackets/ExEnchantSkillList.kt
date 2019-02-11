package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.holder.skillnode.EnchantSkillNode

class ExEnchantSkillList(private val _skills: List<EnchantSkillNode>) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x17)

        writeD(_skills.size)
        for (esn in _skills) {
            writeD(esn.id)
            writeD(esn.value)
            writeD(esn.sp)
            writeQ(esn.exp.toLong())
        }
    }
}