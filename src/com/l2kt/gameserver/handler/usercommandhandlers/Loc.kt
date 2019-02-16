package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class Loc : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        val msg: SystemMessageId

        when (MapRegionData.getMapRegion(activeChar.x, activeChar.y)) {
            0 -> msg = SystemMessageId.LOC_TI_S1_S2_S3
            1 -> msg = SystemMessageId.LOC_ELVEN_S1_S2_S3
            2 -> msg = SystemMessageId.LOC_DARK_ELVEN_S1_S2_S3
            3 -> msg = SystemMessageId.LOC_ORC_S1_S2_S3
            4 -> msg = SystemMessageId.LOC_DWARVEN_S1_S2_S3
            5 -> msg = SystemMessageId.LOC_GLUDIO_S1_S2_S3
            6 -> msg = SystemMessageId.LOC_GLUDIN_S1_S2_S3
            7 -> msg = SystemMessageId.LOC_DION_S1_S2_S3
            8 -> msg = SystemMessageId.LOC_GIRAN_S1_S2_S3
            9 -> msg = SystemMessageId.LOC_OREN_S1_S2_S3
            10 -> msg = SystemMessageId.LOC_ADEN_S1_S2_S3
            11 -> msg = SystemMessageId.LOC_HUNTER_S1_S2_S3
            12 -> msg = SystemMessageId.LOC_GIRAN_HARBOR_S1_S2_S3
            13 -> msg = SystemMessageId.LOC_HEINE_S1_S2_S3
            14 -> msg = SystemMessageId.LOC_RUNE_S1_S2_S3
            15 -> msg = SystemMessageId.LOC_GODDARD_S1_S2_S3
            16 -> msg = SystemMessageId.LOC_SCHUTTGART_S1_S2_S3
            17 -> msg = SystemMessageId.LOC_FLORAN_S1_S2_S3
            18 -> msg = SystemMessageId.LOC_PRIMEVAL_ISLE_S1_S2_S3
            else -> msg = SystemMessageId.LOC_ADEN_S1_S2_S3
        }

        activeChar.sendPacket(
            SystemMessage.getSystemMessage(msg).addNumber(activeChar.x).addNumber(activeChar.y).addNumber(
                activeChar.z
            )
        )
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(0)
    }
}