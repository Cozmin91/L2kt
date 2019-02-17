package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.manager.CursedWeaponManager.forEach
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.*
import com.l2kt.gameserver.model.entity.CursedWeapon
import com.l2kt.gameserver.model.item.instance.ItemInstance
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * Loads and store [CursedWeapon]s. A cursed weapon is a feature involving the drop of a powerful weapon, which stages on player kills and give powerful stats.
 *
 *  * <u>dropRate :</u> the drop rate used by the monster to drop the item. Default : 1/1000000
 *  * <u>duration :</u> the overall lifetime duration in hours. Default : 72 hours (3 days)
 *  * <u>durationLost :</u> the task time duration, launched when someone pickups a cursed weapon. Renewed when the owner kills a player. Default : 24 hours.
 *  * <u>disapearChance :</u> chance to dissapear when the owner dies. Default : 50%
 *  * <u>stageKills :</u> the number used to calculate random number of needed kills to rank up the cursed weapon. That number is used as a base, it takes a random number between 50% and 150% of that value. Default : 10
 *
 */
object CursedWeaponManager : IXmlReader {
    private val _cursedWeapons = HashMap<Int, CursedWeapon>()

    val cursedWeapons: Collection<CursedWeapon>
        get() = _cursedWeapons.values

    val cursedWeaponsIds: Set<Int>
        get() = _cursedWeapons.keys

    init {
        if (!Config.ALLOW_CURSED_WEAPONS) {
            IXmlReader.LOGGER.info("Cursed weapons loading is skipped.")
        }else{
            load()
        }
    }

    override fun load() {
        parseFile("./data/xml/cursedWeapons.xml")
        IXmlReader.LOGGER.info("Loaded {} cursed weapons.", _cursedWeapons.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "item") { itemNode ->
                val set = parseAttributes(itemNode)
                _cursedWeapons[set.getInteger("id")] = CursedWeapon(set)
            }
        }
    }

    /**
     * Ends the life of existing [CursedWeapon]s, clear the map, and reload content.
     */
    fun reload() {
        for (cw in _cursedWeapons.values)
            cw.endOfLife()

        _cursedWeapons.clear()

        load()
    }

    fun isCursed(itemId: Int): Boolean {
        return _cursedWeapons.containsKey(itemId)
    }

    fun getCursedWeapon(itemId: Int): CursedWeapon? {
        return _cursedWeapons[itemId]
    }

    /**
     * Checks if a [CursedWeapon] can drop. Verify if it is already active and if the [Attackable] you killed was a valid candidate.
     * @param attackable : The target to test.
     * @param player : The player who killed the Attackable.
     */
    @Synchronized
    fun checkDrop(attackable: Attackable, player: Player) {
        if (attackable is SiegeGuard || attackable is RiftInvader || attackable is FestivalMonster || attackable is GrandBoss || attackable is FeedableBeast)
            return

        for (cw in _cursedWeapons.values) {
            if (cw.isActive)
                continue

            if (cw.checkDrop(attackable, player))
                break
        }
    }

    /**
     * Assimilate a [CursedWeapon] if you already possess one (which ranks up possessed weapon), or activate it otherwise.
     * @param player : The player to test.
     * @param item : The item player picked up.
     */
    fun activate(player: Player, item: ItemInstance) {
        val cw = _cursedWeapons[item.itemId] ?: return

        // Can't own 2 cursed swords ; ranks the existing one, and ends the life of the newly obtained cursed weapon.
        if (player.isCursedWeaponEquipped) {
            // Ranks up the existing cursed weapon.
            _cursedWeapons[player.cursedWeaponEquippedId]?.rankUp()

            // Setup the player in order to drop the weapon from inventory.
            cw.player = player

            // Erase the newly obtained cursed weapon.
            cw.endOfLife()
        } else
            cw.activate(player, item)
    }

    /**
     * Retrieve the [CursedWeapon] based on its itemId and handle the drop process.
     * @param itemId : The cursed weapon itemId.
     * @param killer : The creature who killed the monster.
     */
    fun drop(itemId: Int, killer: Creature) {
        val cw = _cursedWeapons[itemId] ?: return

        cw.dropIt(killer)
    }

    /**
     * Retrieve the [CursedWeapon] based on its itemId and increase its kills.
     * @param itemId : The cursed weapon itemId.
     */
    fun increaseKills(itemId: Int) {
        val cw = _cursedWeapons[itemId] ?: return

        cw.increaseKills()
    }

    fun getCurrentStage(itemId: Int): Int {
        val cw = _cursedWeapons[itemId]
        return cw?.currentStage ?: 0
    }

    /**
     * This method is used on EnterWorld to check if the [Player] is equipped with a [CursedWeapon].<br></br>
     * If so, we set the player and item references on the cursed weapon, then we reward cursed skills to that player.
     * @param player : The player to check.
     */
    fun checkPlayer(player: Player?) {
        if (player == null)
            return

        for (cw in _cursedWeapons.values) {
            if (cw.isActivated && player.objectId == cw.playerId) {
                cw.player = player

                val item = player.inventory!!.getItemByItemId(cw.itemId)
                item?.let { cw.setItem(it) }
                cw.giveDemonicSkills()

                player.cursedWeaponEquippedId = cw.itemId
                break
            }
        }
    }
}