package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.type.FishingZone

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.templates.skills.L2SkillType

class Fishing : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

// Cancels fishing
        if (activeChar.isFishing) {
            activeChar.fishingStance.end(false)
            activeChar.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED)
            return
        }

        // Fishing pole isn't equipped.
        if (activeChar.attackType != WeaponType.FISHINGROD) {
            activeChar.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED)
            return
        }

        // You can't fish while you are on boat
        if (activeChar.isInBoat) {
            activeChar.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT)
            return
        }

        if (activeChar.isCrafting || activeChar.isInStoreMode) {
            activeChar.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK)
            return
        }

        // You can't fish in water
        if (activeChar.isInsideZone(ZoneId.WATER)) {
            activeChar.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER)
            return
        }

        // Check equipped baits.
        val lure = activeChar.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND)
        if (lure == null) {
            activeChar.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING)
            return
        }

        val rnd = Rnd[50] + 250
        val radian = Math.toRadians(MathUtil.convertHeadingToDegree(activeChar.heading))

        val x = activeChar.x + (Math.cos(radian) * rnd).toInt()
        val y = activeChar.y + (Math.sin(radian) * rnd).toInt()

        var canFish = false
        var z = 0

        // Pick the fishing zone.
        val zone = ZoneManager.getZone(x, y, FishingZone::class.java)
        if (zone != null) {
            z = zone.waterZ

            // Check if the height related to the bait location is above water level. If yes, it means the water isn't visible.
            if (GeoEngine.canSeeTarget(activeChar, Location(x, y, z)) && GeoEngine.getHeight(x, y, z) < z) {
                z += 10
                canFish = true
            }
        }

        // You can't fish here.
        if (!canFish) {
            activeChar.sendPacket(SystemMessageId.CANNOT_FISH_HERE)
            return
        }

        // Has enough bait, consume 1 and update inventory.
        if (!activeChar.destroyItem("Consume", lure, 1, activeChar, false)) {
            activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT)
            return
        }

        // Start fishing.
        activeChar.fishingStance.start(x, y, z, lure)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.FISHING)
    }
}