package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Speaking NPCs implementation.<br></br>
 * <br></br>
 * This AI leads the behavior of any speaking NPC.<br></br>
 * It sends back the good string following the action and the npcId.<br></br>
 * <br></br>
 * <font color="red">**<u>TODO:</u>** Replace the system of switch by an XML, once a decent amount of NPCs is mapped.</font>
 */
class SpeakingNPCs : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addEventIds(NPC_IDS, EventType.ON_ATTACK, EventType.ON_KILL)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc.isScriptValue(1))
            return super.onAttack(npc, attacker, damage, skill)

        var message = ""

        when (npc.npcId) {
            27219, 27220, 27221, 27222, 27223, 27224, 27225, 27226, 27227, 27228, 27229, 27230, 27231, 27232, 27233, 27234, 27235, 27236, 27237, 27238, 27239, 27240, 27241, 27242, 27243, 27244, 27245, 27246, 27247, 27249 -> message =
                    "You dare to disturb the order of the shrine! Die!"
            27016 -> message = "...How dare you challenge me!"
            27021 -> message = "I will taste your blood!"
            27022 -> message = "I shall put you in a never-ending nightmare!"
        }

        npc.broadcastNpcSay(message)
        npc.scriptValue = 1 // Make the mob speaks only once, else he will spam.

        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        var message = ""

        when (npc.npcId) {
            27219, 27220, 27221, 27222, 27223, 27224, 27225, 27226, 27227, 27228, 27229, 27230, 27231, 27232, 27233, 27234, 27235, 27236, 27237, 27238, 27239, 27240, 27241, 27242, 27243, 27244, 27245, 27246, 27247, 27249 -> message =
                    "My spirit is releasing from this shell. I'm getting close to Halisha..."

            27016 -> message = "May Beleth's power be spread on the whole world...!"

            27021 -> message = "I have fulfilled my contract with Trader Creamees."

            27022 -> message = "My soul belongs to Icarus..."
        }

        npc.broadcastNpcSay(message)

        return super.onKill(npc, killer)
    }

    companion object {
        private val NPC_IDS = intArrayOf(
            27016, // Nerkas
            27021, // Kirunak
            27022, // Merkenis

            27219, //
            27220, //
            27221, //
            27222, //
            27223, //
            27224, //
            27225, //
            27226, //
            27227, //
            27228, //
            27229, //
            27230, //
            27231, //
            27232, // Archon of Halisha
            27233, //
            27234, //
            27235, //
            27236, //
            27237, //
            27238, //
            27239, //
            27240, //
            27241, //
            27242, //
            27243, //
            27244, //
            27245, //
            27246, //
            27247, //
            27249
        )
    }
}