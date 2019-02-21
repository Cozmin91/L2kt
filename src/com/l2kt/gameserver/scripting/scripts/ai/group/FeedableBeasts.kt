package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.TamedBeast
import com.l2kt.gameserver.network.serverpackets.NpcSay
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.quests.Q020_BringUpWithLove
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FeedableBeasts : L2AttackableAIScript("ai/group") {
    init {
        MAD_COW_POLYMORPH[21824] = 21468
        MAD_COW_POLYMORPH[21825] = 21469
        MAD_COW_POLYMORPH[21826] = 21487
        MAD_COW_POLYMORPH[21827] = 21488
        MAD_COW_POLYMORPH[21828] = 21506
        MAD_COW_POLYMORPH[21829] = 21507
    }

    private class GrowthCapableMob(private val _growthLevel: Int, private val _chance: Int) {

        private val _spiceToMob = HashMap<Int, Array<IntArray>>()

        val chance: Int?
            get() = _chance

        val growthLevel: Int?
            get() = _growthLevel

        fun addMobs(spice: Int, Mobs: Array<IntArray>) {
            _spiceToMob[spice] = Mobs
        }

        fun getMob(spice: Int, mobType: Int, classType: Int): Int? {
            return if (_spiceToMob.containsKey(spice)) _spiceToMob[spice]!![mobType][classType] else null

        }

        fun getRandomMob(spice: Int): Int? {
            val temp: Array<IntArray> = _spiceToMob[spice]!!
            val rand = Rnd[temp[0].size]
            return temp[0][rand]
        }
    }

    init {

        var temp: GrowthCapableMob

        val Kookabura_0_Gold = arrayOf(intArrayOf(21452, 21453, 21454, 21455))
        val Kookabura_0_Crystal = arrayOf(intArrayOf(21456, 21457, 21458, 21459))
        val Kookabura_1_Gold_1 = arrayOf(intArrayOf(21460, 21462))
        val Kookabura_1_Gold_2 = arrayOf(intArrayOf(21461, 21463))
        val Kookabura_1_Crystal_1 = arrayOf(intArrayOf(21464, 21466))
        val Kookabura_1_Crystal_2 = arrayOf(intArrayOf(21465, 21467))
        val Kookabura_2_1 = arrayOf(intArrayOf(21468, 21824), intArrayOf(16017, 16018))
        val Kookabura_2_2 = arrayOf(intArrayOf(21469, 21825), intArrayOf(16017, 16018))

        val Buffalo_0_Gold = arrayOf(intArrayOf(21471, 21472, 21473, 21474))
        val Buffalo_0_Crystal = arrayOf(intArrayOf(21475, 21476, 21477, 21478))
        val Buffalo_1_Gold_1 = arrayOf(intArrayOf(21479, 21481))
        val Buffalo_1_Gold_2 = arrayOf(intArrayOf(21481, 21482))
        val Buffalo_1_Crystal_1 = arrayOf(intArrayOf(21483, 21485))
        val Buffalo_1_Crystal_2 = arrayOf(intArrayOf(21484, 21486))
        val Buffalo_2_1 = arrayOf(intArrayOf(21487, 21826), intArrayOf(16013, 16014))
        val Buffalo_2_2 = arrayOf(intArrayOf(21488, 21827), intArrayOf(16013, 16014))

        val Cougar_0_Gold = arrayOf(intArrayOf(21490, 21491, 21492, 21493))
        val Cougar_0_Crystal = arrayOf(intArrayOf(21494, 21495, 21496, 21497))
        val Cougar_1_Gold_1 = arrayOf(intArrayOf(21498, 21500))
        val Cougar_1_Gold_2 = arrayOf(intArrayOf(21499, 21501))
        val Cougar_1_Crystal_1 = arrayOf(intArrayOf(21502, 21504))
        val Cougar_1_Crystal_2 = arrayOf(intArrayOf(21503, 21505))
        val Cougar_2_1 = arrayOf(intArrayOf(21506, 21828), intArrayOf(16015, 16016))
        val Cougar_2_2 = arrayOf(intArrayOf(21507, 21829), intArrayOf(16015, 16016))

        // Alpen Kookabura
        temp = GrowthCapableMob(0, 100)
        temp.addMobs(GOLDEN_SPICE, Kookabura_0_Gold)
        temp.addMobs(CRYSTAL_SPICE, Kookabura_0_Crystal)
        GROWTH_CAPABLE_MOBS[21451] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(GOLDEN_SPICE, Kookabura_1_Gold_1)
        GROWTH_CAPABLE_MOBS[21452] = temp
        GROWTH_CAPABLE_MOBS[21454] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(GOLDEN_SPICE, Kookabura_1_Gold_2)
        GROWTH_CAPABLE_MOBS[21453] = temp
        GROWTH_CAPABLE_MOBS[21455] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(CRYSTAL_SPICE, Kookabura_1_Crystal_1)
        GROWTH_CAPABLE_MOBS[21456] = temp
        GROWTH_CAPABLE_MOBS[21458] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(CRYSTAL_SPICE, Kookabura_1_Crystal_2)
        GROWTH_CAPABLE_MOBS[21457] = temp
        GROWTH_CAPABLE_MOBS[21459] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(GOLDEN_SPICE, Kookabura_2_1)
        GROWTH_CAPABLE_MOBS[21460] = temp
        GROWTH_CAPABLE_MOBS[21462] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(GOLDEN_SPICE, Kookabura_2_2)
        GROWTH_CAPABLE_MOBS[21461] = temp
        GROWTH_CAPABLE_MOBS[21463] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(CRYSTAL_SPICE, Kookabura_2_1)
        GROWTH_CAPABLE_MOBS[21464] = temp
        GROWTH_CAPABLE_MOBS[21466] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(CRYSTAL_SPICE, Kookabura_2_2)
        GROWTH_CAPABLE_MOBS[21465] = temp
        GROWTH_CAPABLE_MOBS[21467] = temp

        // Alpen Buffalo
        temp = GrowthCapableMob(0, 100)
        temp.addMobs(GOLDEN_SPICE, Buffalo_0_Gold)
        temp.addMobs(CRYSTAL_SPICE, Buffalo_0_Crystal)
        GROWTH_CAPABLE_MOBS[21470] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(GOLDEN_SPICE, Buffalo_1_Gold_1)
        GROWTH_CAPABLE_MOBS[21471] = temp
        GROWTH_CAPABLE_MOBS[21473] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(GOLDEN_SPICE, Buffalo_1_Gold_2)
        GROWTH_CAPABLE_MOBS[21472] = temp
        GROWTH_CAPABLE_MOBS[21474] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(CRYSTAL_SPICE, Buffalo_1_Crystal_1)
        GROWTH_CAPABLE_MOBS[21475] = temp
        GROWTH_CAPABLE_MOBS[21477] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(CRYSTAL_SPICE, Buffalo_1_Crystal_2)
        GROWTH_CAPABLE_MOBS[21476] = temp
        GROWTH_CAPABLE_MOBS[21478] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(GOLDEN_SPICE, Buffalo_2_1)
        GROWTH_CAPABLE_MOBS[21479] = temp
        GROWTH_CAPABLE_MOBS[21481] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(GOLDEN_SPICE, Buffalo_2_2)
        GROWTH_CAPABLE_MOBS[21480] = temp
        GROWTH_CAPABLE_MOBS[21482] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(CRYSTAL_SPICE, Buffalo_2_1)
        GROWTH_CAPABLE_MOBS[21483] = temp
        GROWTH_CAPABLE_MOBS[21485] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(CRYSTAL_SPICE, Buffalo_2_2)
        GROWTH_CAPABLE_MOBS[21484] = temp
        GROWTH_CAPABLE_MOBS[21486] = temp

        // Alpen Cougar
        temp = GrowthCapableMob(0, 100)
        temp.addMobs(GOLDEN_SPICE, Cougar_0_Gold)
        temp.addMobs(CRYSTAL_SPICE, Cougar_0_Crystal)
        GROWTH_CAPABLE_MOBS[21489] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(GOLDEN_SPICE, Cougar_1_Gold_1)
        GROWTH_CAPABLE_MOBS[21490] = temp
        GROWTH_CAPABLE_MOBS[21492] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(GOLDEN_SPICE, Cougar_1_Gold_2)
        GROWTH_CAPABLE_MOBS[21491] = temp
        GROWTH_CAPABLE_MOBS[21493] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(CRYSTAL_SPICE, Cougar_1_Crystal_1)
        GROWTH_CAPABLE_MOBS[21494] = temp
        GROWTH_CAPABLE_MOBS[21496] = temp

        temp = GrowthCapableMob(1, 40)
        temp.addMobs(CRYSTAL_SPICE, Cougar_1_Crystal_2)
        GROWTH_CAPABLE_MOBS[21495] = temp
        GROWTH_CAPABLE_MOBS[21497] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(GOLDEN_SPICE, Cougar_2_1)
        GROWTH_CAPABLE_MOBS[21498] = temp
        GROWTH_CAPABLE_MOBS[21500] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(GOLDEN_SPICE, Cougar_2_2)
        GROWTH_CAPABLE_MOBS[21499] = temp
        GROWTH_CAPABLE_MOBS[21501] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(CRYSTAL_SPICE, Cougar_2_1)
        GROWTH_CAPABLE_MOBS[21502] = temp
        GROWTH_CAPABLE_MOBS[21504] = temp

        temp = GrowthCapableMob(2, 25)
        temp.addMobs(CRYSTAL_SPICE, Cougar_2_2)
        GROWTH_CAPABLE_MOBS[21503] = temp
        GROWTH_CAPABLE_MOBS[21505] = temp
    }

    override fun registerNpcs() {
        addEventIds(FEEDABLE_BEASTS, EventType.ON_KILL, EventType.ON_SKILL_SEE)
    }

    fun spawnNext(npc: Npc, growthLevel: Int, player: Player, food: Int) {
        val npcId = npc.npcId
        var nextNpcId: Int

        // Find the next mob to spawn, based on the current npcId, growthlevel, and food.
        if (growthLevel == 2) {
            // If tamed, the mob that will spawn depends on the class type (fighter/mage) of the player!
            if (Rnd[2] == 0) {
                if (player.isMageClass)
                    nextNpcId = GROWTH_CAPABLE_MOBS[npcId]!!.getMob(food, 1, 1)!!
                else
                    nextNpcId = GROWTH_CAPABLE_MOBS[npcId]!!.getMob(food, 1, 0)!!
            } else {
                /*
				 * If not tamed, there is a small chance that have "mad cow" disease. that is a stronger-than-normal animal that attacks its feeder
				 */
                if (Rnd[5] == 0)
                    nextNpcId = GROWTH_CAPABLE_MOBS[npcId]!!.getMob(food, 0, 1)!!
                else
                    nextNpcId = GROWTH_CAPABLE_MOBS[npcId]!!.getMob(food, 0, 0)!!
            }
        } else
            nextNpcId =
                    GROWTH_CAPABLE_MOBS[npcId]!!.getRandomMob(food)!!// All other levels of growth are straight-forward

        // Remove the feedinfo of the mob that got despawned, if any
        if (FEED_INFO.getOrDefault(npc.objectId, 0) == player.objectId)
            FEED_INFO.remove(npc.objectId)

        // Despawn the old mob
        npc.deleteMe()

        // if this is finally a trained mob, then despawn any other trained mobs that the player might have and initialize the Tamed Beast.
        if (ArraysUtil.contains(TAMED_BEASTS, nextNpcId)) {
            if (player.trainedBeast != null)
                player.trainedBeast.deleteMe()

            val template = NpcData.getTemplate(nextNpcId) ?: return
            val nextNpc = TamedBeast(IdFactory.getInstance().nextId, template, player, food, npc.position)
            nextNpc.setRunning()

            // If player has Q020 going, give quest item
            val st = player.getQuestState(Q020_BringUpWithLove.qn)
            if (st != null && Rnd[100] < 5 && !st.hasQuestItems(7185)) {
                st.giveItems(7185, 1)
                st["cond"] = "2"
            }

            // Also, perform a rare random chat
            val rand = Rnd[20]
            if (rand < 5)
                npc.broadcastPacket(
                    NpcSay(
                        nextNpc.objectId,
                        0,
                        nextNpc.npcId,
                        SPAWN_CHATS[rand].replace("\$s1", player.name)
                    )
                )
        } else {
            // If not trained, the newly spawned mob will automatically be aggro against its feeder
            val nextNpc = addSpawn(nextNpcId, npc, false, 0, false) as Attackable

            if (MAD_COW_POLYMORPH.containsKey(nextNpcId))
                startQuestTimer("polymorph Mad Cow", 10000, nextNpc, player, false)

            // Register the player in the feedinfo for the mob that just spawned
            FEED_INFO[nextNpc.objectId] = player.objectId

            attack(nextNpc, player)
        }
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("polymorph Mad Cow", ignoreCase = true) && npc != null && player != null) {
            if (MAD_COW_POLYMORPH.containsKey(npc.npcId)) {
                // remove the feed info from the previous mob
                if ((FEED_INFO).getOrDefault(npc.objectId, 0) == player.objectId)
                    FEED_INFO.remove(npc.objectId)

                // despawn the mad cow
                npc.deleteMe()

                // spawn the new mob
                val nextNpc = addSpawn(MAD_COW_POLYMORPH[npc.npcId]!!, npc, false, 0, false) as Attackable

                // register the player in the feedinfo for the mob that just spawned
                FEED_INFO[nextNpc.objectId] = player.objectId

                attack(nextNpc, player)
            }
        }

        return super.onAdvEvent(event, npc, player)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        if (!targets.contains(npc))
            return super.onSkillSee(npc, caster, skill, targets, isPet)

        // Gather some values on local variables
        val npcId = npc.npcId
        val skillId = skill!!.id

        // Check if the npc and skills used are valid for this script. Exit if invalid.
        if (!ArraysUtil.contains(
                FEEDABLE_BEASTS,
                npcId
            ) || skillId != SKILL_GOLDEN_SPICE && skillId != SKILL_CRYSTAL_SPICE
        )
            return super.onSkillSee(npc, caster, skill, targets, isPet)

        // First gather some values on local variables
        val objectId = npc.objectId
        var growthLevel =
            3 // if a mob is in FEEDABLE_BEASTS but not in GROWTH_CAPABLE_MOBS, then it's at max growth (3)

        if (GROWTH_CAPABLE_MOBS.containsKey(npcId))
            growthLevel = GROWTH_CAPABLE_MOBS[npcId]!!.growthLevel!!

        // Prevent exploit which allows 2 players to simultaneously raise the same 0-growth beast
        // If the mob is at 0th level (when it still listens to all feeders) lock it to the first feeder!
        if (growthLevel == 0 && FEED_INFO.containsKey(objectId))
            return super.onSkillSee(npc, caster, skill, targets, isPet)

        FEED_INFO[objectId] = caster!!.objectId

        var food = 0
        if (skillId == SKILL_GOLDEN_SPICE)
            food = GOLDEN_SPICE
        else if (skillId == SKILL_CRYSTAL_SPICE)
            food = CRYSTAL_SPICE

        // Display the social action of the beast eating the food.
        npc.broadcastPacket(SocialAction(npc, 2))

        // If the pet can grow
        if (GROWTH_CAPABLE_MOBS.containsKey(npcId)) {
            // Do nothing if this mob doesn't eat the specified food (food gets consumed but has no effect).
            if (GROWTH_CAPABLE_MOBS[npcId]!!.getMob(food, 0, 0) == null)
                return super.onSkillSee(npc, caster, skill, targets, isPet)

            // Rare random talk...
            if (Rnd[20] == 0)
                npc.broadcastPacket(NpcSay(objectId, 0, npc.npcId, Rnd[TEXT[growthLevel]]!!))

            if (growthLevel > 0 && FEED_INFO.getOrDefault(
                    objectId,
                    0
                ) != caster.objectId
            ) {
                // check if this is the same player as the one who raised it from growth 0.
                // if no, then do not allow a chance to raise the pet (food gets consumed but has no effect).
                return super.onSkillSee(npc, caster, skill, targets, isPet)
            }

            // Polymorph the mob, with a certain chance, given its current growth level
            if (Rnd[100] < GROWTH_CAPABLE_MOBS[npcId]!!.chance!!)
                spawnNext(npc, growthLevel, caster, food)
        }

        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        // Remove the feedinfo of the mob that got killed, if any
        FEED_INFO.remove(npc.objectId)

        return super.onKill(npc, killer)
    }

    companion object {
        private const val GOLDEN_SPICE = 6643
        private const val CRYSTAL_SPICE = 6644
        private const val SKILL_GOLDEN_SPICE = 2188
        private const val SKILL_CRYSTAL_SPICE = 2189

        private val TAMED_BEASTS = intArrayOf(16013, 16014, 16015, 16016, 16017, 16018)

        private val FEEDABLE_BEASTS = intArrayOf(
            21451,
            21452,
            21453,
            21454,
            21455,
            21456,
            21457,
            21458,
            21459,
            21460,
            21461,
            21462,
            21463,
            21464,
            21465,
            21466,
            21467,
            21468,
            21469, // Alpen Kookaburra
            21470,
            21471,
            21472,
            21473,
            21474,
            21475,
            21476,
            21477,
            21478,
            21479,
            21480,
            21481,
            21482,
            21483,
            21484,
            21485,
            21486,
            21487,
            21488, // Alpen Buffalo
            21489,
            21490,
            21491,
            21492,
            21493,
            21494,
            21495,
            21496,
            21497,
            21498,
            21499,
            21500,
            21501,
            21502,
            21503,
            21504,
            21505,
            21506,
            21507, // Alpen Cougar
            21824,
            21825,
            21826,
            21827,
            21828,
            21829
        )// Alpen Kookaburra, Buffalo, Cougar

        private val MAD_COW_POLYMORPH = HashMap<Int, Int>()

        private val TEXT = arrayOf(
            arrayOf(
                "What did you just do to me?",
                "You want to tame me, huh?",
                "Do not give me this. Perhaps you will be in danger.",
                "Bah bah. What is this unpalatable thing?",
                "My belly has been complaining. This hit the spot.",
                "What is this? Can I eat it?",
                "You don't need to worry about me.",
                "Delicious food, thanks.",
                "I am starting to like you!",
                "Gulp!"
            ),
            arrayOf(
                "I do not think you have given up on the idea of taming me.",
                "That is just food to me. Perhaps I can eat your hand too.",
                "Will eating this make me fat? Ha ha.",
                "Why do you always feed me?",
                "Do not trust me. I may betray you."
            ),
            arrayOf(
                "Destroy!",
                "Look what you have done!",
                "Strange feeling...! Evil intentions grow in my heart...!",
                "It is happening!",
                "This is sad...Good is sad...!"
            )
        )

        private val SPAWN_CHATS = arrayOf(
            "\$s1, will you show me your hideaway?",
            "\$s1, whenever I look at spice, I think about you.",
            "\$s1, you do not need to return to the village. I will give you strength.",
            "Thanks, \$s1. I hope I can help you.",
            "\$s1, what can I do to help you?"
        )

        private val FEED_INFO = ConcurrentHashMap<Int, Int>()
        private val GROWTH_CAPABLE_MOBS = HashMap<Int, GrowthCapableMob>()
    }
}