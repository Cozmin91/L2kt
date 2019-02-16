package com.l2kt.gameserver.handler

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.logging.Logger

interface ISkillHandler {

    /**
     * this method is called at initialization to register all the skill ids automatically
     * @return all known itemIds
     */
    val skillIds: Array<L2SkillType>

    /**
     * this is the worker method that is called when using a skill.
     * @param activeChar The Creature who uses that skill.
     * @param skill The skill object itself.
     * @param targets Eventual targets.
     */
    fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>)

    companion object {
        val _log = Logger.getLogger(ISkillHandler::class.java.name)
    }
}