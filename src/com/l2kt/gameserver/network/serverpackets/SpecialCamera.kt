package com.l2kt.gameserver.network.serverpackets

class SpecialCamera : L2GameServerPacket {
    private val _id: Int
    private val _dist: Int
    private val _yaw: Int
    private val _pitch: Int
    private val _time: Int
    private val _duration: Int
    private val _turn: Int
    private val _rise: Int
    private val _widescreen: Int
    private val _unknown: Int

    constructor(id: Int, dist: Int, yaw: Int, pitch: Int, time: Int, duration: Int) {
        _id = id
        _dist = dist
        _yaw = yaw
        _pitch = pitch
        _time = time
        _duration = duration
        _turn = 0
        _rise = 0
        _widescreen = 0
        _unknown = 0
    }

    constructor(
        id: Int,
        dist: Int,
        yaw: Int,
        pitch: Int,
        time: Int,
        duration: Int,
        turn: Int,
        rise: Int,
        widescreen: Int,
        unk: Int
    ) {
        _id = id
        _dist = dist
        _yaw = yaw
        _pitch = pitch
        _time = time
        _duration = duration
        _turn = turn
        _rise = rise
        _widescreen = widescreen
        _unknown = unk
    }

    public override fun writeImpl() {
        writeC(0xc7)
        writeD(_id)
        writeD(_dist)
        writeD(_yaw)
        writeD(_pitch)
        writeD(_time)
        writeD(_duration)
        writeD(_turn)
        writeD(_rise)
        writeD(_widescreen)
        writeD(_unknown)
    }
}