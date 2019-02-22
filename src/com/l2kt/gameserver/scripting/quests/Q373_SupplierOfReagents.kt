package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q373_SupplierOfReagents : Quest(373, "Supplier of Reagents") {
    init {
        DROPLIST[PLATINUM_GUARDIAN_SHAMAN] = intArrayOf(REAGENT_BOX, 442000, 0)
        DROPLIST[HAMES_ORC_SHAMAN] = intArrayOf(REAGENT_POUCH_3, 470000, 0)
        DROPLIST[PLATINUM_TRIBE_SHAMAN] = intArrayOf(REAGENT_POUCH_2, QUICKSILVER, 680, 1000)
        DROPLIST[HALLATE_MAID] = intArrayOf(REAGENT_POUCH_1, VOLCANIC_ASH, 664, 844)
        DROPLIST[HALLATE_GUARDIAN] = intArrayOf(DEMONS_BLOOD, MOONSTONE_SHARD, 729, 833)
        DROPLIST[CRENDION] = intArrayOf(ROTTEN_BONE, QUICKSILVER, 618, 1000)
        DROPLIST[LAVA_WYRM] = intArrayOf(WYRMS_BLOOD, LAVA_STONE, 505, 750)
    }

    init {

        setItemsIds(MIXING_STONE, MIXING_MANUAL)

        addStartNpc(WESLEY)
        addTalkId(WESLEY, URN)

        addKillId(
            CRENDION,
            HALLATE_MAID,
            HALLATE_GUARDIAN,
            PLATINUM_TRIBE_SHAMAN,
            PLATINUM_GUARDIAN_SHAMAN,
            LAVA_WYRM,
            HAMES_ORC_SHAMAN
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Wesley
        if (event.equals("30166-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)

            st.giveItems(MIXING_STONE, 1)
            st.giveItems(MIXING_MANUAL, 1)
        } else if (event.equals("30166-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("31149-02.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(MIXING_STONE))
                htmltext = "31149-04.htm"
        } else if (event.startsWith("31149-03-")) {
            val regentId = Integer.parseInt(event.substring(9, 13))
            for (formula in FORMULAS) {
                if (formula[1] != regentId)
                    continue

                // Not enough items, cancel the operation.
                if (st.getQuestItemsCount(regentId) < formula[0])
                    break

                st[_ingredient] = Integer.toString(regentId)
                return htmltext
            }
            htmltext = "31149-04.htm"
        } else if (event.startsWith("31149-06-")) {
            val catalyst = Integer.parseInt(event.substring(9, 13))

            // Not enough items, cancel the operation.
            if (!st.hasQuestItems(catalyst))
                return "31149-04.htm"

            st[_catalyst] = Integer.toString(catalyst)
        } else if (event.startsWith("31149-12-")) {
            val regent = st.getInt(_ingredient)
            val catalyst = st.getInt(_catalyst)

            for (formula in FORMULAS) {
                if (formula[1] != regent || formula[2] != catalyst)
                    continue

                // Not enough regents.
                if (st.getQuestItemsCount(regent) < formula[0])
                    break

                // Not enough catalysts.
                if (!st.hasQuestItems(catalyst))
                    break

                st.takeItems(regent, formula[0])
                st.takeItems(catalyst, 1)

                val tempIndex = Integer.parseInt(event.substring(9, 10))
                for (temperature in TEMPERATURES) {
                    if (temperature[0] != tempIndex)
                        continue

                    if (Rnd[100] < temperature[1]) {
                        st.giveItems(formula[3], temperature[2])
                        return "31149-12-" + formula[3] + ".htm"
                    }
                    return "31149-11.htm"
                }
            }
            htmltext = "31149-13.htm"
        }// Urn
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 57) "30166-01.htm" else "30166-02.htm"

            Quest.STATE_STARTED -> if (npc.npcId == WESLEY)
                htmltext = "30166-05.htm"
            else
                htmltext = "31149-01.htm"
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val drop = DROPLIST[npc.npcId] ?: return null

        if (drop[2] == 0)
            st.dropItems(drop[0], 1, 0, drop[1])
        else {
            val random = Rnd[1000]
            if (random < drop[3])
                st.dropItemsAlways(if (random < drop[2]) drop[0] else drop[1], 1, 0)
        }

        return null
    }

    companion object {
        private val qn = "Q373_SupplierOfReagents"

        // Variables
        private val _ingredient = "ingredient"
        private val _catalyst = "catalyst"

        // NPCs
        private val WESLEY = 30166
        private val URN = 31149

        // Monsters
        private val CRENDION = 20813
        private val HALLATE_MAID = 20822
        private val HALLATE_GUARDIAN = 21061
        private val PLATINUM_TRIBE_SHAMAN = 20828
        private val PLATINUM_GUARDIAN_SHAMAN = 21066
        private val LAVA_WYRM = 21111
        private val HAMES_ORC_SHAMAN = 21115

        // Quest items
        private val MIXING_STONE = 5904
        private val MIXING_MANUAL = 6317

        // Items - pouches
        private val REAGENT_POUCH_1 = 6007
        private val REAGENT_POUCH_2 = 6008
        private val REAGENT_POUCH_3 = 6009
        private val REAGENT_BOX = 6010
        // Items - ingredients
        private val WYRMS_BLOOD = 6011
        private val LAVA_STONE = 6012
        private val MOONSTONE_SHARD = 6013
        private val ROTTEN_BONE = 6014
        private val DEMONS_BLOOD = 6015
        private val INFERNIUM_ORE = 6016
        // Items - catalysts
        private val BLOOD_ROOT = 6017
        private val VOLCANIC_ASH = 6018
        private val QUICKSILVER = 6019
        private val SULFUR = 6020
        private val DEMONIC_ESSENCE = 6031
        private val MIDNIGHT_OIL = 6030
        // Items - products
        private val DRACOPLASM = 6021
        private val MAGMA_DUST = 6022
        private val MOON_DUST = 6023
        private val NECROPLASM = 6024
        private val DEMONPLASM = 6025
        private val INFERNO_DUST = 6026
        private val FIRE_ESSENCE = 6028
        private val LUNARGENT = 6029
        // Items - products final
        private val DRACONIC_ESSENCE = 6027
        private val ABYSS_OIL = 6032
        private val HELLFIRE_OIL = 6033
        private val NIGHTMARE_OIL = 6034
        private val PURE_SILVER = 6320

        /**
         * This droplist defines the npcId, the item dropped and the luck.
         *
         *  * HAMES_ORC_SHAMAN : 47% chance to drop - reagent pouch (47%)
         *  * HALLATES_MAID : 84,4% chance to drop - reageant pouch (66,4%) and volcanic ash (18%)
         *  * HALLATES_GUARDIAN : 83,3% chance to drop - demon's blood (72,9%) and moonstone shard (10,4%)
         *  * PLATINUM_GUARDIAN_SHAMAN : 44,2% chance to drop - reagent box (44,2%)
         *  * PLATINUM_TRIBE_SHAMAN : 100% chance to drop - reagent pouch (68%) and quichsilver (32%)
         *  * CRENDION : 100% chance to drop - rotten bone piece (61,8%) and quicksilver (38,2%)
         *  * LAVA_WYRM : 75% chance to drop - wyrm's blood (50,5%) and lava stone (24,5%)
         *
         */
        private val DROPLIST = HashMap<Int, IntArray>()

        private val FORMULAS = arrayOf(
            intArrayOf(10, WYRMS_BLOOD, BLOOD_ROOT, DRACOPLASM),
            intArrayOf(10, LAVA_STONE, VOLCANIC_ASH, MAGMA_DUST),
            intArrayOf(10, MOONSTONE_SHARD, VOLCANIC_ASH, MOON_DUST),
            intArrayOf(10, ROTTEN_BONE, BLOOD_ROOT, NECROPLASM),
            intArrayOf(10, DEMONS_BLOOD, BLOOD_ROOT, DEMONPLASM),
            intArrayOf(10, INFERNIUM_ORE, VOLCANIC_ASH, INFERNO_DUST),
            intArrayOf(10, DRACOPLASM, QUICKSILVER, DRACONIC_ESSENCE),
            intArrayOf(10, MAGMA_DUST, SULFUR, FIRE_ESSENCE),
            intArrayOf(10, MOON_DUST, QUICKSILVER, LUNARGENT),
            intArrayOf(10, NECROPLASM, QUICKSILVER, MIDNIGHT_OIL),
            intArrayOf(10, DEMONPLASM, SULFUR, DEMONIC_ESSENCE),
            intArrayOf(10, INFERNO_DUST, SULFUR, ABYSS_OIL),
            intArrayOf(1, FIRE_ESSENCE, DEMONIC_ESSENCE, HELLFIRE_OIL),
            intArrayOf(1, LUNARGENT, MIDNIGHT_OIL, NIGHTMARE_OIL),
            intArrayOf(1, LUNARGENT, QUICKSILVER, PURE_SILVER)
        )

        private val TEMPERATURES = arrayOf(intArrayOf(1, 100, 1), intArrayOf(2, 45, 3), intArrayOf(3, 15, 5))
    }
}