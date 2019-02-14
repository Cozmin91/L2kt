package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.network.SystemMessageId

class RequestBlock : L2GameClientPacket() {

    private var _name: String = ""
    private var _type: Int = 0

    override fun readImpl() {
        _type = readD() // 0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

        if (_type == BLOCK || _type == UNBLOCK)
            _name = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        when (_type) {
            BLOCK, UNBLOCK -> {
                // Can't block/unblock inexisting or self.
                val targetId = PlayerInfoTable.getPlayerObjectId(_name)
                if (targetId <= 0 || activeChar.objectId == targetId) {
                    activeChar.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST)
                    return
                }

                // Can't block a GM character.
                if (PlayerInfoTable.getPlayerAccessLevel(targetId) > 0) {
                    activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM)
                    return
                }

                if (_type == BLOCK)
                    BlockList.addToBlockList(activeChar, targetId)
                else
                    BlockList.removeFromBlockList(activeChar, targetId)
            }

            BLOCKLIST -> BlockList.sendListToOwner(activeChar)

            ALLBLOCK -> {
                activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE)// Update by rocknow
                BlockList.setBlockAll(activeChar, true)
            }

            ALLUNBLOCK -> {
                activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE)// Update by rocknow
                BlockList.setBlockAll(activeChar, false)
            }

            else -> L2GameClientPacket.LOGGER.warn("Unknown block type detected: {}.", _type)
        }
    }

    companion object {
        private const val BLOCK = 0
        private const val UNBLOCK = 1
        private const val BLOCKLIST = 2
        private const val ALLBLOCK = 3
        private const val ALLUNBLOCK = 4
    }
}