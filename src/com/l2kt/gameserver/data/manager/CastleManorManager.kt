package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.CastleManorManager.forEach
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.manor.CropProcure
import com.l2kt.gameserver.model.manor.Seed
import com.l2kt.gameserver.model.manor.SeedProduction
import com.l2kt.gameserver.network.SystemMessageId
import org.w3c.dom.Document
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

/**
 * Loads and stores Manor [Seed]s informations for all [Castle]s, using database and XML informations.
 */
object CastleManorManager : IXmlReader {

    private var _mode = ManorMode.APPROVED

    private var _nextModeChange: Calendar? = null

    private val _seeds = HashMap<Int, Seed>()

    private val _procure = HashMap<Int, MutableList<CropProcure>>()
    private val _procureNext = HashMap<Int, MutableList<CropProcure>>()
    private val _production = HashMap<Int, MutableList<SeedProduction>>()
    private val _productionNext = HashMap<Int, MutableList<SeedProduction>>()

    val isUnderMaintenance: Boolean
        get() = _mode == ManorMode.MAINTENANCE

    val isManorApproved: Boolean
        get() = _mode == ManorMode.APPROVED

    val isModifiablePeriod: Boolean
        get() = _mode == ManorMode.MODIFIABLE

    val currentModeName: String
        get() = _mode.toString()

    val nextModeChange: String
        get() = SimpleDateFormat("dd/MM HH:mm:ss").format(_nextModeChange!!.time)

    val crops: List<Seed>
        get() {
            val seeds = ArrayList<Seed>()
            val cropIds = ArrayList<Int>()
            for (seed in _seeds.values) {
                if (!cropIds.contains(seed.cropId)) {
                    seeds.add(seed)
                    cropIds.add(seed.cropId)
                }
            }
            cropIds.clear()
            return seeds
        }

    val seedIds: Set<Int>
        get() = _seeds.keys

    val cropIds: Set<Int>
        get() = _seeds.values.map{ it.cropId }.toSet()

    enum class ManorMode {
        DISABLED,
        MODIFIABLE,
        MAINTENANCE,
        APPROVED
    }

    init {
        if (Config.ALLOW_MANOR) {
            // Load static data.
            load()

            // Load dynamic data.
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(LOAD_PRODUCTION).use { stProduction ->
                        con.prepareStatement(LOAD_PROCURE).use { stProcure ->
                            for (castle in CastleManager.castles) {
                                val castleId = castle.castleId

                                // Seed production
                                val pCurrent = ArrayList<SeedProduction>()
                                val pNext = ArrayList<SeedProduction>()

                                stProduction.clearParameters()
                                stProduction.setInt(1, castleId)

                                stProduction.executeQuery().use { rs ->
                                    while (rs.next()) {
                                        val sp =
                                            SeedProduction(
                                                rs.getInt("seed_id"),
                                                rs.getInt("amount"),
                                                rs.getInt("price"),
                                                rs.getInt("start_amount")
                                            )
                                        if (rs.getBoolean("next_period"))
                                            pNext.add(sp)
                                        else
                                            pCurrent.add(sp)
                                    }
                                }
                                _production[castleId] = pCurrent
                                _productionNext[castleId] = pNext

                                // Seed procure
                                val current = ArrayList<CropProcure>()
                                val next = ArrayList<CropProcure>()

                                stProcure.clearParameters()
                                stProcure.setInt(1, castleId)

                                stProcure.executeQuery().use { rs ->
                                    while (rs.next()) {
                                        val cp = CropProcure(
                                            rs.getInt("crop_id"),
                                            rs.getInt("amount"),
                                            rs.getInt("reward_type"),
                                            rs.getInt("start_amount"),
                                            rs.getInt("price")
                                        )
                                        if (rs.getBoolean("next_period"))
                                            next.add(cp)
                                        else
                                            current.add(cp)
                                    }
                                }
                                _procure[castleId] = current
                                _procureNext[castleId] = next
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                IXmlReader.LOGGER.error("Error restoring manor data.", e)
            }

            // Set mode and start timer
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val min = currentTime.get(Calendar.MINUTE)
            val maintenanceMin = Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN

            if (hour >= Config.ALT_MANOR_REFRESH_TIME && min >= maintenanceMin || hour < Config.ALT_MANOR_APPROVE_TIME || hour == Config.ALT_MANOR_APPROVE_TIME && min <= Config.ALT_MANOR_APPROVE_MIN)
                _mode = ManorMode.MODIFIABLE
            else if (hour == Config.ALT_MANOR_REFRESH_TIME && min >= Config.ALT_MANOR_REFRESH_MIN && min < maintenanceMin)
                _mode = ManorMode.MAINTENANCE

            // Schedule mode change
            scheduleModeChange()

            // Schedule autosave
            ThreadPool.scheduleAtFixedRate(
                { this.storeMe() },
                Config.ALT_MANOR_SAVE_PERIOD_RATE.toLong(),
                Config.ALT_MANOR_SAVE_PERIOD_RATE.toLong()
            )

            IXmlReader.LOGGER.debug("Current Manor mode is: {}.", _mode.toString())
        } else {

            _mode = ManorMode.DISABLED
            IXmlReader.LOGGER.info("Manor system is deactivated.")
        }
    }

    override fun load() {
        parseFile("./data/xml/seeds.xml")
        IXmlReader.LOGGER.info("Loaded {} seeds.", _seeds.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "seed") { seedNode ->
                val set = parseAttributes(seedNode)
                _seeds[set.getInteger("id")] = Seed(set)
            }
        }
    }

    private fun scheduleModeChange() {
        // Calculate next mode change
        _nextModeChange = Calendar.getInstance()
        _nextModeChange!!.set(Calendar.SECOND, 0)

        when (_mode) {
            CastleManorManager.ManorMode.MODIFIABLE -> {
                _nextModeChange!!.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_APPROVE_TIME)
                _nextModeChange!!.set(Calendar.MINUTE, Config.ALT_MANOR_APPROVE_MIN)
                if (_nextModeChange!!.before(Calendar.getInstance())) {
                    _nextModeChange!!.add(Calendar.DATE, 1)
                }
            }

            CastleManorManager.ManorMode.MAINTENANCE -> {
                _nextModeChange!!.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME)
                _nextModeChange!!.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN)
            }

