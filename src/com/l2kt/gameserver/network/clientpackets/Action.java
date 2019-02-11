package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.entity.Duel;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	private boolean _isShiftAction;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_isShiftAction = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isInObserverMode())
		{
			player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		if (player.getActiveRequester() != null)
		{
			player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		final WorldObject target = (player.getTargetId() == _objectId) ? player.getTarget() : World.getInstance().getObject(_objectId);
		if (target == null)
		{
			player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		final Player targetPlayer = target.getActingPlayer();
		if (targetPlayer != null && targetPlayer.getDuelState() == Duel.DuelState.DEAD)
		{
			player.sendPacket(SystemMessageId.OTHER_PARTY_IS_FROZEN);
			player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		if (_isShiftAction)
			target.onActionShift(player);
		else
			target.onAction(player);
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}