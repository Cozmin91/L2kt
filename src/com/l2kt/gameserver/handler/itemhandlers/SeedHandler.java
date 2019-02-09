package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.Config;
import com.l2kt.gameserver.data.manager.CastleManorManager;
import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Attackable;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.manor.Seed;
import com.l2kt.gameserver.network.SystemMessageId;

public class SeedHandler implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!Config.ALLOW_MANOR || !(playable instanceof Player))
			return;
		
		final WorldObject tgt = playable.getTarget();
		if (!(tgt instanceof Attackable) || !((Attackable) tgt).getTemplate().isSeedable())
		{
			playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return;
		}
		
		final Attackable target = (Attackable) tgt;
		if (target.isDead() || target.isSeeded())
		{
			playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final Seed seed = CastleManorManager.getInstance().getSeed(item.getItemId());
		if (seed == null)
			return;
		
		if (seed.getCastleId() != MapRegionData.getInstance().getAreaCastle(playable.getX(), playable.getY()))
		{
			playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return;
		}
		
		target.setSeeded(seed, playable.getObjectId());
		
		final IntIntHolder[] skills = item.getEtcItem().getSkills();
		if (skills != null)
		{
			if (skills[0] == null)
				return;
			
			playable.useMagic(skills[0].getSkill(), false, false);
		}
	}
}