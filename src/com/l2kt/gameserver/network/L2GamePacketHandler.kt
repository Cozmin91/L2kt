package com.l2kt.gameserver.network

import com.l2kt.Config
import com.l2kt.commons.lang.HexUtil
import com.l2kt.commons.mmocore.*
import com.l2kt.gameserver.network.L2GameClient.GameClientState
import com.l2kt.gameserver.network.clientpackets.*

import java.nio.ByteBuffer
import java.util.logging.Logger

/**
 * The Stateful approach prevents the server from handling inconsistent packets.<BR></BR>
 * <BR></BR>
 * Note : If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 * @author KenM
 */
class L2GamePacketHandler : IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient> {

    override fun handlePacket(buf: ByteBuffer, client: L2GameClient): ReceivablePacket<L2GameClient>? {
        if (client.dropPacket())
            return null

        val opcode = buf.get().toInt() and 0xFF

        var msg: ReceivablePacket<L2GameClient>? = null
        val state = client.state

        when (state) {
            L2GameClient.GameClientState.CONNECTED -> when (opcode) {
                0x00 -> msg = ProtocolVersion()
                0x08 -> msg = AuthLogin()
                else -> printDebug(opcode, buf, state, client)
            }
            L2GameClient.GameClientState.AUTHED -> when (opcode) {
                0x09 -> msg = Logout()
                0x0b -> msg = CharacterCreate()
                0x0c -> msg = CharacterDelete()
                0x0d -> msg = CharacterSelected()
                0x0e -> msg = NewCharacter()
                0x62 -> msg = CharacterRestore()
                0x68 -> msg = RequestPledgeCrest()
                else -> printDebug(opcode, buf, state, client)
            }
            L2GameClient.GameClientState.IN_GAME -> when (opcode) {
                0x01 -> msg = MoveBackwardToLocation()
                // case 0x02:
                // // Say ... not used any more ??
                // break;
                0x03 -> msg = EnterWorld()
                0x04 -> msg = Action()
                0x09 -> msg = Logout()
                0x0a -> msg = AttackRequest()
                0x0f -> msg = RequestItemList()
                // case 0x10:
                // // RequestEquipItem ... not used any more, instead "useItem"
                // break;
                0x11 -> msg = RequestUnEquipItem()
                0x12 -> msg = RequestDropItem()
                0x14 -> msg = UseItem()
                0x15 -> msg = TradeRequest()
                0x16 -> msg = AddTradeItem()
                0x17 -> msg = TradeDone()
                0x1a -> msg = DummyPacket()
                0x1b -> msg = RequestSocialAction()
                0x1c -> msg = RequestChangeMoveType()
                0x1d -> msg = RequestChangeWaitType()
                0x1e -> msg = RequestSellItem()
                0x1f -> msg = RequestBuyItem()
                0x20 -> msg = RequestLinkHtml()
                0x21 -> msg = RequestBypassToServer()
                0x22 -> msg = RequestBBSwrite()
                0x23 -> msg = DummyPacket()
                0x24 -> msg = RequestJoinPledge()
                0x25 -> msg = RequestAnswerJoinPledge()
                0x26 -> msg = RequestWithdrawPledge()
                0x27 -> msg = RequestOustPledgeMember()
                // case 0x28:
                // // RequestDismissPledge
                // break;
                0x29 -> msg = RequestJoinParty()
                0x2a -> msg = RequestAnswerJoinParty()
                0x2b -> msg = RequestWithdrawParty()
                0x2c -> msg = RequestOustPartyMember()
                0x2d -> {
                }
                0x2e -> msg = DummyPacket()
                0x2f -> msg = RequestMagicSkillUse()
                0x30 -> msg = Appearing() // (after death)
                0x31 -> if (Config.ALLOW_WAREHOUSE)
                    msg = SendWarehouseDepositList()
                0x32 -> msg = SendWarehouseWithdrawList()
                0x33 -> msg = RequestShortCutReg()
                0x34 -> msg = DummyPacket()
                0x35 -> msg = RequestShortCutDel()
                0x36 -> msg = CannotMoveAnymore()
                0x37 -> msg = RequestTargetCanceld()
                0x38 -> msg = Say2()
                0x3c -> msg = RequestPledgeMemberList()
                0x3e -> msg = DummyPacket()
                0x3f -> msg = RequestSkillList()
                // case 0x41:
                // // MoveWithDelta ... unused ?? or only on ship ??
                // break;
                0x42 -> msg = RequestGetOnVehicle()
                0x43 -> msg = RequestGetOffVehicle()
                0x44 -> msg = AnswerTradeRequest()
                0x45 -> msg = RequestActionUse()
                0x46 -> msg = RequestRestart()
                // case 0x47:
                // // RequestSiegeInfo
                // break;
                0x48 -> msg = ValidatePosition()
                // case 0x49:
                // // RequestSEKCustom
                // break;
                0x4a -> msg = StartRotating()
                0x4b -> msg = FinishRotating()
                0x4d -> msg = RequestStartPledgeWar()
                0x4e -> msg = RequestReplyStartPledgeWar()
                0x4f -> msg = RequestStopPledgeWar()
                0x50 -> msg = RequestReplyStopPledgeWar()
                0x51 -> msg = RequestSurrenderPledgeWar()
                0x52 -> msg = RequestReplySurrenderPledgeWar()
                0x53 -> msg = RequestSetPledgeCrest()
                0x55 -> msg = RequestGiveNickName()
                0x57 -> msg = RequestShowBoard()
                0x58 -> msg = RequestEnchantItem()
                0x59 -> msg = RequestDestroyItem()
                0x5b -> msg = SendBypassBuildCmd()
                0x5c -> msg = RequestMoveToLocationInVehicle()
                0x5d -> msg = CannotMoveAnymoreInVehicle()
                0x5e -> msg = RequestFriendInvite()
                0x5f -> msg = RequestAnswerFriendInvite()

                0x60 -> msg = RequestFriendList()
                0x61 -> msg = RequestFriendDel()
                0x63 -> msg = RequestQuestList()
                0x64 -> msg = RequestQuestAbort()
                0x66 -> msg = RequestPledgeInfo()
                // case 0x67:
                // // RequestPledgeExtendedInfo
                // break;
                0x68 -> msg = RequestPledgeCrest()
                0x69 -> msg = RequestSurrenderPersonally()
                // case 0x6a:
                // // Ride
                // break;
                0x6b // send when talking to trainer npc, to show list of available skills
                -> msg = RequestAcquireSkillInfo()
                0x6c // send when a skill to be learned is selected
                -> msg = RequestAcquireSkill()
                0x6d -> msg = RequestRestartPoint()
                0x6e -> msg = RequestGMCommand()
                0x6f -> msg = RequestPartyMatchConfig()
                0x70 -> msg = RequestPartyMatchList()
                0x71 -> msg = RequestPartyMatchDetail()
                0x72 -> msg = RequestCrystallizeItem()
                0x73 -> msg = RequestPrivateStoreManageSell()
                0x74 -> msg = SetPrivateStoreListSell()
                // case 0x75:
                // msg = new RequestPrivateStoreManageCancel(data, _client);
                // break;
                0x76 -> msg = RequestPrivateStoreQuitSell()
                0x77 -> msg = SetPrivateStoreMsgSell()
                // case 0x78:
                // // RequestPrivateStoreList
                // break;
                0x79 -> msg = RequestPrivateStoreBuy()
                // case 0x7a:
                // // ReviveReply
                // break;
                0x7b -> msg = RequestTutorialLinkHtml()
                0x7c -> msg = RequestTutorialPassCmdToServer()
                0x7d -> msg = RequestTutorialQuestionMark()
                0x7e -> msg = RequestTutorialClientEvent()
                0x7f -> msg = RequestPetition()
                0x80 -> msg = RequestPetitionCancel()
                0x81 -> msg = RequestGmList()
                0x82 -> msg = RequestJoinAlly()
                0x83 -> msg = RequestAnswerJoinAlly()
                0x84 -> msg = AllyLeave()
                0x85 -> msg = AllyDismiss()
                0x86 -> msg = RequestDismissAlly()
                0x87 -> msg = RequestSetAllyCrest()
                0x88 -> msg = RequestAllyCrest()
                0x89 -> msg = RequestChangePetName()
                0x8a -> msg = RequestPetUseItem()
                0x8b -> msg = RequestGiveItemToPet()
                0x8c -> msg = RequestGetItemFromPet()
                0x8e -> msg = RequestAllyInfo()
                0x8f -> msg = RequestPetGetItem()
                0x90 -> msg = RequestPrivateStoreManageBuy()
                0x91 -> msg = SetPrivateStoreListBuy()
                // case 0x92:
                // // RequestPrivateStoreBuyManageCancel
                // break;
                0x93 -> msg = RequestPrivateStoreQuitBuy()
                0x94 -> msg = SetPrivateStoreMsgBuy()
                // case 0x95:
                // // RequestPrivateStoreBuyList
                // break;
                0x96 -> msg = RequestPrivateStoreSell()
                // case 0x97:
                // // SendTimeCheckPacket
                // break;
                // case 0x98:
                // // RequestStartAllianceWar
                // break;
                // case 0x99:
                // // ReplyStartAllianceWar
                // break;
                // case 0x9a:
                // // RequestStopAllianceWar
                // break;
                // case 0x9b:
                // // ReplyStopAllianceWar
                // break;
                // case 0x9c:
                // // RequestSurrenderAllianceWar
                // break;
                0x9d -> {
                }
                0x9e -> msg = RequestPackageSendableItemList()
                0x9f -> msg = RequestPackageSend()
                0xa0 -> msg = RequestBlock()
                // case 0xa1:
                // // RequestCastleSiegeInfo
                // break;
                0xa2 -> msg = RequestSiegeAttackerList()
                0xa3 -> msg = RequestSiegeDefenderList()
                0xa4 -> msg = RequestJoinSiege()
                0xa5 -> msg = RequestConfirmSiegeWaitingList()
                // case 0xa6:
                // // RequestSetCastleSiegeTime
                // break;
                0xa7 -> msg = MultiSellChoose()
                // case 0xa8:
                // // NetPing
                // break;
                0xaa -> msg = RequestUserCommand()
                0xab -> msg = SnoopQuit()
                0xac // we still need this packet to handle BACK button of craft dialog
                -> msg = RequestRecipeBookOpen()
                0xad -> msg = RequestRecipeBookDestroy()
                0xae -> msg = RequestRecipeItemMakeInfo()
                0xaf -> msg = RequestRecipeItemMakeSelf()

                // case 0xb0:
                // msg = new RequestRecipeShopManageList(data, client);
                // break;
                0xb1 -> msg = RequestRecipeShopMessageSet()
                0xb2 -> msg = RequestRecipeShopListSet()
                0xb3 -> msg = RequestRecipeShopManageQuit()
                0xb5 -> msg = RequestRecipeShopMakeInfo()
                0xb6 -> msg = RequestRecipeShopMakeItem()
                0xb7 -> msg = RequestRecipeShopManagePrev()
                0xb8 -> msg = ObserverReturn()
                0xb9 -> msg = RequestEvaluate()
                0xba -> msg = RequestHennaList()
                0xbb -> msg = RequestHennaItemInfo()
                0xbc -> msg = RequestHennaEquip()
                0xbd -> msg = RequestHennaRemoveList()
                0xbe -> msg = RequestHennaItemRemoveInfo()
                0xbf -> msg = RequestHennaRemove()
                0xc0 ->
                    // Clan Privileges
                    msg = RequestPledgePower()
                0xc1 -> msg = RequestMakeMacro()
                0xc2 -> msg = RequestDeleteMacro()
                // Manor
                0xc3 -> msg = RequestBuyProcure()
                0xc4 -> msg = RequestBuySeed()
                0xc5 -> msg = DlgAnswer()
                0xc6 -> msg = RequestPreviewItem()
                0xc7 -> msg = RequestSSQStatus()
                0xCA -> msg = GameGuardReply()
                0xcc -> msg = RequestSendFriendMsg()
                0xcd -> msg = RequestShowMiniMap()
                0xce // MSN dialogs so that you dont see them in the console.
                -> {
                }
                0xcf // record video
                -> msg = RequestRecordInfo()

                0xd0 -> {
                    var id2 = -1
                    if (buf.remaining() >= 2) {
                        id2 = buf.short.toInt() and 0xffff
                    } else {
                        _log.warning("Client: $client sent a 0xd0 without the second opcode.")
                        return null
                    }

                    when (id2) {
                        1 -> msg = RequestOustFromPartyRoom()
                        2 -> msg = RequestDismissPartyRoom()
                        3 -> msg = RequestWithdrawPartyRoom()
                        4 -> msg = RequestChangePartyLeader()
                        5 -> msg = RequestAutoSoulShot()
                        6 -> msg = RequestExEnchantSkillInfo()
                        7 -> msg = RequestExEnchantSkill()
                        8 -> msg = RequestManorList()
                        9 -> msg = RequestProcureCropList()
                        0x0a -> msg = RequestSetSeed()
                        0x0b -> msg = RequestSetCrop()
                        0x0c -> msg = RequestWriteHeroWords()
                        0x0d -> msg = RequestExAskJoinMPCC()
                        0x0e -> msg = RequestExAcceptJoinMPCC()
                        0x0f -> msg = RequestExOustFromMPCC()
                        0x10 -> msg = RequestExPledgeCrestLarge()
                        0x11 -> msg = RequestExSetPledgeCrestLarge()
                        0x12 -> msg = RequestOlympiadObserverEnd()
                        0x13 -> msg = RequestOlympiadMatchList()
                        0x14 -> msg = RequestAskJoinPartyRoom()
                        0x15 -> msg = AnswerJoinPartyRoom()
                        0x16 -> msg = RequestListPartyMatchingWaitingRoom()
                        0x17 -> msg = RequestExitPartyMatchingWaitingRoom()
                        0x18 -> msg = RequestGetBossRecord()
                        0x19 -> msg = RequestPledgeSetAcademyMaster()
                        0x1a -> msg = RequestPledgePowerGradeList()
                        0x1b -> msg = RequestPledgeMemberPowerInfo()
                        0x1c -> msg = RequestPledgeSetMemberPowerGrade()
                        0x1d -> msg = RequestPledgeMemberInfo()
                        0x1e -> msg = RequestPledgeWarList()
                        0x1f -> msg = RequestExFishRanking()
                        0x20 -> msg = RequestPCCafeCouponUse()
                        // couldnt find it 0x21 :S
                        0x22 -> msg = RequestCursedWeaponList()
                        0x23 -> msg = RequestCursedWeaponLocation()
                        0x24 -> msg = RequestPledgeReorganizeMember()
                        // couldnt find it 0x25 :S
                        0x26 -> msg = RequestExMPCCShowPartyMembersInfo()
                        0x27 -> msg = RequestDuelStart()
                        0x28 -> msg = RequestDuelAnswerStart()
                        0x29 -> msg = RequestConfirmTargetItem()
                        0x2a -> msg = RequestConfirmRefinerItem()
                        0x2b -> msg = RequestConfirmGemStone()
                        0x2c -> msg = RequestRefine()
                        0x2d -> msg = RequestConfirmCancelItem()
                        0x2e -> msg = RequestRefineCancel()
                        0x2f -> msg = RequestExMagicSkillUseGround()
                        0x30 -> msg = RequestDuelSurrender()
                        else -> printDebugDoubleOpcode(opcode, id2, buf, state, client)
                    }
                }
                /*
					 * case 0xee: msg = new RequestChangePartyLeader(data, _client); break;
					 */
                else -> printDebug(opcode, buf, state, client)
            }// RequestDismissParty
            // RequestSkillCoolTime
        }
        return msg
    }

