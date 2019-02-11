package com.l2kt.gameserver.idfactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.commons.math.PrimeFinder;

public class BitSetIDFactory extends IdFactory
{
	private static Logger _log = Logger.getLogger(BitSetIDFactory.class.getName());
	
	private BitSet _freeIds;
	private AtomicInteger _freeIdCount;
	private AtomicInteger _nextFreeId;
	
	protected class BitSetCapacityCheck implements Runnable
	{
		@Override
		public void run()
		{
			if (reachingBitSetCapacity())
				increaseBitSetCapacity();
		}
	}
	
	protected BitSetIDFactory()
	{
		super();
		initialize();
		
		ThreadPool.scheduleAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
	}
	
	private void initialize()
	{
		try
		{
			_freeIds = new BitSet(PrimeFinder.INSTANCE.nextPrime(100000));
			_freeIds.clear();
			_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			for (int usedObjectId : extractUsedObjectIDTable())
			{
				int objectID = usedObjectId - FIRST_OID;
				if (objectID < 0)
				{
					_log.warning("Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				_freeIds.set(usedObjectId - FIRST_OID);
				_freeIdCount.decrementAndGet();
			}
			
			_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
			_initialized = true;
			
			_log.info("IDFactory: " + _freeIds.size() + " id's available.");
		}
		catch (Exception e)
		{
			_initialized = false;
			_log.log(Level.SEVERE, "BitSet ID Factory could not be initialized correctly: " + e.getMessage(), e);
		}
	}
	
	@Override
	public synchronized void releaseId(int objectID)
	{
		if ((objectID - FIRST_OID) > -1)
		{
			_freeIds.clear(objectID - FIRST_OID);
			_freeIdCount.incrementAndGet();
		}
		else
			_log.warning("BitSet ID Factory: release objectID " + objectID + " failed (< " + FIRST_OID + ")");
	}
	
	@Override
	public synchronized int getNextId()
	{
		int newID = _nextFreeId.get();
		_freeIds.set(newID);
		_freeIdCount.decrementAndGet();
		
		int nextFree = _freeIds.nextClearBit(newID);
		
		if (nextFree < 0)
			nextFree = _freeIds.nextClearBit(0);
		
		if (nextFree < 0)
		{
			if (_freeIds.size() < FREE_OBJECT_ID_SIZE)
				increaseBitSetCapacity();
			else
				throw new NullPointerException("Ran out of valid Id's.");
		}
		
		_nextFreeId.set(nextFree);
		
		return newID + FIRST_OID;
	}
	
	@Override
	public synchronized int size()
	{
		return _freeIdCount.get();
	}
	
	protected synchronized int usedIdCount()
	{
		return (size() - FIRST_OID);
	}
	
	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.INSTANCE.nextPrime(usedIdCount() * 11 / 10) > _freeIds.size();
	}
	
	protected synchronized void increaseBitSetCapacity()
	{
		BitSet newBitSet = new BitSet(PrimeFinder.INSTANCE.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(_freeIds);
		_freeIds = newBitSet;
	}
}