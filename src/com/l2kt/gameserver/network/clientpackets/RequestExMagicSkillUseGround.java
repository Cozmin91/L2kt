package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.commons.math.MathUtil;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.location.Location;

import com.l2kt.gameserver.network.serverpackets.ValidateLocation;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	
	private int _skillId;
	
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		
		_skillId = readD();
		
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current player
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = player.getSkill(_skillId);
		if (skill == null)
			return;
		
		player.setCurrentSkillWorldPosition(new Location(_x, _y, _z));
		
		// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
		player.setHeading(MathUtil.calculateHeadingFrom(player.getX(), player.getY(), _x, _y));
		player.broadcastPacket(new ValidateLocation(player));
		
		player.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
}