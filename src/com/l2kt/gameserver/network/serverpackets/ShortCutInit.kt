package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.L2ShortCut
import com.l2kt.gameserver.model.actor.instance.Player

class ShortCutInit(private val _activeChar: Player) : L2GameServerPacket() {
    private val _shortCuts: Array<L2ShortCut> = _activeChar.allShortCuts

    override fun writeImpl() {
        writeC(0x45)
        writeD(_shortCuts.size)

        for (sc in _shortCuts) {
            writeD(sc.type)
            writeD(sc.slot + sc.page * 12)

            when (sc.type) {
                L2ShortCut.TYPE_ITEM // 1
                -> {
                    writeD(sc.id)
                    writeD(sc.characterType)
                    writeD(sc.sharedReuseGroup)

                    if (sc.sharedReuseGroup < 0) {
                        writeD(0x00) // Remaining time
                        writeD(0x00) // Cooldown time
                    } else {
                        val item = _activeChar.inventory!!.getItemByObjectId(sc.id)
                        if (item == null || !item.isEtcItem) {
                            writeD(0x00) // Remaining time
                            writeD(0x00) // Cooldown time
                        } else {
                            val skills = item.etcItem!!.skills
                            if (skills.isEmpty()) {
                                writeD(0x00) // Remaining time
                                writeD(0x00) // Cooldown time
                            } else {
                                for (skillInfo in skills) {
                                    val itemSkill = skillInfo.skill
                                    if (_activeChar.reuseTimeStamp.containsKey(itemSkill?.reuseHashCode)) {
                                        writeD((_activeChar.reuseTimeStamp[itemSkill?.reuseHashCode]!!.remaining / 1000L).toInt())
                                        writeD(((itemSkill?.reuseDelay ?: 0) / 1000L).toInt())
                                    } else {
                                        writeD(0x00) // Remaining time
                                        writeD(0x00) // Cooldown time
                                    }
                                }
                            }
                        }
                    }

                    writeD(0x00) // Augmentation
                }

                L2ShortCut.TYPE_SKILL // 2
                -> {
                    writeD(sc.id)
                    writeD(sc.level)
                    writeC(0x00) // C5
                    writeD(0x01) // C6
                }

                else -> {
                    writeD(sc.id)
                    writeD(0x01) // C6
                }
            }
        }
    }
}