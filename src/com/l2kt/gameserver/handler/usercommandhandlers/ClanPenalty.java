package com.l2kt.gameserver.handler.usercommandhandlers;

import java.text.SimpleDateFormat;

import com.l2kt.commons.lang.StringUtil;
import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.handler.IUserCommandHandler;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.entity.Castle;
import com.l2kt.gameserver.model.pledge.Clan;

import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanPenalty implements IUserCommandHandler
{
	private static final String NO_PENALTY = "<tr><td width=170>No penalty is imposed.</td><td width=100 align=center></td></tr>";
	
	private static final int[] COMMAND_IDS =
	{
		100
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final StringBuilder sb = new StringBuilder();
		final long currentTime = System.currentTimeMillis();
		
		// Join a clan penalty.
		if (activeChar.getClanJoinExpiryTime() > currentTime)
			StringUtil.append(sb, "<tr><td width=170>Unable to join a clan.</td><td width=100 align=center>", sdf.format(activeChar.getClanJoinExpiryTime()), "</td></tr>");
		
		// Create a clan penalty.
		if (activeChar.getClanCreateExpiryTime() > currentTime)
			StringUtil.append(sb, "<tr><td width=170>Unable to create a clan.</td><td width=100 align=center>", sdf.format(activeChar.getClanCreateExpiryTime()), "</td></tr>");
		
		final Clan clan = activeChar.getClan();
		if (clan != null)
		{
			// Invitation in a clan penalty.
			if (clan.getCharPenaltyExpiryTime() > currentTime)
				StringUtil.append(sb, "<tr><td width=170>Unable to invite a clan member.</td><td width=100 align=center>", sdf.format(clan.getCharPenaltyExpiryTime()), "</td></tr>");
			
			// Alliance penalties.
			final int penaltyType = clan.getAllyPenaltyType();
			if (penaltyType != 0)
			{
				final long expiryTime = clan.getAllyPenaltyExpiryTime();
				if (expiryTime > currentTime)
				{
					// Unable to join an alliance.
					if (penaltyType == Clan.PENALTY_TYPE_CLAN_LEAVED || penaltyType == Clan.PENALTY_TYPE_CLAN_DISMISSED)
						StringUtil.append(sb, "<tr><td width=170>Unable to join an alliance.</td><td width=100 align=center>", sdf.format(expiryTime), "</td></tr>");
					// Unable to invite a new alliance member.
					else if (penaltyType == Clan.PENALTY_TYPE_DISMISS_CLAN)
						StringUtil.append(sb, "<tr><td width=170>Unable to invite a new alliance member.</td><td width=100 align=center>", sdf.format(expiryTime), "</td></tr>");
					// Unable to create an alliance.
					else if (penaltyType == Clan.PENALTY_TYPE_DISSOLVE_ALLY)
						StringUtil.append(sb, "<tr><td width=170>Unable to create an alliance.</td><td width=100 align=center>", sdf.format(expiryTime), "</td></tr>");
				}
			}
			
			// Clan dissolution request.
			if (clan.getDissolvingExpiryTime() > currentTime)
				StringUtil.append(sb, "<tr><td width=170>The request to dissolve the clan is currently being processed.  (Restrictions are now going to be imposed on the use of clan functions.)</td><td width=100 align=center>", sdf.format(clan.getDissolvingExpiryTime()), "</td></tr>");
			
			boolean registeredOnAnySiege = false;
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				if (castle.getSiege().checkSides(clan))
				{
					registeredOnAnySiege = true;
					break;
				}
			}
			
			// Unable to dissolve a clan.
			if (clan.getAllyId() != 0 || clan.isAtWar() || clan.hasCastle() || clan.hasHideout() || registeredOnAnySiege)
				StringUtil.append(sb, "<tr><td width=170>Unable to dissolve a clan.</td><td></td></tr>");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/clan_penalty.htm");
		html.replace("%content%", (sb.length() == 0) ? NO_PENALTY : sb.toString());
		activeChar.sendPacket(html);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}