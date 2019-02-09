package com.l2kt.gameserver.model.actor.instance;

import com.l2kt.gameserver.model.rift.DimensionalRift;
import com.l2kt.gameserver.instancemanager.SevenSigns;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.group.Party;

/**
 * An instance type extending {@link Folk}, used by Guardian of Border NPC (internal room rift teleporters).
 */
public class BorderGuard extends Folk
{
	public BorderGuard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final Party party = player.getParty();
		if (party == null)
			return;
		
		final DimensionalRift rift = party.getDimensionalRift();
		if (rift == null)
			return;
		
		if (command.startsWith("ChangeRiftRoom"))
			rift.manualTeleport(player, this);
		else if (command.startsWith("ExitRift"))
			rift.manualExitRift(player, this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return SevenSigns.SEVEN_SIGNS_HTML_PATH + "rift/GuardianOfBorder.htm";
	}
}