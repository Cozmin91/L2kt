package com.l2kt.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2kt.L2DatabaseFactory;
import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.gameserver.data.sql.ClanTable;
import com.l2kt.gameserver.model.actor.instance.Player;

import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.instancemanager.AuctionManager;
import com.l2kt.gameserver.instancemanager.ClanHallManager;
import com.l2kt.gameserver.model.pledge.Clan;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.SystemMessage;

public class Auction
{
	protected static final Logger _log = Logger.getLogger(Auction.class.getName());
	private int _id = 0;
	private long _endDate;
	private int _highestBidderId = 0;
	private String _highestBidderName = "";
	private int _highestBidderMaxBid = 0;
	private int _itemId = 0;
	private String _itemName = "";
	private int _sellerId = 0;
	private String _sellerClanName = "";
	private String _sellerName = "";
	private int _currentBid = 0;
	private int _startingBid = 0;
	
	private final Map<Integer, Bidder> _bidders = new HashMap<>();
	
	public class Bidder
	{
		private final String _name;
		private final String _clanName;
		private int _bid;
		private final Calendar _timeBid;
		
		public Bidder(String name, String clanName, int bid, long timeBid)
		{
			_name = name;
			_clanName = clanName;
			_bid = bid;
			_timeBid = Calendar.getInstance();
			_timeBid.setTimeInMillis(timeBid);
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getClanName()
		{
			return _clanName;
		}
		
		public int getBid()
		{
			return _bid;
		}
		
		public Calendar getTimeBid()
		{
			return _timeBid;
		}
		
		public void setTimeBid(long timeBid)
		{
			_timeBid.setTimeInMillis(timeBid);
		}
		
		public void setBid(int bid)
		{
			_bid = bid;
		}
	}
	
	/** Task Sheduler for endAuction */
	public class AutoEndTask implements Runnable
	{
		public AutoEndTask()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				endAuction();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/**
	 * Constructor
	 * @param auctionId The id linked to that auction
	 */
	public Auction(int auctionId)
	{
		_id = auctionId;
		load();
		startAutoTask();
	}
	
	public Auction(int itemId, Clan Clan, long delay, int bid, String name)
	{
		_id = itemId;
		_endDate = System.currentTimeMillis() + delay;
		_itemId = itemId;
		_itemName = name;
		_sellerId = Clan.getLeaderId();
		_sellerName = Clan.getLeaderName();
		_sellerClanName = Clan.getName();
		_startingBid = bid;
	}
	
	/** Load auctions */
	private void load()
	{
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM auction WHERE id = ?");
			statement.setInt(1, getId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				_currentBid = rs.getInt("currentBid");
				_endDate = rs.getLong("endDate");
				_itemId = rs.getInt("itemId");
				_itemName = rs.getString("itemName");
				_sellerId = rs.getInt("sellerId");
				_sellerClanName = rs.getString("sellerClanName");
				_sellerName = rs.getString("sellerName");
				_startingBid = rs.getInt("startingBid");
			}
			rs.close();
			statement.close();
			loadBid();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: Auction.load(): " + e.getMessage(), e);
		}
	}
	
	/** Load bidders **/
	private void loadBid()
	{
		_highestBidderId = 0;
		_highestBidderName = "";
		_highestBidderMaxBid = 0;
		
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC");
			statement.setInt(1, getId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				if (rs.isFirst())
				{
					_highestBidderId = rs.getInt("bidderId");
					_highestBidderName = rs.getString("bidderName");
					_highestBidderMaxBid = rs.getInt("maxBid");
				}
				_bidders.put(rs.getInt("bidderId"), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid")));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: Auction.loadBid(): " + e.getMessage(), e);
		}
	}
	
	/** Task Manage */
	private void startAutoTask()
	{
		long currentTime = System.currentTimeMillis();
		long taskDelay = 0;
		
		if (_endDate <= currentTime)
		{
			_endDate = currentTime + 604800000; // 1 week
			saveAuctionDate();
		}
		else
			taskDelay = _endDate - currentTime;
		
		ThreadPool.schedule(new AutoEndTask(), taskDelay);
	}
	
