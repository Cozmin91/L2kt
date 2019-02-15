package com.l2kt.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.List;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.commons.random.Rnd;
import com.l2kt.gameserver.data.manager.FourSepulchersManager;
import com.l2kt.gameserver.data.xml.DoorData;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.item.instance.ItemInstance;

import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.network.clientpackets.Say2;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.CreatureSay;
import com.l2kt.gameserver.network.serverpackets.MoveToPawn;
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2kt.gameserver.scripting.EventType;
import com.l2kt.gameserver.scripting.Quest;

public class SepulcherNpc extends Folk
{
	private static final String HTML_FILE_PATH = "data/html/sepulchers/";
	private static final int HALLS_KEY = 7260;
	
	public SepulcherNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
			else if (!isAutoAttackable(player))
			{
				// Calculate the distance between the Player and this instance.
				if (!canInteract(player))
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				else
				{
					// Stop moving if we're already in interact range.
					if (player.isMoving() || player.isInCombat())
						player.getAI().setIntention(CtrlIntention.IDLE);
					
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
					
					if (hasRandomAnimation())
						onRandomAnimation(Rnd.INSTANCE.get(8));
					
					doAction(player);
				}
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		// Check if the Player is a GM ; send him NPC infos if true.
		if (player.isGM())
			sendNpcInfos(player);
		
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player))
			{
				if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this))
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
				else
					player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			}
			else if (canInteract(player))
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
				
				if (hasRandomAnimation())
					onRandomAnimation(Rnd.INSTANCE.get(8));
				
				doAction(player);
			}
			else
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
		}
	}
	
	private void doAction(Player player)
	{
		if (isDead())
		{
			player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		switch (getNpcId())
		{
			case 31468:
			case 31469:
			case 31470:
			case 31471:
			case 31472:
			case 31473:
			case 31474:
			case 31475:
			case 31476:
			case 31477:
			case 31478:
			case 31479:
			case 31480:
			case 31481:
			case 31482:
			case 31483:
			case 31484:
			case 31485:
			case 31486:
			case 31487:
				// Time limit is reached. You can't open anymore Mysterious boxes after the 49th minute.
				if (Calendar.getInstance().get(Calendar.MINUTE) >= 50)
				{
					broadcastNpcSay("You can start at the scheduled time.");
					return;
				}
				FourSepulchersManager.INSTANCE.spawnMonster(getNpcId());
				deleteMe();
				break;
			
			case 31455:
			case 31456:
			case 31457:
			case 31458:
			case 31459:
			case 31460:
			case 31461:
			case 31462:
			case 31463:
			case 31464:
			case 31465:
			case 31466:
			case 31467:
				if (player.isInParty() && !player.getParty().isLeader(player))
					player = player.getParty().getLeader();
				
				player.addItem("Quest", HALLS_KEY, 1, player, true);
				
				deleteMe();
				break;
			
			default:
			{
				List<Quest> scripts = getTemplate().getEventQuests(EventType.QUEST_START);
				if (scripts != null && !scripts.isEmpty())
					player.setLastQuestNpcObject(getObjectId());
				
				scripts = getTemplate().getEventQuests(EventType.ON_FIRST_TALK);
				if (scripts != null && scripts.size() == 1)
					scripts.get(0).notifyFirstTalk(this, player);
				else
					showChatWindow(player);
			}
		}
		player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return HTML_FILE_PATH + filename + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("open_gate"))
		{
			final ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
			if (hallsKey == null)
				showHtmlFile(player, "Gatekeeper-no.htm");
			else if (FourSepulchersManager.INSTANCE.isAttackTime())
			{
				switch (getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						FourSepulchersManager.INSTANCE.spawnShadow(getNpcId());
					default:
					{
						openNextDoor(getNpcId());
						
						final Party party = player.getParty();
						if (party != null)
						{
							for (Player member : player.getParty().getMembers())
							{
								final ItemInstance key = member.getInventory().getItemByItemId(HALLS_KEY);
								if (key != null)
									member.destroyItemByItemId("Quest", HALLS_KEY, key.getCount(), member, true);
							}
						}
						else
							player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.getCount(), player, true);
					}
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	public void openNextDoor(int npcId)
	{
		final int doorId = FourSepulchersManager.INSTANCE.getHallGateKeepers().get(npcId);
		final Door door = DoorData.INSTANCE.getDoor(doorId);
		
		// Open the door.
		door.openMe();
		
		// Schedule the automatic door close.
		ThreadPool.INSTANCE.schedule(() -> door.closeMe(), 10000);
		
		// Spawn the next mysterious box.
		FourSepulchersManager.INSTANCE.spawnMysteriousBox(npcId);
		
		sayInShout("The monsters have spawned!");
	}
	
	public void sayInShout(String msg)
	{
		if (msg == null || msg.isEmpty())
			return;
		
		final CreatureSay sm = new CreatureSay(getObjectId(), Say2.SHOUT, getName(), msg);
		for (Player player : getKnownType(Player.class))
			player.sendPacket(sm);
	}
	
	public void showHtmlFile(Player player, String file)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/sepulchers/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}