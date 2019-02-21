package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.geoengine.geodata.IGeoObject
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI
import com.l2kt.gameserver.model.actor.ai.type.DoorAI
import com.l2kt.gameserver.model.actor.stat.DoorStat
import com.l2kt.gameserver.model.actor.status.DoorStatus
import com.l2kt.gameserver.model.actor.template.DoorTemplate
import com.l2kt.gameserver.model.actor.template.DoorTemplate.DoorType
import com.l2kt.gameserver.model.actor.template.DoorTemplate.OpenType
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*

class Door(objectId: Int, template: DoorTemplate) : Creature(objectId, template), IGeoObject {
    val castle: Castle?
    private val _clanHall: ClanHall?

    /**
     * Returns true, when [Door] is opened.
     * @return boolean : True, when opened.
     */
    var isOpened: Boolean = false
        private set

    override val isAttackable: Boolean
        get() = castle != null && castle.siege.isInProgress

    override val geoX: Int
        get() = template.geoX

    override val geoY: Int
        get() = template.geoY

    override val geoZ: Int
        get() = template.geoZ

    override val height: Int
        get() = template.collisionHeight.toInt()

    override val objectGeoData: Array<ByteArray>
        get() = template.geoData

    /**
     * Returns the [Door] ID.
     * @return int : Returns the ID.
     */
    val doorId: Int
        get() = template.id

    /**
     * Returns true, when [Door] can be unlocked and opened.
     * @return boolean : True, when can be unlocked and opened.
     */
    val isUnlockable: Boolean
        get() = template.openType === OpenType.SKILL

    /**
     * Returns the actual damage of the door.
     * @return int : Door damage.
     */
    val damage: Int
        get() = Math.max(0, Math.min(6, 6 - Math.ceil(currentHp / maxHp * 6).toInt()))

    init {

        // assign door to a castle
        castle = CastleManager.getCastleById(template.castle)
        castle?.doors?.add(this)

        // assign door to a clan hall
        _clanHall = ClanHallManager.getNearbyClanHall(template.posX, template.posY, 500)
        _clanHall?.doors?.add(this)

        // temporarily set opposite state to initial state (will be set correctly by onSpawn)
        isOpened = !getTemplate().isOpened

        // set name
        name = template.name
    }

    override fun getAI(): CreatureAI {
        return _ai ?: synchronized(this) {
            if (_ai == null)
                _ai = DoorAI(this)

            return _ai
        }
    }

    override fun initCharStat() {
        stat = DoorStat(this)
    }

    override fun getStat(): DoorStat {
        return super.getStat() as DoorStat
    }

    override fun initCharStatus() {
        status = DoorStatus(this)
    }

    override fun getStatus(): DoorStatus {
        return super.getStatus() as DoorStatus
    }

    override fun getTemplate(): DoorTemplate {
        return super.getTemplate() as DoorTemplate
    }

    override fun addFuncsToNewCharacter() {}

    override fun getLevel(): Int {
        return template.level
    }

    override fun updateAbnormalEffect() {}

    override fun getActiveWeaponInstance(): ItemInstance? {
        return null
    }

    override fun getActiveWeaponItem(): Weapon? {
        return null
    }

    override fun getSecondaryWeaponInstance(): ItemInstance? {
        return null
    }

    override fun getSecondaryWeaponItem(): Weapon? {
        return null
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        // Doors can't be attacked by NPCs
        if (attacker !is Playable)
            return false

        if (isUnlockable)
            return true

        // Attackable during siege by attacker only
        val isCastle = castle != null && castle.siege.isInProgress
        if (isCastle) {
            val clan = attacker.actingPlayer!!.clan
            if (clan != null && clan.clanId == castle!!.ownerId)
                return false
        }
        return isCastle
    }

