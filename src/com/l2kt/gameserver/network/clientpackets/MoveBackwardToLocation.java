package com.l2kt.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;

import com.l2kt.Config;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.location.Location;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.ActionFailed;
import com.l2kt.gameserver.network.serverpackets.EnchantResult;
import com.l2kt.gameserver.network.serverpackets.StopMove;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	
	@SuppressWarnings("unused")
	private int _moveMovement;
	
	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		
		try
		{
			_moveMovement = readD(); // is 0 if cursor keys are used 1 if mouse is used
		}
		catch (BufferUnderflowException e)
		{
			if (Config.L2WALKER_PROTECTION)
			{
				final Player player = getClient().getActiveChar();
				if (player != null)
					player.logout(false);
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isOutOfControl())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getActiveEnchantItem() != null)
		{
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}
		
		// Correcting targetZ from floor level to head level
		_targetZ += activeChar.getCollisionHeight();
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, 0);
			return;
		}
		
		double dx = _targetX - _originX;
		double dy = _targetY - _originY;
		
		if ((dx * dx + dy * dy) > 98010000) // 9900*9900
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		activeChar.getAI().setIntention(CtrlIntention.MOVE_TO, new Location(_targetX, _targetY, _targetZ));
	}
}