package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.CrystalType.*
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats

class FuncEnchant(pStat: Stats, pOrder: Int, owner: Any, lambda: Lambda) : Func(pStat, pOrder, owner, lambda) {

    override fun calc(env: Env) {
        if (cond != null && cond?.test(env) == false)
            return

        val item = funcOwner as ItemInstance

        var enchant = item.enchantLevel
        if (enchant <= 0)
            return

        var overenchant = 0

        if (enchant > 3) {
            overenchant = enchant - 3
            enchant = 3
        }

        if (stat === Stats.MAGIC_DEFENCE || stat === Stats.POWER_DEFENCE) {
            env.addValue((enchant + 3 * overenchant).toDouble())
            return
        }

        if (stat === Stats.MAGIC_ATTACK) {
            when (item.item.crystalType) {
                S -> env.addValue((4 * enchant + 8 * overenchant).toDouble())

                A, B, C -> env.addValue((3 * enchant + 6 * overenchant).toDouble())

                D -> env.addValue((2 * enchant + 4 * overenchant).toDouble())
            }
            return
        }

        if (item.isWeapon) {
            val type = item.itemType as WeaponType

            when (item.item.crystalType) {
                S -> when (type) {
                    WeaponType.BOW -> env.addValue((10 * enchant + 20 * overenchant).toDouble())

                    WeaponType.BIGBLUNT, WeaponType.BIGSWORD, WeaponType.DUALFIST, WeaponType.DUAL -> env.addValue((6 * enchant + 12 * overenchant).toDouble())

                    else -> env.addValue((5 * enchant + 10 * overenchant).toDouble())
                }

                A -> when (type) {
                    WeaponType.BOW -> env.addValue((8 * enchant + 16 * overenchant).toDouble())

                    WeaponType.BIGBLUNT, WeaponType.BIGSWORD, WeaponType.DUALFIST, WeaponType.DUAL -> env.addValue((5 * enchant + 10 * overenchant).toDouble())

                    else -> env.addValue((4 * enchant + 8 * overenchant).toDouble())
                }

                B -> when (type) {
                    WeaponType.BOW -> env.addValue((6 * enchant + 12 * overenchant).toDouble())

                    WeaponType.BIGBLUNT, WeaponType.BIGSWORD, WeaponType.DUALFIST, WeaponType.DUAL -> env.addValue((4 * enchant + 8 * overenchant).toDouble())

                    else -> env.addValue((3 * enchant + 6 * overenchant).toDouble())
                }

                C -> when (type) {
                    WeaponType.BOW -> env.addValue((6 * enchant + 12 * overenchant).toDouble())

                    WeaponType.BIGBLUNT, WeaponType.BIGSWORD, WeaponType.DUALFIST, WeaponType.DUAL -> env.addValue((4 * enchant + 8 * overenchant).toDouble())

                    else -> env.addValue((3 * enchant + 6 * overenchant).toDouble())
                }

                D -> when (type) {
                    WeaponType.BOW -> env.addValue((4 * enchant + 8 * overenchant).toDouble())

                    else -> env.addValue((2 * enchant + 4 * overenchant).toDouble())
                }
            }
        }
    }
}