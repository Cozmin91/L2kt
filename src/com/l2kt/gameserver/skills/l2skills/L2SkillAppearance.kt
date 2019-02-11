package com.l2kt.gameserver.skills.l2skills

import java.util.logging.Level

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.StatsSet

class L2SkillAppearance(set: StatsSet) : L2Skill(set) {
    private val _faceId: Int = set.getInteger("faceId", -1)
    private val _hairColorId: Int = set.getInteger("hairColorId", -1)
    private val _hairStyleId: Int = set.getInteger("hairStyleId", -1)

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        try {
            for (target in targets) {
                if (target is Player) {
                    if (_faceId >= 0)
                        target.appearance.setFace(_faceId)
                    if (_hairColorId >= 0)
                        target.appearance.setHairColor(_hairColorId)
                    if (_hairStyleId >= 0)
                        target.appearance.setHairStyle(_hairStyleId)

                    target.broadcastUserInfo()
                }
            }
        } catch (e: Exception) {
            L2Skill._log.log(Level.SEVERE, "", e)
        }

    }
}