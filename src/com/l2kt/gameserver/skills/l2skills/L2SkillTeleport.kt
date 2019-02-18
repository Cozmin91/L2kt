package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.templates.StatsSet
import com.l2kt.gameserver.templates.skills.L2SkillType

class L2SkillTeleport(set: StatsSet) : L2Skill(set) {
    private val _recallType: String? = set.getString("recallType", null)
    private val _loc: Location?

    init {

        val coords = set.getString("teleCoords", null)
        if (coords != null) {
            val valuesSplit = coords.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
            _loc = Location(
                Integer.parseInt(valuesSplit[0]),
                Integer.parseInt(valuesSplit[1]),
                Integer.parseInt(valuesSplit[2])
            )
        } else
            _loc = null
    }

    override fun useSkill(activeChar: Creature, targets: Array<WorldObject>) {
        if (activeChar is Player) {
            // Check invalid states.
            if (activeChar.isAfraid || activeChar.isInOlympiadMode || activeChar.isInsideZone(ZoneId.BOSS))
                return
        }

        val bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj is Player) {

                // Check invalid states.
                if (obj.isFestivalParticipant || obj.isInJail || obj.isInDuel)
                    continue

                if (obj != activeChar) {
                    if (obj.isInOlympiadMode)
                        continue

                    if (obj.isInsideZone(ZoneId.BOSS))
                        continue
                }
            }

            var loc: Location? = null
            if (skillType === L2SkillType.TELEPORT) {
                if (_loc != null) {
                    if (obj !is Player || !obj.isFlying)
                        loc = _loc
                }
            } else {
                if (_recallType.equals("Castle", ignoreCase = true))
                    loc = MapRegionData.getLocationToTeleport(obj, MapRegionData.TeleportType.CASTLE)
                else if (_recallType.equals("ClanHall", ignoreCase = true))
                    loc = MapRegionData.getLocationToTeleport(obj, MapRegionData.TeleportType.CLAN_HALL)
                else
                    loc = MapRegionData.getLocationToTeleport(obj, MapRegionData.TeleportType.TOWN)
            }

            if (loc != null) {
                if (obj is Player)
                    obj.isIn7sDungeon = false

                obj.teleToLocation(loc, 20)
            }
        }

        activeChar.setChargedShot(if (bsps) ShotType.BLESSED_SPIRITSHOT else ShotType.SPIRITSHOT, isStaticReuse)
    }
}