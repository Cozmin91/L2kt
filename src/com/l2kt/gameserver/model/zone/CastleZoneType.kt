package com.l2kt.gameserver.model.zone

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.model.entity.Castle

/**
 * A zone type extending [ZoneType] used for castle zones.
 */
abstract class CastleZoneType protected constructor(id: Int) : ZoneType(id) {
    private var _castleId: Int = 0
    private var _castle: Castle? = null

    var isEnabled: Boolean = false

    val castle: Castle?
        get() {
            if (_castleId > 0 && _castle == null)
                _castle = CastleManager.getInstance().getCastleById(_castleId)

            return _castle
        }

    override fun setParameter(name: String, value: String) {
        if (name == "castleId")
            _castleId = Integer.parseInt(value)
        else
            super.setParameter(name, value)
    }
}