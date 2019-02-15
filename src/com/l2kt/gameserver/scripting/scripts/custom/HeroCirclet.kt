package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class HeroCirclet : Quest(-1, "custom") {
    init {

        addStartNpc(31690, 31769, 31770, 31771, 31772)
        addTalkId(31690, 31769, 31770, 31771, 31772)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        var st = player.getQuestState(name)
        if (st == null)
            st = newQuestState(player)

        if (player.isHero) {
            if (player.inventory!!.getItemByItemId(6842) == null)
                st.giveItems(6842, 1)
            else
                htmltext = "already_have_circlet.htm"
        } else
            htmltext = "no_hero.htm"

        st.exitQuest(true)
        return htmltext
    }
}