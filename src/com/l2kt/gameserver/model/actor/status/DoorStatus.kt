package com.l2kt.gameserver.model.actor.status

import com.l2kt.gameserver.model.actor.instance.Door

class DoorStatus(activeChar: Door) : CreatureStatus(activeChar) {

    override val activeChar: Door
        get() = super.activeChar as Door
}