package com.l2kt.gameserver.model.actor.status

import com.l2kt.gameserver.model.actor.Playable

open class PlayableStatus(activeChar: Playable) : CreatureStatus(activeChar) {

    override val activeChar: Playable
        get() = super.activeChar as Playable
}