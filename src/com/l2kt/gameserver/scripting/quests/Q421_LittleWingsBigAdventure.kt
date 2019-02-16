package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

/**
 * iCond is an internal variable, used because cond isn't developped on that quest (only 3 states) :
 *
 *  * 1-3 leads initial mimyu behavior ;
 *  * used for leaves support as mask : 4, 8, 16 or 32 = 60 overall ;
 *  * 63 becomes the "marker" to get back to mimyu (60 + 3), meaning you hitted the 4 trees ;
 *  * setted to 100 if mimyu check is ok.
 *
 */
class Q421_LittleWingsBigAdventure : Quest(421, "Little Wing's Big Adventure") {
    init {

        setItemsIds(FAIRY_LEAF)

        addStartNpc(CRONOS)
        addTalkId(CRONOS, MIMYU)

        addAttackId(27185, 27186, 27187, 27188)
        addKillId(27185, 27186, 27187, 27188)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30610-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) == 1) {
                // Find the level of the flute.
                for (i in 3500..3502) {
                    val item = player.inventory!!.getItemByItemId(i)
                    if (item != null && item.enchantLevel >= 55) {
                        st.state = Quest.STATE_STARTED
                        st["cond"] = "1"
                        st["iCond"] = "1"
                        st["summonOid"] = item.objectId.toString()
                        st.playSound(QuestState.SOUND_ACCEPT)
                        return "30610-05.htm"
                    }
                }
            }

            // Exit quest if you got more than one flute, or the flute level doesn't meat requirements.
            st.exitQuest(true)
        } else if (event.equals("30747-02.htm", ignoreCase = true)) {
            val summon = player.pet
            if (summon != null)
                htmltext = if (summon.controlItemId == st.getInt("summonOid")) "30747-04.htm" else "30747-03.htm"
        } else if (event.equals("30747-05.htm", ignoreCase = true)) {
            val summon = player.pet
            if (summon == null || summon.controlItemId != st.getInt("summonOid"))
                htmltext = "30747-06.htm"
            else {
                st["cond"] = "2"
                st["iCond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(FAIRY_LEAF, 4)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED ->
                // Wrong level.
                if (player.level < 45)
                    htmltext = "30610-01.htm"
                else if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) != 1)
                    htmltext = "30610-02.htm"
                else {
                    // Find the level of the hatchling.
                    for (i in 3500..3502) {
                        val item = player.inventory!!.getItemByItemId(i)
                        if (item != null && item.enchantLevel >= 55)
                            return "30610-04.htm"
                    }

                    // Invalid level.
                    htmltext = "30610-03.htm"
                }// Got more than one flute, or none.

            Quest.STATE_STARTED -> when (npc.npcId) {
                CRONOS -> htmltext = "30610-07.htm"

                MIMYU -> {
                    val id = st.getInt("iCond")
                    if (id == 1) {
                        htmltext = "30747-01.htm"
                        st["iCond"] = "2"
                    } else if (id == 2) {
                        val summon = player.pet
                        htmltext =
                                if (summon != null) if (summon.controlItemId == st.getInt("summonOid")) "30747-04.htm" else "30747-03.htm" else "30747-02.htm"
                    } else if (id == 3)
                    // Explanation is done, leaves are already given.
                        htmltext = "30747-07.htm"
                    else if (id > 3 && id < 63)
                    // Did at least one tree, but didn't manage to make them all.
                        htmltext = "30747-11.htm"
                    else if (id == 63)
                    // Did all trees, no more leaves.
                    {
                        val summon = player.pet ?: return "30747-12.htm"

                        if (summon.controlItemId != st.getInt("summonOid"))
                            return "30747-14.htm"

                        htmltext = "30747-13.htm"
                        st["iCond"] = "100"
                    } else if (id == 100)
                    // Spoke with the Fairy.
                    {
                        val summon = player.pet
                        if (summon != null && summon.controlItemId == st.getInt("summonOid"))
                            return "30747-15.htm"

                        if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) > 1)
                            return "30747-17.htm"

                        for (i in 3500..3502) {
                            val item = player.inventory!!.getItemByItemId(i)
                            if (item != null && item.objectId == st.getInt("summonOid")) {
                                st.takeItems(i, 1)
                                st.giveItems(
                                    i + 922,
                                    1,
                                    item.enchantLevel
                                ) // TODO rebuild entirely pet system in order enchant is given a fuck. Supposed to give an item lvl XX for a flute level XX.
                                st.playSound(QuestState.SOUND_FINISH)
                                st.exitQuest(true)
                                return "30747-16.htm"
                            }
                        }

                        // Curse if the registered objectId is the wrong one (switch flutes).
                        htmltext = "30747-18.htm"

                        val skill = SkillTable.getInfo(4167, 1)
                        if (skill != null && player.getFirstEffect(skill) == null)
                            skill.getEffects(npc, player)
                    }
                }
            }
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        // Minions scream no matter current quest state.
        if ((npc as Monster).hasMinions()) {
            for (ghost in npc.minionList.spawnedMinions) {
                if (!ghost.isDead && Rnd[100] < 1)
                    ghost.broadcastNpcSay("We must protect the fairy tree!")
            }
        }

        if (attacker is Pet) {
            val player = attacker.actingPlayer

            // Condition required : 2.
            val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

            // A pet was the attacker, and the objectId is the good one - random luck is reached and you still have some leaves ; go further.
            if (attacker.controlItemId == st.getInt("summonOid") && Rnd[100] < 1 && st.hasQuestItems(FAIRY_LEAF)) {
                val idMask = Math.pow(2.0, (npc.getNpcId() - 27182 - 1).toDouble()).toInt()
                val iCond = st.getInt("iCond")

                if (iCond or idMask != iCond) {
                    st["iCond"] = (iCond or idMask).toString()

                    npc.broadcastNpcSay("Give me a Fairy Leaf...!")
                    st.takeItems(FAIRY_LEAF, 1)
                    npc.broadcastNpcSay("Leave now, before you incur the wrath of the guardian ghost...")

                    // Four leafs have been used ; update quest state.
                    if (st.getInt("iCond") == 63) {
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                }
            }
        }
        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        // Tree curses the killer.
        if (Rnd[100] < 30) {
            val skill = SkillTable.getInfo(4243, 1)
            if (skill != null && killer.getFirstEffect(skill) == null)
                skill.getEffects(npc, killer)
        }

        // Spawn 20 ghosts, attacking the killer.
        for (i in 0..19) {
            val newNpc = addSpawn(27189, npc, true, 300000, false) as Attackable?

            newNpc!!.setRunning()
            newNpc.addDamageHate(killer, 0, 999)
            newNpc.ai.setIntention(CtrlIntention.ATTACK, killer)
        }

        return null
    }

    companion object {
        private val qn = "Q421_LittleWingsBigAdventure"

        // NPCs
        private val CRONOS = 30610
        private val MIMYU = 30747

        // Item
        private val FAIRY_LEAF = 4325
    }
}