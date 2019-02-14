package com.l2kt.gameserver.handler.itemhandlers;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.data.xml.SummonItemData;
import com.l2kt.gameserver.extensions.BroadcastExtensionsKt;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.model.L2Spawn;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.Playable;
import com.l2kt.gameserver.model.actor.instance.ChristmasTree;
import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.holder.IntIntHolder;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse;
import com.l2kt.gameserver.network.serverpackets.SetupGauge;
import com.l2kt.gameserver.network.serverpackets.SetupGauge.GaugeColor;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (activeChar.isInObserverMode())
			return;
		
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;
		
		final IntIntHolder sitem = SummonItemData.INSTANCE.getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.getValue() > 0)
		{
			activeChar.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return;
		}
		
		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		final int npcId = sitem.getId();
		if (npcId == 0)
			return;
		
		final NpcTemplate npcTemplate = NpcData.INSTANCE.getTemplate(npcId);
		if (npcTemplate == null)
			return;
		
		activeChar.stopMove(null);
		
		switch (sitem.getValue())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					for (ChristmasTree ch : activeChar.getKnownTypeInRadius(ChristmasTree.class, 1200))
					{
						if (npcTemplate.getNpcId() == ChristmasTree.SPECIAL_TREE_ID)
						{
							activeChar.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(ch));
							return;
						}
					}
					
					if (activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false))
					{
						final L2Spawn spawn = new L2Spawn(npcTemplate);
						spawn.setLoc(activeChar.getPosition());
						spawn.setRespawnState(false);
						
						final Npc npc = spawn.doSpawn(true);
						npc.setTitle(activeChar.getName());
						npc.setIsRunning(false); // broadcast info
					}
				}
				catch (Exception e)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
				}
				break;
			case 1: // pet summons
				final WorldObject oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				BroadcastExtensionsKt.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(GaugeColor.BLUE, 5000));
				activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
				activeChar.setIsCastingNow(true);
				
				ThreadPool.schedule(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getId(), item.getObjectId());
				break;
		}
	}
	
	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final Player _activeChar;
		private final ItemInstance _item;
		private final NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(Player activeChar, NpcTemplate npcTemplate, ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
			_activeChar.setIsCastingNow(false);
			
			// check for summon item validity
			if (_item == null || _item.getOwnerId() != _activeChar.getObjectId() || _item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
				return;
			
			// Owner has a pet listed in world.
			if (World.getInstance().getPet(_activeChar.getObjectId()) != null)
				return;
			
			// Add the pet instance to world.
			final Pet pet = Pet.restore(_item, _npcTemplate, _activeChar);
			if (pet == null)
				return;
			
			World.getInstance().addPet(_activeChar.getObjectId(), pet);
			
			_activeChar.setPet(pet);
			
			pet.setRunning();
			pet.setTitle(_activeChar.getName());
			pet.spawnMe();
			pet.startFeed();
			pet.setFollowStatus(true);
		}
	}
}
