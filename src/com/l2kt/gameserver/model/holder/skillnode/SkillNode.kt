package com.l2kt.gameserver.model.holder.skillnode

import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.templates.StatsSet

/**
 * A generic datatype used to store skills informations for player templates.<br></br>
 * <br></br>
 * It extends [IntIntHolder] and isn't directly used.
 */
open class SkillNode(set: StatsSet) : IntIntHolder(set.getInteger("id"), set.getInteger("lvl")) {
    val minLvl: Int = set.getInteger("minLvl")

}