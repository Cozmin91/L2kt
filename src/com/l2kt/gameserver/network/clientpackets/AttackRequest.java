package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;

public final class AttackRequest extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	@SuppressWarnings("unused")
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
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isInObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// avoid using expensive operations if not needed
		final WorldObject target;
		if (activeChar.getTargetId() == _objectId)
			target = activeChar.getTarget();
		else
			target = World.getInstance().getObject(_objectId);
		
		if (target == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getTarget() != target)
			target.onAction(activeChar);
		else
		{
			if ((target.getObjectId() != activeChar.getObjectId()) && !activeChar.isInStoreMode() && activeChar.getActiveRequester() == null)
				target.onForcedAttack(activeChar);
			else
				sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}