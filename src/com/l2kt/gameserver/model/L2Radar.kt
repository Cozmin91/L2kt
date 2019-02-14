package com.l2kt.gameserver.model

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.RadarControl
import java.util.*

/**
 * @author dalrond
 */
class L2Radar(private val _player: Player) {
    private val _markers: MutableList<RadarMarker>

    init {
        _markers = ArrayList()
    }

    // Add a marker to player's radar
    fun addMarker(x: Int, y: Int, z: Int) {
        val newMarker = RadarMarker(x, y, z)

        _markers.add(newMarker)
        _player.sendPacket(RadarControl(2, 2, x, y, z))
        _player.sendPacket(RadarControl(0, 1, x, y, z))
    }

    // Remove a marker from player's radar
    fun removeMarker(x: Int, y: Int, z: Int) {
        val newMarker = RadarMarker(x, y, z)

        _markers.remove(newMarker)
        _player.sendPacket(RadarControl(1, 1, x, y, z))
    }

    fun removeAllMarkers() {
        for (tempMarker in _markers)
            _player.sendPacket(RadarControl(2, 2, tempMarker._x, tempMarker._y, tempMarker._z))

        _markers.clear()
    }

    fun loadMarkers() {
        _player.sendPacket(RadarControl(2, 2, _player.x, _player.y, _player.z))
        for (tempMarker in _markers)
            _player.sendPacket(RadarControl(0, 1, tempMarker._x, tempMarker._y, tempMarker._z))
    }

    class RadarMarker {
        // Simple class to model radar points.
        var _type: Int = 0
        var _x: Int = 0
        var _y: Int = 0
        var _z: Int = 0

        constructor(type: Int, x: Int, y: Int, z: Int) {
            _type = type
            _x = x
            _y = y
            _z = z
        }

        constructor(x: Int, y: Int, z: Int) {
            _type = 1
            _x = x
            _y = y
            _z = z
        }

        /**
         * @see java.lang.Object.hashCode
         */
        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + _type
            result = prime * result + _x
            result = prime * result + _y
            result = prime * result + _z
            return result
        }

        /**
         * @see java.lang.Object.equals
         */
        override fun equals(obj: Any?): Boolean {
            if (this === obj)
                return true

            if (obj == null)
                return false

            if (obj !is RadarMarker)
                return false

            val other = obj as RadarMarker?
            if (_type != other!!._type)
                return false

            if (_x != other._x)
                return false

            if (_y != other._y)
                return false

            return _z == other._z

        }
    }
}