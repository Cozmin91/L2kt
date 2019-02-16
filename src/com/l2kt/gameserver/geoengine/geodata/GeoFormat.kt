package com.l2kt.gameserver.geoengine.geodata

enum class GeoFormat constructor(val filename: String) {
    L2J("%d_%d.l2j"),
    L2OFF("%d_%d_conv.dat"),
    L2D("%d_%d.l2d")
}