package com.l2kt.gameserver.skills.l2skills

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.templates.StatsSet
import java.util.logging.Level

class L2SkillSpawn(set: StatsSet) : L2Skill(set) {
    private val _npcId: Int = set.getInteger("npcId", 0)
    private val _despawnDelay: Int = set.getInteger("despawnDelay", 0)
    private val _summonSpawn: Boolean = set.getBool("isSummonSpawn", false)
    private val _randomOffset: Boolean = set.getBool("randomOffset", true)

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        if (caster.isAlikeDead)
            return

        if (_npcId == 0) {
            L2Skill._log.warning("NPC ID not defined for skill ID: $id")
            return
        }

        val template = NpcData.getTemplate(_npcId)

        try {
            val spawn = L2Spawn(template)

            var x = caster.x
            var y = caster.y
            if (_randomOffset) {
                x += if (Rnd.nextBoolean()) Rnd[20, 50] else Rnd[-50, -20]
                y += if (Rnd.nextBoolean()) Rnd[20, 50] else Rnd[-50, -20]
            }
            spawn.setLoc(x, y, caster.z + 20, caster.heading)

            spawn.setRespawnState(false)
            val npc = spawn.doSpawn(_summonSpawn)

            if (_despawnDelay > 0)
                npc!!.scheduleDespawn(_despawnDelay.toLong())
        } catch (e: Exception) {
            L2Skill._log.log(
                Level.WARNING,
                "Exception while spawning NPC ID: " + _npcId + ", skill ID: " + id + ", exception: " + e.message,
                e
            )
        }

    }
}