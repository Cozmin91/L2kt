package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.data.xml.SpellbookData
import com.l2kt.gameserver.network.serverpackets.AcquireSkillInfo

class RequestAcquireSkillInfo : L2GameClientPacket() {
    private var _skillId: Int = 0
    private var _skillLevel: Int = 0
    private var _skillType: Int = 0

    override fun readImpl() {
        _skillId = readD()
        _skillLevel = readD()
        _skillType = readD()
    }

    override fun runImpl() {
        // Not valid skill data, return.
        if (_skillId <= 0 || _skillLevel <= 0)
            return

        // Incorrect player, return.
        val player = client.activeChar ?: return

        // Incorrect npc, return.
        val folk = player.currentFolk
        if (folk == null || !folk.canInteract(player))
            return

        // Skill doesn't exist, return.
        val skill = SkillTable.getInfo(_skillId, _skillLevel) ?: return

        val asi: AcquireSkillInfo

        when (_skillType) {
            // General skills
            0 -> {
                // Player already has such skill with same or higher level.
                val skillLvl = player.getSkillLevel(_skillId)
                if (skillLvl >= _skillLevel)
                    return

                // Requested skill must be 1 level higher than existing skill.
                if (skillLvl != _skillLevel - 1)
                    return

                if (!folk.template.canTeach(player.classId))
                    return

                // Search if the asked skill exists on player template.
                val gsn = player.template.findSkill(_skillId, _skillLevel)
                if (gsn != null) {
                    asi = AcquireSkillInfo(_skillId, _skillLevel, gsn.correctedCost, 0)
                    val bookId = SpellbookData.getInstance().getBookForSkill(_skillId, _skillLevel)
                    if (bookId != 0)
                        asi.addRequirement(99, bookId, 1, 50)
                    sendPacket(asi)
                }
            }

            // Common skills
            1 -> {
                // Player already has such skill with same or higher level.
                val skillLvl = player.getSkillLevel(_skillId)
                if (skillLvl >= _skillLevel)
                    return

                // Requested skill must be 1 level higher than existing skill.
                if (skillLvl != _skillLevel - 1)
                    return

                val fsn = SkillTreeData.getInstance().getFishingSkillFor(player, _skillId, _skillLevel)
                if (fsn != null) {
                    asi = AcquireSkillInfo(_skillId, _skillLevel, 0, 1)
                    asi.addRequirement(4, fsn.itemId, fsn.itemCount, 0)
                    sendPacket(asi)
                }
            }

            // Pledge skills.
            2 -> {
                if (!player.isClanLeader)
                    return

                val csn = SkillTreeData.getInstance().getClanSkillFor(player, _skillId, _skillLevel)
                if (csn != null) {
                    asi = AcquireSkillInfo(skill.id, skill.level, csn.cost, 2)
                    if (Config.LIFE_CRYSTAL_NEEDED && csn.itemId != 0)
                        asi.addRequirement(1, csn.itemId, 1, 0)
                    sendPacket(asi)
                }
            }
        }
    }
}