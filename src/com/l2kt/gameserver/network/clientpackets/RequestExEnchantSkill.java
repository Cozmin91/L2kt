package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.data.xml.SkillTreeData;
import com.l2kt.gameserver.model.L2ShortCut;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.instance.Folk;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.skillnode.EnchantSkillNode;
import com.l2kt.gameserver.network.SystemMessageId;

import com.l2kt.gameserver.network.serverpackets.ShortCutRegister;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;
import com.l2kt.gameserver.network.serverpackets.UserInfo;

public final class RequestExEnchantSkill extends L2GameClientPacket
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
		
		final EnchantSkillNode esn = SkillTreeData.getInstance().getEnchantSkillFor(player, _skillId, _skillLevel);
		if (esn == null)
			return;
		
		// Check exp and sp neccessary to enchant skill.
		if (player.getSp() < esn.getSp())
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		if (player.getExp() - esn.getExp() < player.getStat().getExpForLevel(76))
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		// Check item restriction, and try to consume item.
		if (Config.ES_SP_BOOK_NEEDED && esn.getItem() != null && !player.destroyItemByItemId("SkillEnchant", esn.getItem().getId(), esn.getItem().getValue(), folk, true))
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		// All conditions fulfilled, consume exp and sp.
		player.removeExpAndSp(esn.getExp(), esn.getSp());
		
		// The skill level used for shortcuts.
		int skillLevel = _skillLevel;
		
		// Try to enchant skill.
		if (Rnd.INSTANCE.get(100) <= esn.getEnchantRate(player.getLevel()))
		{
			player.addSkill(skill, true);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1).addSkillName(_skillId, _skillLevel));
		}
		else
		{
			skillLevel = SkillTable.INSTANCE.getMaxLevel(_skillId);
			
			player.addSkill(SkillTable.INSTANCE.getInfo(_skillId, skillLevel), true);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1).addSkillName(_skillId, _skillLevel));
		}
		
		player.sendSkillList();
		player.sendPacket(new UserInfo(player));
		
		// Update shortcuts.
		for (L2ShortCut sc : player.getAllShortCuts())
		{
			if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut shortcut = new L2ShortCut(sc.getSlot(), sc.getPage(), L2ShortCut.TYPE_SKILL, _skillId, skillLevel, 1);
				player.sendPacket(new ShortCutRegister(shortcut));
				player.registerShortCut(shortcut);
			}
		}
		
		// Show enchant skill list.
		folk.showEnchantSkillList(player);
	}
}