package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExAutoSoulShot
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestAutoSoulShot : L2GameClientPacket() {
    private var _itemId: Int = 0
    private var _type: Int = 0 // 1 = on : 0 = off;

    override fun readImpl() {
        _itemId = readD()
        _type = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!activeChar.isInStoreMode && activeChar.activeRequester == null && !activeChar.isDead) {
            val item = activeChar.inventory!!.getItemByItemId(_itemId) ?: return

            if (_type == 1) {
                // Fishingshots are not automatic on retail
                if (_itemId < 6535 || _itemId > 6540) {
                    // Attempt to charge first shot on activation
                    if (_itemId == 6645 || _itemId == 6646 || _itemId == 6647) {
                        if (activeChar.pet != null) {
                            // Cannot activate bss automation during Olympiad.
                            if (_itemId == 6647 && activeChar.isInOlympiadMode) {
                                activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT)
                                return
                            }

                            if (_itemId == 6645) {
                                if (activeChar.pet!!.soulShotsPerHit > item.count) {
                                    activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET)
                                    return
                                }
                            } else {
                                if (activeChar.pet!!.spiritShotsPerHit > item.count) {
                                    activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS_FOR_PET)
                                    return
                                }
                            }

                            // start the auto soulshot use
                            activeChar.addAutoSoulShot(_itemId)
                            activeChar.sendPacket(ExAutoSoulShot(_itemId, _type))
                            activeChar.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(
                                    _itemId
                                )
                            )
                            activeChar.rechargeShots(true, true)
                            activeChar.pet!!.rechargeShots(true, true)
                        } else
                            activeChar.sendPacket(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE)
                    } else {
                        // Cannot activate bss automation during Olympiad.
                        if (_itemId >= 3947 && _itemId <= 3952 && activeChar.isInOlympiadMode) {
                            activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT)
                            return
                        }

                        // Activate the visual effect
                        activeChar.addAutoSoulShot(_itemId)
                        activeChar.sendPacket(ExAutoSoulShot(_itemId, _type))

                        // start the auto soulshot use
                        if (activeChar.activeWeaponInstance != null && item.item.crystalType == activeChar.activeWeaponItem.crystalType)
                            activeChar.rechargeShots(true, true)
                        else {
                            if (_itemId in 2509..2514 || _itemId in 3947..3952 || _itemId == 5790)
                                activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH)
                            else
                                activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH)
                        }

                        // In both cases (match/mismatch), that message is displayed.
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(
                                _itemId
                            )
                        )
                    }
                }
            } else if (_type == 0) {
                // cancel the auto soulshot use
                activeChar.removeAutoSoulShot(_itemId)
                activeChar.sendPacket(ExAutoSoulShot(_itemId, _type))
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(
                        _itemId
                    )
                )
            }
        }
    }
}