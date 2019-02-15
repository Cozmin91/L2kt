package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.scripts.village_master.FirstClassChange
import com.l2kt.gameserver.scripting.scripts.village_master.SecondClassChange

/**
 * @authors: DrLecter (python), Nyaran (java)
 */
class ShadowWeapon : Quest(-1, "custom") {
    init {

        addStartNpc(*FirstClassChange.FIRSTCLASSNPCS)
        addTalkId(*FirstClassChange.FIRSTCLASSNPCS)

        addStartNpc(*SecondClassChange.SECONDCLASSNPCS)
        addTalkId(*SecondClassChange.SECONDCLASSNPCS)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = noQuestMsg
        if (st == null)
            return htmltext

        val hasD = st.hasQuestItems(D_COUPON)
        val hasC = st.hasQuestItems(C_COUPON)

        if (hasD || hasC) {
            // let's assume character had both c & d-grade coupons, we'll confirm later
            var multisell = "306893003"
            if (!hasD)
            // if s/he had c-grade only...
                multisell = "306893002"
            else if (!hasC)
            // or d-grade only.
                multisell = "306893001"

            // finally, return htm with proper multisell value in it.
            htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell)
        } else
            htmltext = "exchange-no.htm"

        st.exitQuest(true)
        return htmltext
    }

    companion object {
        private const val qn = "ShadowWeapon"

        // itemId for shadow weapon coupons, it's not used more than once but increases readability
        private const val D_COUPON = 8869
        private const val C_COUPON = 8870
    }
}