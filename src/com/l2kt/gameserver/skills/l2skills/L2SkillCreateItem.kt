package com.l2kt.gameserver.skills.l2skills

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PetItemList
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet

class L2SkillCreateItem(set: StatsSet) : L2Skill(set) {
    private val _createItemId: IntArray? = set.getIntegerArray("create_item_id")
    private val _createItemCount: Int = set.getInteger("create_item_count", 0)
    private val _randomCount: Int = set.getInteger("random_count", 1)

    override fun useSkill(activeChar: Creature, targets: Array<WorldObject>) {
        val player = activeChar.actingPlayer
        if (activeChar.isAlikeDead)
            return

        if (activeChar is Playable) {
            if (_createItemId == null || _createItemCount == 0) {
                val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE)
                sm.addSkillName(this)
                activeChar.sendPacket(sm)
                return
            }

            val count = _createItemCount + Rnd[_randomCount]
            val rndid = Rnd[_createItemId.size]

            if (activeChar is Player)
                player!!.addItem("Skill", _createItemId[rndid], count, activeChar, true)
            else if (activeChar is Pet) {
                activeChar.inventory!!.addItem("Skill", _createItemId[rndid], count, player, activeChar)
                player!!.sendPacket(PetItemList(activeChar))
            }
        }
    }
}