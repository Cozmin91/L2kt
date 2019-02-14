package com.l2kt.gameserver.model

data class ItemRequest(var objectId: Int, var count: Int, var price: Int, var itemId: Int = 0)