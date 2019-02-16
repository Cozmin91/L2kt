package com.l2kt.gameserver.model.petition

import java.util.ArrayList

import com.l2kt.gameserver.data.manager.PetitionManager
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * A Petition is a report, generally made by a [Player] to a Game Master. The categories of report are multiple.
 */
class Petition(val petitioner: Player?, val content: String, type: Int) {
    private val _messageLog = ArrayList<CreatureSay>()

    val id: Int
    private val _type: PetitionType
    val submitTime = System.currentTimeMillis()

    var state = PetitionState.PENDING
    var responder: Player? = null
        set(respondingAdmin) {
            if (responder != null)
                return

            field = respondingAdmin
        }

    val logMessages: List<CreatureSay>
        get() = _messageLog

    val typeAsString: String
        get() = _type.toString().replace("_", " ")

    init {
        var type = type
        type--

        id = IdFactory.getInstance().nextId
        _type = PetitionType.values()[type]
    }

    fun addLogMessage(cs: CreatureSay): Boolean {
        return _messageLog.add(cs)
    }

    fun endPetitionConsultation(endState: PetitionState): Boolean {
        state = endState

        if (responder != null && responder!!.isOnline) {
            if (endState === PetitionState.RESPONDER_REJECT)
                petitioner!!.sendMessage("Your petition was rejected. Please try again later.")
            else {
                // Ending petition consultation with <Player>.
                responder!!.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.PETITION_ENDED_WITH_S1).addCharName(
                        petitioner!!
                    )
                )

                // Receipt No. <ID> petition cancelled.
                if (endState === PetitionState.PETITIONER_CANCEL)
                    responder!!.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED).addNumber(
                            id
                        )
                    )
            }
        }

        // End petition consultation and inform them, if they are still online.
        if (petitioner != null && petitioner.isOnline)
            petitioner.sendPacket(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK)

        PetitionManager.completedPetitions.put(id, this)
        return PetitionManager.pendingPetitions.remove(id) != null
    }

    fun sendPetitionerPacket(responsePacket: L2GameServerPacket) {
        if (petitioner == null || !petitioner.isOnline)
            return

        petitioner.sendPacket(responsePacket)
    }

    fun sendResponderPacket(responsePacket: L2GameServerPacket) {
        if (responder == null || !responder!!.isOnline) {
            endPetitionConsultation(PetitionState.RESPONDER_MISSING)
            return
        }

        responder!!.sendPacket(responsePacket)
    }
}