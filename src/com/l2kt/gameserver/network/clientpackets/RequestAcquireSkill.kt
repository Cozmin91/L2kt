package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.data.xml.SpellbookData
import com.l2kt.gameserver.model.L2ShortCut
import com.l2kt.gameserver.model.actor.instance.Fisherman
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.VillageMaster
import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExStorageMaxCount
import com.l2kt.gameserver.network.serverpackets.ShortCutRegister
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestAcquireSkill : L2GameClientPacket() {
    private var _skillId: Int = 0
    private var _skillLevel: Int = 0
    private var _skillType: Int = 0

    override fun readImpl() {
        _skillId = readD()
        _skillLevel = readD()
        _skillType = readD()
    }

    override fun runImpl() {
        if (_skillId <= 0 || _skillLevel <= 0)
            return

        val player = client.activeChar ?: return

        val folk = player.currentFolk
        if (folk == null || !folk.canInteract(player))
            return

        val skill = SkillTable.getInfo(_skillId, _skillLevel) ?: return

        when (_skillType) {
            // General skills.
            0 -> {
                // Player already has such skill with same or higher level.
                val skillLvl = player.getSkillLevel(_skillId)
                if (skillLvl >= _skillLevel)
                    return

                // Requested skill must be 1 level higher than existing skill.
                if (skillLvl != _skillLevel - 1)
                    return

                // Search if the asked skill exists on player template.
                val gsn = (player.template as PlayerTemplate).findSkill(_skillId, _skillLevel) ?: return

                // Not enought SP.
                if (player.sp < gsn.correctedCost) {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL)
                    folk.showSkillList(player)
                    return
                }

                // Get spellbook and try to consume it.
                val bookId = SpellbookData.getBookForSkill(_skillId, _skillLevel)
                if (bookId > 0 && !player.destroyItemByItemId("SkillLearn", bookId, 1, folk, true)) {
                    player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL)
                    folk.showSkillList(player)
                    return
                }

                // Consume SP.
                player.removeExpAndSp(0, gsn.correctedCost)

                // Add skill new skill.
                player.addSkill(skill, true)
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill))

                // Update player and return.
                updateShortCuts(player)
                player.sendSkillList()
                folk.showSkillList(player)
            }

            // Common skills.
            1 -> {
                // Player already has such skill with same or higher level.
                val skillLvl = player.getSkillLevel(_skillId)
                if (skillLvl >= _skillLevel)
                    return

                // Requested skill must be 1 level higher than existing skill.
                if (skillLvl != _skillLevel - 1)
                    return

                val fsn = SkillTreeData.getFishingSkillFor(player, _skillId, _skillLevel) ?: return

                if (!player.destroyItemByItemId("Consume", fsn.itemId, fsn.itemCount, folk, true)) {
                    player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL)
                    Fisherman.showFishSkillList(player)
                    return
                }

                player.addSkill(skill, true)
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill))

                if (_skillId >= 1368 && _skillId <= 1372)
                    player.sendPacket(ExStorageMaxCount(player))

                updateShortCuts(player)
                player.sendSkillList()
                Fisherman.showFishSkillList(player)
            }

            // Pledge skills.
            2 -> {
                if (!player.isClanLeader)
                    return

                val csn = SkillTreeData.getClanSkillFor(player, _skillId, _skillLevel) ?: return

                if (player.clan.reputationScore < csn.cost) {
                    player.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE)
                    VillageMaster.showPledgeSkillList(player)
                    return
                }

                if (Config.LIFE_CRYSTAL_NEEDED && !player.destroyItemByItemId("Consume", csn.itemId, 1, folk, true)) {
                    player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL)
                    VillageMaster.showPledgeSkillList(player)
                    return
                }

                player.clan.takeReputationScore(csn.cost)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(
                        csn.cost
                    )
                )

                player.clan.addNewSkill(skill)

                VillageMaster.showPledgeSkillList(player)
                return
            }
        }
    }

    private fun updateShortCuts(player: Player) {
        if (_skillLevel > 1) {
            for (sc in player.allShortCuts) {
                if (sc.id == _skillId && sc.type == L2ShortCut.TYPE_SKILL) {
                    val newsc = L2ShortCut(sc.slot, sc.page, L2ShortCut.TYPE_SKILL, _skillId, _skillLevel, 1)
                    player.sendPacket(ShortCutRegister(newsc))
                    player.registerShortCut(newsc)
                }
            }
        }
    }
}