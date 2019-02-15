package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class HeroWeapon : Quest(-1, "custom") {
    init {

        addStartNpc(31690, 31769, 31770, 31771, 31772, 31773)
        addTalkId(31690, 31769, 31770, 31771, 31772, 31773)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(name)

        val weaponId = Integer.valueOf(event)
        if (ArraysUtil.contains(WEAPON_IDS, weaponId))
            st?.giveItems(weaponId, 1)

        st?.exitQuest(true)
        return null
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        val st = player.getQuestState(name)
        if (st == null)
            newQuestState(player)

        if (st != null) {
            if (player.isHero) {
                if (hasHeroWeapon(player)) {
                    htmltext = "already_have_weapon.htm"
                    st.exitQuest(true)
                } else
                    htmltext = "weapon_list.htm"
            } else {
                htmltext = "no_hero.htm"
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    companion object {
        private val WEAPON_IDS = intArrayOf(6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621)

        private fun hasHeroWeapon(player: Player): Boolean {
            for (i in WEAPON_IDS) {
                if (player.inventory!!.getItemByItemId(i) != null)
                    return true
            }

            return false
        }
    }
}