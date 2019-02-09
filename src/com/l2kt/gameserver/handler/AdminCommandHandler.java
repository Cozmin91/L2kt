package com.l2kt.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import com.l2kt.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminBookmark;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminBuffs;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminCamera;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminCreateItem;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminDelete;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminDoorControl;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminEditNpc;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminEffects;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminEnchant;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminExpSp;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminGeoEngine;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminGm;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminGmChat;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminHeal;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminHelpPage;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminInvul;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminKick;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminKnownlist;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminLevel;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminMaintenance;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminMammon;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminManor;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminMenu;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminMovieMaker;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminOlympiad;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminPForge;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminPetition;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminPledge;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminPolymorph;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminRes;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminShop;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminSiege;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminSkill;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminSpawn;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2kt.gameserver.handler.admincommandhandlers.AdminZone;

public class AdminCommandHandler
{
	private final Map<Integer, IAdminCommandHandler> _entries = new HashMap<>();
	
	protected AdminCommandHandler()
	{
		registerHandler(new AdminAdmin());
		registerHandler(new AdminAnnouncements());
		registerHandler(new AdminBan());
		registerHandler(new AdminBookmark());
		registerHandler(new AdminBuffs());
		registerHandler(new AdminCamera());
		registerHandler(new AdminCreateItem());
		registerHandler(new AdminCursedWeapons());
		registerHandler(new AdminDelete());
		registerHandler(new AdminDoorControl());
		registerHandler(new AdminEditChar());
		registerHandler(new AdminEditNpc());
		registerHandler(new AdminEffects());
		registerHandler(new AdminEnchant());
		registerHandler(new AdminExpSp());
		registerHandler(new AdminGeoEngine());
		registerHandler(new AdminGm());
		registerHandler(new AdminGmChat());
		registerHandler(new AdminHeal());
		registerHandler(new AdminHelpPage());
		registerHandler(new AdminInvul());
		registerHandler(new AdminKick());
		registerHandler(new AdminKnownlist());
		registerHandler(new AdminLevel());
		registerHandler(new AdminMaintenance());
		registerHandler(new AdminMammon());
		registerHandler(new AdminManor());
		registerHandler(new AdminMenu());
		registerHandler(new AdminMovieMaker());
		registerHandler(new AdminOlympiad());
		registerHandler(new AdminPetition());
		registerHandler(new AdminPForge());
		registerHandler(new AdminPledge());
		registerHandler(new AdminPolymorph());
		registerHandler(new AdminRes());
		registerHandler(new AdminRideWyvern());
		registerHandler(new AdminShop());
		registerHandler(new AdminSiege());
		registerHandler(new AdminSkill());
		registerHandler(new AdminSpawn());
		registerHandler(new AdminTarget());
		registerHandler(new AdminTeleport());
		registerHandler(new AdminZone());
	}
	
	private void registerHandler(IAdminCommandHandler handler)
	{
		for (String id : handler.getAdminCommandList())
			_entries.put(id.hashCode(), handler);
	}
	
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		
		if (adminCommand.indexOf(" ") != -1)
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		
		return _entries.get(command.hashCode());
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler INSTANCE = new AdminCommandHandler();
	}
}