	/** Save Auction Data End */
	private void saveAuctionDate()
	{
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
			statement.setLong(1, _endDate);
			statement.setInt(2, _id);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Set a bid
	 * @param bidder The bidder.
	 * @param bid The bid amount.
	 */
	public synchronized void setBid(Player bidder, int bid)
	{
		int requiredAdena = bid;
		
		if (getHighestBidderName().equals(bidder.getClan().getLeaderName()))
			requiredAdena = bid - getHighestBidderMaxBid();
		
		if ((getHighestBidderId() > 0 && bid > getHighestBidderMaxBid()) || (getHighestBidderId() == 0 && bid >= getStartingBid()))
		{
			if (takeItem(bidder, requiredAdena))
			{
				updateInDB(bidder, bid);
				bidder.getClan().setAuctionBiddedAt(_id);
				return;
			}
		}
		
		if ((bid < getStartingBid()) || (bid <= getHighestBidderMaxBid()))
			bidder.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
	}
	
	/**
	 * Return Item in WHC
	 * @param Clan The clan to make warehouse checks on.
	 * @param quantity amount of returned adenas.
	 * @param penalty if true, 10% of quantity is lost.
	 */
	private static void returnItem(String Clan, int quantity, boolean penalty)
	{
		if (penalty)
			quantity *= 0.9; // Take 10% tax fee if needed
			
		// avoid overflow on return
		final int limit = Integer.MAX_VALUE - ClanTable.getInstance().getClanByName(Clan).getWarehouse().getAdena();
		quantity = Math.min(quantity, limit);
		
		ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", 57, quantity, null, null);
	}
	
	/**
	 * Take Item in WHC
	 * @param bidder The bidder to make checks on.
	 * @param quantity amount of money.
	 * @return true if successful.
	 */
	public static boolean takeItem(Player bidder, int quantity)
	{
		if (bidder.getClan() != null && bidder.getClan().getWarehouse().getAdena() >= quantity)
		{
			bidder.getClan().getWarehouse().destroyItemByItemId("Buy", 57, quantity, bidder, bidder);
			return true;
		}
		
		bidder.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
		return false;
	}
	
	/**
	 * Update auction in DB
	 * @param bidder The bidder to make checks on.
	 * @param bid The related bid id.
	 */
	private void updateInDB(Player bidder, int bid)
	{
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement;
			
			if (getBidders().get(bidder.getClanId()) != null)
			{
				statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
				statement.setInt(1, bidder.getClanId());
				statement.setString(2, bidder.getClan().getLeaderName());
				statement.setInt(3, bid);
				statement.setLong(4, System.currentTimeMillis());
				statement.setInt(5, getId());
				statement.setInt(6, bidder.getClanId());
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, IdFactory.getInstance().getNextId());
				statement.setInt(2, getId());
				statement.setInt(3, bidder.getClanId());
				statement.setString(4, bidder.getName());
				statement.setInt(5, bid);
				statement.setString(6, bidder.getClan().getName());
				statement.setLong(7, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			
			_highestBidderId = bidder.getClanId();
			_highestBidderMaxBid = bid;
			_highestBidderName = bidder.getClan().getLeaderName();
			
			if (_bidders.get(_highestBidderId) == null)
				_bidders.put(_highestBidderId, new Bidder(_highestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
			else
			{
				_bidders.get(_highestBidderId).setBid(bid);
				_bidders.get(_highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
			}
			bidder.sendPacket(SystemMessageId.BID_IN_CLANHALL_AUCTION);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.updateInDB(Player bidder, int bid): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove bids.
	 * @param newOwner The Clan object who won the bid.
	 */
	private void removeBids(Clan newOwner)
	{
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?");
			statement.setInt(1, getId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
		}
		
		Clan biddingClan;
		for (Bidder b : _bidders.values())
		{
			biddingClan = ClanTable.getInstance().getClanByName(b.getClanName());
			biddingClan.setAuctionBiddedAt(0);
			
			if (biddingClan != newOwner)
				returnItem(b.getClanName(), b.getBid(), true); // 10 % tax
				
			biddingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_AWARDED_TO_CLAN_S1).addString(newOwner.getName()));
		}
		_bidders.clear();
	}
	
	/** Remove auctions */
	public void deleteAuctionFromDB()
	{
		AuctionManager.INSTANCE.getAuctions().remove(this);
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?");
			statement.setInt(1, _itemId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
		}
	}
	
	/** End of auction */
	public void endAuction()
	{
		if (ClanHallManager.getInstance().loaded())
		{
			if (_highestBidderId == 0 && _sellerId == 0)
			{
				startAutoTask();
				return;
			}
			
			// If seller hasn't sell clanHall, the auction is dropped. Money of seller is lost.
			if (_highestBidderId == 0 && _sellerId > 0)
			{
				int aucId = AuctionManager.INSTANCE.getAuctionIndex(_id);
				AuctionManager.INSTANCE.getAuctions().remove(aucId);
				
				// Retrieves the seller.
				Clan owner = ClanTable.getInstance().getClanByName(_sellerClanName);
				owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_NOT_SOLD));
				return;
			}
			
			// Return intial lease of seller + highest bid amount.
			if (_sellerId > 0)
			{
				returnItem(_sellerClanName, _highestBidderMaxBid, true);
				returnItem(_sellerClanName, ClanHallManager.getInstance().getClanHallById(_itemId).getLease(), false);
			}
			
			deleteAuctionFromDB();
			
			Clan newOwner = ClanTable.getInstance().getClanByName(_bidders.get(_highestBidderId).getClanName());
			removeBids(newOwner);
			ClanHallManager.getInstance().setOwner(_itemId, newOwner);
		}
		// Task waiting ClanHallManager is loaded every 3s
		else
			ThreadPool.schedule(new AutoEndTask(), 3000);
	}
	
	/**
	 * Cancel bid
	 * @param bidder The bidder id.
	 */
	public synchronized void cancelBid(int bidder)
	{
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?");
			statement.setInt(1, getId());
			statement.setInt(2, bidder);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + e.getMessage(), e);
		}
		
		returnItem(_bidders.get(bidder).getClanName(), _bidders.get(bidder).getBid(), true);
		ClanTable.getInstance().getClanByName(_bidders.get(bidder).getClanName()).setAuctionBiddedAt(0);
		_bidders.clear();
		loadBid();
	}
	
