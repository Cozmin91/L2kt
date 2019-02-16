package com.l2kt.gameserver.model.manor

class CropProcure(id: Int, amount: Int, val reward: Int, startAmount: Int, price: Int) : SeedProduction(id, amount, price, startAmount)