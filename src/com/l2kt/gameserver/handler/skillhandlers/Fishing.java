package com.l2kt.gameserver.handler.skillhandlers;

import com.l2kt.commons.math.MathUtil;
import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.data.manager.ZoneManager;
import com.l2kt.gameserver.handler.ISkillHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Creature;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.type.WeaponType;
import com.l2kt.gameserver.model.itemcontainer.Inventory;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.model.zone.ZoneId;
import com.l2kt.gameserver.model.zone.type.FishingZone;

import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.templates.skills.L2SkillType;

public class Fishing implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.FISHING
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		Player player = (Player) activeChar;
		
		// Cancels fishing
		if (player.isFishing())
		{
			player.getFishingStance().end(false);
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return;
		}
		
		// Fishing pole isn't equipped.
		if (player.getAttackType() != WeaponType.FISHINGROD)
		{
			player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
			return;
		}
		
		// You can't fish while you are on boat
		if (player.isInBoat())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
			return;
		}
		
		if (player.isCrafting() || player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
			return;
		}
		
		// You can't fish in water
		if (player.isInsideZone(ZoneId.WATER))
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
			return;
		}
		
		// Check equipped baits.
		final ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			return;
		}
		
		final int rnd = Rnd.INSTANCE.get(50) + 250;
		final double radian = Math.toRadians(MathUtil.INSTANCE.convertHeadingToDegree(player.getHeading()));
		
		final int x = player.getX() + (int) (Math.cos(radian) * rnd);
		final int y = player.getY() + (int) (Math.sin(radian) * rnd);
		
		boolean canFish = false;
		int z = 0;
		
		// Pick the fishing zone.
		final FishingZone zone = ZoneManager.INSTANCE.getZone(x, y, FishingZone.class);
		if (zone != null)
		{
			z = zone.getWaterZ();
			
			// Check if the height related to the bait location is above water level. If yes, it means the water isn't visible.
			if (GeoEngine.INSTANCE.canSeeTarget(player, new Location(x, y, z)) && GeoEngine.INSTANCE.getHeight(x, y, z) < z)
			{
				z += 10;
				canFish = true;
			}
		}
		
		// You can't fish here.
		if (!canFish)
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}
		
		// Has enough bait, consume 1 and update inventory.
		if (!player.destroyItem("Consume", lure, 1, player, false))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
			return;
		}
		
		// Start fishing.
		player.getFishingStance().start(x, y, z, lure);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}