package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.model.L2ShortCut
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ShortCutRegister
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.network.serverpackets.UserInfo

class RequestExEnchantSkill : L2GameClientPacket() {
    private var _skillId: Int = 0
    private var _skillLevel: Int = 0

    override fun readImpl() {
        _skillId = readD()
        _skillLevel = readD()
    }

    override fun runImpl() {
        if (_skillId <= 0 || _skillLevel <= 0)
            return

        val player = client.activeChar ?: return

        if (player.classId.level() < 3 || player.level < 76)
            return

        val folk = player.currentFolk
        if (folk == null || !folk.canInteract(player))
            return

        if (player.getSkillLevel(_skillId) >= _skillLevel)
            return

        val skill = SkillTable.getInfo(_skillId, _skillLevel) ?: return

        val esn = SkillTreeData.getInstance().getEnchantSkillFor(player, _skillId, _skillLevel) ?: return

        // Check exp and sp neccessary to enchant skill.
        if (player.sp < esn.sp) {
            player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL)
            return
        }

        if (player.exp - esn.exp < player.stat.getExpForLevel(76)) {
            player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL)
            return
        }

        // Check item restriction, and try to consume item.
        if (Config.ES_SP_BOOK_NEEDED && esn.item != null && !player.destroyItemByItemId(
                "SkillEnchant",
                esn.item.id,
                esn.item.value,
                folk,
                true
            )
        ) {
            player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL)
            return
        }

        // All conditions fulfilled, consume exp and sp.
        player.removeExpAndSp(esn.exp.toLong(), esn.sp)

        // The skill level used for shortcuts.
        var skillLevel = _skillLevel

        // Try to enchant skill.
        if (Rnd[100] <= esn.getEnchantRate(player.level)) {
            player.addSkill(skill, true)
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1).addSkillName(
                    _skillId,
                    _skillLevel
                )
            )
        } else {
            skillLevel = SkillTable.getMaxLevel(_skillId)

            player.addSkill(SkillTable.getInfo(_skillId, skillLevel), true)
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1).addSkillName(
                    _skillId,
                    _skillLevel
                )
            )
        }

        player.sendSkillList()
        player.sendPacket(UserInfo(player))

        // Update shortcuts.
        for (sc in player.allShortCuts) {
            if (sc.id == _skillId && sc.type == L2ShortCut.TYPE_SKILL) {
                val shortcut = L2ShortCut(sc.slot, sc.page, L2ShortCut.TYPE_SKILL, _skillId, skillLevel, 1)
                player.sendPacket(ShortCutRegister(shortcut))
                player.registerShortCut(shortcut)
            }
        }

        // Show enchant skill list.
        folk.showEnchantSkillList(player)
    }
}