            CastleManorManager.ManorMode.APPROVED -> {
                _nextModeChange!!.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME)
                _nextModeChange!!.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN)
            }
        }

        // Schedule mode change
        ThreadPool.schedule({ this.changeMode() }, _nextModeChange!!.timeInMillis - System.currentTimeMillis())
    }

    fun changeMode() {
        when (_mode) {
            CastleManorManager.ManorMode.APPROVED -> {
                // Change mode
                _mode = ManorMode.MAINTENANCE

                // Update manor period
                for (castle in CastleManager.castles) {
                    val owner = ClanTable.getClan(castle.ownerId) ?: continue

                    val castleId = castle.castleId
                    val cwh = owner.warehouse

                    for (crop in _procure[castleId].orEmpty()) {
                        if (crop.startAmount > 0) {
                            // Adding bought crops to clan warehouse
                            if (crop.startAmount != crop.amount) {
                                var count = ((crop.startAmount - crop.amount) * 0.9).toInt()
                                if (count < 1 && Rnd.nextInt(99) < 90)
                                    count = 1

                                if (count > 0)
                                    cwh.addItem("Manor", getSeedByCrop(crop.id)!!.matureId, count, null, null)
                            }

                            // Reserved and not used money giving back to treasury
                            if (crop.amount > 0)
                                castle.addToTreasuryNoTax((crop.amount * crop.price).toLong())
                        }
                    }

                    // Change next period to current and prepare next period data
                    val _nextProduction = _productionNext[castleId]
                    val _nextProcure = _procureNext[castleId]

                    _production[castleId] = _nextProduction!!
                    _procure[castleId] = _nextProcure!!

                    if (castle.treasury < getManorCost(castleId, false)) {
                        _productionNext[castleId] = mutableListOf()
                        _procureNext[castleId] = mutableListOf()
                    } else {
                        val production = ArrayList(_nextProduction)
                        for (s in production)
                            s.amount = s.startAmount

                        _productionNext[castleId] = production

                        val procure = ArrayList(_nextProcure)
                        for (cr in procure)
                            cr.amount = cr.startAmount

                        _procureNext[castleId] = procure
                    }
                }

                // Save changes
                storeMe()
            }

            CastleManorManager.ManorMode.MAINTENANCE -> {
                // Notify clan leader about manor mode change
                for (castle in CastleManager.castles) {
                    val owner = ClanTable.getClan(castle.ownerId)
                    if (owner != null) {
                        val clanLeader = owner.leader
                        if (clanLeader != null && clanLeader.isOnline)
                            clanLeader.playerInstance!!.sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED)
                    }
                }
                _mode = ManorMode.MODIFIABLE
            }

            CastleManorManager.ManorMode.MODIFIABLE -> {
                _mode = ManorMode.APPROVED

                for (castle in CastleManager.castles) {
                    val owner = ClanTable.getClan(castle.ownerId) ?: continue

                    var slots = 0
                    val castleId = castle.castleId
                    val cwh = owner.warehouse

                    for (crop in _procureNext[castleId].orEmpty()) {
                        if (crop.startAmount > 0 && cwh.getItemsByItemId(getSeedByCrop(crop.id)!!.matureId) == null)
                            slots++
                    }

                    val manorCost = getManorCost(castleId, true)
                    if (!cwh.validateCapacity(slots) && castle.treasury < manorCost) {
                        _productionNext[castleId]?.clear()
                        _procureNext[castleId]?.clear()

                        // Notify clan leader
                        val clanLeader = owner.leader
                        if (clanLeader != null && clanLeader.isOnline)
                            clanLeader.playerInstance!!.sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION)
                    } else
                        castle.addToTreasuryNoTax(-manorCost)
                }
            }
        }
        scheduleModeChange()

        IXmlReader.LOGGER.debug("Manor mode changed to: {}.", _mode.toString())
    }

    fun setNextSeedProduction(list: MutableList<SeedProduction>, castleId: Int) {
        _productionNext[castleId] = list
    }

    fun setNextCropProcure(list: MutableList<CropProcure>, castleId: Int) {
        _procureNext[castleId] = list
    }

    fun getSeedProduction(castleId: Int, nextPeriod: Boolean): MutableList<SeedProduction> {
        return if (nextPeriod) _productionNext[castleId]!! else _production[castleId]!!
    }

    fun getSeedProduct(castleId: Int, seedId: Int, nextPeriod: Boolean): SeedProduction? {
        for (sp in getSeedProduction(castleId, nextPeriod)) {
            if (sp.id == seedId)
                return sp
        }
        return null
    }

    fun getCropProcure(castleId: Int, nextPeriod: Boolean): MutableList<CropProcure> {
        return if (nextPeriod) _procureNext[castleId]!! else _procure[castleId]!!
    }

    fun getCropProcure(castleId: Int, cropId: Int, nextPeriod: Boolean): CropProcure? {
        for (cp in getCropProcure(castleId, nextPeriod)) {
            if (cp.id == cropId)
                return cp
        }
        return null
    }

    fun getManorCost(castleId: Int, nextPeriod: Boolean): Long {
        val procure = getCropProcure(castleId, nextPeriod)
        val production = getSeedProduction(castleId, nextPeriod)

        var total: Long = 0
        for (seed in production) {
            val s = getSeed(seed.id)
            total += (if (s == null) 1 else s.seedReferencePrice * seed.startAmount).toLong()
        }
        for (crop in procure) {
            total += (crop.price * crop.startAmount).toLong()
        }
        return total
    }

    fun storeMe(): Boolean {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_ALL_PRODUCTS).use { ds ->
                    con.prepareStatement(INSERT_PRODUCT).use { `is` ->
                        con.prepareStatement(DELETE_ALL_PROCURE).use { dp ->
                            con.prepareStatement(INSERT_CROP).use { ip ->
                                // Delete old seeds
                                ds.executeUpdate()

                                // Current production
                                for ((key, value) in _production) {
                                    for (sp in value) {
                                        `is`.setInt(1, key)
                                        `is`.setInt(2, sp.id)
                                        `is`.setLong(3, sp.amount.toLong())
                                        `is`.setLong(4, sp.startAmount.toLong())
                                        `is`.setLong(5, sp.price.toLong())
                                        `is`.setBoolean(6, false)
                                        `is`.addBatch()
                                    }
                                }

                                // Next production
                                for ((key, value) in _productionNext) {
                                    for (sp in value) {
                                        `is`.setInt(1, key)
                                        `is`.setInt(2, sp.id)
                                        `is`.setLong(3, sp.amount.toLong())
                                        `is`.setLong(4, sp.startAmount.toLong())
                                        `is`.setLong(5, sp.price.toLong())
                                        `is`.setBoolean(6, true)
                                        `is`.addBatch()
                                    }
                                }

                                // Execute production batch
                                `is`.executeBatch()

                                // Delete old procure
                                dp.executeUpdate()

                                // Current procure
                                for ((key, value) in _procure) {
                                    for (cp in value) {
                                        ip.setInt(1, key)
                                        ip.setInt(2, cp.id)
                                        ip.setLong(3, cp.amount.toLong())
                                        ip.setLong(4, cp.startAmount.toLong())
                                        ip.setLong(5, cp.price.toLong())
                                        ip.setInt(6, cp.reward)
                                        ip.setBoolean(7, false)
                                        ip.addBatch()
                                    }
                                }

                                // Next procure
                                for ((key, value) in _procureNext) {
                                    for (cp in value) {
                                        ip.setInt(1, key)
                                        ip.setInt(2, cp.id)
                                        ip.setLong(3, cp.amount.toLong())
                                        ip.setLong(4, cp.startAmount.toLong())
                                        ip.setLong(5, cp.price.toLong())
                                        ip.setInt(6, cp.reward)
                                        ip.setBoolean(7, true)
                                        ip.addBatch()
                                    }
                                }

                                // Execute procure batch
                                ip.executeBatch()

                                return true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Unable to store manor data.", e)
            return false
        }

    }

    fun resetManorData(castleId: Int) {
        if (_mode == ManorMode.DISABLED)
            return

        _procure[castleId]?.clear()
        _procureNext[castleId]?.clear()
        _production[castleId]?.clear()
        _productionNext[castleId]?.clear()
    }

    fun getSeedsForCastle(castleId: Int): Set<Seed> {
        return _seeds.values.filter { s -> s.castleId == castleId }.toSet()
    }

    fun getSeed(seedId: Int): Seed? {
        return _seeds[seedId]
    }

    fun getSeedByCrop(cropId: Int, castleId: Int): Seed? {
        return _seeds.values.firstOrNull { s -> s.castleId == castleId && s.cropId == cropId }
    }

    fun getSeedByCrop(cropId: Int): Seed? {
        return _seeds.values.firstOrNull { s -> s.cropId == cropId }
    }

    private const val LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?"
    private const val LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?"

    private const val UPDATE_PRODUCTION =
        "UPDATE castle_manor_production SET amount = ? WHERE castle_id = ? AND seed_id = ? AND next_period = 0"
    private const val UPDATE_PROCURE =
        "UPDATE castle_manor_procure SET amount = ? WHERE castle_id = ? AND crop_id = ? AND next_period = 0"

    private const val DELETE_ALL_PRODUCTS = "DELETE FROM castle_manor_production"
    private const val INSERT_PRODUCT = "INSERT INTO castle_manor_production VALUES (?, ?, ?, ?, ?, ?)"

    private const val DELETE_ALL_PROCURE = "DELETE FROM castle_manor_procure"
    private const val INSERT_CROP = "INSERT INTO castle_manor_procure VALUES (?, ?, ?, ?, ?, ?, ?)"

    fun updateCurrentProduction(castleId: Int, items: Collection<SeedProduction>) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_PRODUCTION).use { ps ->
                    for (sp in items) {
                        ps.setLong(1, sp.amount.toLong())
                        ps.setInt(2, castleId)
                        ps.setInt(3, sp.id)
                        ps.addBatch()
                    }
                    ps.executeBatch()
                }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Unable to store manor data.", e)
        }
    }

    fun updateCurrentProcure(castleId: Int, items: Collection<CropProcure>) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_PROCURE).use { ps ->
                    for (sp in items) {
                        ps.setLong(1, sp.amount.toLong())
                        ps.setInt(2, castleId)
                        ps.setInt(3, sp.id)
                        ps.addBatch()
                    }
                    ps.executeBatch()
                }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Unable to store manor data.", e)
        }
    }
}