package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncPDefMod : Func(Stats.POWER_DEFENCE, 0x20, null, null) {

    override fun calc(env: Env) {
        if (env.character is Player) {
            val player = env.player
            val isMage = player!!.isMageClass

            if (player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
                env.subValue(12.0)
            if (player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
                env.subValue((if (isMage) 15 else 31).toDouble())
            if (player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
                env.subValue((if (isMage) 8 else 18).toDouble())
            if (player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
                env.subValue(8.0)
            if (player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
                env.subValue(7.0)
        }

        env.mulValue(env.character!!.levelMod)
    }
}