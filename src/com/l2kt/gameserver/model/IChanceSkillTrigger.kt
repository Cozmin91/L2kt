package com.l2kt.gameserver.model

/**
 * This interface provides method to handle triggered skills from other objects.<br></br>
 * For example, other skill, an effect, etc...
 * @author DrHouse
 */
interface IChanceSkillTrigger {

    val triggeredChanceId: Int

    val triggeredChanceLevel: Int

    val triggeredChanceCondition: ChanceCondition?
    fun triggersChanceSkill(): Boolean
}