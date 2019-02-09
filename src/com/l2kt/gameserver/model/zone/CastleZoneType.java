package com.l2kt.gameserver.model.zone;

import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.model.entity.Castle;

/**
 * A zone type extending {@link ZoneType} used for castle zones.
 */
public abstract class CastleZoneType extends ZoneType
{
	private int _castleId;
	private Castle _castle;
	
	private boolean _enabled;
	
	protected CastleZoneType(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
			_castleId = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	public Castle getCastle()
	{
		if (_castleId > 0 && _castle == null)
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		
		return _castle;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public void setEnabled(boolean val)
	{
		_enabled = val;
	}
}