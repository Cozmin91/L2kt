package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item

class EquipUpdate(private val _item: ItemInstance, private val _change: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x4b)
        writeD(_change)
        writeD(_item.objectId)

        var bodypart = 0
        when (_item.item.bodyPart) {
            Item.SLOT_L_EAR -> bodypart = 0x01
            Item.SLOT_R_EAR -> bodypart = 0x02
            Item.SLOT_NECK -> bodypart = 0x03
            Item.SLOT_R_FINGER -> bodypart = 0x04
            Item.SLOT_L_FINGER -> bodypart = 0x05
            Item.SLOT_HEAD -> bodypart = 0x06
            Item.SLOT_R_HAND -> bodypart = 0x07
            Item.SLOT_L_HAND -> bodypart = 0x08
            Item.SLOT_GLOVES -> bodypart = 0x09
            Item.SLOT_CHEST -> bodypart = 0x0a
            Item.SLOT_LEGS -> bodypart = 0x0b
            Item.SLOT_FEET -> bodypart = 0x0c
            Item.SLOT_BACK -> bodypart = 0x0d
            Item.SLOT_LR_HAND -> bodypart = 0x0e
            Item.SLOT_HAIR -> bodypart = 0x0f
        }

        writeD(bodypart)
    }
}