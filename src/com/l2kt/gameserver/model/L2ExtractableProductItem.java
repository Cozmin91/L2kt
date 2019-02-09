package com.l2kt.gameserver.model;

import java.util.List;

import com.l2kt.gameserver.model.holder.IntIntHolder;

/**
 * @author -Nemesiss-, Zoey76
 */
public class L2ExtractableProductItem
{
	private final List<IntIntHolder> _items;
	private final double _chance;
	
	public L2ExtractableProductItem(List<IntIntHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}
	
	public List<IntIntHolder> getItems()
	{
		return _items;
	}
	
	public double getChance()
	{
		return _chance;
	}
}