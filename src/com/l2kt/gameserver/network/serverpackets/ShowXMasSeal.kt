package com.l2kt.gameserver.network.serverpackets

/**
 * @author devScarlet & mrTJO
 */
class ShowXMasSeal(private val _item: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xF2)
        writeD(_item)
    }
}