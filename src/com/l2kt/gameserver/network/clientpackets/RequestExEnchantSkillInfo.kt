package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.network.serverpackets.ExEnchantSkillInfo

class RequestExEnchantSkillInfo : L2GameClientPacket() {
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

        SkillTable.getInfo(_skillId, _skillLevel) ?: return

        if (!folk.template.canTeach(player.classId))
            return

        val esn = SkillTreeData.getInstance().getEnchantSkillFor(player, _skillId, _skillLevel) ?: return

        val esi = ExEnchantSkillInfo(_skillId, _skillLevel, esn.sp, esn.exp, esn.getEnchantRate(player.level))
        if (Config.ES_SP_BOOK_NEEDED && esn.item != null)
            esi.addRequirement(4, esn.item.id, esn.item.value, 0)

        sendPacket(esi)
    }
}