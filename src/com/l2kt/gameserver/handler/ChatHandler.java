package com.l2kt.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import com.l2kt.gameserver.handler.chathandlers.ChatAll;
import com.l2kt.gameserver.handler.chathandlers.ChatAlliance;
import com.l2kt.gameserver.handler.chathandlers.ChatClan;
import com.l2kt.gameserver.handler.chathandlers.ChatHeroVoice;
import com.l2kt.gameserver.handler.chathandlers.ChatParty;
import com.l2kt.gameserver.handler.chathandlers.ChatPartyMatchRoom;
import com.l2kt.gameserver.handler.chathandlers.ChatPartyRoomAll;
import com.l2kt.gameserver.handler.chathandlers.ChatPartyRoomCommander;
import com.l2kt.gameserver.handler.chathandlers.ChatPetition;
import com.l2kt.gameserver.handler.chathandlers.ChatShout;
import com.l2kt.gameserver.handler.chathandlers.ChatTell;
import com.l2kt.gameserver.handler.chathandlers.ChatTrade;

public class ChatHandler
{
	private final Map<Integer, IChatHandler> _entries = new HashMap<>();
	
	protected ChatHandler()
	{
		registerHandler(new ChatAll());
		registerHandler(new ChatAlliance());
		registerHandler(new ChatClan());
		registerHandler(new ChatHeroVoice());
		registerHandler(new ChatParty());
		registerHandler(new ChatPartyMatchRoom());
		registerHandler(new ChatPartyRoomAll());
		registerHandler(new ChatPartyRoomCommander());
		registerHandler(new ChatPetition());
		registerHandler(new ChatShout());
		registerHandler(new ChatTell());
		registerHandler(new ChatTrade());
	}
	
	private void registerHandler(IChatHandler handler)
	{
		for (int id : handler.getChatTypeList())
			_entries.put(id, handler);
	}
	
	public IChatHandler getHandler(int chatType)
	{
		return _entries.get(chatType);
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler INSTANCE = new ChatHandler();
	}
}