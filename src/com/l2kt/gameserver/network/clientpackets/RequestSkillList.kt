package com.l2kt.gameserver.network.clientpackets

class RequestSkillList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val cha = client.activeChar ?: return

        cha.sendSkillList()
    }
}