    // impl
    override fun create(con: MMOConnection<L2GameClient>): L2GameClient {
        return L2GameClient(con)
    }

    override fun execute(rp: ReceivablePacket<L2GameClient>) {
        rp.client.execute(rp)
    }

    companion object {
        private val _log = Logger.getLogger(L2GamePacketHandler::class.java.name)

        private fun printDebug(opcode: Int, buf: ByteBuffer, state: GameClientState, client: L2GameClient) {
            client.onUnknownPacket()
            if (!Config.PACKET_HANDLER_DEBUG)
                return

            val size = buf.remaining()
            _log.warning("Unknown Packet: 0x" + Integer.toHexString(opcode) + " on State: " + state.name + " Client: " + client.toString())
            val array = ByteArray(size)
            buf.get(array)
            _log.warning(HexUtil.printData(array, size))
        }

        private fun printDebugDoubleOpcode(
            opcode: Int,
            id2: Int,
            buf: ByteBuffer,
            state: GameClientState,
            client: L2GameClient
        ) {
            client.onUnknownPacket()
            if (!Config.PACKET_HANDLER_DEBUG)
                return

            val size = buf.remaining()
            _log.warning("Unknown Packet: 0x" + Integer.toHexString(opcode) + ":" + Integer.toHexString(id2) + " on State: " + state.name + " Client: " + client.toString())
            val array = ByteArray(size)
            buf.get(array)
            _log.warning(HexUtil.printData(array, size))
        }
    }
}