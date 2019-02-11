package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.serverpackets.CharSelected
import com.l2kt.gameserver.network.serverpackets.SSQInfo

class CharacterSelected : L2GameClientPacket() {
    private var _charSlot: Int = 0

    private var _unk1: Int = 0 // new in C4
    private var _unk2: Int = 0 // new in C4
    private var _unk3: Int = 0 // new in C4
    private var _unk4: Int = 0 // new in C4

    override fun readImpl() {
        _charSlot = readD()
        _unk1 = readH()
        _unk2 = readD()
        _unk3 = readD()
        _unk4 = readD()
    }

    override fun runImpl() {
        val client = client
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.CHARACTER_SELECT))
            return

        // we should always be able to acquire the lock but if we cant lock then nothing should be done (ie repeated packet)
        if (client.activeCharLock.tryLock()) {
            try {
                // should always be null but if not then this is repeated packet and nothing should be done here
                if (client.activeChar == null) {
                    val info = client.getCharSelectSlot(_charSlot)
                    if (info == null || info.accessLevel < 0)
                        return

                    // Load up character from disk
                    val cha = client.loadCharFromDisk(_charSlot) ?: return

                    cha.client = client
                    client.activeChar = cha
                    cha.setOnlineStatus(true, true)

                    sendPacket(SSQInfo.sendSky())

                    client.state = L2GameClient.GameClientState.IN_GAME

                    sendPacket(CharSelected(cha, client.sessionId!!.playOkID1))
                }
            } finally {
                client.activeCharLock.unlock()
            }
        }
    }
}