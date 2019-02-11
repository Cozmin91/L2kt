package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.data.xml.SkillTreeData;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.instance.Folk;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.skillnode.EnchantSkillNode;
import com.l2kt.gameserver.network.serverpackets.ExEnchantSkillInfo;

public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLevel;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLevel = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (_skillId <= 0 || _skillLevel <= 0)
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.getClassId().level() < 3 || player.getLevel() < 76)
			return;
		
		final Folk folk = player.getCurrentFolk();
		if (folk == null || !folk.canInteract(player))
			return;
		
		if (player.getSkillLevel(_skillId) >= _skillLevel)
			return;
		
		final L2Skill skill = SkillTable.INSTANCE.getInfo(_skillId, _skillLevel);
		if (skill == null)
			return;
		
		if (!folk.getTemplate().canTeach(player.getClassId()))
			return;
		
		final EnchantSkillNode esn = SkillTreeData.getInstance().getEnchantSkillFor(player, _skillId, _skillLevel);
		if (esn == null)
			return;
		
		final ExEnchantSkillInfo esi = new ExEnchantSkillInfo(_skillId, _skillLevel, esn.getSp(), esn.getExp(), esn.getEnchantRate(player.getLevel()));
		if (Config.ES_SP_BOOK_NEEDED && esn.getItem() != null)
			esi.addRequirement(4, esn.getItem().getId(), esn.getItem().getValue(), 0);
		
		sendPacket(esi);
	}
}