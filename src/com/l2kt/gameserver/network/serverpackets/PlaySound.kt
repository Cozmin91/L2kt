package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.location.Location

class PlaySound : L2GameServerPacket {
    private val _soundType: Int
    private val _soundFile: String
    private val _bindToObject: Boolean
    private val _objectId: Int
    private val _location: Location
    private val _delay: Int

    /**
     * Used for static sound.
     * @param soundFile : The name of the sound file.
     */
    constructor(soundFile: String) {
        _soundType = 0
        _soundFile = soundFile
        _bindToObject = false
        _objectId = 0
        _location = Location.DUMMY_LOC
        _delay = 0
    }

    /**
     * Used for static sound.
     * @param soundType : The type of sound file. 0 - Sound, 1 - Music, 2 - Voice
     * @param soundFile : The name of the sound file.
     */
    constructor(soundType: Int, soundFile: String) {
        _soundType = soundType
        _soundFile = soundFile
        _bindToObject = false
        _objectId = 0
        _location = Location.DUMMY_LOC
        _delay = 0
    }

    /**
     * Play the sound file in the client. We use a [WorldObject] as parameter, notably to find the position of the sound.
     * @param soundType : The type of sound file. 0 - Sound, 1 - Music, 2 - Voice
     * @param soundFile : The name of the sound file.
     * @param object : The object to use.
     */
    constructor(soundType: Int, soundFile: String, `object`: WorldObject) {
        _soundType = soundType
        _soundFile = soundFile
        _bindToObject = true
        _objectId = `object`.objectId
        _location = `object`.position
        _delay = 0
    }

    /**
     * Play the sound file in the client. All parameters can be set.
     * @param soundType : The type of sound file. 0 - Sound, 1 - Music, 2 - Voice
     * @param soundFile : The name of the sound file.
     * @param bindToObject - true, if sound file binded for some object.
     * @param objectId - object ID of caller. 0 - for quest, tutorial, etc.
     * @param location - Location of binded object.
     * @param delay - playing time
     */
    constructor(
        soundType: Int,
        soundFile: String,
        bindToObject: Boolean,
        objectId: Int,
        location: Location,
        delay: Int
    ) {
        _soundType = soundType
        _soundFile = soundFile
        _bindToObject = bindToObject
        _objectId = objectId
        _location = location
        _delay = delay
    }

    override fun writeImpl() {
        writeC(0x98)
        writeD(_soundType)
        writeS(_soundFile)
        writeD(if (_bindToObject) 1 else 0)
        writeD(_objectId)
        writeD(_location.x)
        writeD(_location.y)
        writeD(_location.z)
        writeD(_delay)
    }
}