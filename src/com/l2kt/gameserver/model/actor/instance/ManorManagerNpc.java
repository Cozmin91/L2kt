package com.l2kt.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2kt.Config;
import com.l2kt.gameserver.data.manager.CastleManorManager;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.BuyListSeed;
import com.l2kt.gameserver.network.serverpackets.ExShowCropInfo;
import com.l2kt.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import com.l2kt.gameserver.network.serverpackets.ExShowProcureCropDetail;
import com.l2kt.gameserver.network.serverpackets.ExShowSeedInfo;
import com.l2kt.gameserver.network.serverpackets.ExShowSellCropList;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class ManorManagerNpc extends Merchant
{
	public ManorManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("manor_menu_select"))
		{
			if (CastleManorManager.INSTANCE.isUnderMaintenance())
			{
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
				player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				return;
			}
			
			final StringTokenizer st = new StringTokenizer(command, "&");
			
			final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			final int state = Integer.parseInt(st.nextToken().split("=")[1]);
			final boolean time = st.nextToken().split("=")[1].equals("1");
			
			final int castleId = (state < 0) ? getCastle().getCastleId() : state;
			
			switch (ask)
			{
				case 1: // Seed purchase
					if (castleId != getCastle().getCastleId())
						player.sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR).addString(getCastle().getName()));
					else
						player.sendPacket(new BuyListSeed(player.getAdena(), castleId));
					break;
				
				case 2: // Crop sales
					player.sendPacket(new ExShowSellCropList(player.getInventory(), castleId));
					break;
				
				case 3: // Current seeds (Manor info)
					player.sendPacket(new ExShowSeedInfo(castleId, time, false));
					break;
				
				case 4: // Current crops (Manor info)
					player.sendPacket(new ExShowCropInfo(castleId, time, false));
					break;
				
				case 5: // Basic info (Manor info)
					player.sendPacket(new ExShowManorDefaultInfo(false));
					break;
				
				case 6: // Buy harvester
					showBuyWindow(player, 300000 + getNpcId());
					break;
				
				case 9: // Edit sales (Crop sales)
					player.sendPacket(new ExShowProcureCropDetail(state));
					break;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/manormanager/manager.htm";
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		if (!Config.ALLOW_MANOR)
		{
			showChatWindow(player, "data/html/npcdefault.htm");
			return;
		}
		
		if (getCastle() != null && player.getClan() != null && getCastle().getOwnerId() == player.getClanId() && player.isClanLeader())
			showChatWindow(player, "data/html/manormanager/manager-lord.htm");
		else
			showChatWindow(player, "data/html/manormanager/manager.htm");
	}
}
