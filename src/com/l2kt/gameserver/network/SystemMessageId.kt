package com.l2kt.gameserver.network

import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.lang.reflect.Modifier
import java.util.*

class SystemMessageId private constructor(val id: Int) {
    var name: String? = null
        private set
    private var _params: Byte = 0
    var staticSystemMessage: SystemMessage? = null

    var paramCount: Int
        get() = _params.toInt()
        set(params) {
            if (params < 0)
                throw IllegalArgumentException("Invalid negative param count: $params")

            if (params > 10)
                throw IllegalArgumentException("Maximum param count exceeded: $params")

            if (params != 0)
                staticSystemMessage = null

            _params = params.toByte()
        }

    override fun toString(): String {
        return "SM[$id:$name]"
    }

    companion object {
        private val LOGGER = CLogger(SystemMessageId::class.java.name)

        val EMPTY_ARRAY = arrayOfNulls<SystemMessageId>(0)

        /**
         * ID: 0<br></br>
         * Message: You have been disconnected from the server.
         */
        val YOU_HAVE_BEEN_DISCONNECTED: SystemMessageId = SystemMessageId(0)

        /**
         * ID: 1<br></br>
         * Message: The server will be coming down in $1 seconds. Please find a safe place to log out.
         */
        val THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS: SystemMessageId = SystemMessageId(1)

        /**
         * ID: 2<br></br>
         * Message: $s1 does not exist.
         */
        val S1_DOES_NOT_EXIST: SystemMessageId = SystemMessageId(2)

        /**
         * ID: 3<br></br>
         * Message: $s1 is not currently logged in.
         */
        val S1_IS_NOT_ONLINE: SystemMessageId = SystemMessageId(id = 3)

        /**
         * ID: 4<br></br>
         * Message: You cannot ask yourself to apply to a clan.
         */
        val CANNOT_INVITE_YOURSELF: SystemMessageId = SystemMessageId(4)

        /**
         * ID: 5<br></br>
         * Message: $s1 already exists.
         */
        val S1_ALREADY_EXISTS: SystemMessageId = SystemMessageId(5)

        /**
         * ID: 6<br></br>
         * Message: $s1 does not exist
         */
        val S1_DOES_NOT_EXIST2: SystemMessageId = SystemMessageId(6)

        /**
         * ID: 7<br></br>
         * Message: You are already a member of $s1.
         */
        val ALREADY_MEMBER_OF_S1: SystemMessageId

        /**
         * ID: 8<br></br>
         * Message: You are working with another clan.
         */
        val YOU_ARE_WORKING_WITH_ANOTHER_CLAN: SystemMessageId

        /**
         * ID: 9<br></br>
         * Message: $s1 is not a clan leader.
         */
        val S1_IS_NOT_A_CLAN_LEADER: SystemMessageId

        /**
         * ID: 10<br></br>
         * Message: $s1 is working with another clan.
         */
        val S1_WORKING_WITH_ANOTHER_CLAN: SystemMessageId

        /**
         * ID: 11<br></br>
         * Message: There are no applicants for this clan.
         */
        val NO_APPLICANTS_FOR_THIS_CLAN: SystemMessageId

        /**
         * ID: 12<br></br>
         * Message: The applicant information is incorrect.
         */
        val APPLICANT_INFORMATION_INCORRECT: SystemMessageId

        /**
         * ID: 13<br></br>
         * Message: Unable to disperse: your clan has requested to participate in a castle siege.
         */
        val CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE: SystemMessageId

        /**
         * ID: 14<br></br>
         * Message: Unable to disperse: your clan owns one or more castles or hideouts.
         */
        val CANNOT_DISSOLVE_CAUSE_CLAN_OWNS_CASTLES_HIDEOUTS: SystemMessageId

        /**
         * ID: 15<br></br>
         * Message: You are in siege.
         */
        val YOU_ARE_IN_SIEGE: SystemMessageId

        /**
         * ID: 16<br></br>
         * Message: You are not in siege.
         */
        val YOU_ARE_NOT_IN_SIEGE: SystemMessageId

        /**
         * ID: 17<br></br>
         * Message: The castle siege has begun.
         */
        val CASTLE_SIEGE_HAS_BEGUN: SystemMessageId

        /**
         * ID: 18<br></br>
         * Message: The castle siege has ended.
         */
        val CASTLE_SIEGE_HAS_ENDED: SystemMessageId

        /**
         * ID: 19<br></br>
         * Message: There is a new Lord of the castle!
         */
        val NEW_CASTLE_LORD: SystemMessageId

        /**
         * ID: 20<br></br>
         * Message: The gate is being opened.
         */
        val GATE_IS_OPENING: SystemMessageId

        /**
         * ID: 21<br></br>
         * Message: The gate is being destroyed.
         */
        val GATE_IS_DESTROYED: SystemMessageId

        /**
         * ID: 22<br></br>
         * Message: Your target is out of range.
         */
        val TARGET_TOO_FAR: SystemMessageId

        /**
         * ID: 23<br></br>
         * Message: Not enough HP.
         */
        val NOT_ENOUGH_HP: SystemMessageId

        /**
         * ID: 24<br></br>
         * Message: Not enough MP.
         */
        val NOT_ENOUGH_MP: SystemMessageId

        /**
         * ID: 25<br></br>
         * Message: Rejuvenating HP.
         */
        val REJUVENATING_HP: SystemMessageId

        /**
         * ID: 26<br></br>
         * Message: Rejuvenating MP.
         */
        val REJUVENATING_MP: SystemMessageId

        /**
         * ID: 27<br></br>
         * Message: Your casting has been interrupted.
         */
        val CASTING_INTERRUPTED: SystemMessageId

        /**
         * ID: 28<br></br>
         * Message: You have obtained $s1 adena.
         */
        val YOU_PICKED_UP_S1_ADENA: SystemMessageId

        /**
         * ID: 29<br></br>
         * Message: You have obtained $s2 $s1.
         */
        val YOU_PICKED_UP_S2_S1: SystemMessageId

        /**
         * ID: 30<br></br>
         * Message: You have obtained $s1.
         */
        val YOU_PICKED_UP_S1: SystemMessageId

        /**
         * ID: 31<br></br>
         * Message: You cannot move while sitting.
         */
        val CANT_MOVE_SITTING: SystemMessageId

        /**
         * ID: 32<br></br>
         * Message: You are unable to engage in combat. Please go to the nearest restart point.
         */
        val UNABLE_COMBAT_PLEASE_GO_RESTART: SystemMessageId

        /**
         * ID: 32<br></br>
         * Message: You cannot move while casting.
         */
        val CANT_MOVE_CASTING: SystemMessageId

        /**
         * ID: 34<br></br>
         * Message: Welcome to the World of Lineage II.
         */
        val WELCOME_TO_LINEAGE: SystemMessageId

        /**
         * ID: 35<br></br>
         * Message: You hit for $s1 damage
         */
        val YOU_DID_S1_DMG: SystemMessageId

        /**
         * ID: 36<br></br>
         * Message: $s1 hit you for $s2 damage.
         */
        val S1_GAVE_YOU_S2_DMG: SystemMessageId

        /**
         * ID: 37<br></br>
         * Message: $s1 hit you for $s2 damage.
         */
        val S1_GAVE_YOU_S2_DMG2: SystemMessageId

        /**
         * ID: 41<br></br>
         * Message: You carefully nock an arrow.
         */
        val GETTING_READY_TO_SHOOT_AN_ARROW: SystemMessageId

        /**
         * ID: 42<br></br>
         * Message: You have avoided $s1's attack.
         */
        val AVOIDED_S1_ATTACK: SystemMessageId

        /**
         * ID: 43<br></br>
         * Message: You have missed.
         */
        val MISSED_TARGET: SystemMessageId

        /**
         * ID: 44<br></br>
         * Message: Critical hit!
         */
        val CRITICAL_HIT: SystemMessageId

        /**
         * ID: 45<br></br>
         * Message: You have earned $s1 experience.
         */
        val EARNED_S1_EXPERIENCE: SystemMessageId

        /**
         * ID: 46<br></br>
         * Message: You use $s1.
         */
        val USE_S1: SystemMessageId

        /**
         * ID: 47<br></br>
         * Message: You begin to use a(n) $s1.
         */
        val BEGIN_TO_USE_S1: SystemMessageId

        /**
         * ID: 48<br></br>
         * Message: $s1 is not available at this time: being prepared for reuse.
         */
        val S1_PREPARED_FOR_REUSE: SystemMessageId

        /**
         * ID: 49<br></br>
         * Message: You have equipped your $s1.
         */
        val S1_EQUIPPED: SystemMessageId

        /**
         * ID: 50<br></br>
         * Message: Your target cannot be found.
         */
        val TARGET_CANT_FOUND: SystemMessageId

        /**
         * ID: 51<br></br>
         * Message: You cannot use this on yourself.
         */
        val CANNOT_USE_ON_YOURSELF: SystemMessageId

        /**
         * ID: 52<br></br>
         * Message: You have earned $s1 adena.
         */
        val EARNED_S1_ADENA: SystemMessageId

        /**
         * ID: 53<br></br>
         * Message: You have earned $s2 $s1(s).
         */
        val EARNED_S2_S1_S: SystemMessageId

        /**
         * ID: 54<br></br>
         * Message: You have earned $s1.
         */
        val EARNED_ITEM_S1: SystemMessageId

        /**
         * ID: 55<br></br>
         * Message: You have failed to pick up $s1 adena.
         */
        val FAILED_TO_PICKUP_S1_ADENA: SystemMessageId

        /**
         * ID: 56<br></br>
         * Message: You have failed to pick up $s1.
         */
        val FAILED_TO_PICKUP_S1: SystemMessageId

        /**
         * ID: 57<br></br>
         * Message: You have failed to pick up $s2 $s1(s).
         */
        val FAILED_TO_PICKUP_S2_S1_S: SystemMessageId

        /**
         * ID: 58<br></br>
         * Message: You have failed to earn $s1 adena.
         */
        val FAILED_TO_EARN_S1_ADENA: SystemMessageId

        /**
         * ID: 59<br></br>
         * Message: You have failed to earn $s1.
         */
        val FAILED_TO_EARN_S1: SystemMessageId

        /**
         * ID: 60<br></br>
         * Message: You have failed to earn $s2 $s1(s).
         */
        val FAILED_TO_EARN_S2_S1_S: SystemMessageId

        /**
         * ID: 61<br></br>
         * Message: Nothing happened.
         */
        val NOTHING_HAPPENED: SystemMessageId

        /**
         * ID: 62<br></br>
         * Message: Your $s1 has been successfully enchanted.
         */
        val S1_SUCCESSFULLY_ENCHANTED: SystemMessageId

        /**
         * ID: 63<br></br>
         * Message: Your +$S1 $S2 has been successfully enchanted.
         */
        val S1_S2_SUCCESSFULLY_ENCHANTED: SystemMessageId

        /**
         * ID: 64<br></br>
         * Message: The enchantment has failed! Your $s1 has been crystallized.
         */
        val ENCHANTMENT_FAILED_S1_EVAPORATED: SystemMessageId

        /**
         * ID: 65<br></br>
         * Message: The enchantment has failed! Your +$s1 $s2 has been crystallized.
         */
        val ENCHANTMENT_FAILED_S1_S2_EVAPORATED: SystemMessageId

        /**
         * ID: 66<br></br>
         * Message: $s1 is inviting you to join a party. Do you accept?
         */
        val S1_INVITED_YOU_TO_PARTY: SystemMessageId

        /**
         * ID: 67<br></br>
         * Message: $s1 has invited you to the join the clan, $s2. Do you wish to join?
         */
        val S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2: SystemMessageId

        /**
         * ID: 68<br></br>
         * Message: Would you like to withdraw from the $s1 clan? If you leave, you will have to wait at least a day before joining another clan.
         */
        val WOULD_YOU_LIKE_TO_WITHDRAW_FROM_THE_S1_CLAN: SystemMessageId

        /**
         * ID: 69<br></br>
         * Message: Would you like to dismiss $s1 from the clan? If you do so, you will have to wait at least a day before accepting a new member.
         */
        val WOULD_YOU_LIKE_TO_DISMISS_S1_FROM_THE_CLAN: SystemMessageId

        /**
         * ID: 70<br></br>
         * Message: Do you wish to disperse the clan, $s1?
         */
        val DO_YOU_WISH_TO_DISPERSE_THE_CLAN_S1: SystemMessageId

        /**
         * ID: 71<br></br>
         * Message: How many of your $s1(s) do you wish to discard?
         */
        val HOW_MANY_S1_DISCARD: SystemMessageId

        /**
         * ID: 72<br></br>
         * Message: How many of your $s1(s) do you wish to move?
         */
        val HOW_MANY_S1_MOVE: SystemMessageId

        /**
         * ID: 73<br></br>
         * Message: How many of your $s1(s) do you wish to destroy?
         */
        val HOW_MANY_S1_DESTROY: SystemMessageId

        /**
         * ID: 74<br></br>
         * Message: Do you wish to destroy your $s1?
         */
        val WISH_DESTROY_S1: SystemMessageId

        /**
         * ID: 75<br></br>
         * Message: ID does not exist.
         */
        val ID_NOT_EXIST: SystemMessageId

        /**
         * ID: 76<br></br>
         * Message: Incorrect password.
         */
        val INCORRECT_PASSWORD: SystemMessageId

        /**
         * ID: 77<br></br>
         * Message: You cannot create another character. Please delete the existing character and try again.
         */
        val CANNOT_CREATE_CHARACTER: SystemMessageId

        /**
         * ID: 78<br></br>
         * Message: When you delete a character, any items in his/her possession will also be deleted. Do you really wish to delete $s1%?
         */
        val WISH_DELETE_S1: SystemMessageId

        /**
         * ID: 79<br></br>
         * Message: This name already exists.
         */
        val NAMING_NAME_ALREADY_EXISTS: SystemMessageId

        /**
         * ID: 80<br></br>
         * Message: Names must be between 1-16 characters, excluding spaces or special characters.
         */
        val NAMING_CHARNAME_UP_TO_16CHARS: SystemMessageId

        /**
         * ID: 81<br></br>
         * Message: Please select your race.
         */
        val PLEASE_SELECT_RACE: SystemMessageId

        /**
         * ID: 82<br></br>
         * Message: Please select your occupation.
         */
        val PLEASE_SELECT_OCCUPATION: SystemMessageId

        /**
         * ID: 83<br></br>
         * Message: Please select your gender.
         */
        val PLEASE_SELECT_GENDER: SystemMessageId

        /**
         * ID: 84<br></br>
         * Message: You may not attack in a peaceful zone.
         */
        val CANT_ATK_PEACEZONE: SystemMessageId

        /**
         * ID: 85<br></br>
         * Message: You may not attack this target in a peaceful zone.
         */
        val TARGET_IN_PEACEZONE: SystemMessageId

        /**
         * ID: 86<br></br>
         * Message: Please enter your ID.
         */
        val PLEASE_ENTER_ID: SystemMessageId

        /**
         * ID: 87<br></br>
         * Message: Please enter your password.
         */
        val PLEASE_ENTER_PASSWORD: SystemMessageId

        /**
         * ID: 88<br></br>
         * Message: Your protocol version is different, please restart your client and run a full check.
         */
        val WRONG_PROTOCOL_CHECK: SystemMessageId

        /**
         * ID: 89<br></br>
         * Message: Your protocol version is different, please continue.
         */
        val WRONG_PROTOCOL_CONTINUE: SystemMessageId

        /**
         * ID: 90<br></br>
         * Message: You are unable to connect to the server.
         */
        val UNABLE_TO_CONNECT: SystemMessageId

        /**
         * ID: 91<br></br>
         * Message: Please select your hairstyle.
         */
        val PLEASE_SELECT_HAIRSTYLE: SystemMessageId

        /**
         * ID: 92<br></br>
         * Message: $s1 has worn off.
         */
        val S1_HAS_WORN_OFF: SystemMessageId

        /**
         * ID: 93<br></br>
         * Message: You do not have enough SP for this.
         */
        val NOT_ENOUGH_SP: SystemMessageId

        /**
         * ID: 94<br></br>
         * Message: 2004-2009 (c) Copyright NCsoft Corporation. All Rights Reserved.
         */
        val COPYRIGHT: SystemMessageId

        /**
         * ID: 95<br></br>
         * Message: You have earned $s1 experience and $s2 SP.
         */
        val YOU_EARNED_S1_EXP_AND_S2_SP: SystemMessageId

        /**
         * ID: 96<br></br>
         * Message: Your level has increased!
         */
        val YOU_INCREASED_YOUR_LEVEL: SystemMessageId

        /**
         * ID: 97<br></br>
         * Message: This item cannot be moved.
         */
        val CANNOT_MOVE_THIS_ITEM: SystemMessageId

        /**
         * ID: 98<br></br>
         * Message: This item cannot be discarded.
         */
        val CANNOT_DISCARD_THIS_ITEM: SystemMessageId

        /**
         * ID: 99<br></br>
         * Message: This item cannot be traded or sold.
         */
        val CANNOT_TRADE_THIS_ITEM: SystemMessageId

        /**
         * ID: 100<br></br>
         * Message: $s1 is requesting to trade. Do you wish to continue?
         */
        val S1_REQUESTS_TRADE: SystemMessageId

        /**
         * ID: 101<br></br>
         * Message: You cannot exit while in combat.
         */
        val CANT_LOGOUT_WHILE_FIGHTING: SystemMessageId

        /**
         * ID: 102<br></br>
         * Message: You cannot restart while in combat.
         */
        val CANT_RESTART_WHILE_FIGHTING: SystemMessageId

        /**
         * ID: 103<br></br>
         * Message: This ID is currently logged in.
         */
        val ID_LOGGED_IN: SystemMessageId

        /**
         * ID: 104<br></br>
         * Message: You may not equip items while casting or performing a skill.
         */
        val CANNOT_USE_ITEM_WHILE_USING_MAGIC: SystemMessageId

        /**
         * ID: 105<br></br>
         * Message: You have invited $s1 to your party.
         */
        val YOU_INVITED_S1_TO_PARTY: SystemMessageId

        /**
         * ID: 106<br></br>
         * Message: You have joined $s1's party.
         */
        val YOU_JOINED_S1_PARTY: SystemMessageId

        /**
         * ID: 107<br></br>
         * Message: $s1 has joined the party.
         */
        val S1_JOINED_PARTY: SystemMessageId

        /**
         * ID: 108<br></br>
         * Message: $s1 has left the party.
         */
        val S1_LEFT_PARTY: SystemMessageId

        /**
         * ID: 109<br></br>
         * Message: Invalid target.
         */
        val INCORRECT_TARGET: SystemMessageId

        /**
         * ID: 110<br></br>
         * Message: $s1 $s2's effect can be felt.
         */
        val YOU_FEEL_S1_EFFECT: SystemMessageId

        /**
         * ID: 111<br></br>
         * Message: Your shield defense has succeeded.
         */
        val SHIELD_DEFENCE_SUCCESSFULL: SystemMessageId

        /**
         * ID: 112<br></br>
         * Message: You may no longer adjust items in the trade because the trade has been confirmed.
         */
        val NOT_ENOUGH_ARROWS: SystemMessageId

        /**
         * ID: 113<br></br>
         * Message: $s1 cannot be used due to unsuitable terms.
         */
        val S1_CANNOT_BE_USED: SystemMessageId

        /**
         * ID: 114<br></br>
         * Message: You have entered the shadow of the Mother Tree.
         */
        val ENTER_SHADOW_MOTHER_TREE: SystemMessageId

        /**
         * ID: 115<br></br>
         * Message: You have left the shadow of the Mother Tree.
         */
        val EXIT_SHADOW_MOTHER_TREE: SystemMessageId

        /**
         * ID: 116<br></br>
         * Message: You have entered a peaceful zone.
         */
        val ENTER_PEACEFUL_ZONE: SystemMessageId

        /**
         * ID: 117<br></br>
         * Message: You have left the peaceful zone.
         */
        val EXIT_PEACEFUL_ZONE: SystemMessageId

        /**
         * ID: 118<br></br>
         * Message: You have requested a trade with $s1
         */
        val REQUEST_S1_FOR_TRADE: SystemMessageId

        /**
         * ID: 119<br></br>
         * Message: $s1 has denied your request to trade.
         */
        val S1_DENIED_TRADE_REQUEST: SystemMessageId

        /**
         * ID: 120<br></br>
         * Message: You begin trading with $s1.
         */
        val BEGIN_TRADE_WITH_S1: SystemMessageId

        /**
         * ID: 121<br></br>
         * Message: $s1 has confirmed the trade.
         */
        val S1_CONFIRMED_TRADE: SystemMessageId

        /**
         * ID: 122<br></br>
         * Message: You may no longer adjust items in the trade because the trade has been confirmed.
         */
        val CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED: SystemMessageId

        /**
         * ID: 123<br></br>
         * Message: Your trade is successful.
         */
        val TRADE_SUCCESSFUL: SystemMessageId

        /**
         * ID: 124<br></br>
         * Message: $s1 has cancelled the trade.
         */
        val S1_CANCELED_TRADE: SystemMessageId

        /**
         * ID: 125<br></br>
         * Message: Do you wish to exit the game?
         */
        val WISH_EXIT_GAME: SystemMessageId

        /**
         * ID: 126<br></br>
         * Message: Do you wish to return to the character select screen?
         */
        val WISH_RESTART_GAME: SystemMessageId

        /**
         * ID: 127<br></br>
         * Message: You have been disconnected from the server. Please login again.
         */
        val DISCONNECTED_FROM_SERVER: SystemMessageId

        /**
         * ID: 128<br></br>
         * Message: Your character creation has failed.
         */
        val CHARACTER_CREATION_FAILED: SystemMessageId

        /**
         * ID: 129<br></br>
         * Message: Your inventory is full.
         */
        val SLOTS_FULL: SystemMessageId

        /**
         * ID: 130<br></br>
         * Message: Your warehouse is full.
         */
        val WAREHOUSE_FULL: SystemMessageId

        /**
         * ID: 131<br></br>
         * Message: $s1 has logged in.
         */
        val S1_LOGGED_IN: SystemMessageId

        /**
         * ID: 132<br></br>
         * Message: $s1 has been added to your friends list.
         */
        val S1_ADDED_TO_FRIENDS: SystemMessageId

        /**
         * ID: 133<br></br>
         * Message: $s1 has been removed from your friends list.
         */
        val S1_REMOVED_FROM_YOUR_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 134<br></br>
         * Message: Please check your friends list again.
         */
        val PLEACE_CHECK_YOUR_FRIEND_LIST_AGAIN: SystemMessageId

        /**
         * ID: 135<br></br>
         * Message: $s1 did not reply to your invitation. Your invitation has been cancelled.
         */
        val S1_DID_NOT_REPLY_TO_YOUR_INVITE: SystemMessageId

        /**
         * ID: 136<br></br>
         * Message: You have not replied to $s1's invitation. The offer has been cancelled.
         */
        val YOU_DID_NOT_REPLY_TO_S1_INVITE: SystemMessageId

        /**
         * ID: 137<br></br>
         * Message: There are no more items in the shortcut.
         */
        val NO_MORE_ITEMS_SHORTCUT: SystemMessageId

        /**
         * ID: 138<br></br>
         * Message: Designate shortcut.
         */
        val DESIGNATE_SHORTCUT: SystemMessageId

        /**
         * ID: 139<br></br>
         * Message: $s1 has resisted your $s2.
         */
        val S1_RESISTED_YOUR_S2: SystemMessageId

        /**
         * ID: 140<br></br>
         * Message: Your skill was removed due to a lack of MP.
         */
        val SKILL_REMOVED_DUE_LACK_MP: SystemMessageId

        /**
         * ID: 141<br></br>
         * Message: Once the trade is confirmed, the item cannot be moved again.
         */
        val ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN: SystemMessageId

        /**
         * ID: 142<br></br>
         * Message: You are already trading with someone.
         */
        val ALREADY_TRADING: SystemMessageId

        /**
         * ID: 143<br></br>
         * Message: $s1 is already trading with another person. Please try again later.
         */
        val S1_ALREADY_TRADING: SystemMessageId

        /**
         * ID: 144<br></br>
         * Message: That is the incorrect target.
         */
        val TARGET_IS_INCORRECT: SystemMessageId

        /**
         * ID: 145<br></br>
         * Message: That player is not online.
         */
        val TARGET_IS_NOT_FOUND_IN_THE_GAME: SystemMessageId

        /**
         * ID: 146<br></br>
         * Message: Chatting is now permitted.
         */
        val CHATTING_PERMITTED: SystemMessageId

        /**
         * ID: 147<br></br>
         * Message: Chatting is currently prohibited.
         */
        val CHATTING_PROHIBITED: SystemMessageId

        /**
         * ID: 148<br></br>
         * Message: You cannot use quest items.
         */
        val CANNOT_USE_QUEST_ITEMS: SystemMessageId

        /**
         * ID: 149<br></br>
         * Message: You cannot pick up or use items while trading.
         */
        val CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING: SystemMessageId

        /**
         * ID: 150<br></br>
         * Message: You cannot discard or destroy an item while trading at a private store.
         */
        val CANNOT_DISCARD_OR_DESTROY_ITEM_WHILE_TRADING: SystemMessageId

        /**
         * ID: 151<br></br>
         * Message: That is too far from you to discard.
         */
        val CANNOT_DISCARD_DISTANCE_TOO_FAR: SystemMessageId

        /**
         * ID: 152<br></br>
         * Message: You have invited the wrong target.
         */
        val YOU_HAVE_INVITED_THE_WRONG_TARGET: SystemMessageId

        /**
         * ID: 153<br></br>
         * Message: $s1 is on another task. Please try again later.
         */
        val S1_IS_BUSY_TRY_LATER: SystemMessageId

        /**
         * ID: 154<br></br>
         * Message: Only the leader can give out invitations.
         */
        val ONLY_LEADER_CAN_INVITE: SystemMessageId

        /**
         * ID: 155<br></br>
         * Message: The party is full.
         */
        val PARTY_FULL: SystemMessageId

        /**
         * ID: 156<br></br>
         * Message: Drain was only 50 percent successful.
         */
        val DRAIN_HALF_SUCCESFUL: SystemMessageId

        /**
         * ID: 157<br></br>
         * Message: You resisted $s1's drain.
         */
        val RESISTED_S1_DRAIN: SystemMessageId

        /**
         * ID: 158<br></br>
         * Message: Your attack has failed.
         */
        val ATTACK_FAILED: SystemMessageId

        /**
         * ID: 159<br></br>
         * Message: You resisted $s1's magic.
         */
        val RESISTED_S1_MAGIC: SystemMessageId

        /**
         * ID: 160<br></br>
         * Message: $s1 is a member of another party and cannot be invited.
         */
        val S1_IS_ALREADY_IN_PARTY: SystemMessageId

        /**
         * ID: 161<br></br>
         * Message: That player is not currently online.
         */
        val INVITED_USER_NOT_ONLINE: SystemMessageId

        /**
         * ID: 162<br></br>
         * Message: Warehouse is too far.
         */
        val WAREHOUSE_TOO_FAR: SystemMessageId

        /**
         * ID: 163<br></br>
         * Message: You cannot destroy it because the number is incorrect.
         */
        val CANNOT_DESTROY_NUMBER_INCORRECT: SystemMessageId

        /**
         * ID: 164<br></br>
         * Message: Waiting for another reply.
         */
        val WAITING_FOR_ANOTHER_REPLY: SystemMessageId

        /**
         * ID: 165<br></br>
         * Message: You cannot add yourself to your own friend list.
         */
        val YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST: SystemMessageId

        /**
         * ID: 166<br></br>
         * Message: Friend list is not ready yet. Please register again later.
         */
        val FRIEND_LIST_NOT_READY_YET_REGISTER_LATER: SystemMessageId

        /**
         * ID: 167<br></br>
         * Message: $s1 is already on your friend list.
         */
        val S1_ALREADY_ON_FRIEND_LIST: SystemMessageId

        /**
         * ID: 168<br></br>
         * Message: $s1 has sent a friend request.
         */
        val S1_REQUESTED_TO_BECOME_FRIENDS: SystemMessageId

        /**
         * ID: 169<br></br>
         * Message: Accept friendship 0/1 (1 to accept, 0 to deny)
         */
        val ACCEPT_THE_FRIENDSHIP: SystemMessageId

        /**
         * ID: 170<br></br>
         * Message: The user who requested to become friends is not found in the game.
         */
        val THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME: SystemMessageId

        /**
         * ID: 171<br></br>
         * Message: $s1 is not on your friend list.
         */
        val S1_NOT_ON_YOUR_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 172<br></br>
         * Message: You lack the funds needed to pay for this transaction.
         */
        val LACK_FUNDS_FOR_TRANSACTION1: SystemMessageId

        /**
         * ID: 173<br></br>
         * Message: You lack the funds needed to pay for this transaction.
         */
        val LACK_FUNDS_FOR_TRANSACTION2: SystemMessageId

        /**
         * ID: 174<br></br>
         * Message: That person's inventory is full.
         */
        val OTHER_INVENTORY_FULL: SystemMessageId

        /**
         * ID: 175<br></br>
         * Message: That skill has been de-activated as HP was fully recovered.
         */
        val SKILL_DEACTIVATED_HP_FULL: SystemMessageId

        /**
         * ID: 176<br></br>
         * Message: That person is in message refusal mode.
         */
        val THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE: SystemMessageId

        /**
         * ID: 177<br></br>
         * Message: Message refusal mode.
         */
        val MESSAGE_REFUSAL_MODE: SystemMessageId

        /**
         * ID: 178<br></br>
         * Message: Message acceptance mode.
         */
        val MESSAGE_ACCEPTANCE_MODE: SystemMessageId

        /**
         * ID: 179<br></br>
         * Message: You cannot discard those items here.
         */
        val CANT_DISCARD_HERE: SystemMessageId

        /**
         * ID: 180<br></br>
         * Message: You have $s1 day(s) left until deletion. Do you wish to cancel this action?
         */
        val S1_DAYS_LEFT_CANCEL_ACTION: SystemMessageId

        /**
         * ID: 181<br></br>
         * Message: Cannot see target.
         */
        val CANT_SEE_TARGET: SystemMessageId

        /**
         * ID: 182<br></br>
         * Message: Do you want to quit the current quest?
         */
        val WANT_QUIT_CURRENT_QUEST: SystemMessageId

        /**
         * ID: 183<br></br>
         * Message: There are too many users on the server. Please try again later
         */
        val TOO_MANY_USERS: SystemMessageId

        /**
         * ID: 184<br></br>
         * Message: Please try again later.
         */
        val TRY_AGAIN_LATER: SystemMessageId

        /**
         * ID: 185<br></br>
         * Message: You must first select a user to invite to your party.
         */
        val FIRST_SELECT_USER_TO_INVITE_TO_PARTY: SystemMessageId

        /**
         * ID: 186<br></br>
         * Message: You must first select a user to invite to your clan.
         */
        val FIRST_SELECT_USER_TO_INVITE_TO_CLAN: SystemMessageId

        /**
         * ID: 187<br></br>
         * Message: Select user to expel.
         */
        val SELECT_USER_TO_EXPEL: SystemMessageId

        /**
         * ID: 188<br></br>
         * Message: Please create your clan name.
         */
        val PLEASE_CREATE_CLAN_NAME: SystemMessageId

        /**
         * ID: 189<br></br>
         * Message: Your clan has been created.
         */
        val CLAN_CREATED: SystemMessageId

        /**
         * ID: 190<br></br>
         * Message: You have failed to create a clan.
         */
        val FAILED_TO_CREATE_CLAN: SystemMessageId

        /**
         * ID: 191<br></br>
         * Message: Clan member $s1 has been expelled.
         */
        val CLAN_MEMBER_S1_EXPELLED: SystemMessageId

        /**
         * ID: 192<br></br>
         * Message: You have failed to expel $s1 from the clan.
         */
        val FAILED_EXPEL_S1: SystemMessageId

        /**
         * ID: 193<br></br>
         * Message: Clan has dispersed.
         */
        val CLAN_HAS_DISPERSED: SystemMessageId

        /**
         * ID: 194<br></br>
         * Message: You have failed to disperse the clan.
         */
        val FAILED_TO_DISPERSE_CLAN: SystemMessageId

        /**
         * ID: 195<br></br>
         * Message: Entered the clan.
         */
        val ENTERED_THE_CLAN: SystemMessageId

        /**
         * ID: 196<br></br>
         * Message: $s1 declined your clan invitation.
         */
        val S1_REFUSED_TO_JOIN_CLAN: SystemMessageId

        /**
         * ID: 197<br></br>
         * Message: You have withdrawn from the clan.
         */
        val YOU_HAVE_WITHDRAWN_FROM_CLAN: SystemMessageId

        /**
         * ID: 198<br></br>
         * Message: You have failed to withdraw from the $s1 clan.
         */
        val FAILED_TO_WITHDRAW_FROM_S1_CLAN: SystemMessageId

        /**
         * ID: 199<br></br>
         * Message: You have recently been dismissed from a clan. You are not allowed to join another clan for 24-hours.
         */
        val CLAN_MEMBERSHIP_TERMINATED: SystemMessageId

        /**
         * ID: 200<br></br>
         * Message: You have withdrawn from the party.
         */
        val YOU_LEFT_PARTY: SystemMessageId

        /**
         * ID: 201<br></br>
         * Message: $s1 was expelled from the party.
         */
        val S1_WAS_EXPELLED_FROM_PARTY: SystemMessageId

        /**
         * ID: 202<br></br>
         * Message: You have been expelled from the party.
         */
        val HAVE_BEEN_EXPELLED_FROM_PARTY: SystemMessageId

        /**
         * ID: 203<br></br>
         * Message: The party has dispersed.
         */
        val PARTY_DISPERSED: SystemMessageId

        /**
         * ID: 204<br></br>
         * Message: Incorrect name. Please try again.
         */
        val INCORRECT_NAME_TRY_AGAIN: SystemMessageId

        /**
         * ID: 205<br></br>
         * Message: Incorrect character name. Please try again.
         */
        val INCORRECT_CHARACTER_NAME_TRY_AGAIN: SystemMessageId

        /**
         * ID: 206<br></br>
         * Message: Please enter the name of the clan you wish to declare war on.
         */
        val ENTER_CLAN_NAME_TO_DECLARE_WAR: SystemMessageId

        /**
         * ID: 207<br></br>
         * Message: $s2 of the clan $s1 requests declaration of war. Do you accept?
         */
        val S2_OF_THE_CLAN_S1_REQUESTS_WAR: SystemMessageId

        /**
         * ID: 212<br></br>
         * Message: You are not a clan member and cannot perform this action.
         */
        val YOU_ARE_NOT_A_CLAN_MEMBER: SystemMessageId

        /**
         * ID: 213<br></br>
         * Message: Not working. Please try again later.
         */
        val NOT_WORKING_PLEASE_TRY_AGAIN_LATER: SystemMessageId

        /**
         * ID: 214<br></br>
         * Message: Your title has been changed.
         */
        val TITLE_CHANGED: SystemMessageId

        /**
         * ID: 215<br></br>
         * Message: War with the $s1 clan has begun.
         */
        val WAR_WITH_THE_S1_CLAN_HAS_BEGUN: SystemMessageId

        /**
         * ID: 216<br></br>
         * Message: War with the $s1 clan has ended.
         */
        val WAR_WITH_THE_S1_CLAN_HAS_ENDED: SystemMessageId

        /**
         * ID: 217<br></br>
         * Message: You have won the war over the $s1 clan!
         */
        val YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN: SystemMessageId

        /**
         * ID: 218<br></br>
         * Message: You have surrendered to the $s1 clan.
         */
        val YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN: SystemMessageId

        /**
         * ID: 219<br></br>
         * Message: Your clan leader has died. You have been defeated by the $s1 clan.
         */
        val YOU_WERE_DEFEATED_BY_S1_CLAN: SystemMessageId

        /**
         * ID: 220<br></br>
         * Message: You have $s1 minutes left until the clan war ends.
         */
        val S1_MINUTES_LEFT_UNTIL_CLAN_WAR_ENDS: SystemMessageId

        /**
         * ID: 221<br></br>
         * Message: The time limit for the clan war is up. War with the $s1 clan is over.
         */
        val CLAN_WAR_WITH_S1_CLAN_HAS_ENDED: SystemMessageId

        /**
         * ID: 222<br></br>
         * Message: $s1 has joined the clan.
         */
        val S1_HAS_JOINED_CLAN: SystemMessageId

        /**
         * ID: 223<br></br>
         * Message: $s1 has withdrawn from the clan.
         */
        val S1_HAS_WITHDRAWN_FROM_THE_CLAN: SystemMessageId

        /**
         * ID: 224<br></br>
         * Message: $s1 did not respond: Invitation to the clan has been cancelled.
         */
        val S1_DID_NOT_RESPOND_TO_CLAN_INVITATION: SystemMessageId

        /**
         * ID: 225<br></br>
         * Message: You didn't respond to $s1's invitation: joining has been cancelled.
         */
        val YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION: SystemMessageId

        /**
         * ID: 226<br></br>
         * Message: The $s1 clan did not respond: war proclamation has been refused.
         */
        val S1_CLAN_DID_NOT_RESPOND: SystemMessageId

        /**
         * ID: 227<br></br>
         * Message: Clan war has been refused because you did not respond to $s1 clan's war proclamation.
         */
        val CLAN_WAR_REFUSED_YOU_DID_NOT_RESPOND_TO_S1: SystemMessageId

        /**
         * ID: 228<br></br>
         * Message: Request to end war has been denied.
         */
        val REQUEST_TO_END_WAR_HAS_BEEN_DENIED: SystemMessageId

        /**
         * ID: 229<br></br>
         * Message: You do not meet the criteria in order to create a clan.
         */
        val YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN: SystemMessageId

        /**
         * ID: 230<br></br>
         * Message: You must wait 10 days before creating a new clan.
         */
        val YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN: SystemMessageId

        /**
         * ID: 231<br></br>
         * Message: After a clan member is dismissed from a clan, the clan must wait at least a day before accepting a new member.
         */
        val YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER: SystemMessageId

        /**
         * ID: 232<br></br>
         * Message: After leaving or having been dismissed from a clan, you must wait at least a day before joining another clan.
         */
        val YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN: SystemMessageId

        /**
         * ID: 233<br></br>
         * Message: The Academy/Royal Guard/Order of Knights is full and cannot accept new members at this time.
         */
        val SUBCLAN_IS_FULL: SystemMessageId

        /**
         * ID: 234<br></br>
         * Message: The target must be a clan member.
         */
        val TARGET_MUST_BE_IN_CLAN: SystemMessageId

        /**
         * ID: 235<br></br>
         * Message: You are not authorized to bestow these rights.
         */
        val NOT_AUTHORIZED_TO_BESTOW_RIGHTS: SystemMessageId

        /**
         * ID: 236<br></br>
         * Message: Only the clan leader is enabled.
         */
        val ONLY_THE_CLAN_LEADER_IS_ENABLED: SystemMessageId

        /**
         * ID: 237<br></br>
         * Message: The clan leader could not be found.
         */
        val CLAN_LEADER_NOT_FOUND: SystemMessageId

        /**
         * ID: 238<br></br>
         * Message: Not joined in any clan.
         */
        val NOT_JOINED_IN_ANY_CLAN: SystemMessageId

        /**
         * ID: 239<br></br>
         * Message: The clan leader cannot withdraw.
         */
        val CLAN_LEADER_CANNOT_WITHDRAW: SystemMessageId

        /**
         * ID: 240<br></br>
         * Message: Currently involved in clan war.
         */
        val CURRENTLY_INVOLVED_IN_CLAN_WAR: SystemMessageId

        /**
         * ID: 241<br></br>
         * Message: Leader of the $s1 Clan is not logged in.
         */
        val LEADER_OF_S1_CLAN_NOT_FOUND: SystemMessageId

        /**
         * ID: 242<br></br>
         * Message: Select target.
         */
        val SELECT_TARGET: SystemMessageId

        /**
         * ID: 243<br></br>
         * Message: You cannot declare war on an allied clan.
         */
        val CANNOT_DECLARE_WAR_ON_ALLIED_CLAN: SystemMessageId

        /**
         * ID: 244<br></br>
         * Message: You are not allowed to issue this challenge.
         */
        val NOT_ALLOWED_TO_CHALLENGE: SystemMessageId

        /**
         * ID: 245<br></br>
         * Message: 5 days has not passed since you were refused war. Do you wish to continue?
         */
        val FIVE_DAYS_NOT_PASSED_SINCE_REFUSED_WAR: SystemMessageId

        /**
         * ID: 246<br></br>
         * Message: That clan is currently at war.
         */
        val CLAN_CURRENTLY_AT_WAR: SystemMessageId

        /**
         * ID: 247<br></br>
         * Message: You have already been at war with the $s1 clan: 5 days must pass before you can challenge this clan again
         */
        val FIVE_DAYS_MUST_PASS_BEFORE_CHALLENGE_S1_AGAIN: SystemMessageId

        /**
         * ID: 248<br></br>
         * Message: You cannot proclaim war: the $s1 clan does not have enough members.
         */
        val S1_CLAN_NOT_ENOUGH_MEMBERS_FOR_WAR: SystemMessageId

        /**
         * ID: 249<br></br>
         * Message: Do you wish to surrender to the $s1 clan?
         */
        val WISH_SURRENDER_TO_S1_CLAN: SystemMessageId

        /**
         * ID: 250<br></br>
         * Message: You have personally surrendered to the $s1 clan. You are no longer participating in this clan war.
         */
        val YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN: SystemMessageId

        /**
         * ID: 251<br></br>
         * Message: You cannot proclaim war: you are at war with another clan.
         */
        val ALREADY_AT_WAR_WITH_ANOTHER_CLAN: SystemMessageId

        /**
         * ID: 252<br></br>
         * Message: Enter the clan name to surrender to.
         */
        val ENTER_CLAN_NAME_TO_SURRENDER_TO: SystemMessageId

        /**
         * ID: 253<br></br>
         * Message: Enter the name of the clan you wish to end the war with.
         */
        val ENTER_CLAN_NAME_TO_END_WAR: SystemMessageId

        /**
         * ID: 254<br></br>
         * Message: A clan leader cannot personally surrender.
         */
        val LEADER_CANT_PERSONALLY_SURRENDER: SystemMessageId

        /**
         * ID: 255<br></br>
         * Message: The $s1 clan has requested to end war. Do you agree?
         */
        val S1_CLAN_REQUESTED_END_WAR: SystemMessageId

        /**
         * ID: 256<br></br>
         * Message: Enter title
         */
        val ENTER_TITLE: SystemMessageId

        /**
         * ID: 257<br></br>
         * Message: Do you offer the $s1 clan a proposal to end the war?
         */
        val DO_YOU_OFFER_S1_CLAN_END_WAR: SystemMessageId

        /**
         * ID: 258<br></br>
         * Message: You are not involved in a clan war.
         */
        val NOT_INVOLVED_CLAN_WAR: SystemMessageId

        /**
         * ID: 259<br></br>
         * Message: Select clan members from list.
         */
        val SELECT_MEMBERS_FROM_LIST: SystemMessageId

        /**
         * ID: 260<br></br>
         * Message: Fame level has decreased: 5 days have not passed since you were refused war
         */
        val FIVE_DAYS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR: SystemMessageId

        /**
         * ID: 261<br></br>
         * Message: Clan name is invalid.
         */
        val CLAN_NAME_INVALID: SystemMessageId

        /**
         * ID: 262<br></br>
         * Message: Clan name's length is incorrect.
         */
        val CLAN_NAME_LENGTH_INCORRECT: SystemMessageId

        /**
         * ID: 263<br></br>
         * Message: You have already requested the dissolution of your clan.
         */
        val DISSOLUTION_IN_PROGRESS: SystemMessageId

        /**
         * ID: 264<br></br>
         * Message: You cannot dissolve a clan while engaged in a war.
         */
        val CANNOT_DISSOLVE_WHILE_IN_WAR: SystemMessageId

        /**
         * ID: 265<br></br>
         * Message: You cannot dissolve a clan during a siege or while protecting a castle.
         */
        val CANNOT_DISSOLVE_WHILE_IN_SIEGE: SystemMessageId

        /**
         * ID: 266<br></br>
         * Message: You cannot dissolve a clan while owning a clan hall or castle.
         */
        val CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE: SystemMessageId

        /**
         * ID: 267<br></br>
         * Message: There are no requests to disperse.
         */
        val NO_REQUESTS_TO_DISPERSE: SystemMessageId

        /**
         * ID: 268<br></br>
         * Message: That player already belongs to another clan.
         */
        val PLAYER_ALREADY_ANOTHER_CLAN: SystemMessageId

        /**
         * ID: 269<br></br>
         * Message: You cannot dismiss yourself.
         */
        val YOU_CANNOT_DISMISS_YOURSELF: SystemMessageId

        /**
         * ID: 270<br></br>
         * Message: You have already surrendered.
         */
        val YOU_HAVE_ALREADY_SURRENDERED: SystemMessageId

        /**
         * ID: 271<br></br>
         * Message: A player can only be granted a title if the clan is level 3 or above
         */
        val CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE: SystemMessageId

        /**
         * ID: 272<br></br>
         * Message: A clan crest can only be registered when the clan's skill level is 3 or above.
         */
        val CLAN_LVL_3_NEEDED_TO_SET_CREST: SystemMessageId

        /**
         * ID: 273<br></br>
         * Message: A clan war can only be declared when a clan's skill level is 3 or above.
         */
        val CLAN_LVL_3_NEEDED_TO_DECLARE_WAR: SystemMessageId

        /**
         * ID: 274<br></br>
         * Message: Your clan's skill level has increased.
         */
        val CLAN_LEVEL_INCREASED: SystemMessageId

        /**
         * ID: 275<br></br>
         * Message: Clan has failed to increase skill level.
         */
        val CLAN_LEVEL_INCREASE_FAILED: SystemMessageId

        /**
         * ID: 276<br></br>
         * Message: You do not have the necessary materials or prerequisites to learn this skill.
         */
        val ITEM_MISSING_TO_LEARN_SKILL: SystemMessageId

        /**
         * ID: 277<br></br>
         * Message: You have earned $s1.
         */
        val LEARNED_SKILL_S1: SystemMessageId

        /**
         * ID: 278<br></br>
         * Message: You do not have enough SP to learn this skill.
         */
        val NOT_ENOUGH_SP_TO_LEARN_SKILL: SystemMessageId

        /**
         * ID: 279<br></br>
         * Message: You do not have enough adena.
         */
        val YOU_NOT_ENOUGH_ADENA: SystemMessageId

        /**
         * ID: 280<br></br>
         * Message: You do not have any items to sell.
         */
        val NO_ITEMS_TO_SELL: SystemMessageId

        /**
         * ID: 281<br></br>
         * Message: You do not have enough adena to pay the fee.
         */
        val YOU_NOT_ENOUGH_ADENA_PAY_FEE: SystemMessageId

        /**
         * ID: 282<br></br>
         * Message: You have not deposited any items in your warehouse.
         */
        val NO_ITEM_DEPOSITED_IN_WH: SystemMessageId

        /**
         * ID: 283<br></br>
         * Message: You have entered a combat zone.
         */
        val ENTERED_COMBAT_ZONE: SystemMessageId

        /**
         * ID: 284<br></br>
         * Message: You have left a combat zone.
         */
        val LEFT_COMBAT_ZONE: SystemMessageId

        /**
         * ID: 285<br></br>
         * Message: Clan $s1 has succeeded in engraving the ruler!
         */
        val CLAN_S1_ENGRAVED_RULER: SystemMessageId

        /**
         * ID: 286<br></br>
         * Message: Your base is being attacked.
         */
        val BASE_UNDER_ATTACK: SystemMessageId

        /**
         * ID: 287<br></br>
         * Message: The opposing clan has stared to engrave to monument!
         */
        val OPPONENT_STARTED_ENGRAVING: SystemMessageId

        /**
         * ID: 288<br></br>
         * Message: The castle gate has been broken down.
         */
        val CASTLE_GATE_BROKEN_DOWN: SystemMessageId

        /**
         * ID: 289<br></br>
         * Message: An outpost or headquarters cannot be built because at least one already exists.
         */
        val NOT_ANOTHER_HEADQUARTERS: SystemMessageId

        /**
         * ID: 290<br></br>
         * Message: You cannot set up a base here.
         */
        val NOT_SET_UP_BASE_HERE: SystemMessageId

        /**
         * ID: 291<br></br>
         * Message: Clan $s1 is victorious over $s2's castle siege!
         */
        val CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE: SystemMessageId

        /**
         * ID: 292<br></br>
         * Message: $s1 has announced the castle siege time.
         */
        val S1_ANNOUNCED_SIEGE_TIME: SystemMessageId

        /**
         * ID: 293<br></br>
         * Message: The registration term for $s1 has ended.
         */
        val REGISTRATION_TERM_FOR_S1_ENDED: SystemMessageId

        /**
         * ID: 294<br></br>
         * Message: Because your clan is not currently on the offensive in a Clan Hall siege war, it cannot summon its base camp.
         */
        val BECAUSE_YOUR_CLAN_IS_NOT_CURRENTLY_ON_THE_OFFENSIVE_IN_A_CLAN_HALL_SIEGE_WAR_IT_CANNOT_SUMMON_ITS_BASE_CAMP: SystemMessageId

        /**
         * ID: 295<br></br>
         * Message: $s1's siege was canceled because there were no clans that participated.
         */
        val S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED: SystemMessageId

        /**
         * ID: 296<br></br>
         * Message: You received $s1 damage from taking a high fall.
         */
        val FALL_DAMAGE_S1: SystemMessageId

        /**
         * ID: 297<br></br>
         * Message: You have taken $s1 damage because you were unable to breathe.
         */
        val DROWN_DAMAGE_S1: SystemMessageId

        /**
         * ID: 298<br></br>
         * Message: You have dropped $s1.
         */
        val YOU_DROPPED_S1: SystemMessageId

        /**
         * ID: 299<br></br>
         * Message: $s1 has obtained $s3 $s2.
         */
        val S1_OBTAINED_S3_S2: SystemMessageId

        /**
         * ID: 300<br></br>
         * Message: $s1 has obtained $s2.
         */
        val S1_OBTAINED_S2: SystemMessageId

        /**
         * ID: 301<br></br>
         * Message: $s2 $s1 has disappeared.
         */
        val S2_S1_DISAPPEARED: SystemMessageId

        /**
         * ID: 302<br></br>
         * Message: $s1 has disappeared.
         */
        val S1_DISAPPEARED: SystemMessageId

        /**
         * ID: 303<br></br>
         * Message: Select item to enchant.
         */
        val SELECT_ITEM_TO_ENCHANT: SystemMessageId

        /**
         * ID: 304<br></br>
         * Message: Clan member $s1 has logged into game.
         */
        val CLAN_MEMBER_S1_LOGGED_IN: SystemMessageId

        /**
         * ID: 305<br></br>
         * Message: The player declined to join your party.
         */
        val PLAYER_DECLINED: SystemMessageId

        // 306 - 308 empty

        /**
         * ID: 309<br></br>
         * Message: You have succeeded in expelling the clan member.
         */
        val YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER: SystemMessageId

        // 310 empty

        /**
         * ID: 311<br></br>
         * Message: The clan war declaration has been accepted.
         */
        val CLAN_WAR_DECLARATION_ACCEPTED: SystemMessageId

        /**
         * ID: 312<br></br>
         * Message: The clan war declaration has been refused.
         */
        val CLAN_WAR_DECLARATION_REFUSED: SystemMessageId

        /**
         * ID: 313<br></br>
         * Message: The cease war request has been accepted.
         */
        val CEASE_WAR_REQUEST_ACCEPTED: SystemMessageId

        /**
         * ID: 314<br></br>
         * Message: You have failed to surrender.
         */
        val FAILED_TO_SURRENDER: SystemMessageId

        /**
         * ID: 315<br></br>
         * Message: You have failed to personally surrender.
         */
        val FAILED_TO_PERSONALLY_SURRENDER: SystemMessageId

        /**
         * ID: 316<br></br>
         * Message: You have failed to withdraw from the party.
         */
        val FAILED_TO_WITHDRAW_FROM_THE_PARTY: SystemMessageId

        /**
         * ID: 317<br></br>
         * Message: You have failed to expel the party member.
         */
        val FAILED_TO_EXPEL_THE_PARTY_MEMBER: SystemMessageId

        /**
         * ID: 318<br></br>
         * Message: You have failed to disperse the party.
         */
        val FAILED_TO_DISPERSE_THE_PARTY: SystemMessageId

        /**
         * ID: 319<br></br>
         * Message: This door cannot be unlocked.
         */
        val UNABLE_TO_UNLOCK_DOOR: SystemMessageId

        /**
         * ID: 320<br></br>
         * Message: You have failed to unlock the door.
         */
        val FAILED_TO_UNLOCK_DOOR: SystemMessageId

        /**
         * ID: 321<br></br>
         * Message: It is not locked.
         */
        val ITS_NOT_LOCKED: SystemMessageId

        /**
         * ID: 322<br></br>
         * Message: Please decide on the sales price.
         */
        val DECIDE_SALES_PRICE: SystemMessageId

        /**
         * ID: 323<br></br>
         * Message: Your force has increased to $s1 level.
         */
        val FORCE_INCREASED_TO_S1: SystemMessageId

        /**
         * ID: 324<br></br>
         * Message: Your force has reached maximum capacity.
         */
        val FORCE_MAXLEVEL_REACHED: SystemMessageId

        /**
         * ID: 325<br></br>
         * Message: The corpse has already disappeared.
         */
        val CORPSE_ALREADY_DISAPPEARED: SystemMessageId

        /**
         * ID: 326<br></br>
         * Message: Select target from list.
         */
        val SELECT_TARGET_FROM_LIST: SystemMessageId

        /**
         * ID: 327<br></br>
         * Message: You cannot exceed 80 characters.
         */
        val CANNOT_EXCEED_80_CHARACTERS: SystemMessageId

        /**
         * ID: 328<br></br>
         * Message: Please input title using less than 128 characters.
         */
        val PLEASE_INPUT_TITLE_LESS_128_CHARACTERS: SystemMessageId

        /**
         * ID: 329<br></br>
         * Message: Please input content using less than 3000 characters.
         */
        val PLEASE_INPUT_CONTENT_LESS_3000_CHARACTERS: SystemMessageId

        /**
         * ID: 330<br></br>
         * Message: A one-line response may not exceed 128 characters.
         */
        val ONE_LINE_RESPONSE_NOT_EXCEED_128_CHARACTERS: SystemMessageId

        /**
         * ID: 331<br></br>
         * Message: You have acquired $s1 SP.
         */
        val ACQUIRED_S1_SP: SystemMessageId

        /**
         * ID: 332<br></br>
         * Message: Do you want to be restored?
         */
        val DO_YOU_WANT_TO_BE_RESTORED: SystemMessageId

        /**
         * ID: 333<br></br>
         * Message: You have received $s1 damage by Core's barrier.
         */
        val S1_DAMAGE_BY_CORE_BARRIER: SystemMessageId

        /**
         * ID: 334<br></br>
         * Message: Please enter your private store display message.
         */
        val ENTER_PRIVATE_STORE_MESSAGE: SystemMessageId

        /**
         * ID: 335<br></br>
         * Message: $s1 has been aborted.
         */
        val S1_HAS_BEEN_ABORTED: SystemMessageId

        /**
         * ID: 336<br></br>
         * Message: You are attempting to crystallize $s1. Do you wish to continue?
         */
        val WISH_TO_CRYSTALLIZE_S1: SystemMessageId

        /**
         * ID: 337<br></br>
         * Message: The soulshot you are attempting to use does not match the grade of your equipped weapon.
         */
        val SOULSHOTS_GRADE_MISMATCH: SystemMessageId

        /**
         * ID: 338<br></br>
         * Message: You do not have enough soulshots for that.
         */
        val NOT_ENOUGH_SOULSHOTS: SystemMessageId

        /**
         * ID: 339<br></br>
         * Message: Cannot use soulshots.
         */
        val CANNOT_USE_SOULSHOTS: SystemMessageId

        /**
         * ID: 340<br></br>
         * Message: Your private store is now open for business.
         */
        val PRIVATE_STORE_UNDER_WAY: SystemMessageId

        /**
         * ID: 341<br></br>
         * Message: You do not have enough materials to perform that action.
         */
        val NOT_ENOUGH_MATERIALS: SystemMessageId

        /**
         * ID: 342<br></br>
         * Message: Power of the spirits enabled.
         */
        val ENABLED_SOULSHOT: SystemMessageId

        /**
         * ID: 343<br></br>
         * Message: Sweeper failed, target not spoiled.
         */
        val SWEEPER_FAILED_TARGET_NOT_SPOILED: SystemMessageId

        /**
         * ID: 344<br></br>
         * Message: Power of the spirits disabled.
         */
        val SOULSHOTS_DISABLED: SystemMessageId

        /**
         * ID: 345<br></br>
         * Message: Chat enabled.
         */
        val CHAT_ENABLED: SystemMessageId

        /**
         * ID: 346<br></br>
         * Message: Chat disabled.
         */
        val CHAT_DISABLED: SystemMessageId

        /**
         * ID: 347<br></br>
         * Message: Incorrect item count.
         */
        val INCORRECT_ITEM_COUNT: SystemMessageId

        /**
         * ID: 348<br></br>
         * Message: Incorrect item price.
         */
        val INCORRECT_ITEM_PRICE: SystemMessageId

        /**
         * ID: 349<br></br>
         * Message: Private store already closed.
         */
        val PRIVATE_STORE_ALREADY_CLOSED: SystemMessageId

        /**
         * ID: 350<br></br>
         * Message: Item out of stock.
         */
        val ITEM_OUT_OF_STOCK: SystemMessageId

        /**
         * ID: 351<br></br>
         * Message: Incorrect item count.
         */
        val NOT_ENOUGH_ITEMS: SystemMessageId

        // 352 - 353: empty

        /**
         * ID: 354<br></br>
         * Message: Cancel enchant.
         */
        val CANCEL_ENCHANT: SystemMessageId

        /**
         * ID: 355<br></br>
         * Message: Inappropriate enchant conditions.
         */
        val INAPPROPRIATE_ENCHANT_CONDITION: SystemMessageId

        /**
         * ID: 356<br></br>
         * Message: Reject resurrection.
         */
        val REJECT_RESURRECTION: SystemMessageId

        /**
         * ID: 357<br></br>
         * Message: It has already been spoiled.
         */
        val ALREADY_SPOILED: SystemMessageId

        /**
         * ID: 358<br></br>
         * Message: $s1 hour(s) until catle siege conclusion.
         */
        val S1_HOURS_UNTIL_SIEGE_CONCLUSION: SystemMessageId

        /**
         * ID: 359<br></br>
         * Message: $s1 minute(s) until catle siege conclusion.
         */
        val S1_MINUTES_UNTIL_SIEGE_CONCLUSION: SystemMessageId

        /**
         * ID: 360<br></br>
         * Message: Castle siege $s1 second(s) left!
         */
        val CASTLE_SIEGE_S1_SECONDS_LEFT: SystemMessageId

        /**
         * ID: 361<br></br>
         * Message: Over-hit!
         */
        val OVER_HIT: SystemMessageId

        /**
         * ID: 362<br></br>
         * Message: You have acquired $s1 bonus experience from a successful over-hit.
         */
        val ACQUIRED_BONUS_EXPERIENCE_THROUGH_OVER_HIT: SystemMessageId

        /**
         * ID: 363<br></br>
         * Message: Chat available time: $s1 minute.
         */
        val CHAT_AVAILABLE_S1_MINUTE: SystemMessageId

        /**
         * ID: 364<br></br>
         * Message: Enter user's name to search
         */
        val ENTER_USER_NAME_TO_SEARCH: SystemMessageId

        /**
         * ID: 365<br></br>
         * Message: Are you sure?
         */
        val ARE_YOU_SURE: SystemMessageId

        /**
         * ID: 366<br></br>
         * Message: Please select your hair color.
         */
        val PLEASE_SELECT_HAIR_COLOR: SystemMessageId

        /**
         * ID: 367<br></br>
         * Message: You cannot remove that clan character at this time.
         */
        val CANNOT_REMOVE_CLAN_CHARACTER: SystemMessageId

        /**
         * ID: 368<br></br>
         * Message: Equipped +$s1 $s2.
         */
        val S1_S2_EQUIPPED: SystemMessageId

        /**
         * ID: 369<br></br>
         * Message: You have obtained a +$s1 $s2.
         */
        val YOU_PICKED_UP_A_S1_S2: SystemMessageId

        /**
         * ID: 370<br></br>
         * Message: Failed to pickup $s1.
         */
        val FAILED_PICKUP_S1: SystemMessageId

        /**
         * ID: 371<br></br>
         * Message: Acquired +$s1 $s2.
         */
        val ACQUIRED_S1_S2: SystemMessageId

        /**
         * ID: 372<br></br>
         * Message: Failed to earn $s1.
         */
        val FAILED_EARN_S1: SystemMessageId

        /**
         * ID: 373<br></br>
         * Message: You are trying to destroy +$s1 $s2. Do you wish to continue?
         */
        val WISH_DESTROY_S1_S2: SystemMessageId

        /**
         * ID: 374<br></br>
         * Message: You are attempting to crystallize +$s1 $s2. Do you wish to continue?
         */
        val WISH_CRYSTALLIZE_S1_S2: SystemMessageId

        /**
         * ID: 375<br></br>
         * Message: You have dropped +$s1 $s2 .
         */
        val DROPPED_S1_S2: SystemMessageId

        /**
         * ID: 376<br></br>
         * Message: $s1 has obtained +$s2$s3.
         */
        val S1_OBTAINED_S2_S3: SystemMessageId

        /**
         * ID: 377<br></br>
         * Message: $S1 $S2 disappeared.
         */
        val S1_S2_DISAPPEARED: SystemMessageId

        /**
         * ID: 378<br></br>
         * Message: $s1 purchased $s2.
         */
        val S1_PURCHASED_S2: SystemMessageId

        /**
         * ID: 379<br></br>
         * Message: $s1 purchased +$s2$s3.
         */
        val S1_PURCHASED_S2_S3: SystemMessageId

        /**
         * ID: 380<br></br>
         * Message: $s1 purchased $s3 $s2(s).
         */
        val S1_PURCHASED_S3_S2_S: SystemMessageId

        /**
         * ID: 381<br></br>
         * Message: The game client encountered an error and was unable to connect to the petition server.
         */
        val GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER: SystemMessageId

        /**
         * ID: 382<br></br>
         * Message: Currently there are no users that have checked out a GM ID.
         */
        val NO_USERS_CHECKED_OUT_GM_ID: SystemMessageId

        /**
         * ID: 383<br></br>
         * Message: Request confirmed to end consultation at petition server.
         */
        val REQUEST_CONFIRMED_TO_END_CONSULTATION: SystemMessageId

        /**
         * ID: 384<br></br>
         * Message: The client is not logged onto the game server.
         */
        val CLIENT_NOT_LOGGED_ONTO_GAME_SERVER: SystemMessageId

        /**
         * ID: 385<br></br>
         * Message: Request confirmed to begin consultation at petition server.
         */
        val REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION: SystemMessageId

        /**
         * ID: 386<br></br>
         * Message: The body of your petition must be more than five characters in length.
         */
        val PETITION_MORE_THAN_FIVE_CHARACTERS: SystemMessageId

        /**
         * ID: 387<br></br>
         * Message: This ends the GM petition consultation. Please take a moment to provide feedback about this service.
         */
        val THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK: SystemMessageId

        /**
         * ID: 388<br></br>
         * Message: Not under petition consultation.
         */
        val NOT_UNDER_PETITION_CONSULTATION: SystemMessageId

        /**
         * ID: 389<br></br>
         * Message: our petition application has been accepted. - Receipt No. is $s1.
         */
        val PETITION_ACCEPTED_RECENT_NO_S1: SystemMessageId

        /**
         * ID: 390<br></br>
         * Message: You may only submit one petition (active) at a time.
         */
        val ONLY_ONE_ACTIVE_PETITION_AT_TIME: SystemMessageId

        /**
         * ID: 391<br></br>
         * Message: Receipt No. $s1, petition cancelled.
         */
        val RECENT_NO_S1_CANCELED: SystemMessageId

        /**
         * ID: 392<br></br>
         * Message: Under petition advice.
         */
        val UNDER_PETITION_ADVICE: SystemMessageId

        /**
         * ID: 393<br></br>
         * Message: Failed to cancel petition. Please try again later.
         */
        val FAILED_CANCEL_PETITION_TRY_LATER: SystemMessageId

        /**
         * ID: 394<br></br>
         * Message: Petition consultation with $s1, under way.
         */
        val PETITION_WITH_S1_UNDER_WAY: SystemMessageId

        /**
         * ID: 395<br></br>
         * Message: Ending petition consultation with $s1.
         */
        val PETITION_ENDED_WITH_S1: SystemMessageId

        /**
         * ID: 396<br></br>
         * Message: Please login after changing your temporary password.
         */
        val TRY_AGAIN_AFTER_CHANGING_PASSWORD: SystemMessageId

        /**
         * ID: 397<br></br>
         * Message: Not a paid account.
         */
        val NO_PAID_ACCOUNT: SystemMessageId

        /**
         * ID: 398<br></br>
         * Message: There is no time left on this account.
         */
        val NO_TIME_LEFT_ON_ACCOUNT: SystemMessageId

        // 399: empty

        /**
         * ID: 400<br></br>
         * Message: You are attempting to drop $s1. Dou you wish to continue?
         */
        val WISH_TO_DROP_S1: SystemMessageId

        /**
         * ID: 401<br></br>
         * Message: You have to many ongoing quests.
         */
        val TOO_MANY_QUESTS: SystemMessageId

        /**
         * ID: 402<br></br>
         * Message: You do not possess the correct ticket to board the boat.
         */
        val NOT_CORRECT_BOAT_TICKET: SystemMessageId

        /**
         * ID: 403<br></br>
         * Message: You have exceeded your out-of-pocket adena limit.
         */
        val EXCEECED_POCKET_ADENA_LIMIT: SystemMessageId

        /**
         * ID: 404<br></br>
         * Message: Your Create Item level is too low to register this recipe.
         */
        val CREATE_LVL_TOO_LOW_TO_REGISTER: SystemMessageId

        /**
         * ID: 405<br></br>
         * Message: The total price of the product is too high.
         */
        val TOTAL_PRICE_TOO_HIGH: SystemMessageId

        /**
         * ID: 406<br></br>
         * Message: Petition application accepted.
         */
        val PETITION_APP_ACCEPTED: SystemMessageId

        /**
         * ID: 407<br></br>
         * Message: Petition under process.
         */
        val PETITION_UNDER_PROCESS: SystemMessageId

        /**
         * ID: 408<br></br>
         * Message: Set Period
         */
        val SET_PERIOD: SystemMessageId

        /**
         * ID: 409<br></br>
         * Message: Set Time-$s1:$s2:$s3
         */
        val SET_TIME_S1_S2_S3: SystemMessageId

        /**
         * ID: 410<br></br>
         * Message: Registration Period
         */
        val REGISTRATION_PERIOD: SystemMessageId

        /**
         * ID: 411<br></br>
         * Message: Registration Time-$s1:$s2:$s3
         */
        val REGISTRATION_TIME_S1_S2_S3: SystemMessageId

        /**
         * ID: 412<br></br>
         * Message: Battle begins in $s1:$s2:$s3
         */
        val BATTLE_BEGINS_S1_S2_S3: SystemMessageId

        /**
         * ID: 413<br></br>
         * Message: Battle ends in $s1:$s2:$s3
         */
        val BATTLE_ENDS_S1_S2_S3: SystemMessageId

        /**
         * ID: 414<br></br>
         * Message: Standby
         */
        val STANDBY: SystemMessageId

        /**
         * ID: 415<br></br>
         * Message: Under Siege
         */
        val UNDER_SIEGE: SystemMessageId

        /**
         * ID: 416<br></br>
         * Message: This item cannot be exchanged.
         */
        val ITEM_CANNOT_EXCHANGE: SystemMessageId

        /**
         * ID: 417<br></br>
         * Message: $s1 has been disarmed.
         */
        val S1_DISARMED: SystemMessageId

        /**
         * ID: 419<br></br>
         * Message: $s1 minute(s) of usage time left.
         */
        val S1_MINUTES_USAGE_LEFT: SystemMessageId

        /**
         * ID: 420<br></br>
         * Message: Time expired.
         */
        val TIME_EXPIRED: SystemMessageId

        /**
         * ID: 421<br></br>
         * Message: Another person has logged in with the same account.
         */
        val ANOTHER_LOGIN_WITH_ACCOUNT: SystemMessageId

        /**
         * ID: 422<br></br>
         * Message: You have exceeded the weight limit.
         */
        val WEIGHT_LIMIT_EXCEEDED: SystemMessageId

        /**
         * ID: 423<br></br>
         * Message: You have cancelled the enchanting process.
         */
        val ENCHANT_SCROLL_CANCELLED: SystemMessageId

        /**
         * ID: 424<br></br>
         * Message: Does not fit strengthening conditions of the scroll.
         */
        val DOES_NOT_FIT_SCROLL_CONDITIONS: SystemMessageId

        /**
         * ID: 425<br></br>
         * Message: Your Create Item level is too low to register this recipe.
         */
        val CREATE_LVL_TOO_LOW_TO_REGISTER2: SystemMessageId

        /**
         * ID: 445<br></br>
         * Message: (Reference Number Regarding Membership Withdrawal Request: $s1)
         */
        val REFERENCE_MEMBERSHIP_WITHDRAWAL_S1: SystemMessageId

        /**
         * ID: 447<br></br>
         * Message: .
         */
        val DOT: SystemMessageId

        /**
         * ID: 448<br></br>
         * Message: There is a system error. Please log in again later.
         */
        val SYSTEM_ERROR_LOGIN_LATER: SystemMessageId

        /**
         * ID: 449<br></br>
         * Message: The password you have entered is incorrect.
         */
        val PASSWORD_ENTERED_INCORRECT1: SystemMessageId

        /**
         * ID: 450<br></br>
         * Message: Confirm your account information and log in later.
         */
        val CONFIRM_ACCOUNT_LOGIN_LATER: SystemMessageId

        /**
         * ID: 451<br></br>
         * Message: The password you have entered is incorrect.
         */
        val PASSWORD_ENTERED_INCORRECT2: SystemMessageId

        /**
         * ID: 452<br></br>
         * Message: Please confirm your account information and try logging in later.
         */
        val PLEASE_CONFIRM_ACCOUNT_LOGIN_LATER: SystemMessageId

        /**
         * ID: 453<br></br>
         * Message: Your account information is incorrect.
         */
        val ACCOUNT_INFORMATION_INCORRECT: SystemMessageId

        /**
         * ID: 455<br></br>
         * Message: Account is already in use. Unable to log in.
         */
        val ACCOUNT_IN_USE: SystemMessageId

        /**
         * ID: 456<br></br>
         * Message: Lineage II game services may be used by individuals 15 years of age or older except for PvP servers,which may only be used by adults 18 years of age and older (Korea Only)
         */
        val LINAGE_MINIMUM_AGE: SystemMessageId

        /**
         * ID: 457<br></br>
         * Message: Currently undergoing game server maintenance. Please log in again later.
         */
        val SERVER_MAINTENANCE: SystemMessageId

        /**
         * ID: 458<br></br>
         * Message: Your usage term has expired.
         */
        val USAGE_TERM_EXPIRED: SystemMessageId

        /**
         * ID: 460<br></br>
         * Message: to reactivate your account.
         */
        val TO_REACTIVATE_YOUR_ACCOUNT: SystemMessageId

        /**
         * ID: 461<br></br>
         * Message: Access failed.
         */
        val ACCESS_FAILED: SystemMessageId

        /**
         * ID: 461<br></br>
         * Message: Please try again later.
         */
        val PLEASE_TRY_AGAIN_LATER: SystemMessageId

        /**
         * ID: 464<br></br>
         * Message: This feature is only available alliance leaders.
         */
        val FEATURE_ONLY_FOR_ALLIANCE_LEADER: SystemMessageId

        /**
         * ID: 465<br></br>
         * Message: You are not currently allied with any clans.
         */
        val NO_CURRENT_ALLIANCES: SystemMessageId

        /**
         * ID: 466<br></br>
         * Message: You have exceeded the limit.
         */
        val YOU_HAVE_EXCEEDED_THE_LIMIT: SystemMessageId

        /**
         * ID: 467<br></br>
         * Message: You may not accept any clan within a day after expelling another clan.
         */
        val CANT_INVITE_CLAN_WITHIN_1_DAY: SystemMessageId

        /**
         * ID: 468<br></br>
         * Message: A clan that has withdrawn or been expelled cannot enter into an alliance within one day of withdrawal or expulsion.
         */
        val CANT_ENTER_ALLIANCE_WITHIN_1_DAY: SystemMessageId

        /**
         * ID: 469<br></br>
         * Message: You may not ally with a clan you are currently at war with. That would be diabolical and treacherous.
         */
        val MAY_NOT_ALLY_CLAN_BATTLE: SystemMessageId

        /**
         * ID: 470<br></br>
         * Message: Only the clan leader may apply for withdrawal from the alliance.
         */
        val ONLY_CLAN_LEADER_WITHDRAW_ALLY: SystemMessageId

        /**
         * ID: 471<br></br>
         * Message: Alliance leaders cannot withdraw.
         */
        val ALLIANCE_LEADER_CANT_WITHDRAW: SystemMessageId

        /**
         * ID: 472<br></br>
         * Message: You cannot expel yourself from the clan.
         */
        val CANNOT_EXPEL_YOURSELF: SystemMessageId

        /**
         * ID: 473<br></br>
         * Message: Different alliance.
         */
        val DIFFERENT_ALLIANCE: SystemMessageId

        /**
         * ID: 474<br></br>
         * Message: That clan does not exist.
         */
        val CLAN_DOESNT_EXISTS: SystemMessageId

        /**
         * ID: 475<br></br>
         * Message: Different alliance.
         */
        val DIFFERENT_ALLIANCE2: SystemMessageId

        /**
         * ID: 476<br></br>
         * Message: Please adjust the image size to 8x12.
         */
        val ADJUST_IMAGE_8_12: SystemMessageId

        /**
         * ID: 477<br></br>
         * Message: No response. Invitation to join an alliance has been cancelled.
         */
        val NO_RESPONSE_TO_ALLY_INVITATION: SystemMessageId

        /**
         * ID: 478<br></br>
         * Message: No response. Your entrance to the alliance has been cancelled.
         */
        val YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION: SystemMessageId

        /**
         * ID: 479<br></br>
         * Message: $s1 has joined as a friend.
         */
        val S1_JOINED_AS_FRIEND: SystemMessageId

        /**
         * ID: 480<br></br>
         * Message: Please check your friend list.
         */
        val PLEASE_CHECK_YOUR_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 481<br></br>
         * Message: $s1 has been deleted from your friends list.
         */
        val S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 482<br></br>
         * Message: You cannot add yourself to your own friend list.
         */
        val YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 483<br></br>
         * Message: This function is inaccessible right now. Please try again later.
         */
        val FUNCTION_INACCESSIBLE_NOW: SystemMessageId

        /**
         * ID: 484<br></br>
         * Message: This player is already registered in your friends list.
         */
        val S1_ALREADY_IN_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 485<br></br>
         * Message: No new friend invitations may be accepted.
         */
        val NO_NEW_INVITATIONS_ACCEPTED: SystemMessageId

        /**
         * ID: 486<br></br>
         * Message: The following user is not in your friends list.
         */
        val THE_USER_NOT_IN_FRIENDS_LIST: SystemMessageId

        /**
         * ID: 487<br></br>
         * Message: ======<Friends List>======
        </Friends> */
        val FRIEND_LIST_HEADER: SystemMessageId

        /**
         * ID: 488<br></br>
         * Message: $s1 (Currently: Online)
         */
        val S1_ONLINE: SystemMessageId

        /**
         * ID: 489<br></br>
         * Message: $s1 (Currently: Offline)
         */
        val S1_OFFLINE: SystemMessageId

        /**
         * ID: 490<br></br>
         * Message: ========================
         */
        val FRIEND_LIST_FOOTER: SystemMessageId

        /**
         * ID: 491<br></br>
         * Message: =======<Alliance Information>=======
        </Alliance> */
        val ALLIANCE_INFO_HEAD: SystemMessageId

        /**
         * ID: 492<br></br>
         * Message: Alliance Name: $s1
         */
        val ALLIANCE_NAME_S1: SystemMessageId

        /**
         * ID: 493<br></br>
         * Message: Connection: $s1 / Total $s2
         */
        val CONNECTION_S1_TOTAL_S2: SystemMessageId

        /**
         * ID: 494<br></br>
         * Message: Alliance Leader: $s2 of $s1
         */
        val ALLIANCE_LEADER_S2_OF_S1: SystemMessageId

        /**
         * ID: 495<br></br>
         * Message: Affiliated clans: Total $s1 clan(s)
         */
        val ALLIANCE_CLAN_TOTAL_S1: SystemMessageId

        /**
         * ID: 496<br></br>
         * Message: =====<Clan Information>=====
        </Clan> */
        val CLAN_INFO_HEAD: SystemMessageId

        /**
         * ID: 497<br></br>
         * Message: Clan Name: $s1
         */
        val CLAN_INFO_NAME_S1: SystemMessageId

        /**
         * ID: 498<br></br>
         * Message: Clan Leader: $s1
         */
        val CLAN_INFO_LEADER_S1: SystemMessageId

        /**
         * ID: 499<br></br>
         * Message: Clan Level: $s1
         */
        val CLAN_INFO_LEVEL_S1: SystemMessageId

        /**
         * ID: 500<br></br>
         * Message: ------------------------
         */
        val CLAN_INFO_SEPARATOR: SystemMessageId

        /**
         * ID: 501<br></br>
         * Message: ========================
         */
        val CLAN_INFO_FOOT: SystemMessageId

        /**
         * ID: 502<br></br>
         * Message: You already belong to another alliance.
         */
        val ALREADY_JOINED_ALLIANCE: SystemMessageId

        /**
         * ID: 503<br></br>
         * Message: $s1 (Friend) has logged in.
         */
        val FRIEND_S1_HAS_LOGGED_IN: SystemMessageId

        /**
         * ID: 504<br></br>
         * Message: Only clan leaders may create alliances.
         */
        val ONLY_CLAN_LEADER_CREATE_ALLIANCE: SystemMessageId

        /**
         * ID: 505<br></br>
         * Message: You cannot create a new alliance within 10 days after dissolution.
         */
        val CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION: SystemMessageId

        /**
         * ID: 506<br></br>
         * Message: Incorrect alliance name. Please try again.
         */
        val INCORRECT_ALLIANCE_NAME: SystemMessageId

        /**
         * ID: 507<br></br>
         * Message: Incorrect length for an alliance name.
         */
        val INCORRECT_ALLIANCE_NAME_LENGTH: SystemMessageId

        /**
         * ID: 508<br></br>
         * Message: This alliance name already exists.
         */
        val ALLIANCE_ALREADY_EXISTS: SystemMessageId

        /**
         * ID: 509<br></br>
         * Message: Cannot accept. clan ally is registered as an enemy during siege battle.
         */
        val CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE: SystemMessageId

        /**
         * ID: 510<br></br>
         * Message: You have invited someone to your alliance.
         */
        val YOU_INVITED_FOR_ALLIANCE: SystemMessageId

        /**
         * ID: 511<br></br>
         * Message: You must first select a user to invite.
         */
        val SELECT_USER_TO_INVITE: SystemMessageId

        /**
         * ID: 512<br></br>
         * Message: Do you really wish to withdraw from the alliance?
         */
        val DO_YOU_WISH_TO_WITHDRW: SystemMessageId

        /**
         * ID: 513<br></br>
         * Message: Enter the name of the clan you wish to expel.
         */
        val ENTER_NAME_CLAN_TO_EXPEL: SystemMessageId

        /**
         * ID: 514<br></br>
         * Message: Do you really wish to dissolve the alliance?
         */
        val DO_YOU_WISH_TO_DISOLVE: SystemMessageId

        /**
         * ID: 516<br></br>
         * Message: $s1 has invited you to be their friend.
         */
        val SI_INVITED_YOU_AS_FRIEND: SystemMessageId

        /**
         * ID: 517<br></br>
         * Message: You have accepted the alliance.
         */
        val YOU_ACCEPTED_ALLIANCE: SystemMessageId

        /**
         * ID: 518<br></br>
         * Message: You have failed to invite a clan into the alliance.
         */
        val FAILED_TO_INVITE_CLAN_IN_ALLIANCE: SystemMessageId

        /**
         * ID: 519<br></br>
         * Message: You have withdrawn from the alliance.
         */
        val YOU_HAVE_WITHDRAWN_FROM_ALLIANCE: SystemMessageId

        /**
         * ID: 520<br></br>
         * Message: You have failed to withdraw from the alliance.
         */
        val YOU_HAVE_FAILED_TO_WITHDRAWN_FROM_ALLIANCE: SystemMessageId

        /**
         * ID: 521<br></br>
         * Message: You have succeeded in expelling a clan.
         */
        val YOU_HAVE_EXPELED_A_CLAN: SystemMessageId

        /**
         * ID: 522<br></br>
         * Message: You have failed to expel a clan.
         */
        val FAILED_TO_EXPELED_A_CLAN: SystemMessageId

        /**
         * ID: 523<br></br>
         * Message: The alliance has been dissolved.
         */
        val ALLIANCE_DISOLVED: SystemMessageId

        /**
         * ID: 524<br></br>
         * Message: You have failed to dissolve the alliance.
         */
        val FAILED_TO_DISOLVE_ALLIANCE: SystemMessageId

        /**
         * ID: 525<br></br>
         * Message: You have succeeded in inviting a friend to your friends list.
         */
        val YOU_HAVE_SUCCEEDED_INVITING_FRIEND: SystemMessageId

        /**
         * ID: 526<br></br>
         * Message: You have failed to add a friend to your friends list.
         */
        val FAILED_TO_INVITE_A_FRIEND: SystemMessageId

        /**
         * ID: 527<br></br>
         * Message: $s1 leader, $s2, has requested an alliance.
         */
        val S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE: SystemMessageId

        /**
         * ID: 530<br></br>
         * Message: The Spiritshot does not match the weapon's grade.
         */
        val SPIRITSHOTS_GRADE_MISMATCH: SystemMessageId

        /**
         * ID: 531<br></br>
         * Message: You do not have enough Spiritshots for that.
         */
        val NOT_ENOUGH_SPIRITSHOTS: SystemMessageId

        /**
         * ID: 532<br></br>
         * Message: You may not use Spiritshots.
         */
        val CANNOT_USE_SPIRITSHOTS: SystemMessageId

        /**
         * ID: 533<br></br>
         * Message: Power of Mana enabled.
         */
        val ENABLED_SPIRITSHOT: SystemMessageId

        /**
         * ID: 534<br></br>
         * Message: Power of Mana disabled.
         */
        val DISABLED_SPIRITSHOT: SystemMessageId

        /**
         * ID: 536<br></br>
         * Message: How much adena do you wish to transfer to your Inventory?
         */
        val HOW_MUCH_ADENA_TRANSFER: SystemMessageId

        /**
         * ID: 537<br></br>
         * Message: How much will you transfer?
         */
        val HOW_MUCH_TRANSFER: SystemMessageId

        /**
         * ID: 538<br></br>
         * Message: Your SP has decreased by $s1.
         */
        val SP_DECREASED_S1: SystemMessageId

        /**
         * ID: 539<br></br>
         * Message: Your Experience has decreased by $s1.
         */
        val EXP_DECREASED_BY_S1: SystemMessageId

        /**
         * ID: 540<br></br>
         * Message: Clan leaders may not be deleted. Dissolve the clan first and try again.
         */
        val CLAN_LEADERS_MAY_NOT_BE_DELETED: SystemMessageId

        /**
         * ID: 541<br></br>
         * Message: You may not delete a clan member. Withdraw from the clan first and try again.
         */
        val CLAN_MEMBER_MAY_NOT_BE_DELETED: SystemMessageId

        /**
         * ID: 542<br></br>
         * Message: The NPC server is currently down. Pets and servitors cannot be summoned at this time.
         */
        val THE_NPC_SERVER_IS_CURRENTLY_DOWN: SystemMessageId

        /**
         * ID: 543<br></br>
         * Message: You already have a pet.
         */
        val YOU_ALREADY_HAVE_A_PET: SystemMessageId

        /**
         * ID: 544<br></br>
         * Message: Your pet cannot carry this item.
         */
        val ITEM_NOT_FOR_PETS: SystemMessageId

        /**
         * ID: 545<br></br>
         * Message: Your pet cannot carry any more items. Remove some, then try again.
         */
        val YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS: SystemMessageId

        /**
         * ID: 546<br></br>
         * Message: Unable to place item, your pet is too encumbered.
         */
        val UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED: SystemMessageId

        /**
         * ID: 547<br></br>
         * Message: Summoning your pet.
         */
        val SUMMON_A_PET: SystemMessageId

        /**
         * ID: 548<br></br>
         * Message: Your pet's name can be up to 8 characters in length.
         */
        val NAMING_PETNAME_UP_TO_8CHARS: SystemMessageId

        /**
         * ID: 549<br></br>
         * Message: To create an alliance, your clan must be Level 5 or higher.
         */
        val TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER: SystemMessageId

        /**
         * ID: 550<br></br>
         * Message: You may not create an alliance during the term of dissolution postponement.
         */
        val YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING: SystemMessageId

        /**
         * ID: 551<br></br>
         * Message: You cannot raise your clan level during the term of dispersion postponement.
         */
        val CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS: SystemMessageId

        /**
         * ID: 552<br></br>
         * Message: During the grace period for dissolving a clan, the registration or deletion of a clan's crest is not allowed.
         */
        val CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS: SystemMessageId

        /**
         * ID: 553<br></br>
         * Message: The opposing clan has applied for dispersion.
         */
        val OPPOSING_CLAN_APPLIED_DISPERSION: SystemMessageId

        /**
         * ID: 554<br></br>
         * Message: You cannot disperse the clans in your alliance.
         */
        val CANNOT_DISPERSE_THE_CLANS_IN_ALLY: SystemMessageId

        /**
         * ID: 555<br></br>
         * Message: You cannot move - you are too encumbered
         */
        val CANT_MOVE_TOO_ENCUMBERED: SystemMessageId

        /**
         * ID: 556<br></br>
         * Message: You cannot move in this state
         */
        val CANT_MOVE_IN_THIS_STATE: SystemMessageId

        /**
         * ID: 557<br></br>
         * Message: Your pet has been summoned and may not be destroyed
         */
        val PET_SUMMONED_MAY_NOT_DESTROYED: SystemMessageId

        /**
         * ID: 558<br></br>
         * Message: Your pet has been summoned and may not be let go.
         */
        val PET_SUMMONED_MAY_NOT_LET_GO: SystemMessageId

        /**
         * ID: 559<br></br>
         * Message: You have purchased $s2 from $s1.
         */
        val PURCHASED_S2_FROM_S1: SystemMessageId

        /**
         * ID: 560<br></br>
         * Message: You have purchased +$s2 $s3 from $s1.
         */
        val PURCHASED_S2_S3_FROM_S1: SystemMessageId

        /**
         * ID: 561<br></br>
         * Message: You have purchased $s3 $s2(s) from $s1.
         */
        val PURCHASED_S3_S2_S_FROM_S1: SystemMessageId

        /**
         * ID: 562<br></br>
         * Message: You may not crystallize this item. Your crystallization skill level is too low.
         */
        val CRYSTALLIZE_LEVEL_TOO_LOW: SystemMessageId

        /**
         * ID: 563<br></br>
         * Message: Failed to disable attack target.
         */
        val FAILED_DISABLE_TARGET: SystemMessageId

        /**
         * ID: 564<br></br>
         * Message: Failed to change attack target.
         */
        val FAILED_CHANGE_TARGET: SystemMessageId

        /**
         * ID: 565<br></br>
         * Message: Not enough luck.
         */
        val NOT_ENOUGH_LUCK: SystemMessageId

        /**
         * ID: 566<br></br>
         * Message: Your confusion spell failed.
         */
        val CONFUSION_FAILED: SystemMessageId

        /**
         * ID: 567<br></br>
         * Message: Your fear spell failed.
         */
        val FEAR_FAILED: SystemMessageId

        /**
         * ID: 568<br></br>
         * Message: Cubic Summoning failed.
         */
        val CUBIC_SUMMONING_FAILED: SystemMessageId

        /**
         * ID: 572<br></br>
         * Message: Do you accept $s1's party invitation? (Item Distribution: Finders Keepers.)
         */
        val S1_INVITED_YOU_TO_PARTY_FINDERS_KEEPERS: SystemMessageId

        /**
         * ID: 573<br></br>
         * Message: Do you accept $s1's party invitation? (Item Distribution: Random.)
         */
        val S1_INVITED_YOU_TO_PARTY_RANDOM: SystemMessageId

        /**
         * ID: 574<br></br>
         * Message: Pets and Servitors are not available at this time.
         */
        val PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME: SystemMessageId

        /**
         * ID: 575<br></br>
         * Message: How much adena do you wish to transfer to your pet?
         */
        val HOW_MUCH_ADENA_TRANSFER_TO_PET: SystemMessageId

        /**
         * ID: 576<br></br>
         * Message: How much do you wish to transfer?
         */
        val HOW_MUCH_TRANSFER2: SystemMessageId

        /**
         * ID: 577<br></br>
         * Message: You cannot summon during a trade or while using the private shops.
         */
        val CANNOT_SUMMON_DURING_TRADE_SHOP: SystemMessageId

        /**
         * ID: 578<br></br>
         * Message: You cannot summon during combat.
         */
        val YOU_CANNOT_SUMMON_IN_COMBAT: SystemMessageId

        /**
         * ID: 579<br></br>
         * Message: A pet cannot be sent back during battle.
         */
        val PET_CANNOT_SENT_BACK_DURING_BATTLE: SystemMessageId

        /**
         * ID: 580<br></br>
         * Message: You may not use multiple pets or servitors at the same time.
         */
        val SUMMON_ONLY_ONE: SystemMessageId

        /**
         * ID: 581<br></br>
         * Message: There is a space in the name.
         */
        val NAMING_THERE_IS_A_SPACE: SystemMessageId

        /**
         * ID: 582<br></br>
         * Message: Inappropriate character name.
         */
        val NAMING_INAPPROPRIATE_CHARACTER_NAME: SystemMessageId

        /**
         * ID: 583<br></br>
         * Message: Name includes forbidden words.
         */
        val NAMING_INCLUDES_FORBIDDEN_WORDS: SystemMessageId

        /**
         * ID: 584<br></br>
         * Message: This is already in use by another pet.
         */
        val NAMING_ALREADY_IN_USE_BY_ANOTHER_PET: SystemMessageId

        /**
         * ID: 585<br></br>
         * Message: Please decide on the price.
         */
        val DECIDE_ON_PRICE: SystemMessageId

        /**
         * ID: 586<br></br>
         * Message: Pet items cannot be registered as shortcuts.
         */
        val PET_NO_SHORTCUT: SystemMessageId

        /**
         * ID: 588<br></br>
         * Message: Your pet's inventory is full.
         */
        val PET_INVENTORY_FULL: SystemMessageId

        /**
         * ID: 589<br></br>
         * Message: A dead pet cannot be sent back.
         */
        val DEAD_PET_CANNOT_BE_RETURNED: SystemMessageId

        /**
         * ID: 590<br></br>
         * Message: Your pet is motionless and any attempt you make to give it something goes unrecognized.
         */
        val CANNOT_GIVE_ITEMS_TO_DEAD_PET: SystemMessageId

        /**
         * ID: 591<br></br>
         * Message: An invalid character is included in the pet's name.
         */
        val NAMING_PETNAME_CONTAINS_INVALID_CHARS: SystemMessageId

        /**
         * ID: 592<br></br>
         * Message: Do you wish to dismiss your pet? Dismissing your pet will cause the pet necklace to disappear
         */
        val WISH_TO_DISMISS_PET: SystemMessageId

        /**
         * ID: 593<br></br>
         * Message: Starving, grumpy and fed up, your pet has left.
         */
        val STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT: SystemMessageId

        /**
         * ID: 594<br></br>
         * Message: You may not restore a hungry pet.
         */
        val YOU_CANNOT_RESTORE_HUNGRY_PETS: SystemMessageId

        /**
         * ID: 595<br></br>
         * Message: Your pet is very hungry.
         */
        val YOUR_PET_IS_VERY_HUNGRY: SystemMessageId

        /**
         * ID: 596<br></br>
         * Message: Your pet ate a little, but is still hungry.
         */
        val YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY: SystemMessageId

        /**
         * ID: 597<br></br>
         * Message: Your pet is very hungry. Please be careful.
         */
        val YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL: SystemMessageId

        /**
         * ID: 598<br></br>
         * Message: You may not chat while you are invisible.
         */
        val NOT_CHAT_WHILE_INVISIBLE: SystemMessageId

        /**
         * ID: 599<br></br>
         * Message: The GM has an important notice. Chat has been temporarily disabled.
         */
        val GM_NOTICE_CHAT_DISABLED: SystemMessageId

        /**
         * ID: 600<br></br>
         * Message: You may not equip a pet item.
         */
        val CANNOT_EQUIP_PET_ITEM: SystemMessageId

        /**
         * ID: 601<br></br>
         * Message: There are $S1 petitions currently on the waiting list.
         */
        val S1_PETITION_ON_WAITING_LIST: SystemMessageId

        /**
         * ID: 602<br></br>
         * Message: The petition system is currently unavailable. Please try again later.
         */
        val PETITION_SYSTEM_CURRENT_UNAVAILABLE: SystemMessageId

        /**
         * ID: 603<br></br>
         * Message: That item cannot be discarded or exchanged.
         */
        val CANNOT_DISCARD_EXCHANGE_ITEM: SystemMessageId

        /**
         * ID: 604<br></br>
         * Message: You may not call forth a pet or summoned creature from this location
         */
        val NOT_CALL_PET_FROM_THIS_LOCATION: SystemMessageId

        /**
         * ID: 605<br></br>
         * Message: You may register up to 64 people on your list.
         */
        val MAY_REGISTER_UP_TO_64_PEOPLE: SystemMessageId

        /**
         * ID: 606<br></br>
         * Message: You cannot be registered because the other person has already registered 64 people on his/her list.
         */
        val OTHER_PERSON_ALREADY_64_PEOPLE: SystemMessageId

        /**
         * ID: 607<br></br>
         * Message: You do not have any further skills to learn. Come back when you have reached Level $s1.
         */
        val DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1: SystemMessageId

        /**
         * ID: 608<br></br>
         * Message: $s1 has obtained $s3 $s2 by using Sweeper.
         */
        val S1_SWEEPED_UP_S3_S2: SystemMessageId

        /**
         * ID: 609<br></br>
         * Message: $s1 has obtained $s2 by using Sweeper.
         */
        val S1_SWEEPED_UP_S2: SystemMessageId

        /**
         * ID: 610<br></br>
         * Message: Your skill has been canceled due to lack of HP.
         */
        val SKILL_REMOVED_DUE_LACK_HP: SystemMessageId

        /**
         * ID: 611<br></br>
         * Message: You have succeeded in Confusing the enemy.
         */
        val CONFUSING_SUCCEEDED: SystemMessageId

        /**
         * ID: 612<br></br>
         * Message: The Spoil condition has been activated.
         */
        val SPOIL_SUCCESS: SystemMessageId

        /**
         * ID: 613<br></br>
         * Message: ======<Ignore List>======
        </Ignore> */
        val BLOCK_LIST_HEADER: SystemMessageId

        /**
         * ID: 614<br></br>
         * Message: $s1 : $s2
         */
        val S1_S2: SystemMessageId

        /**
         * ID: 615<br></br>
         * Message: You have failed to register the user to your Ignore List.
         */
        val FAILED_TO_REGISTER_TO_IGNORE_LIST: SystemMessageId

        /**
         * ID: 616<br></br>
         * Message: You have failed to delete the character.
         */
        val FAILED_TO_DELETE_CHARACTER: SystemMessageId

        /**
         * ID: 617<br></br>
         * Message: $s1 has been added to your Ignore List.
         */
        val S1_WAS_ADDED_TO_YOUR_IGNORE_LIST: SystemMessageId

        /**
         * ID: 618<br></br>
         * Message: $s1 has been removed from your Ignore List.
         */
        val S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST: SystemMessageId

        /**
         * ID: 619<br></br>
         * Message: $s1 has placed you on his/her Ignore List.
         */
        val S1_HAS_ADDED_YOU_TO_IGNORE_LIST: SystemMessageId

        /**
         * ID: 620<br></br>
         * Message: $s1 has placed you on his/her Ignore List.
         */
        val S1_HAS_ADDED_YOU_TO_IGNORE_LIST2: SystemMessageId

        /**
         * ID: 621<br></br>
         * Message: Game connection attempted through a restricted IP.
         */
        val CONNECTION_RESTRICTED_IP: SystemMessageId

        /**
         * ID: 622<br></br>
         * Message: You may not make a declaration of war during an alliance battle.
         */
        val NO_WAR_DURING_ALLY_BATTLE: SystemMessageId

        /**
         * ID: 623<br></br>
         * Message: Your opponent has exceeded the number of simultaneous alliance battles alllowed.
         */
        val OPPONENT_TOO_MUCH_ALLY_BATTLES1: SystemMessageId

        /**
         * ID: 624<br></br>
         * Message: $s1 Clan leader is not currently connected to the game server.
         */
        val S1_LEADER_NOT_CONNECTED: SystemMessageId

        /**
         * ID: 625<br></br>
         * Message: Your request for Alliance Battle truce has been denied.
         */
        val ALLY_BATTLE_TRUCE_DENIED: SystemMessageId

        /**
         * ID: 626<br></br>
         * Message: The $s1 clan did not respond: war proclamation has been refused.
         */
        val WAR_PROCLAMATION_HAS_BEEN_REFUSED: SystemMessageId

        /**
         * ID: 627<br></br>
         * Message: Clan battle has been refused because you did not respond to $s1 clan's war proclamation.
         */
        val YOU_REFUSED_CLAN_WAR_PROCLAMATION: SystemMessageId

        /**
         * ID: 628<br></br>
         * Message: You have already been at war with the $s1 clan: 5 days must pass before you can declare war again.
         */
        val ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS: SystemMessageId

        /**
         * ID: 629<br></br>
         * Message: Your opponent has exceeded the number of simultaneous alliance battles alllowed.
         */
        val OPPONENT_TOO_MUCH_ALLY_BATTLES2: SystemMessageId

        /**
         * ID: 630<br></br>
         * Message: War with the clan has begun.
         */
        val WAR_WITH_CLAN_BEGUN: SystemMessageId

        /**
         * ID: 631<br></br>
         * Message: War with the clan is over.
         */
        val WAR_WITH_CLAN_ENDED: SystemMessageId

        /**
         * ID: 632<br></br>
         * Message: You have won the war over the clan!
         */
        val WON_WAR_OVER_CLAN: SystemMessageId

        /**
         * ID: 633<br></br>
         * Message: You have surrendered to the clan.
         */
        val SURRENDERED_TO_CLAN: SystemMessageId

        /**
         * ID: 634<br></br>
         * Message: Your alliance leader has been slain. You have been defeated by the clan.
         */
        val DEFEATED_BY_CLAN: SystemMessageId

        /**
         * ID: 635<br></br>
         * Message: The time limit for the clan war has been exceeded. War with the clan is over.
         */
        val TIME_UP_WAR_OVER: SystemMessageId

        /**
         * ID: 636<br></br>
         * Message: You are not involved in a clan war.
         */
        val NOT_INVOLVED_IN_WAR: SystemMessageId

        /**
         * ID: 637<br></br>
         * Message: A clan ally has registered itself to the opponent.
         */
        val ALLY_REGISTERED_SELF_TO_OPPONENT: SystemMessageId

        /**
         * ID: 638<br></br>
         * Message: You have already requested a Siege Battle.
         */
        val ALREADY_REQUESTED_SIEGE_BATTLE: SystemMessageId

        /**
         * ID: 639<br></br>
         * Message: Your application has been denied because you have already submitted a request for another Siege Battle.
         */
        val APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE: SystemMessageId

        // 640 - 641: empty

        /**
         * ID: 642<br></br>
         * Message: You are already registered to the attacker side and must not cancel your registration before submitting your request
         */
        val ALREADY_ATTACKER_NOT_CANCEL: SystemMessageId

        /**
         * ID: 643<br></br>
         * Message: You are already registered to the defender side and must not cancel your registration before submitting your request
         */
        val ALREADY_DEFENDER_NOT_CANCEL: SystemMessageId

        /**
         * ID: 644<br></br>
         * Message: You are not yet registered for the castle siege.
         */
        val NOT_REGISTERED_FOR_SIEGE: SystemMessageId

        /**
         * ID: 645<br></br>
         * Message: Only clans of level 4 or higher may register for a castle siege.
         */
        val ONLY_CLAN_LEVEL_4_ABOVE_MAY_SIEGE: SystemMessageId

        // 646 - 647: empty

        /**
         * ID: 648<br></br>
         * Message: No more registrations may be accepted for the attacker side.
         */
        val ATTACKER_SIDE_FULL: SystemMessageId

        /**
         * ID: 649<br></br>
         * Message: No more registrations may be accepted for the defender side.
         */
        val DEFENDER_SIDE_FULL: SystemMessageId

        /**
         * ID: 650<br></br>
         * Message: You may not summon from your current location.
         */
        val YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION: SystemMessageId

        /**
         * ID: 651<br></br>
         * Message: Place $s1 in the current location and direction. Do you wish to continue?
         */
        val PLACE_S1_IN_CURRENT_LOCATION_AND_DIRECTION: SystemMessageId

        /**
         * ID: 652<br></br>
         * Message: The target of the summoned monster is wrong.
         */
        val TARGET_OF_SUMMON_WRONG: SystemMessageId

        /**
         * ID: 653<br></br>
         * Message: You do not have the authority to position mercenaries.
         */
        val YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES: SystemMessageId

        /**
         * ID: 654<br></br>
         * Message: You do not have the authority to cancel mercenary positioning.
         */
        val YOU_DO_NOT_HAVE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING: SystemMessageId

        /**
         * ID: 655<br></br>
         * Message: Mercenaries cannot be positioned here.
         */
        val MERCENARIES_CANNOT_BE_POSITIONED_HERE: SystemMessageId

        /**
         * ID: 656<br></br>
         * Message: This mercenary cannot be positioned anymore.
         */
        val THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE: SystemMessageId

        /**
         * ID: 657<br></br>
         * Message: Positioning cannot be done here because the distance between mercenaries is too short.
         */
        val POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT: SystemMessageId

        /**
         * ID: 658<br></br>
         * Message: This is not a mercenary of a castle that you own and so you cannot cancel its positioning.
         */
        val THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING: SystemMessageId

        /**
         * ID: 659<br></br>
         * Message: This is not the time for siege registration and so registrations cannot be accepted or rejected.
         */
        val NOT_SIEGE_REGISTRATION_TIME1: SystemMessageId

        /**
         * ID: 659<br></br>
         * Message: This is not the time for siege registration and so registration and cancellation cannot be done.
         */
        val NOT_SIEGE_REGISTRATION_TIME2: SystemMessageId

        /**
         * ID: 661<br></br>
         * Message: This character cannot be spoiled.
         */
        val SPOIL_CANNOT_USE: SystemMessageId

        /**
         * ID: 662<br></br>
         * Message: The other player is rejecting friend invitations.
         */
        val THE_PLAYER_IS_REJECTING_FRIEND_INVITATIONS: SystemMessageId

        // 663 will crash client

        /**
         * ID: 664<br></br>
         * Message: Please choose a person to receive.
         */
        val CHOOSE_PERSON_TO_RECEIVE: SystemMessageId

        /**
         * ID: 665<br></br>
         * Message: of alliance is applying for alliance war. Do you want to accept the challenge?
         */
        val APPLYING_ALLIANCE_WAR: SystemMessageId

        /**
         * ID: 666<br></br>
         * Message: A request for ceasefire has been received from alliance. Do you agree?
         */
        val REQUEST_FOR_CEASEFIRE: SystemMessageId

        /**
         * ID: 667<br></br>
         * Message: You are registering on the attacking side of the siege. Do you want to continue?
         */
        val REGISTERING_ON_ATTACKING_SIDE: SystemMessageId

        /**
         * ID: 668<br></br>
         * Message: You are registering on the defending side of the siege. Do you want to continue?
         */
        val REGISTERING_ON_DEFENDING_SIDE: SystemMessageId

        /**
         * ID: 669<br></br>
         * Message: You are canceling your application to participate in the siege battle. Do you want to continue?
         */
        val CANCELING_REGISTRATION: SystemMessageId

        /**
         * ID: 670<br></br>
         * Message: You are refusing the registration of clan on the defending side. Do you want to continue?
         */
        val REFUSING_REGISTRATION: SystemMessageId

        /**
         * ID: 671<br></br>
         * Message: You are agreeing to the registration of clan on the defending side. Do you want to continue?
         */
        val AGREEING_REGISTRATION: SystemMessageId

        /**
         * ID: 672<br></br>
         * Message: $s1 adena disappeared.
         */
        val S1_DISAPPEARED_ADENA: SystemMessageId

        /**
         * ID: 673<br></br>
         * Message: Only a clan leader whose clan is of level 2 or higher is allowed to participate in a clan hall auction.
         */
        val AUCTION_ONLY_CLAN_LEVEL_2_HIGHER: SystemMessageId

        /**
         * ID: 674<br></br>
         * Message: I has not yet been seven days since canceling an auction.
         */
        val NOT_SEVEN_DAYS_SINCE_CANCELING_AUCTION: SystemMessageId

        /**
         * ID: 675<br></br>
         * Message: There are no clan halls up for auction.
         */
        val NO_CLAN_HALLS_UP_FOR_AUCTION: SystemMessageId

        /**
         * ID: 676<br></br>
         * Message: Since you have already submitted a bid, you are not allowed to participate in another auction at this time.
         */
        val ALREADY_SUBMITTED_BID: SystemMessageId

        /**
         * ID: 677<br></br>
         * Message: Your bid price must be higher than the minimum price that can be bid.
         */
        val BID_PRICE_MUST_BE_HIGHER: SystemMessageId

        /**
         * ID: 678<br></br>
         * Message: You have submitted a bid for the auction of $s1.
         */
        val SUBMITTED_A_BID: SystemMessageId

        /**
         * ID: 679<br></br>
         * Message: You have canceled your bid.
         */
        val CANCELED_BID: SystemMessageId

        /**
         * ID: 680<br></br>
         * You cannot participate in an auction.
         */
        val CANNOT_PARTICIPATE_IN_AUCTION: SystemMessageId

        /**
         * ID: 681<br></br>
         * Message: The clan does not own a clan hall.
         */
        // CLAN_HAS_NO_CLAN_HALL(681) // Doesn't exist in Hellbound anymore

        /**
         * ID: 683<br></br>
         * Message: There are no priority rights on a sweeper.
         */
        val SWEEP_NOT_ALLOWED: SystemMessageId

        /**
         * ID: 684<br></br>
         * Message: You cannot position mercenaries during a siege.
         */
        val CANNOT_POSITION_MERCS_DURING_SIEGE: SystemMessageId

        /**
         * ID: 685<br></br>
         * Message: You cannot apply for clan war with a clan that belongs to the same alliance
         */
        val CANNOT_DECLARE_WAR_ON_ALLY: SystemMessageId

        /**
         * ID: 686<br></br>
         * Message: You have received $s1 damage from the fire of magic.
         */
        val S1_DAMAGE_FROM_FIRE_MAGIC: SystemMessageId

        /**
         * ID: 687<br></br>
         * Message: You cannot move while frozen. Please wait.
         */
        val CANNOT_MOVE_FROZEN: SystemMessageId

        /**
         * ID: 688<br></br>
         * Message: The clan that owns the castle is automatically registered on the defending side.
         */
        val CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING: SystemMessageId

        /**
         * ID: 689<br></br>
         * Message: A clan that owns a castle cannot participate in another siege.
         */
        val CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE: SystemMessageId

        /**
         * ID: 690<br></br>
         * Message: You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.
         */
        val CANNOT_ATTACK_ALLIANCE_CASTLE: SystemMessageId

        /**
         * ID: 691<br></br>
         * Message: $s1 clan is already a member of $s2 alliance.
         */
        val S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE: SystemMessageId

        /**
         * ID: 692<br></br>
         * Message: The other party is frozen. Please wait a moment.
         */
        val OTHER_PARTY_IS_FROZEN: SystemMessageId

        /**
         * ID: 693<br></br>
         * Message: The package that arrived is in another warehouse.
         */
        val PACKAGE_IN_ANOTHER_WAREHOUSE: SystemMessageId

        /**
         * ID: 694<br></br>
         * Message: No packages have arrived.
         */
        val NO_PACKAGES_ARRIVED: SystemMessageId

        /**
         * ID: 695<br></br>
         * Message: You cannot set the name of the pet.
         */
        val NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET: SystemMessageId

        /**
         * ID: 697<br></br>
         * Message: The item enchant value is strange
         */
        val ITEM_ENCHANT_VALUE_STRANGE: SystemMessageId

        /**
         * ID: 698<br></br>
         * Message: The price is different than the same item on the sales list.
         */
        val PRICE_DIFFERENT_FROM_SALES_LIST: SystemMessageId

        /**
         * ID: 699<br></br>
         * Message: Currently not purchasing.
         */
        val CURRENTLY_NOT_PURCHASING: SystemMessageId

        /**
         * ID: 700<br></br>
         * Message: The purchase is complete.
         */
        val THE_PURCHASE_IS_COMPLETE: SystemMessageId

        /**
         * ID: 701<br></br>
         * Message: You do not have enough required items.
         */
        val NOT_ENOUGH_REQUIRED_ITEMS: SystemMessageId

        /**
         * ID: 702 <br></br>
         * Message: There are no GMs currently visible in the public list as they may be performing other functions at the moment.
         */
        val NO_GM_PROVIDING_SERVICE_NOW: SystemMessageId

        /**
         * ID: 703<br></br>
         * Message: ======<GM List>======
        </GM> */
        val GM_LIST: SystemMessageId

        /**
         * ID: 704<br></br>
         * Message: GM : $s1
         */
        val GM_S1: SystemMessageId

        /**
         * ID: 705<br></br>
         * Message: You cannot exclude yourself.
         */
        val CANNOT_EXCLUDE_SELF: SystemMessageId

        /**
         * ID: 706<br></br>
         * Message: You can only register up to 64 names on your exclude list.
         */
        val ONLY_64_NAMES_ON_EXCLUDE_LIST: SystemMessageId

        /**
         * ID: 707<br></br>
         * Message: You cannot teleport to a village that is in a siege.
         */
        val CANNOT_PORT_VILLAGE_IN_SIEGE: SystemMessageId

        /**
         * ID: 708<br></br>
         * Message: You do not have the right to use the castle warehouse.
         */
        val YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CASTLE_WAREHOUSE: SystemMessageId

        /**
         * ID: 709<br></br>
         * Message: You do not have the right to use the clan warehouse.
         */
        val YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE: SystemMessageId

        /**
         * ID: 710<br></br>
         * Message: Only clans of clan level 1 or higher can use a clan warehouse.
         */
        val ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE: SystemMessageId

        /**
         * ID: 711<br></br>
         * Message: The siege of $s1 has started.
         */
        val SIEGE_OF_S1_HAS_STARTED: SystemMessageId

        /**
         * ID: 712<br></br>
         * Message: The siege of $s1 has finished.
         */
        val SIEGE_OF_S1_HAS_ENDED: SystemMessageId

        /**
         * ID: 713<br></br>
         * Message: $s1/$s2/$s3 :
         */
        val S1_S2_S3_D: SystemMessageId

        /**
         * ID: 714<br></br>
         * Message: A trap device has been tripped.
         */
        val A_TRAP_DEVICE_HAS_BEEN_TRIPPED: SystemMessageId

        /**
         * ID: 715<br></br>
         * Message: A trap device has been stopped.
         */
        val A_TRAP_DEVICE_HAS_BEEN_STOPPED: SystemMessageId

        /**
         * ID: 716<br></br>
         * Message: If a base camp does not exist, resurrection is not possible.
         */
        val NO_RESURRECTION_WITHOUT_BASE_CAMP: SystemMessageId

        /**
         * ID: 717<br></br>
         * Message: The guardian tower has been destroyed and resurrection is not possible
         */
        val TOWER_DESTROYED_NO_RESURRECTION: SystemMessageId

        /**
         * ID: 718<br></br>
         * Message: The castle gates cannot be opened and closed during a siege.
         */
        val GATES_NOT_OPENED_CLOSED_DURING_SIEGE: SystemMessageId

        /**
         * ID: 719<br></br>
         * Message: You failed at mixing the item.
         */
        val ITEM_MIXING_FAILED: SystemMessageId

        /**
         * ID: 720<br></br>
         * Message: The purchase price is higher than the amount of money that you have and so you cannot open a personal store.
         */
        val THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY: SystemMessageId

        /**
         * ID: 721<br></br>
         * Message: You cannot create an alliance while participating in a siege.
         */
        val NO_ALLY_CREATION_WHILE_SIEGE: SystemMessageId

        /**
         * ID: 722<br></br>
         * Message: You cannot dissolve an alliance while an affiliated clan is participating in a siege battle.
         */
        val CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE: SystemMessageId

        /**
         * ID: 723<br></br>
         * Message: The opposing clan is participating in a siege battle.
         */
        val OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE: SystemMessageId

        /**
         * ID: 724<br></br>
         * Message: You cannot leave while participating in a siege battle.
         */
        val CANNOT_LEAVE_WHILE_SIEGE: SystemMessageId

        /**
         * ID: 725<br></br>
         * Message: You cannot banish a clan from an alliance while the clan is participating in a siege
         */
        val CANNOT_DISMISS_WHILE_SIEGE: SystemMessageId

        /**
         * ID: 726<br></br>
         * Message: Frozen condition has started. Please wait a moment.
         */
        val FROZEN_CONDITION_STARTED: SystemMessageId

        /**
         * ID: 727<br></br>
         * Message: The frozen condition was removed.
         */
        val FROZEN_CONDITION_REMOVED: SystemMessageId

        /**
         * ID: 728<br></br>
         * Message: You cannot apply for dissolution again within seven days after a previous application for dissolution.
         */
        val CANNOT_APPLY_DISSOLUTION_AGAIN: SystemMessageId

        /**
         * ID: 729<br></br>
         * Message: That item cannot be discarded.
         */
        val ITEM_NOT_DISCARDED: SystemMessageId

        /**
         * ID: 730<br></br>
         * Message: - You have submitted your $s1th petition. - You may submit $s2 more petition(s) today.
         */
        val SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT: SystemMessageId

        /**
         * ID: 731<br></br>
         * Message: A petition has been received by the GM on behalf of $s1. The petition code is $s2.
         */
        val PETITION_S1_RECEIVED_CODE_IS_S2: SystemMessageId

        /**
         * ID: 732<br></br>
         * Message: $s1 has received a request for a consultation with the GM.
         */
        val S1_RECEIVED_CONSULTATION_REQUEST: SystemMessageId

        /**
         * ID: 733<br></br>
         * Message: We have received $s1 petitions from you today and that is the maximum that you can submit in one day. You cannot submit any more petitions.
         */
        val WE_HAVE_RECEIVED_S1_PETITIONS_TODAY: SystemMessageId

        /**
         * ID: 734<br></br>
         * Message: You have failed at submitting a petition on behalf of someone else. $s1 already submitted a petition.
         */
        val PETITION_FAILED_S1_ALREADY_SUBMITTED: SystemMessageId

        /**
         * ID: 735<br></br>
         * Message: You have failed at submitting a petition on behalf of $s1. The error number is $s2.
         */
        val PETITION_FAILED_FOR_S1_ERROR_NUMBER_S2: SystemMessageId

        /**
         * ID: 736<br></br>
         * Message: The petition was canceled. You may submit $s1 more petition(s) today.
         */
        val PETITION_CANCELED_SUBMIT_S1_MORE_TODAY: SystemMessageId

        /**
         * ID: 737<br></br>
         * Message: You have cancelled submitting a petition on behalf of $s1.
         */
        val CANCELED_PETITION_ON_S1: SystemMessageId

        /**
         * ID: 738<br></br>
         * Message: You have not submitted a petition.
         */
        val PETITION_NOT_SUBMITTED: SystemMessageId

        /**
         * ID: 739<br></br>
         * Message: You have failed at cancelling a petition on behalf of $s1. The error number is $s2.
         */
        val PETITION_CANCEL_FAILED_FOR_S1_ERROR_NUMBER_S2: SystemMessageId

        /**
         * ID: 740<br></br>
         * Message: $s1 participated in a petition chat at the request of the GM.
         */
        val S1_PARTICIPATE_PETITION: SystemMessageId

        /**
         * ID: 741<br></br>
         * Message: You have failed at adding $s1 to the petition chat. Petition has already been submitted.
         */
        val FAILED_ADDING_S1_TO_PETITION: SystemMessageId

        /**
         * ID: 742<br></br>
         * Message: You have failed at adding $s1 to the petition chat. The error code is $s2.
         */
        val PETITION_ADDING_S1_FAILED_ERROR_NUMBER_S2: SystemMessageId

        /**
         * ID: 743<br></br>
         * Message: $s1 left the petition chat.
         */
        val S1_LEFT_PETITION_CHAT: SystemMessageId

        /**
         * ID: 744<br></br>
         * Message: You have failed at removing $s1 from the petition chat. The error code is $s2.
         */
        val PETITION_REMOVING_S1_FAILED_ERROR_NUMBER_S2: SystemMessageId

        /**
         * ID: 745<br></br>
         * Message: You are currently not in a petition chat.
         */
        val YOU_ARE_NOT_IN_PETITION_CHAT: SystemMessageId

        /**
         * ID: 746<br></br>
         * Message: It is not currently a petition.
         */
        val CURRENTLY_NO_PETITION: SystemMessageId

        /**
         * ID: 748<br></br>
         * Message: The distance is too far and so the casting has been stopped.
         */
        val DIST_TOO_FAR_CASTING_STOPPED: SystemMessageId

        /**
         * ID: 749<br></br>
         * Message: The effect of $s1 has been removed.
         */
        val EFFECT_S1_DISAPPEARED: SystemMessageId

        /**
         * ID: 750<br></br>
         * Message: There are no other skills to learn.
         */
        val NO_MORE_SKILLS_TO_LEARN: SystemMessageId

        /**
         * ID: 751<br></br>
         * Message: As there is a conflict in the siege relationship with a clan in the alliance, you cannot invite that clan to the alliance.
         */
        val CANNOT_INVITE_CONFLICT_CLAN: SystemMessageId

        /**
         * ID: 752<br></br>
         * Message: That name cannot be used.
         */
        val CANNOT_USE_NAME: SystemMessageId

        /**
         * ID: 753<br></br>
         * Message: You cannot position mercenaries here.
         */
        val NO_MERCS_HERE: SystemMessageId

        /**
         * ID: 754<br></br>
         * Message: There are $s1 hours and $s2 minutes left in this week's usage time.
         */
        val S1_HOURS_S2_MINUTES_LEFT_THIS_WEEK: SystemMessageId

        /**
         * ID: 755<br></br>
         * Message: There are $s1 minutes left in this week's usage time.
         */
        val S1_MINUTES_LEFT_THIS_WEEK: SystemMessageId

        /**
         * ID: 756<br></br>
         * Message: This week's usage time has finished.
         */
        val WEEKS_USAGE_TIME_FINISHED: SystemMessageId

        /**
         * ID: 757<br></br>
         * Message: There are $s1 hours and $s2 minutes left in the fixed use time.
         */
        val S1_HOURS_S2_MINUTES_LEFT_IN_TIME: SystemMessageId

        /**
         * ID: 758<br></br>
         * Message: There are $s1 hours and $s2 minutes left in this week's play time.
         */
        val S1_HOURS_S2_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME: SystemMessageId

        /**
         * ID: 759<br></br>
         * Message: There are $s1 minutes left in this week's play time.
         */
        val S1_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME: SystemMessageId

        /**
         * ID: 760<br></br>
         * Message: $s1 cannot join the clan because one day has not yet passed since he/she left another clan.
         */
        val S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN: SystemMessageId

        /**
         * ID: 761<br></br>
         * Message: $s1 clan cannot join the alliance because one day has not yet passed since it left another alliance.
         */
        val S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY: SystemMessageId

        /**
         * ID: 762<br></br>
         * Message: $s1 rolled $s2 and $s3's eye came out.
         */
        val S1_ROLLED_S2_S3_EYE_CAME_OUT: SystemMessageId

        /**
         * ID: 763<br></br>
         * Message: You failed at sending the package because you are too far from the warehouse.
         */
        val FAILED_SENDING_PACKAGE_TOO_FAR: SystemMessageId

        /**
         * ID: 764<br></br>
         * Message: You have been playing for an extended period of time. Please consider taking a break.
         */
        val PLAYING_FOR_LONG_TIME: SystemMessageId

        /**
         * ID: 769<br></br>
         * Message: A hacking tool has been discovered. Please try again after closing unnecessary programs.
         */
        val HACKING_TOOL: SystemMessageId

        /**
         * ID: 774<br></br>
         * Message: Play time is no longer accumulating.
         */
        val PLAY_TIME_NO_LONGER_ACCUMULATING: SystemMessageId

        /**
         * ID: 775<br></br>
         * Message: From here on, play time will be expended.
         */
        val PLAY_TIME_EXPENDED: SystemMessageId

        /**
         * ID: 776<br></br>
         * Message: The clan hall which was put up for auction has been awarded to clan s1.
         */
        val CLANHALL_AWARDED_TO_CLAN_S1: SystemMessageId

        /**
         * ID: 777<br></br>
         * Message: The clan hall which was put up for auction was not sold and therefore has been re-listed.
         */
        val CLANHALL_NOT_SOLD: SystemMessageId

        /**
         * ID: 778<br></br>
         * Message: You may not log out from this location.
         */
        val NO_LOGOUT_HERE: SystemMessageId

        /**
         * ID: 779<br></br>
         * Message: You may not restart in this location.
         */
        val NO_RESTART_HERE: SystemMessageId

        /**
         * ID: 780<br></br>
         * Message: Observation is only possible during a siege.
         */
        val ONLY_VIEW_SIEGE: SystemMessageId

        /**
         * ID: 781<br></br>
         * Message: Observers cannot participate.
         */
        val OBSERVERS_CANNOT_PARTICIPATE: SystemMessageId

        /**
         * ID: 782<br></br>
         * Message: You may not observe a siege with a pet or servitor summoned.
         */
        val NO_OBSERVE_WITH_PET: SystemMessageId

        /**
         * ID: 783<br></br>
         * Message: Lottery ticket sales have been temporarily suspended.
         */
        val LOTTERY_TICKET_SALES_TEMP_SUSPENDED: SystemMessageId

        /**
         * ID: 784<br></br>
         * Message: Tickets for the current lottery are no longer available.
         */
        val NO_LOTTERY_TICKETS_AVAILABLE: SystemMessageId

        /**
         * ID: 785<br></br>
         * Message: The results of lottery number $s1 have not yet been published.
         */
        val LOTTERY_S1_RESULT_NOT_PUBLISHED: SystemMessageId

        /**
         * ID: 786<br></br>
         * Message: Incorrect syntax.
         */
        val INCORRECT_SYNTAX: SystemMessageId

        /**
         * ID: 787<br></br>
         * Message: The tryouts are finished.
         */
        val CLANHALL_SIEGE_TRYOUTS_FINISHED: SystemMessageId

        /**
         * ID: 788<br></br>
         * Message: The finals are finished.
         */
        val CLANHALL_SIEGE_FINALS_FINISHED: SystemMessageId

        /**
         * ID: 789<br></br>
         * Message: The tryouts have begun.
         */
        val CLANHALL_SIEGE_TRYOUTS_BEGUN: SystemMessageId

        /**
         * ID: 790<br></br>
         * Message: The finals are finished.
         */
        val CLANHALL_SIEGE_FINALS_BEGUN: SystemMessageId

        /**
         * ID: 791<br></br>
         * Message: The final match is about to begin. Line up!
         */
        val FINAL_MATCH_BEGIN: SystemMessageId

        /**
         * ID: 792<br></br>
         * Message: The siege of the clan hall is finished.
         */
        val CLANHALL_SIEGE_ENDED: SystemMessageId

        /**
         * ID: 793<br></br>
         * Message: The siege of the clan hall has begun.
         */
        val CLANHALL_SIEGE_BEGUN: SystemMessageId

        /**
         * ID: 794<br></br>
         * Message: You are not authorized to do that.
         */
        val YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT: SystemMessageId

        /**
         * ID: 795<br></br>
         * Message: Only clan leaders are authorized to set rights.
         */
        val ONLY_LEADERS_CAN_SET_RIGHTS: SystemMessageId

        /**
         * ID: 796<br></br>
         * Message: Your remaining observation time is minutes.
         */
        val REMAINING_OBSERVATION_TIME: SystemMessageId

        /**
         * ID: 797<br></br>
         * Message: You may create up to 24 macros.
         */
        val YOU_MAY_CREATE_UP_TO_24_MACROS: SystemMessageId

        /**
         * ID: 798<br></br>
         * Message: Item registration is irreversible. Do you wish to continue?
         */
        val ITEM_REGISTRATION_IRREVERSIBLE: SystemMessageId

        /**
         * ID: 799<br></br>
         * Message: The observation time has expired.
         */
        val OBSERVATION_TIME_EXPIRED: SystemMessageId

        /**
         * ID: 800<br></br>
         * Message: You are too late. The registration period is over.
         */
        val REGISTRATION_PERIOD_OVER: SystemMessageId

        /**
         * ID: 801<br></br>
         * Message: Registration for the clan hall siege is closed.
         */
        val REGISTRATION_CLOSED: SystemMessageId

        /**
         * ID: 802<br></br>
         * Message: Petitions are not being accepted at this time. You may submit your petition after a.m./p.m.
         */
        val PETITION_NOT_ACCEPTED_NOW: SystemMessageId

        /**
         * ID: 803<br></br>
         * Message: Enter the specifics of your petition.
         */
        val PETITION_NOT_SPECIFIED: SystemMessageId

        /**
         * ID: 804<br></br>
         * Message: Select a type.
         */
        val SELECT_TYPE: SystemMessageId

        /**
         * ID: 805<br></br>
         * Message: Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.
         */
        val PETITION_NOT_ACCEPTED_SUBMIT_AT_S1: SystemMessageId

        /**
         * ID: 806<br></br>
         * Message: If you are trapped, try typing "/unstuck".
         */
        val TRY_UNSTUCK_WHEN_TRAPPED: SystemMessageId

        /**
         * ID: 807<br></br>
         * Message: This terrain is navigable. Prepare for transport to the nearest village.
         */
        val STUCK_PREPARE_FOR_TRANSPORT: SystemMessageId

        /**
         * ID: 808<br></br>
         * Message: You are stuck. You may submit a petition by typing "/gm".
         */
        val STUCK_SUBMIT_PETITION: SystemMessageId

        /**
         * ID: 809<br></br>
         * Message: You are stuck. You will be transported to the nearest village in five minutes.
         */
        val STUCK_TRANSPORT_IN_FIVE_MINUTES: SystemMessageId

        /**
         * ID: 810<br></br>
         * Message: Invalid macro. Refer to the Help file for instructions.
         */
        val INVALID_MACRO: SystemMessageId

        /**
         * ID: 811<br></br>
         * Message: You will be moved to (). Do you wish to continue?
         */
        val WILL_BE_MOVED: SystemMessageId

        /**
         * ID: 812<br></br>
         * Message: The secret trap has inflicted $s1 damage on you.
         */
        val TRAP_DID_S1_DAMAGE: SystemMessageId

        /**
         * ID: 813<br></br>
         * Message: You have been poisoned by a Secret Trap.
         */
        val POISONED_BY_TRAP: SystemMessageId

        /**
         * ID: 814<br></br>
         * Message: Your speed has been decreased by a Secret Trap.
         */
        val SLOWED_BY_TRAP: SystemMessageId

        /**
         * ID: 815<br></br>
         * Message: The tryouts are about to begin. Line up!
         */
        val TRYOUTS_ABOUT_TO_BEGIN: SystemMessageId

        /**
         * ID: 816<br></br>
         * Message: Tickets are now available for Monster Race $s1!
         */
        val MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE: SystemMessageId

        /**
         * ID: 817<br></br>
         * Message: Now selling tickets for Monster Race $s1!
         */
        val MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE: SystemMessageId

        /**
         * ID: 818<br></br>
         * Message: Ticket sales for the Monster Race will end in $s1 minute(s).
         */
        val MONSRACE_TICKETS_STOP_IN_S1_MINUTES: SystemMessageId

        /**
         * ID: 819<br></br>
         * Message: Tickets sales are closed for Monster Race $s1. Odds are posted.
         */
        val MONSRACE_S1_TICKET_SALES_CLOSED: SystemMessageId

        /**
         * ID: 820<br></br>
         * Message: Monster Race $s2 will begin in $s1 minute(s)!
         */
        val MONSRACE_S2_BEGINS_IN_S1_MINUTES: SystemMessageId

        /**
         * ID: 821<br></br>
         * Message: Monster Race $s1 will begin in 30 seconds!
         */
        val MONSRACE_S1_BEGINS_IN_30_SECONDS: SystemMessageId

        /**
         * ID: 822<br></br>
         * Message: Monster Race $s1 is about to begin! Countdown in five seconds!
         */
        val MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS: SystemMessageId

        /**
         * ID: 823<br></br>
         * Message: The race will begin in $s1 second(s)!
         */
        val MONSRACE_BEGINS_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 824<br></br>
         * Message: They're off!
         */
        val MONSRACE_RACE_START: SystemMessageId

        /**
         * ID: 825<br></br>
         * Message: Monster Race $s1 is finished!
         */
        val MONSRACE_S1_RACE_END: SystemMessageId

        /**
         * ID: 826<br></br>
         * Message: First prize goes to the player in lane $s1. Second prize goes to the player in lane $s2.
         */
        val MONSRACE_FIRST_PLACE_S1_SECOND_S2: SystemMessageId

        /**
         * ID: 827<br></br>
         * Message: You may not impose a block on a GM.
         */
        val YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM: SystemMessageId

        /**
         * ID: 828<br></br>
         * Message: Are you sure you wish to delete the $s1 macro?
         */
        val WISH_TO_DELETE_S1_MACRO: SystemMessageId

        /**
         * ID: 829<br></br>
         * Message: You cannot recommend yourself.
         */
        val YOU_CANNOT_RECOMMEND_YOURSELF: SystemMessageId

        /**
         * ID: 830<br></br>
         * Message: You have recommended $s1. You have $s2 recommendations left.
         */
        val YOU_HAVE_RECOMMENDED_S1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT: SystemMessageId

        /**
         * ID: 831<br></br>
         * Message: You have been recommended by $s1.
         */
        val YOU_HAVE_BEEN_RECOMMENDED_BY_S1: SystemMessageId

        /**
         * ID: 832<br></br>
         * Message: That character has already been recommended.
         */
        val THAT_CHARACTER_IS_RECOMMENDED: SystemMessageId

        /**
         * ID: 833<br></br>
         * Message: You are not authorized to make further recommendations at this time. You will receive more recommendation credits each day at 1 p.m.
         */
        val NO_MORE_RECOMMENDATIONS_TO_HAVE: SystemMessageId

        /**
         * ID: 834<br></br>
         * Message: $s1 has rolled $s2.
         */
        val S1_ROLLED_S2: SystemMessageId

        /**
         * ID: 835<br></br>
         * Message: You may not throw the dice at this time. Try again later.
         */
        val YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER: SystemMessageId

        /**
         * ID: 836<br></br>
         * Message: You have exceeded your inventory volume limit and cannot take this item.
         */
        val YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM: SystemMessageId

        /**
         * ID: 837<br></br>
         * Message: Macro descriptions may contain up to 32 characters.
         */
        val MACRO_DESCRIPTION_MAX_32_CHARS: SystemMessageId

        /**
         * ID: 838<br></br>
         * Message: Enter the name of the macro.
         */
        val ENTER_THE_MACRO_NAME: SystemMessageId

        /**
         * ID: 839<br></br>
         * Message: That name is already assigned to another macro.
         */
        val MACRO_NAME_ALREADY_USED: SystemMessageId

        /**
         * ID: 840<br></br>
         * Message: That recipe is already registered.
         */
        val RECIPE_ALREADY_REGISTERED: SystemMessageId

        /**
         * ID: 841<br></br>
         * Message: No further recipes may be registered.
         */
        val NO_FUTHER_RECIPES_CAN_BE_ADDED: SystemMessageId

        /**
         * ID: 842<br></br>
         * Message: You are not authorized to register a recipe.
         */
        val NOT_AUTHORIZED_REGISTER_RECIPE: SystemMessageId

        /**
         * ID: 843<br></br>
         * Message: The siege of $s1 is finished.
         */
        val SIEGE_OF_S1_FINISHED: SystemMessageId

        /**
         * ID: 844<br></br>
         * Message: The siege to conquer $s1 has begun.
         */
        val SIEGE_OF_S1_BEGUN: SystemMessageId

        /**
         * ID: 845<br></br>
         * Message: The deadlineto register for the siege of $s1 has passed.
         */
        val DEADLINE_FOR_SIEGE_S1_PASSED: SystemMessageId

        /**
         * ID: 846<br></br>
         * Message: The siege of $s1 has been canceled due to lack of interest.
         */
        val SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST: SystemMessageId

        /**
         * ID: 847<br></br>
         * Message: A clan that owns a clan hall may not participate in a clan hall siege.
         */
        val CLAN_OWNING_CLANHALL_MAY_NOT_SIEGE_CLANHALL: SystemMessageId

        /**
         * ID: 848<br></br>
         * Message: $s1 has been deleted.
         */
        val S1_HAS_BEEN_DELETED: SystemMessageId

        /**
         * ID: 849<br></br>
         * Message: $s1 cannot be found.
         */
        val S1_NOT_FOUND: SystemMessageId

        /**
         * ID: 850<br></br>
         * Message: $s1 already exists.
         */
        val S1_ALREADY_EXISTS2: SystemMessageId

        /**
         * ID: 851<br></br>
         * Message: $s1 has been added.
         */
        val S1_ADDED: SystemMessageId

        /**
         * ID: 852<br></br>
         * Message: The recipe is incorrect.
         */
        val RECIPE_INCORRECT: SystemMessageId

        /**
         * ID: 853<br></br>
         * Message: You may not alter your recipe book while engaged in manufacturing.
         */
        val CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING: SystemMessageId

        /**
         * ID: 854<br></br>
         * Message: You are missing $s2 $s1 required to create that.
         */
        val MISSING_S2_S1_TO_CREATE: SystemMessageId

        /**
         * ID: 855<br></br>
         * Message: $s1 clan has defeated $s2.
         */
        val S1_CLAN_DEFEATED_S2: SystemMessageId

        /**
         * ID: 856<br></br>
         * Message: The siege of $s1 has ended in a draw.
         */
        val SIEGE_S1_DRAW: SystemMessageId

        /**
         * ID: 857<br></br>
         * Message: $s1 clan has won in the preliminary match of $s2.
         */
        val S1_CLAN_WON_MATCH_S2: SystemMessageId

        /**
         * ID: 858<br></br>
         * Message: The preliminary match of $s1 has ended in a draw.
         */
        val MATCH_OF_S1_DRAW: SystemMessageId

        /**
         * ID: 859<br></br>
         * Message: Please register a recipe.
         */
        val PLEASE_REGISTER_RECIPE: SystemMessageId

        /**
         * ID: 860<br></br>
         * Message: You may not buld your headquarters in close proximity to another headquarters.
         */
        val HEADQUARTERS_TOO_CLOSE: SystemMessageId

        /**
         * ID: 861<br></br>
         * Message: You have exceeded the maximum number of memos.
         */
        val TOO_MANY_MEMOS: SystemMessageId

        /**
         * ID: 862<br></br>
         * Message: Odds are not posted until ticket sales have closed.
         */
        val ODDS_NOT_POSTED: SystemMessageId

        /**
         * ID: 863<br></br>
         * Message: You feel the energy of fire.
         */
        val FEEL_ENERGY_FIRE: SystemMessageId

        /**
         * ID: 864<br></br>
         * Message: You feel the energy of water.
         */
        val FEEL_ENERGY_WATER: SystemMessageId

        /**
         * ID: 865<br></br>
         * Message: You feel the energy of wind.
         */
        val FEEL_ENERGY_WIND: SystemMessageId

        /**
         * ID: 866<br></br>
         * Message: You may no longer gather energy.
         */
        val NO_LONGER_ENERGY: SystemMessageId

        /**
         * ID: 867<br></br>
         * Message: The energy is depleted.
         */
        val ENERGY_DEPLETED: SystemMessageId

        /**
         * ID: 868<br></br>
         * Message: The energy of fire has been delivered.
         */
        val ENERGY_FIRE_DELIVERED: SystemMessageId

        /**
         * ID: 869<br></br>
         * Message: The energy of water has been delivered.
         */
        val ENERGY_WATER_DELIVERED: SystemMessageId

        /**
         * ID: 870<br></br>
         * Message: The energy of wind has been delivered.
         */
        val ENERGY_WIND_DELIVERED: SystemMessageId

        /**
         * ID: 871<br></br>
         * Message: The seed has been sown.
         */
        val THE_SEED_HAS_BEEN_SOWN: SystemMessageId

        /**
         * ID: 872<br></br>
         * Message: This seed may not be sown here.
         */
        val THIS_SEED_MAY_NOT_BE_SOWN_HERE: SystemMessageId

        /**
         * ID: 873<br></br>
         * Message: That character does not exist.
         */
        val CHARACTER_DOES_NOT_EXIST: SystemMessageId

        /**
         * ID: 874<br></br>
         * Message: The capacity of the warehouse has been exceeded.
         */
        val WAREHOUSE_CAPACITY_EXCEEDED: SystemMessageId

        /**
         * ID: 875<br></br>
         * Message: The transport of the cargo has been canceled.
         */
        val CARGO_CANCELED: SystemMessageId

        /**
         * ID: 876<br></br>
         * Message: The cargo was not delivered.
         */
        val CARGO_NOT_DELIVERED: SystemMessageId

        /**
         * ID: 877<br></br>
         * Message: The symbol has been added.
         */
        val SYMBOL_ADDED: SystemMessageId

        /**
         * ID: 878<br></br>
         * Message: The symbol has been deleted.
         */
        val SYMBOL_DELETED: SystemMessageId

        /**
         * ID: 879<br></br>
         * Message: The manor system is currently under maintenance.
         */
        val THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE: SystemMessageId

        /**
         * ID: 880<br></br>
         * Message: The transaction is complete.
         */
        val THE_TRANSACTION_IS_COMPLETE: SystemMessageId

        /**
         * ID: 881<br></br>
         * Message: There is a discrepancy on the invoice.
         */
        val THERE_IS_A_DISCREPANCY_ON_THE_INVOICE: SystemMessageId

        /**
         * ID: 882<br></br>
         * Message: The seed quantity is incorrect.
         */
        val THE_SEED_QUANTITY_IS_INCORRECT: SystemMessageId

        /**
         * ID: 883<br></br>
         * Message: The seed information is incorrect.
         */
        val THE_SEED_INFORMATION_IS_INCORRECT: SystemMessageId

        /**
         * ID: 884<br></br>
         * Message: The manor information has been updated.
         */
        val THE_MANOR_INFORMATION_HAS_BEEN_UPDATED: SystemMessageId

        /**
         * ID: 885<br></br>
         * Message: The number of crops is incorrect.
         */
        val THE_NUMBER_OF_CROPS_IS_INCORRECT: SystemMessageId

        /**
         * ID: 886<br></br>
         * Message: The crops are priced incorrectly.
         */
        val THE_CROPS_ARE_PRICED_INCORRECTLY: SystemMessageId

        /**
         * ID: 887<br></br>
         * Message: The type is incorrect.
         */
        val THE_TYPE_IS_INCORRECT: SystemMessageId

        /**
         * ID: 888<br></br>
         * Message: No crops can be purchased at this time.
         */
        val NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME: SystemMessageId

        /**
         * ID: 889<br></br>
         * Message: The seed was successfully sown.
         */
        val THE_SEED_WAS_SUCCESSFULLY_SOWN: SystemMessageId

        /**
         * ID: 890<br></br>
         * Message: The seed was not sown.
         */
        val THE_SEED_WAS_NOT_SOWN: SystemMessageId

        /**
         * ID: 891<br></br>
         * Message: You are not authorized to harvest.
         */
        val YOU_ARE_NOT_AUTHORIZED_TO_HARVEST: SystemMessageId

        /**
         * ID: 892<br></br>
         * Message: The harvest has failed.
         */
        val THE_HARVEST_HAS_FAILED: SystemMessageId

        /**
         * ID: 893<br></br>
         * Message: The harvest failed because the seed was not sown.
         */
        val THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN: SystemMessageId

        /**
         * ID: 894<br></br>
         * Message: Up to $s1 recipes can be registered.
         */
        val UP_TO_S1_RECIPES_CAN_REGISTER: SystemMessageId

        /**
         * ID: 895<br></br>
         * Message: No recipes have been registered.
         */
        val NO_RECIPES_REGISTERED: SystemMessageId

        /**
         * ID: 896<br></br>
         * Message:The ferry has arrived at Gludin Harbor.
         */
        val FERRY_AT_GLUDIN: SystemMessageId

        /**
         * ID: 897<br></br>
         * Message:The ferry will leave for Talking Island Harbor after anchoring for ten minutes.
         */
        val FERRY_LEAVE_TALKING: SystemMessageId

        /**
         * ID: 898<br></br>
         * Message: Only characters of level 10 or above are authorized to make recommendations.
         */
        val ONLY_LEVEL_SUP_10_CAN_RECOMMEND: SystemMessageId

        /**
         * ID: 899<br></br>
         * Message: The symbol cannot be drawn.
         */
        val CANT_DRAW_SYMBOL: SystemMessageId

        /**
         * ID: 900<br></br>
         * Message: No slot exists to draw the symbol
         */
        val SYMBOLS_FULL: SystemMessageId

        /**
         * ID: 901<br></br>
         * Message: The symbol information cannot be found.
         */
        val SYMBOL_NOT_FOUND: SystemMessageId

        /**
         * ID: 902<br></br>
         * Message: The number of items is incorrect.
         */
        val NUMBER_INCORRECT: SystemMessageId

        /**
         * ID: 903<br></br>
         * Message: You may not submit a petition while frozen. Be patient.
         */
        val NO_PETITION_WHILE_FROZEN: SystemMessageId

        /**
         * ID: 904<br></br>
         * Message: Items cannot be discarded while in private store status.
         */
        val NO_DISCARD_WHILE_PRIVATE_STORE: SystemMessageId

        /**
         * ID: 905<br></br>
         * Message: The current score for the Humans is $s1.
         */
        val HUMAN_SCORE_S1: SystemMessageId

        /**
         * ID: 906<br></br>
         * Message: The current score for the Elves is $s1.
         */
        val ELVES_SCORE_S1: SystemMessageId

        /**
         * ID: 907<br></br>
         * Message: The current score for the Dark Elves is $s1.
         */
        val DARK_ELVES_SCORE_S1: SystemMessageId

        /**
         * ID: 908<br></br>
         * Message: The current score for the Orcs is $s1.
         */
        val ORCS_SCORE_S1: SystemMessageId

        /**
         * ID: 909<br></br>
         * Message: The current score for the Dwarves is $s1.
         */
        val DWARVEN_SCORE_S1: SystemMessageId

        /**
         * ID: 910<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near Talking Island Village)
         */
        val LOC_TI_S1_S2_S3: SystemMessageId

        /**
         * ID: 911<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near Gludin Village)
         */
        val LOC_GLUDIN_S1_S2_S3: SystemMessageId

        /**
         * ID: 912<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Town of Gludio)
         */
        val LOC_GLUDIO_S1_S2_S3: SystemMessageId

        /**
         * ID: 913<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Neutral Zone)
         */
        val LOC_NEUTRAL_ZONE_S1_S2_S3: SystemMessageId

        /**
         * ID: 914<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Elven Village)
         */
        val LOC_ELVEN_S1_S2_S3: SystemMessageId

        /**
         * ID: 915<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Dark Elf Village)
         */
        val LOC_DARK_ELVEN_S1_S2_S3: SystemMessageId

        /**
         * ID: 916<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Town of Dion)
         */
        val LOC_DION_S1_S2_S3: SystemMessageId

        /**
         * ID: 917<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Floran Village)
         */
        val LOC_FLORAN_S1_S2_S3: SystemMessageId

        /**
         * ID: 918<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Town of Giran)
         */
        val LOC_GIRAN_S1_S2_S3: SystemMessageId

        /**
         * ID: 919<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near Giran Harbor)
         */
        val LOC_GIRAN_HARBOR_S1_S2_S3: SystemMessageId

        /**
         * ID: 920<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Orc Village)
         */
        val LOC_ORC_S1_S2_S3: SystemMessageId

        /**
         * ID: 921<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Dwarven Village)
         */
        val LOC_DWARVEN_S1_S2_S3: SystemMessageId

        /**
         * ID: 922<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Town of Oren)
         */
        val LOC_OREN_S1_S2_S3: SystemMessageId

        /**
         * ID: 923<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near Hunters Village)
         */
        val LOC_HUNTER_S1_S2_S3: SystemMessageId

        /**
         * ID: 924<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near Aden Castle Town)
         */
        val LOC_ADEN_S1_S2_S3: SystemMessageId

        /**
         * ID: 925<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near the Coliseum)
         */
        val LOC_COLISEUM_S1_S2_S3: SystemMessageId

        /**
         * ID: 926<br></br>
         * Message: Current location : $s1, $s2, $s3 (Near Heine)
         */
        val LOC_HEINE_S1_S2_S3: SystemMessageId

        /**
         * ID: 927<br></br>
         * Message: The current time is $s1:$s2.
         */
        val TIME_S1_S2_IN_THE_DAY: SystemMessageId

        /**
         * ID: 928<br></br>
         * Message: The current time is $s1:$s2.
         */
        val TIME_S1_S2_IN_THE_NIGHT: SystemMessageId

        /**
         * ID: 929<br></br>
         * Message: No compensation was given for the farm products.
         */
        val NO_COMPENSATION_FOR_FARM_PRODUCTS: SystemMessageId

        /**
         * ID: 930<br></br>
         * Message: Lottery tickets are not currently being sold.
         */
        val NO_LOTTERY_TICKETS_CURRENT_SOLD: SystemMessageId

        /**
         * ID: 931<br></br>
         * Message: The winning lottery ticket numbers has not yet been anonunced.
         */
        val LOTTERY_WINNERS_NOT_ANNOUNCED_YET: SystemMessageId

        /**
         * ID: 932<br></br>
         * Message: You cannot chat locally while observing.
         */
        val NO_ALLCHAT_WHILE_OBSERVING: SystemMessageId

        /**
         * ID: 933<br></br>
         * Message: The seed pricing greatly differs from standard seed prices.
         */
        val THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES: SystemMessageId

        /**
         * ID: 934<br></br>
         * Message: It is a deleted recipe.
         */
        val A_DELETED_RECIPE: SystemMessageId

        /**
         * ID: 935<br></br>
         * Message: The amount is not sufficient and so the manor is not in operation.
         */
        val THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION: SystemMessageId

        /**
         * ID: 936<br></br>
         * Message: Use $s1.
         */
        val USE_S1_: SystemMessageId

        /**
         * ID: 937<br></br>
         * Message: Currently preparing for private workshop.
         */
        val PREPARING_PRIVATE_WORKSHOP: SystemMessageId

        /**
         * ID: 938<br></br>
         * Message: The community server is currently offline.
         */
        val CB_OFFLINE: SystemMessageId

        /**
         * ID: 939<br></br>
         * Message: You cannot exchange while blocking everything.
         */
        val NO_EXCHANGE_WHILE_BLOCKING: SystemMessageId

        /**
         * ID: 940<br></br>
         * Message: $s1 is blocked everything.
         */
        val S1_BLOCKED_EVERYTHING: SystemMessageId

        /**
         * ID: 941<br></br>
         * Message: Restart at Talking Island Village.
         */
        val RESTART_AT_TI: SystemMessageId

        /**
         * ID: 942<br></br>
         * Message: Restart at Gludin Village.
         */
        val RESTART_AT_GLUDIN: SystemMessageId

        /**
         * ID: 943<br></br>
         * Message: Restart at the Town of Gludin. || guess should be Gludio ;)
         */
        val RESTART_AT_GLUDIO: SystemMessageId

        /**
         * ID: 944<br></br>
         * Message: Restart at the Neutral Zone.
         */
        val RESTART_AT_NEUTRAL_ZONE: SystemMessageId

        /**
         * ID: 945<br></br>
         * Message: Restart at the Elven Village.
         */
        val RESTART_AT_ELFEN_VILLAGE: SystemMessageId

        /**
         * ID: 946<br></br>
         * Message: Restart at the Dark Elf Village.
         */
        val RESTART_AT_DARKELF_VILLAGE: SystemMessageId

        /**
         * ID: 947<br></br>
         * Message: Restart at the Town of Dion.
         */
        val RESTART_AT_DION: SystemMessageId

        /**
         * ID: 948<br></br>
         * Message: Restart at Floran Village.
         */
        val RESTART_AT_FLORAN: SystemMessageId

        /**
         * ID: 949<br></br>
         * Message: Restart at the Town of Giran.
         */
        val RESTART_AT_GIRAN: SystemMessageId

        /**
         * ID: 950<br></br>
         * Message: Restart at Giran Harbor.
         */
        val RESTART_AT_GIRAN_HARBOR: SystemMessageId

        /**
         * ID: 951<br></br>
         * Message: Restart at the Orc Village.
         */
        val RESTART_AT_ORC_VILLAGE: SystemMessageId

        /**
         * ID: 952<br></br>
         * Message: Restart at the Dwarven Village.
         */
        val RESTART_AT_DWARFEN_VILLAGE: SystemMessageId

        /**
         * ID: 953<br></br>
         * Message: Restart at the Town of Oren.
         */
        val RESTART_AT_OREN: SystemMessageId

        /**
         * ID: 954<br></br>
         * Message: Restart at Hunters Village.
         */
        val RESTART_AT_HUNTERS_VILLAGE: SystemMessageId

        /**
         * ID: 955<br></br>
         * Message: Restart at the Town of Aden.
         */
        val RESTART_AT_ADEN: SystemMessageId

        /**
         * ID: 956<br></br>
         * Message: Restart at the Coliseum.
         */
        val RESTART_AT_COLISEUM: SystemMessageId

        /**
         * ID: 957<br></br>
         * Message: Restart at Heine.
         */
        val RESTART_AT_HEINE: SystemMessageId

        /**
         * ID: 958<br></br>
         * Message: Items cannot be discarded or destroyed while operating a private store or workshop.
         */
        val ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP: SystemMessageId

        /**
         * ID: 959<br></br>
         * Message: $s1 (*$s2) manufactured successfully.
         */
        val S1_S2_MANUFACTURED_SUCCESSFULLY: SystemMessageId

        /**
         * ID: 960<br></br>
         * Message: $s1 manufacturing failure.
         */
        val S1_MANUFACTURE_FAILURE: SystemMessageId

        /**
         * ID: 961<br></br>
         * Message: You are now blocking everything.
         */
        val BLOCKING_ALL: SystemMessageId

        /**
         * ID: 962<br></br>
         * Message: You are no longer blocking everything.
         */
        val NOT_BLOCKING_ALL: SystemMessageId

        /**
         * ID: 963<br></br>
         * Message: Please determine the manufacturing price.
         */
        val DETERMINE_MANUFACTURE_PRICE: SystemMessageId

        /**
         * ID: 964<br></br>
         * Message: Chatting is prohibited for one minute.
         */
        val CHATBAN_FOR_1_MINUTE: SystemMessageId

        /**
         * ID: 965<br></br>
         * Message: The chatting prohibition has been removed.
         */
        val CHATBAN_REMOVED: SystemMessageId

        /**
         * ID: 966<br></br>
         * Message: Chatting is currently prohibited. If you try to chat before the prohibition is removed, the prohibition time will become even longer.
         */
        val CHATTING_IS_CURRENTLY_PROHIBITED: SystemMessageId

        /**
         * ID: 967<br></br>
         * Message: Do you accept $s1's party invitation? (Item Distribution: Random including spoil.)
         */
        val S1_PARTY_INVITE_RANDOM_INCLUDING_SPOIL: SystemMessageId

        /**
         * ID: 968<br></br>
         * Message: Do you accept $s1's party invitation? (Item Distribution: By Turn.)
         */
        val S1_PARTY_INVITE_BY_TURN: SystemMessageId

        /**
         * ID: 969<br></br>
         * Message: Do you accept $s1's party invitation? (Item Distribution: By Turn including spoil.)
         */
        val S1_PARTY_INVITE_BY_TURN_INCLUDING_SPOIL: SystemMessageId

        /**
         * ID: 970<br></br>
         * Message: $s2's MP has been drained by $s1.
         */
        val S2_MP_HAS_BEEN_DRAINED_BY_S1: SystemMessageId

        /**
         * ID: 971<br></br>
         * Message: Petitions cannot exceed 255 characters.
         */
        val PETITION_MAX_CHARS_255: SystemMessageId

        /**
         * ID: 972<br></br>
         * Message: This pet cannot use this item.
         */
        val PET_CANNOT_USE_ITEM: SystemMessageId

        /**
         * ID: 973<br></br>
         * Message: Please input no more than the number you have.
         */
        val INPUT_NO_MORE_YOU_HAVE: SystemMessageId

        /**
         * ID: 974<br></br>
         * Message: The soul crystal succeeded in absorbing a soul.
         */
        val SOUL_CRYSTAL_ABSORBING_SUCCEEDED: SystemMessageId

        /**
         * ID: 975<br></br>
         * Message: The soul crystal was not able to absorb a soul.
         */
        val SOUL_CRYSTAL_ABSORBING_FAILED: SystemMessageId

        /**
         * ID: 976<br></br>
         * Message: The soul crystal broke because it was not able to endure the soul energy.
         */
        val SOUL_CRYSTAL_BROKE: SystemMessageId

        /**
         * ID: 977<br></br>
         * Message: The soul crystals caused resonation and failed at absorbing a soul.
         */
        val SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION: SystemMessageId

        /**
         * ID: 978<br></br>
         * Message: The soul crystal is refusing to absorb a soul.
         */
        val SOUL_CRYSTAL_ABSORBING_REFUSED: SystemMessageId

        /**
         * ID: 979<br></br>
         * Message: The ferry arrived at Talking Island Harbor.
         */
        val FERRY_ARRIVED_AT_TALKING: SystemMessageId

        /**
         * ID: 980<br></br>
         * Message: The ferry will leave for Gludin Harbor after anchoring for ten minutes.
         */
        val FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES: SystemMessageId

        /**
         * ID: 981<br></br>
         * Message: The ferry will leave for Gludin Harbor in five minutes.
         */
        val FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES: SystemMessageId

        /**
         * ID: 982<br></br>
         * Message: The ferry will leave for Gludin Harbor in one minute.
         */
        val FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE: SystemMessageId

        /**
         * ID: 983<br></br>
         * Message: Those wishing to ride should make haste to get on.
         */
        val MAKE_HASTE_GET_ON_BOAT: SystemMessageId

        /**
         * ID: 984<br></br>
         * Message: The ferry will be leaving soon for Gludin Harbor.
         */
        val FERRY_LEAVE_SOON_FOR_GLUDIN: SystemMessageId

        /**
         * ID: 985<br></br>
         * Message: The ferry is leaving for Gludin Harbor.
         */
        val FERRY_LEAVING_FOR_GLUDIN: SystemMessageId

        /**
         * ID: 986<br></br>
         * Message: The ferry has arrived at Gludin Harbor.
         */
        val FERRY_ARRIVED_AT_GLUDIN: SystemMessageId

        /**
         * ID: 987<br></br>
         * Message: The ferry will leave for Talking Island Harbor after anchoring for ten minutes.
         */
        val FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES: SystemMessageId

        /**
         * ID: 988<br></br>
         * Message: The ferry will leave for Talking Island Harbor in five minutes.
         */
        val FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES: SystemMessageId

        /**
         * ID: 989<br></br>
         * Message: The ferry will leave for Talking Island Harbor in one minute.
         */
        val FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE: SystemMessageId

        /**
         * ID: 990<br></br>
         * Message: The ferry will be leaving soon for Talking Island Harbor.
         */
        val FERRY_LEAVE_SOON_FOR_TALKING: SystemMessageId

        /**
         * ID: 991<br></br>
         * Message: The ferry is leaving for Talking Island Harbor.
         */
        val FERRY_LEAVING_FOR_TALKING: SystemMessageId

        /**
         * ID: 992<br></br>
         * Message: The ferry has arrived at Giran Harbor.
         */
        val FERRY_ARRIVED_AT_GIRAN: SystemMessageId

        /**
         * ID: 993<br></br>
         * Message: The ferry will leave for Giran Harbor after anchoring for ten minutes.
         */
        val FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES: SystemMessageId

        /**
         * ID: 994<br></br>
         * Message: The ferry will leave for Giran Harbor in five minutes.
         */
        val FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES: SystemMessageId

        /**
         * ID: 995<br></br>
         * Message: The ferry will leave for Giran Harbor in one minute.
         */
        val FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE: SystemMessageId

        /**
         * ID: 996<br></br>
         * Message: The ferry will be leaving soon for Giran Harbor.
         */
        val FERRY_LEAVE_SOON_FOR_GIRAN: SystemMessageId

        /**
         * ID: 997<br></br>
         * Message: The ferry is leaving for Giran Harbor.
         */
        val FERRY_LEAVING_FOR_GIRAN: SystemMessageId

        /**
         * ID: 998<br></br>
         * Message: The Innadril pleasure boat has arrived. It will anchor for ten minutes.
         */
        val INNADRIL_BOAT_ANCHOR_10_MINUTES: SystemMessageId

        /**
         * ID: 999<br></br>
         * Message: The Innadril pleasure boat will leave in five minutes.
         */
        val INNADRIL_BOAT_LEAVE_IN_5_MINUTES: SystemMessageId

        /**
         * ID: 1000<br></br>
         * Message: The Innadril pleasure boat will leave in one minute.
         */
        val INNADRIL_BOAT_LEAVE_IN_1_MINUTE: SystemMessageId

        /**
         * ID: 1001<br></br>
         * Message: The Innadril pleasure boat will be leaving soon.
         */
        val INNADRIL_BOAT_LEAVE_SOON: SystemMessageId

        /**
         * ID: 1002<br></br>
         * Message: The Innadril pleasure boat is leaving.
         */
        val INNADRIL_BOAT_LEAVING: SystemMessageId

        /**
         * ID: 1003<br></br>
         * Message: Cannot possess a monster race ticket.
         */
        val CANNOT_POSSES_MONS_TICKET: SystemMessageId

        /**
         * ID: 1004<br></br>
         * Message: You have registered for a clan hall auction.
         */
        val REGISTERED_FOR_CLANHALL: SystemMessageId

        /**
         * ID: 1005<br></br>
         * Message: There is not enough adena in the clan hall warehouse.
         */
        val NOT_ENOUGH_ADENA_IN_CWH: SystemMessageId

        /**
         * ID: 1006<br></br>
         * Message: You have bid in a clan hall auction.
         */
        val BID_IN_CLANHALL_AUCTION: SystemMessageId

        /**
         * ID: 1007<br></br>
         * Message: The preliminary match registration of $s1 has finished.
         */
        val PRELIMINARY_REGISTRATION_OF_S1_FINISHED: SystemMessageId

        /**
         * ID: 1008<br></br>
         * Message: A hungry strider cannot be mounted or dismounted.
         */
        val HUNGRY_STRIDER_NOT_MOUNT: SystemMessageId

        /**
         * ID: 1009<br></br>
         * Message: A strider cannot be ridden when dead.
         */
        val STRIDER_CANT_BE_RIDDEN_WHILE_DEAD: SystemMessageId

        /**
         * ID: 1010<br></br>
         * Message: A dead strider cannot be ridden.
         */
        val DEAD_STRIDER_CANT_BE_RIDDEN: SystemMessageId

        /**
         * ID: 1011<br></br>
         * Message: A strider in battle cannot be ridden.
         */
        val STRIDER_IN_BATLLE_CANT_BE_RIDDEN: SystemMessageId

        /**
         * ID: 1012<br></br>
         * Message: A strider cannot be ridden while in battle.
         */
        val STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE: SystemMessageId

        /**
         * ID: 1013<br></br>
         * Message: A strider can be ridden only when standing.
         */
        val STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING: SystemMessageId

        /**
         * ID: 1014<br></br>
         * Message: Your pet gained $s1 experience points.
         */
        val PET_EARNED_S1_EXP: SystemMessageId

        /**
         * ID: 1015<br></br>
         * Message: Your pet hit for $s1 damage.
         */
        val PET_HIT_FOR_S1_DAMAGE: SystemMessageId

        /**
         * ID: 1016<br></br>
         * Message: Pet received $s2 damage by $s1.
         */
        val PET_RECEIVED_S2_DAMAGE_BY_S1: SystemMessageId

        /**
         * ID: 1017<br></br>
         * Message: Pet's critical hit!
         */
        val CRITICAL_HIT_BY_PET: SystemMessageId

        /**
         * ID: 1018<br></br>
         * Message: Your pet uses $s1.
         */
        val PET_USES_S1: SystemMessageId

        /**
         * ID: 1019<br></br>
         * Message: Your pet uses $s1.
         */
        val PET_USES_S1_: SystemMessageId

        /**
         * ID: 1020<br></br>
         * Message: Your pet picked up $s1.
         */
        val PET_PICKED_S1: SystemMessageId

        /**
         * ID: 1021<br></br>
         * Message: Your pet picked up $s2 $s1(s).
         */
        val PET_PICKED_S2_S1_S: SystemMessageId

        /**
         * ID: 1022<br></br>
         * Message: Your pet picked up +$s1 $s2.
         */
        val PET_PICKED_S1_S2: SystemMessageId

        /**
         * ID: 1023<br></br>
         * Message: Your pet picked up $s1 adena.
         */
        val PET_PICKED_S1_ADENA: SystemMessageId

        /**
         * ID: 1024<br></br>
         * Message: Your pet put on $s1.
         */
        val PET_PUT_ON_S1: SystemMessageId

        /**
         * ID: 1025<br></br>
         * Message: Your pet took off $s1.
         */
        val PET_TOOK_OFF_S1: SystemMessageId

        /**
         * ID: 1026<br></br>
         * Message: The summoned monster gave damage of $s1
         */
        val SUMMON_GAVE_DAMAGE_S1: SystemMessageId

        /**
         * ID: 1027<br></br>
         * Message: Servitor received $s2 damage caused by $s1.
         */
        val SUMMON_RECEIVED_DAMAGE_S2_BY_S1: SystemMessageId

        /**
         * ID: 1028<br></br>
         * Message: Summoned monster's critical hit!
         */
        val CRITICAL_HIT_BY_SUMMONED_MOB: SystemMessageId

        /**
         * ID: 1029<br></br>
         * Message: Summoned monster uses $s1.
         */
        val SUMMONED_MOB_USES_S1: SystemMessageId

        /**
         * ID: 1030<br></br>
         * Message: <Party Information>
        </Party> */
        val PARTY_INFORMATION: SystemMessageId

        /**
         * ID: 1031<br></br>
         * Message: Looting method: Finders keepers
         */
        val LOOTING_FINDERS_KEEPERS: SystemMessageId

        /**
         * ID: 1032<br></br>
         * Message: Looting method: Random
         */
        val LOOTING_RANDOM: SystemMessageId

        /**
         * ID: 1033<br></br>
         * Message: Looting method: Random including spoil
         */
        val LOOTING_RANDOM_INCLUDE_SPOIL: SystemMessageId

        /**
         * ID: 1034<br></br>
         * Message: Looting method: By turn
         */
        val LOOTING_BY_TURN: SystemMessageId

        /**
         * ID: 1035<br></br>
         * Message: Looting method: By turn including spoil
         */
        val LOOTING_BY_TURN_INCLUDE_SPOIL: SystemMessageId

        /**
         * ID: 1036<br></br>
         * Message: You have exceeded the quantity that can be inputted.
         */
        val YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED: SystemMessageId

        /**
         * ID: 1037<br></br>
         * Message: $s1 manufactured $s2.
         */
        val S1_MANUFACTURED_S2: SystemMessageId

        /**
         * ID: 1038<br></br>
         * Message: $s1 manufactured $s3 $s2(s).
         */
        val S1_MANUFACTURED_S3_S2_S: SystemMessageId

        /**
         * ID: 1039<br></br>
         * Message: Items left at the clan hall warehouse can only be retrieved by the clan leader. Do you want to continue?
         */
        val ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE: SystemMessageId

        /**
         * ID: 1040<br></br>
         * Message: Items sent by freight can be picked up from any Warehouse location. Do you want to continue?
         */
        val ITEMS_SENT_BY_FREIGHT_PICKED_UP_FROM_ANYWHERE: SystemMessageId

        /**
         * ID: 1041<br></br>
         * Message: The next seed purchase price is $s1 adena.
         */
        val THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA: SystemMessageId

        /**
         * ID: 1042<br></br>
         * Message: The next farm goods purchase price is $s1 adena.
         */
        val THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA: SystemMessageId

        /**
         * ID: 1043<br></br>
         * Message: At the current time, the "/unstuck" command cannot be used. Please send in a petition.
         */
        val NO_UNSTUCK_PLEASE_SEND_PETITION: SystemMessageId

        /**
         * ID: 1044<br></br>
         * Message: Monster race payout information is not available while tickets are being sold.
         */
        val MONSRACE_NO_PAYOUT_INFO: SystemMessageId

        /**
         * ID: 1046<br></br>
         * Message: Monster race tickets are no longer available.
         */
        val MONSRACE_TICKETS_NOT_AVAILABLE: SystemMessageId

        /**
         * ID: 1047<br></br>
         * Message: We did not succeed in producing $s1 item.
         */
        val NOT_SUCCEED_PRODUCING_S1: SystemMessageId

        /**
         * ID: 1048<br></br>
         * Message: When "blocking" everything, whispering is not possible.
         */
        val NO_WHISPER_WHEN_BLOCKING: SystemMessageId

        /**
         * ID: 1049<br></br>
         * Message: When "blocking" everything, it is not possible to send invitations for organizing parties.
         */
        val NO_PARTY_WHEN_BLOCKING: SystemMessageId

        /**
         * ID: 1050<br></br>
         * Message: There are no communities in my clan. Clan communities are allowed for clans with skill levels of 2 and higher.
         */
        val NO_CB_IN_MY_CLAN: SystemMessageId

        /**
         * ID: 1051 <br></br>
         * Message: Payment for your clan hall has not been made please make payment tomorrow.
         */
        val PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW: SystemMessageId

        /**
         * ID: 1052 <br></br>
         * Message: Payment of Clan Hall is overdue the owner loose Clan Hall.
         */
        val THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED: SystemMessageId

        /**
         * ID: 1053<br></br>
         * Message: It is not possible to resurrect in battlefields where a siege war is taking place.
         */
        val CANNOT_BE_RESURRECTED_DURING_SIEGE: SystemMessageId

        /**
         * ID: 1054<br></br>
         * Message: You have entered a mystical land.
         */
        val ENTERED_MYSTICAL_LAND: SystemMessageId

        /**
         * ID: 1055<br></br>
         * Message: You have left a mystical land.
         */
        val EXITED_MYSTICAL_LAND: SystemMessageId

        /**
         * ID: 1056<br></br>
         * Message: You have exceeded the storage capacity of the castle's vault.
         */
        val VAULT_CAPACITY_EXCEEDED: SystemMessageId

        /**
         * ID: 1057<br></br>
         * Message: This command can only be used in the relax server.
         */
        val RELAX_SERVER_ONLY: SystemMessageId

        /**
         * ID: 1058<br></br>
         * Message: The sales price for seeds is $s1 adena.
         */
        val THE_SALES_PRICE_FOR_SEEDS_IS_S1_ADENA: SystemMessageId

        /**
         * ID: 1059<br></br>
         * Message: The remaining purchasing amount is $s1 adena.
         */
        val THE_REMAINING_PURCHASING_IS_S1_ADENA: SystemMessageId

        /**
         * ID: 1060<br></br>
         * Message: The remainder after selling the seeds is $s1.
         */
        val THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1: SystemMessageId

        /**
         * ID: 1061<br></br>
         * Message: The recipe cannot be registered. You do not have the ability to create items.
         */
        val CANT_REGISTER_NO_ABILITY_TO_CRAFT: SystemMessageId

        /**
         * ID: 1062<br></br>
         * Message: Writing something new is possible after level 10.
         */
        val WRITING_SOMETHING_NEW_POSSIBLE_AFTER_LEVEL_10: SystemMessageId

        /**
         * ID: 1063<br></br>
         * if you become trapped or unable to move, please use the '/unstuck' command.
         */
        val PETITION_UNAVAILABLE: SystemMessageId

        /**
         * ID: 1064<br></br>
         * Message: The equipment, +$s1 $s2, has been removed.
         */
        val EQUIPMENT_S1_S2_REMOVED: SystemMessageId

        /**
         * ID: 1065<br></br>
         * Message: While operating a private store or workshop, you cannot discard, destroy, or trade an item.
         */
        val CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE: SystemMessageId

        /**
         * ID: 1066<br></br>
         * Message: $s1 HP has been restored.
         */
        val S1_HP_RESTORED: SystemMessageId

        /**
         * ID: 1067<br></br>
         * Message: $s2 HP has been restored by $s1
         */
        val S2_HP_RESTORED_BY_S1: SystemMessageId

        /**
         * ID: 1068<br></br>
         * Message: $s1 MP has been restored.
         */
        val S1_MP_RESTORED: SystemMessageId

        /**
         * ID: 1069<br></br>
         * Message: $s2 MP has been restored by $s1.
         */
        val S2_MP_RESTORED_BY_S1: SystemMessageId

        /**
         * ID: 1070<br></br>
         * Message: You do not have 'read' permission.
         */
        val NO_READ_PERMISSION: SystemMessageId

        /**
         * ID: 1071<br></br>
         * Message: You do not have 'write' permission.
         */
        val NO_WRITE_PERMISSION: SystemMessageId

        /**
         * ID: 1072<br></br>
         * Message: You have obtained a ticket for the Monster Race #$s1 - Single
         */
        val OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE: SystemMessageId

        /**
         * ID: 1073<br></br>
         * Message: You have obtained a ticket for the Monster Race #$s1 - Single
         */
        val OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE_: SystemMessageId

        /**
         * ID: 1074<br></br>
         * Message: You do not meet the age requirement to purchase a Monster Race Ticket.
         */
        val NOT_MEET_AGE_REQUIREMENT_FOR_MONS_RACE: SystemMessageId

        /**
         * ID: 1075<br></br>
         * Message: The bid amount must be higher than the previous bid.
         */
        val BID_AMOUNT_HIGHER_THAN_PREVIOUS_BID: SystemMessageId

        /**
         * ID: 1076<br></br>
         * Message: The game cannot be terminated at this time.
         */
        val GAME_CANNOT_TERMINATE_NOW: SystemMessageId

        /**
         * ID: 1077<br></br>
         * Message: A GameGuard Execution error has occurred. Please send the *.erl file(s) located in the GameGuard folder to game@inca.co.kr
         */
        val GG_EXECUTION_ERROR: SystemMessageId

        /**
         * ID: 1078<br></br>
         * Message: When a user's keyboard input exceeds a certain cumulative score a chat ban will be applied. This is done to discourage spamming. Please avoid posting the same message multiple times during a short period.
         */
        val DONT_SPAM: SystemMessageId

        /**
         * ID: 1079<br></br>
         * Message: The target is currently banend from chatting.
         */
        val TARGET_IS_CHAT_BANNED: SystemMessageId

        /**
         * ID: 1080<br></br>
         * Message: Being permanent, are you sure you wish to use the facelift potion - Type A?
         */
        val FACELIFT_POTION_TYPE_A: SystemMessageId

        /**
         * ID: 1081<br></br>
         * Message: Being permanent, are you sure you wish to use the hair dye potion - Type A?
         */
        val HAIRDYE_POTION_TYPE_A: SystemMessageId

        /**
         * ID: 1082<br></br>
         * Message: Do you wish to use the hair style change potion - Type A? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_A: SystemMessageId

        /**
         * ID: 1083<br></br>
         * Message: Facelift potion - Type A is being applied.
         */
        val FACELIFT_POTION_TYPE_A_APPLIED: SystemMessageId

        /**
         * ID: 1084<br></br>
         * Message: Hair dye potion - Type A is being applied.
         */
        val HAIRDYE_POTION_TYPE_A_APPLIED: SystemMessageId

        /**
         * ID: 1085<br></br>
         * Message: The hair style chance potion - Type A is being used.
         */
        val HAIRSTYLE_POTION_TYPE_A_USED: SystemMessageId

        /**
         * ID: 1086<br></br>
         * Message: Your facial appearance has been changed.
         */
        val FACE_APPEARANCE_CHANGED: SystemMessageId

        /**
         * ID: 1087<br></br>
         * Message: Your hair color has changed.
         */
        val HAIR_COLOR_CHANGED: SystemMessageId

        /**
         * ID: 1088<br></br>
         * Message: Your hair style has been changed.
         */
        val HAIR_STYLE_CHANGED: SystemMessageId

        /**
         * ID: 1089<br></br>
         * Message: $s1 has obtained a first anniversary commemorative item.
         */
        val S1_OBTAINED_ANNIVERSARY_ITEM: SystemMessageId

        /**
         * ID: 1090<br></br>
         * Message: Being permanent, are you sure you wish to use the facelift potion - Type B?
         */
        val FACELIFT_POTION_TYPE_B: SystemMessageId

        /**
         * ID: 1091<br></br>
         * Message: Being permanent, are you sure you wish to use the facelift potion - Type C?
         */
        val FACELIFT_POTION_TYPE_C: SystemMessageId

        /**
         * ID: 1092<br></br>
         * Message: Being permanent, are you sure you wish to use the hair dye potion - Type B?
         */
        val HAIRDYE_POTION_TYPE_B: SystemMessageId

        /**
         * ID: 1093<br></br>
         * Message: Being permanent, are you sure you wish to use the hair dye potion - Type C?
         */
        val HAIRDYE_POTION_TYPE_C: SystemMessageId

        /**
         * ID: 1094<br></br>
         * Message: Being permanent, are you sure you wish to use the hair dye potion - Type D?
         */
        val HAIRDYE_POTION_TYPE_D: SystemMessageId

        /**
         * ID: 1095<br></br>
         * Message: Do you wish to use the hair style change potion - Type B? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_B: SystemMessageId

        /**
         * ID: 1096<br></br>
         * Message: Do you wish to use the hair style change potion - Type C? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_C: SystemMessageId

        /**
         * ID: 1097<br></br>
         * Message: Do you wish to use the hair style change potion - Type D? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_D: SystemMessageId

        /**
         * ID: 1098<br></br>
         * Message: Do you wish to use the hair style change potion - Type E? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_E: SystemMessageId

        /**
         * ID: 1099<br></br>
         * Message: Do you wish to use the hair style change potion - Type F? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_F: SystemMessageId

        /**
         * ID: 1100<br></br>
         * Message: Do you wish to use the hair style change potion - Type G? It is permanent.
         */
        val HAIRSTYLE_POTION_TYPE_G: SystemMessageId

        /**
         * ID: 1101<br></br>
         * Message: Facelift potion - Type B is being applied.
         */
        val FACELIFT_POTION_TYPE_B_APPLIED: SystemMessageId

        /**
         * ID: 1102<br></br>
         * Message: Facelift potion - Type C is being applied.
         */
        val FACELIFT_POTION_TYPE_C_APPLIED: SystemMessageId

        /**
         * ID: 1103<br></br>
         * Message: Hair dye potion - Type B is being applied.
         */
        val HAIRDYE_POTION_TYPE_B_APPLIED: SystemMessageId

        /**
         * ID: 1104<br></br>
         * Message: Hair dye potion - Type C is being applied.
         */
        val HAIRDYE_POTION_TYPE_C_APPLIED: SystemMessageId

        /**
         * ID: 1105<br></br>
         * Message: Hair dye potion - Type D is being applied.
         */
        val HAIRDYE_POTION_TYPE_D_APPLIED: SystemMessageId

        /**
         * ID: 1106<br></br>
         * Message: The hair style chance potion - Type B is being used.
         */
        val HAIRSTYLE_POTION_TYPE_B_USED: SystemMessageId

        /**
         * ID: 1107<br></br>
         * Message: The hair style chance potion - Type C is being used.
         */
        val HAIRSTYLE_POTION_TYPE_C_USED: SystemMessageId

        /**
         * ID: 1108<br></br>
         * Message: The hair style chance potion - Type D is being used.
         */
        val HAIRSTYLE_POTION_TYPE_D_USED: SystemMessageId

        /**
         * ID: 1109<br></br>
         * Message: The hair style chance potion - Type E is being used.
         */
        val HAIRSTYLE_POTION_TYPE_E_USED: SystemMessageId

        /**
         * ID: 1110<br></br>
         * Message: The hair style chance potion - Type F is being used.
         */
        val HAIRSTYLE_POTION_TYPE_F_USED: SystemMessageId

        /**
         * ID: 1111<br></br>
         * Message: The hair style chance potion - Type G is being used.
         */
        val HAIRSTYLE_POTION_TYPE_G_USED: SystemMessageId

        /**
         * ID: 1112<br></br>
         * Message: The prize amount for the winner of Lottery #$s1 is $s2 adena. We have $s3 first prize winners.
         */
        val AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER: SystemMessageId

        /**
         * ID: 1113<br></br>
         * Message: The prize amount for Lucky Lottery #$s1 is $s2 adena. There was no first prize winner in this drawing, therefore the jackpot will be added to the next drawing.
         */
        val AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER: SystemMessageId

        /**
         * ID: 1114<br></br>
         * Message: Your clan may not register to participate in a siege while under a grace period of the clan's dissolution.
         */
        val CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS: SystemMessageId

        /**
         * ID: 1115<br></br>
         * Message: Individuals may not surrender during combat.
         */
        val INDIVIDUALS_NOT_SURRENDER_DURING_COMBAT: SystemMessageId

        /**
         * ID: 1116<br></br>
         * Message: One cannot leave one's clan during combat.
         */
        val YOU_CANNOT_LEAVE_DURING_COMBAT: SystemMessageId

        /**
         * ID: 1117<br></br>
         * Message: A clan member may not be dismissed during combat.
         */
        val CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT: SystemMessageId

        /**
         * ID: 1118<br></br>
         * Message: Progress in a quest is possible only when your inventory's weight and volume are less than 80 percent of capacity.
         */
        val INVENTORY_LESS_THAN_80_PERCENT: SystemMessageId

        /**
         * ID: 1119<br></br>
         * Message: Quest was automatically canceled when you attempted to settle the accounts of your quest while your inventory exceeded 80 percent of capacity.
         */
        val QUEST_CANCELED_INVENTORY_EXCEEDS_80_PERCENT: SystemMessageId

        /**
         * ID: 1120<br></br>
         * Message: You are still a member of the clan.
         */
        val STILL_CLAN_MEMBER: SystemMessageId

        /**
         * ID: 1121<br></br>
         * Message: You do not have the right to vote.
         */
        val NO_RIGHT_TO_VOTE: SystemMessageId

        /**
         * ID: 1122<br></br>
         * Message: There is no candidate.
         */
        val NO_CANDIDATE: SystemMessageId

        /**
         * ID: 1123<br></br>
         * Message: Weight and volume limit has been exceeded. That skill is currently unavailable.
         */
        val WEIGHT_EXCEEDED_SKILL_UNAVAILABLE: SystemMessageId

        /**
         * ID: 1124<br></br>
         * Message: Your recipe book may not be accessed while using a skill.
         */
        val NO_RECIPE_BOOK_WHILE_CASTING: SystemMessageId

        /**
         * ID: 1125<br></br>
         * Message: An item may not be created while engaged in trading.
         */
        val CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING: SystemMessageId

        /**
         * ID: 1126<br></br>
         * Message: You cannot enter a negative number.
         */
        val NO_NEGATIVE_NUMBER: SystemMessageId

        /**
         * ID: 1127<br></br>
         * Message: The reward must be less than 10 times the standard price.
         */
        val REWARD_LESS_THAN_10_TIMES_STANDARD_PRICE: SystemMessageId

        /**
         * ID: 1128<br></br>
         * Message: A private store may not be opened while using a skill.
         */
        val PRIVATE_STORE_NOT_WHILE_CASTING: SystemMessageId

        /**
         * ID: 1129<br></br>
         * Message: This is not allowed while riding a ferry or boat.
         */
        val NOT_ALLOWED_ON_BOAT: SystemMessageId

        /**
         * ID: 1130<br></br>
         * Message: You have given $s1 damage to your target and $s2 damage to the servitor.
         */
        val GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR: SystemMessageId

        /**
         * ID: 1131<br></br>
         * Message: It is now midnight and the effect of $s1 can be felt.
         */
        val NIGHT_S1_EFFECT_APPLIES: SystemMessageId

        /**
         * ID: 1132<br></br>
         * Message: It is now dawn and the effect of $s1 will now disappear.
         */
        val DAY_S1_EFFECT_DISAPPEARS: SystemMessageId

        /**
         * ID: 1133<br></br>
         * Message: Since HP has decreased, the effect of $s1 can be felt.
         */
        val HP_DECREASED_EFFECT_APPLIES: SystemMessageId

        /**
         * ID: 1134<br></br>
         * Message: Since HP has increased, the effect of $s1 will disappear.
         */
        val HP_INCREASED_EFFECT_DISAPPEARS: SystemMessageId

        /**
         * ID: 1135<br></br>
         * Message: While you are engaged in combat, you cannot operate a private store or private workshop.
         */
        val CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT: SystemMessageId

        /**
         * ID: 1136<br></br>
         * Message: Since there was an account that used this IP and attempted to log in illegally, this account is not allowed to connect to the game server for $s1 minutes. Please use another game server.
         */
        val ACCOUNT_NOT_ALLOWED_TO_CONNECT: SystemMessageId

        /**
         * ID: 1137<br></br>
         * Message: $s1 harvested $s3 $s2(s).
         */
        val S1_HARVESTED_S3_S2S: SystemMessageId

        /**
         * ID: 1138<br></br>
         * Message: $s1 harvested $s2(s).
         */
        val S1_HARVESTED_S2S: SystemMessageId

        /**
         * ID: 1139<br></br>
         * Message: The weight and volume limit of your inventory must not be exceeded.
         */
        val INVENTORY_LIMIT_MUST_NOT_BE_EXCEEDED: SystemMessageId

        /**
         * ID: 1140<br></br>
         * Message: Would you like to open the gate?
         */
        val WOULD_YOU_LIKE_TO_OPEN_THE_GATE: SystemMessageId

        /**
         * ID: 1141<br></br>
         * Message: Would you like to close the gate?
         */
        val WOULD_YOU_LIKE_TO_CLOSE_THE_GATE: SystemMessageId

        /**
         * ID: 1142<br></br>
         * Message: Since $s1 already exists nearby, you cannot summon it again.
         */
        val CANNOT_SUMMON_S1_AGAIN: SystemMessageId

        /**
         * ID: 1143<br></br>
         * Message: Since you do not have enough items to maintain the servitor's stay, the servitor will disappear.
         */
        val SERVITOR_DISAPPEARED_NOT_ENOUGH_ITEMS: SystemMessageId

        /**
         * ID: 1144<br></br>
         * Message: Currently, you don't have anybody to chat with in the game.
         */
        val NOBODY_IN_GAME_TO_CHAT: SystemMessageId

        /**
         * ID: 1145<br></br>
         * Message: $s2 has been created for $s1 after the payment of $s3 adena is received.
         */
        val S2_CREATED_FOR_S1_FOR_S3_ADENA: SystemMessageId

        /**
         * ID: 1146<br></br>
         * Message: $s1 created $s2 after receiving $s3 adena.
         */
        val S1_CREATED_S2_FOR_S3_ADENA: SystemMessageId

        /**
         * ID: 1147<br></br>
         * Message: $s2 $s3 have been created for $s1 at the price of $s4 adena.
         */
        val S2_S3_S_CREATED_FOR_S1_FOR_S4_ADENA: SystemMessageId

        /**
         * ID: 1148<br></br>
         * Message: $s1 created $s2 $s3 at the price of $s4 adena.
         */
        val S1_CREATED_S2_S3_S_FOR_S4_ADENA: SystemMessageId

        /**
         * ID: 1149<br></br>
         * Message: Your attempt to create $s2 for $s1 at the price of $s3 adena has failed.
         */
        val CREATION_OF_S2_FOR_S1_AT_S3_ADENA_FAILED: SystemMessageId

        /**
         * ID: 1150<br></br>
         * Message: $s1 has failed to create $s2 at the price of $s3 adena.
         */
        val S1_FAILED_TO_CREATE_S2_FOR_S3_ADENA: SystemMessageId

        /**
         * ID: 1151<br></br>
         * Message: $s2 is sold to $s1 at the price of $s3 adena.
         */
        val S2_SOLD_TO_S1_FOR_S3_ADENA: SystemMessageId

        /**
         * ID: 1152<br></br>
         * Message: $s2 $s3 have been sold to $s1 for $s4 adena.
         */
        val S3_S2_S_SOLD_TO_S1_FOR_S4_ADENA: SystemMessageId

        /**
         * ID: 1153<br></br>
         * Message: $s2 has been purchased from $s1 at the price of $s3 adena.
         */
        val S2_PURCHASED_FROM_S1_FOR_S3_ADENA: SystemMessageId

        /**
         * ID: 1154<br></br>
         * Message: $s3 $s2 has been purchased from $s1 for $s4 adena.
         */
        val S3_S2_S_PURCHASED_FROM_S1_FOR_S4_ADENA: SystemMessageId

        /**
         * ID: 1155<br></br>
         * Message: +$s2 $s3 have been sold to $s1 for $s4 adena.
         */
        val S3_S2_SOLD_TO_S1_FOR_S4_ADENA: SystemMessageId

        /**
         * ID: 1156<br></br>
         * Message: +$s2 $s3 has been purchased from $s1 for $s4 adena.
         */
        val S2_S3_PURCHASED_FROM_S1_FOR_S4_ADENA: SystemMessageId

        /**
         * ID: 1157<br></br>
         * Message: Trying on state lasts for only 5 seconds. When a character's state changes, it can be cancelled.
         */
        val TRYING_ON_STATE: SystemMessageId

        /**
         * ID: 1158<br></br>
         * Message: You cannot dismount from this elevation.
         */
        val CANNOT_DISMOUNT_FROM_ELEVATION: SystemMessageId

        /**
         * ID: 1159<br></br>
         * Message: The ferry from Talking Island will arrive at Gludin Harbor in approximately 10 minutes.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES: SystemMessageId

        /**
         * ID: 1160<br></br>
         * Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 5 minutes.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES: SystemMessageId

        /**
         * ID: 1161<br></br>
         * Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 1 minute.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE: SystemMessageId

        /**
         * ID: 1162<br></br>
         * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 15 minutes.
         */
        val FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES: SystemMessageId

        /**
         * ID: 1163<br></br>
         * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 10 minutes.
         */
        val FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES: SystemMessageId

        /**
         * ID: 1164<br></br>
         * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 5 minutes.
         */
        val FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES: SystemMessageId

        /**
         * ID: 1165<br></br>
         * Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 1 minute.
         */
        val FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE: SystemMessageId

        /**
         * ID: 1166<br></br>
         * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES: SystemMessageId

        /**
         * ID: 1167<br></br>
         * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES: SystemMessageId

        /**
         * ID: 1168<br></br>
         * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES: SystemMessageId

        /**
         * ID: 1169<br></br>
         * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES: SystemMessageId

        /**
         * ID: 1170<br></br>
         * Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 1 minute.
         */
        val FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE: SystemMessageId

        /**
         * ID: 1171<br></br>
         * Message: The Innadril pleasure boat will arrive in approximately 20 minutes.
         */
        val INNADRIL_BOAT_ARRIVE_20_MINUTES: SystemMessageId

        /**
         * ID: 1172<br></br>
         * Message: The Innadril pleasure boat will arrive in approximately 15 minutes.
         */
        val INNADRIL_BOAT_ARRIVE_15_MINUTES: SystemMessageId

        /**
         * ID: 1173<br></br>
         * Message: The Innadril pleasure boat will arrive in approximately 10 minutes.
         */
        val INNADRIL_BOAT_ARRIVE_10_MINUTES: SystemMessageId

        /**
         * ID: 1174<br></br>
         * Message: The Innadril pleasure boat will arrive in approximately 5 minutes.
         */
        val INNADRIL_BOAT_ARRIVE_5_MINUTES: SystemMessageId

        /**
         * ID: 1175<br></br>
         * Message: The Innadril pleasure boat will arrive in approximately 1 minute.
         */
        val INNADRIL_BOAT_ARRIVE_1_MINUTE: SystemMessageId

        /**
         * ID: 1176<br></br>
         * Message: This is a quest event period.
         */
        val QUEST_EVENT_PERIOD: SystemMessageId

        /**
         * ID: 1177<br></br>
         * Message: This is the seal validation period.
         */
        val VALIDATION_PERIOD: SystemMessageId

        /**
         * ID: 1178<br></br>
         * <Seal of Avarice description>
        </Seal> */
        val AVARICE_DESCRIPTION: SystemMessageId

        /**
         * ID: 1179<br></br>
         * <Seal of Gnosis description>
        </Seal> */
        val GNOSIS_DESCRIPTION: SystemMessageId

        /**
         * ID: 1180<br></br>
         * <Seal of Strife description>
        </Seal> */
        val STRIFE_DESCRIPTION: SystemMessageId

        /**
         * ID: 1181<br></br>
         * Message: Do you really wish to change the title?
         */
        val CHANGE_TITLE_CONFIRM: SystemMessageId

        /**
         * ID: 1182<br></br>
         * Message: Are you sure you wish to delete the clan crest?
         */
        val CREST_DELETE_CONFIRM: SystemMessageId

        /**
         * ID: 1183<br></br>
         * Message: This is the initial period.
         */
        val INITIAL_PERIOD: SystemMessageId

        /**
         * ID: 1184<br></br>
         * Message: This is a period of calculating statistics in the server.
         */
        val RESULTS_PERIOD: SystemMessageId

        /**
         * ID: 1185<br></br>
         * Message: days left until deletion.
         */
        val DAYS_LEFT_UNTIL_DELETION: SystemMessageId

        /**
         * ID: 1186<br></br>
         * Message: To create a new account, please visit the PlayNC website (http://www.plaync.com/us/support/)
         */
        val TO_CREATE_ACCOUNT_VISIT_WEBSITE: SystemMessageId

        /**
         * ID: 1187<br></br>
         * Message: If you forgotten your account information or password, please visit the Support Center on the PlayNC website(http://www.plaync.com/us/support/)
         */
        val ACCOUNT_INFORMATION_FORGOTTON_VISIT_WEBSITE: SystemMessageId

        /**
         * ID: 1188<br></br>
         * Message: Your selected target can no longer receive a recommendation.
         */
        val YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION: SystemMessageId

        /**
         * ID: 1189<br></br>
         * Message: This temporary alliance of the Castle Attacker team is in effect. It will be dissolved when the Castle Lord is replaced.
         */
        val TEMPORARY_ALLIANCE: SystemMessageId

        /**
         * ID: 1189<br></br>
         * Message: This temporary alliance of the Castle Attacker team has been dissolved.
         */
        val TEMPORARY_ALLIANCE_DISSOLVED: SystemMessageId

        /**
         * ID: 1191<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 10 minutes.
         */
        val FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES: SystemMessageId

        /**
         * ID: 1192<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 5 minutes.
         */
        val FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES: SystemMessageId

        /**
         * ID: 1193<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 1 minute.
         */
        val FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE: SystemMessageId

        /**
         * ID: 1194<br></br>
         * Message: A mercenary can be assigned to a position from the beginning of the Seal Validatio period until the time when a siege starts.
         */
        val MERC_CAN_BE_ASSIGNED: SystemMessageId

        /**
         * ID: 1195<br></br>
         * Message: This mercenary cannot be assigned to a position by using the Seal of Strife.
         */
        val MERC_CANT_BE_ASSIGNED_USING_STRIFE: SystemMessageId

        /**
         * ID: 1196<br></br>
         * Message: Your force has reached maximum capacity.
         */
        val FORCE_MAXIMUM: SystemMessageId

        /**
         * ID: 1197<br></br>
         * Message: Summoning a servitor costs $s2 $s1.
         */
        val SUMMONING_SERVITOR_COSTS_S2_S1: SystemMessageId

        /**
         * ID: 1198<br></br>
         * Message: The item has been successfully crystallized.
         */
        val CRYSTALLIZATION_SUCCESSFUL: SystemMessageId

        /**
         * ID: 1199<br></br>
         * Message: =======<Clan War Target>=======
        </Clan> */
        val CLAN_WAR_HEADER: SystemMessageId

        /**
         * ID: 1200<br></br>
         * Message:($s1 ($s2 Alliance)
         */
        val S1_S2_ALLIANCE: SystemMessageId

        /**
         * ID: 1201<br></br>
         * Message: Please select the quest you wish to abort.
         */
        val SELECT_QUEST_TO_ABOR: SystemMessageId

        /**
         * ID: 1202<br></br>
         * Message:($s1 (No alliance exists)
         */
        val S1_NO_ALLI_EXISTS: SystemMessageId

        /**
         * ID: 1203<br></br>
         * Message: There is no clan war in progress.
         */
        val NO_WAR_IN_PROGRESS: SystemMessageId

        /**
         * ID: 1204<br></br>
         * Message: The screenshot has been saved. ($s1 $s2x$s3)
         */
        val SCREENSHOT: SystemMessageId

        /**
         * ID: 1205<br></br>
         * Message: Your mailbox is full. There is a 100 message limit.
         */
        val MAILBOX_FULL: SystemMessageId

        /**
         * ID: 1206<br></br>
         * Message: The memo box is full. There is a 100 memo limit.
         */
        val MEMOBOX_FULL: SystemMessageId

        /**
         * ID: 1207<br></br>
         * Message: Please make an entry in the field.
         */
        val MAKE_AN_ENTRY: SystemMessageId

        /**
         * ID: 1208<br></br>
         * Message: $s1 died and dropped $s3 $s2.
         */
        val S1_DIED_DROPPED_S3_S2: SystemMessageId

        /**
         * ID: 1209<br></br>
         * Message: Congratulations. Your raid was successful.
         */
        val RAID_WAS_SUCCESSFUL: SystemMessageId

        /**
         * ID: 1210<br></br>
         * Message: Seven Signs: The quest event period has begun. Visit a Priest of Dawn or Priestess of Dusk to participate in the event.
         */
        val QUEST_EVENT_PERIOD_BEGUN: SystemMessageId

        /**
         * ID: 1211<br></br>
         * Message: Seven Signs: The quest event period has ended. The next quest event will start in one week.
         */
        val QUEST_EVENT_PERIOD_ENDED: SystemMessageId

        /**
         * ID: 1212<br></br>
         * Message: Seven Signs: The Lords of Dawn have obtained the Seal of Avarice.
         */
        val DAWN_OBTAINED_AVARICE: SystemMessageId

        /**
         * ID: 1213<br></br>
         * Message: Seven Signs: The Lords of Dawn have obtained the Seal of Gnosis.
         */
        val DAWN_OBTAINED_GNOSIS: SystemMessageId

        /**
         * ID: 1214<br></br>
         * Message: Seven Signs: The Lords of Dawn have obtained the Seal of Strife.
         */
        val DAWN_OBTAINED_STRIFE: SystemMessageId

        /**
         * ID: 1215<br></br>
         * Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Avarice.
         */
        val DUSK_OBTAINED_AVARICE: SystemMessageId

        /**
         * ID: 1216<br></br>
         * Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Gnosis.
         */
        val DUSK_OBTAINED_GNOSIS: SystemMessageId

        /**
         * ID: 1217<br></br>
         * Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Strife.
         */
        val DUSK_OBTAINED_STRIFE: SystemMessageId

        /**
         * ID: 1218<br></br>
         * Message: Seven Signs: The Seal Validation period has begun.
         */
        val SEAL_VALIDATION_PERIOD_BEGUN: SystemMessageId

        /**
         * ID: 1219<br></br>
         * Message: Seven Signs: The Seal Validation period has ended.
         */
        val SEAL_VALIDATION_PERIOD_ENDED: SystemMessageId

        /**
         * ID: 1220<br></br>
         * Message: Are you sure you wish to summon it?
         */
        val SUMMON_CONFIRM: SystemMessageId

        /**
         * ID: 1221<br></br>
         * Message: Are you sure you wish to return it?
         */
        val RETURN_CONFIRM: SystemMessageId

        /**
         * ID: 1222<br></br>
         * Message: Current location : $s1, $s2, $s3 (GM Consultation Service)
         */
        val LOC_GM_CONSULATION_SERVICE_S1_S2_S3: SystemMessageId

        /**
         * ID: 1223<br></br>
         * Message: We depart for Talking Island in five minutes.
         */
        val DEPART_FOR_TALKING_5_MINUTES: SystemMessageId

        /**
         * ID: 1224<br></br>
         * Message: We depart for Talking Island in one minute.
         */
        val DEPART_FOR_TALKING_1_MINUTE: SystemMessageId

        /**
         * ID: 1225<br></br>
         * Message: All aboard for Talking Island
         */
        val DEPART_FOR_TALKING: SystemMessageId

        /**
         * ID: 1226<br></br>
         * Message: We are now leaving for Talking Island.
         */
        val LEAVING_FOR_TALKING: SystemMessageId

        /**
         * ID: 1227<br></br>
         * Message: You have $s1 unread messages.
         */
        val S1_UNREAD_MESSAGES: SystemMessageId

        /**
         * ID: 1228<br></br>
         * Message: $s1 has blocked you. You cannot send mail to $s1.
         */
        val S1_BLOCKED_YOU_CANNOT_MAIL: SystemMessageId

        /**
         * ID: 1229<br></br>
         * Message: No more messages may be sent at this time. Each account is allowed 10 messages per day.
         */
        val NO_MORE_MESSAGES_TODAY: SystemMessageId

        /**
         * ID: 1230<br></br>
         * Message: You are limited to five recipients at a time.
         */
        val ONLY_FIVE_RECIPIENTS: SystemMessageId

        /**
         * ID: 1231<br></br>
         * Message: You've sent mail.
         */
        val SENT_MAIL: SystemMessageId

        /**
         * ID: 1232<br></br>
         * Message: The message was not sent.
         */
        val MESSAGE_NOT_SENT: SystemMessageId

        /**
         * ID: 1233<br></br>
         * Message: You've got mail.
         */
        val NEW_MAIL: SystemMessageId

        /**
         * ID: 1234<br></br>
         * Message: The mail has been stored in your temporary mailbox.
         */
        val MAIL_STORED_IN_MAILBOX: SystemMessageId

        /**
         * ID: 1235<br></br>
         * Message: Do you wish to delete all your friends?
         */
        val ALL_FRIENDS_DELETE_CONFIRM: SystemMessageId

        /**
         * ID: 1236<br></br>
         * Message: Please enter security card number.
         */
        val ENTER_SECURITY_CARD_NUMBER: SystemMessageId

        /**
         * ID: 1237<br></br>
         * Message: Please enter the card number for number $s1.
         */
        val ENTER_CARD_NUMBER_FOR_S1: SystemMessageId

        /**
         * ID: 1238<br></br>
         * Message: Your temporary mailbox is full. No more mail can be stored; you have reached the 10 message limit.
         */
        val TEMP_MAILBOX_FULL: SystemMessageId

        /**
         * ID: 1239<br></br>
         * Message: The keyboard security module has failed to load. Please exit the game and try again.
         */
        val KEYBOARD_MODULE_FAILED_LOAD: SystemMessageId

        /**
         * ID: 1240<br></br>
         * Message: Seven Signs: The Revolutionaries of Dusk have won.
         */
        val DUSK_WON: SystemMessageId

        /**
         * ID: 1241<br></br>
         * Message: Seven Signs: The Lords of Dawn have won.
         */
        val DAWN_WON: SystemMessageId

        /**
         * ID: 1242<br></br>
         * Message: Users who have not verified their age may not log in between the hours if 10:00 p.m. and 6:00 a.m.
         */
        val NOT_VERIFIED_AGE_NO_LOGIN: SystemMessageId

        /**
         * ID: 1243<br></br>
         * Message: The security card number is invalid.
         */
        val SECURITY_CARD_NUMBER_INVALID: SystemMessageId

        /**
         * ID: 1244<br></br>
         * Message: Users who have not verified their age may not log in between the hours if 10:00 p.m. and 6:00 a.m. Logging off now
         */
        val NOT_VERIFIED_AGE_LOG_OFF: SystemMessageId

        /**
         * ID: 1245<br></br>
         * Message: You will be loged out in $s1 minutes.
         */
        val LOGOUT_IN_S1_MINUTES: SystemMessageId

        /**
         * ID: 1246<br></br>
         * Message: $s1 died and has dropped $s2 adena.
         */
        val S1_DIED_DROPPED_S2_ADENA: SystemMessageId

        /**
         * ID: 1247<br></br>
         * Message: The corpse is too old. The skill cannot be used.
         */
        val CORPSE_TOO_OLD_SKILL_NOT_USED: SystemMessageId

        /**
         * ID: 1248<br></br>
         * Message: You are out of feed. Mount status canceled.
         */
        val OUT_OF_FEED_MOUNT_CANCELED: SystemMessageId

        /**
         * ID: 1249<br></br>
         * Message: You may only ride a wyvern while you're riding a strider.
         */
        val YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER: SystemMessageId

        /**
         * ID: 1250<br></br>
         * Message: Do you really want to surrender? If you surrender during an alliance war, your Exp will drop the same as if you were to die once.
         */
        val SURRENDER_ALLY_WAR_CONFIRM: SystemMessageId

        /**
         * ID: 1251<br></br>
         * you will not be able to accept another clan to your alliance for one day.
         */
        val DISMISS_ALLY_CONFIRM: SystemMessageId

        /**
         * ID: 1252<br></br>
         * Message: Are you sure you want to surrender? Exp penalty will be the same as death.
         */
        val SURRENDER_CONFIRM1: SystemMessageId

        /**
         * ID: 1253<br></br>
         * Message: Are you sure you want to surrender? Exp penalty will be the same as death and you will not be allowed to participate in clan war.
         */
        val SURRENDER_CONFIRM2: SystemMessageId

        /**
         * ID: 1254<br></br>
         * Message: Thank you for submitting feedback.
         */
        val THANKS_FOR_FEEDBACK: SystemMessageId

        /**
         * ID: 1255<br></br>
         * Message: GM consultation has begun.
         */
        val GM_CONSULTATION_BEGUN: SystemMessageId

        /**
         * ID: 1256<br></br>
         * Message: Please write the name after the command.
         */
        val PLEASE_WRITE_NAME_AFTER_COMMAND: SystemMessageId

        /**
         * ID: 1257<br></br>
         * Message: The special skill of a servitor or pet cannot be registerd as a macro.
         */
        val PET_SKILL_NOT_AS_MACRO: SystemMessageId

        /**
         * ID: 1258<br></br>
         * Message: $s1 has been crystallized
         */
        val S1_CRYSTALLIZED: SystemMessageId

        /**
         * ID: 1259<br></br>
         * Message: =======<Alliance Target>=======
        </Alliance> */
        val ALLIANCE_TARGET_HEADER: SystemMessageId

        /**
         * ID: 1260<br></br>
         * Message: Seven Signs: Preparations have begun for the next quest event.
         */
        val PREPARATIONS_PERIOD_BEGUN: SystemMessageId

        /**
         * ID: 1261<br></br>
         * Message: Seven Signs: The quest event period has begun. Speak with a Priest of Dawn or Dusk Priestess if you wish to participate in the event.
         */
        val COMPETITION_PERIOD_BEGUN: SystemMessageId

        /**
         * ID: 1262<br></br>
         * Message: Seven Signs: Quest event has ended. Results are being tallied.
         */
        val RESULTS_PERIOD_BEGUN: SystemMessageId

        /**
         * ID: 1263<br></br>
         * Message: Seven Signs: This is the seal validation period. A new quest event period begins next Monday.
         */
        val VALIDATION_PERIOD_BEGUN: SystemMessageId

        /**
         * ID: 1264<br></br>
         * Message: This soul stone cannot currently absorb souls. Absorption has failed.
         */
        val STONE_CANNOT_ABSORB: SystemMessageId

        /**
         * ID: 1265<br></br>
         * Message: You can't absorb souls without a soul stone.
         */
        val CANT_ABSORB_WITHOUT_STONE: SystemMessageId

        /**
         * ID: 1266<br></br>
         * Message: The exchange has ended.
         */
        val EXCHANGE_HAS_ENDED: SystemMessageId

        /**
         * ID: 1267<br></br>
         * Message: Your contribution score is increased by $s1.
         */
        val CONTRIB_SCORE_INCREASED_S1: SystemMessageId

        /**
         * ID: 1268<br></br>
         * Message: Do you wish to add class as your sub class?
         */
        val ADD_SUBCLASS_CONFIRM: SystemMessageId

        /**
         * ID: 1269<br></br>
         * Message: The new sub class has been added.
         */
        val ADD_NEW_SUBCLASS: SystemMessageId

        /**
         * ID: 1270<br></br>
         * Message: The transfer of sub class has been completed.
         */
        val SUBCLASS_TRANSFER_COMPLETED: SystemMessageId

        /**
         * ID: 1271<br></br>
         * Message: Do you wish to participate? Until the next seal validation period, you are a member of the Lords of Dawn.
         */
        val DAWN_CONFIRM: SystemMessageId

        /**
         * ID: 1271<br></br>
         * Message: Do you wish to participate? Until the next seal validation period, you are a member of the Revolutionaries of Dusk.
         */
        val DUSK_CONFIRM: SystemMessageId

        /**
         * ID: 1273<br></br>
         * Message: You will participate in the Seven Signs as a member of the Lords of Dawn.
         */
        val SEVENSIGNS_PARTECIPATION_DAWN: SystemMessageId

        /**
         * ID: 1274<br></br>
         * Message: You will participate in the Seven Signs as a member of the Revolutionaries of Dusk.
         */
        val SEVENSIGNS_PARTECIPATION_DUSK: SystemMessageId

        /**
         * ID: 1275<br></br>
         * Message: You've chosen to fight for the Seal of Avarice during this quest event period.
         */
        val FIGHT_FOR_AVARICE: SystemMessageId

        /**
         * ID: 1276<br></br>
         * Message: You've chosen to fight for the Seal of Gnosis during this quest event period.
         */
        val FIGHT_FOR_GNOSIS: SystemMessageId

        /**
         * ID: 1277<br></br>
         * Message: You've chosen to fight for the Seal of Strife during this quest event period.
         */
        val FIGHT_FOR_STRIFE: SystemMessageId

        /**
         * ID: 1278<br></br>
         * Message: The NPC server is not operating at this time.
         */
        val NPC_SERVER_NOT_OPERATING: SystemMessageId

        /**
         * ID: 1279<br></br>
         * Message: Contribution level has exceeded the limit. You may not continue.
         */
        val CONTRIB_SCORE_EXCEEDED: SystemMessageId

        /**
         * ID: 1280<br></br>
         * Message: Magic Critical Hit!
         */
        val CRITICAL_HIT_MAGIC: SystemMessageId

        /**
         * ID: 1281<br></br>
         * Message: Your excellent shield defense was a success!
         */
        val YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS: SystemMessageId

        /**
         * ID: 1282<br></br>
         * Message: Your Karma has been changed to $s1
         */
        val YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1: SystemMessageId

        /**
         * ID: 1283<br></br>
         * Message: The minimum frame option has been activated.
         */
        val MINIMUM_FRAME_ACTIVATED: SystemMessageId

        /**
         * ID: 1284<br></br>
         * Message: The minimum frame option has been deactivated.
         */
        val MINIMUM_FRAME_DEACTIVATED: SystemMessageId

        /**
         * ID: 1285<br></br>
         * Message: No inventory exists: You cannot purchase an item.
         */
        val NO_INVENTORY_CANNOT_PURCHASE: SystemMessageId

        /**
         * ID: 1286<br></br>
         * Message: (Until next Monday at 6:00 p.m.)
         */
        val UNTIL_MONDAY_6PM: SystemMessageId

        /**
         * ID: 1287<br></br>
         * Message: (Until today at 6:00 p.m.)
         */
        val UNTIL_TODAY_6PM: SystemMessageId

        /**
         * ID: 1288<br></br>
         * Message: If trends continue, $s1 will win and the seal will belong to:
         */
        val S1_WILL_WIN_COMPETITION: SystemMessageId

        /**
         * ID: 1289<br></br>
         * Message: (Until next Monday at 6:00 p.m.)
         */
        val SEAL_OWNED_10_MORE_VOTED: SystemMessageId

        /**
         * ID: 1290<br></br>
         * Message: Although the seal was not owned, since 35 percent or more people have voted.
         */
        val SEAL_NOT_OWNED_35_MORE_VOTED: SystemMessageId

        /**
         * ID: 1291<br></br>
         * because less than 10 percent of people have voted.
         */
        val SEAL_OWNED_10_LESS_VOTED: SystemMessageId

        /**
         * ID: 1292<br></br>
         * and since less than 35 percent of people have voted.
         */
        val SEAL_NOT_OWNED_35_LESS_VOTED: SystemMessageId

        /**
         * ID: 1293<br></br>
         * Message: If current trends continue, it will end in a tie.
         */
        val COMPETITION_WILL_TIE: SystemMessageId

        /**
         * ID: 1294<br></br>
         * Message: The competition has ended in a tie. Therefore, nobody has been awarded the seal.
         */
        val COMPETITION_TIE_SEAL_NOT_AWARDED: SystemMessageId

        /**
         * ID: 1295<br></br>
         * Message: Sub classes may not be created or changed while a skill is in use.
         */
        val SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE: SystemMessageId

        /**
         * ID: 1296<br></br>
         * Message: You cannot open a Private Store here.
         */
        val NO_PRIVATE_STORE_HERE: SystemMessageId

        /**
         * ID: 1297<br></br>
         * Message: You cannot open a Private Workshop here.
         */
        val NO_PRIVATE_WORKSHOP_HERE: SystemMessageId

        /**
         * ID: 1298<br></br>
         * Message: Please confirm that you would like to exit the Monster Race Track.
         */
        val MONS_EXIT_CONFIRM: SystemMessageId

        /**
         * ID: 1299<br></br>
         * Message: $s1's casting has been interrupted.
         */
        val S1_CASTING_INTERRUPTED: SystemMessageId

        /**
         * ID: 1300<br></br>
         * Message: You are no longer trying on equipment.
         */
        val WEAR_ITEMS_STOPPED: SystemMessageId

        /**
         * ID: 1301<br></br>
         * Message: Only a Lord of Dawn may use this.
         */
        val CAN_BE_USED_BY_DAWN: SystemMessageId

        /**
         * ID: 1302<br></br>
         * Message: Only a Revolutionary of Dusk may use this.
         */
        val CAN_BE_USED_BY_DUSK: SystemMessageId

        /**
         * ID: 1303<br></br>
         * Message: This may only be used during the quest event period.
         */
        val CAN_BE_USED_DURING_QUEST_EVENT_PERIOD: SystemMessageId

        /**
         * ID: 1304<br></br>
         * except for an Alliance with a castle owning clan.
         */
        val STRIFE_CANCELED_DEFENSIVE_REGISTRATION: SystemMessageId

        /**
         * ID: 1305<br></br>
         * Message: Seal Stones may only be transferred during the quest event period.
         */
        val SEAL_STONES_ONLY_WHILE_QUEST: SystemMessageId

        /**
         * ID: 1306<br></br>
         * Message: You are no longer trying on equipment.
         */
        val NO_LONGER_TRYING_ON: SystemMessageId

        /**
         * ID: 1307<br></br>
         * Message: Only during the seal validation period may you settle your account.
         */
        val SETTLE_ACCOUNT_ONLY_IN_SEAL_VALIDATION: SystemMessageId

        /**
         * ID: 1308<br></br>
         * Message: Congratulations - You've completed a class transfer!
         */
        val CLASS_TRANSFER: SystemMessageId

        /**
         * ID: 1309<br></br>
         * Message:To use this option, you must have the lastest version of MSN Messenger installed on your computer.
         */
        val LATEST_MSN_REQUIRED: SystemMessageId

        /**
         * ID: 1310<br></br>
         * Message: For full functionality, the latest version of MSN Messenger must be installed on your computer.
         */
        val LATEST_MSN_RECOMMENDED: SystemMessageId

        /**
         * ID: 1311<br></br>
         * Message: Previous versions of MSN Messenger only provide the basic features for in-game MSN Messenger Chat. Add/Delete Contacts and other MSN Messenger options are not available
         */
        val MSN_ONLY_BASIC: SystemMessageId

        /**
         * ID: 1312<br></br>
         * Message: The latest version of MSN Messenger may be obtained from the MSN web site (http://messenger.msn.com).
         */
        val MSN_OBTAINED_FROM: SystemMessageId

        /**
         * ID: 1313<br></br>
         * Message: $s1, to better serve our customers, all chat histories [...]
         */
        val S1_CHAT_HISTORIES_STORED: SystemMessageId

        /**
         * ID: 1314<br></br>
         * Message: Please enter the passport ID of the person you wish to add to your contact list.
         */
        val ENTER_PASSPORT_FOR_ADDING: SystemMessageId

        /**
         * ID: 1315<br></br>
         * Message: Deleting a contact will remove that contact from MSN Messenger as well. The contact can still check your online status and well not be blocked from sending you a message.
         */
        val DELETING_A_CONTACT: SystemMessageId

        /**
         * ID: 1316<br></br>
         * Message: The contact will be deleted and blocked from your contact list.
         */
        val CONTACT_WILL_DELETED: SystemMessageId

        /**
         * ID: 1317<br></br>
         * Message: Would you like to delete this contact?
         */
        val CONTACT_DELETE_CONFIRM: SystemMessageId

        /**
         * ID: 1318<br></br>
         * Message: Please select the contact you want to block or unblock.
         */
        val SELECT_CONTACT_FOR_BLOCK_UNBLOCK: SystemMessageId

        /**
         * ID: 1319<br></br>
         * Message: Please select the name of the contact you wish to change to another group.
         */
        val SELECT_CONTACT_FOR_CHANGE_GROUP: SystemMessageId

        /**
         * ID: 1320<br></br>
         * Message: After selecting the group you wish to move your contact to, press the OK button.
         */
        val SELECT_GROUP_PRESS_OK: SystemMessageId

        /**
         * ID: 1321<br></br>
         * Message: Enter the name of the group you wish to add.
         */
        val ENTER_GROUP_NAME: SystemMessageId

        /**
         * ID: 1322<br></br>
         * Message: Select the group and enter the new name.
         */
        val SELECT_GROUP_ENTER_NAME: SystemMessageId

        /**
         * ID: 1323<br></br>
         * Message: Select the group you wish to delete and click the OK button.
         */
        val SELECT_GROUP_TO_DELETE: SystemMessageId

        /**
         * ID: 1324<br></br>
         * Message: Signing in...
         */
        val SIGNING_IN: SystemMessageId

        /**
         * ID: 1325<br></br>
         * Message: You've logged into another computer and have been logged out of the .NET Messenger Service on this computer.
         */
        val ANOTHER_COMPUTER_LOGOUT: SystemMessageId

        /**
         * ID: 1326<br></br>
         * Message: $s1 :
         */
        val S1_D: SystemMessageId

        /**
         * ID: 1327<br></br>
         * Message: The following message could not be delivered:
         */
        val MESSAGE_NOT_DELIVERED: SystemMessageId

        /**
         * ID: 1328<br></br>
         * Message: Members of the Revolutionaries of Dusk will not be resurrected.
         */
        val DUSK_NOT_RESURRECTED: SystemMessageId

        /**
         * ID: 1329<br></br>
         * Message: You are currently blocked from using the Private Store and Private Workshop.
         */
        val BLOCKED_FROM_USING_STORE: SystemMessageId

        /**
         * ID: 1330<br></br>
         * Message: You may not open a Private Store or Private Workshop for another $s1 minute(s)
         */
        val NO_STORE_FOR_S1_MINUTES: SystemMessageId

        /**
         * ID: 1331<br></br>
         * Message: You are no longer blocked from using the Private Store and Private Workshop
         */
        val NO_LONGER_BLOCKED_USING_STORE: SystemMessageId

        /**
         * ID: 1332<br></br>
         * Message: Items may not be used after your character or pet dies.
         */
        val NO_ITEMS_AFTER_DEATH: SystemMessageId

        /**
         * ID: 1333<br></br>
         * Message: The replay file is not accessible. Please verify that the replay.ini exists in your Linage 2 directory.
         */
        val REPLAY_INACCESSIBLE: SystemMessageId

        /**
         * ID: 1334<br></br>
         * Message: The new camera data has been stored.
         */
        val NEW_CAMERA_STORED: SystemMessageId

        /**
         * ID: 1335<br></br>
         * Message: The attempt to store the new camera data has failed.
         */
        val CAMERA_STORING_FAILED: SystemMessageId

        /**
         * ID: 1336<br></br>
         * Message: The replay file, $s1.$$s2 has been corrupted, please check the fle.
         */
        val REPLAY_S1_S2_CORRUPTED: SystemMessageId

        /**
         * ID: 1337<br></br>
         * Message: This will terminate the replay. Do you wish to continue?
         */
        val REPLAY_TERMINATE_CONFIRM: SystemMessageId

        /**
         * ID: 1338<br></br>
         * Message: You have exceeded the maximum amount that may be transferred at one time.
         */
        val EXCEEDED_MAXIMUM_AMOUNT: SystemMessageId

        /**
         * ID: 1339<br></br>
         * Message: Once a macro is assigned to a shortcut, it cannot be run as a macro again.
         */
        val MACRO_SHORTCUT_NOT_RUN: SystemMessageId

        /**
         * ID: 1340<br></br>
         * Message: This server cannot be accessed by the coupon you are using.
         */
        val SERVER_NOT_ACCESSED_BY_COUPON: SystemMessageId

        /**
         * ID: 1341<br></br>
         * Message: Incorrect name and/or email address.
         */
        val INCORRECT_NAME_OR_ADDRESS: SystemMessageId

        /**
         * ID: 1342<br></br>
         * Message: You are already logged in.
         */
        val ALREADY_LOGGED_IN: SystemMessageId

        /**
         * ID: 1343<br></br>
         * Message: Incorrect email address and/or password. Your attempt to log into .NET Messenger Service has failed.
         */
        val INCORRECT_ADDRESS_OR_PASSWORD: SystemMessageId

        /**
         * ID: 1344<br></br>
         * Message: Your request to log into the .NET Messenger service has failed. Please verify that you are currently connected to the internet.
         */
        val NET_LOGIN_FAILED: SystemMessageId

        /**
         * ID: 1345<br></br>
         * Message: Click the OK button after you have selected a contact name.
         */
        val SELECT_CONTACT_CLICK_OK: SystemMessageId

        /**
         * ID: 1346<br></br>
         * Message: You are currently entering a chat message.
         */
        val CURRENTLY_ENTERING_CHAT: SystemMessageId

        /**
         * ID: 1347<br></br>
         * Message: The Linage II messenger could not carry out the task you requested.
         */
        val MESSENGER_FAILED_CARRYING_OUT_TASK: SystemMessageId

        /**
         * ID: 1348<br></br>
         * Message: $s1 has entered the chat room.
         */
        val S1_ENTERED_CHAT_ROOM: SystemMessageId

        /**
         * ID: 1349<br></br>
         * Message: $s1 has left the chat room.
         */
        val S1_LEFT_CHAT_ROOM: SystemMessageId

        /**
         * ID: 1350<br></br>
         * Message: The state will be changed to indicate "off-line." All the chat windows currently opened will be closed.
         */
        val GOING_OFFLINE: SystemMessageId

        /**
         * ID: 1351<br></br>
         * Message: Click the Delete button after selecting the contact you wish to remove.
         */
        val SELECT_CONTACT_CLICK_REMOVE: SystemMessageId

        /**
         * ID: 1352<br></br>
         * Message: You have been added to $s1 ($s2)'s contact list.
         */
        val ADDED_TO_S1_S2_CONTACT_LIST: SystemMessageId

        /**
         * ID: 1353<br></br>
         * Message: You can set the option to show your status as always being off-line to all of your contacts.
         */
        val CAN_SET_OPTION_TO_ALWAYS_SHOW_OFFLINE: SystemMessageId

        /**
         * ID: 1354<br></br>
         * Message: You are not allowed to chat with a contact while chatting block is imposed.
         */
        val NO_CHAT_WHILE_BLOCKED: SystemMessageId

        /**
         * ID: 1355<br></br>
         * Message: The contact is currently blocked from chatting.
         */
        val CONTACT_CURRENTLY_BLOCKED: SystemMessageId

        /**
         * ID: 1356<br></br>
         * Message: The contact is not currently logged in.
         */
        val CONTACT_CURRENTLY_OFFLINE: SystemMessageId

        /**
         * ID: 1357<br></br>
         * Message: You have been blocked from chatting with that contact.
         */
        val YOU_ARE_BLOCKED: SystemMessageId

        /**
         * ID: 1358<br></br>
         * Message: You are being logged out...
         */
        val YOU_ARE_LOGGING_OUT: SystemMessageId

        /**
         * ID: 1359<br></br>
         * Message: $s1 has logged in.
         */
        val S1_LOGGED_IN2: SystemMessageId

        /**
         * ID: 1360<br></br>
         * Message: You have received a message from $s1.
         */
        val GOT_MESSAGE_FROM_S1: SystemMessageId

        /**
         * ID: 1361<br></br>
         * Message: Due to a system error, you have been logged out of the .NET Messenger Service.
         */
        val LOGGED_OUT_DUE_TO_ERROR: SystemMessageId

        /**
         * ID: 1362<br></br>
         * click the button next to My Status and then use the Options menu.
         */
        val SELECT_CONTACT_TO_DELETE: SystemMessageId

        /**
         * ID: 1363<br></br>
         * Message: Your request to participate in the alliance war has been denied.
         */
        val YOUR_REQUEST_ALLIANCE_WAR_DENIED: SystemMessageId

        /**
         * ID: 1364<br></br>
         * Message: The request for an alliance war has been rejected.
         */
        val REQUEST_ALLIANCE_WAR_REJECTED: SystemMessageId

        /**
         * ID: 1365<br></br>
         * Message: $s2 of $s1 clan has surrendered as an individual.
         */
        val S2_OF_S1_SURRENDERED_AS_INDIVIDUAL: SystemMessageId

        /**
         * ID: 1366<br></br>
         * Message: In order to delete a group, you must not [...]
         */
        val DELTE_GROUP_INSTRUCTION: SystemMessageId

        /**
         * ID: 1367<br></br>
         * Message: Only members of the group are allowed to add records.
         */
        val ONLY_GROUP_CAN_ADD_RECORDS: SystemMessageId

        /**
         * ID: 1368<br></br>
         * Message: You can not try those items on at the same time.
         */
        val YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME: SystemMessageId

        /**
         * ID: 1369<br></br>
         * Message: You've exceeded the maximum.
         */
        val EXCEEDED_THE_MAXIMUM: SystemMessageId

        /**
         * ID: 1370<br></br>
         * Message: Your message to $s1 did not reach its recipient. You cannot send mail to the GM staff.
         */
        val CANNOT_MAIL_GM_S1: SystemMessageId

        /**
         * ID: 1371<br></br>
         * Message: It has been determined that you're not engaged in normal gameplay and a restriction has been imposed upon you. You may not move for $s1 minutes.
         */
        val GAMEPLAY_RESTRICTION_PENALTY_S1: SystemMessageId

        /**
         * ID: 1372<br></br>
         * Message: Your punishment will continue for $s1 minutes.
         */
        val PUNISHMENT_CONTINUE_S1_MINUTES: SystemMessageId

        /**
         * ID: 1373<br></br>
         * Message: $s1 has picked up $s2 that was dropped by a Raid Boss.
         */
        val S1_OBTAINED_S2_FROM_RAIDBOSS: SystemMessageId

        /**
         * ID: 1374<br></br>
         * Message: $s1 has picked up $s3 $s2(s) that was dropped by a Raid Boss.
         */
        val S1_PICKED_UP_S3_S2_S_FROM_RAIDBOSS: SystemMessageId

        /**
         * ID: 1375<br></br>
         * Message: $s1 has picked up $s2 adena that was dropped by a Raid Boss.
         */
        val S1_OBTAINED_S2_ADENA_FROM_RAIDBOSS: SystemMessageId

        /**
         * ID: 1376<br></br>
         * Message: $s1 has picked up $s2 that was dropped by another character.
         */
        val S1_OBTAINED_S2_FROM_ANOTHER_CHARACTER: SystemMessageId

        /**
         * ID: 1377<br></br>
         * Message: $s1 has picked up $s3 $s2(s) that was dropped by a another character.
         */
        val S1_PICKED_UP_S3_S2_S_FROM_ANOTHER_CHARACTER: SystemMessageId

        /**
         * ID: 1378<br></br>
         * Message: $s1 has picked up +$s3 $s2 that was dropped by a another character.
         */
        val S1_PICKED_UP_S3_S2_FROM_ANOTHER_CHARACTER: SystemMessageId

        /**
         * ID: 1379<br></br>
         * Message: $s1 has obtained $s2 adena.
         */
        val S1_OBTAINED_S2_ADENA: SystemMessageId

        /**
         * ID: 1380<br></br>
         * Message: You can't summon a $s1 while on the battleground.
         */
        val CANT_SUMMON_S1_ON_BATTLEGROUND: SystemMessageId

        /**
         * ID: 1381<br></br>
         * Message: The party leader has obtained $s2 of $s1.
         */
        val LEADER_OBTAINED_S2_OF_S1: SystemMessageId

        /**
         * ID: 1382<br></br>
         * Message: To fulfill the quest, you must bring the chosen weapon. Are you sure you want to choose this weapon?
         */
        val CHOOSE_WEAPON_CONFIRM: SystemMessageId

        /**
         * ID: 1383<br></br>
         * Message: Are you sure you want to exchange?
         */
        val EXCHANGE_CONFIRM: SystemMessageId

        /**
         * ID: 1384<br></br>
         * Message: $s1 has become the party leader.
         */
        val S1_HAS_BECOME_A_PARTY_LEADER: SystemMessageId

        /**
         * ID: 1385<br></br>
         * Message: You are not allowed to dismount at this location.
         */
        val NO_DISMOUNT_HERE: SystemMessageId

        /**
         * ID: 1386<br></br>
         * Message: You are no longer held in place.
         */
        val NO_LONGER_HELD_IN_PLACE: SystemMessageId

        /**
         * ID: 1387<br></br>
         * Message: Please select the item you would like to try on.
         */
        val SELECT_ITEM_TO_TRY_ON: SystemMessageId

        /**
         * ID: 1388<br></br>
         * Message: A party room has been created.
         */
        val PARTY_ROOM_CREATED: SystemMessageId

        /**
         * ID: 1389<br></br>
         * Message: The party room's information has been revised.
         */
        val PARTY_ROOM_REVISED: SystemMessageId

        /**
         * ID: 1390<br></br>
         * Message: You are not allowed to enter the party room.
         */
        val PARTY_ROOM_FORBIDDEN: SystemMessageId

        /**
         * ID: 1391<br></br>
         * Message: You have exited from the party room.
         */
        val PARTY_ROOM_EXITED: SystemMessageId

        /**
         * ID: 1392<br></br>
         * Message: $s1 has left the party room.
         */
        val S1_LEFT_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1393<br></br>
         * Message: You have been ousted from the party room.
         */
        val OUSTED_FROM_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1394<br></br>
         * Message: $s1 has been kicked from the party room.
         */
        val S1_KICKED_FROM_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1395<br></br>
         * Message: The party room has been disbanded.
         */
        val PARTY_ROOM_DISBANDED: SystemMessageId

        /**
         * ID: 1396<br></br>
         * Message: The list of party rooms can only be viewed by a person who has not joined a party or who is currently the leader of a party.
         */
        val CANT_VIEW_PARTY_ROOMS: SystemMessageId

        /**
         * ID: 1397<br></br>
         * Message: The leader of the party room has changed.
         */
        val PARTY_ROOM_LEADER_CHANGED: SystemMessageId

        /**
         * ID: 1398<br></br>
         * Message: We are recruiting party members.
         */
        val RECRUITING_PARTY_MEMBERS: SystemMessageId

        /**
         * ID: 1399<br></br>
         * Message: Only the leader of the party can transfer party leadership to another player.
         */
        val ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER: SystemMessageId

        /**
         * ID: 1400<br></br>
         * Message: Please select the person you wish to make the party leader.
         */
        val PLEASE_SELECT_THE_PERSON_TO_WHOM_YOU_WOULD_LIKE_TO_TRANSFER_THE_RIGHTS_OF_A_PARTY_LEADER: SystemMessageId

        /**
         * ID: 1401<br></br>
         * Message: Slow down.you are already the party leader.
         */
        val YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF: SystemMessageId

        /**
         * ID: 1402<br></br>
         * Message: You may only transfer party leadership to another member of the party.
         */
        val YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER: SystemMessageId

        /**
         * ID: 1403<br></br>
         * Message: You have failed to transfer the party leadership.
         */
        val YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADER_RIGHTS: SystemMessageId

        /**
         * ID: 1404<br></br>
         * Message: The owner of the private manufacturing store has changed the price for creating this item. Please check the new price before trying again.
         */
        val MANUFACTURE_PRICE_HAS_CHANGED: SystemMessageId

        /**
         * ID: 1405<br></br>
         * Message: $s1 CPs have been restored.
         */
        val S1_CP_WILL_BE_RESTORED: SystemMessageId

        /**
         * ID: 1406<br></br>
         * Message: $s2 CPs has been restored by $s1.
         */
        val S2_CP_WILL_BE_RESTORED_BY_S1: SystemMessageId

        /**
         * ID: 1407<br></br>
         * Message: You are using a computer that does not allow you to log in with two accounts at the same time.
         */
        val NO_LOGIN_WITH_TWO_ACCOUNTS: SystemMessageId

        /**
         * ID: 1408<br></br>
         * Message: Your prepaid remaining usage time is $s1 hours and $s2 minutes. You have $s3 paid reservations left.
         */
        val PREPAID_LEFT_S1_S2_S3: SystemMessageId

        /**
         * ID: 1409<br></br>
         * Message: Your prepaid usage time has expired. Your new prepaid reservation will be used. The remaining usage time is $s1 hours and $s2 minutes.
         */
        val PREPAID_EXPIRED_S1_S2: SystemMessageId

        /**
         * ID: 1410<br></br>
         * Message: Your prepaid usage time has expired. You do not have any more prepaid reservations left.
         */
        val PREPAID_EXPIRED: SystemMessageId

        /**
         * ID: 1411<br></br>
         * Message: The number of your prepaid reservations has changed.
         */
        val PREPAID_CHANGED: SystemMessageId

        /**
         * ID: 1412<br></br>
         * Message: Your prepaid usage time has $s1 minutes left.
         */
        val PREPAID_LEFT_S1: SystemMessageId

        /**
         * ID: 1413<br></br>
         * Message: You do not meet the requirements to enter that party room.
         */
        val CANT_ENTER_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1414<br></br>
         * Message: The width and length should be 100 or more grids and less than 5000 grids respectively.
         */
        val WRONG_GRID_COUNT: SystemMessageId

        /**
         * ID: 1415<br></br>
         * Message: The command file is not sent.
         */
        val COMMAND_FILE_NOT_SENT: SystemMessageId

        /**
         * ID: 1416<br></br>
         * Message: The representative of Team 1 has not been selected.
         */
        val TEAM_1_NO_REPRESENTATIVE: SystemMessageId

        /**
         * ID: 1417<br></br>
         * Message: The representative of Team 2 has not been selected.
         */
        val TEAM_2_NO_REPRESENTATIVE: SystemMessageId

        /**
         * ID: 1418<br></br>
         * Message: The name of Team 1 has not yet been chosen.
         */
        val TEAM_1_NO_NAME: SystemMessageId

        /**
         * ID: 1419<br></br>
         * Message: The name of Team 2 has not yet been chosen.
         */
        val TEAM_2_NO_NAME: SystemMessageId

        /**
         * ID: 1420<br></br>
         * Message: The name of Team 1 and the name of Team 2 are identical.
         */
        val TEAM_NAME_IDENTICAL: SystemMessageId

        /**
         * ID: 1421<br></br>
         * Message: The race setup file has not been designated.
         */
        val RACE_SETUP_FILE1: SystemMessageId

        /**
         * ID: 1422<br></br>
         * Message: Race setup file error - BuffCnt is not specified
         */
        val RACE_SETUP_FILE2: SystemMessageId

        /**
         * ID: 1423<br></br>
         * Message: Race setup file error - BuffID$s1 is not specified.
         */
        val RACE_SETUP_FILE3: SystemMessageId

        /**
         * ID: 1424<br></br>
         * Message: Race setup file error - BuffLv$s1 is not specified.
         */
        val RACE_SETUP_FILE4: SystemMessageId

        /**
         * ID: 1425<br></br>
         * Message: Race setup file error - DefaultAllow is not specified
         */
        val RACE_SETUP_FILE5: SystemMessageId

        /**
         * ID: 1426<br></br>
         * Message: Race setup file error - ExpSkillCnt is not specified.
         */
        val RACE_SETUP_FILE6: SystemMessageId

        /**
         * ID: 1427<br></br>
         * Message: Race setup file error - ExpSkillID$s1 is not specified.
         */
        val RACE_SETUP_FILE7: SystemMessageId

        /**
         * ID: 1428<br></br>
         * Message: Race setup file error - ExpItemCnt is not specified.
         */
        val RACE_SETUP_FILE8: SystemMessageId

        /**
         * ID: 1429<br></br>
         * Message: Race setup file error - ExpItemID$s1 is not specified.
         */
        val RACE_SETUP_FILE9: SystemMessageId

        /**
         * ID: 1430<br></br>
         * Message: Race setup file error - TeleportDelay is not specified
         */
        val RACE_SETUP_FILE10: SystemMessageId

        /**
         * ID: 1431<br></br>
         * Message: The race will be stopped temporarily.
         */
        val RACE_STOPPED_TEMPORARILY: SystemMessageId

        /**
         * ID: 1432<br></br>
         * Message: Your opponent is currently in a petrified state.
         */
        val OPPONENT_PETRIFIED: SystemMessageId

        /**
         * ID: 1433<br></br>
         * Message: You will now automatically apply $s1 to your target.
         */
        val USE_OF_S1_WILL_BE_AUTO: SystemMessageId

        /**
         * ID: 1434<br></br>
         * Message: You will no longer automatically apply $s1 to your weapon.
         */
        val AUTO_USE_OF_S1_CANCELLED: SystemMessageId

        /**
         * ID: 1435<br></br>
         * Message: Due to insufficient $s1, the automatic use function has been deactivated.
         */
        val AUTO_USE_CANCELLED_LACK_OF_S1: SystemMessageId

        /**
         * ID: 1436<br></br>
         * Message: Due to insufficient $s1, the automatic use function cannot be activated.
         */
        val CANNOT_AUTO_USE_LACK_OF_S1: SystemMessageId

        /**
         * ID: 1437<br></br>
         * Message: Players are no longer allowed to play dice. Dice can no longer be purchased from a village store. However, you can still sell them to any village store.
         */
        val DICE_NO_LONGER_ALLOWED: SystemMessageId

        /**
         * ID: 1438<br></br>
         * Message: There is no skill that enables enchant.
         */
        val THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT: SystemMessageId

        /**
         * ID: 1439<br></br>
         * Message: You do not have all of the items needed to enchant that skill.
         */
        val YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL: SystemMessageId

        /**
         * ID: 1440<br></br>
         * Message: You have succeeded in enchanting the skill $s1.
         */
        val YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1: SystemMessageId

        /**
         * ID: 1441<br></br>
         * Message: Skill enchant failed. The skill will be initialized.
         */
        val YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1: SystemMessageId

        /**
         * ID: 1443<br></br>
         * Message: You do not have enough SP to enchant that skill.
         */
        val YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL: SystemMessageId

        /**
         * ID: 1444<br></br>
         * Message: You do not have enough experience (Exp) to enchant that skill.
         */
        val YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL: SystemMessageId

        /**
         * ID: 1445<br></br>
         * Message: Your previous subclass will be removed and replaced with the new subclass at level 40. Do you wish to continue?
         */
        val REPLACE_SUBCLASS_CONFIRM: SystemMessageId

        /**
         * ID: 1446<br></br>
         * Message: The ferry from $s1 to $s2 has been delayed.
         */
        val FERRY_FROM_S1_TO_S2_DELAYED: SystemMessageId

        /**
         * ID: 1447<br></br>
         * Message: You cannot do that while fishing.
         */
        val CANNOT_DO_WHILE_FISHING_1: SystemMessageId

        /**
         * ID: 1448<br></br>
         * Message: Only fishing skills may be used at this time.
         */
        val ONLY_FISHING_SKILLS_NOW: SystemMessageId

        /**
         * ID: 1449<br></br>
         * Message: You've got a bite!
         */
        val GOT_A_BITE: SystemMessageId

        /**
         * ID: 1450<br></br>
         * Message: That fish is more determined than you are - it spit the hook!
         */
        val FISH_SPIT_THE_HOOK: SystemMessageId

        /**
         * ID: 1451<br></br>
         * Message: Your bait was stolen by that fish!
         */
        val BAIT_STOLEN_BY_FISH: SystemMessageId

        /**
         * ID: 1452<br></br>
         * Message: Baits have been lost because the fish got away.
         */
        val BAIT_LOST_FISH_GOT_AWAY: SystemMessageId

        /**
         * ID: 1453<br></br>
         * Message: You do not have a fishing pole equipped.
         */
        val FISHING_POLE_NOT_EQUIPPED: SystemMessageId

        /**
         * ID: 1454<br></br>
         * Message: You must put bait on your hook before you can fish.
         */
        val BAIT_ON_HOOK_BEFORE_FISHING: SystemMessageId

        /**
         * ID: 1455<br></br>
         * Message: You cannot fish while under water.
         */
        val CANNOT_FISH_UNDER_WATER: SystemMessageId

        /**
         * ID: 1456<br></br>
         * Message: You cannot fish while riding as a passenger of a boat - it's against the rules.
         */
        val CANNOT_FISH_ON_BOAT: SystemMessageId

        /**
         * ID: 1457<br></br>
         * Message: You can't fish here.
         */
        val CANNOT_FISH_HERE: SystemMessageId

        /**
         * ID: 1458<br></br>
         * Message: Your attempt at fishing has been cancelled.
         */
        val FISHING_ATTEMPT_CANCELLED: SystemMessageId

        /**
         * ID: 1459<br></br>
         * Message: You do not have enough bait.
         */
        val NOT_ENOUGH_BAIT: SystemMessageId

        /**
         * ID: 1460<br></br>
         * Message: You reel your line in and stop fishing.
         */
        val REEL_LINE_AND_STOP_FISHING: SystemMessageId

        /**
         * ID: 1461<br></br>
         * Message: You cast your line and start to fish.
         */
        val CAST_LINE_AND_START_FISHING: SystemMessageId

        /**
         * ID: 1462<br></br>
         * Message: You may only use the Pumping skill while you are fishing.
         */
        val CAN_USE_PUMPING_ONLY_WHILE_FISHING: SystemMessageId

        /**
         * ID: 1463<br></br>
         * Message: You may only use the Reeling skill while you are fishing.
         */
        val CAN_USE_REELING_ONLY_WHILE_FISHING: SystemMessageId

        /**
         * ID: 1464<br></br>
         * Message: The fish has resisted your attempt to bring it in.
         */
        val FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN: SystemMessageId

        /**
         * ID: 1465<br></br>
         * Message: Your pumping is successful, causing $s1 damage.
         */
        val PUMPING_SUCCESFUL_S1_DAMAGE: SystemMessageId

        /**
         * ID: 1466<br></br>
         * Message: You failed to do anything with the fish and it regains $s1 HP.
         */
        val FISH_RESISTED_PUMPING_S1_HP_REGAINED: SystemMessageId

        /**
         * ID: 1467<br></br>
         * Message: You reel that fish in closer and cause $s1 damage.
         */
        val REELING_SUCCESFUL_S1_DAMAGE: SystemMessageId

        /**
         * ID: 1468<br></br>
         * Message: You failed to reel that fish in further and it regains $s1 HP.
         */
        val FISH_RESISTED_REELING_S1_HP_REGAINED: SystemMessageId

        /**
         * ID: 1469<br></br>
         * Message: You caught something!
         */
        val YOU_CAUGHT_SOMETHING: SystemMessageId

        /**
         * ID: 1470<br></br>
         * Message: You cannot do that while fishing.
         */
        val CANNOT_DO_WHILE_FISHING_2: SystemMessageId

        /**
         * ID: 1471<br></br>
         * Message: You cannot do that while fishing.
         */
        val CANNOT_DO_WHILE_FISHING_3: SystemMessageId

        /**
         * ID: 1472<br></br>
         * Message: You look oddly at the fishing pole in disbelief and realize that you can't attack anything with this.
         */
        val CANNOT_ATTACK_WITH_FISHING_POLE: SystemMessageId

        /**
         * ID: 1473<br></br>
         * Message: $s1 is not sufficient.
         */
        val S1_NOT_SUFFICIENT: SystemMessageId

        /**
         * ID: 1474<br></br>
         * Message: $s1 is not available.
         */
        val S1_NOT_AVAILABLE: SystemMessageId

        /**
         * ID: 1475<br></br>
         * Message: Pet has dropped $s1.
         */
        val PET_DROPPED_S1: SystemMessageId

        /**
         * ID: 1476<br></br>
         * Message: Pet has dropped +$s1 $s2.
         */
        val PET_DROPPED_S1_S2: SystemMessageId

        /**
         * ID: 1477<br></br>
         * Message: Pet has dropped $s2 of $s1.
         */
        val PET_DROPPED_S2_S1_S: SystemMessageId

        /**
         * ID: 1478<br></br>
         * Message: You may only register a 64 x 64 pixel, 256-color BMP.
         */
        val ONLY_64_PIXEL_256_COLOR_BMP: SystemMessageId

        /**
         * ID: 1479<br></br>
         * Message: That is the wrong grade of soulshot for that fishing pole.
         */
        val WRONG_FISHINGSHOT_GRADE: SystemMessageId

        /**
         * ID: 1480<br></br>
         * Message: Are you sure you want to remove yourself from the Grand Olympiad Games waiting list?
         */
        val OLYMPIAD_REMOVE_CONFIRM: SystemMessageId

        /**
         * ID: 1481<br></br>
         * Message: You have selected a class irrelevant individual match. Do you wish to participate?
         */
        val OLYMPIAD_NON_CLASS_CONFIRM: SystemMessageId

        /**
         * ID: 1482<br></br>
         * Message: You've selected to join a class specific game. Continue?
         */
        val OLYMPIAD_CLASS_CONFIRM: SystemMessageId

        /**
         * ID: 1483<br></br>
         * Message: Are you ready to be a Hero?
         */
        val HERO_CONFIRM: SystemMessageId

        /**
         * ID: 1484<br></br>
         * Message: Are you sure this is the Hero weapon you wish to use? Kamael race cannot use this.
         */
        val HERO_WEAPON_CONFIRM: SystemMessageId

        /**
         * ID: 1485<br></br>
         * Message: The ferry from Talking Island to Gludin Harbor has been delayed.
         */
        val FERRY_TALKING_GLUDIN_DELAYED: SystemMessageId

        /**
         * ID: 1486<br></br>
         * Message: The ferry from Gludin Harbor to Talking Island has been delayed.
         */
        val FERRY_GLUDIN_TALKING_DELAYED: SystemMessageId

        /**
         * ID: 1487<br></br>
         * Message: The ferry from Giran Harbor to Talking Island has been delayed.
         */
        val FERRY_GIRAN_TALKING_DELAYED: SystemMessageId

        /**
         * ID: 1488<br></br>
         * Message: The ferry from Talking Island to Giran Harbor has been delayed.
         */
        val FERRY_TALKING_GIRAN_DELAYED: SystemMessageId

        /**
         * ID: 1489<br></br>
         * Message: Innadril cruise service has been delayed.
         */
        val INNADRIL_BOAT_DELAYED: SystemMessageId

        /**
         * ID: 1490<br></br>
         * Message: Traded $s2 of crop $s1.
         */
        val TRADED_S2_OF_CROP_S1: SystemMessageId

        /**
         * ID: 1491<br></br>
         * Message: Failed in trading $s2 of crop $s1.
         */
        val FAILED_IN_TRADING_S2_OF_CROP_S1: SystemMessageId

        /**
         * ID: 1492<br></br>
         * Message: You will be moved to the Olympiad Stadium in $s1 second(s).
         */
        val YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S: SystemMessageId

        /**
         * ID: 1493<br></br>
         * Message: Your opponent made haste with their tail between their legs, the match has been cancelled.
         */
        val THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME: SystemMessageId

        /**
         * ID: 1494<br></br>
         * Message: Your opponent does not meet the requirements to do battle, the match has been cancelled.
         */
        val THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME: SystemMessageId

        /**
         * ID: 1495<br></br>
         * Message: The match will start in $s1 second(s).
         */
        val THE_GAME_WILL_START_IN_S1_SECOND_S: SystemMessageId

        /**
         * ID: 1496<br></br>
         * Message: The match has started, fight!
         */
        val STARTS_THE_GAME: SystemMessageId

        /**
         * ID: 1497<br></br>
         * Message: Congratulations, $s1! You win the match!
         */
        val S1_HAS_WON_THE_GAME: SystemMessageId

        /**
         * ID: 1498<br></br>
         * Message: There is no victor, the match ends in a tie.
         */
        val THE_GAME_ENDED_IN_A_TIE: SystemMessageId

        /**
         * ID: 1499<br></br>
         * Message: You will be moved back to town in $s1 second(s).
         */
        val YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 1500<br></br>
         * Message: You cannot participate in the Grand Olympiad Games with a character in their subclass.
         */
        val YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER: SystemMessageId

        /**
         * ID: 1501<br></br>
         * Message: Only Noblesse can participate in the Olympiad.
         */
        val ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD: SystemMessageId

        /**
         * ID: 1502<br></br>
         * Message: You have already been registered in a waiting list of an event.
         */
        val YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT: SystemMessageId

        /**
         * ID: 1503<br></br>
         * Message: You have been registered in the Grand Olympiad Games waiting list for a class specific match.
         */
        val YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES: SystemMessageId

        /**
         * ID: 1504<br></br>
         * Message: You have been registered in the Grand Olympiad Games waiting list for a non-class specific match.
         */
        val YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES: SystemMessageId

        /**
         * ID: 1505<br></br>
         * Message: You have been removed from the Grand Olympiad Games waiting list.
         */
        val YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME: SystemMessageId

        /**
         * ID: 1506<br></br>
         * Message: You are not currently registered on any Grand Olympiad Games waiting list.
         */
        val YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME: SystemMessageId

        /**
         * ID: 1507<br></br>
         * Message: You cannot equip that item in a Grand Olympiad Games match.
         */
        val THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT: SystemMessageId

        /**
         * ID: 1508<br></br>
         * Message: You cannot use that item in a Grand Olympiad Games match.
         */
        val THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT: SystemMessageId

        /**
         * ID: 1509<br></br>
         * Message: You cannot use that skill in a Grand Olympiad Games match.
         */
        val THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT: SystemMessageId

        /**
         * ID: 1510<br></br>
         * Message: $s1 is making an attempt at resurrection. Do you want to continue with this resurrection?
         */
        val RESSURECTION_REQUEST_BY_S1: SystemMessageId

        /**
         * ID: 1511<br></br>
         * Message: While a pet is attempting to resurrect, it cannot help in resurrecting its master.
         */
        val MASTER_CANNOT_RES: SystemMessageId

        /**
         * ID: 1512<br></br>
         * Message: You cannot resurrect a pet while their owner is being resurrected.
         */
        val CANNOT_RES_PET: SystemMessageId

        /**
         * ID: 1513<br></br>
         * Message: Resurrection has already been proposed.
         */
        val RES_HAS_ALREADY_BEEN_PROPOSED: SystemMessageId

        /**
         * ID: 1514<br></br>
         * Message: You cannot the owner of a pet while their pet is being resurrected
         */
        val CANNOT_RES_MASTER: SystemMessageId

        /**
         * ID: 1515<br></br>
         * Message: A pet cannot be resurrected while it's owner is in the process of resurrecting.
         */
        val CANNOT_RES_PET2: SystemMessageId

        /**
         * ID: 1516<br></br>
         * Message: The target is unavailable for seeding.
         */
        val THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING: SystemMessageId

        /**
         * ID: 1517<br></br>
         * Message: Failed in Blessed Enchant. The enchant value of the item became 0.
         */
        val BLESSED_ENCHANT_FAILED: SystemMessageId

        /**
         * ID: 1518<br></br>
         * Message: You do not meet the required condition to equip that item.
         */
        val CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION: SystemMessageId

        /**
         * ID: 1519<br></br>
         * Message: Your pet has been killed! Make sure you resurrect your pet within 20 minutes or your pet and all of it's items will disappear forever!
         */
        val MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_20_MINUTES: SystemMessageId

        /**
         * ID: 1520<br></br>
         * Message: Servitor passed away.
         */
        val SERVITOR_PASSED_AWAY: SystemMessageId

        /**
         * ID: 1521<br></br>
         * Message: Your servitor has vanished! You'll need to summon a new one.
         */
        val YOUR_SERVITOR_HAS_VANISHED: SystemMessageId

        /**
         * ID: 1522<br></br>
         * Message: Your pet's corpse has decayed!
         */
        val YOUR_PETS_CORPSE_HAS_DECAYED: SystemMessageId

        /**
         * ID: 1523<br></br>
         * Message: You should release your pet or servitor so that it does not fall off of the boat and drown!
         */
        val RELEASE_PET_ON_BOAT: SystemMessageId

        /**
         * ID: 1524<br></br>
         * Message: $s1's pet gained $s2.
         */
        val S1_PET_GAINED_S2: SystemMessageId

        /**
         * ID: 1525<br></br>
         * Message: $s1's pet gained $s3 of $s2.
         */
        val S1_PET_GAINED_S3_S2_S: SystemMessageId

        /**
         * ID: 1526<br></br>
         * Message: $s1's pet gained +$s2$s3.
         */
        val S1_PET_GAINED_S2_S3: SystemMessageId

        /**
         * ID: 1527<br></br>
         * Message: Your pet was hungry so it ate $s1.
         */
        val PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY: SystemMessageId

        /**
         * ID: 1528<br></br>
         * Message: You've sent a petition to the GM staff.
         */
        val SENT_PETITION_TO_GM: SystemMessageId

        /**
         * ID: 1529<br></br>
         * Message: $s1 is inviting you to the command channel. Do you want accept?
         */
        val COMMAND_CHANNEL_CONFIRM_FROM_S1: SystemMessageId

        /**
         * ID: 1530<br></br>
         * Message: Select a target or enter the name.
         */
        val SELECT_TARGET_OR_ENTER_NAME: SystemMessageId

        /**
         * ID: 1531<br></br>
         * Message: Enter the name of the clan that you wish to declare war on.
         */
        val ENTER_CLAN_NAME_TO_DECLARE_WAR2: SystemMessageId

        /**
         * ID: 1532<br></br>
         * Message: Enter the name of the clan that you wish to have a cease-fire with.
         */
        val ENTER_CLAN_NAME_TO_CEASE_FIRE: SystemMessageId

        /**
         * ID: 1533<br></br>
         * Message: Attention: $s1 has picked up $s2.
         */
        val ATTENTION_S1_PICKED_UP_S2: SystemMessageId

        /**
         * ID: 1534<br></br>
         * Message: Attention: $s1 has picked up +$s2$s3.
         */
        val ATTENTION_S1_PICKED_UP_S2_S3: SystemMessageId

        /**
         * ID: 1535<br></br>
         * Message: Attention: $s1's pet has picked up $s2.
         */
        val ATTENTION_S1_PET_PICKED_UP_S2: SystemMessageId

        /**
         * ID: 1536<br></br>
         * Message: Attention: $s1's pet has picked up +$s2$s3.
         */
        val ATTENTION_S1_PET_PICKED_UP_S2_S3: SystemMessageId

        /**
         * ID: 1537<br></br>
         * Message: Current Location: $s1, $s2, $s3 (near Rune Village)
         */
        val LOC_RUNE_S1_S2_S3: SystemMessageId

        /**
         * ID: 1538<br></br>
         * Message: Current Location: $s1, $s2, $s3 (near the Town of Goddard)
         */
        val LOC_GODDARD_S1_S2_S3: SystemMessageId

        /**
         * ID: 1539<br></br>
         * Message: Cargo has arrived at Talking Island Village.
         */
        val CARGO_AT_TALKING_VILLAGE: SystemMessageId

        /**
         * ID: 1540<br></br>
         * Message: Cargo has arrived at the Dark Elf Village.
         */
        val CARGO_AT_DARKELF_VILLAGE: SystemMessageId

        /**
         * ID: 1541<br></br>
         * Message: Cargo has arrived at Elven Village.
         */
        val CARGO_AT_ELVEN_VILLAGE: SystemMessageId

        /**
         * ID: 1542<br></br>
         * Message: Cargo has arrived at Orc Village.
         */
        val CARGO_AT_ORC_VILLAGE: SystemMessageId

        /**
         * ID: 1543<br></br>
         * Message: Cargo has arrived at Dwarfen Village.
         */
        val CARGO_AT_DWARVEN_VILLAGE: SystemMessageId

        /**
         * ID: 1544<br></br>
         * Message: Cargo has arrived at Aden Castle Town.
         */
        val CARGO_AT_ADEN: SystemMessageId

        /**
         * ID: 1545<br></br>
         * Message: Cargo has arrived at Town of Oren.
         */
        val CARGO_AT_OREN: SystemMessageId

        /**
         * ID: 1546<br></br>
         * Message: Cargo has arrived at Hunters Village.
         */
        val CARGO_AT_HUNTERS: SystemMessageId

        /**
         * ID: 1547<br></br>
         * Message: Cargo has arrived at the Town of Dion.
         */
        val CARGO_AT_DION: SystemMessageId

        /**
         * ID: 1548<br></br>
         * Message: Cargo has arrived at Floran Village.
         */
        val CARGO_AT_FLORAN: SystemMessageId

        /**
         * ID: 1549<br></br>
         * Message: Cargo has arrived at Gludin Village.
         */
        val CARGO_AT_GLUDIN: SystemMessageId

        /**
         * ID: 1550<br></br>
         * Message: Cargo has arrived at the Town of Gludio.
         */
        val CARGO_AT_GLUDIO: SystemMessageId

        /**
         * ID: 1551<br></br>
         * Message: Cargo has arrived at Giran Castle Town.
         */
        val CARGO_AT_GIRAN: SystemMessageId

        /**
         * ID: 1552<br></br>
         * Message: Cargo has arrived at Heine.
         */
        val CARGO_AT_HEINE: SystemMessageId

        /**
         * ID: 1553<br></br>
         * Message: Cargo has arrived at Rune Village.
         */
        val CARGO_AT_RUNE: SystemMessageId

        /**
         * ID: 1554<br></br>
         * Message: Cargo has arrived at the Town of Goddard.
         */
        val CARGO_AT_GODDARD: SystemMessageId

        /**
         * ID: 1555<br></br>
         * Message: Do you want to cancel character deletion?
         */
        val CANCEL_CHARACTER_DELETION_CONFIRM: SystemMessageId

        /**
         * ID: 1556<br></br>
         * Message: Your clan notice has been saved.
         */
        val CLAN_NOTICE_SAVED: SystemMessageId

        /**
         * ID: 1557<br></br>
         * Message: Seed price should be more than $s1 and less than $s2.
         */
        val SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2: SystemMessageId

        /**
         * ID: 1558<br></br>
         * Message: The quantity of seed should be more than $s1 and less than $s2.
         */
        val THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2: SystemMessageId

        /**
         * ID: 1559<br></br>
         * Message: Crop price should be more than $s1 and less than $s2.
         */
        val CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2: SystemMessageId

        /**
         * ID: 1560<br></br>
         * Message: The quantity of crop should be more than $s1 and less than $s2
         */
        val THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2: SystemMessageId

        /**
         * ID: 1561<br></br>
         * Message: The clan, $s1, has declared a Clan War.
         */
        val CLAN_S1_DECLARED_WAR: SystemMessageId

        /**
         * ID: 1562<br></br>
         * Message: A Clan War has been declared against the clan, $s1. you will only lose a quarter of the normal experience from death.
         */
        val CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP: SystemMessageId

        /**
         * ID: 1563<br></br>
         * Message: The clan, $s1, cannot declare a Clan War because their clan is less than level three, and or they do not have enough members.
         */
        val S1_CLAN_CANNOT_DECLARE_WAR_TOO_LOW_LEVEL_OR_NOT_ENOUGH_MEMBERS: SystemMessageId

        /**
         * ID: 1564<br></br>
         * Message: A Clan War can be declared only if the clan is level three or above, and the number of clan members is fifteen or greater.
         */
        val CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER: SystemMessageId

        /**
         * ID: 1565<br></br>
         * Message: A Clan War cannot be declared against a clan that does not exist!
         */
        val CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST: SystemMessageId

        /**
         * ID: 1566<br></br>
         * Message: The clan, $s1, has decided to stop the war.
         */
        val CLAN_S1_HAS_DECIDED_TO_STOP: SystemMessageId

        /**
         * ID: 1567<br></br>
         * Message: The war against $s1 Clan has been stopped.
         */
        val WAR_AGAINST_S1_HAS_STOPPED: SystemMessageId

        /**
         * ID: 1568<br></br>
         * Message: The target for declaration is wrong.
         */
        val WRONG_DECLARATION_TARGET: SystemMessageId

        /**
         * ID: 1569<br></br>
         * Message: A declaration of Clan War against an allied clan can't be made.
         */
        val CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK: SystemMessageId

        /**
         * ID: 1570<br></br>
         * Message: A declaration of war against more than 30 Clans can't be made at the same time
         */
        val TOO_MANY_CLAN_WARS: SystemMessageId

        /**
         * ID: 1571<br></br>
         * Message: ======<Clans You></Clans>'ve Declared War On>======
         */
        val CLANS_YOU_DECLARED_WAR_ON: SystemMessageId

        /**
         * ID: 1572<br></br>
         * Message: ======<Clans That Have Declared War On You>======
        </Clans> */
        val CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU: SystemMessageId

        /**
         * ID: 1573<br></br>
         * Message: There are no clans that your clan has declared war against.
         */
        val YOU_ARENT_IN_CLAN_WARS: SystemMessageId

        /**
         * ID: 1574<br></br>
         * Message: All is well. There are no clans that have declared war against your clan.
         */
        val NO_CLAN_WARS_VS_YOU: SystemMessageId

        /**
         * ID: 1575<br></br>
         * Message: Command Channels can only be formed by a party leader who is also the leader of a level 5 clan.
         */
        val COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER: SystemMessageId

        /**
         * ID: 1576<br></br>
         * Message: Pet uses the power of spirit.
         */
        val PET_USE_THE_POWER_OF_SPIRIT: SystemMessageId

        /**
         * ID: 1577<br></br>
         * Message: Servitor uses the power of spirit.
         */
        val SERVITOR_USE_THE_POWER_OF_SPIRIT: SystemMessageId

        /**
         * ID: 1578<br></br>
         * Message: Items are not available for a private store or a private manufacture.
         */
        val ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE: SystemMessageId

        /**
         * ID: 1579<br></br>
         * Message: $s1's pet gained $s2 adena.
         */
        val S1_PET_GAINED_S2_ADENA: SystemMessageId

        /**
         * ID: 1580<br></br>
         * Message: The Command Channel has been formed.
         */
        val COMMAND_CHANNEL_FORMED: SystemMessageId

        /**
         * ID: 1581<br></br>
         * Message: The Command Channel has been disbanded.
         */
        val COMMAND_CHANNEL_DISBANDED: SystemMessageId

        /**
         * ID: 1582<br></br>
         * Message: You have joined the Command Channel.
         */
        val JOINED_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1583<br></br>
         * Message: You were dismissed from the Command Channel.
         */
        val DISMISSED_FROM_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1584<br></br>
         * Message: $s1's party has been dismissed from the Command Channel.
         */
        val S1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1585<br></br>
         * Message: The Command Channel has been disbanded.
         */
        val COMMAND_CHANNEL_DISBANDED2: SystemMessageId

        /**
         * ID: 1586<br></br>
         * Message: You have quit the Command Channel.
         */
        val LEFT_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1587<br></br>
         * Message: $s1's party has left the Command Channel.
         */
        val S1_PARTY_LEFT_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1588<br></br>
         * Message: The Command Channel is activated only when there are at least 5 parties participating.
         */
        val COMMAND_CHANNEL_ONLY_AT_LEAST_5_PARTIES: SystemMessageId

        /**
         * ID: 1589<br></br>
         * Message: Command Channel authority has been transferred to $s1.
         */
        val COMMAND_CHANNEL_LEADER_NOW_S1: SystemMessageId

        /**
         * ID: 1590<br></br>
         * Message: ===<Guild Info (Total Parties: $s1)>===
        </Guild> */
        val GUILD_INFO_HEADER: SystemMessageId

        /**
         * ID: 1591<br></br>
         * Message: No user has been invited to the Command Channel.
         */
        val NO_USER_INVITED_TO_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1592<br></br>
         * Message: You can no longer set up a Command Channel.
         */
        val CANNOT_LONGER_SETUP_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1593<br></br>
         * Message: You do not have authority to invite someone to the Command Channel.
         */
        val CANNOT_INVITE_TO_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1594<br></br>
         * Message: $s1's party is already a member of the Command Channel.
         */
        val S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1595<br></br>
         * Message: $s1 has succeeded.
         */
        val S1_SUCCEEDED: SystemMessageId

        /**
         * ID: 1596<br></br>
         * Message: You were hit by $s1!
         */
        val HIT_BY_S1: SystemMessageId

        /**
         * ID: 1597<br></br>
         * Message: $s1 has failed.
         */
        val S1_FAILED: SystemMessageId

        /**
         * ID: 1598<br></br>
         * Message: Soulshots and spiritshots are not available for a dead pet or servitor. Sad, isn't it?
         */
        val SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET: SystemMessageId

        /**
         * ID: 1599<br></br>
         * Message: You cannot observe while you are in combat!
         */
        val CANNOT_OBSERVE_IN_COMBAT: SystemMessageId

        /**
         * ID: 1600<br></br>
         * Message: Tomorrow's items will ALL be set to 0. Do you wish to continue?
         */
        val TOMORROW_ITEM_ZERO_CONFIRM: SystemMessageId

        /**
         * ID: 1601<br></br>
         * Message: Tomorrow's items will all be set to the same value as today's items. Do you wish to continue?
         */
        val TOMORROW_ITEM_SAME_CONFIRM: SystemMessageId

        /**
         * ID: 1602<br></br>
         * Message: Only a party leader can access the Command Channel.
         */
        val COMMAND_CHANNEL_ONLY_FOR_PARTY_LEADER: SystemMessageId

        /**
         * ID: 1603<br></br>
         * Message: Only channel operator can give All Command.
         */
        val ONLY_COMMANDER_GIVE_COMMAND: SystemMessageId

        /**
         * ID: 1604<br></br>
         * Message: While dressed in formal wear, you can't use items that require all skills and casting operations.
         */
        val CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR: SystemMessageId

        /**
         * ID: 1605<br></br>
         * Message: * Here, you can buy only seeds of $s1 Manor.
         */
        val HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR: SystemMessageId

        /**
         * ID: 1606<br></br>
         * Message: Congratulations - You've completed the third-class transfer quest!
         */
        val THIRD_CLASS_TRANSFER: SystemMessageId

        /**
         * ID: 1607<br></br>
         * Message: $s1 adena has been withdrawn to pay for purchasing fees.
         */
        val S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES: SystemMessageId

        /**
         * ID: 1608<br></br>
         * Message: Due to insufficient adena you cannot buy another castle.
         */
        val INSUFFICIENT_ADENA_TO_BUY_CASTLE: SystemMessageId

        /**
         * ID: 1609<br></br>
         * Message: War has already been declared against that clan... but I'll make note that you really don't like them.
         */
        val WAR_ALREADY_DECLARED: SystemMessageId

        /**
         * ID: 1610<br></br>
         * Message: Fool! You cannot declare war against your own clan!
         */
        val CANNOT_DECLARE_AGAINST_OWN_CLAN: SystemMessageId

        /**
         * ID: 1611<br></br>
         * Message: Leader: $s1
         */
        val PARTY_LEADER_S1: SystemMessageId

        /**
         * ID: 1612<br></br>
         * Message: =====<War List>=====
        </War> */
        val WAR_LIST: SystemMessageId

        /**
         * ID: 1613<br></br>
         * Message: There is no clan listed on War List.
         */
        val NO_CLAN_ON_WAR_LIST: SystemMessageId

        /**
         * ID: 1614<br></br>
         * Message: You have joined a channel that was already open.
         */
        val JOINED_CHANNEL_ALREADY_OPEN: SystemMessageId

        /**
         * ID: 1615<br></br>
         * Message: The number of remaining parties is $s1 until a channel is activated
         */
        val S1_PARTIES_REMAINING_UNTIL_CHANNEL: SystemMessageId

        /**
         * ID: 1616<br></br>
         * Message: The Command Channel has been activated.
         */
        val COMMAND_CHANNEL_ACTIVATED: SystemMessageId

        /**
         * ID: 1617<br></br>
         * Message: You do not have the authority to use the Command Channel.
         */
        val CANT_USE_COMMAND_CHANNEL: SystemMessageId

        /**
         * ID: 1618<br></br>
         * Message: The ferry from Rune Harbor to Gludin Harbor has been delayed.
         */
        val FERRY_RUNE_GLUDIN_DELAYED: SystemMessageId

        /**
         * ID: 1619<br></br>
         * Message: The ferry from Gludin Harbor to Rune Harbor has been delayed.
         */
        val FERRY_GLUDIN_RUNE_DELAYED: SystemMessageId

        /**
         * ID: 1620<br></br>
         * Message: Arrived at Rune Harbor.
         */
        val ARRIVED_AT_RUNE: SystemMessageId

        /**
         * ID: 1621<br></br>
         * Message: Departure for Gludin Harbor will take place in five minutes!
         */
        val DEPARTURE_FOR_GLUDIN_5_MINUTES: SystemMessageId

        /**
         * ID: 1622<br></br>
         * Message: Departure for Gludin Harbor will take place in one minute!
         */
        val DEPARTURE_FOR_GLUDIN_1_MINUTE: SystemMessageId

        /**
         * ID: 1623<br></br>
         * Message: Make haste! We will be departing for Gludin Harbor shortly...
         */
        val DEPARTURE_FOR_GLUDIN_SHORTLY: SystemMessageId

        /**
         * ID: 1624<br></br>
         * Message: We are now departing for Gludin Harbor Hold on and enjoy the ride!
         */
        val DEPARTURE_FOR_GLUDIN_NOW: SystemMessageId

        /**
         * ID: 1625<br></br>
         * Message: Departure for Rune Harbor will take place after anchoring for ten minutes.
         */
        val DEPARTURE_FOR_RUNE_10_MINUTES: SystemMessageId

        /**
         * ID: 1626<br></br>
         * Message: Departure for Rune Harbor will take place in five minutes!
         */
        val DEPARTURE_FOR_RUNE_5_MINUTES: SystemMessageId

        /**
         * ID: 1627<br></br>
         * Message: Departure for Rune Harbor will take place in one minute!
         */
        val DEPARTURE_FOR_RUNE_1_MINUTE: SystemMessageId

        /**
         * ID: 1628<br></br>
         * Message: Make haste! We will be departing for Gludin Harbor shortly...
         */
        val DEPARTURE_FOR_GLUDIN_SHORTLY2: SystemMessageId

        /**
         * ID: 1629<br></br>
         * Message: We are now departing for Rune Harbor Hold on and enjoy the ride!
         */
        val DEPARTURE_FOR_RUNE_NOW: SystemMessageId

        /**
         * ID: 1630<br></br>
         * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 15 minutes.
         */
        val FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES: SystemMessageId

        /**
         * ID: 1631<br></br>
         * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.
         */
        val FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES: SystemMessageId

        /**
         * ID: 1632<br></br>
         * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.
         */
        val FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES: SystemMessageId

        /**
         * ID: 1633<br></br>
         * Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 1 minute.
         */
        val FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE: SystemMessageId

        /**
         * ID: 1634<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 15 minutes.
         */
        val FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES: SystemMessageId

        /**
         * ID: 1635<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Rune harbor in approximately 10 minutes.
         */
        val FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES: SystemMessageId

        /**
         * ID: 1636<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 10 minutes.
         */
        val FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES: SystemMessageId

        /**
         * ID: 1637<br></br>
         * Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 1 minute.
         */
        val FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE: SystemMessageId

        /**
         * ID: 1638<br></br>
         * Message: You cannot fish while using a recipe book, private manufacture or private store.
         */
        val CANNOT_FISH_WHILE_USING_RECIPE_BOOK: SystemMessageId

        /**
         * ID: 1639<br></br>
         * Message: Period $s1 of the Grand Olympiad Games has started!
         */
        val OLYMPIAD_PERIOD_S1_HAS_STARTED: SystemMessageId

        /**
         * ID: 1640<br></br>
         * Message: Period $s1 of the Grand Olympiad Games has now ended.
         */
        val OLYMPIAD_PERIOD_S1_HAS_ENDED: SystemMessageId

        /**
         * ID: 1641<br></br>
         * and make haste to a Grand Olympiad Manager! Battles in the Grand Olympiad Games are now taking place!
         */
        val THE_OLYMPIAD_GAME_HAS_STARTED: SystemMessageId

        /**
         * ID: 1642<br></br>
         * Message: Much carnage has been left for the cleanup crew of the Olympiad Stadium. Battles in the Grand Olympiad Games are now over!
         */
        val THE_OLYMPIAD_GAME_HAS_ENDED: SystemMessageId

        /**
         * ID: 1643<br></br>
         * Message: Current Location: $s1, $s2, $s3 (Dimensional Gap)
         */
        val LOC_DIMENSIONAL_GAP_S1_S2_S3: SystemMessageId

        // 1644 - 1648: none

        /**
         * ID: 1649<br></br>
         * Message: Play time is now accumulating.
         */
        val PLAY_TIME_NOW_ACCUMULATING: SystemMessageId

        /**
         * ID: 1650<br></br>
         * Message: Due to high server traffic, your login attempt has failed. Please try again soon.
         */
        val TRY_LOGIN_LATER: SystemMessageId

        /**
         * ID: 1651<br></br>
         * Message: The Grand Olympiad Games are not currently in progress.
         */
        val THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS: SystemMessageId

        /**
         * ID: 1652<br></br>
         * Message: You are now recording gameplay.
         */
        val RECORDING_GAMEPLAY_START: SystemMessageId

        /**
         * ID: 1653<br></br>
         * Message: Your recording has been successfully stored. ($s1)
         */
        val RECORDING_GAMEPLAY_STOP_S1: SystemMessageId

        /**
         * ID: 1654<br></br>
         * Message: Your attempt to record the replay file has failed.
         */
        val RECORDING_GAMEPLAY_FAILED: SystemMessageId

        /**
         * ID: 1655<br></br>
         * Message: You caught something smelly and scary, maybe you should throw it back!?
         */
        val YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK: SystemMessageId

        /**
         * ID: 1656<br></br>
         * Message: You have successfully traded the item with the NPC.
         */
        val SUCCESSFULLY_TRADED_WITH_NPC: SystemMessageId

        /**
         * ID: 1657<br></br>
         * Message: $s1 has earned $s2 points in the Grand Olympiad Games.
         */
        val S1_HAS_GAINED_S2_OLYMPIAD_POINTS: SystemMessageId

        /**
         * ID: 1658<br></br>
         * Message: $s1 has lost $s2 points in the Grand Olympiad Games.
         */
        val S1_HAS_LOST_S2_OLYMPIAD_POINTS: SystemMessageId

        /**
         * ID: 1659<br></br>
         * Message: Current Location: $s1, $s2, $s3 (Cemetery of the Empire)
         */
        val LOC_CEMETARY_OF_THE_EMPIRE_S1_S2_S3: SystemMessageId

        /**
         * ID: 1660<br></br>
         * Message: Channel Creator: $s1.
         */
        val CHANNEL_CREATOR_S1: SystemMessageId

        /**
         * ID: 1661<br></br>
         * Message: $s1 has obtained $s3 $s2s.
         */
        val S1_OBTAINED_S3_S2_S: SystemMessageId

        /**
         * ID: 1662<br></br>
         * Message: The fish are no longer biting here because you've caught too many! Try fishing in another location.
         */
        val FISH_NO_MORE_BITING_TRY_OTHER_LOCATION: SystemMessageId

        /**
         * ID: 1663<br></br>
         * Message: The clan crest was successfully registered. Remember, only a clan that owns a clan hall or castle can have their crest displayed.
         */
        val CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED: SystemMessageId

        /**
         * ID: 1664<br></br>
         * Message: The fish is resisting your efforts to haul it in! Look at that bobber go!
         */
        val FISH_RESISTING_LOOK_BOBBLER: SystemMessageId

        /**
         * ID: 1665<br></br>
         * Message: You've worn that fish out! It can't even pull the bobber under the water!
         */
        val YOU_WORN_FISH_OUT: SystemMessageId

        /**
         * ID: 1666<br></br>
         * Message: You have obtained +$s1 $s2.
         */
        val OBTAINED_S1_S2: SystemMessageId

        /**
         * ID: 1667<br></br>
         * Message: Lethal Strike!
         */
        val LETHAL_STRIKE: SystemMessageId

        /**
         * ID: 1668<br></br>
         * Message: Your lethal strike was successful!
         */
        val LETHAL_STRIKE_SUCCESSFUL: SystemMessageId

        /**
         * ID: 1669<br></br>
         * Message: There was nothing found inside of that.
         */
        val NOTHING_INSIDE_THAT: SystemMessageId

        /**
         * ID: 1670<br></br>
         * Message: Due to your Reeling and/or Pumping skill being three or more levels higher than your Fishing skill, a 50 damage penalty will be applied.
         */
        val REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY: SystemMessageId

        /**
         * ID: 1671<br></br>
         * Message: Your reeling was successful! (Mastery Penalty:$s1 )
         */
        val REELING_SUCCESSFUL_PENALTY_S1: SystemMessageId

        /**
         * ID: 1672<br></br>
         * Message: Your pumping was successful! (Mastery Penalty:$s1 )
         */
        val PUMPING_SUCCESSFUL_PENALTY_S1: SystemMessageId

        /**
         * ID: 1673<br></br>
         * Message: Your current record for this Grand Olympiad is $s1 match(es), $s2 win(s) and $s3 defeat(s). You have earned $s4 Olympiad Point(s).
         */
        val THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS: SystemMessageId

        /**
         * ID: 1674<br></br>
         * Message: This command can only be used by a Noblesse.
         */
        val NOBLESSE_ONLY: SystemMessageId

        /**
         * ID: 1675<br></br>
         * Message: A manor cannot be set up between 6 a.m. and 8 p.m.
         */
        val A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM: SystemMessageId

        /**
         * ID: 1676<br></br>
         * Message: You do not have a servitor or pet and therefore cannot use the automatic-use function.
         */
        val NO_SERVITOR_CANNOT_AUTOMATE_USE: SystemMessageId

        /**
         * ID: 1677<br></br>
         * Message: A cease-fire during a Clan War can not be called while members of your clan are engaged in battle.
         */
        val CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT: SystemMessageId

        /**
         * ID: 1678<br></br>
         * Message: You have not declared a Clan War against the clan $s1.
         */
        val NO_CLAN_WAR_AGAINST_CLAN_S1: SystemMessageId

        /**
         * ID: 1679<br></br>
         * Message: Only the creator of a channel can issue a global command.
         */
        val ONLY_CHANNEL_CREATOR_CAN_GLOBAL_COMMAND: SystemMessageId

        /**
         * ID: 1680<br></br>
         * Message: $s1 has declined the channel invitation.
         */
        val S1_DECLINED_CHANNEL_INVITATION: SystemMessageId

        /**
         * ID: 1681<br></br>
         * Message: Since $s1 did not respond, your channel invitation has failed.
         */
        val S1_DID_NOT_RESPOND_CHANNEL_INVITATION_FAILED: SystemMessageId

        /**
         * ID: 1682<br></br>
         * Message: Only the creator of a channel can use the channel dismiss command.
         */
        val ONLY_CHANNEL_CREATOR_CAN_DISMISS: SystemMessageId

        /**
         * ID: 1683<br></br>
         * Message: Only a party leader can choose the option to leave a channel.
         */
        val ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL: SystemMessageId

        /**
         * ID: 1684<br></br>
         * Message: A Clan War can not be declared against a clan that is being dissolved.
         */
        val NO_CLAN_WAR_AGAINST_DISSOLVING_CLAN: SystemMessageId

        /**
         * ID: 1685<br></br>
         * Message: You are unable to equip this item when your PK count is greater or equal to one.
         */
        val YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE: SystemMessageId

        /**
         * ID: 1686<br></br>
         * Message: Stones and mortar tumble to the earth - the castle wall has taken damage!
         */
        val CASTLE_WALL_DAMAGED: SystemMessageId

        /**
         * ID: 1687<br></br>
         * Message: This area cannot be entered while mounted atop of a Wyvern. You will be dismounted from your Wyvern if you do not leave!
         */
        val AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN: SystemMessageId

        /**
         * ID: 1688<br></br>
         * Message: You cannot enchant while operating a Private Store or Private Workshop.
         */
        val CANNOT_ENCHANT_WHILE_STORE: SystemMessageId

        /**
         * ID: 1689<br></br>
         * Message: You have already joined the waiting list for a class specific match.
         */
        val YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS: SystemMessageId

        /**
         * ID: 1690<br></br>
         * Message: You have already joined the waiting list for a non-class specific match.
         */
        val YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME: SystemMessageId

        /**
         * ID: 1691<br></br>
         * Message: You can't join a Grand Olympiad Game match with that much stuff on you! Reduce your weight to below 80 percent full and request to join again!
         */
        val SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD: SystemMessageId

        /**
         * ID: 1692<br></br>
         * Message: You have changed from your main class to a subclass and therefore are removed from the Grand Olympiad Games waiting list.
         */
        val SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD: SystemMessageId

        /**
         * ID: 1693<br></br>
         * Message: You may not observe a Grand Olympiad Games match while you are on the waiting list.
         */
        val WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME: SystemMessageId

        /**
         * ID: 1694<br></br>
         * Message: Only a clan leader that is a Noblesse can view the Siege War Status window during a siege war.
         */
        val ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW: SystemMessageId

        /**
         * ID: 1695<br></br>
         * Message: You can only use that during a Siege War!
         */
        val ONLY_DURING_SIEGE: SystemMessageId

        /**
         * ID: 1696<br></br>
         * Message: Your accumulated play time is $s1.
         */
        val ACCUMULATED_PLAY_TIME_IS_S1: SystemMessageId

        /**
         * ID: 1697<br></br>
         * Message: Your accumulated play time has reached Fatigue level, so you will receive experience or item drops at only 50 percent [...]
         */
        val ACCUMULATED_PLAY_TIME_WARNING1: SystemMessageId

        /**
         * ID: 1698<br></br>
         * Message: Your accumulated play time has reached Ill-health level, so you will no longer gain experience or item drops. [...}
         */
        val ACCUMULATED_PLAY_TIME_WARNING2: SystemMessageId

        /**
         * ID: 1699<br></br>
         * Message: You cannot dismiss a party member by force.
         */
        val CANNOT_DISMISS_PARTY_MEMBER: SystemMessageId

        /**
         * ID: 1700<br></br>
         * Message: You don't have enough spiritshots needed for a pet/servitor.
         */
        val NOT_ENOUGH_SPIRITSHOTS_FOR_PET: SystemMessageId

        /**
         * ID: 1701<br></br>
         * Message: You don't have enough soulshots needed for a pet/servitor.
         */
        val NOT_ENOUGH_SOULSHOTS_FOR_PET: SystemMessageId

        /**
         * ID: 1702<br></br>
         * Message: $s1 is using a third party program.
         */
        val S1_USING_THIRD_PARTY_PROGRAM: SystemMessageId

        /**
         * ID: 1703<br></br>
         * Message: The previous investigated user is not using a third party program
         */
        val NOT_USING_THIRD_PARTY_PROGRAM: SystemMessageId

        /**
         * ID: 1704<br></br>
         * Message: Please close the setup window for your private manufacturing store or private store, and try again.
         */
        val CLOSE_STORE_WINDOW_AND_TRY_AGAIN: SystemMessageId

        /**
         * ID: 1705<br></br>
         * Message: PC Bang Points acquisition period. Points acquisition period left $s1 hour.
         */
        val PCPOINT_ACQUISITION_PERIOD: SystemMessageId

        /**
         * ID: 1706<br></br>
         * Message: PC Bang Points use period. Points acquisition period left $s1 hour.
         */
        val PCPOINT_USE_PERIOD: SystemMessageId

        /**
         * ID: 1707<br></br>
         * Message: You acquired $s1 PC Bang Point.
         */
        val ACQUIRED_S1_PCPOINT: SystemMessageId

        /**
         * ID: 1708<br></br>
         * Message: Double points! You acquired $s1 PC Bang Point.
         */
        val ACQUIRED_S1_PCPOINT_DOUBLE: SystemMessageId

        /**
         * ID: 1709<br></br>
         * Message: You are using $s1 point.
         */
        val USING_S1_PCPOINT: SystemMessageId

        /**
         * ID: 1710<br></br>
         * Message: You are short of accumulated points.
         */
        val SHORT_OF_ACCUMULATED_POINTS: SystemMessageId

        /**
         * ID: 1711<br></br>
         * Message: PC Bang Points use period has expired.
         */
        val PCPOINT_USE_PERIOD_EXPIRED: SystemMessageId

        /**
         * ID: 1712<br></br>
         * Message: The PC Bang Points accumulation period has expired.
         */
        val PCPOINT_ACCUMULATION_PERIOD_EXPIRED: SystemMessageId

        /**
         * ID: 1713<br></br>
         * Message: The games may be delayed due to an insufficient number of players waiting.
         */
        val GAMES_DELAYED: SystemMessageId

        /**
         * ID: 1714<br></br>
         * Message: Current Location: $s1, $s2, $s3 (Near the Town of Schuttgart)
         */
        val LOC_SCHUTTGART_S1_S2_S3: SystemMessageId

        /**
         * ID: 1715<br></br>
         * Message: This is a Peaceful Zone
         */
        val PEACEFUL_ZONE: SystemMessageId

        /**
         * ID: 1716<br></br>
         * Message: Altered Zone
         */
        val ALTERED_ZONE: SystemMessageId

        /**
         * ID: 1717<br></br>
         * Message: Siege War Zone
         */
        val SIEGE_ZONE: SystemMessageId

        /**
         * ID: 1718<br></br>
         * Message: General Field
         */
        val GENERAL_ZONE: SystemMessageId

        /**
         * ID: 1719<br></br>
         * Message: Seven Signs Zone
         */
        val SEVENSIGNS_ZONE: SystemMessageId

        /**
         * ID: 1720<br></br>
         * Message: ---
         */
        val UNKNOWN1: SystemMessageId

        /**
         * ID: 1721<br></br>
         * Message: Combat Zone
         */
        val COMBAT_ZONE: SystemMessageId

        /**
         * ID: 1722<br></br>
         * Message: Please enter the name of the item you wish to search for.
         */
        val ENTER_ITEM_NAME_SEARCH: SystemMessageId

        /**
         * ID: 1723<br></br>
         * Message: Please take a moment to provide feedback about the petition service.
         */
        val PLEASE_PROVIDE_PETITION_FEEDBACK: SystemMessageId

        /**
         * ID: 1724<br></br>
         * Message: A servitor whom is engaged in battle cannot be de-activated.
         */
        val SERVITOR_NOT_RETURN_IN_BATTLE: SystemMessageId

        /**
         * ID: 1725<br></br>
         * Message: You have earned $s1 raid point(s).
         */
        val EARNED_S1_RAID_POINTS: SystemMessageId

        /**
         * ID: 1726<br></br>
         * Message: $s1 has disappeared because its time period has expired.
         */
        val S1_PERIOD_EXPIRED_DISAPPEARED: SystemMessageId

        /**
         * ID: 1727<br></br>
         * Message: $s1 has invited you to a party room. Do you accept?
         */
        val S1_INVITED_YOU_TO_PARTY_ROOM_CONFIRM: SystemMessageId

        /**
         * ID: 1728<br></br>
         * Message: The recipient of your invitation did not accept the party matching invitation.
         */
        val PARTY_MATCHING_REQUEST_NO_RESPONSE: SystemMessageId

        /**
         * ID: 1729<br></br>
         * Message: You cannot join a Command Channel while teleporting.
         */
        val NOT_JOIN_CHANNEL_WHILE_TELEPORTING: SystemMessageId

        /**
         * ID: 1730<br></br>
         * Message: To establish a Clan Academy, your clan must be Level 5 or higher.
         */
        val YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY: SystemMessageId

        /**
         * ID: 1731<br></br>
         * Message: Only the leader can create a Clan Academy.
         */
        val ONLY_LEADER_CAN_CREATE_ACADEMY: SystemMessageId

        /**
         * ID: 1732<br></br>
         * Message: To create a Clan Academy, a Blood Mark is needed.
         */
        val NEED_BLOODMARK_FOR_ACADEMY: SystemMessageId

        /**
         * ID: 1733<br></br>
         * Message: You do not have enough adena to create a Clan Academy.
         */
        val NEED_ADENA_FOR_ACADEMY: SystemMessageId

        /**
         * ID: 1734<br></br>
         * not belong another clan and not yet completed their 2nd class transfer.
         */
        val ACADEMY_REQUIREMENTS: SystemMessageId

        /**
         * ID: 1735<br></br>
         * Message: $s1 does not meet the requirements to join a Clan Academy.
         */
        val S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY: SystemMessageId

        /**
         * ID: 1736<br></br>
         * Message: The Clan Academy has reached its maximum enrollment.
         */
        val ACADEMY_MAXIMUM: SystemMessageId

        /**
         * ID: 1737<br></br>
         * Message: Your clan has not established a Clan Academy but is eligible to do so.
         */
        val CLAN_CAN_CREATE_ACADEMY: SystemMessageId

        /**
         * ID: 1738<br></br>
         * Message: Your clan has already established a Clan Academy.
         */
        val CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY: SystemMessageId

        /**
         * ID: 1739<br></br>
         * Message: Would you like to create a Clan Academy?
         */
        val CLAN_ACADEMY_CREATE_CONFIRM: SystemMessageId

        /**
         * ID: 1740<br></br>
         * Message: Please enter the name of the Clan Academy.
         */
        val ACADEMY_CREATE_ENTER_NAME: SystemMessageId

        /**
         * ID: 1741<br></br>
         * Message: Congratulations! The $s1's Clan Academy has been created.
         */
        val THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED: SystemMessageId

        /**
         * ID: 1742<br></br>
         * Message: A message inviting $s1 to join the Clan Academy is being sent.
         */
        val ACADEMY_INVITATION_SENT_TO_S1: SystemMessageId

        /**
         * ID: 1743<br></br>
         * Message: To open a Clan Academy, the leader of a Level 5 clan or above must pay XX Proofs of Blood or a certain amount of adena.
         */
        val OPEN_ACADEMY_CONDITIONS: SystemMessageId

        /**
         * ID: 1744<br></br>
         * Message: There was no response to your invitation to join the Clan Academy, so the invitation has been rescinded.
         */
        val ACADEMY_JOIN_NO_RESPONSE: SystemMessageId

        /**
         * ID: 1745<br></br>
         * Message: The recipient of your invitation to join the Clan Academy has declined.
         */
        val ACADEMY_JOIN_DECLINE: SystemMessageId

        /**
         * ID: 1746<br></br>
         * Message: You have already joined a Clan Academy.
         */
        val ALREADY_JOINED_ACADEMY: SystemMessageId

        /**
         * ID: 1747<br></br>
         * Message: $s1 has sent you an invitation to join the Clan Academy belonging to the $s2 clan. Do you accept?
         */
        val JOIN_ACADEMY_REQUEST_BY_S1_FOR_CLAN_S2: SystemMessageId

        /**
         * ID: 1748<br></br>
         * Message: Clan Academy member $s1 has successfully completed the 2nd class transfer and obtained $s2 Clan Reputation points.
         */
        val CLAN_MEMBER_GRADUATED_FROM_ACADEMY: SystemMessageId

        /**
         * ID: 1749<br></br>
         * Message: Congratulations! You will now graduate from the Clan Academy and leave your current clan. As a graduate of the academy, you can immediately join a clan as a regular member without being subject to any penalties.
         */
        val ACADEMY_MEMBERSHIP_TERMINATED: SystemMessageId

        /**
         * ID: 1750<br></br>
         * Message: If you possess $s1, you cannot participate in the Olympiad.
         */
        val CANNOT_JOIN_OLYMPIAD_POSSESSING_S1: SystemMessageId

        /**
         * ID: 1751<br></br>
         * Message: The Grand Master has given you a commemorative item.
         */
        val GRAND_MASTER_COMMEMORATIVE_ITEM: SystemMessageId

        /**
         * ID: 1752<br></br>
         * Message: Since the clan has received a graduate of the Clan Academy, it has earned $s1 points towards its reputation score.
         */
        val MEMBER_GRADUATED_EARNED_S1_REPU: SystemMessageId

        /**
         * ID: 1753<br></br>
         * Message: The clan leader has decreed that that particular privilege cannot be granted to a Clan Academy member.
         */
        val CANT_TRANSFER_PRIVILEGE_TO_ACADEMY_MEMBER: SystemMessageId

        /**
         * ID: 1754<br></br>
         * Message: That privilege cannot be granted to a Clan Academy member.
         */
        val RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER: SystemMessageId

        /**
         * ID: 1755<br></br>
         * Message: $s2 has been designated as the apprentice of clan member $s1.
         */
        val S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1: SystemMessageId

        /**
         * ID: 1756<br></br>
         * Message: Your apprentice, $s1, has logged in.
         */
        val YOUR_APPRENTICE_S1_HAS_LOGGED_IN: SystemMessageId

        /**
         * ID: 1757<br></br>
         * Message: Your apprentice, $s1, has logged out.
         */
        val YOUR_APPRENTICE_S1_HAS_LOGGED_OUT: SystemMessageId

        /**
         * ID: 1758<br></br>
         * Message: Your sponsor, $s1, has logged in.
         */
        val YOUR_SPONSOR_S1_HAS_LOGGED_IN: SystemMessageId

        /**
         * ID: 1759<br></br>
         * Message: Your sponsor, $s1, has logged out.
         */
        val YOUR_SPONSOR_S1_HAS_LOGGED_OUT: SystemMessageId

        /**
         * ID: 1760<br></br>
         * Message: Clan member $s1's name title has been changed to $2.
         */
        val CLAN_MEMBER_S1_TITLE_CHANGED_TO_S2: SystemMessageId

        /**
         * ID: 1761<br></br>
         * Message: Clan member $s1's privilege level has been changed to $s2.
         */
        val CLAN_MEMBER_S1_PRIVILEGE_CHANGED_TO_S2: SystemMessageId

        /**
         * ID: 1762<br></br>
         * Message: You do not have the right to dismiss an apprentice.
         */
        val YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE: SystemMessageId

        /**
         * ID: 1763<br></br>
         * Message: $s2, clan member $s1's apprentice, has been removed.
         */
        val S2_CLAN_MEMBER_S1_APPRENTICE_HAS_BEEN_REMOVED: SystemMessageId

        /**
         * ID: 1764<br></br>
         * Message: This item can only be worn by a member of the Clan Academy.
         */
        val EQUIP_ONLY_FOR_ACADEMY: SystemMessageId

        /**
         * ID: 1765<br></br>
         * Message: As a graduate of the Clan Academy, you can no longer wear this item.
         */
        val EQUIP_NOT_FOR_GRADUATES: SystemMessageId

        /**
         * ID: 1766<br></br>
         * Message: An application to join the clan has been sent to $s1 in $s2.
         */
        val CLAN_JOIN_APPLICATION_SENT_TO_S1_IN_S2: SystemMessageId

        /**
         * ID: 1767<br></br>
         * Message: An application to join the clan Academy has been sent to $s1.
         */
        val ACADEMY_JOIN_APPLICATION_SENT_TO_S1: SystemMessageId

        /**
         * ID: 1768<br></br>
         * Message: $s1 has invited you to join the Clan Academy of $s2 clan. Would you like to join?
         */
        val JOIN_REQUEST_BY_S1_TO_CLAN_S2_ACADEMY: SystemMessageId

        /**
         * ID: 1769<br></br>
         * Message: $s1 has sent you an invitation to join the $s3 Order of Knights under the $s2 clan. Would you like to join?
         */
        val JOIN_REQUEST_BY_S1_TO_ORDER_OF_KNIGHTS_S3_UNDER_CLAN_S2: SystemMessageId

        /**
         * ID: 1770<br></br>
         * Message: The clan's reputation score has dropped below 0. The clan may face certain penalties as a result.
         */
        val CLAN_REPU_0_MAY_FACE_PENALTIES: SystemMessageId

        /**
         * ID: 1771<br></br>
         * Message: Now that your clan level is above Level 5, it can accumulate clan reputation points.
         */
        val CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS: SystemMessageId

        /**
         * ID: 1772<br></br>
         * Message: Since your clan was defeated in a siege, $s1 points have been deducted from your clan's reputation score and given to the opposing clan.
         */
        val CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS: SystemMessageId

        /**
         * ID: 1773<br></br>
         * Message: Since your clan emerged victorious from the siege, $s1 points have been added to your clan's reputation score.
         */
        val CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS: SystemMessageId

        /**
         * ID: 1774<br></br>
         * Message: Your clan's newly acquired contested clan hall has added $s1 points to your clan's reputation score.
         */
        val CLAN_ACQUIRED_CONTESTED_CLAN_HALL_AND_S1_REPUTATION_POINTS: SystemMessageId

        /**
         * ID: 1775<br></br>
         * Message: Clan member $s1 was an active member of the highest-ranked party in the Festival of Darkness. $s2 points have been added to your clan's reputation score.
         */
        val CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION: SystemMessageId

        /**
         * ID: 1776<br></br>
         * Message: Clan member $s1 was named a hero. $2s points have been added to your clan's reputation score.
         */
        val CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS: SystemMessageId

        /**
         * ID: 1777<br></br>
         * Message: You have successfully completed a clan quest. $s1 points have been added to your clan's reputation score.
         */
        val CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED: SystemMessageId

        /**
         * ID: 1778<br></br>
         * Message: An opposing clan has captured your clan's contested clan hall. $s1 points have been deducted from your clan's reputation score.
         */
        val OPPOSING_CLAN_CAPTURED_CLAN_HALL_AND_YOUR_CLAN_LOSES_S1_POINTS: SystemMessageId

        /**
         * ID: 1779<br></br>
         * Message: After losing the contested clan hall, 300 points have been deducted from your clan's reputation score.
         */
        val CLAN_LOST_CONTESTED_CLAN_HALL_AND_300_POINTS: SystemMessageId

        /**
         * ID: 1780<br></br>
         * Message: Your clan has captured your opponent's contested clan hall. $s1 points have been deducted from your opponent's clan reputation score.
         */
        val CLAN_CAPTURED_CONTESTED_CLAN_HALL_AND_S1_POINTS_DEDUCTED_FROM_OPPONENT: SystemMessageId

        /**
         * ID: 1781<br></br>
         * Message: Your clan has added $1s points to its clan reputation score.
         */
        val CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE: SystemMessageId

        /**
         * ID: 1782<br></br>
         * Message: Your clan member $s1 was killed. $s2 points have been deducted from your clan's reputation score and added to your opponent's clan reputation score.
         */
        val CLAN_MEMBER_S1_WAS_KILLED_AND_S2_POINTS_DEDUCTED_FROM_REPUTATION: SystemMessageId

        /**
         * ID: 1783<br></br>
         * Message: For killing an opposing clan member, $s1 points have been deducted from your opponents' clan reputation score.
         */
        val FOR_KILLING_OPPOSING_MEMBER_S1_POINTS_WERE_DEDUCTED_FROM_OPPONENTS: SystemMessageId

        /**
         * ID: 1784<br></br>
         * Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score and added to your opponents'.
         */
        val YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST_AND_ADDED_TO_OPPONENT: SystemMessageId

        /**
         * ID: 1785<br></br>
         * Message: The clan you belong to has been initialized. $s1 points have been deducted from your clan reputation score.
         */
        val YOUR_CLAN_HAS_BEEN_INITIALIZED_AND_S1_POINTS_LOST: SystemMessageId

        /**
         * ID: 1786<br></br>
         * Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score.
         */
        val YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST: SystemMessageId

        /**
         * ID: 1787<br></br>
         * Message: $s1 points have been deducted from the clan's reputation score.
         */
        val S1_DEDUCTED_FROM_CLAN_REP: SystemMessageId

        /**
         * ID: 1788<br></br>
         * Message: The clan skill $s1 has been added.
         */
        val CLAN_SKILL_S1_ADDED: SystemMessageId

        /**
         * ID: 1789<br></br>
         * Message: Since the Clan Reputation Score has dropped to 0 or lower, your clan skill(s) will be de-activated.
         */
        val REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED: SystemMessageId

        /**
         * ID: 1790<br></br>
         * Message: The conditions necessary to increase the clan's level have not been met.
         */
        val FAILED_TO_INCREASE_CLAN_LEVEL: SystemMessageId

        /**
         * ID: 1791<br></br>
         * Message: The conditions necessary to create a military unit have not been met.
         */
        val YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT: SystemMessageId

        /**
         * ID: 1792<br></br>
         * Message: Please assign a manager for your new Order of Knights.
         */
        val ASSIGN_MANAGER_FOR_ORDER_OF_KNIGHTS: SystemMessageId

        /**
         * ID: 1793<br></br>
         * Message: $s1 has been selected as the captain of $s2.
         */
        val S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2: SystemMessageId

        /**
         * ID: 1794<br></br>
         * Message: The Knights of $s1 have been created.
         */
        val THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED: SystemMessageId

        /**
         * ID: 1795<br></br>
         * Message: The Royal Guard of $s1 have been created.
         */
        val THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED: SystemMessageId

        /**
         * ID: 1796<br></br>
         * Message: Your account has been suspended ...
         */
        val ILLEGAL_USE17: SystemMessageId

        /**
         * ID: 1797<br></br>
         * Message: $s1 has been promoted to $s2.
         */
        val S1_PROMOTED_TO_S2: SystemMessageId

        /**
         * ID: 1798<br></br>
         * Message: Clan lord privileges have been transferred to $s1.
         */
        val CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1: SystemMessageId

        /**
         * ID: 1799<br></br>
         * Message: We are searching for BOT users. Please try again later.
         */
        val SEARCHING_FOR_BOT_USERS_TRY_AGAIN_LATER: SystemMessageId

        /**
         * ID: 1800<br></br>
         * Message: User $s1 has a history of using BOT.
         */
        val S1_HISTORY_USING_BOT: SystemMessageId

        /**
         * ID: 1801<br></br>
         * Message: The attempt to sell has failed.
         */
        val SELL_ATTEMPT_FAILED: SystemMessageId

        /**
         * ID: 1802<br></br>
         * Message: The attempt to trade has failed.
         */
        val TRADE_ATTEMPT_FAILED: SystemMessageId

        /**
         * ID: 1803<br></br>
         * Message: The request to participate in the game cannot be made starting from 10 minutes before the end of the game.
         */
        val GAME_REQUEST_CANNOT_BE_MADE: SystemMessageId

        /**
         * ID: 1804<br></br>
         * Message: Your account has been suspended ...
         */
        val ILLEGAL_USE18: SystemMessageId

        /**
         * ID: 1805<br></br>
         * Message: Your account has been suspended ...
         */
        val ILLEGAL_USE19: SystemMessageId

        /**
         * ID: 1806<br></br>
         * Message: Your account has been suspended ...
         */
        val ILLEGAL_USE20: SystemMessageId

        /**
         * ID: 1807<br></br>
         * Message: Your account has been suspended ...
         */
        val ILLEGAL_USE21: SystemMessageId

        /**
         * ID: 1808<br></br>
         * Message: Your account has been suspended ...
         */
        val ILLEGAL_USE22: SystemMessageId

        /**
         * ID: 1809<br></br>
         * please visit the PlayNC website (http://www.plaync.com/us/support/)
         */
        val ACCOUNT_MUST_VERIFIED: SystemMessageId

        /**
         * ID: 1810<br></br>
         * Message: The refuse invitation state has been activated.
         */
        val REFUSE_INVITATION_ACTIVATED: SystemMessageId

        /**
         * ID: 1812<br></br>
         * Message: Since the refuse invitation state is currently activated, no invitation can be made
         */
        val REFUSE_INVITATION_CURRENTLY_ACTIVE: SystemMessageId

        /**
         * ID: 1813<br></br>
         * Message: $s1 has $s2 hour(s) of usage time remaining.
         */
        val S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1: SystemMessageId

        /**
         * ID: 1814<br></br>
         * Message: $s1 has $s2 minute(s) of usage time remaining.
         */
        val S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1: SystemMessageId

        /**
         * ID: 1815<br></br>
         * Message: $s2 was dropped in the $s1 region.
         */
        val S2_WAS_DROPPED_IN_THE_S1_REGION: SystemMessageId

        /**
         * ID: 1816<br></br>
         * Message: The owner of $s2 has appeared in the $s1 region.
         */
        val THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION: SystemMessageId

        /**
         * ID: 1817<br></br>
         * Message: $s2's owner has logged into the $s1 region.
         */
        val S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION: SystemMessageId

        /**
         * ID: 1818<br></br>
         * Message: $s1 has disappeared.
         */
        val S1_HAS_DISAPPEARED: SystemMessageId

        /**
         * ID: 1819<br></br>
         * Message: An evil is pulsating from $s2 in $s1.
         */
        val EVIL_FROM_S2_IN_S1: SystemMessageId

        /**
         * ID: 1820<br></br>
         * Message: $s1 is currently asleep.
         */
        val S1_CURRENTLY_SLEEP: SystemMessageId

        /**
         * ID: 1821<br></br>
         * Message: $s2's evil presence is felt in $s1.
         */
        val S2_EVIL_PRESENCE_FELT_IN_S1: SystemMessageId

        /**
         * ID: 1822<br></br>
         * Message: $s1 has been sealed.
         */
        val S1_SEALED: SystemMessageId

        /**
         * ID: 1823<br></br>
         * Message: The registration period for a clan hall war has ended.
         */
        val CLANHALL_WAR_REGISTRATION_PERIOD_ENDED: SystemMessageId

        /**
         * ID: 1824<br></br>
         * Message: You have been registered for a clan hall war. Please move to the left side of the clan hall's arena and get ready.
         */
        val REGISTERED_FOR_CLANHALL_WAR: SystemMessageId

        /**
         * ID: 1825<br></br>
         * Message: You have failed in your attempt to register for the clan hall war. Please try again.
         */
        val CLANHALL_WAR_REGISTRATION_FAILED: SystemMessageId

        /**
         * ID: 1826<br></br>
         * Message: In $s1 minute(s), the game will begin. All players must hurry and move to the left side of the clan hall's arena.
         */
        val CLANHALL_WAR_BEGINS_IN_S1_MINUTES: SystemMessageId

        /**
         * ID: 1827<br></br>
         * Message: In $s1 minute(s), the game will begin. All players must, please enter the arena now
         */
        val CLANHALL_WAR_BEGINS_IN_S1_MINUTES_ENTER_NOW: SystemMessageId

        /**
         * ID: 1828<br></br>
         * Message: In $s1 seconds(s), the game will begin.
         */
        val CLANHALL_WAR_BEGINS_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 1829<br></br>
         * Message: The Command Channel is full.
         */
        val COMMAND_CHANNEL_FULL: SystemMessageId

        /**
         * ID: 1830<br></br>
         * Message: $s1 is not allowed to use the party room invite command. Please update the waiting list.
         */
        val S1_NOT_ALLOWED_INVITE_TO_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1831<br></br>
         * Message: $s1 does not meet the conditions of the party room. Please update the waiting list.
         */
        val S1_NOT_MEET_CONDITIONS_FOR_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1832<br></br>
         * Message: Only a room leader may invite others to a party room.
         */
        val ONLY_ROOM_LEADER_CAN_INVITE: SystemMessageId

        /**
         * ID: 1833<br></br>
         * Message: All of $s1 will be dropped. Would you like to continue?
         */
        val CONFIRM_DROP_ALL_OF_S1: SystemMessageId

        /**
         * ID: 1834<br></br>
         * Message: The party room is full. No more characters can be invitet in
         */
        val PARTY_ROOM_FULL: SystemMessageId

        /**
         * ID: 1835<br></br>
         * Message: $s1 is full and cannot accept additional clan members at this time.
         */
        val S1_CLAN_IS_FULL: SystemMessageId

        /**
         * ID: 1836<br></br>
         * Message: You cannot join a Clan Academy because you have successfully completed your 2nd class transfer.
         */
        val CANNOT_JOIN_ACADEMY_AFTER_2ND_OCCUPATION: SystemMessageId

        /**
         * ID: 1837<br></br>
         * Message: $s1 has sent you an invitation to join the $s3 Royal Guard under the $s2 clan. Would you like to join?
         */
        val S1_SENT_INVITATION_TO_ROYAL_GUARD_S3_OF_CLAN_S2: SystemMessageId

        /**
         * ID: 1838<br></br>
         * Message: 1. The coupon an be used once per character.
         */
        val COUPON_ONCE_PER_CHARACTER: SystemMessageId

        /**
         * ID: 1839<br></br>
         * Message: 2. A used serial number may not be used again.
         */
        val SERIAL_MAY_USED_ONCE: SystemMessageId

        /**
         * ID: 1840<br></br>
         * Message: 3. If you enter the incorrect serial number more than 5 times, ...
         */
        val SERIAL_INPUT_INCORRECT: SystemMessageId

        /**
         * ID: 1841<br></br>
         * Message: The clan hall war has been cancelled. Not enough clans have registered.
         */
        val CLANHALL_WAR_CANCELLED: SystemMessageId

        /**
         * ID: 1842<br></br>
         * Message: $s1 wishes to summon you from $s2. Do you accept?
         */
        val S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT: SystemMessageId

        /**
         * ID: 1843<br></br>
         * Message: $s1 is engaged in combat and cannot be summoned.
         */
        val S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED: SystemMessageId

        /**
         * ID: 1844<br></br>
         * Message: $s1 is dead at the moment and cannot be summoned.
         */
        val S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED: SystemMessageId

        /**
         * ID: 1845<br></br>
         * Message: Hero weapons cannot be destroyed.
         */
        val HERO_WEAPONS_CANT_DESTROYED: SystemMessageId

        /**
         * ID: 1846<br></br>
         * Message: You are too far away from the Strider to mount it.
         */
        val TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT: SystemMessageId

        /**
         * ID: 1847<br></br>
         * Message: You caught a fish $s1 in length.
         */
        val CAUGHT_FISH_S1_LENGTH: SystemMessageId

        /**
         * ID: 1848<br></br>
         * Message: Because of the size of fish caught, you will be registered in the ranking
         */
        val REGISTERED_IN_FISH_SIZE_RANKING: SystemMessageId

        /**
         * ID: 1849<br></br>
         * Message: All of $s1 will be discarded. Would you like to continue?
         */
        val CONFIRM_DISCARD_ALL_OF_S1: SystemMessageId

        /**
         * ID: 1850<br></br>
         * Message: The Captain of the Order of Knights cannot be appointed.
         */
        val CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED: SystemMessageId

        /**
         * ID: 1851<br></br>
         * Message: The Captain of the Royal Guard cannot be appointed.
         */
        val CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED: SystemMessageId

        /**
         * ID: 1852<br></br>
         * Message: The attempt to acquire the skill has failed because of an insufficient Clan Reputation Score.
         */
        val ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE: SystemMessageId

        /**
         * ID: 1853<br></br>
         * Message: Quantity items of the same type cannot be exchanged at the same time
         */
        val CANT_EXCHANGE_QUANTITY_ITEMS_OF_SAME_TYPE: SystemMessageId

        /**
         * ID: 1854<br></br>
         * Message: The item was converted successfully.
         */
        val ITEM_CONVERTED_SUCCESSFULLY: SystemMessageId

        /**
         * ID: 1855<br></br>
         * Message: Another military unit is already using that name. Please enter a different name.
         */
        val ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME: SystemMessageId

        /**
         * ID: 1856<br></br>
         * Message: Since your opponent is now the owner of $s1, the Olympiad has been cancelled.
         */
        val OPPONENT_POSSESSES_S1_OLYMPIAD_CANCELLED: SystemMessageId

        /**
         * ID: 1857<br></br>
         * Message: $s1 is the owner of $s2 and cannot participate in the Olympiad.
         */
        val S1_OWNS_S2_AND_CANNOT_PARTICIPATE_IN_OLYMPIAD: SystemMessageId

        /**
         * ID: 1858<br></br>
         * Message: You cannot participate in the Olympiad while dead.
         */
        val CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD: SystemMessageId

        /**
         * ID: 1859<br></br>
         * Message: You exceeded the quantity that can be moved at one time.
         */
        val EXCEEDED_QUANTITY_FOR_MOVED: SystemMessageId

        /**
         * ID: 1860<br></br>
         * Message: The Clan Reputation Score is too low.
         */
        val THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW: SystemMessageId

        /**
         * ID: 1861<br></br>
         * Message: The clan's crest has been deleted.
         */
        val CLAN_CREST_HAS_BEEN_DELETED: SystemMessageId

        /**
         * ID: 1862<br></br>
         * Message: Clan skills will now be activated since the clan's reputation score is 0 or higher.
         */
        val CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER: SystemMessageId

        /**
         * ID: 1863<br></br>
         * Message: $s1 purchased a clan item, reducing the Clan Reputation by $s2 points.
         */
        val S1_PURCHASED_CLAN_ITEM_REDUCING_S2_REPU_POINTS: SystemMessageId

        /**
         * ID: 1864<br></br>
         * Message: Your pet/servitor is unresponsive and will not obey any orders.
         */
        val PET_REFUSING_ORDER: SystemMessageId

        /**
         * ID: 1865<br></br>
         * Message: Your pet/servitor is currently in a state of distress.
         */
        val PET_IN_STATE_OF_DISTRESS: SystemMessageId

        /**
         * ID: 1866<br></br>
         * Message: MP was reduced by $s1.
         */
        val MP_REDUCED_BY_S1: SystemMessageId

        /**
         * ID: 1867<br></br>
         * Message: Your opponent's MP was reduced by $s1.
         */
        val YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1: SystemMessageId

        /**
         * ID: 1868<br></br>
         * Message: You cannot exchange an item while it is being used.
         */
        val CANNOT_EXCHANCE_USED_ITEM: SystemMessageId

        /**
         * ID: 1869<br></br>
         * Message: $s1 has granted the Command Channel's master party the privilege of item looting.
         */
        val S1_GRANTED_MASTER_PARTY_LOOTING_RIGHTS: SystemMessageId

        /**
         * ID: 1870<br></br>
         * Message: A Command Channel with looting rights already exists.
         */
        val COMMAND_CHANNEL_WITH_LOOTING_RIGHTS_EXISTS: SystemMessageId

        /**
         * ID: 1871<br></br>
         * Message: Do you want to dismiss $s1 from the clan?
         */
        val CONFIRM_DISMISS_S1_FROM_CLAN: SystemMessageId

        /**
         * ID: 1872<br></br>
         * Message: You have $s1 hour(s) and $s2 minute(s) left.
         */
        val S1_HOURS_S2_MINUTES_LEFT: SystemMessageId

        /**
         * ID: 1873<br></br>
         * Message: There are $s1 hour(s) and $s2 minute(s) left in the fixed use time for this PC Cafe.
         */
        val S1_HOURS_S2_MINUTES_LEFT_FOR_THIS_PCCAFE: SystemMessageId

        /**
         * ID: 1874<br></br>
         * Message: There are $s1 minute(s) left for this individual user.
         */
        val S1_MINUTES_LEFT_FOR_THIS_USER: SystemMessageId

        /**
         * ID: 1875<br></br>
         * Message: There are $s1 minute(s) left in the fixed use time for this PC Cafe.
         */
        val S1_MINUTES_LEFT_FOR_THIS_PCCAFE: SystemMessageId

        /**
         * ID: 1876<br></br>
         * Message: Do you want to leave $s1 clan?
         */
        val CONFIRM_LEAVE_S1_CLAN: SystemMessageId

        /**
         * ID: 1877<br></br>
         * Message: The game will end in $s1 minutes.
         */
        val GAME_WILL_END_IN_S1_MINUTES: SystemMessageId

        /**
         * ID: 1878<br></br>
         * Message: The game will end in $s1 seconds.
         */
        val GAME_WILL_END_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 1879<br></br>
         * Message: In $s1 minute(s), you will be teleported outside of the game arena.
         */
        val IN_S1_MINUTES_TELEPORTED_OUTSIDE_OF_GAME_ARENA: SystemMessageId

        /**
         * ID: 1880<br></br>
         * Message: In $s1 seconds(s), you will be teleported outside of the game arena.
         */
        val IN_S1_SECONDS_TELEPORTED_OUTSIDE_OF_GAME_ARENA: SystemMessageId

        /**
         * ID: 1881<br></br>
         * Message: The preliminary match will begin in $s1 second(s). Prepare yourself.
         */
        val PRELIMINARY_MATCH_BEGIN_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 1882<br></br>
         * Message: Characters cannot be created from this server.
         */
        val CHARACTERS_NOT_CREATED_FROM_THIS_SERVER: SystemMessageId

        /**
         * ID: 1883<br></br>
         * Message: There are no offerings I own or I made a bid for.
         */
        val NO_OFFERINGS_OWN_OR_MADE_BID_FOR: SystemMessageId

        /**
         * ID: 1884<br></br>
         * Message: Enter the PC Room coupon serial number.
         */
        val ENTER_PCROOM_SERIAL_NUMBER: SystemMessageId

        /**
         * ID: 1885<br></br>
         * Message: This serial number cannot be entered. Please try again in minute(s).
         */
        val SERIAL_NUMBER_CANT_ENTERED: SystemMessageId

        /**
         * ID: 1886<br></br>
         * Message: This serial has already been used.
         */
        val SERIAL_NUMBER_ALREADY_USED: SystemMessageId

        /**
         * ID: 1887<br></br>
         * Message: Invalid serial number. Your attempt to enter the number has failed time(s). You will be allowed to make more attempt(s).
         */
        val SERIAL_NUMBER_ENTERING_FAILED: SystemMessageId

        /**
         * ID: 1888<br></br>
         * Message: Invalid serial number. Your attempt to enter the number has failed 5 time(s). Please try again in 4 hours.
         */
        val SERIAL_NUMBER_ENTERING_FAILED_5_TIMES: SystemMessageId

        /**
         * ID: 1889<br></br>
         * Message: Congratulations! You have received $s1.
         */
        val CONGRATULATIONS_RECEIVED_S1: SystemMessageId

        /**
         * ID: 1890<br></br>
         * Message: Since you have already used this coupon, you may not use this serial number.
         */
        val ALREADY_USED_COUPON_NOT_USE_SERIAL_NUMBER: SystemMessageId

        /**
         * ID: 1891<br></br>
         * Message: You may not use items in a private store or private work shop.
         */
        val NOT_USE_ITEMS_IN_PRIVATE_STORE: SystemMessageId

        /**
         * ID: 1892<br></br>
         * Message: The replay file for the previous version cannot be played.
         */
        val REPLAY_FILE_PREVIOUS_VERSION_CANT_PLAYED: SystemMessageId

        /**
         * ID: 1893<br></br>
         * Message: This file cannot be replayed.
         */
        val FILE_CANT_REPLAYED: SystemMessageId

        /**
         * ID: 1894<br></br>
         * Message: A sub-class cannot be created or changed while you are over your weight limit.
         */
        val NOT_SUBCLASS_WHILE_OVERWEIGHT: SystemMessageId

        /**
         * ID: 1895<br></br>
         * Message: $s1 is in an area which blocks summoning.
         */
        val S1_IN_SUMMON_BLOCKING_AREA: SystemMessageId

        /**
         * ID: 1896<br></br>
         * Message: $s1 has already been summoned.
         */
        val S1_ALREADY_SUMMONED: SystemMessageId

        /**
         * ID: 1897<br></br>
         * Message: $s1 is required for summoning.
         */
        val S1_REQUIRED_FOR_SUMMONING: SystemMessageId

        /**
         * ID: 1898<br></br>
         * Message: $s1 is currently trading or operating a private store and cannot be summoned.
         */
        val S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED: SystemMessageId

        /**
         * ID: 1899<br></br>
         * Message: Your target is in an area which blocks summoning.
         */
        val YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING: SystemMessageId

        /**
         * ID: 1900<br></br>
         * Message: $s1 has entered the party room.
         */
        val S1_ENTERED_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1901<br></br>
         * Message: $s1 has invited you to enter the party room.
         */
        val S1_INVITED_YOU_TO_PARTY_ROOM: SystemMessageId

        /**
         * ID: 1902<br></br>
         * Message: Incompatible item grade. This item cannot be used.
         */
        val INCOMPATIBLE_ITEM_GRADE: SystemMessageId

        /**
         * ID: 1903<br></br>
         * Message: Those of you who have requested NCOTP should run NCOTP by using your cell phone [...]
         */
        val NCOTP: SystemMessageId

        /**
         * ID: 1904<br></br>
         * Message: A sub-class may not be created or changed while a servitor or pet is summoned.
         */
        val CANT_SUBCLASS_WITH_SUMMONED_SERVITOR: SystemMessageId

        /**
         * ID: 1905<br></br>
         * Message: $s2 of $s1 will be replaced with $s4 of $s3.
         */
        val S2_OF_S1_WILL_REPLACED_WITH_S4_OF_S3: SystemMessageId

        /**
         * ID: 1906<br></br>
         * Message: Select the combat unit
         */
        val SELECT_COMBAT_UNIT: SystemMessageId

        /**
         * ID: 1907<br></br>
         * Message: Select the character who will [...]
         */
        val SELECT_CHARACTER_WHO_WILL: SystemMessageId

        /**
         * ID: 1908<br></br>
         * Message: $s1 in a state which prevents summoning.
         */
        val S1_STATE_FORBIDS_SUMMONING: SystemMessageId

        /**
         * ID: 1909<br></br>
         * Message: ==< List of Academy Graduates During the Past Week >==
         */
        val ACADEMY_LIST_HEADER: SystemMessageId

        /**
         * ID: 1910<br></br>
         * Message: Graduates: $s1.
         */
        val GRADUATES_S1: SystemMessageId

        /**
         * ID: 1911<br></br>
         * Message: You cannot summon players who are currently participating in the Grand Olympiad.
         */
        val YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD: SystemMessageId

        /**
         * ID: 1912<br></br>
         * Message: Only those requesting NCOTP should make an entry into this field.
         */
        val NCOTP2: SystemMessageId

        /**
         * ID: 1913<br></br>
         * Message: The remaining recycle time for $s1 is $s2 minute(s).
         */
        val TIME_FOR_S1_IS_S2_MINUTES_REMAINING: SystemMessageId

        /**
         * ID: 1914<br></br>
         * Message: The remaining recycle time for $s1 is $s2 seconds(s).
         */
        val TIME_FOR_S1_IS_S2_SECONDS_REMAINING: SystemMessageId

        /**
         * ID: 1915<br></br>
         * Message: The game will end in $s1 second(s).
         */
        val GAME_ENDS_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 1916<br></br>
         * Message: Your Death Penalty is now level $s1.
         */
        val DEATH_PENALTY_LEVEL_S1_ADDED: SystemMessageId

        /**
         * ID: 1917<br></br>
         * Message: Your Death Penalty has been lifted.
         */
        val DEATH_PENALTY_LIFTED: SystemMessageId

        /**
         * ID: 1918<br></br>
         * Message: Your pet is too high level to control.
         */
        val PET_TOO_HIGH_TO_CONTROL: SystemMessageId

        /**
         * ID: 1919<br></br>
         * Message: The Grand Olympiad registration period has ended.
         */
        val OLYMPIAD_REGISTRATION_PERIOD_ENDED: SystemMessageId

        /**
         * ID: 1920<br></br>
         * Message: Your account is currently inactive because you have not logged into the game for some time. You may reactivate your account by visiting the PlayNC website (http://www.plaync.com/us/support/).
         */
        val ACCOUNT_INACTIVITY: SystemMessageId

        /**
         * ID: 1921<br></br>
         * Message: $s2 hour(s) and $s3 minute(s) have passed since $s1 has killed.
         */
        val S2_HOURS_S3_MINUTES_SINCE_S1_KILLED: SystemMessageId

        /**
         * ID: 1922<br></br>
         * Message: Because $s1 has failed to kill for one full day, it has expired.
         */
        val S1_FAILED_KILLING_EXPIRED: SystemMessageId

        /**
         * ID: 1923<br></br>
         * Message: Court Magician: The portal has been created!
         */
        val COURT_MAGICIAN_CREATED_PORTAL: SystemMessageId

        /**
         * ID: 1924<br></br>
         * Message: Current Location: $s1, $s2, $s3 (Near the Primeval Isle)
         */
        val LOC_PRIMEVAL_ISLE_S1_S2_S3: SystemMessageId

        /**
         * ID: 1925<br></br>
         * Message: Due to the affects of the Seal of Strife, it is not possible to summon at this time.
         */
        val SEAL_OF_STRIFE_FORBIDS_SUMMONING: SystemMessageId

        /**
         * ID: 1926<br></br>
         * Message: There is no opponent to receive your challenge for a duel.
         */
        val THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL: SystemMessageId

        /**
         * ID: 1927<br></br>
         * Message: $s1 has been challenged to a duel.
         */
        val S1_HAS_BEEN_CHALLENGED_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1928<br></br>
         * Message: $s1's party has been challenged to a duel.
         */
        val S1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1929<br></br>
         * Message: $s1 has accepted your challenge to a duel. The duel will begin in a few moments.
         */
        val S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS: SystemMessageId

        /**
         * ID: 1930<br></br>
         * Message: You have accepted $s1's challenge to a duel. The duel will begin in a few moments.
         */
        val YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS: SystemMessageId

        /**
         * ID: 1931<br></br>
         * Message: $s1 has declined your challenge to a duel.
         */
        val S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1932<br></br>
         * Message: $s1 has declined your challenge to a duel.
         */
        val S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL2: SystemMessageId

        /**
         * ID: 1933<br></br>
         * Message: You have accepted $s1's challenge to a party duel. The duel will begin in a few moments.
         */
        val YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS: SystemMessageId

        /**
         * ID: 1934<br></br>
         * Message: $s1 has accepted your challenge to duel against their party. The duel will begin in a few moments.
         */
        val S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS: SystemMessageId

        /**
         * ID: 1935<br></br>
         * Message: $s1 has declined your challenge to a party duel.
         */
        val S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL: SystemMessageId

        /**
         * ID: 1936<br></br>
         * Message: The opposing party has declined your challenge to a duel.
         */
        val THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1937<br></br>
         * Message: Since the person you challenged is not currently in a party, they cannot duel against your party.
         */
        val SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY: SystemMessageId

        /**
         * ID: 1938<br></br>
         * Message: $s1 has challenged you to a duel.
         */
        val S1_HAS_CHALLENGED_YOU_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1939<br></br>
         * Message: $s1's party has challenged your party to a duel.
         */
        val S1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1940<br></br>
         * Message: You are unable to request a duel at this time.
         */
        val YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME: SystemMessageId

        /**
         * ID: 1941<br></br>
         * Message: This is no suitable place to challenge anyone or party to a duel.
         */
        val NO_PLACE_FOR_DUEL: SystemMessageId

        /**
         * ID: 1942<br></br>
         * Message: The opposing party is currently unable to accept a challenge to a duel.
         */
        val THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1943<br></br>
         * Message: The opposing party is currently not in a suitable location for a duel.
         */
        val THE_OPPOSING_PARTY_IS_AT_BAD_LOCATION_FOR_A_DUEL: SystemMessageId

        /**
         * ID: 1944<br></br>
         * Message: In a moment, you will be transported to the site where the duel will take place.
         */
        val IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE: SystemMessageId

        /**
         * ID: 1945<br></br>
         * Message: The duel will begin in $s1 second(s).
         */
        val THE_DUEL_WILL_BEGIN_IN_S1_SECONDS: SystemMessageId

        /**
         * ID: 1946<br></br>
         * Message: $s1 has challenged you to a duel. Will you accept?
         */
        val S1_CHALLENGED_YOU_TO_A_DUEL: SystemMessageId

        /**
         * ID: 1947<br></br>
         * Message: $s1's party has challenged your party to a duel. Will you accept?
         */
        val S1_CHALLENGED_YOU_TO_A_PARTY_DUEL: SystemMessageId

        /**
         * ID: 1948<br></br>
         * Message: The duel will begin in $s1 second(s).
         */
        val THE_DUEL_WILL_BEGIN_IN_S1_SECONDS2: SystemMessageId

        /**
         * ID: 1949<br></br>
         * Message: Let the duel begin!
         */
        val LET_THE_DUEL_BEGIN: SystemMessageId

        /**
         * ID: 1950<br></br>
         * Message: $s1 has won the duel.
         */
        val S1_HAS_WON_THE_DUEL: SystemMessageId

        /**
         * ID: 1951<br></br>
         * Message: $s1's party has won the duel.
         */
        val S1_PARTY_HAS_WON_THE_DUEL: SystemMessageId

        /**
         * ID: 1952<br></br>
         * Message: The duel has ended in a tie.
         */
        val THE_DUEL_HAS_ENDED_IN_A_TIE: SystemMessageId

        /**
         * ID: 1953<br></br>
         * Message: Since $s1 was disqualified, $s2 has won.
         */
        val SINCE_S1_WAS_DISQUALIFIED_S2_HAS_WON: SystemMessageId

        /**
         * ID: 1954<br></br>
         * Message: Since $s1's party was disqualified, $s2's party has won.
         */
        val SINCE_S1_PARTY_WAS_DISQUALIFIED_S2_PARTY_HAS_WON: SystemMessageId

        /**
         * ID: 1955<br></br>
         * Message: Since $s1 withdrew from the duel, $s2 has won.
         */
        val SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON: SystemMessageId

        /**
         * ID: 1956<br></br>
         * Message: Since $s1's party withdrew from the duel, $s2's party has won.
         */
        val SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON: SystemMessageId

        /**
         * ID: 1957<br></br>
         * Message: Select the item to be augmented.
         */
        val SELECT_THE_ITEM_TO_BE_AUGMENTED: SystemMessageId

        /**
         * ID: 1958<br></br>
         * Message: Select the catalyst for augmentation.
         */
        val SELECT_THE_CATALYST_FOR_AUGMENTATION: SystemMessageId

        /**
         * ID: 1959<br></br>
         * Message: Requires $s1 $s2.
         */
        val REQUIRES_S1_S2: SystemMessageId

        /**
         * ID: 1960<br></br>
         * Message: This is not a suitable item.
         */
        val THIS_IS_NOT_A_SUITABLE_ITEM: SystemMessageId

        /**
         * ID: 1961<br></br>
         * Message: Gemstone quantity is incorrect.
         */
        val GEMSTONE_QUANTITY_IS_INCORRECT: SystemMessageId

        /**
         * ID: 1962<br></br>
         * Message: The item was successfully augmented!
         */
        val THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED: SystemMessageId

        /**
         * ID : 1963<br></br>
         * Message: Select the item from which you wish to remove augmentation.
         */
        val SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION: SystemMessageId

        /**
         * ID: 1964<br></br>
         * Message: Augmentation removal can only be done on an augmented item.
         */
        val AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM: SystemMessageId

        /**
         * ID: 1965<br></br>
         * Message: Augmentation has been successfully removed from your $s1.
         */
        val AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1: SystemMessageId

        /**
         * ID: 1966<br></br>
         * Message: Only the clan leader may issue commands.
         */
        val ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS: SystemMessageId

        /**
         * ID: 1967<br></br>
         * Message: The gate is firmly locked. Please try again later.
         */
        val GATE_LOCKED_TRY_AGAIN_LATER: SystemMessageId

        /**
         * ID: 1968<br></br>
         * Message: $s1's owner.
         */
        val S1_OWNER: SystemMessageId

        /**
         * ID: 1968<br></br>
         * Message: Area where $s1 appears.
         */
        val AREA_S1_APPEARS: SystemMessageId

        /**
         * ID: 1970<br></br>
         * Message: Once an item is augmented, it cannot be augmented again.
         */
        val ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN: SystemMessageId

        /**
         * ID: 1971<br></br>
         * Message: The level of the hardener is too high to be used.
         */
        val HARDENER_LEVEL_TOO_HIGH: SystemMessageId

        /**
         * ID: 1972<br></br>
         * Message: You cannot augment items while a private store or private workshop is in operation.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION: SystemMessageId

        /**
         * ID: 1973<br></br>
         * Message: You cannot augment items while frozen.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN: SystemMessageId

        /**
         * ID: 1974<br></br>
         * Message: You cannot augment items while dead.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD: SystemMessageId

        /**
         * ID: 1975<br></br>
         * Message: You cannot augment items while engaged in trade activities.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING: SystemMessageId

        /**
         * ID: 1976<br></br>
         * Message: You cannot augment items while paralyzed.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED: SystemMessageId

        /**
         * ID: 1977<br></br>
         * Message: You cannot augment items while fishing.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING: SystemMessageId

        /**
         * ID: 1978<br></br>
         * Message: You cannot augment items while sitting down.
         */
        val YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN: SystemMessageId

        /**
         * ID: 1979<br></br>
         * Message: $s1's remaining Mana is now 10.
         */
        val S1S_REMAINING_MANA_IS_NOW_10: SystemMessageId

        /**
         * ID: 1980<br></br>
         * Message: $s1's remaining Mana is now 5.
         */
        val S1S_REMAINING_MANA_IS_NOW_5: SystemMessageId

        /**
         * ID: 1981<br></br>
         * Message: $s1's remaining Mana is now 1. It will disappear soon.
         */
        val S1S_REMAINING_MANA_IS_NOW_1: SystemMessageId

        /**
         * ID: 1982<br></br>
         * Message: $s1's remaining Mana is now 0, and the item has disappeared.
         */
        val S1S_REMAINING_MANA_IS_NOW_0: SystemMessageId

        /**
         * ID: 1984<br></br>
         * Message: Press the Augment button to begin.
         */
        val PRESS_THE_AUGMENT_BUTTON_TO_BEGIN: SystemMessageId

        /**
         * ID: 1985<br></br>
         * Message: $s1's drop area ($s2)
         */
        val S1_DROP_AREA_S2: SystemMessageId

        /**
         * ID: 1986<br></br>
         * Message: $s1's owner ($s2)
         */
        val S1_OWNER_S2: SystemMessageId

        /**
         * ID: 1987<br></br>
         * Message: $s1
         */
        val S1: SystemMessageId

        /**
         * ID: 1988<br></br>
         * Message: The ferry has arrived at Primeval Isle.
         */
        val FERRY_ARRIVED_AT_PRIMEVAL: SystemMessageId

        /**
         * ID: 1989<br></br>
         * Message: The ferry will leave for Rune Harbor after anchoring for three minutes.
         */
        val FERRY_LEAVING_FOR_RUNE_3_MINUTES: SystemMessageId

        /**
         * ID: 1990<br></br>
         * Message: The ferry is now departing Primeval Isle for Rune Harbor.
         */
        val FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW: SystemMessageId

        /**
         * ID: 1991<br></br>
         * Message: The ferry will leave for Primeval Isle after anchoring for three minutes.
         */
        val FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES: SystemMessageId

        /**
         * ID: 1992<br></br>
         * Message: The ferry is now departing Rune Harbor for Primeval Isle.
         */
        val FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW: SystemMessageId

        /**
         * ID: 1993<br></br>
         * Message: The ferry from Primeval Isle to Rune Harbor has been delayed.
         */
        val FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED: SystemMessageId

        /**
         * ID: 1994<br></br>
         * Message: The ferry from Rune Harbor to Primeval Isle has been delayed.
         */
        val FERRY_FROM_RUNE_TO_PRIMEVAL_DELAYED: SystemMessageId

        /**
         * ID: 1995<br></br>
         * Message: $s1 channel filtering option
         */
        val S1_CHANNEL_FILTER_OPTION: SystemMessageId

        /**
         * ID: 1996<br></br>
         * Message: The attack has been blocked.
         */
        val ATTACK_WAS_BLOCKED: SystemMessageId

        /**
         * ID: 1997<br></br>
         * Message: $s1 is performing a counterattack.
         */
        val S1_PERFORMING_COUNTERATTACK: SystemMessageId

        /**
         * ID: 1998<br></br>
         * Message: You countered $s1's attack.
         */
        val COUNTERED_S1_ATTACK: SystemMessageId

        /**
         * ID: 1999<br></br>
         * Message: $s1 dodges the attack.
         */
        val S1_DODGES_ATTACK: SystemMessageId

        /**
         * ID: 2000<br></br>
         * Message: You have avoided $s1's attack.
         */
        val AVOIDED_S1_ATTACK2: SystemMessageId

        /**
         * ID: 2001<br></br>
         * Message: Augmentation failed due to inappropriate conditions.
         */
        val AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS: SystemMessageId

        /**
         * ID: 2002<br></br>
         * Message: Trap failed.
         */
        val TRAP_FAILED: SystemMessageId

        /**
         * ID: 2003<br></br>
         * Message: You obtained an ordinary material.
         */
        val OBTAINED_ORDINARY_MATERIAL: SystemMessageId

        /**
         * ID: 2004<br></br>
         * Message: You obtained a rare material.
         */
        val OBTAINED_RATE_MATERIAL: SystemMessageId

        /**
         * ID: 2005<br></br>
         * Message: You obtained a unique material.
         */
        val OBTAINED_UNIQUE_MATERIAL: SystemMessageId

        /**
         * ID: 2006<br></br>
         * Message: You obtained the only material of this kind.
         */
        val OBTAINED_ONLY_MATERIAL: SystemMessageId

        /**
         * ID: 2007<br></br>
         * Message: Please enter the recipient's name.
         */
        val ENTER_RECIPIENTS_NAME: SystemMessageId

        /**
         * ID: 2008<br></br>
         * Message: Please enter the text.
         */
        val ENTER_TEXT: SystemMessageId

        /**
         * ID: 2009<br></br>
         * Message: You cannot exceed 1500 characters.
         */
        val CANT_EXCEED_1500_CHARACTERS: SystemMessageId

        /**
         * ID: 2009<br></br>
         * Message: $s2 $s1
         */
        val S2_S1: SystemMessageId

        /**
         * ID: 2011<br></br>
         * Message: The augmented item cannot be discarded.
         */
        val AUGMENTED_ITEM_CANNOT_BE_DISCARDED: SystemMessageId

        /**
         * ID: 2012<br></br>
         * Message: $s1 has been activated.
         */
        val S1_HAS_BEEN_ACTIVATED: SystemMessageId

        /**
         * ID: 2013<br></br>
         * Message: Your seed or remaining purchase amount is inadequate.
         */
        val YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE: SystemMessageId

        /**
         * ID: 2014<br></br>
         * Message: You cannot proceed because the manor cannot accept any more crops. All crops have been returned and no adena withdrawn.
         */
        val MANOR_CANT_ACCEPT_MORE_CROPS: SystemMessageId

        /**
         * ID: 2015<br></br>
         * Message: A skill is ready to be used again.
         */
        val SKILL_READY_TO_USE_AGAIN: SystemMessageId

        /**
         * ID: 2016<br></br>
         * Message: A skill is ready to be used again but its re-use counter time has increased.
         */
        val SKILL_READY_TO_USE_AGAIN_BUT_TIME_INCREASED: SystemMessageId

        /**
         * ID: 2017<br></br>
         * Message: $s1 cannot duel because $s1 is currently engaged in a private store or manufacture.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE: SystemMessageId

        /**
         * ID: 2018<br></br>
         * Message: $s1 cannot duel because $s1 is currently fishing.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING: SystemMessageId

        /**
         * ID: 2019<br></br>
         * Message: $s1 cannot duel because $s1's HP or MP is below 50%.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT: SystemMessageId

        /**
         * ID: 2020<br></br>
         * Message: $s1 cannot make a challenge to a duel because $s1 is currently in a duel-prohibited area (Peaceful Zone / Seven Signs Zone / Near Water / Restart Prohibited Area).
         */
        val S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA: SystemMessageId

        /**
         * ID: 2021<br></br>
         * Message: $s1 cannot duel because $s1 is currently engaged in battle.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE: SystemMessageId

        /**
         * ID: 2022<br></br>
         * Message: $s1 cannot duel because $s1 is already engaged in a duel.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL: SystemMessageId

        /**
         * ID: 2023<br></br>
         * Message: $s1 cannot duel because $s1 is in a chaotic state.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE: SystemMessageId

        /**
         * ID: 2024<br></br>
         * Message: $s1 cannot duel because $s1 is participating in the Olympiad.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD: SystemMessageId

        /**
         * ID: 2025<br></br>
         * Message: $s1 cannot duel because $s1 is participating in a clan hall war.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR: SystemMessageId

        /**
         * ID: 2026<br></br>
         * Message: $s1 cannot duel because $s1 is participating in a siege war.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_SIEGE_WAR: SystemMessageId

        /**
         * ID: 2027<br></br>
         * Message: $s1 cannot duel because $s1 is currently riding a boat or strider.
         */
        val S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER: SystemMessageId

        /**
         * ID: 2028<br></br>
         * Message: $s1 cannot receive a duel challenge because $s1 is too far away.
         */
        val S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY: SystemMessageId

        /**
         * ID: 2029<br></br>
         * Message: $s1 is currently teleporting and cannot participate in the Olympiad.
         */
        val S1_CANNOT_PARTICIPATE_IN_OLYMPIAD_DURING_TELEPORT: SystemMessageId

        /**
         * ID: 2030<br></br>
         * Message: You are currently logging in.
         */
        val CURRENTLY_LOGGING_IN: SystemMessageId

        /**
         * ID: 2031<br></br>
         * Message: Please wait a moment.
         */
        val PLEASE_WAIT_A_MOMENT: SystemMessageId

        /**
         * Array containing all SystemMessageIds<br></br>
         * Important: Always initialize with a length of the highest SystemMessageId + 1!!!
         */
        private var VALUES: Array<SystemMessageId?>? = null

        init {
            ALREADY_MEMBER_OF_S1 = SystemMessageId(7)
            YOU_ARE_WORKING_WITH_ANOTHER_CLAN = SystemMessageId(8)
            S1_IS_NOT_A_CLAN_LEADER = SystemMessageId(9)
            S1_WORKING_WITH_ANOTHER_CLAN = SystemMessageId(10)
            NO_APPLICANTS_FOR_THIS_CLAN = SystemMessageId(11)
            APPLICANT_INFORMATION_INCORRECT = SystemMessageId(12)
            CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE = SystemMessageId(13)
            CANNOT_DISSOLVE_CAUSE_CLAN_OWNS_CASTLES_HIDEOUTS = SystemMessageId(14)
            YOU_ARE_IN_SIEGE = SystemMessageId(15)
            YOU_ARE_NOT_IN_SIEGE = SystemMessageId(16)
            CASTLE_SIEGE_HAS_BEGUN = SystemMessageId(17)
            CASTLE_SIEGE_HAS_ENDED = SystemMessageId(18)
            NEW_CASTLE_LORD = SystemMessageId(19)
            GATE_IS_OPENING = SystemMessageId(20)
            GATE_IS_DESTROYED = SystemMessageId(21)
            TARGET_TOO_FAR = SystemMessageId(22)
            NOT_ENOUGH_HP = SystemMessageId(23)
            NOT_ENOUGH_MP = SystemMessageId(24)
            REJUVENATING_HP = SystemMessageId(25)
            REJUVENATING_MP = SystemMessageId(26)
            CASTING_INTERRUPTED = SystemMessageId(27)
            YOU_PICKED_UP_S1_ADENA = SystemMessageId(28)
            YOU_PICKED_UP_S2_S1 = SystemMessageId(29)
            YOU_PICKED_UP_S1 = SystemMessageId(30)
            CANT_MOVE_SITTING = SystemMessageId(31)
            UNABLE_COMBAT_PLEASE_GO_RESTART = SystemMessageId(32)
            CANT_MOVE_CASTING = SystemMessageId(33)
            WELCOME_TO_LINEAGE = SystemMessageId(34)
            YOU_DID_S1_DMG = SystemMessageId(35)
            S1_GAVE_YOU_S2_DMG = SystemMessageId(36)
            S1_GAVE_YOU_S2_DMG2 = SystemMessageId(37)
            GETTING_READY_TO_SHOOT_AN_ARROW = SystemMessageId(41)
            AVOIDED_S1_ATTACK = SystemMessageId(42)
            MISSED_TARGET = SystemMessageId(43)
            CRITICAL_HIT = SystemMessageId(44)
            EARNED_S1_EXPERIENCE = SystemMessageId(45)
            USE_S1 = SystemMessageId(46)
            BEGIN_TO_USE_S1 = SystemMessageId(47)
            S1_PREPARED_FOR_REUSE = SystemMessageId(48)
            S1_EQUIPPED = SystemMessageId(49)
            TARGET_CANT_FOUND = SystemMessageId(50)
            CANNOT_USE_ON_YOURSELF = SystemMessageId(51)
            EARNED_S1_ADENA = SystemMessageId(52)
            EARNED_S2_S1_S = SystemMessageId(53)
            EARNED_ITEM_S1 = SystemMessageId(54)
            FAILED_TO_PICKUP_S1_ADENA = SystemMessageId(55)
            FAILED_TO_PICKUP_S1 = SystemMessageId(56)
            FAILED_TO_PICKUP_S2_S1_S = SystemMessageId(57)
            FAILED_TO_EARN_S1_ADENA = SystemMessageId(58)
            FAILED_TO_EARN_S1 = SystemMessageId(59)
            FAILED_TO_EARN_S2_S1_S = SystemMessageId(60)
            NOTHING_HAPPENED = SystemMessageId(61)
            S1_SUCCESSFULLY_ENCHANTED = SystemMessageId(62)
            S1_S2_SUCCESSFULLY_ENCHANTED = SystemMessageId(63)
            ENCHANTMENT_FAILED_S1_EVAPORATED = SystemMessageId(64)
            ENCHANTMENT_FAILED_S1_S2_EVAPORATED = SystemMessageId(65)
            S1_INVITED_YOU_TO_PARTY = SystemMessageId(66)
            S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2 = SystemMessageId(67)
            WOULD_YOU_LIKE_TO_WITHDRAW_FROM_THE_S1_CLAN = SystemMessageId(68)
            WOULD_YOU_LIKE_TO_DISMISS_S1_FROM_THE_CLAN = SystemMessageId(69)
            DO_YOU_WISH_TO_DISPERSE_THE_CLAN_S1 = SystemMessageId(70)
            HOW_MANY_S1_DISCARD = SystemMessageId(71)
            HOW_MANY_S1_MOVE = SystemMessageId(72)
            HOW_MANY_S1_DESTROY = SystemMessageId(73)
            WISH_DESTROY_S1 = SystemMessageId(74)
            ID_NOT_EXIST = SystemMessageId(75)
            INCORRECT_PASSWORD = SystemMessageId(76)
            CANNOT_CREATE_CHARACTER = SystemMessageId(77)
            WISH_DELETE_S1 = SystemMessageId(78)
            NAMING_NAME_ALREADY_EXISTS = SystemMessageId(79)
            NAMING_CHARNAME_UP_TO_16CHARS = SystemMessageId(80)
            PLEASE_SELECT_RACE = SystemMessageId(81)
            PLEASE_SELECT_OCCUPATION = SystemMessageId(82)
            PLEASE_SELECT_GENDER = SystemMessageId(83)
            CANT_ATK_PEACEZONE = SystemMessageId(84)
            TARGET_IN_PEACEZONE = SystemMessageId(85)
            PLEASE_ENTER_ID = SystemMessageId(86)
            PLEASE_ENTER_PASSWORD = SystemMessageId(87)
            WRONG_PROTOCOL_CHECK = SystemMessageId(88)
            WRONG_PROTOCOL_CONTINUE = SystemMessageId(89)
            UNABLE_TO_CONNECT = SystemMessageId(90)
            PLEASE_SELECT_HAIRSTYLE = SystemMessageId(91)
            S1_HAS_WORN_OFF = SystemMessageId(92)
            NOT_ENOUGH_SP = SystemMessageId(93)
            COPYRIGHT = SystemMessageId(94)
            YOU_EARNED_S1_EXP_AND_S2_SP = SystemMessageId(95)
            YOU_INCREASED_YOUR_LEVEL = SystemMessageId(96)
            CANNOT_MOVE_THIS_ITEM = SystemMessageId(97)
            CANNOT_DISCARD_THIS_ITEM = SystemMessageId(98)
            CANNOT_TRADE_THIS_ITEM = SystemMessageId(99)
            S1_REQUESTS_TRADE = SystemMessageId(100)
            CANT_LOGOUT_WHILE_FIGHTING = SystemMessageId(101)
            CANT_RESTART_WHILE_FIGHTING = SystemMessageId(102)
            ID_LOGGED_IN = SystemMessageId(103)
            CANNOT_USE_ITEM_WHILE_USING_MAGIC = SystemMessageId(104)
            YOU_INVITED_S1_TO_PARTY = SystemMessageId(105)
            YOU_JOINED_S1_PARTY = SystemMessageId(106)
            S1_JOINED_PARTY = SystemMessageId(107)
            S1_LEFT_PARTY = SystemMessageId(108)
            INCORRECT_TARGET = SystemMessageId(109)
            YOU_FEEL_S1_EFFECT = SystemMessageId(110)
            SHIELD_DEFENCE_SUCCESSFULL = SystemMessageId(111)
            NOT_ENOUGH_ARROWS = SystemMessageId(112)
            S1_CANNOT_BE_USED = SystemMessageId(113)
            ENTER_SHADOW_MOTHER_TREE = SystemMessageId(114)
            EXIT_SHADOW_MOTHER_TREE = SystemMessageId(115)
            ENTER_PEACEFUL_ZONE = SystemMessageId(116)
            EXIT_PEACEFUL_ZONE = SystemMessageId(117)
            REQUEST_S1_FOR_TRADE = SystemMessageId(118)
            S1_DENIED_TRADE_REQUEST = SystemMessageId(119)
            BEGIN_TRADE_WITH_S1 = SystemMessageId(120)
            S1_CONFIRMED_TRADE = SystemMessageId(121)
            CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED = SystemMessageId(122)
            TRADE_SUCCESSFUL = SystemMessageId(123)
            S1_CANCELED_TRADE = SystemMessageId(124)
            WISH_EXIT_GAME = SystemMessageId(125)
            WISH_RESTART_GAME = SystemMessageId(126)
            DISCONNECTED_FROM_SERVER = SystemMessageId(127)
            CHARACTER_CREATION_FAILED = SystemMessageId(128)
            SLOTS_FULL = SystemMessageId(129)
            WAREHOUSE_FULL = SystemMessageId(130)
            S1_LOGGED_IN = SystemMessageId(131)
            S1_ADDED_TO_FRIENDS = SystemMessageId(132)
            S1_REMOVED_FROM_YOUR_FRIENDS_LIST = SystemMessageId(133)
            PLEACE_CHECK_YOUR_FRIEND_LIST_AGAIN = SystemMessageId(134)
            S1_DID_NOT_REPLY_TO_YOUR_INVITE = SystemMessageId(135)
            YOU_DID_NOT_REPLY_TO_S1_INVITE = SystemMessageId(136)
            NO_MORE_ITEMS_SHORTCUT = SystemMessageId(137)
            DESIGNATE_SHORTCUT = SystemMessageId(138)
            S1_RESISTED_YOUR_S2 = SystemMessageId(139)
            SKILL_REMOVED_DUE_LACK_MP = SystemMessageId(140)
            ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN = SystemMessageId(141)
            ALREADY_TRADING = SystemMessageId(142)
            S1_ALREADY_TRADING = SystemMessageId(143)
            TARGET_IS_INCORRECT = SystemMessageId(144)
            TARGET_IS_NOT_FOUND_IN_THE_GAME = SystemMessageId(145)
            CHATTING_PERMITTED = SystemMessageId(146)
            CHATTING_PROHIBITED = SystemMessageId(147)
            CANNOT_USE_QUEST_ITEMS = SystemMessageId(148)
            CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING = SystemMessageId(149)
            CANNOT_DISCARD_OR_DESTROY_ITEM_WHILE_TRADING = SystemMessageId(150)
            CANNOT_DISCARD_DISTANCE_TOO_FAR = SystemMessageId(151)
            YOU_HAVE_INVITED_THE_WRONG_TARGET = SystemMessageId(152)
            S1_IS_BUSY_TRY_LATER = SystemMessageId(153)
            ONLY_LEADER_CAN_INVITE = SystemMessageId(154)
            PARTY_FULL = SystemMessageId(155)
            DRAIN_HALF_SUCCESFUL = SystemMessageId(156)
            RESISTED_S1_DRAIN = SystemMessageId(157)
            ATTACK_FAILED = SystemMessageId(158)
            RESISTED_S1_MAGIC = SystemMessageId(159)
            S1_IS_ALREADY_IN_PARTY = SystemMessageId(160)
            INVITED_USER_NOT_ONLINE = SystemMessageId(161)
            WAREHOUSE_TOO_FAR = SystemMessageId(162)
            CANNOT_DESTROY_NUMBER_INCORRECT = SystemMessageId(163)
            WAITING_FOR_ANOTHER_REPLY = SystemMessageId(164)
            YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST = SystemMessageId(165)
            FRIEND_LIST_NOT_READY_YET_REGISTER_LATER = SystemMessageId(166)
            S1_ALREADY_ON_FRIEND_LIST = SystemMessageId(167)
            S1_REQUESTED_TO_BECOME_FRIENDS = SystemMessageId(168)
            ACCEPT_THE_FRIENDSHIP = SystemMessageId(169)
            THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME = SystemMessageId(170)
            S1_NOT_ON_YOUR_FRIENDS_LIST = SystemMessageId(171)
            LACK_FUNDS_FOR_TRANSACTION1 = SystemMessageId(172)
            LACK_FUNDS_FOR_TRANSACTION2 = SystemMessageId(173)
            OTHER_INVENTORY_FULL = SystemMessageId(174)
            SKILL_DEACTIVATED_HP_FULL = SystemMessageId(175)
            THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE = SystemMessageId(176)
            MESSAGE_REFUSAL_MODE = SystemMessageId(177)
            MESSAGE_ACCEPTANCE_MODE = SystemMessageId(178)
            CANT_DISCARD_HERE = SystemMessageId(179)
            S1_DAYS_LEFT_CANCEL_ACTION = SystemMessageId(180)
            CANT_SEE_TARGET = SystemMessageId(181)
            WANT_QUIT_CURRENT_QUEST = SystemMessageId(182)
            TOO_MANY_USERS = SystemMessageId(183)
            TRY_AGAIN_LATER = SystemMessageId(184)
            FIRST_SELECT_USER_TO_INVITE_TO_PARTY = SystemMessageId(185)
            FIRST_SELECT_USER_TO_INVITE_TO_CLAN = SystemMessageId(186)
            SELECT_USER_TO_EXPEL = SystemMessageId(187)
            PLEASE_CREATE_CLAN_NAME = SystemMessageId(188)
            CLAN_CREATED = SystemMessageId(189)
            FAILED_TO_CREATE_CLAN = SystemMessageId(190)
            CLAN_MEMBER_S1_EXPELLED = SystemMessageId(191)
            FAILED_EXPEL_S1 = SystemMessageId(192)
            CLAN_HAS_DISPERSED = SystemMessageId(193)
            FAILED_TO_DISPERSE_CLAN = SystemMessageId(194)
            ENTERED_THE_CLAN = SystemMessageId(195)
            S1_REFUSED_TO_JOIN_CLAN = SystemMessageId(196)
            YOU_HAVE_WITHDRAWN_FROM_CLAN = SystemMessageId(197)
            FAILED_TO_WITHDRAW_FROM_S1_CLAN = SystemMessageId(198)
            CLAN_MEMBERSHIP_TERMINATED = SystemMessageId(199)
            YOU_LEFT_PARTY = SystemMessageId(200)
            S1_WAS_EXPELLED_FROM_PARTY = SystemMessageId(201)
            HAVE_BEEN_EXPELLED_FROM_PARTY = SystemMessageId(202)
            PARTY_DISPERSED = SystemMessageId(203)
            INCORRECT_NAME_TRY_AGAIN = SystemMessageId(204)
            INCORRECT_CHARACTER_NAME_TRY_AGAIN = SystemMessageId(205)
            ENTER_CLAN_NAME_TO_DECLARE_WAR = SystemMessageId(206)
            S2_OF_THE_CLAN_S1_REQUESTS_WAR = SystemMessageId(207)
            YOU_ARE_NOT_A_CLAN_MEMBER = SystemMessageId(212)
            NOT_WORKING_PLEASE_TRY_AGAIN_LATER = SystemMessageId(213)
            TITLE_CHANGED = SystemMessageId(214)
            WAR_WITH_THE_S1_CLAN_HAS_BEGUN = SystemMessageId(215)
            WAR_WITH_THE_S1_CLAN_HAS_ENDED = SystemMessageId(216)
            YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN = SystemMessageId(217)
            YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN = SystemMessageId(218)
            YOU_WERE_DEFEATED_BY_S1_CLAN = SystemMessageId(219)
            S1_MINUTES_LEFT_UNTIL_CLAN_WAR_ENDS = SystemMessageId(220)
            CLAN_WAR_WITH_S1_CLAN_HAS_ENDED = SystemMessageId(221)
            S1_HAS_JOINED_CLAN = SystemMessageId(222)
            S1_HAS_WITHDRAWN_FROM_THE_CLAN = SystemMessageId(223)
            S1_DID_NOT_RESPOND_TO_CLAN_INVITATION = SystemMessageId(224)
            YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION = SystemMessageId(225)
            S1_CLAN_DID_NOT_RESPOND = SystemMessageId(226)
            CLAN_WAR_REFUSED_YOU_DID_NOT_RESPOND_TO_S1 = SystemMessageId(227)
            REQUEST_TO_END_WAR_HAS_BEEN_DENIED = SystemMessageId(228)
            YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN = SystemMessageId(229)
            YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN = SystemMessageId(230)
            YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER = SystemMessageId(231)
            YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN = SystemMessageId(232)
            SUBCLAN_IS_FULL = SystemMessageId(233)
            TARGET_MUST_BE_IN_CLAN = SystemMessageId(234)
            NOT_AUTHORIZED_TO_BESTOW_RIGHTS = SystemMessageId(235)
            ONLY_THE_CLAN_LEADER_IS_ENABLED = SystemMessageId(236)
            CLAN_LEADER_NOT_FOUND = SystemMessageId(237)
            NOT_JOINED_IN_ANY_CLAN = SystemMessageId(238)
            CLAN_LEADER_CANNOT_WITHDRAW = SystemMessageId(239)
            CURRENTLY_INVOLVED_IN_CLAN_WAR = SystemMessageId(240)
            LEADER_OF_S1_CLAN_NOT_FOUND = SystemMessageId(241)
            SELECT_TARGET = SystemMessageId(242)
            CANNOT_DECLARE_WAR_ON_ALLIED_CLAN = SystemMessageId(243)
            NOT_ALLOWED_TO_CHALLENGE = SystemMessageId(244)
            FIVE_DAYS_NOT_PASSED_SINCE_REFUSED_WAR = SystemMessageId(245)
            CLAN_CURRENTLY_AT_WAR = SystemMessageId(246)
            FIVE_DAYS_MUST_PASS_BEFORE_CHALLENGE_S1_AGAIN = SystemMessageId(247)
            S1_CLAN_NOT_ENOUGH_MEMBERS_FOR_WAR = SystemMessageId(248)
            WISH_SURRENDER_TO_S1_CLAN = SystemMessageId(249)
            YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN = SystemMessageId(250)
            ALREADY_AT_WAR_WITH_ANOTHER_CLAN = SystemMessageId(251)
            ENTER_CLAN_NAME_TO_SURRENDER_TO = SystemMessageId(252)
            ENTER_CLAN_NAME_TO_END_WAR = SystemMessageId(253)
            LEADER_CANT_PERSONALLY_SURRENDER = SystemMessageId(254)
            S1_CLAN_REQUESTED_END_WAR = SystemMessageId(255)
            ENTER_TITLE = SystemMessageId(256)
            DO_YOU_OFFER_S1_CLAN_END_WAR = SystemMessageId(257)
            NOT_INVOLVED_CLAN_WAR = SystemMessageId(258)
            SELECT_MEMBERS_FROM_LIST = SystemMessageId(259)
            FIVE_DAYS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR = SystemMessageId(260)
            CLAN_NAME_INVALID = SystemMessageId(261)
            CLAN_NAME_LENGTH_INCORRECT = SystemMessageId(262)
            DISSOLUTION_IN_PROGRESS = SystemMessageId(263)
            CANNOT_DISSOLVE_WHILE_IN_WAR = SystemMessageId(264)
            CANNOT_DISSOLVE_WHILE_IN_SIEGE = SystemMessageId(265)
            CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE = SystemMessageId(266)
            NO_REQUESTS_TO_DISPERSE = SystemMessageId(267)
            PLAYER_ALREADY_ANOTHER_CLAN = SystemMessageId(268)
            YOU_CANNOT_DISMISS_YOURSELF = SystemMessageId(269)
            YOU_HAVE_ALREADY_SURRENDERED = SystemMessageId(270)
            CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE = SystemMessageId(271)
            CLAN_LVL_3_NEEDED_TO_SET_CREST = SystemMessageId(272)
            CLAN_LVL_3_NEEDED_TO_DECLARE_WAR = SystemMessageId(273)
            CLAN_LEVEL_INCREASED = SystemMessageId(274)
            CLAN_LEVEL_INCREASE_FAILED = SystemMessageId(275)
            ITEM_MISSING_TO_LEARN_SKILL = SystemMessageId(276)
            LEARNED_SKILL_S1 = SystemMessageId(277)
            NOT_ENOUGH_SP_TO_LEARN_SKILL = SystemMessageId(278)
            YOU_NOT_ENOUGH_ADENA = SystemMessageId(279)
            NO_ITEMS_TO_SELL = SystemMessageId(280)
            YOU_NOT_ENOUGH_ADENA_PAY_FEE = SystemMessageId(281)
            NO_ITEM_DEPOSITED_IN_WH = SystemMessageId(282)
            ENTERED_COMBAT_ZONE = SystemMessageId(283)
            LEFT_COMBAT_ZONE = SystemMessageId(284)
            CLAN_S1_ENGRAVED_RULER = SystemMessageId(285)
            BASE_UNDER_ATTACK = SystemMessageId(286)
            OPPONENT_STARTED_ENGRAVING = SystemMessageId(287)
            CASTLE_GATE_BROKEN_DOWN = SystemMessageId(288)
            NOT_ANOTHER_HEADQUARTERS = SystemMessageId(289)
            NOT_SET_UP_BASE_HERE = SystemMessageId(290)
            CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE = SystemMessageId(291)
            S1_ANNOUNCED_SIEGE_TIME = SystemMessageId(292)
            REGISTRATION_TERM_FOR_S1_ENDED = SystemMessageId(293)
            BECAUSE_YOUR_CLAN_IS_NOT_CURRENTLY_ON_THE_OFFENSIVE_IN_A_CLAN_HALL_SIEGE_WAR_IT_CANNOT_SUMMON_ITS_BASE_CAMP =
                SystemMessageId(294)
            S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED = SystemMessageId(295)
            FALL_DAMAGE_S1 = SystemMessageId(296)
            DROWN_DAMAGE_S1 = SystemMessageId(297)
            YOU_DROPPED_S1 = SystemMessageId(298)
            S1_OBTAINED_S3_S2 = SystemMessageId(299)
            S1_OBTAINED_S2 = SystemMessageId(300)
            S2_S1_DISAPPEARED = SystemMessageId(301)
            S1_DISAPPEARED = SystemMessageId(302)
            SELECT_ITEM_TO_ENCHANT = SystemMessageId(303)
            CLAN_MEMBER_S1_LOGGED_IN = SystemMessageId(304)
            PLAYER_DECLINED = SystemMessageId(305)
            YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER = SystemMessageId(309)
            CLAN_WAR_DECLARATION_ACCEPTED = SystemMessageId(311)
            CLAN_WAR_DECLARATION_REFUSED = SystemMessageId(312)
            CEASE_WAR_REQUEST_ACCEPTED = SystemMessageId(313)
            FAILED_TO_SURRENDER = SystemMessageId(314)
            FAILED_TO_PERSONALLY_SURRENDER = SystemMessageId(315)
            FAILED_TO_WITHDRAW_FROM_THE_PARTY = SystemMessageId(316)
            FAILED_TO_EXPEL_THE_PARTY_MEMBER = SystemMessageId(317)
            FAILED_TO_DISPERSE_THE_PARTY = SystemMessageId(318)
            UNABLE_TO_UNLOCK_DOOR = SystemMessageId(319)
            FAILED_TO_UNLOCK_DOOR = SystemMessageId(320)
            ITS_NOT_LOCKED = SystemMessageId(321)
            DECIDE_SALES_PRICE = SystemMessageId(322)
            FORCE_INCREASED_TO_S1 = SystemMessageId(323)
            FORCE_MAXLEVEL_REACHED = SystemMessageId(324)
            CORPSE_ALREADY_DISAPPEARED = SystemMessageId(325)
            SELECT_TARGET_FROM_LIST = SystemMessageId(326)
            CANNOT_EXCEED_80_CHARACTERS = SystemMessageId(327)
            PLEASE_INPUT_TITLE_LESS_128_CHARACTERS = SystemMessageId(328)
            PLEASE_INPUT_CONTENT_LESS_3000_CHARACTERS = SystemMessageId(329)
            ONE_LINE_RESPONSE_NOT_EXCEED_128_CHARACTERS = SystemMessageId(330)
            ACQUIRED_S1_SP = SystemMessageId(331)
            DO_YOU_WANT_TO_BE_RESTORED = SystemMessageId(332)
            S1_DAMAGE_BY_CORE_BARRIER = SystemMessageId(333)
            ENTER_PRIVATE_STORE_MESSAGE = SystemMessageId(334)
            S1_HAS_BEEN_ABORTED = SystemMessageId(335)
            WISH_TO_CRYSTALLIZE_S1 = SystemMessageId(336)
            SOULSHOTS_GRADE_MISMATCH = SystemMessageId(337)
            NOT_ENOUGH_SOULSHOTS = SystemMessageId(338)
            CANNOT_USE_SOULSHOTS = SystemMessageId(339)
            PRIVATE_STORE_UNDER_WAY = SystemMessageId(340)
            NOT_ENOUGH_MATERIALS = SystemMessageId(341)
            ENABLED_SOULSHOT = SystemMessageId(342)
            SWEEPER_FAILED_TARGET_NOT_SPOILED = SystemMessageId(343)
            SOULSHOTS_DISABLED = SystemMessageId(344)
            CHAT_ENABLED = SystemMessageId(345)
            CHAT_DISABLED = SystemMessageId(346)
            INCORRECT_ITEM_COUNT = SystemMessageId(347)
            INCORRECT_ITEM_PRICE = SystemMessageId(348)
            PRIVATE_STORE_ALREADY_CLOSED = SystemMessageId(349)
            ITEM_OUT_OF_STOCK = SystemMessageId(350)
            NOT_ENOUGH_ITEMS = SystemMessageId(351)
            CANCEL_ENCHANT = SystemMessageId(354)
            INAPPROPRIATE_ENCHANT_CONDITION = SystemMessageId(355)
            REJECT_RESURRECTION = SystemMessageId(356)
            ALREADY_SPOILED = SystemMessageId(357)
            S1_HOURS_UNTIL_SIEGE_CONCLUSION = SystemMessageId(358)
            S1_MINUTES_UNTIL_SIEGE_CONCLUSION = SystemMessageId(359)
            CASTLE_SIEGE_S1_SECONDS_LEFT = SystemMessageId(360)
            OVER_HIT = SystemMessageId(361)
            ACQUIRED_BONUS_EXPERIENCE_THROUGH_OVER_HIT = SystemMessageId(362)
            CHAT_AVAILABLE_S1_MINUTE = SystemMessageId(363)
            ENTER_USER_NAME_TO_SEARCH = SystemMessageId(364)
            ARE_YOU_SURE = SystemMessageId(365)
            PLEASE_SELECT_HAIR_COLOR = SystemMessageId(366)
            CANNOT_REMOVE_CLAN_CHARACTER = SystemMessageId(367)
            S1_S2_EQUIPPED = SystemMessageId(368)
            YOU_PICKED_UP_A_S1_S2 = SystemMessageId(369)
            FAILED_PICKUP_S1 = SystemMessageId(370)
            ACQUIRED_S1_S2 = SystemMessageId(371)
            FAILED_EARN_S1 = SystemMessageId(372)
            WISH_DESTROY_S1_S2 = SystemMessageId(373)
            WISH_CRYSTALLIZE_S1_S2 = SystemMessageId(374)
            DROPPED_S1_S2 = SystemMessageId(375)
            S1_OBTAINED_S2_S3 = SystemMessageId(376)
            S1_S2_DISAPPEARED = SystemMessageId(377)
            S1_PURCHASED_S2 = SystemMessageId(378)
            S1_PURCHASED_S2_S3 = SystemMessageId(379)
            S1_PURCHASED_S3_S2_S = SystemMessageId(380)
            GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER = SystemMessageId(381)
            NO_USERS_CHECKED_OUT_GM_ID = SystemMessageId(382)
            REQUEST_CONFIRMED_TO_END_CONSULTATION = SystemMessageId(383)
            CLIENT_NOT_LOGGED_ONTO_GAME_SERVER = SystemMessageId(384)
            REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION = SystemMessageId(385)
            PETITION_MORE_THAN_FIVE_CHARACTERS = SystemMessageId(386)
            THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK = SystemMessageId(387)
            NOT_UNDER_PETITION_CONSULTATION = SystemMessageId(388)
            PETITION_ACCEPTED_RECENT_NO_S1 = SystemMessageId(389)
            ONLY_ONE_ACTIVE_PETITION_AT_TIME = SystemMessageId(390)
            RECENT_NO_S1_CANCELED = SystemMessageId(391)
            UNDER_PETITION_ADVICE = SystemMessageId(392)
            FAILED_CANCEL_PETITION_TRY_LATER = SystemMessageId(393)
            PETITION_WITH_S1_UNDER_WAY = SystemMessageId(394)
            PETITION_ENDED_WITH_S1 = SystemMessageId(395)
            TRY_AGAIN_AFTER_CHANGING_PASSWORD = SystemMessageId(396)
            NO_PAID_ACCOUNT = SystemMessageId(397)
            NO_TIME_LEFT_ON_ACCOUNT = SystemMessageId(398)
            WISH_TO_DROP_S1 = SystemMessageId(400)
            TOO_MANY_QUESTS = SystemMessageId(401)
            NOT_CORRECT_BOAT_TICKET = SystemMessageId(402)
            EXCEECED_POCKET_ADENA_LIMIT = SystemMessageId(403)
            CREATE_LVL_TOO_LOW_TO_REGISTER = SystemMessageId(404)
            TOTAL_PRICE_TOO_HIGH = SystemMessageId(405)
            PETITION_APP_ACCEPTED = SystemMessageId(406)
            PETITION_UNDER_PROCESS = SystemMessageId(407)
            SET_PERIOD = SystemMessageId(408)
            SET_TIME_S1_S2_S3 = SystemMessageId(409)
            REGISTRATION_PERIOD = SystemMessageId(410)
            REGISTRATION_TIME_S1_S2_S3 = SystemMessageId(411)
            BATTLE_BEGINS_S1_S2_S3 = SystemMessageId(412)
            BATTLE_ENDS_S1_S2_S3 = SystemMessageId(413)
            STANDBY = SystemMessageId(414)
            UNDER_SIEGE = SystemMessageId(415)
            ITEM_CANNOT_EXCHANGE = SystemMessageId(416)
            S1_DISARMED = SystemMessageId(417)
            S1_MINUTES_USAGE_LEFT = SystemMessageId(419)
            TIME_EXPIRED = SystemMessageId(420)
            ANOTHER_LOGIN_WITH_ACCOUNT = SystemMessageId(421)
            WEIGHT_LIMIT_EXCEEDED = SystemMessageId(422)
            ENCHANT_SCROLL_CANCELLED = SystemMessageId(423)
            DOES_NOT_FIT_SCROLL_CONDITIONS = SystemMessageId(424)
            CREATE_LVL_TOO_LOW_TO_REGISTER2 = SystemMessageId(425)
            REFERENCE_MEMBERSHIP_WITHDRAWAL_S1 = SystemMessageId(445)
            DOT = SystemMessageId(447)
            SYSTEM_ERROR_LOGIN_LATER = SystemMessageId(448)
            PASSWORD_ENTERED_INCORRECT1 = SystemMessageId(449)
            CONFIRM_ACCOUNT_LOGIN_LATER = SystemMessageId(450)
            PASSWORD_ENTERED_INCORRECT2 = SystemMessageId(451)
            PLEASE_CONFIRM_ACCOUNT_LOGIN_LATER = SystemMessageId(452)
            ACCOUNT_INFORMATION_INCORRECT = SystemMessageId(453)
            ACCOUNT_IN_USE = SystemMessageId(455)
            LINAGE_MINIMUM_AGE = SystemMessageId(456)
            SERVER_MAINTENANCE = SystemMessageId(457)
            USAGE_TERM_EXPIRED = SystemMessageId(458)
            TO_REACTIVATE_YOUR_ACCOUNT = SystemMessageId(460)
            ACCESS_FAILED = SystemMessageId(461)
            PLEASE_TRY_AGAIN_LATER = SystemMessageId(462)
            FEATURE_ONLY_FOR_ALLIANCE_LEADER = SystemMessageId(464)
            NO_CURRENT_ALLIANCES = SystemMessageId(465)
            YOU_HAVE_EXCEEDED_THE_LIMIT = SystemMessageId(466)
            CANT_INVITE_CLAN_WITHIN_1_DAY = SystemMessageId(467)
            CANT_ENTER_ALLIANCE_WITHIN_1_DAY = SystemMessageId(468)
            MAY_NOT_ALLY_CLAN_BATTLE = SystemMessageId(469)
            ONLY_CLAN_LEADER_WITHDRAW_ALLY = SystemMessageId(470)
            ALLIANCE_LEADER_CANT_WITHDRAW = SystemMessageId(471)
            CANNOT_EXPEL_YOURSELF = SystemMessageId(472)
            DIFFERENT_ALLIANCE = SystemMessageId(473)
            CLAN_DOESNT_EXISTS = SystemMessageId(474)
            DIFFERENT_ALLIANCE2 = SystemMessageId(475)
            ADJUST_IMAGE_8_12 = SystemMessageId(476)
            NO_RESPONSE_TO_ALLY_INVITATION = SystemMessageId(477)
            YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION = SystemMessageId(478)
            S1_JOINED_AS_FRIEND = SystemMessageId(479)
            PLEASE_CHECK_YOUR_FRIENDS_LIST = SystemMessageId(480)
            S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST = SystemMessageId(481)
            YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIENDS_LIST = SystemMessageId(482)
            FUNCTION_INACCESSIBLE_NOW = SystemMessageId(483)
            S1_ALREADY_IN_FRIENDS_LIST = SystemMessageId(484)
            NO_NEW_INVITATIONS_ACCEPTED = SystemMessageId(485)
            THE_USER_NOT_IN_FRIENDS_LIST = SystemMessageId(486)
            FRIEND_LIST_HEADER = SystemMessageId(487)
            S1_ONLINE = SystemMessageId(488)
            S1_OFFLINE = SystemMessageId(489)
            FRIEND_LIST_FOOTER = SystemMessageId(490)
            ALLIANCE_INFO_HEAD = SystemMessageId(491)
            ALLIANCE_NAME_S1 = SystemMessageId(492)
            CONNECTION_S1_TOTAL_S2 = SystemMessageId(493)
            ALLIANCE_LEADER_S2_OF_S1 = SystemMessageId(494)
            ALLIANCE_CLAN_TOTAL_S1 = SystemMessageId(495)
            CLAN_INFO_HEAD = SystemMessageId(496)
            CLAN_INFO_NAME_S1 = SystemMessageId(497)
            CLAN_INFO_LEADER_S1 = SystemMessageId(498)
            CLAN_INFO_LEVEL_S1 = SystemMessageId(499)
            CLAN_INFO_SEPARATOR = SystemMessageId(500)
            CLAN_INFO_FOOT = SystemMessageId(501)
            ALREADY_JOINED_ALLIANCE = SystemMessageId(502)
            FRIEND_S1_HAS_LOGGED_IN = SystemMessageId(503)
            ONLY_CLAN_LEADER_CREATE_ALLIANCE = SystemMessageId(504)
            CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION = SystemMessageId(505)
            INCORRECT_ALLIANCE_NAME = SystemMessageId(506)
            INCORRECT_ALLIANCE_NAME_LENGTH = SystemMessageId(507)
            ALLIANCE_ALREADY_EXISTS = SystemMessageId(508)
            CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE = SystemMessageId(509)
            YOU_INVITED_FOR_ALLIANCE = SystemMessageId(510)
            SELECT_USER_TO_INVITE = SystemMessageId(511)
            DO_YOU_WISH_TO_WITHDRW = SystemMessageId(512)
            ENTER_NAME_CLAN_TO_EXPEL = SystemMessageId(513)
            DO_YOU_WISH_TO_DISOLVE = SystemMessageId(514)
            SI_INVITED_YOU_AS_FRIEND = SystemMessageId(516)
            YOU_ACCEPTED_ALLIANCE = SystemMessageId(517)
            FAILED_TO_INVITE_CLAN_IN_ALLIANCE = SystemMessageId(518)
            YOU_HAVE_WITHDRAWN_FROM_ALLIANCE = SystemMessageId(519)
            YOU_HAVE_FAILED_TO_WITHDRAWN_FROM_ALLIANCE = SystemMessageId(520)
            YOU_HAVE_EXPELED_A_CLAN = SystemMessageId(521)
            FAILED_TO_EXPELED_A_CLAN = SystemMessageId(522)
            ALLIANCE_DISOLVED = SystemMessageId(523)
            FAILED_TO_DISOLVE_ALLIANCE = SystemMessageId(524)
            YOU_HAVE_SUCCEEDED_INVITING_FRIEND = SystemMessageId(525)
            FAILED_TO_INVITE_A_FRIEND = SystemMessageId(526)
            S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE = SystemMessageId(527)
            SPIRITSHOTS_GRADE_MISMATCH = SystemMessageId(530)
            NOT_ENOUGH_SPIRITSHOTS = SystemMessageId(531)
            CANNOT_USE_SPIRITSHOTS = SystemMessageId(532)
            ENABLED_SPIRITSHOT = SystemMessageId(533)
            DISABLED_SPIRITSHOT = SystemMessageId(534)
            HOW_MUCH_ADENA_TRANSFER = SystemMessageId(536)
            HOW_MUCH_TRANSFER = SystemMessageId(537)
            SP_DECREASED_S1 = SystemMessageId(538)
            EXP_DECREASED_BY_S1 = SystemMessageId(539)
            CLAN_LEADERS_MAY_NOT_BE_DELETED = SystemMessageId(540)
            CLAN_MEMBER_MAY_NOT_BE_DELETED = SystemMessageId(541)
            THE_NPC_SERVER_IS_CURRENTLY_DOWN = SystemMessageId(542)
            YOU_ALREADY_HAVE_A_PET = SystemMessageId(543)
            ITEM_NOT_FOR_PETS = SystemMessageId(544)
            YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS = SystemMessageId(545)
            UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED = SystemMessageId(546)
            SUMMON_A_PET = SystemMessageId(547)
            NAMING_PETNAME_UP_TO_8CHARS = SystemMessageId(548)
            TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER = SystemMessageId(549)
            YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING = SystemMessageId(550)
            CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS = SystemMessageId(551)
            CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS = SystemMessageId(552)
            OPPOSING_CLAN_APPLIED_DISPERSION = SystemMessageId(553)
            CANNOT_DISPERSE_THE_CLANS_IN_ALLY = SystemMessageId(554)
            CANT_MOVE_TOO_ENCUMBERED = SystemMessageId(555)
            CANT_MOVE_IN_THIS_STATE = SystemMessageId(556)
            PET_SUMMONED_MAY_NOT_DESTROYED = SystemMessageId(557)
            PET_SUMMONED_MAY_NOT_LET_GO = SystemMessageId(558)
            PURCHASED_S2_FROM_S1 = SystemMessageId(559)
            PURCHASED_S2_S3_FROM_S1 = SystemMessageId(560)
            PURCHASED_S3_S2_S_FROM_S1 = SystemMessageId(561)
            CRYSTALLIZE_LEVEL_TOO_LOW = SystemMessageId(562)
            FAILED_DISABLE_TARGET = SystemMessageId(563)
            FAILED_CHANGE_TARGET = SystemMessageId(564)
            NOT_ENOUGH_LUCK = SystemMessageId(565)
            CONFUSION_FAILED = SystemMessageId(566)
            FEAR_FAILED = SystemMessageId(567)
            CUBIC_SUMMONING_FAILED = SystemMessageId(568)
            S1_INVITED_YOU_TO_PARTY_FINDERS_KEEPERS = SystemMessageId(572)
            S1_INVITED_YOU_TO_PARTY_RANDOM = SystemMessageId(573)
            PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME = SystemMessageId(574)
            HOW_MUCH_ADENA_TRANSFER_TO_PET = SystemMessageId(575)
            HOW_MUCH_TRANSFER2 = SystemMessageId(576)
            CANNOT_SUMMON_DURING_TRADE_SHOP = SystemMessageId(577)
            YOU_CANNOT_SUMMON_IN_COMBAT = SystemMessageId(578)
            PET_CANNOT_SENT_BACK_DURING_BATTLE = SystemMessageId(579)
            SUMMON_ONLY_ONE = SystemMessageId(580)
            NAMING_THERE_IS_A_SPACE = SystemMessageId(581)
            NAMING_INAPPROPRIATE_CHARACTER_NAME = SystemMessageId(582)
            NAMING_INCLUDES_FORBIDDEN_WORDS = SystemMessageId(583)
            NAMING_ALREADY_IN_USE_BY_ANOTHER_PET = SystemMessageId(584)
            DECIDE_ON_PRICE = SystemMessageId(585)
            PET_NO_SHORTCUT = SystemMessageId(586)
            PET_INVENTORY_FULL = SystemMessageId(588)
            DEAD_PET_CANNOT_BE_RETURNED = SystemMessageId(589)
            CANNOT_GIVE_ITEMS_TO_DEAD_PET = SystemMessageId(590)
            NAMING_PETNAME_CONTAINS_INVALID_CHARS = SystemMessageId(591)
            WISH_TO_DISMISS_PET = SystemMessageId(592)
            STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT = SystemMessageId(593)
            YOU_CANNOT_RESTORE_HUNGRY_PETS = SystemMessageId(594)
            YOUR_PET_IS_VERY_HUNGRY = SystemMessageId(595)
            YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY = SystemMessageId(596)
            YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL = SystemMessageId(597)
            NOT_CHAT_WHILE_INVISIBLE = SystemMessageId(598)
            GM_NOTICE_CHAT_DISABLED = SystemMessageId(599)
            CANNOT_EQUIP_PET_ITEM = SystemMessageId(600)
            S1_PETITION_ON_WAITING_LIST = SystemMessageId(601)
            PETITION_SYSTEM_CURRENT_UNAVAILABLE = SystemMessageId(602)
            CANNOT_DISCARD_EXCHANGE_ITEM = SystemMessageId(603)
            NOT_CALL_PET_FROM_THIS_LOCATION = SystemMessageId(604)
            MAY_REGISTER_UP_TO_64_PEOPLE = SystemMessageId(605)
            OTHER_PERSON_ALREADY_64_PEOPLE = SystemMessageId(606)
            DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1 = SystemMessageId(607)
            S1_SWEEPED_UP_S3_S2 = SystemMessageId(608)
            S1_SWEEPED_UP_S2 = SystemMessageId(609)
            SKILL_REMOVED_DUE_LACK_HP = SystemMessageId(610)
            CONFUSING_SUCCEEDED = SystemMessageId(611)
            SPOIL_SUCCESS = SystemMessageId(612)
            BLOCK_LIST_HEADER = SystemMessageId(613)
            S1_S2 = SystemMessageId(614)
            FAILED_TO_REGISTER_TO_IGNORE_LIST = SystemMessageId(615)
            FAILED_TO_DELETE_CHARACTER = SystemMessageId(616)
            S1_WAS_ADDED_TO_YOUR_IGNORE_LIST = SystemMessageId(617)
            S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST = SystemMessageId(618)
            S1_HAS_ADDED_YOU_TO_IGNORE_LIST = SystemMessageId(619)
            S1_HAS_ADDED_YOU_TO_IGNORE_LIST2 = SystemMessageId(620)
            CONNECTION_RESTRICTED_IP = SystemMessageId(621)
            NO_WAR_DURING_ALLY_BATTLE = SystemMessageId(622)
            OPPONENT_TOO_MUCH_ALLY_BATTLES1 = SystemMessageId(623)
            S1_LEADER_NOT_CONNECTED = SystemMessageId(624)
            ALLY_BATTLE_TRUCE_DENIED = SystemMessageId(625)
            WAR_PROCLAMATION_HAS_BEEN_REFUSED = SystemMessageId(626)
            YOU_REFUSED_CLAN_WAR_PROCLAMATION = SystemMessageId(627)
            ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS = SystemMessageId(628)
            OPPONENT_TOO_MUCH_ALLY_BATTLES2 = SystemMessageId(629)
            WAR_WITH_CLAN_BEGUN = SystemMessageId(630)
            WAR_WITH_CLAN_ENDED = SystemMessageId(631)
            WON_WAR_OVER_CLAN = SystemMessageId(632)
            SURRENDERED_TO_CLAN = SystemMessageId(633)
            DEFEATED_BY_CLAN = SystemMessageId(634)
            TIME_UP_WAR_OVER = SystemMessageId(635)
            NOT_INVOLVED_IN_WAR = SystemMessageId(636)
            ALLY_REGISTERED_SELF_TO_OPPONENT = SystemMessageId(637)
            ALREADY_REQUESTED_SIEGE_BATTLE = SystemMessageId(638)
            APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE = SystemMessageId(639)
            ALREADY_ATTACKER_NOT_CANCEL = SystemMessageId(642)
            ALREADY_DEFENDER_NOT_CANCEL = SystemMessageId(643)
            NOT_REGISTERED_FOR_SIEGE = SystemMessageId(644)
            ONLY_CLAN_LEVEL_4_ABOVE_MAY_SIEGE = SystemMessageId(645)
            ATTACKER_SIDE_FULL = SystemMessageId(648)
            DEFENDER_SIDE_FULL = SystemMessageId(649)
            YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION = SystemMessageId(650)
            PLACE_S1_IN_CURRENT_LOCATION_AND_DIRECTION = SystemMessageId(651)
            TARGET_OF_SUMMON_WRONG = SystemMessageId(652)
            YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES = SystemMessageId(653)
            YOU_DO_NOT_HAVE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING = SystemMessageId(654)
            MERCENARIES_CANNOT_BE_POSITIONED_HERE = SystemMessageId(655)
            THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE = SystemMessageId(656)
            POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT = SystemMessageId(657)
            THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING = SystemMessageId(658)
            NOT_SIEGE_REGISTRATION_TIME1 = SystemMessageId(659)
            NOT_SIEGE_REGISTRATION_TIME2 = SystemMessageId(660)
            SPOIL_CANNOT_USE = SystemMessageId(661)
            THE_PLAYER_IS_REJECTING_FRIEND_INVITATIONS = SystemMessageId(662)
            CHOOSE_PERSON_TO_RECEIVE = SystemMessageId(664)
            APPLYING_ALLIANCE_WAR = SystemMessageId(665)
            REQUEST_FOR_CEASEFIRE = SystemMessageId(666)
            REGISTERING_ON_ATTACKING_SIDE = SystemMessageId(667)
            REGISTERING_ON_DEFENDING_SIDE = SystemMessageId(668)
            CANCELING_REGISTRATION = SystemMessageId(669)
            REFUSING_REGISTRATION = SystemMessageId(670)
            AGREEING_REGISTRATION = SystemMessageId(671)
            S1_DISAPPEARED_ADENA = SystemMessageId(672)
            AUCTION_ONLY_CLAN_LEVEL_2_HIGHER = SystemMessageId(673)
            NOT_SEVEN_DAYS_SINCE_CANCELING_AUCTION = SystemMessageId(674)
            NO_CLAN_HALLS_UP_FOR_AUCTION = SystemMessageId(675)
            ALREADY_SUBMITTED_BID = SystemMessageId(676)
            BID_PRICE_MUST_BE_HIGHER = SystemMessageId(677)
            SUBMITTED_A_BID = SystemMessageId(678)
            CANCELED_BID = SystemMessageId(679)
            CANNOT_PARTICIPATE_IN_AUCTION = SystemMessageId(680)
            SWEEP_NOT_ALLOWED = SystemMessageId(683)
            CANNOT_POSITION_MERCS_DURING_SIEGE = SystemMessageId(684)
            CANNOT_DECLARE_WAR_ON_ALLY = SystemMessageId(685)
            S1_DAMAGE_FROM_FIRE_MAGIC = SystemMessageId(686)
            CANNOT_MOVE_FROZEN = SystemMessageId(687)
            CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING = SystemMessageId(688)
            CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE = SystemMessageId(689)
            CANNOT_ATTACK_ALLIANCE_CASTLE = SystemMessageId(690)
            S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE = SystemMessageId(691)
            OTHER_PARTY_IS_FROZEN = SystemMessageId(692)
            PACKAGE_IN_ANOTHER_WAREHOUSE = SystemMessageId(693)
            NO_PACKAGES_ARRIVED = SystemMessageId(694)
            NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET = SystemMessageId(695)
            ITEM_ENCHANT_VALUE_STRANGE = SystemMessageId(697)
            PRICE_DIFFERENT_FROM_SALES_LIST = SystemMessageId(698)
            CURRENTLY_NOT_PURCHASING = SystemMessageId(699)
            THE_PURCHASE_IS_COMPLETE = SystemMessageId(700)
            NOT_ENOUGH_REQUIRED_ITEMS = SystemMessageId(701)
            NO_GM_PROVIDING_SERVICE_NOW = SystemMessageId(702)
            GM_LIST = SystemMessageId(703)
            GM_S1 = SystemMessageId(704)
            CANNOT_EXCLUDE_SELF = SystemMessageId(705)
            ONLY_64_NAMES_ON_EXCLUDE_LIST = SystemMessageId(706)
            CANNOT_PORT_VILLAGE_IN_SIEGE = SystemMessageId(707)
            YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CASTLE_WAREHOUSE = SystemMessageId(708)
            YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE = SystemMessageId(709)
            ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE = SystemMessageId(710)
            SIEGE_OF_S1_HAS_STARTED = SystemMessageId(711)
            SIEGE_OF_S1_HAS_ENDED = SystemMessageId(712)
            S1_S2_S3_D = SystemMessageId(713)
            A_TRAP_DEVICE_HAS_BEEN_TRIPPED = SystemMessageId(714)
            A_TRAP_DEVICE_HAS_BEEN_STOPPED = SystemMessageId(715)
            NO_RESURRECTION_WITHOUT_BASE_CAMP = SystemMessageId(716)
            TOWER_DESTROYED_NO_RESURRECTION = SystemMessageId(717)
            GATES_NOT_OPENED_CLOSED_DURING_SIEGE = SystemMessageId(718)
            ITEM_MIXING_FAILED = SystemMessageId(719)
            THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY = SystemMessageId(720)
            NO_ALLY_CREATION_WHILE_SIEGE = SystemMessageId(721)
            CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE = SystemMessageId(722)
            OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE = SystemMessageId(723)
            CANNOT_LEAVE_WHILE_SIEGE = SystemMessageId(724)
            CANNOT_DISMISS_WHILE_SIEGE = SystemMessageId(725)
            FROZEN_CONDITION_STARTED = SystemMessageId(726)
            FROZEN_CONDITION_REMOVED = SystemMessageId(727)
            CANNOT_APPLY_DISSOLUTION_AGAIN = SystemMessageId(728)
            ITEM_NOT_DISCARDED = SystemMessageId(729)
            SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT = SystemMessageId(730)
            PETITION_S1_RECEIVED_CODE_IS_S2 = SystemMessageId(731)
            S1_RECEIVED_CONSULTATION_REQUEST = SystemMessageId(732)
            WE_HAVE_RECEIVED_S1_PETITIONS_TODAY = SystemMessageId(733)
            PETITION_FAILED_S1_ALREADY_SUBMITTED = SystemMessageId(734)
            PETITION_FAILED_FOR_S1_ERROR_NUMBER_S2 = SystemMessageId(735)
            PETITION_CANCELED_SUBMIT_S1_MORE_TODAY = SystemMessageId(736)
            CANCELED_PETITION_ON_S1 = SystemMessageId(737)
            PETITION_NOT_SUBMITTED = SystemMessageId(738)
            PETITION_CANCEL_FAILED_FOR_S1_ERROR_NUMBER_S2 = SystemMessageId(739)
            S1_PARTICIPATE_PETITION = SystemMessageId(740)
            FAILED_ADDING_S1_TO_PETITION = SystemMessageId(741)
            PETITION_ADDING_S1_FAILED_ERROR_NUMBER_S2 = SystemMessageId(742)
            S1_LEFT_PETITION_CHAT = SystemMessageId(743)
            PETITION_REMOVING_S1_FAILED_ERROR_NUMBER_S2 = SystemMessageId(744)
            YOU_ARE_NOT_IN_PETITION_CHAT = SystemMessageId(745)
            CURRENTLY_NO_PETITION = SystemMessageId(746)
            DIST_TOO_FAR_CASTING_STOPPED = SystemMessageId(748)
            EFFECT_S1_DISAPPEARED = SystemMessageId(749)
            NO_MORE_SKILLS_TO_LEARN = SystemMessageId(750)
            CANNOT_INVITE_CONFLICT_CLAN = SystemMessageId(751)
            CANNOT_USE_NAME = SystemMessageId(752)
            NO_MERCS_HERE = SystemMessageId(753)
            S1_HOURS_S2_MINUTES_LEFT_THIS_WEEK = SystemMessageId(754)
            S1_MINUTES_LEFT_THIS_WEEK = SystemMessageId(755)
            WEEKS_USAGE_TIME_FINISHED = SystemMessageId(756)
            S1_HOURS_S2_MINUTES_LEFT_IN_TIME = SystemMessageId(757)
            S1_HOURS_S2_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME = SystemMessageId(758)
            S1_MINUTES_LEFT_THIS_WEEKS_PLAY_TIME = SystemMessageId(759)
            S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN = SystemMessageId(760)
            S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY = SystemMessageId(761)
            S1_ROLLED_S2_S3_EYE_CAME_OUT = SystemMessageId(762)
            FAILED_SENDING_PACKAGE_TOO_FAR = SystemMessageId(763)
            PLAYING_FOR_LONG_TIME = SystemMessageId(764)
            HACKING_TOOL = SystemMessageId(769)
            PLAY_TIME_NO_LONGER_ACCUMULATING = SystemMessageId(774)
            PLAY_TIME_EXPENDED = SystemMessageId(775)
            CLANHALL_AWARDED_TO_CLAN_S1 = SystemMessageId(776)
            CLANHALL_NOT_SOLD = SystemMessageId(777)
            NO_LOGOUT_HERE = SystemMessageId(778)
            NO_RESTART_HERE = SystemMessageId(779)
            ONLY_VIEW_SIEGE = SystemMessageId(780)
            OBSERVERS_CANNOT_PARTICIPATE = SystemMessageId(781)
            NO_OBSERVE_WITH_PET = SystemMessageId(782)
            LOTTERY_TICKET_SALES_TEMP_SUSPENDED = SystemMessageId(783)
            NO_LOTTERY_TICKETS_AVAILABLE = SystemMessageId(784)
            LOTTERY_S1_RESULT_NOT_PUBLISHED = SystemMessageId(785)
            INCORRECT_SYNTAX = SystemMessageId(786)
            CLANHALL_SIEGE_TRYOUTS_FINISHED = SystemMessageId(787)
            CLANHALL_SIEGE_FINALS_FINISHED = SystemMessageId(788)
            CLANHALL_SIEGE_TRYOUTS_BEGUN = SystemMessageId(789)
            CLANHALL_SIEGE_FINALS_BEGUN = SystemMessageId(790)
            FINAL_MATCH_BEGIN = SystemMessageId(791)
            CLANHALL_SIEGE_ENDED = SystemMessageId(792)
            CLANHALL_SIEGE_BEGUN = SystemMessageId(793)
            YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT = SystemMessageId(794)
            ONLY_LEADERS_CAN_SET_RIGHTS = SystemMessageId(795)
            REMAINING_OBSERVATION_TIME = SystemMessageId(796)
            YOU_MAY_CREATE_UP_TO_24_MACROS = SystemMessageId(797)
            ITEM_REGISTRATION_IRREVERSIBLE = SystemMessageId(798)
            OBSERVATION_TIME_EXPIRED = SystemMessageId(799)
            REGISTRATION_PERIOD_OVER = SystemMessageId(800)
            REGISTRATION_CLOSED = SystemMessageId(801)
            PETITION_NOT_ACCEPTED_NOW = SystemMessageId(802)
            PETITION_NOT_SPECIFIED = SystemMessageId(803)
            SELECT_TYPE = SystemMessageId(804)
            PETITION_NOT_ACCEPTED_SUBMIT_AT_S1 = SystemMessageId(805)
            TRY_UNSTUCK_WHEN_TRAPPED = SystemMessageId(806)
            STUCK_PREPARE_FOR_TRANSPORT = SystemMessageId(807)
            STUCK_SUBMIT_PETITION = SystemMessageId(808)
            STUCK_TRANSPORT_IN_FIVE_MINUTES = SystemMessageId(809)
            INVALID_MACRO = SystemMessageId(810)
            WILL_BE_MOVED = SystemMessageId(811)
            TRAP_DID_S1_DAMAGE = SystemMessageId(812)
            POISONED_BY_TRAP = SystemMessageId(813)
            SLOWED_BY_TRAP = SystemMessageId(814)
            TRYOUTS_ABOUT_TO_BEGIN = SystemMessageId(815)
            MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE = SystemMessageId(816)
            MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE = SystemMessageId(817)
            MONSRACE_TICKETS_STOP_IN_S1_MINUTES = SystemMessageId(818)
            MONSRACE_S1_TICKET_SALES_CLOSED = SystemMessageId(819)
            MONSRACE_S2_BEGINS_IN_S1_MINUTES = SystemMessageId(820)
            MONSRACE_S1_BEGINS_IN_30_SECONDS = SystemMessageId(821)
            MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS = SystemMessageId(822)
            MONSRACE_BEGINS_IN_S1_SECONDS = SystemMessageId(823)
            MONSRACE_RACE_START = SystemMessageId(824)
            MONSRACE_S1_RACE_END = SystemMessageId(825)
            MONSRACE_FIRST_PLACE_S1_SECOND_S2 = SystemMessageId(826)
            YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM = SystemMessageId(827)
            WISH_TO_DELETE_S1_MACRO = SystemMessageId(828)
            YOU_CANNOT_RECOMMEND_YOURSELF = SystemMessageId(829)
            YOU_HAVE_RECOMMENDED_S1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT = SystemMessageId(830)
            YOU_HAVE_BEEN_RECOMMENDED_BY_S1 = SystemMessageId(831)
            THAT_CHARACTER_IS_RECOMMENDED = SystemMessageId(832)
            NO_MORE_RECOMMENDATIONS_TO_HAVE = SystemMessageId(833)
            S1_ROLLED_S2 = SystemMessageId(834)
            YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER = SystemMessageId(835)
            YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM = SystemMessageId(836)
            MACRO_DESCRIPTION_MAX_32_CHARS = SystemMessageId(837)
            ENTER_THE_MACRO_NAME = SystemMessageId(838)
            MACRO_NAME_ALREADY_USED = SystemMessageId(839)
            RECIPE_ALREADY_REGISTERED = SystemMessageId(840)
            NO_FUTHER_RECIPES_CAN_BE_ADDED = SystemMessageId(841)
            NOT_AUTHORIZED_REGISTER_RECIPE = SystemMessageId(842)
            SIEGE_OF_S1_FINISHED = SystemMessageId(843)
            SIEGE_OF_S1_BEGUN = SystemMessageId(844)
            DEADLINE_FOR_SIEGE_S1_PASSED = SystemMessageId(845)
            SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST = SystemMessageId(846)
            CLAN_OWNING_CLANHALL_MAY_NOT_SIEGE_CLANHALL = SystemMessageId(847)
            S1_HAS_BEEN_DELETED = SystemMessageId(848)
            S1_NOT_FOUND = SystemMessageId(849)
            S1_ALREADY_EXISTS2 = SystemMessageId(850)
            S1_ADDED = SystemMessageId(851)
            RECIPE_INCORRECT = SystemMessageId(852)
            CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING = SystemMessageId(853)
            MISSING_S2_S1_TO_CREATE = SystemMessageId(854)
            S1_CLAN_DEFEATED_S2 = SystemMessageId(855)
            SIEGE_S1_DRAW = SystemMessageId(856)
            S1_CLAN_WON_MATCH_S2 = SystemMessageId(857)
            MATCH_OF_S1_DRAW = SystemMessageId(858)
            PLEASE_REGISTER_RECIPE = SystemMessageId(859)
            HEADQUARTERS_TOO_CLOSE = SystemMessageId(860)
            TOO_MANY_MEMOS = SystemMessageId(861)
            ODDS_NOT_POSTED = SystemMessageId(862)
            FEEL_ENERGY_FIRE = SystemMessageId(863)
            FEEL_ENERGY_WATER = SystemMessageId(864)
            FEEL_ENERGY_WIND = SystemMessageId(865)
            NO_LONGER_ENERGY = SystemMessageId(866)
            ENERGY_DEPLETED = SystemMessageId(867)
            ENERGY_FIRE_DELIVERED = SystemMessageId(868)
            ENERGY_WATER_DELIVERED = SystemMessageId(869)
            ENERGY_WIND_DELIVERED = SystemMessageId(870)
            THE_SEED_HAS_BEEN_SOWN = SystemMessageId(871)
            THIS_SEED_MAY_NOT_BE_SOWN_HERE = SystemMessageId(872)
            CHARACTER_DOES_NOT_EXIST = SystemMessageId(873)
            WAREHOUSE_CAPACITY_EXCEEDED = SystemMessageId(874)
            CARGO_CANCELED = SystemMessageId(875)
            CARGO_NOT_DELIVERED = SystemMessageId(876)
            SYMBOL_ADDED = SystemMessageId(877)
            SYMBOL_DELETED = SystemMessageId(878)
            THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE = SystemMessageId(879)
            THE_TRANSACTION_IS_COMPLETE = SystemMessageId(880)
            THERE_IS_A_DISCREPANCY_ON_THE_INVOICE = SystemMessageId(881)
            THE_SEED_QUANTITY_IS_INCORRECT = SystemMessageId(882)
            THE_SEED_INFORMATION_IS_INCORRECT = SystemMessageId(883)
            THE_MANOR_INFORMATION_HAS_BEEN_UPDATED = SystemMessageId(884)
            THE_NUMBER_OF_CROPS_IS_INCORRECT = SystemMessageId(885)
            THE_CROPS_ARE_PRICED_INCORRECTLY = SystemMessageId(886)
            THE_TYPE_IS_INCORRECT = SystemMessageId(887)
            NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME = SystemMessageId(888)
            THE_SEED_WAS_SUCCESSFULLY_SOWN = SystemMessageId(889)
            THE_SEED_WAS_NOT_SOWN = SystemMessageId(890)
            YOU_ARE_NOT_AUTHORIZED_TO_HARVEST = SystemMessageId(891)
            THE_HARVEST_HAS_FAILED = SystemMessageId(892)
            THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN = SystemMessageId(893)
            UP_TO_S1_RECIPES_CAN_REGISTER = SystemMessageId(894)
            NO_RECIPES_REGISTERED = SystemMessageId(895)
            FERRY_AT_GLUDIN = SystemMessageId(896)
            FERRY_LEAVE_TALKING = SystemMessageId(897)
            ONLY_LEVEL_SUP_10_CAN_RECOMMEND = SystemMessageId(898)
            CANT_DRAW_SYMBOL = SystemMessageId(899)
            SYMBOLS_FULL = SystemMessageId(900)
            SYMBOL_NOT_FOUND = SystemMessageId(901)
            NUMBER_INCORRECT = SystemMessageId(902)
            NO_PETITION_WHILE_FROZEN = SystemMessageId(903)
            NO_DISCARD_WHILE_PRIVATE_STORE = SystemMessageId(904)
            HUMAN_SCORE_S1 = SystemMessageId(905)
            ELVES_SCORE_S1 = SystemMessageId(906)
            DARK_ELVES_SCORE_S1 = SystemMessageId(907)
            ORCS_SCORE_S1 = SystemMessageId(908)
            DWARVEN_SCORE_S1 = SystemMessageId(909)
            LOC_TI_S1_S2_S3 = SystemMessageId(910)
            LOC_GLUDIN_S1_S2_S3 = SystemMessageId(911)
            LOC_GLUDIO_S1_S2_S3 = SystemMessageId(912)
            LOC_NEUTRAL_ZONE_S1_S2_S3 = SystemMessageId(913)
            LOC_ELVEN_S1_S2_S3 = SystemMessageId(914)
            LOC_DARK_ELVEN_S1_S2_S3 = SystemMessageId(915)
            LOC_DION_S1_S2_S3 = SystemMessageId(916)
            LOC_FLORAN_S1_S2_S3 = SystemMessageId(917)
            LOC_GIRAN_S1_S2_S3 = SystemMessageId(918)
            LOC_GIRAN_HARBOR_S1_S2_S3 = SystemMessageId(919)
            LOC_ORC_S1_S2_S3 = SystemMessageId(920)
            LOC_DWARVEN_S1_S2_S3 = SystemMessageId(921)
            LOC_OREN_S1_S2_S3 = SystemMessageId(922)
            LOC_HUNTER_S1_S2_S3 = SystemMessageId(923)
            LOC_ADEN_S1_S2_S3 = SystemMessageId(924)
            LOC_COLISEUM_S1_S2_S3 = SystemMessageId(925)
            LOC_HEINE_S1_S2_S3 = SystemMessageId(926)
            TIME_S1_S2_IN_THE_DAY = SystemMessageId(927)
            TIME_S1_S2_IN_THE_NIGHT = SystemMessageId(928)
            NO_COMPENSATION_FOR_FARM_PRODUCTS = SystemMessageId(929)
            NO_LOTTERY_TICKETS_CURRENT_SOLD = SystemMessageId(930)
            LOTTERY_WINNERS_NOT_ANNOUNCED_YET = SystemMessageId(931)
            NO_ALLCHAT_WHILE_OBSERVING = SystemMessageId(932)
            THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES = SystemMessageId(933)
            A_DELETED_RECIPE = SystemMessageId(934)
            THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION = SystemMessageId(935)
            USE_S1_ = SystemMessageId(936)
            PREPARING_PRIVATE_WORKSHOP = SystemMessageId(937)
            CB_OFFLINE = SystemMessageId(938)
            NO_EXCHANGE_WHILE_BLOCKING = SystemMessageId(939)
            S1_BLOCKED_EVERYTHING = SystemMessageId(940)
            RESTART_AT_TI = SystemMessageId(941)
            RESTART_AT_GLUDIN = SystemMessageId(942)
            RESTART_AT_GLUDIO = SystemMessageId(943)
            RESTART_AT_NEUTRAL_ZONE = SystemMessageId(944)
            RESTART_AT_ELFEN_VILLAGE = SystemMessageId(945)
            RESTART_AT_DARKELF_VILLAGE = SystemMessageId(946)
            RESTART_AT_DION = SystemMessageId(947)
            RESTART_AT_FLORAN = SystemMessageId(948)
            RESTART_AT_GIRAN = SystemMessageId(949)
            RESTART_AT_GIRAN_HARBOR = SystemMessageId(950)
            RESTART_AT_ORC_VILLAGE = SystemMessageId(951)
            RESTART_AT_DWARFEN_VILLAGE = SystemMessageId(952)
            RESTART_AT_OREN = SystemMessageId(953)
            RESTART_AT_HUNTERS_VILLAGE = SystemMessageId(954)
            RESTART_AT_ADEN = SystemMessageId(955)
            RESTART_AT_COLISEUM = SystemMessageId(956)
            RESTART_AT_HEINE = SystemMessageId(957)
            ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP = SystemMessageId(958)
            S1_S2_MANUFACTURED_SUCCESSFULLY = SystemMessageId(959)
            S1_MANUFACTURE_FAILURE = SystemMessageId(960)
            BLOCKING_ALL = SystemMessageId(961)
            NOT_BLOCKING_ALL = SystemMessageId(962)
            DETERMINE_MANUFACTURE_PRICE = SystemMessageId(963)
            CHATBAN_FOR_1_MINUTE = SystemMessageId(964)
            CHATBAN_REMOVED = SystemMessageId(965)
            CHATTING_IS_CURRENTLY_PROHIBITED = SystemMessageId(966)
            S1_PARTY_INVITE_RANDOM_INCLUDING_SPOIL = SystemMessageId(967)
            S1_PARTY_INVITE_BY_TURN = SystemMessageId(968)
            S1_PARTY_INVITE_BY_TURN_INCLUDING_SPOIL = SystemMessageId(969)
            S2_MP_HAS_BEEN_DRAINED_BY_S1 = SystemMessageId(970)
            PETITION_MAX_CHARS_255 = SystemMessageId(971)
            PET_CANNOT_USE_ITEM = SystemMessageId(972)
            INPUT_NO_MORE_YOU_HAVE = SystemMessageId(973)
            SOUL_CRYSTAL_ABSORBING_SUCCEEDED = SystemMessageId(974)
            SOUL_CRYSTAL_ABSORBING_FAILED = SystemMessageId(975)
            SOUL_CRYSTAL_BROKE = SystemMessageId(976)
            SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION = SystemMessageId(977)
            SOUL_CRYSTAL_ABSORBING_REFUSED = SystemMessageId(978)
            FERRY_ARRIVED_AT_TALKING = SystemMessageId(979)
            FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES = SystemMessageId(980)
            FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES = SystemMessageId(981)
            FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE = SystemMessageId(982)
            MAKE_HASTE_GET_ON_BOAT = SystemMessageId(983)
            FERRY_LEAVE_SOON_FOR_GLUDIN = SystemMessageId(984)
            FERRY_LEAVING_FOR_GLUDIN = SystemMessageId(985)
            FERRY_ARRIVED_AT_GLUDIN = SystemMessageId(986)
            FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES = SystemMessageId(987)
            FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES = SystemMessageId(988)
            FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE = SystemMessageId(989)
            FERRY_LEAVE_SOON_FOR_TALKING = SystemMessageId(990)
            FERRY_LEAVING_FOR_TALKING = SystemMessageId(991)
            FERRY_ARRIVED_AT_GIRAN = SystemMessageId(992)
            FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES = SystemMessageId(993)
            FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES = SystemMessageId(994)
            FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE = SystemMessageId(995)
            FERRY_LEAVE_SOON_FOR_GIRAN = SystemMessageId(996)
            FERRY_LEAVING_FOR_GIRAN = SystemMessageId(997)
            INNADRIL_BOAT_ANCHOR_10_MINUTES = SystemMessageId(998)
            INNADRIL_BOAT_LEAVE_IN_5_MINUTES = SystemMessageId(999)
            INNADRIL_BOAT_LEAVE_IN_1_MINUTE = SystemMessageId(1000)
            INNADRIL_BOAT_LEAVE_SOON = SystemMessageId(1001)
            INNADRIL_BOAT_LEAVING = SystemMessageId(1002)
            CANNOT_POSSES_MONS_TICKET = SystemMessageId(1003)
            REGISTERED_FOR_CLANHALL = SystemMessageId(1004)
            NOT_ENOUGH_ADENA_IN_CWH = SystemMessageId(1005)
            BID_IN_CLANHALL_AUCTION = SystemMessageId(1006)
            PRELIMINARY_REGISTRATION_OF_S1_FINISHED = SystemMessageId(1007)
            HUNGRY_STRIDER_NOT_MOUNT = SystemMessageId(1008)
            STRIDER_CANT_BE_RIDDEN_WHILE_DEAD = SystemMessageId(1009)
            DEAD_STRIDER_CANT_BE_RIDDEN = SystemMessageId(1010)
            STRIDER_IN_BATLLE_CANT_BE_RIDDEN = SystemMessageId(1011)
            STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE = SystemMessageId(1012)
            STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING = SystemMessageId(1013)
            PET_EARNED_S1_EXP = SystemMessageId(1014)
            PET_HIT_FOR_S1_DAMAGE = SystemMessageId(1015)
            PET_RECEIVED_S2_DAMAGE_BY_S1 = SystemMessageId(1016)
            CRITICAL_HIT_BY_PET = SystemMessageId(1017)
            PET_USES_S1 = SystemMessageId(1018)
            PET_USES_S1_ = SystemMessageId(1019)
            PET_PICKED_S1 = SystemMessageId(1020)
            PET_PICKED_S2_S1_S = SystemMessageId(1021)
            PET_PICKED_S1_S2 = SystemMessageId(1022)
            PET_PICKED_S1_ADENA = SystemMessageId(1023)
            PET_PUT_ON_S1 = SystemMessageId(1024)
            PET_TOOK_OFF_S1 = SystemMessageId(1025)
            SUMMON_GAVE_DAMAGE_S1 = SystemMessageId(1026)
            SUMMON_RECEIVED_DAMAGE_S2_BY_S1 = SystemMessageId(1027)
            CRITICAL_HIT_BY_SUMMONED_MOB = SystemMessageId(1028)
            SUMMONED_MOB_USES_S1 = SystemMessageId(1029)
            PARTY_INFORMATION = SystemMessageId(1030)
            LOOTING_FINDERS_KEEPERS = SystemMessageId(1031)
            LOOTING_RANDOM = SystemMessageId(1032)
            LOOTING_RANDOM_INCLUDE_SPOIL = SystemMessageId(1033)
            LOOTING_BY_TURN = SystemMessageId(1034)
            LOOTING_BY_TURN_INCLUDE_SPOIL = SystemMessageId(1035)
            YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED = SystemMessageId(1036)
            S1_MANUFACTURED_S2 = SystemMessageId(1037)
            S1_MANUFACTURED_S3_S2_S = SystemMessageId(1038)
            ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE = SystemMessageId(1039)
            ITEMS_SENT_BY_FREIGHT_PICKED_UP_FROM_ANYWHERE = SystemMessageId(1040)
            THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA = SystemMessageId(1041)
            THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA = SystemMessageId(1042)
            NO_UNSTUCK_PLEASE_SEND_PETITION = SystemMessageId(1043)
            MONSRACE_NO_PAYOUT_INFO = SystemMessageId(1044)
            MONSRACE_TICKETS_NOT_AVAILABLE = SystemMessageId(1046)
            NOT_SUCCEED_PRODUCING_S1 = SystemMessageId(1047)
            NO_WHISPER_WHEN_BLOCKING = SystemMessageId(1048)
            NO_PARTY_WHEN_BLOCKING = SystemMessageId(1049)
            NO_CB_IN_MY_CLAN = SystemMessageId(1050)
            PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW =
                SystemMessageId(1051)
            THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED =
                SystemMessageId(1052)
            CANNOT_BE_RESURRECTED_DURING_SIEGE = SystemMessageId(1053)
            ENTERED_MYSTICAL_LAND = SystemMessageId(1054)
            EXITED_MYSTICAL_LAND = SystemMessageId(1055)
            VAULT_CAPACITY_EXCEEDED = SystemMessageId(1056)
            RELAX_SERVER_ONLY = SystemMessageId(1057)
            THE_SALES_PRICE_FOR_SEEDS_IS_S1_ADENA = SystemMessageId(1058)
            THE_REMAINING_PURCHASING_IS_S1_ADENA = SystemMessageId(1059)
            THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1 = SystemMessageId(1060)
            CANT_REGISTER_NO_ABILITY_TO_CRAFT = SystemMessageId(1061)
            WRITING_SOMETHING_NEW_POSSIBLE_AFTER_LEVEL_10 = SystemMessageId(1062)
            PETITION_UNAVAILABLE = SystemMessageId(1063)
            EQUIPMENT_S1_S2_REMOVED = SystemMessageId(1064)
            CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE = SystemMessageId(1065)
            S1_HP_RESTORED = SystemMessageId(1066)
            S2_HP_RESTORED_BY_S1 = SystemMessageId(1067)
            S1_MP_RESTORED = SystemMessageId(1068)
            S2_MP_RESTORED_BY_S1 = SystemMessageId(1069)
            NO_READ_PERMISSION = SystemMessageId(1070)
            NO_WRITE_PERMISSION = SystemMessageId(1071)
            OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE = SystemMessageId(1072)
            OBTAINED_TICKET_FOR_MONS_RACE_S1_SINGLE_ = SystemMessageId(1073)
            NOT_MEET_AGE_REQUIREMENT_FOR_MONS_RACE = SystemMessageId(1074)
            BID_AMOUNT_HIGHER_THAN_PREVIOUS_BID = SystemMessageId(1075)
            GAME_CANNOT_TERMINATE_NOW = SystemMessageId(1076)
            GG_EXECUTION_ERROR = SystemMessageId(1077)
            DONT_SPAM = SystemMessageId(1078)
            TARGET_IS_CHAT_BANNED = SystemMessageId(1079)
            FACELIFT_POTION_TYPE_A = SystemMessageId(1080)
            HAIRDYE_POTION_TYPE_A = SystemMessageId(1081)
            HAIRSTYLE_POTION_TYPE_A = SystemMessageId(1082)
            FACELIFT_POTION_TYPE_A_APPLIED = SystemMessageId(1083)
            HAIRDYE_POTION_TYPE_A_APPLIED = SystemMessageId(1084)
            HAIRSTYLE_POTION_TYPE_A_USED = SystemMessageId(1085)
            FACE_APPEARANCE_CHANGED = SystemMessageId(1086)
            HAIR_COLOR_CHANGED = SystemMessageId(1087)
            HAIR_STYLE_CHANGED = SystemMessageId(1088)
            S1_OBTAINED_ANNIVERSARY_ITEM = SystemMessageId(1089)
            FACELIFT_POTION_TYPE_B = SystemMessageId(1090)
            FACELIFT_POTION_TYPE_C = SystemMessageId(1091)
            HAIRDYE_POTION_TYPE_B = SystemMessageId(1092)
            HAIRDYE_POTION_TYPE_C = SystemMessageId(1093)
            HAIRDYE_POTION_TYPE_D = SystemMessageId(1094)
            HAIRSTYLE_POTION_TYPE_B = SystemMessageId(1095)
            HAIRSTYLE_POTION_TYPE_C = SystemMessageId(1096)
            HAIRSTYLE_POTION_TYPE_D = SystemMessageId(1097)
            HAIRSTYLE_POTION_TYPE_E = SystemMessageId(1098)
            HAIRSTYLE_POTION_TYPE_F = SystemMessageId(1099)
            HAIRSTYLE_POTION_TYPE_G = SystemMessageId(1100)
            FACELIFT_POTION_TYPE_B_APPLIED = SystemMessageId(1101)
            FACELIFT_POTION_TYPE_C_APPLIED = SystemMessageId(1102)
            HAIRDYE_POTION_TYPE_B_APPLIED = SystemMessageId(1103)
            HAIRDYE_POTION_TYPE_C_APPLIED = SystemMessageId(1104)
            HAIRDYE_POTION_TYPE_D_APPLIED = SystemMessageId(1105)
            HAIRSTYLE_POTION_TYPE_B_USED = SystemMessageId(1106)
            HAIRSTYLE_POTION_TYPE_C_USED = SystemMessageId(1107)
            HAIRSTYLE_POTION_TYPE_D_USED = SystemMessageId(1108)
            HAIRSTYLE_POTION_TYPE_E_USED = SystemMessageId(1109)
            HAIRSTYLE_POTION_TYPE_F_USED = SystemMessageId(1110)
            HAIRSTYLE_POTION_TYPE_G_USED = SystemMessageId(1111)
            AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER = SystemMessageId(1112)
            AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER = SystemMessageId(1113)
            CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS = SystemMessageId(1114)
            INDIVIDUALS_NOT_SURRENDER_DURING_COMBAT = SystemMessageId(1115)
            YOU_CANNOT_LEAVE_DURING_COMBAT = SystemMessageId(1116)
            CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT = SystemMessageId(1117)
            INVENTORY_LESS_THAN_80_PERCENT = SystemMessageId(1118)
            QUEST_CANCELED_INVENTORY_EXCEEDS_80_PERCENT = SystemMessageId(1119)
            STILL_CLAN_MEMBER = SystemMessageId(1120)
            NO_RIGHT_TO_VOTE = SystemMessageId(1121)
            NO_CANDIDATE = SystemMessageId(1122)
            WEIGHT_EXCEEDED_SKILL_UNAVAILABLE = SystemMessageId(1123)
            NO_RECIPE_BOOK_WHILE_CASTING = SystemMessageId(1124)
            CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING = SystemMessageId(1125)
            NO_NEGATIVE_NUMBER = SystemMessageId(1126)
            REWARD_LESS_THAN_10_TIMES_STANDARD_PRICE = SystemMessageId(1127)
            PRIVATE_STORE_NOT_WHILE_CASTING = SystemMessageId(1128)
            NOT_ALLOWED_ON_BOAT = SystemMessageId(1129)
            GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR = SystemMessageId(1130)
            NIGHT_S1_EFFECT_APPLIES = SystemMessageId(1131)
            DAY_S1_EFFECT_DISAPPEARS = SystemMessageId(1132)
            HP_DECREASED_EFFECT_APPLIES = SystemMessageId(1133)
            HP_INCREASED_EFFECT_DISAPPEARS = SystemMessageId(1134)
            CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT = SystemMessageId(1135)
            ACCOUNT_NOT_ALLOWED_TO_CONNECT = SystemMessageId(1136)
            S1_HARVESTED_S3_S2S = SystemMessageId(1137)
            S1_HARVESTED_S2S = SystemMessageId(1138)
            INVENTORY_LIMIT_MUST_NOT_BE_EXCEEDED = SystemMessageId(1139)
            WOULD_YOU_LIKE_TO_OPEN_THE_GATE = SystemMessageId(1140)
            WOULD_YOU_LIKE_TO_CLOSE_THE_GATE = SystemMessageId(1141)
            CANNOT_SUMMON_S1_AGAIN = SystemMessageId(1142)
            SERVITOR_DISAPPEARED_NOT_ENOUGH_ITEMS = SystemMessageId(1143)
            NOBODY_IN_GAME_TO_CHAT = SystemMessageId(1144)
            S2_CREATED_FOR_S1_FOR_S3_ADENA = SystemMessageId(1145)
            S1_CREATED_S2_FOR_S3_ADENA = SystemMessageId(1146)
            S2_S3_S_CREATED_FOR_S1_FOR_S4_ADENA = SystemMessageId(1147)
            S1_CREATED_S2_S3_S_FOR_S4_ADENA = SystemMessageId(1148)
            CREATION_OF_S2_FOR_S1_AT_S3_ADENA_FAILED = SystemMessageId(1149)
            S1_FAILED_TO_CREATE_S2_FOR_S3_ADENA = SystemMessageId(1150)
            S2_SOLD_TO_S1_FOR_S3_ADENA = SystemMessageId(1151)
            S3_S2_S_SOLD_TO_S1_FOR_S4_ADENA = SystemMessageId(1152)
            S2_PURCHASED_FROM_S1_FOR_S3_ADENA = SystemMessageId(1153)
            S3_S2_S_PURCHASED_FROM_S1_FOR_S4_ADENA = SystemMessageId(1154)
            S3_S2_SOLD_TO_S1_FOR_S4_ADENA = SystemMessageId(1155)
            S2_S3_PURCHASED_FROM_S1_FOR_S4_ADENA = SystemMessageId(1156)
            TRYING_ON_STATE = SystemMessageId(1157)
            CANNOT_DISMOUNT_FROM_ELEVATION = SystemMessageId(1158)
            FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES = SystemMessageId(1159)
            FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES = SystemMessageId(1160)
            FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE = SystemMessageId(1161)
            FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES = SystemMessageId(1162)
            FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES = SystemMessageId(1163)
            FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES = SystemMessageId(1164)
            FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE = SystemMessageId(1165)
            FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES = SystemMessageId(1166)
            FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES = SystemMessageId(1167)
            FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES = SystemMessageId(1168)
            FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES = SystemMessageId(1169)
            FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE = SystemMessageId(1170)
            INNADRIL_BOAT_ARRIVE_20_MINUTES = SystemMessageId(1171)
            INNADRIL_BOAT_ARRIVE_15_MINUTES = SystemMessageId(1172)
            INNADRIL_BOAT_ARRIVE_10_MINUTES = SystemMessageId(1173)
            INNADRIL_BOAT_ARRIVE_5_MINUTES = SystemMessageId(1174)
            INNADRIL_BOAT_ARRIVE_1_MINUTE = SystemMessageId(1175)
            QUEST_EVENT_PERIOD = SystemMessageId(1176)
            VALIDATION_PERIOD = SystemMessageId(1177)
            AVARICE_DESCRIPTION = SystemMessageId(1178)
            GNOSIS_DESCRIPTION = SystemMessageId(1179)
            STRIFE_DESCRIPTION = SystemMessageId(1180)
            CHANGE_TITLE_CONFIRM = SystemMessageId(1181)
            CREST_DELETE_CONFIRM = SystemMessageId(1182)
            INITIAL_PERIOD = SystemMessageId(1183)
            RESULTS_PERIOD = SystemMessageId(1184)
            DAYS_LEFT_UNTIL_DELETION = SystemMessageId(1185)
            TO_CREATE_ACCOUNT_VISIT_WEBSITE = SystemMessageId(1186)
            ACCOUNT_INFORMATION_FORGOTTON_VISIT_WEBSITE = SystemMessageId(1187)
            YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION = SystemMessageId(1188)
            TEMPORARY_ALLIANCE = SystemMessageId(1189)
            TEMPORARY_ALLIANCE_DISSOLVED = SystemMessageId(1190)
            FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES = SystemMessageId(1191)
            FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES = SystemMessageId(1192)
            FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE = SystemMessageId(1193)
            MERC_CAN_BE_ASSIGNED = SystemMessageId(1194)
            MERC_CANT_BE_ASSIGNED_USING_STRIFE = SystemMessageId(1195)
            FORCE_MAXIMUM = SystemMessageId(1196)
            SUMMONING_SERVITOR_COSTS_S2_S1 = SystemMessageId(1197)
            CRYSTALLIZATION_SUCCESSFUL = SystemMessageId(1198)
            CLAN_WAR_HEADER = SystemMessageId(1199)
            S1_S2_ALLIANCE = SystemMessageId(1200)
            SELECT_QUEST_TO_ABOR = SystemMessageId(1201)
            S1_NO_ALLI_EXISTS = SystemMessageId(1202)
            NO_WAR_IN_PROGRESS = SystemMessageId(1203)
            SCREENSHOT = SystemMessageId(1204)
            MAILBOX_FULL = SystemMessageId(1205)
            MEMOBOX_FULL = SystemMessageId(1206)
            MAKE_AN_ENTRY = SystemMessageId(1207)
            S1_DIED_DROPPED_S3_S2 = SystemMessageId(1208)
            RAID_WAS_SUCCESSFUL = SystemMessageId(1209)
            QUEST_EVENT_PERIOD_BEGUN = SystemMessageId(1210)
            QUEST_EVENT_PERIOD_ENDED = SystemMessageId(1211)
            DAWN_OBTAINED_AVARICE = SystemMessageId(1212)
            DAWN_OBTAINED_GNOSIS = SystemMessageId(1213)
            DAWN_OBTAINED_STRIFE = SystemMessageId(1214)
            DUSK_OBTAINED_AVARICE = SystemMessageId(1215)
            DUSK_OBTAINED_GNOSIS = SystemMessageId(1216)
            DUSK_OBTAINED_STRIFE = SystemMessageId(1217)
            SEAL_VALIDATION_PERIOD_BEGUN = SystemMessageId(1218)
            SEAL_VALIDATION_PERIOD_ENDED = SystemMessageId(1219)
            SUMMON_CONFIRM = SystemMessageId(1220)
            RETURN_CONFIRM = SystemMessageId(1221)
            LOC_GM_CONSULATION_SERVICE_S1_S2_S3 = SystemMessageId(1222)
            DEPART_FOR_TALKING_5_MINUTES = SystemMessageId(1223)
            DEPART_FOR_TALKING_1_MINUTE = SystemMessageId(1224)
            DEPART_FOR_TALKING = SystemMessageId(1225)
            LEAVING_FOR_TALKING = SystemMessageId(1226)
            S1_UNREAD_MESSAGES = SystemMessageId(1227)
            S1_BLOCKED_YOU_CANNOT_MAIL = SystemMessageId(1228)
            NO_MORE_MESSAGES_TODAY = SystemMessageId(1229)
            ONLY_FIVE_RECIPIENTS = SystemMessageId(1230)
            SENT_MAIL = SystemMessageId(1231)
            MESSAGE_NOT_SENT = SystemMessageId(1232)
            NEW_MAIL = SystemMessageId(1233)
            MAIL_STORED_IN_MAILBOX = SystemMessageId(1234)
            ALL_FRIENDS_DELETE_CONFIRM = SystemMessageId(1235)
            ENTER_SECURITY_CARD_NUMBER = SystemMessageId(1236)
            ENTER_CARD_NUMBER_FOR_S1 = SystemMessageId(1237)
            TEMP_MAILBOX_FULL = SystemMessageId(1238)
            KEYBOARD_MODULE_FAILED_LOAD = SystemMessageId(1239)
            DUSK_WON = SystemMessageId(1240)
            DAWN_WON = SystemMessageId(1241)
            NOT_VERIFIED_AGE_NO_LOGIN = SystemMessageId(1242)
            SECURITY_CARD_NUMBER_INVALID = SystemMessageId(1243)
            NOT_VERIFIED_AGE_LOG_OFF = SystemMessageId(1244)
            LOGOUT_IN_S1_MINUTES = SystemMessageId(1245)
            S1_DIED_DROPPED_S2_ADENA = SystemMessageId(1246)
            CORPSE_TOO_OLD_SKILL_NOT_USED = SystemMessageId(1247)
            OUT_OF_FEED_MOUNT_CANCELED = SystemMessageId(1248)
            YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER = SystemMessageId(1249)
            SURRENDER_ALLY_WAR_CONFIRM = SystemMessageId(1250)
            DISMISS_ALLY_CONFIRM = SystemMessageId(1251)
            SURRENDER_CONFIRM1 = SystemMessageId(1252)
            SURRENDER_CONFIRM2 = SystemMessageId(1253)
            THANKS_FOR_FEEDBACK = SystemMessageId(1254)
            GM_CONSULTATION_BEGUN = SystemMessageId(1255)
            PLEASE_WRITE_NAME_AFTER_COMMAND = SystemMessageId(1256)
            PET_SKILL_NOT_AS_MACRO = SystemMessageId(1257)
            S1_CRYSTALLIZED = SystemMessageId(1258)
            ALLIANCE_TARGET_HEADER = SystemMessageId(1259)
            PREPARATIONS_PERIOD_BEGUN = SystemMessageId(1260)
            COMPETITION_PERIOD_BEGUN = SystemMessageId(1261)
            RESULTS_PERIOD_BEGUN = SystemMessageId(1262)
            VALIDATION_PERIOD_BEGUN = SystemMessageId(1263)
            STONE_CANNOT_ABSORB = SystemMessageId(1264)
            CANT_ABSORB_WITHOUT_STONE = SystemMessageId(1265)
            EXCHANGE_HAS_ENDED = SystemMessageId(1266)
            CONTRIB_SCORE_INCREASED_S1 = SystemMessageId(1267)
            ADD_SUBCLASS_CONFIRM = SystemMessageId(1268)
            ADD_NEW_SUBCLASS = SystemMessageId(1269)
            SUBCLASS_TRANSFER_COMPLETED = SystemMessageId(1270)
            DAWN_CONFIRM = SystemMessageId(1271)
            DUSK_CONFIRM = SystemMessageId(1272)
            SEVENSIGNS_PARTECIPATION_DAWN = SystemMessageId(1273)
            SEVENSIGNS_PARTECIPATION_DUSK = SystemMessageId(1274)
            FIGHT_FOR_AVARICE = SystemMessageId(1275)
            FIGHT_FOR_GNOSIS = SystemMessageId(1276)
            FIGHT_FOR_STRIFE = SystemMessageId(1277)
            NPC_SERVER_NOT_OPERATING = SystemMessageId(1278)
            CONTRIB_SCORE_EXCEEDED = SystemMessageId(1279)
            CRITICAL_HIT_MAGIC = SystemMessageId(1280)
            YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS = SystemMessageId(1281)
            YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1 = SystemMessageId(1282)
            MINIMUM_FRAME_ACTIVATED = SystemMessageId(1283)
            MINIMUM_FRAME_DEACTIVATED = SystemMessageId(1284)
            NO_INVENTORY_CANNOT_PURCHASE = SystemMessageId(1285)
            UNTIL_MONDAY_6PM = SystemMessageId(1286)
            UNTIL_TODAY_6PM = SystemMessageId(1287)
            S1_WILL_WIN_COMPETITION = SystemMessageId(1288)
            SEAL_OWNED_10_MORE_VOTED = SystemMessageId(1289)
            SEAL_NOT_OWNED_35_MORE_VOTED = SystemMessageId(1290)
            SEAL_OWNED_10_LESS_VOTED = SystemMessageId(1291)
            SEAL_NOT_OWNED_35_LESS_VOTED = SystemMessageId(1292)
            COMPETITION_WILL_TIE = SystemMessageId(1293)
            COMPETITION_TIE_SEAL_NOT_AWARDED = SystemMessageId(1294)
            SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE = SystemMessageId(1295)
            NO_PRIVATE_STORE_HERE = SystemMessageId(1296)
            NO_PRIVATE_WORKSHOP_HERE = SystemMessageId(1297)
            MONS_EXIT_CONFIRM = SystemMessageId(1298)
            S1_CASTING_INTERRUPTED = SystemMessageId(1299)
            WEAR_ITEMS_STOPPED = SystemMessageId(1300)
            CAN_BE_USED_BY_DAWN = SystemMessageId(1301)
            CAN_BE_USED_BY_DUSK = SystemMessageId(1302)
            CAN_BE_USED_DURING_QUEST_EVENT_PERIOD = SystemMessageId(1303)
            STRIFE_CANCELED_DEFENSIVE_REGISTRATION = SystemMessageId(1304)
            SEAL_STONES_ONLY_WHILE_QUEST = SystemMessageId(1305)
            NO_LONGER_TRYING_ON = SystemMessageId(1306)
            SETTLE_ACCOUNT_ONLY_IN_SEAL_VALIDATION = SystemMessageId(1307)
            CLASS_TRANSFER = SystemMessageId(1308)
            LATEST_MSN_REQUIRED = SystemMessageId(1309)
            LATEST_MSN_RECOMMENDED = SystemMessageId(1310)
            MSN_ONLY_BASIC = SystemMessageId(1311)
            MSN_OBTAINED_FROM = SystemMessageId(1312)
            S1_CHAT_HISTORIES_STORED = SystemMessageId(1313)
            ENTER_PASSPORT_FOR_ADDING = SystemMessageId(1314)
            DELETING_A_CONTACT = SystemMessageId(1315)
            CONTACT_WILL_DELETED = SystemMessageId(1316)
            CONTACT_DELETE_CONFIRM = SystemMessageId(1317)
            SELECT_CONTACT_FOR_BLOCK_UNBLOCK = SystemMessageId(1318)
            SELECT_CONTACT_FOR_CHANGE_GROUP = SystemMessageId(1319)
            SELECT_GROUP_PRESS_OK = SystemMessageId(1320)
            ENTER_GROUP_NAME = SystemMessageId(1321)
            SELECT_GROUP_ENTER_NAME = SystemMessageId(1322)
            SELECT_GROUP_TO_DELETE = SystemMessageId(1323)
            SIGNING_IN = SystemMessageId(1324)
            ANOTHER_COMPUTER_LOGOUT = SystemMessageId(1325)
            S1_D = SystemMessageId(1326)
            MESSAGE_NOT_DELIVERED = SystemMessageId(1327)
            DUSK_NOT_RESURRECTED = SystemMessageId(1328)
            BLOCKED_FROM_USING_STORE = SystemMessageId(1329)
            NO_STORE_FOR_S1_MINUTES = SystemMessageId(1330)
            NO_LONGER_BLOCKED_USING_STORE = SystemMessageId(1331)
            NO_ITEMS_AFTER_DEATH = SystemMessageId(1332)
            REPLAY_INACCESSIBLE = SystemMessageId(1333)
            NEW_CAMERA_STORED = SystemMessageId(1334)
            CAMERA_STORING_FAILED = SystemMessageId(1335)
            REPLAY_S1_S2_CORRUPTED = SystemMessageId(1336)
            REPLAY_TERMINATE_CONFIRM = SystemMessageId(1337)
            EXCEEDED_MAXIMUM_AMOUNT = SystemMessageId(1338)
            MACRO_SHORTCUT_NOT_RUN = SystemMessageId(1339)
            SERVER_NOT_ACCESSED_BY_COUPON = SystemMessageId(1340)
            INCORRECT_NAME_OR_ADDRESS = SystemMessageId(1341)
            ALREADY_LOGGED_IN = SystemMessageId(1342)
            INCORRECT_ADDRESS_OR_PASSWORD = SystemMessageId(1343)
            NET_LOGIN_FAILED = SystemMessageId(1344)
            SELECT_CONTACT_CLICK_OK = SystemMessageId(1345)
            CURRENTLY_ENTERING_CHAT = SystemMessageId(1346)
            MESSENGER_FAILED_CARRYING_OUT_TASK = SystemMessageId(1347)
            S1_ENTERED_CHAT_ROOM = SystemMessageId(1348)
            S1_LEFT_CHAT_ROOM = SystemMessageId(1349)
            GOING_OFFLINE = SystemMessageId(1350)
            SELECT_CONTACT_CLICK_REMOVE = SystemMessageId(1351)
            ADDED_TO_S1_S2_CONTACT_LIST = SystemMessageId(1352)
            CAN_SET_OPTION_TO_ALWAYS_SHOW_OFFLINE = SystemMessageId(1353)
            NO_CHAT_WHILE_BLOCKED = SystemMessageId(1354)
            CONTACT_CURRENTLY_BLOCKED = SystemMessageId(1355)
            CONTACT_CURRENTLY_OFFLINE = SystemMessageId(1356)
            YOU_ARE_BLOCKED = SystemMessageId(1357)
            YOU_ARE_LOGGING_OUT = SystemMessageId(1358)
            S1_LOGGED_IN2 = SystemMessageId(1359)
            GOT_MESSAGE_FROM_S1 = SystemMessageId(1360)
            LOGGED_OUT_DUE_TO_ERROR = SystemMessageId(1361)
            SELECT_CONTACT_TO_DELETE = SystemMessageId(1362)
            YOUR_REQUEST_ALLIANCE_WAR_DENIED = SystemMessageId(1363)
            REQUEST_ALLIANCE_WAR_REJECTED = SystemMessageId(1364)
            S2_OF_S1_SURRENDERED_AS_INDIVIDUAL = SystemMessageId(1365)
            DELTE_GROUP_INSTRUCTION = SystemMessageId(1366)
            ONLY_GROUP_CAN_ADD_RECORDS = SystemMessageId(1367)
            YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME = SystemMessageId(1368)
            EXCEEDED_THE_MAXIMUM = SystemMessageId(1369)
            CANNOT_MAIL_GM_S1 = SystemMessageId(1370)
            GAMEPLAY_RESTRICTION_PENALTY_S1 = SystemMessageId(1371)
            PUNISHMENT_CONTINUE_S1_MINUTES = SystemMessageId(1372)
            S1_OBTAINED_S2_FROM_RAIDBOSS = SystemMessageId(1373)
            S1_PICKED_UP_S3_S2_S_FROM_RAIDBOSS = SystemMessageId(1374)
            S1_OBTAINED_S2_ADENA_FROM_RAIDBOSS = SystemMessageId(1375)
            S1_OBTAINED_S2_FROM_ANOTHER_CHARACTER = SystemMessageId(1376)
            S1_PICKED_UP_S3_S2_S_FROM_ANOTHER_CHARACTER = SystemMessageId(1377)
            S1_PICKED_UP_S3_S2_FROM_ANOTHER_CHARACTER = SystemMessageId(1378)
            S1_OBTAINED_S2_ADENA = SystemMessageId(1379)
            CANT_SUMMON_S1_ON_BATTLEGROUND = SystemMessageId(1380)
            LEADER_OBTAINED_S2_OF_S1 = SystemMessageId(1381)
            CHOOSE_WEAPON_CONFIRM = SystemMessageId(1382)
            EXCHANGE_CONFIRM = SystemMessageId(1383)
            S1_HAS_BECOME_A_PARTY_LEADER = SystemMessageId(1384)
            NO_DISMOUNT_HERE = SystemMessageId(1385)
            NO_LONGER_HELD_IN_PLACE = SystemMessageId(1386)
            SELECT_ITEM_TO_TRY_ON = SystemMessageId(1387)
            PARTY_ROOM_CREATED = SystemMessageId(1388)
            PARTY_ROOM_REVISED = SystemMessageId(1389)
            PARTY_ROOM_FORBIDDEN = SystemMessageId(1390)
            PARTY_ROOM_EXITED = SystemMessageId(1391)
            S1_LEFT_PARTY_ROOM = SystemMessageId(1392)
            OUSTED_FROM_PARTY_ROOM = SystemMessageId(1393)
            S1_KICKED_FROM_PARTY_ROOM = SystemMessageId(1394)
            PARTY_ROOM_DISBANDED = SystemMessageId(1395)
            CANT_VIEW_PARTY_ROOMS = SystemMessageId(1396)
            PARTY_ROOM_LEADER_CHANGED = SystemMessageId(1397)
            RECRUITING_PARTY_MEMBERS = SystemMessageId(1398)
            ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER = SystemMessageId(1399)
            PLEASE_SELECT_THE_PERSON_TO_WHOM_YOU_WOULD_LIKE_TO_TRANSFER_THE_RIGHTS_OF_A_PARTY_LEADER =
                SystemMessageId(1400)
            YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF = SystemMessageId(1401)
            YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER = SystemMessageId(1402)
            YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADER_RIGHTS = SystemMessageId(1403)
            MANUFACTURE_PRICE_HAS_CHANGED = SystemMessageId(1404)
            S1_CP_WILL_BE_RESTORED = SystemMessageId(1405)
            S2_CP_WILL_BE_RESTORED_BY_S1 = SystemMessageId(1406)
            NO_LOGIN_WITH_TWO_ACCOUNTS = SystemMessageId(1407)
            PREPAID_LEFT_S1_S2_S3 = SystemMessageId(1408)
            PREPAID_EXPIRED_S1_S2 = SystemMessageId(1409)
            PREPAID_EXPIRED = SystemMessageId(1410)
            PREPAID_CHANGED = SystemMessageId(1411)
            PREPAID_LEFT_S1 = SystemMessageId(1412)
            CANT_ENTER_PARTY_ROOM = SystemMessageId(1413)
            WRONG_GRID_COUNT = SystemMessageId(1414)
            COMMAND_FILE_NOT_SENT = SystemMessageId(1415)
            TEAM_1_NO_REPRESENTATIVE = SystemMessageId(1416)
            TEAM_2_NO_REPRESENTATIVE = SystemMessageId(1417)
            TEAM_1_NO_NAME = SystemMessageId(1418)
            TEAM_2_NO_NAME = SystemMessageId(1419)
            TEAM_NAME_IDENTICAL = SystemMessageId(1420)
            RACE_SETUP_FILE1 = SystemMessageId(1421)
            RACE_SETUP_FILE2 = SystemMessageId(1422)
            RACE_SETUP_FILE3 = SystemMessageId(1423)
            RACE_SETUP_FILE4 = SystemMessageId(1424)
            RACE_SETUP_FILE5 = SystemMessageId(1425)
            RACE_SETUP_FILE6 = SystemMessageId(1426)
            RACE_SETUP_FILE7 = SystemMessageId(1427)
            RACE_SETUP_FILE8 = SystemMessageId(1428)
            RACE_SETUP_FILE9 = SystemMessageId(1429)
            RACE_SETUP_FILE10 = SystemMessageId(1430)
            RACE_STOPPED_TEMPORARILY = SystemMessageId(1431)
            OPPONENT_PETRIFIED = SystemMessageId(1432)
            USE_OF_S1_WILL_BE_AUTO = SystemMessageId(1433)
            AUTO_USE_OF_S1_CANCELLED = SystemMessageId(1434)
            AUTO_USE_CANCELLED_LACK_OF_S1 = SystemMessageId(1435)
            CANNOT_AUTO_USE_LACK_OF_S1 = SystemMessageId(1436)
            DICE_NO_LONGER_ALLOWED = SystemMessageId(1437)
            THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT = SystemMessageId(1438)
            YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL = SystemMessageId(1439)
            YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1 = SystemMessageId(1440)
            YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1 = SystemMessageId(1441)
            YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL = SystemMessageId(1443)
            YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL = SystemMessageId(1444)
            REPLACE_SUBCLASS_CONFIRM = SystemMessageId(1445)
            FERRY_FROM_S1_TO_S2_DELAYED = SystemMessageId(1446)
            CANNOT_DO_WHILE_FISHING_1 = SystemMessageId(1447)
            ONLY_FISHING_SKILLS_NOW = SystemMessageId(1448)
            GOT_A_BITE = SystemMessageId(1449)
            FISH_SPIT_THE_HOOK = SystemMessageId(1450)
            BAIT_STOLEN_BY_FISH = SystemMessageId(1451)
            BAIT_LOST_FISH_GOT_AWAY = SystemMessageId(1452)
            FISHING_POLE_NOT_EQUIPPED = SystemMessageId(1453)
            BAIT_ON_HOOK_BEFORE_FISHING = SystemMessageId(1454)
            CANNOT_FISH_UNDER_WATER = SystemMessageId(1455)
            CANNOT_FISH_ON_BOAT = SystemMessageId(1456)
            CANNOT_FISH_HERE = SystemMessageId(1457)
            FISHING_ATTEMPT_CANCELLED = SystemMessageId(1458)
            NOT_ENOUGH_BAIT = SystemMessageId(1459)
            REEL_LINE_AND_STOP_FISHING = SystemMessageId(1460)
            CAST_LINE_AND_START_FISHING = SystemMessageId(1461)
            CAN_USE_PUMPING_ONLY_WHILE_FISHING = SystemMessageId(1462)
            CAN_USE_REELING_ONLY_WHILE_FISHING = SystemMessageId(1463)
            FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN = SystemMessageId(1464)
            PUMPING_SUCCESFUL_S1_DAMAGE = SystemMessageId(1465)
            FISH_RESISTED_PUMPING_S1_HP_REGAINED = SystemMessageId(1466)
            REELING_SUCCESFUL_S1_DAMAGE = SystemMessageId(1467)
            FISH_RESISTED_REELING_S1_HP_REGAINED = SystemMessageId(1468)
            YOU_CAUGHT_SOMETHING = SystemMessageId(1469)
            CANNOT_DO_WHILE_FISHING_2 = SystemMessageId(1470)
            CANNOT_DO_WHILE_FISHING_3 = SystemMessageId(1471)
            CANNOT_ATTACK_WITH_FISHING_POLE = SystemMessageId(1472)
            S1_NOT_SUFFICIENT = SystemMessageId(1473)
            S1_NOT_AVAILABLE = SystemMessageId(1474)
            PET_DROPPED_S1 = SystemMessageId(1475)
            PET_DROPPED_S1_S2 = SystemMessageId(1476)
            PET_DROPPED_S2_S1_S = SystemMessageId(1477)
            ONLY_64_PIXEL_256_COLOR_BMP = SystemMessageId(1478)
            WRONG_FISHINGSHOT_GRADE = SystemMessageId(1479)
            OLYMPIAD_REMOVE_CONFIRM = SystemMessageId(1480)
            OLYMPIAD_NON_CLASS_CONFIRM = SystemMessageId(1481)
            OLYMPIAD_CLASS_CONFIRM = SystemMessageId(1482)
            HERO_CONFIRM = SystemMessageId(1483)
            HERO_WEAPON_CONFIRM = SystemMessageId(1484)
            FERRY_TALKING_GLUDIN_DELAYED = SystemMessageId(1485)
            FERRY_GLUDIN_TALKING_DELAYED = SystemMessageId(1486)
            FERRY_GIRAN_TALKING_DELAYED = SystemMessageId(1487)
            FERRY_TALKING_GIRAN_DELAYED = SystemMessageId(1488)
            INNADRIL_BOAT_DELAYED = SystemMessageId(1489)
            TRADED_S2_OF_CROP_S1 = SystemMessageId(1490)
            FAILED_IN_TRADING_S2_OF_CROP_S1 = SystemMessageId(1491)
            YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S = SystemMessageId(1492)
            THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME = SystemMessageId(1493)
            THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME =
                SystemMessageId(1494)
            THE_GAME_WILL_START_IN_S1_SECOND_S = SystemMessageId(1495)
            STARTS_THE_GAME = SystemMessageId(1496)
            S1_HAS_WON_THE_GAME = SystemMessageId(1497)
            THE_GAME_ENDED_IN_A_TIE = SystemMessageId(1498)
            YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS = SystemMessageId(1499)
            YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER = SystemMessageId(1500)
            ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD = SystemMessageId(1501)
            YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT = SystemMessageId(1502)
            YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES = SystemMessageId(1503)
            YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES = SystemMessageId(1504)
            YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME = SystemMessageId(1505)
            YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME = SystemMessageId(1506)
            THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT = SystemMessageId(1507)
            THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT = SystemMessageId(1508)
            THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT = SystemMessageId(1509)
            RESSURECTION_REQUEST_BY_S1 = SystemMessageId(1510)
            MASTER_CANNOT_RES = SystemMessageId(1511)
            CANNOT_RES_PET = SystemMessageId(1512)
            RES_HAS_ALREADY_BEEN_PROPOSED = SystemMessageId(1513)
            CANNOT_RES_MASTER = SystemMessageId(1514)
            CANNOT_RES_PET2 = SystemMessageId(1515)
            THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING = SystemMessageId(1516)
            BLESSED_ENCHANT_FAILED = SystemMessageId(1517)
            CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION = SystemMessageId(1518)
            MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_20_MINUTES = SystemMessageId(1519)
            SERVITOR_PASSED_AWAY = SystemMessageId(1520)
            YOUR_SERVITOR_HAS_VANISHED = SystemMessageId(1521)
            YOUR_PETS_CORPSE_HAS_DECAYED = SystemMessageId(1522)
            RELEASE_PET_ON_BOAT = SystemMessageId(1523)
            S1_PET_GAINED_S2 = SystemMessageId(1524)
            S1_PET_GAINED_S3_S2_S = SystemMessageId(1525)
            S1_PET_GAINED_S2_S3 = SystemMessageId(1526)
            PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY = SystemMessageId(1527)
            SENT_PETITION_TO_GM = SystemMessageId(1528)
            COMMAND_CHANNEL_CONFIRM_FROM_S1 = SystemMessageId(1529)
            SELECT_TARGET_OR_ENTER_NAME = SystemMessageId(1530)
            ENTER_CLAN_NAME_TO_DECLARE_WAR2 = SystemMessageId(1531)
            ENTER_CLAN_NAME_TO_CEASE_FIRE = SystemMessageId(1532)
            ATTENTION_S1_PICKED_UP_S2 = SystemMessageId(1533)
            ATTENTION_S1_PICKED_UP_S2_S3 = SystemMessageId(1534)
            ATTENTION_S1_PET_PICKED_UP_S2 = SystemMessageId(1535)
            ATTENTION_S1_PET_PICKED_UP_S2_S3 = SystemMessageId(1536)
            LOC_RUNE_S1_S2_S3 = SystemMessageId(1537)
            LOC_GODDARD_S1_S2_S3 = SystemMessageId(1538)
            CARGO_AT_TALKING_VILLAGE = SystemMessageId(1539)
            CARGO_AT_DARKELF_VILLAGE = SystemMessageId(1540)
            CARGO_AT_ELVEN_VILLAGE = SystemMessageId(1541)
            CARGO_AT_ORC_VILLAGE = SystemMessageId(1542)
            CARGO_AT_DWARVEN_VILLAGE = SystemMessageId(1543)
            CARGO_AT_ADEN = SystemMessageId(1544)
            CARGO_AT_OREN = SystemMessageId(1545)
            CARGO_AT_HUNTERS = SystemMessageId(1546)
            CARGO_AT_DION = SystemMessageId(1547)
            CARGO_AT_FLORAN = SystemMessageId(1548)
            CARGO_AT_GLUDIN = SystemMessageId(1549)
            CARGO_AT_GLUDIO = SystemMessageId(1550)
            CARGO_AT_GIRAN = SystemMessageId(1551)
            CARGO_AT_HEINE = SystemMessageId(1552)
            CARGO_AT_RUNE = SystemMessageId(1553)
            CARGO_AT_GODDARD = SystemMessageId(1554)
            CANCEL_CHARACTER_DELETION_CONFIRM = SystemMessageId(1555)
            CLAN_NOTICE_SAVED = SystemMessageId(1556)
            SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = SystemMessageId(1557)
            THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = SystemMessageId(1558)
            CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = SystemMessageId(1559)
            THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = SystemMessageId(1560)
            CLAN_S1_DECLARED_WAR = SystemMessageId(1561)
            CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP = SystemMessageId(1562)
            S1_CLAN_CANNOT_DECLARE_WAR_TOO_LOW_LEVEL_OR_NOT_ENOUGH_MEMBERS = SystemMessageId(1563)
            CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER = SystemMessageId(1564)
            CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST = SystemMessageId(1565)
            CLAN_S1_HAS_DECIDED_TO_STOP = SystemMessageId(1566)
            WAR_AGAINST_S1_HAS_STOPPED = SystemMessageId(1567)
            WRONG_DECLARATION_TARGET = SystemMessageId(1568)
            CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK = SystemMessageId(1569)
            TOO_MANY_CLAN_WARS = SystemMessageId(1570)
            CLANS_YOU_DECLARED_WAR_ON = SystemMessageId(1571)
            CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU = SystemMessageId(1572)
            YOU_ARENT_IN_CLAN_WARS = SystemMessageId(1573)
            NO_CLAN_WARS_VS_YOU = SystemMessageId(1574)
            COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER = SystemMessageId(1575)
            PET_USE_THE_POWER_OF_SPIRIT = SystemMessageId(1576)
            SERVITOR_USE_THE_POWER_OF_SPIRIT = SystemMessageId(1577)
            ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE = SystemMessageId(1578)
            S1_PET_GAINED_S2_ADENA = SystemMessageId(1579)
            COMMAND_CHANNEL_FORMED = SystemMessageId(1580)
            COMMAND_CHANNEL_DISBANDED = SystemMessageId(1581)
            JOINED_COMMAND_CHANNEL = SystemMessageId(1582)
            DISMISSED_FROM_COMMAND_CHANNEL = SystemMessageId(1583)
            S1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL = SystemMessageId(1584)
            COMMAND_CHANNEL_DISBANDED2 = SystemMessageId(1585)
            LEFT_COMMAND_CHANNEL = SystemMessageId(1586)
            S1_PARTY_LEFT_COMMAND_CHANNEL = SystemMessageId(1587)
            COMMAND_CHANNEL_ONLY_AT_LEAST_5_PARTIES = SystemMessageId(1588)
            COMMAND_CHANNEL_LEADER_NOW_S1 = SystemMessageId(1589)
            GUILD_INFO_HEADER = SystemMessageId(1590)
            NO_USER_INVITED_TO_COMMAND_CHANNEL = SystemMessageId(1591)
            CANNOT_LONGER_SETUP_COMMAND_CHANNEL = SystemMessageId(1592)
            CANNOT_INVITE_TO_COMMAND_CHANNEL = SystemMessageId(1593)
            S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL = SystemMessageId(1594)
            S1_SUCCEEDED = SystemMessageId(1595)
            HIT_BY_S1 = SystemMessageId(1596)
            S1_FAILED = SystemMessageId(1597)
            SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET = SystemMessageId(1598)
            CANNOT_OBSERVE_IN_COMBAT = SystemMessageId(1599)
            TOMORROW_ITEM_ZERO_CONFIRM = SystemMessageId(1600)
            TOMORROW_ITEM_SAME_CONFIRM = SystemMessageId(1601)
            COMMAND_CHANNEL_ONLY_FOR_PARTY_LEADER = SystemMessageId(1602)
            ONLY_COMMANDER_GIVE_COMMAND = SystemMessageId(1603)
            CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR = SystemMessageId(1604)
            HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR = SystemMessageId(1605)
            THIRD_CLASS_TRANSFER = SystemMessageId(1606)
            S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES = SystemMessageId(1607)
            INSUFFICIENT_ADENA_TO_BUY_CASTLE = SystemMessageId(1608)
            WAR_ALREADY_DECLARED = SystemMessageId(1609)
            CANNOT_DECLARE_AGAINST_OWN_CLAN = SystemMessageId(1610)
            PARTY_LEADER_S1 = SystemMessageId(1611)
            WAR_LIST = SystemMessageId(1612)
            NO_CLAN_ON_WAR_LIST = SystemMessageId(1613)
            JOINED_CHANNEL_ALREADY_OPEN = SystemMessageId(1614)
            S1_PARTIES_REMAINING_UNTIL_CHANNEL = SystemMessageId(1615)
            COMMAND_CHANNEL_ACTIVATED = SystemMessageId(1616)
            CANT_USE_COMMAND_CHANNEL = SystemMessageId(1617)
            FERRY_RUNE_GLUDIN_DELAYED = SystemMessageId(1618)
            FERRY_GLUDIN_RUNE_DELAYED = SystemMessageId(1619)
            ARRIVED_AT_RUNE = SystemMessageId(1620)
            DEPARTURE_FOR_GLUDIN_5_MINUTES = SystemMessageId(1621)
            DEPARTURE_FOR_GLUDIN_1_MINUTE = SystemMessageId(1622)
            DEPARTURE_FOR_GLUDIN_SHORTLY = SystemMessageId(1623)
            DEPARTURE_FOR_GLUDIN_NOW = SystemMessageId(1624)
            DEPARTURE_FOR_RUNE_10_MINUTES = SystemMessageId(1625)
            DEPARTURE_FOR_RUNE_5_MINUTES = SystemMessageId(1626)
            DEPARTURE_FOR_RUNE_1_MINUTE = SystemMessageId(1627)
            DEPARTURE_FOR_GLUDIN_SHORTLY2 = SystemMessageId(1628)
            DEPARTURE_FOR_RUNE_NOW = SystemMessageId(1629)
            FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES = SystemMessageId(1630)
            FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES = SystemMessageId(1631)
            FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES = SystemMessageId(1632)
            FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE = SystemMessageId(1633)
            FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES = SystemMessageId(1634)
            FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES = SystemMessageId(1635)
            FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES = SystemMessageId(1636)
            FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE = SystemMessageId(1637)
            CANNOT_FISH_WHILE_USING_RECIPE_BOOK = SystemMessageId(1638)
            OLYMPIAD_PERIOD_S1_HAS_STARTED = SystemMessageId(1639)
            OLYMPIAD_PERIOD_S1_HAS_ENDED = SystemMessageId(1640)
            THE_OLYMPIAD_GAME_HAS_STARTED = SystemMessageId(1641)
            THE_OLYMPIAD_GAME_HAS_ENDED = SystemMessageId(1642)
            LOC_DIMENSIONAL_GAP_S1_S2_S3 = SystemMessageId(1643)
            PLAY_TIME_NOW_ACCUMULATING = SystemMessageId(1649)
            TRY_LOGIN_LATER = SystemMessageId(1650)
            THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS = SystemMessageId(1651)
            RECORDING_GAMEPLAY_START = SystemMessageId(1652)
            RECORDING_GAMEPLAY_STOP_S1 = SystemMessageId(1653)
            RECORDING_GAMEPLAY_FAILED = SystemMessageId(1654)
            YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK = SystemMessageId(1655)
            SUCCESSFULLY_TRADED_WITH_NPC = SystemMessageId(1656)
            S1_HAS_GAINED_S2_OLYMPIAD_POINTS = SystemMessageId(1657)
            S1_HAS_LOST_S2_OLYMPIAD_POINTS = SystemMessageId(1658)
            LOC_CEMETARY_OF_THE_EMPIRE_S1_S2_S3 = SystemMessageId(1659)
            CHANNEL_CREATOR_S1 = SystemMessageId(1660)
            S1_OBTAINED_S3_S2_S = SystemMessageId(1661)
            FISH_NO_MORE_BITING_TRY_OTHER_LOCATION = SystemMessageId(1662)
            CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED = SystemMessageId(1663)
            FISH_RESISTING_LOOK_BOBBLER = SystemMessageId(1664)
            YOU_WORN_FISH_OUT = SystemMessageId(1665)
            OBTAINED_S1_S2 = SystemMessageId(1666)
            LETHAL_STRIKE = SystemMessageId(1667)
            LETHAL_STRIKE_SUCCESSFUL = SystemMessageId(1668)
            NOTHING_INSIDE_THAT = SystemMessageId(1669)
            REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY = SystemMessageId(1670)
            REELING_SUCCESSFUL_PENALTY_S1 = SystemMessageId(1671)
            PUMPING_SUCCESSFUL_PENALTY_S1 = SystemMessageId(1672)
            THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS =
                SystemMessageId(1673)
            NOBLESSE_ONLY = SystemMessageId(1674)
            A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM = SystemMessageId(1675)
            NO_SERVITOR_CANNOT_AUTOMATE_USE = SystemMessageId(1676)
            CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT = SystemMessageId(1677)
            NO_CLAN_WAR_AGAINST_CLAN_S1 = SystemMessageId(1678)
            ONLY_CHANNEL_CREATOR_CAN_GLOBAL_COMMAND = SystemMessageId(1679)
            S1_DECLINED_CHANNEL_INVITATION = SystemMessageId(1680)
            S1_DID_NOT_RESPOND_CHANNEL_INVITATION_FAILED = SystemMessageId(1681)
            ONLY_CHANNEL_CREATOR_CAN_DISMISS = SystemMessageId(1682)
            ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL = SystemMessageId(1683)
            NO_CLAN_WAR_AGAINST_DISSOLVING_CLAN = SystemMessageId(1684)
            YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE = SystemMessageId(1685)
            CASTLE_WALL_DAMAGED = SystemMessageId(1686)
            AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN = SystemMessageId(1687)
            CANNOT_ENCHANT_WHILE_STORE = SystemMessageId(1688)
            YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS = SystemMessageId(1689)
            YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME =
                SystemMessageId(1690)
            SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD =
                SystemMessageId(1691)
            SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD =
                SystemMessageId(1692)
            WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME = SystemMessageId(1693)
            ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW = SystemMessageId(1694)
            ONLY_DURING_SIEGE = SystemMessageId(1695)
            ACCUMULATED_PLAY_TIME_IS_S1 = SystemMessageId(1696)
            ACCUMULATED_PLAY_TIME_WARNING1 = SystemMessageId(1697)
            ACCUMULATED_PLAY_TIME_WARNING2 = SystemMessageId(1698)
            CANNOT_DISMISS_PARTY_MEMBER = SystemMessageId(1699)
            NOT_ENOUGH_SPIRITSHOTS_FOR_PET = SystemMessageId(1700)
            NOT_ENOUGH_SOULSHOTS_FOR_PET = SystemMessageId(1701)
            S1_USING_THIRD_PARTY_PROGRAM = SystemMessageId(1702)
            NOT_USING_THIRD_PARTY_PROGRAM = SystemMessageId(1703)
            CLOSE_STORE_WINDOW_AND_TRY_AGAIN = SystemMessageId(1704)
            PCPOINT_ACQUISITION_PERIOD = SystemMessageId(1705)
            PCPOINT_USE_PERIOD = SystemMessageId(1706)
            ACQUIRED_S1_PCPOINT = SystemMessageId(1707)
            ACQUIRED_S1_PCPOINT_DOUBLE = SystemMessageId(1708)
            USING_S1_PCPOINT = SystemMessageId(1709)
            SHORT_OF_ACCUMULATED_POINTS = SystemMessageId(1710)
            PCPOINT_USE_PERIOD_EXPIRED = SystemMessageId(1711)
            PCPOINT_ACCUMULATION_PERIOD_EXPIRED = SystemMessageId(1712)
            GAMES_DELAYED = SystemMessageId(1713)
            LOC_SCHUTTGART_S1_S2_S3 = SystemMessageId(1714)
            PEACEFUL_ZONE = SystemMessageId(1715)
            ALTERED_ZONE = SystemMessageId(1716)
            SIEGE_ZONE = SystemMessageId(1717)
            GENERAL_ZONE = SystemMessageId(1718)
            SEVENSIGNS_ZONE = SystemMessageId(1719)
            UNKNOWN1 = SystemMessageId(1720)
            COMBAT_ZONE = SystemMessageId(1721)
            ENTER_ITEM_NAME_SEARCH = SystemMessageId(1722)
            PLEASE_PROVIDE_PETITION_FEEDBACK = SystemMessageId(1723)
            SERVITOR_NOT_RETURN_IN_BATTLE = SystemMessageId(1724)
            EARNED_S1_RAID_POINTS = SystemMessageId(1725)
            S1_PERIOD_EXPIRED_DISAPPEARED = SystemMessageId(1726)
            S1_INVITED_YOU_TO_PARTY_ROOM_CONFIRM = SystemMessageId(1727)
            PARTY_MATCHING_REQUEST_NO_RESPONSE = SystemMessageId(1728)
            NOT_JOIN_CHANNEL_WHILE_TELEPORTING = SystemMessageId(1729)
            YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY = SystemMessageId(1730)
            ONLY_LEADER_CAN_CREATE_ACADEMY = SystemMessageId(1731)
            NEED_BLOODMARK_FOR_ACADEMY = SystemMessageId(1732)
            NEED_ADENA_FOR_ACADEMY = SystemMessageId(1733)
            ACADEMY_REQUIREMENTS = SystemMessageId(1734)
            S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY = SystemMessageId(1735)
            ACADEMY_MAXIMUM = SystemMessageId(1736)
            CLAN_CAN_CREATE_ACADEMY = SystemMessageId(1737)
            CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY = SystemMessageId(1738)
            CLAN_ACADEMY_CREATE_CONFIRM = SystemMessageId(1739)
            ACADEMY_CREATE_ENTER_NAME = SystemMessageId(1740)
            THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED = SystemMessageId(1741)
            ACADEMY_INVITATION_SENT_TO_S1 = SystemMessageId(1742)
            OPEN_ACADEMY_CONDITIONS = SystemMessageId(1743)
            ACADEMY_JOIN_NO_RESPONSE = SystemMessageId(1744)
            ACADEMY_JOIN_DECLINE = SystemMessageId(1745)
            ALREADY_JOINED_ACADEMY = SystemMessageId(1746)
            JOIN_ACADEMY_REQUEST_BY_S1_FOR_CLAN_S2 = SystemMessageId(1747)
            CLAN_MEMBER_GRADUATED_FROM_ACADEMY = SystemMessageId(1748)
            ACADEMY_MEMBERSHIP_TERMINATED = SystemMessageId(1749)
            CANNOT_JOIN_OLYMPIAD_POSSESSING_S1 = SystemMessageId(1750)
            GRAND_MASTER_COMMEMORATIVE_ITEM = SystemMessageId(1751)
            MEMBER_GRADUATED_EARNED_S1_REPU = SystemMessageId(1752)
            CANT_TRANSFER_PRIVILEGE_TO_ACADEMY_MEMBER = SystemMessageId(1753)
            RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER = SystemMessageId(1754)
            S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1 = SystemMessageId(1755)
            YOUR_APPRENTICE_S1_HAS_LOGGED_IN = SystemMessageId(1756)
            YOUR_APPRENTICE_S1_HAS_LOGGED_OUT = SystemMessageId(1757)
            YOUR_SPONSOR_S1_HAS_LOGGED_IN = SystemMessageId(1758)
            YOUR_SPONSOR_S1_HAS_LOGGED_OUT = SystemMessageId(1759)
            CLAN_MEMBER_S1_TITLE_CHANGED_TO_S2 = SystemMessageId(1760)
            CLAN_MEMBER_S1_PRIVILEGE_CHANGED_TO_S2 = SystemMessageId(1761)
            YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE = SystemMessageId(1762)
            S2_CLAN_MEMBER_S1_APPRENTICE_HAS_BEEN_REMOVED = SystemMessageId(1763)
            EQUIP_ONLY_FOR_ACADEMY = SystemMessageId(1764)
            EQUIP_NOT_FOR_GRADUATES = SystemMessageId(1765)
            CLAN_JOIN_APPLICATION_SENT_TO_S1_IN_S2 = SystemMessageId(1766)
            ACADEMY_JOIN_APPLICATION_SENT_TO_S1 = SystemMessageId(1767)
            JOIN_REQUEST_BY_S1_TO_CLAN_S2_ACADEMY = SystemMessageId(1768)
            JOIN_REQUEST_BY_S1_TO_ORDER_OF_KNIGHTS_S3_UNDER_CLAN_S2 = SystemMessageId(1769)
            CLAN_REPU_0_MAY_FACE_PENALTIES = SystemMessageId(1770)
            CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS = SystemMessageId(1771)
            CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS = SystemMessageId(1772)
            CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS = SystemMessageId(1773)
            CLAN_ACQUIRED_CONTESTED_CLAN_HALL_AND_S1_REPUTATION_POINTS = SystemMessageId(1774)
            CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION =
                SystemMessageId(1775)
            CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS = SystemMessageId(1776)
            CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED = SystemMessageId(1777)
            OPPOSING_CLAN_CAPTURED_CLAN_HALL_AND_YOUR_CLAN_LOSES_S1_POINTS = SystemMessageId(1778)
            CLAN_LOST_CONTESTED_CLAN_HALL_AND_300_POINTS = SystemMessageId(1779)
            CLAN_CAPTURED_CONTESTED_CLAN_HALL_AND_S1_POINTS_DEDUCTED_FROM_OPPONENT = SystemMessageId(1780)
            CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE = SystemMessageId(1781)
            CLAN_MEMBER_S1_WAS_KILLED_AND_S2_POINTS_DEDUCTED_FROM_REPUTATION = SystemMessageId(1782)
            FOR_KILLING_OPPOSING_MEMBER_S1_POINTS_WERE_DEDUCTED_FROM_OPPONENTS = SystemMessageId(1783)
            YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST_AND_ADDED_TO_OPPONENT = SystemMessageId(1784)
            YOUR_CLAN_HAS_BEEN_INITIALIZED_AND_S1_POINTS_LOST = SystemMessageId(1785)
            YOUR_CLAN_FAILED_TO_DEFEND_CASTLE_AND_S1_POINTS_LOST = SystemMessageId(1786)
            S1_DEDUCTED_FROM_CLAN_REP = SystemMessageId(1787)
            CLAN_SKILL_S1_ADDED = SystemMessageId(1788)
            REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED = SystemMessageId(1789)
            FAILED_TO_INCREASE_CLAN_LEVEL = SystemMessageId(1790)
            YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT = SystemMessageId(1791)
            ASSIGN_MANAGER_FOR_ORDER_OF_KNIGHTS = SystemMessageId(1792)
            S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2 = SystemMessageId(1793)
            THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED = SystemMessageId(1794)
            THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED = SystemMessageId(1795)
            ILLEGAL_USE17 = SystemMessageId(1796)
            S1_PROMOTED_TO_S2 = SystemMessageId(1797)
            CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1 = SystemMessageId(1798)
            SEARCHING_FOR_BOT_USERS_TRY_AGAIN_LATER = SystemMessageId(1799)
            S1_HISTORY_USING_BOT = SystemMessageId(1800)
            SELL_ATTEMPT_FAILED = SystemMessageId(1801)
            TRADE_ATTEMPT_FAILED = SystemMessageId(1802)
            GAME_REQUEST_CANNOT_BE_MADE = SystemMessageId(1803)
            ILLEGAL_USE18 = SystemMessageId(1804)
            ILLEGAL_USE19 = SystemMessageId(1805)
            ILLEGAL_USE20 = SystemMessageId(1806)
            ILLEGAL_USE21 = SystemMessageId(1807)
            ILLEGAL_USE22 = SystemMessageId(1808)
            ACCOUNT_MUST_VERIFIED = SystemMessageId(1809)
            REFUSE_INVITATION_ACTIVATED = SystemMessageId(1810)
            REFUSE_INVITATION_CURRENTLY_ACTIVE = SystemMessageId(1812)
            S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1 = SystemMessageId(1813)
            S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1 = SystemMessageId(1814)
            S2_WAS_DROPPED_IN_THE_S1_REGION = SystemMessageId(1815)
            THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION = SystemMessageId(1816)
            S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION = SystemMessageId(1817)
            S1_HAS_DISAPPEARED = SystemMessageId(1818)
            EVIL_FROM_S2_IN_S1 = SystemMessageId(1819)
            S1_CURRENTLY_SLEEP = SystemMessageId(1820)
            S2_EVIL_PRESENCE_FELT_IN_S1 = SystemMessageId(1821)
            S1_SEALED = SystemMessageId(1822)
            CLANHALL_WAR_REGISTRATION_PERIOD_ENDED = SystemMessageId(1823)
            REGISTERED_FOR_CLANHALL_WAR = SystemMessageId(1824)
            CLANHALL_WAR_REGISTRATION_FAILED = SystemMessageId(1825)
            CLANHALL_WAR_BEGINS_IN_S1_MINUTES = SystemMessageId(1826)
            CLANHALL_WAR_BEGINS_IN_S1_MINUTES_ENTER_NOW = SystemMessageId(1827)
            CLANHALL_WAR_BEGINS_IN_S1_SECONDS = SystemMessageId(1828)
            COMMAND_CHANNEL_FULL = SystemMessageId(1829)
            S1_NOT_ALLOWED_INVITE_TO_PARTY_ROOM = SystemMessageId(1830)
            S1_NOT_MEET_CONDITIONS_FOR_PARTY_ROOM = SystemMessageId(1831)
            ONLY_ROOM_LEADER_CAN_INVITE = SystemMessageId(1832)
            CONFIRM_DROP_ALL_OF_S1 = SystemMessageId(1833)
            PARTY_ROOM_FULL = SystemMessageId(1834)
            S1_CLAN_IS_FULL = SystemMessageId(1835)
            CANNOT_JOIN_ACADEMY_AFTER_2ND_OCCUPATION = SystemMessageId(1836)
            S1_SENT_INVITATION_TO_ROYAL_GUARD_S3_OF_CLAN_S2 = SystemMessageId(1837)
            COUPON_ONCE_PER_CHARACTER = SystemMessageId(1838)
            SERIAL_MAY_USED_ONCE = SystemMessageId(1839)
            SERIAL_INPUT_INCORRECT = SystemMessageId(1840)
            CLANHALL_WAR_CANCELLED = SystemMessageId(1841)
            S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT = SystemMessageId(1842)
            S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED = SystemMessageId(1843)
            S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED = SystemMessageId(1844)
            HERO_WEAPONS_CANT_DESTROYED = SystemMessageId(1845)
            TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT = SystemMessageId(1846)
            CAUGHT_FISH_S1_LENGTH = SystemMessageId(1847)
            REGISTERED_IN_FISH_SIZE_RANKING = SystemMessageId(1848)
            CONFIRM_DISCARD_ALL_OF_S1 = SystemMessageId(1849)
            CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED = SystemMessageId(1850)
            CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED = SystemMessageId(1851)
            ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE = SystemMessageId(1852)
            CANT_EXCHANGE_QUANTITY_ITEMS_OF_SAME_TYPE = SystemMessageId(1853)
            ITEM_CONVERTED_SUCCESSFULLY = SystemMessageId(1854)
            ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME = SystemMessageId(1855)
            OPPONENT_POSSESSES_S1_OLYMPIAD_CANCELLED = SystemMessageId(1856)
            S1_OWNS_S2_AND_CANNOT_PARTICIPATE_IN_OLYMPIAD = SystemMessageId(1857)
            CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD = SystemMessageId(1858)
            EXCEEDED_QUANTITY_FOR_MOVED = SystemMessageId(1859)
            THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW = SystemMessageId(1860)
            CLAN_CREST_HAS_BEEN_DELETED = SystemMessageId(1861)
            CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER = SystemMessageId(1862)
            S1_PURCHASED_CLAN_ITEM_REDUCING_S2_REPU_POINTS = SystemMessageId(1863)
            PET_REFUSING_ORDER = SystemMessageId(1864)
            PET_IN_STATE_OF_DISTRESS = SystemMessageId(1865)
            MP_REDUCED_BY_S1 = SystemMessageId(1866)
            YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1 = SystemMessageId(1867)
            CANNOT_EXCHANCE_USED_ITEM = SystemMessageId(1868)
            S1_GRANTED_MASTER_PARTY_LOOTING_RIGHTS = SystemMessageId(1869)
            COMMAND_CHANNEL_WITH_LOOTING_RIGHTS_EXISTS = SystemMessageId(1870)
            CONFIRM_DISMISS_S1_FROM_CLAN = SystemMessageId(1871)
            S1_HOURS_S2_MINUTES_LEFT = SystemMessageId(1872)
            S1_HOURS_S2_MINUTES_LEFT_FOR_THIS_PCCAFE = SystemMessageId(1873)
            S1_MINUTES_LEFT_FOR_THIS_USER = SystemMessageId(1874)
            S1_MINUTES_LEFT_FOR_THIS_PCCAFE = SystemMessageId(1875)
            CONFIRM_LEAVE_S1_CLAN = SystemMessageId(1876)
            GAME_WILL_END_IN_S1_MINUTES = SystemMessageId(1877)
            GAME_WILL_END_IN_S1_SECONDS = SystemMessageId(1878)
            IN_S1_MINUTES_TELEPORTED_OUTSIDE_OF_GAME_ARENA = SystemMessageId(1879)
            IN_S1_SECONDS_TELEPORTED_OUTSIDE_OF_GAME_ARENA = SystemMessageId(1880)
            PRELIMINARY_MATCH_BEGIN_IN_S1_SECONDS = SystemMessageId(1881)
            CHARACTERS_NOT_CREATED_FROM_THIS_SERVER = SystemMessageId(1882)
            NO_OFFERINGS_OWN_OR_MADE_BID_FOR = SystemMessageId(1883)
            ENTER_PCROOM_SERIAL_NUMBER = SystemMessageId(1884)
            SERIAL_NUMBER_CANT_ENTERED = SystemMessageId(1885)
            SERIAL_NUMBER_ALREADY_USED = SystemMessageId(1886)
            SERIAL_NUMBER_ENTERING_FAILED = SystemMessageId(1887)
            SERIAL_NUMBER_ENTERING_FAILED_5_TIMES = SystemMessageId(1888)
            CONGRATULATIONS_RECEIVED_S1 = SystemMessageId(1889)
            ALREADY_USED_COUPON_NOT_USE_SERIAL_NUMBER = SystemMessageId(1890)
            NOT_USE_ITEMS_IN_PRIVATE_STORE = SystemMessageId(1891)
            REPLAY_FILE_PREVIOUS_VERSION_CANT_PLAYED = SystemMessageId(1892)
            FILE_CANT_REPLAYED = SystemMessageId(1893)
            NOT_SUBCLASS_WHILE_OVERWEIGHT = SystemMessageId(1894)
            S1_IN_SUMMON_BLOCKING_AREA = SystemMessageId(1895)
            S1_ALREADY_SUMMONED = SystemMessageId(1896)
            S1_REQUIRED_FOR_SUMMONING = SystemMessageId(1897)
            S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED = SystemMessageId(1898)
            YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING = SystemMessageId(1899)
            S1_ENTERED_PARTY_ROOM = SystemMessageId(1900)
            S1_INVITED_YOU_TO_PARTY_ROOM = SystemMessageId(1901)
            INCOMPATIBLE_ITEM_GRADE = SystemMessageId(1902)
            NCOTP = SystemMessageId(1903)
            CANT_SUBCLASS_WITH_SUMMONED_SERVITOR = SystemMessageId(1904)
            S2_OF_S1_WILL_REPLACED_WITH_S4_OF_S3 = SystemMessageId(1905)
            SELECT_COMBAT_UNIT = SystemMessageId(1906)
            SELECT_CHARACTER_WHO_WILL = SystemMessageId(1907)
            S1_STATE_FORBIDS_SUMMONING = SystemMessageId(1908)
            ACADEMY_LIST_HEADER = SystemMessageId(1909)
            GRADUATES_S1 = SystemMessageId(1910)
            YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD = SystemMessageId(1911)
            NCOTP2 = SystemMessageId(1912)
            TIME_FOR_S1_IS_S2_MINUTES_REMAINING = SystemMessageId(1913)
            TIME_FOR_S1_IS_S2_SECONDS_REMAINING = SystemMessageId(1914)
            GAME_ENDS_IN_S1_SECONDS = SystemMessageId(1915)
            DEATH_PENALTY_LEVEL_S1_ADDED = SystemMessageId(1916)
            DEATH_PENALTY_LIFTED = SystemMessageId(1917)
            PET_TOO_HIGH_TO_CONTROL = SystemMessageId(1918)
            OLYMPIAD_REGISTRATION_PERIOD_ENDED = SystemMessageId(1919)
            ACCOUNT_INACTIVITY = SystemMessageId(1920)
            S2_HOURS_S3_MINUTES_SINCE_S1_KILLED = SystemMessageId(1921)
            S1_FAILED_KILLING_EXPIRED = SystemMessageId(1922)
            COURT_MAGICIAN_CREATED_PORTAL = SystemMessageId(1923)
            LOC_PRIMEVAL_ISLE_S1_S2_S3 = SystemMessageId(1924)
            SEAL_OF_STRIFE_FORBIDS_SUMMONING = SystemMessageId(1925)
            THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL = SystemMessageId(1926)
            S1_HAS_BEEN_CHALLENGED_TO_A_DUEL = SystemMessageId(1927)
            S1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL = SystemMessageId(1928)
            S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = SystemMessageId(1929)
            YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = SystemMessageId(1930)
            S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL = SystemMessageId(1931)
            S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL2 = SystemMessageId(1932)
            YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = SystemMessageId(1933)
            S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS =
                SystemMessageId(1934)
            S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL = SystemMessageId(1935)
            THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL = SystemMessageId(1936)
            SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY =
                SystemMessageId(1937)
            S1_HAS_CHALLENGED_YOU_TO_A_DUEL = SystemMessageId(1938)
            S1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL = SystemMessageId(1939)
            YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME = SystemMessageId(1940)
            NO_PLACE_FOR_DUEL = SystemMessageId(1941)
            THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL = SystemMessageId(1942)
            THE_OPPOSING_PARTY_IS_AT_BAD_LOCATION_FOR_A_DUEL = SystemMessageId(1943)
            IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE = SystemMessageId(1944)
            THE_DUEL_WILL_BEGIN_IN_S1_SECONDS = SystemMessageId(1945)
            S1_CHALLENGED_YOU_TO_A_DUEL = SystemMessageId(1946)
            S1_CHALLENGED_YOU_TO_A_PARTY_DUEL = SystemMessageId(1947)
            THE_DUEL_WILL_BEGIN_IN_S1_SECONDS2 = SystemMessageId(1948)
            LET_THE_DUEL_BEGIN = SystemMessageId(1949)
            S1_HAS_WON_THE_DUEL = SystemMessageId(1950)
            S1_PARTY_HAS_WON_THE_DUEL = SystemMessageId(1951)
            THE_DUEL_HAS_ENDED_IN_A_TIE = SystemMessageId(1952)
            SINCE_S1_WAS_DISQUALIFIED_S2_HAS_WON = SystemMessageId(1953)
            SINCE_S1_PARTY_WAS_DISQUALIFIED_S2_PARTY_HAS_WON = SystemMessageId(1954)
            SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON = SystemMessageId(1955)
            SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON = SystemMessageId(1956)
            SELECT_THE_ITEM_TO_BE_AUGMENTED = SystemMessageId(1957)
            SELECT_THE_CATALYST_FOR_AUGMENTATION = SystemMessageId(1958)
            REQUIRES_S1_S2 = SystemMessageId(1959)
            THIS_IS_NOT_A_SUITABLE_ITEM = SystemMessageId(1960)
            GEMSTONE_QUANTITY_IS_INCORRECT = SystemMessageId(1961)
            THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED = SystemMessageId(1962)
            SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION = SystemMessageId(1963)
            AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM = SystemMessageId(1964)
            AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1 = SystemMessageId(1965)
            ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS = SystemMessageId(1966)
            GATE_LOCKED_TRY_AGAIN_LATER = SystemMessageId(1967)
            S1_OWNER = SystemMessageId(1968)
            AREA_S1_APPEARS = SystemMessageId(1969)
            ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN = SystemMessageId(1970)
            HARDENER_LEVEL_TOO_HIGH = SystemMessageId(1971)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION = SystemMessageId(1972)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN = SystemMessageId(1973)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD = SystemMessageId(1974)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING = SystemMessageId(1975)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED = SystemMessageId(1976)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING = SystemMessageId(1977)
            YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN = SystemMessageId(1978)
            S1S_REMAINING_MANA_IS_NOW_10 = SystemMessageId(1979)
            S1S_REMAINING_MANA_IS_NOW_5 = SystemMessageId(1980)
            S1S_REMAINING_MANA_IS_NOW_1 = SystemMessageId(1981)
            S1S_REMAINING_MANA_IS_NOW_0 = SystemMessageId(1982)
            PRESS_THE_AUGMENT_BUTTON_TO_BEGIN = SystemMessageId(1984)
            S1_DROP_AREA_S2 = SystemMessageId(1985)
            S1_OWNER_S2 = SystemMessageId(1986)
            S1 = SystemMessageId(1987)
            FERRY_ARRIVED_AT_PRIMEVAL = SystemMessageId(1988)
            FERRY_LEAVING_FOR_RUNE_3_MINUTES = SystemMessageId(1989)
            FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW = SystemMessageId(1990)
            FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES = SystemMessageId(1991)
            FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW = SystemMessageId(1992)
            FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED = SystemMessageId(1993)
            FERRY_FROM_RUNE_TO_PRIMEVAL_DELAYED = SystemMessageId(1994)
            S1_CHANNEL_FILTER_OPTION = SystemMessageId(1995)
            ATTACK_WAS_BLOCKED = SystemMessageId(1996)
            S1_PERFORMING_COUNTERATTACK = SystemMessageId(1997)
            COUNTERED_S1_ATTACK = SystemMessageId(1998)
            S1_DODGES_ATTACK = SystemMessageId(1999)
            AVOIDED_S1_ATTACK2 = SystemMessageId(2000)
            AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS = SystemMessageId(2001)
            TRAP_FAILED = SystemMessageId(2002)
            OBTAINED_ORDINARY_MATERIAL = SystemMessageId(2003)
            OBTAINED_RATE_MATERIAL = SystemMessageId(2004)
            OBTAINED_UNIQUE_MATERIAL = SystemMessageId(2005)
            OBTAINED_ONLY_MATERIAL = SystemMessageId(2006)
            ENTER_RECIPIENTS_NAME = SystemMessageId(2007)
            ENTER_TEXT = SystemMessageId(2008)
            CANT_EXCEED_1500_CHARACTERS = SystemMessageId(2009)
            S2_S1 = SystemMessageId(2010)
            AUGMENTED_ITEM_CANNOT_BE_DISCARDED = SystemMessageId(2011)
            S1_HAS_BEEN_ACTIVATED = SystemMessageId(2012)
            YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE = SystemMessageId(2013)
            MANOR_CANT_ACCEPT_MORE_CROPS = SystemMessageId(2014)
            SKILL_READY_TO_USE_AGAIN = SystemMessageId(2015)
            SKILL_READY_TO_USE_AGAIN_BUT_TIME_INCREASED = SystemMessageId(2016)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE = SystemMessageId(2017)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING = SystemMessageId(2018)
            S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT = SystemMessageId(2019)
            S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA =
                SystemMessageId(2020)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE = SystemMessageId(2021)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL = SystemMessageId(2022)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE = SystemMessageId(2023)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD = SystemMessageId(2024)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR = SystemMessageId(2025)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_SIEGE_WAR = SystemMessageId(2026)
            S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER = SystemMessageId(2027)
            S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY = SystemMessageId(2028)
            S1_CANNOT_PARTICIPATE_IN_OLYMPIAD_DURING_TELEPORT = SystemMessageId(2029)
            CURRENTLY_LOGGING_IN = SystemMessageId(2030)
            PLEASE_WAIT_A_MOMENT = SystemMessageId(2031)

            buildFastLookupTable()
        }

        private fun buildFastLookupTable() {
            val fields = SystemMessageId::class.java.declaredFields
            val smIds = ArrayList<SystemMessageId>(fields.size)

            var maxId = 0
            var mod: Int
            var smId: SystemMessageId
            for (field in fields) {
                mod = field.modifiers
                if (Modifier.isStatic(mod) && Modifier.isPrivate(mod) && Modifier.isFinal(mod) && field.type == SystemMessageId::class.java) {
                    try {
                        field.isAccessible = true
                        smId = field.get(null) as SystemMessageId
                        smId.name = field.name
                        smId.paramCount = parseMessageParameters(field.name)
                        maxId = Math.max(maxId, smId.id)
                        smIds.add(smId)
                        field.isAccessible = false
                    } catch (e: Exception) {
                        LOGGER.error("Failed to access field for '{}'.", e, field.name)
                    }

                }
            }

            VALUES = arrayOfNulls(maxId + 1)
            var i = smIds.size
            while (i-- > 0) {
                smId = smIds[i]
                VALUES!![smId.id] = smId
            }
        }

        private fun parseMessageParameters(name: String): Int {
            var paramCount = 0
            var s1: Char
            var c2: Char
            var i = 0
            while (i < name.length - 1) {
                s1 = name[i]
                if (s1 == 'C' || s1 == 'S') {
                    c2 = name[i + 1]
                    if (Character.isDigit(c2)) {
                        paramCount = Math.max(paramCount, Character.getNumericValue(c2))
                        i++
                    }
                }
                i++
            }
            return paramCount
        }

        fun getSystemMessageId(id: Int): SystemMessageId {
            val smi = getSystemMessageIdInternal(id)
            return smi ?: SystemMessageId(id)
        }

        private fun getSystemMessageIdInternal(id: Int): SystemMessageId? {
            return if (id < 0 || id >= VALUES!!.size) null else VALUES!![id]

        }

        fun getSystemMessageId(name: String): SystemMessageId? {
            try {
                return SystemMessageId::class.java.getField(name).get(null) as SystemMessageId
            } catch (e: Exception) {
                return null
            }

        }
    }
}