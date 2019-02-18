package com.l2kt.gameserver.model.actor.stat

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.template.PetTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Stats

class PetStat(activeChar: Pet) : SummonStat(activeChar) {

    override// Set level.
    // If a control item exists and its level is different of the new level.
    // Update item
    var level: Byte
        get() = super.level
        set(value) {
            activeChar!!.setPetData(value.toInt())

            super.level = value
            val controlItem = activeChar!!.controlItem
            if (controlItem != null && controlItem.enchantLevel != level.toInt()) {
                activeChar!!.sendPetInfosToOwner()

                controlItem.enchantLevel = level.toInt()
                val iu = InventoryUpdate()
                iu.addModifiedItem(controlItem)
                activeChar!!.owner.sendPacket(iu)
            }
        }

    override val maxHp: Int
        get() = calcStat(Stats.MAX_HP, activeChar!!.petData.maxHp, null, null).toInt()

    override val maxMp: Int
        get() = calcStat(Stats.MAX_MP, activeChar!!.petData.maxMp, null, null).toInt()

    override val mAtkSpd: Int
        get() {
            var base = 333.0

            if (activeChar!!.checkHungryState())
                base /= 2.0

            return calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null).toInt()
        }

    override val pAtkSpd: Int
        get() {
            var base = activeChar!!.template.basePAtkSpd.toDouble()

            if (activeChar!!.checkHungryState())
                base /= 2.0

            return calcStat(Stats.POWER_ATTACK_SPEED, base, null, null).toInt()
        }

    fun addExp(value: Int): Boolean {
        if (!super.addExp(value.toLong()))
            return false

        activeChar!!.updateAndBroadcastStatus(1)
        return true
    }

    override fun addExpAndSp(addToExp: Long, addToSp: Int): Boolean {
        if (!super.addExpAndSp(addToExp, addToSp))
            return false

        activeChar!!.owner.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber(
                addToExp.toInt()
            )
        )
        return true
    }

    override fun addLevel(value: Byte): Boolean {
        if (level + value > maxLevel - 1)
            return false

        val levelIncreased = super.addLevel(value)
        if (levelIncreased)
            activeChar!!.broadcastPacket(SocialAction(activeChar!!, 15))

        return levelIncreased
    }

    override fun getExpForLevel(level: Int): Long {
        return (activeChar!!.template as PetTemplate).getPetDataEntry(level)?.maxExp ?: 0L
    }

    override val activeChar: Pet? get() = super.activeChar as Pet?

    override fun getMAtk(target: Creature?, skill: L2Skill?): Int {
        var attack = activeChar!!.petData.mAtk

        if (skill != null)
            attack += skill.power

        return calcStat(Stats.MAGIC_ATTACK, attack, target, skill).toInt()
    }

    override fun getMDef(target: Creature?, skill: L2Skill?): Int {
        return calcStat(Stats.MAGIC_DEFENCE, activeChar!!.petData.mDef, target, skill).toInt()
    }

    override fun getPAtk(target: Creature?): Int {
        return calcStat(Stats.POWER_ATTACK, activeChar!!.petData.pAtk, target, null).toInt()
    }

    override fun getPDef(target: Creature?): Int {
        return calcStat(Stats.POWER_DEFENCE, activeChar!!.petData.pDef, target, null).toInt()
    }
}