package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats

class FuncSubDiv(pStat: Stats, pOrder: Int, owner: Any, lambda: Lambda) : Func(pStat, pOrder, owner, lambda) {

    override fun calc(env: Env) {
        if (cond == null || cond?.test(env) == true)
            env.divValue(1 - _lambda!!.calc(env) / 100)
    }
}