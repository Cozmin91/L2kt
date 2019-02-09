package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.commons.math.MathUtil;
import com.l2kt.gameserver.model.BlockList;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;

import com.l2kt.gameserver.network.serverpackets.SendTradeRequest;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class TradeRequest extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Player target = World.getInstance().getPlayer(_objectId);
		if (target == null || !player.getKnownType(Player.class).contains(target) || target.equals(player))
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (target.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target cannot trade during Olympiad.");
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0 || target.getKarma() > 0))
		{
			player.sendMessage("You cannot trade in a chaotic state.");
			return;
		}
		
		if (player.isInStoreMode() || target.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		if (target.isProcessingRequest() || target.isProcessingTransaction())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(target);
			player.sendPacket(sm);
			return;
		}
		
		if (target.getTradeRefusal())
		{
			player.sendMessage("Your target is in trade refusal mode.");
			return;
		}
		
		if (BlockList.isBlocked(target, player))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addCharName(target);
			player.sendPacket(sm);
			return;
		}
		
		if (MathUtil.calculateDistance(player, target, true) > Npc.INTERACTION_DISTANCE)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}
		
		player.onTransactionRequest(target);
		target.sendPacket(new SendTradeRequest(player.getObjectId()));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE).addCharName(target));
	}
}