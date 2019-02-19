package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.data.xml.SoulCrystalData
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.soulcrystal.LevelingInfo
import com.l2kt.gameserver.model.soulcrystal.SoulCrystal
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q350_EnhanceYourWeapon : Quest(350, "Enhance Your Weapon") {
    init {

        addStartNpc(30115, 30194, 30856)
        addTalkId(30115, 30194, 30856)

        for (npcId in SoulCrystalData.levelingInfos.keys)
            addKillId(npcId)

        for (crystalId in SoulCrystalData.soulCrystals.keys)
            addItemUse(crystalId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        // Start the quest.
        if (event.endsWith("-04.htm")) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.endsWith("-09.htm")) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(4629, 1)
        } else if (event.endsWith("-10.htm")) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(4640, 1)
        } else if (event.endsWith("-11.htm")) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(4651, 1)
        } else if (event.endsWith("-exit.htm"))
            st.exitQuest(true)// Terminate the quest.
        // Give Blue Soul Crystal.
        // Give Green Soul Crystal.
        // Give Red Soul Crystal.

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 40)
                htmltext = npc.npcId.toString() + "-lvl.htm"
            else
                htmltext = npc.npcId.toString() + "-01.htm"

            Quest.STATE_STARTED -> {
                // Check inventory for soul crystals.
                for (item in player.inventory!!.items) {
                    // Crystal found, show "how to" html.
                    if (SoulCrystalData.soulCrystals[item.itemId] != null)
                        return npc.npcId.toString() + "-03.htm"
                }
                // No crystal found, offer a new crystal.
                htmltext = npc.npcId.toString() + "-21.htm"
            }
        }

        return htmltext
    }

    override fun onItemUse(item: ItemInstance, user: Player, target: WorldObject?): String? {
        // Caster is dead.
        if (user.isDead())
            return null

        // No target, or target isn't an L2Attackable.
        if (target == null || target !is Attackable)
            return null

// Mob is dead or not registered in _npcInfos.
        if (target.isDead() || !SoulCrystalData.levelingInfos.containsKey(target.npcId))
            return null

        // Add user to mob's absorber list.
        target.addAbsorber(user, item)

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer ?: return null

        // Retrieve individual mob informations.
        val npcInfo = SoulCrystalData.levelingInfos[npc.npcId] ?: return null

        val chance = Rnd[1000]

        // Handle npc leveling info type.
        when (npcInfo.absorbCrystalType) {
            LevelingInfo.AbsorbCrystalType.FULL_PARTY -> {
                val mob = npc as Attackable

                for (st in getPartyMembersState(player, npc, Quest.STATE_STARTED))
                    tryToStageCrystal(st.player, mob, npcInfo, chance)
            }

            LevelingInfo.AbsorbCrystalType.PARTY_ONE_RANDOM -> {
                val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED)
                if (st != null)
                    tryToStageCrystal(st.player, npc as Attackable, npcInfo, chance)
            }

            LevelingInfo.AbsorbCrystalType.LAST_HIT -> if (checkPlayerState(player, npc, Quest.STATE_STARTED) != null)
                tryToStageCrystal(player, npc as Attackable, npcInfo, chance)
        }

        return null
    }

    companion object {
        private val qn = "Q350_EnhanceYourWeapon"

        /**
         * Define the Soul Crystal and try to stage it. Checks for quest enabled, crystal(s) in inventory, required usage of crystal, mob's ability to level crystal and mob vs player level gap.
         * @param player : The player to make checks on.
         * @param mob : The mob to make checks on.
         * @param npcInfo : The mob's leveling informations.
         * @param chance : Input variable used to determine keep/stage/break of the crystal.
         */
        private fun tryToStageCrystal(player: Player, mob: Attackable, npcInfo: LevelingInfo, chance: Int) {
            var crystalData: SoulCrystal? = null
            var crystalItem: ItemInstance? = null

            // Iterate through player's inventory to find crystal(s).
            for (item in player.inventory!!.items) {
                val data = SoulCrystalData.soulCrystals[item.itemId] ?: continue

                // More crystals found.
                if (crystalData != null) {
                    // Leveling requires soul crystal being used?
                    if (npcInfo.isSkillRequired) {
                        // Absorb list contains killer and his AbsorbInfo is registered.
                        val ai = mob.getAbsorbInfo(player.objectId)
                        if (ai != null && ai.isRegistered)
                            player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION)
                    } else
                        player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION)

                    return
                }

                crystalData = data
                crystalItem = item
            }

            // No crystal found, return without any notification.
            if (crystalData == null || crystalItem == null)
                return

            // Leveling requires soul crystal being used?
            if (npcInfo.isSkillRequired) {
                // Absorb list doesn't contain killer or his AbsorbInfo is not registered.
                val ai = mob.getAbsorbInfo(player.objectId)
                if (ai == null || !ai.isRegistered)
                    return

                // Check if Absorb list contains valid crystal and whether it was used properly.
                if (!ai.isValid(crystalItem.objectId)) {
                    player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED)
                    return
                }
            }

            // Check, if npc stages this type of crystal.
            if (!ArraysUtil.contains(npcInfo.levelList, crystalData.level)) {
                player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED)
                return
            }

            // Check level difference limitation, dark blue monsters does not stage.
            if (player.level - mob.level > 8) {
                player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED)
                return
            }

            // Lucky, crystal successfully stages.
            if (chance < npcInfo.chanceStage)
                exchangeCrystal(player, crystalData, true)
            else if (chance < npcInfo.chanceStage + npcInfo.chanceBreak)
                exchangeCrystal(player, crystalData, false)
            else
                player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED)// Bad luck, crystal doesn't stage.
            // Bad luck, crystal accidentally breaks.
        }

        /**
         * Remove the old crystal and add new one if stage, broken crystal if break. Send messages in both cases.
         * @param player : The player to check on (inventory and send messages).
         * @param sc : SoulCrystal of to take information form.
         * @param stage : Switch to determine success or fail.
         */
        private fun exchangeCrystal(player: Player, sc: SoulCrystal, stage: Boolean) {
            val st = player.getQuestState(qn)

            st!!.takeItems(sc.initialItemId, 1)
            if (stage) {
                player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED)
                st.giveItems(sc.stagedItemId, 1)
                st.playSound(QuestState.SOUND_ITEMGET)
            } else {
                val broken = sc.brokenItemId
                if (broken != 0) {
                    player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE)
                    st.giveItems(broken, 1)
                }
            }
        }
    }
}