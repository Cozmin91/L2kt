package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncMDefMod : Func(Stats.MAGIC_DEFENCE, 0x20, null, null) {

    override fun calc(env: Env) {
        if (env.character is Player) {
            if (env.player?.inventory?.getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
                env.subValue(5.0)
            if (env.player?.inventory?.getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
                env.subValue(5.0)
            if (env.player?.inventory?.getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
                env.subValue(9.0)
            if (env.player?.inventory?.getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
                env.subValue(9.0)
            if (env.player?.inventory?.getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
                env.subValue(13.0)
        }

        env.mulValue(Formulas.MEN_BONUS[env.character!!.men] * env.character!!.levelMod)
    }
}