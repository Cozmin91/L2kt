package com.l2kt.gameserver.model.holder

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill

/**
 * A generic int/int container.
 */
open class IntIntHolder(var id: Int, var value: Int) {

    /**
     * @return the L2Skill associated to the id/value.
     */
    val skill: L2Skill?
        get() = SkillTable.getInfo(id, value)

    override fun toString(): String {
        return javaClass.simpleName + ": Id: " + id + ", Value: " + value
    }
}