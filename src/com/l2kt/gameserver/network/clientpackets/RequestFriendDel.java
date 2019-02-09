package com.l2kt.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.l2kt.L2DatabaseFactory;
import com.l2kt.gameserver.data.sql.PlayerInfoTable;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.FriendList;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendDel extends L2GameClientPacket
{
	private static final String DELETE_FRIEND = "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final int friendId = PlayerInfoTable.getInstance().getPlayerObjectId(_name);
		if (friendId == -1 || !player.getFriendList().contains(friendId))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(_name));
			return;
		}
		
		// Player deleted from your friendlist
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
		
		player.getFriendList().remove(Integer.valueOf(friendId));
		player.sendPacket(new FriendList(player)); // update friendList *heavy method*
		
		final Player friend = World.getInstance().getPlayer(_name);
		if (friend != null)
		{
			friend.getFriendList().remove(Integer.valueOf(player.getObjectId()));
			friend.sendPacket(new FriendList(friend)); // update friendList *heavy method*
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_FRIEND))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, friendId);
			ps.setInt(3, friendId);
			ps.setInt(4, player.getObjectId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete friendId {} for {}.", e, friendId, player.toString());
		}
	}
}