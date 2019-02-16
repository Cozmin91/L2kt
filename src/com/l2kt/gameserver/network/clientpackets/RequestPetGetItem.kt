package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.network.serverpackets.ActionFailed

class RequestPetGetItem : L2GameClientPacket() {
    private var _objectId: Int = 0

    override fun readImpl() {
        _objectId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar
        if (activeChar == null || !activeChar.hasPet())
            return

        val item = World.getObject(_objectId) ?: return

        val pet = activeChar.pet as Pet
        if (pet.isDead || pet.isOutOfControl) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        pet.ai.setIntention(CtrlIntention.PICK_UP, item)
    }
}