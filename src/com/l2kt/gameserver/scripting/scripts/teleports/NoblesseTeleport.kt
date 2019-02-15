package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class NoblesseTeleport : Quest(-1, "teleports") {
    init {

        addStartNpc(
            30006,
            30059,
            30080,
            30134,
            30146,
            30177,
            30233,
            30256,
            30320,
            30540,
            30576,
            30836,
            30848,
            30878,
            30899,
            31275,
            31320,
            31964
        )
        addTalkId(
            30006,
            30059,
            30080,
            30134,
            30146,
            30177,
            30233,
            30256,
            30320,
            30540,
            30576,
            30836,
            30848,
            30878,
            30899,
            31275,
            31320,
            31964
        )
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        return if (player.isNoble) "noble.htm" else "nobleteleporter-no.htm"
    }
}