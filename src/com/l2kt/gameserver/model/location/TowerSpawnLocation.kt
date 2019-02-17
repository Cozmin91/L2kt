package com.l2kt.gameserver.model.location

/**
 * A datatype extending [SpawnLocation], which handles a single Control Tower spawn point and its parameters (such as guards npcId List), npcId to spawn and upgrade level.
 */
class TowerSpawnLocation : SpawnLocation {
    val id: Int
    private val _zoneList: MutableList<Int> = mutableListOf()
    var upgradeLevel: Int = 0

    val zoneList: List<Int>
        get() = _zoneList

    constructor(npcId: Int, location: SpawnLocation) : super(location) {

        id = npcId
    }

    constructor(npcId: Int, location: SpawnLocation, zoneList: Array<String>) : super(location) {

        id = npcId

        for (zoneId in zoneList)
            _zoneList.add(Integer.parseInt(zoneId))
    }
}