	/** Cancel auction */
	public void cancelAuction()
	{
		deleteAuctionFromDB();
		removeBids(ClanTable.getInstance().getClanByName(_sellerClanName));
	}
	
	/** Confirm an auction */
	public void confirmAuction()
	{
		AuctionManager.INSTANCE.getAuctions().add(this);
		try (Connection con = L2DatabaseFactory.INSTANCE.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemId, itemName, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getId());
			statement.setInt(2, _sellerId);
			statement.setString(3, _sellerName);
			statement.setString(4, _sellerClanName);
			statement.setInt(5, _itemId);
			statement.setString(6, _itemName);
			statement.setInt(7, _startingBid);
			statement.setInt(8, _currentBid);
			statement.setLong(9, _endDate);
			statement.execute();
			statement.close();
			loadBid();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.load(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return the auction id.
	 */
	public final int getId()
	{
		return _id;
	}
	
	public final int getCurrentBid()
	{
		return _currentBid;
	}
	
	public final long getEndDate()
	{
		return _endDate;
	}
	
	public final int getHighestBidderId()
	{
		return _highestBidderId;
	}
	
	public final String getHighestBidderName()
	{
		return _highestBidderName;
	}
	
	public final int getHighestBidderMaxBid()
	{
		return _highestBidderMaxBid;
	}
	
	public final int getItemId()
	{
		return _itemId;
	}
	
	public final String getItemName()
	{
		return _itemName;
	}
	
	public final int getSellerId()
	{
		return _sellerId;
	}
	
	public final String getSellerName()
	{
		return _sellerName;
	}
	
	public final String getSellerClanName()
	{
		return _sellerClanName;
	}
	
	public final int getStartingBid()
	{
		return _startingBid;
	}
	
	public final Map<Integer, Bidder> getBidders()
	{
		return _bidders;
	}
}