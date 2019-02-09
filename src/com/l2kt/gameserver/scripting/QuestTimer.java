package com.l2kt.gameserver.scripting;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.gameserver.model.actor.Npc;
import com.l2kt.gameserver.model.actor.instance.Player;

public class QuestTimer
{
	protected static final Logger _log = Logger.getLogger(QuestTimer.class.getName());
	
	protected final Quest _quest;
	protected final String _name;
	protected final Npc _npc;
	protected final Player _player;
	protected final boolean _isRepeating;
	
	protected ScheduledFuture<?> _schedular;
	
	public QuestTimer(Quest quest, String name, Npc npc, Player player, long time, boolean repeating)
	{
		_quest = quest;
		_name = name;
		_npc = npc;
		_player = player;
		_isRepeating = repeating;
		
		if (repeating)
			_schedular = ThreadPool.scheduleAtFixedRate(new ScheduleTimerTask(), time, time);
		else
			_schedular = ThreadPool.schedule(new ScheduleTimerTask(), time);
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
	
	protected final class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_schedular == null)
				return;
			
			if (!_isRepeating)
				cancel();
			
			_quest.notifyEvent(_name, _npc, _player);
		}
	}
	
	public final void cancel()
	{
		if (_schedular != null)
		{
			_schedular.cancel(false);
			_schedular = null;
		}
		
		_quest.removeQuestTimer(this);
	}
	
	/**
	 * public method to compare if this timer matches with the key attributes passed.
	 * @param quest : Quest instance to which the timer is attached
	 * @param name : Name of the timer
	 * @param npc : Npc instance attached to the desired timer (null if no npc attached)
	 * @param player : Player instance attached to the desired timer (null if no player attached)
	 * @return boolean
	 */
	public final boolean equals(Quest quest, String name, Npc npc, Player player)
	{
		if (quest == null || quest != _quest)
			return false;
		
		if (name == null || !name.equals(_name))
			return false;
		
		return ((npc == _npc) && (player == _player));
	}
}