    override fun onForcedAttack(player: Player) {
        onAction(player)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this) {
            player.target = this
            player.sendPacket(DoorStatusUpdate(this))
        } else {
            if (isAutoAttackable(player)) {
                if (Math.abs(player.z - z) < 400)
                // this max heigth difference might need some tweaking
                    player.ai.setIntention(CtrlIntention.ATTACK, this)
            } else if (!isInsideRadius(player, Npc.INTERACTION_DISTANCE, false, false))
                player.ai.setIntention(CtrlIntention.INTERACT, this)
            else if (player.clan != null && _clanHall != null && player.clanId == _clanHall.ownerId) {
                player.setRequestedGate(this)
                player.sendPacket(ConfirmDlg(if (!isOpened) 1140 else 1141))
                player.sendPacket(ActionFailed.STATIC_PACKET)
            } else
            // Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
                player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    override fun onActionShift(player: Player) {
        if (player.isGM) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/admin/doorinfo.htm")
            html.replace("%name%", name)
            html.replace("%objid%", objectId)
            html.replace("%doorid%", template.id)
            html.replace("%doortype%", template.type.toString())
            html.replace("%doorlvl%", template.level)
            html.replace("%castle%", castle?.name ?: "none")
            html.replace("%opentype%", template.openType.toString())
            html.replace("%initial%", if (template.isOpened) "Opened" else "Closed")
            html.replace("%ot%", template.openTime)
            html.replace("%ct%", template.closeTime)
            html.replace("%rt%", template.randomTime)
            html.replace("%controlid%", template.triggerId)
            html.replace("%hp%", currentHp.toInt())
            html.replace("%hpmax%", maxHp)
            html.replace("%hpratio%", stat.upgradeHpRatio)
            html.replace("%pdef%", getPDef(null))
            html.replace("%mdef%", getMDef(null, null))
            html.replace("%spawn%", "$x $y $z")
            html.replace("%height%", template.collisionHeight)
            player.sendPacket(html)
        }

        if (player.target !== this) {
            player.target = this

            if (isAutoAttackable(player))
                player.sendPacket(DoorStatusUpdate(this))
        } else
            player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun reduceCurrentHp(damage: Double, attacker: Creature, awake: Boolean, isDOT: Boolean, skill: L2Skill?) {
        // HPs can only be reduced during castle sieges.
        if (!(castle != null && castle.siege.isInProgress))
            return

        // Only siege summons can damage walls and use skills on walls/doors.
        if (attacker !is SiegeSummon && (template.type === DoorType.WALL || skill != null))
            return

        super.reduceCurrentHp(damage, attacker, awake, isDOT, skill)
    }

    override fun reduceCurrentHpByDOT(i: Double, attacker: Creature, skill: L2Skill) {
        // Doors can't be damaged by DOTs.
    }

    override fun onSpawn() {
        changeState(template.isOpened, false)

        super.onSpawn()
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        if (!isOpened)
            GeoEngine.removeGeoObject(this)

        if (castle != null && castle.siege.isInProgress)
            castle.siege.announceToPlayers(
                SystemMessage.getSystemMessage(if (template.type === DoorType.WALL) SystemMessageId.CASTLE_WALL_DAMAGED else SystemMessageId.CASTLE_GATE_BROKEN_DOWN),
                false
            )

        return true
    }

    override fun doRevive() {
        isOpened = template.isOpened

        if (!isOpened)
            GeoEngine.addGeoObject(this)

        super.doRevive()
    }

    override fun broadcastStatusUpdate() {
        broadcastPacket(DoorStatusUpdate(this))
    }

    override fun moveToLocation(x: Int, y: Int, z: Int, offset: Int) {}

    override fun stopMove(loc: SpawnLocation) {}

    @Synchronized
    override fun doAttack(target: Creature) {
    }

    override fun doCast(skill: L2Skill) {}

    override fun sendInfo(activeChar: Player) {
        activeChar.sendPacket(DoorInfo(this))
        activeChar.sendPacket(DoorStatusUpdate(this))
    }

    override fun getCollisionHeight(): Double {
        return template.collisionHeight / 2
    }

    /**
     * Opens the [Door].
     */
    fun openMe() {
        // open door using external action
        changeState(true, false)
    }

    /**
     * Closes the [Door].
     */
    fun closeMe() {
        // close door using external action
        changeState(false, false)
    }

    /**
     * Open/closes the [Door], triggers other [Door] and schedules automatic open/close task.
     * @param open : Requested status change.
     * @param triggered : The status change was triggered by other.
     */
    fun changeState(open: Boolean, triggered: Boolean) {
        // door is dead or already in requested state, return
        if (isDead || isOpened == open)
            return

        // change door state and broadcast change
        isOpened = open
        if (open)
            GeoEngine.removeGeoObject(this)
        else
            GeoEngine.addGeoObject(this)

        broadcastStatusUpdate()

        // door controls another door
        val triggerId = template.triggerId
        if (triggerId > 0) {
            // get door and trigger state change
            val door = DoorData.getDoor(triggerId)
            door?.changeState(open, true)
        }

        // request is not triggered
        if (!triggered) {
            // calculate time for automatic state change
            var time = if (open) template.closeTime else template.openTime
            if (template.randomTime > 0)
                time += Rnd[template.randomTime]

            // try to schedule automatic state change
            if (time > 0)
                ThreadPool.schedule(Runnable{ changeState(!open, false) }, (time * 1000).toLong())
        }
    }
}