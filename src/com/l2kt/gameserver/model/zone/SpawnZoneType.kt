package com.l2kt.gameserver.model.zone

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.location.Location
import java.util.*

/**
 * An abstract zone with spawn locations, inheriting [ZoneType] behavior.<br></br>
 * <br></br>
 * Two lazy initialized [List]s can hold [Location]s.
 */
abstract class SpawnZoneType(id: Int) : ZoneType(id) {
    private var _locs: MutableList<Location>? = null
    private var _chaoticLocs: MutableList<Location>? = null

    val locs: List<Location>?
        get() = _locs

    /**
     * @return a random [Location] from _locs [List].
     */
    val randomLoc: Location?
        get() = Rnd[_locs]

    /**
     * @return a random [Location] from _chaoticLocs [List]. If _chaoticLocs isn't initialized, return a random Location from _locs.
     */
    val randomChaoticLoc: Location?
        get() = Rnd[if (_chaoticLocs != null) _chaoticLocs else _locs]

    /**
     * Add a [Location] to either _locs or _chaoticLocs. Initialize the container if not yet initialized.
     * @param loc : The Location to register.
     * @param isChaotic : Set the location on the correct container.
     */
    fun addLoc(loc: Location, isChaotic: Boolean) {
        if (isChaotic) {
            if (_chaoticLocs == null)
                _chaoticLocs = ArrayList()

            _chaoticLocs!!.add(loc)
        } else {
            if (_locs == null)
                _locs = ArrayList()

            _locs!!.add(loc)
        }
    }
}