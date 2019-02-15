package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class HuntingGroundsTeleport : Quest(-1, "teleports") {
    init {

        addStartNpc(*PRIESTS)
        addTalkId(*PRIESTS)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val playerCabal = SevenSigns.getInstance().getPlayerCabal(player.objectId)
        if (playerCabal == CabalType.NORMAL)
            return if (ArraysUtil.contains(DAWN_NPCS, npc.npcId)) "dawn_tele-no.htm" else "dusk_tele-no.htm"

        var htmltext = ""
        val check =
            SevenSigns.getInstance().isSealValidationPeriod && playerCabal == SevenSigns.getInstance().getSealOwner(
                SealType.GNOSIS
            ) && SevenSigns.getInstance().getPlayerSeal(player.objectId) == SealType.GNOSIS

        when (npc.npcId) {
            31078, 31085 -> htmltext = if (check) "low_gludin.htm" else "hg_gludin.htm"

            31079, 31086 -> htmltext = if (check) "low_gludio.htm" else "hg_gludio.htm"

            31080, 31087 -> htmltext = if (check) "low_dion.htm" else "hg_dion.htm"

            31081, 31088 -> htmltext = if (check) "low_giran.htm" else "hg_giran.htm"

            31082, 31089 -> htmltext = if (check) "low_heine.htm" else "hg_heine.htm"

            31083, 31090 -> htmltext = if (check) "low_oren.htm" else "hg_oren.htm"

            31084, 31091 -> htmltext = if (check) "low_aden.htm" else "hg_aden.htm"

            31168, 31169 -> htmltext = if (check) "low_hw.htm" else "hg_hw.htm"

            31692, 31693 -> htmltext = if (check) "low_goddard.htm" else "hg_goddard.htm"

            31694, 31695 -> htmltext = if (check) "low_rune.htm" else "hg_rune.htm"

            31997, 31998 -> htmltext = if (check) "low_schuttgart.htm" else "hg_schuttgart.htm"
        }
        return htmltext
    }

    companion object {
        private val PRIESTS = intArrayOf(
            31078,
            31079,
            31080,
            31081,
            31082,
            31083,
            31084,
            31085,
            31086,
            31087,
            31088,
            31089,
            31090,
            31091,
            31168,
            31169,
            31692,
            31693,
            31694,
            31695,
            31997,
            31998
        )

        private val DAWN_NPCS = intArrayOf(31078, 31079, 31080, 31081, 31082, 31083, 31084, 31168, 31692, 31694, 31997)
    }
}