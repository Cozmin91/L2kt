package com.l2kt.loginserver.network

import com.l2kt.Config
import com.l2kt.loginserver.network.serverpackets.LoginOk
import com.l2kt.loginserver.network.serverpackets.PlayOk

/**
 *
 *
 * This class is used to represent session keys used by the client to authenticate in the gameserver
 *
 *
 *
 * A SessionKey is made up of two 8 bytes keys. One is send in the [LoginOk] packet and the other is sent in [PlayOk]
 *
 */
class SessionKey(var loginOkID1: Int, var loginOkID2: Int, var playOkID1: Int, var playOkID2: Int) {

    override fun toString(): String {
        return "PlayOk: $playOkID1 $playOkID2 LoginOk:$loginOkID1 $loginOkID2"
    }

    fun checkLoginPair(loginOk1: Int, loginOk2: Int): Boolean {
        return loginOkID1 == loginOk1 && loginOkID2 == loginOk2
    }

    /**
     * Only checks the PlayOk part of the session key if server doesnt show the licence when player logs in.
     * @param key
     * @return true if keys are equal.
     */
    fun equals(key: SessionKey): Boolean {
        // when server doesnt show licence it deosnt send the LoginOk packet, client doesnt have this part of the key then.
        return if (Config.SHOW_LICENCE) playOkID1 == key.playOkID1 && loginOkID1 == key.loginOkID1 && playOkID2 == key.playOkID2 && loginOkID2 == key.loginOkID2 else playOkID1 == key.playOkID1 && playOkID2 == key.playOkID2

    }
}