package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate

class HalishaChest(objectId: Int, template: NpcTemplate) : Monster(objectId, template) {
    init {

        setIsNoRndWalk(true)
        isShowSummonAnimation = true
        disableCoreAI(true)
    }

    override fun isMovementDisabled(): Boolean {
        return true
    }

    override fun hasRandomAnimation(): Boolean {
        return false
    }
}