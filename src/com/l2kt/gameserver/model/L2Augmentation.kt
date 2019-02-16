package com.l2kt.gameserver.model

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.AugmentationData
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SkillCoolTime
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.FuncAdd
import com.l2kt.gameserver.skills.basefuncs.LambdaConst

/**
 * Used to store an augmentation and its boni
 * @author durgus
 */
class L2Augmentation(effects: Int, skill: L2Skill?) {
    var attributes = 0
    private var _boni: AugmentationStatBoni? = null
    var skill: L2Skill? = null

    init {
        attributes = effects
        _boni = AugmentationStatBoni(attributes)
        this.skill = skill
    }

    constructor(effects: Int, skill: Int, skillLevel: Int) : this(effects, if (skill != 0) SkillTable.getInfo(skill, skillLevel) else null)

    class AugmentationStatBoni(augmentationId: Int) {
        private val _stats: Array<Stats?>
        private val _values: FloatArray
        private var _active: Boolean = false

        init {
            _active = false
            val `as` = AugmentationData.getAugStatsById(augmentationId)

            _stats = arrayOfNulls(`as`.size)
            _values = FloatArray(`as`.size)

            var i = 0
            for (aStat in `as`) {
                _stats[i] = aStat.stat
                _values[i] = aStat.value
                i++
            }
        }

        fun applyBonus(player: Player) {
            // make sure the bonuses are not applied twice..
            if (_active)
                return

            for (i in _stats.indices)
                (player as Creature).addStatFunc(FuncAdd(_stats[i] ?: return, 0x40, this, LambdaConst(_values[i].toDouble())))

            _active = true
        }

        fun removeBonus(player: Player) {
            // make sure the bonuses are not removed twice
            if (!_active)
                return

            (player as Creature).removeStatsByOwner(this)

            _active = false
        }
    }

    /**
     * Get the augmentation "id" used in serverpackets.
     * @return augmentationId
     */
    fun getAugmentationId(): Int {
        return attributes
    }

    /**
     * Applies the bonuses to the player.
     * @param player
     */
    fun applyBonus(player: Player) {
        var updateTimeStamp = false
        _boni!!.applyBonus(player)

        // add the skill if any
        if (skill != null) {
            player.addSkill(skill, false)
            if (skill!!.isActive) {
                if (player.reuseTimeStamp.containsKey(skill!!.reuseHashCode)) {
                    val delay = player.reuseTimeStamp[skill!!.reuseHashCode]!!.remaining
                    if (delay > 0) {
                        player.disableSkill(skill, delay)
                        updateTimeStamp = true
                    }
                }
            }
            player.sendSkillList()
            if (updateTimeStamp)
                player.sendPacket(SkillCoolTime(player))
        }
    }

    /**
     * Removes the augmentation bonuses from the player.
     * @param player
     */
    fun removeBonus(player: Player) {
        _boni!!.removeBonus(player)

        // remove the skill if any
        if (skill != null) {
            player.removeSkill(skill!!.id, false, skill!!.isPassive || skill!!.isToggle)
            player.sendSkillList()
        }
    }
}