package com.l2kt.gameserver.network.clientpackets;

import com.l2kt.Config;
import com.l2kt.commons.lang.StringUtil;
import com.l2kt.gameserver.data.sql.PlayerInfoTable;
import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.data.xml.PlayerData;
import com.l2kt.gameserver.data.xml.ScriptData;
import com.l2kt.gameserver.model.L2ShortCut;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.template.PlayerTemplate;
import com.l2kt.gameserver.model.base.Sex;
import com.l2kt.gameserver.model.holder.skillnode.GeneralSkillNode;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.Item;

import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.network.serverpackets.CharCreateFail;
import com.l2kt.gameserver.network.serverpackets.CharCreateOk;
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo;
import com.l2kt.gameserver.scripting.Quest;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		// Invalid race.
		if (_race > 4 || _race < 0)
		{
			sendPacket(CharCreateFail.Companion.getREASON_CREATION_FAILED());
			return;
		}
		
		// Invalid face.
		if (_face > 2 || _face < 0)
		{
			sendPacket(CharCreateFail.Companion.getREASON_CREATION_FAILED());
			return;
		}
		
		// Invalid hair style.
		if (_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			sendPacket(CharCreateFail.Companion.getREASON_CREATION_FAILED());
			return;
		}
		
		// Invalid hair color.
		if (_hairColor > 3 || _hairColor < 0)
		{
			sendPacket(CharCreateFail.Companion.getREASON_CREATION_FAILED());
			return;
		}
		
		// Invalid name length, or name typo.
		if (!StringUtil.INSTANCE.isValidString(_name, "^[A-Za-z0-9]{3,16}$"))
		{
			sendPacket((_name.length() > 16) ? CharCreateFail.Companion.getREASON_16_ENG_CHARS() : CharCreateFail.Companion.getREASON_INCORRECT_NAME());
			return;
		}
		
		// Your name is already taken by a NPC.
		if (NpcData.getInstance().getTemplateByName(_name) != null)
		{
			sendPacket(CharCreateFail.Companion.getREASON_INCORRECT_NAME());
			return;
		}
		
		// You already have the maximum amount of characters for this account.
		if (PlayerInfoTable.getInstance().getCharactersInAcc(getClient().getAccountName()) >= 7)
		{
			sendPacket(CharCreateFail.Companion.getREASON_TOO_MANY_CHARACTERS());
			return;
		}
		
		// The name already exists.
		if (PlayerInfoTable.getInstance().getPlayerObjectId(_name) > 0)
		{
			sendPacket(CharCreateFail.Companion.getREASON_NAME_ALREADY_EXISTS());
			return;
		}
		
		// The class id related to this template is post-newbie.
		final PlayerTemplate template = PlayerData.getInstance().getTemplate(_classId);
		if (template == null || template.getClassBaseLevel() > 1)
		{
			sendPacket(CharCreateFail.Companion.getREASON_CREATION_FAILED());
			return;
		}
		
		// Create the player Object.
		final Player player = Player.create(IdFactory.getInstance().getNextId(), template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, Sex.values()[_sex]);
		if (player == null)
		{
			sendPacket(CharCreateFail.Companion.getREASON_CREATION_FAILED());
			return;
		}
		
		// Set default values.
		player.setCurrentCp(0);
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
		
		// send acknowledgement
		sendPacket(CharCreateOk.Companion.getSTATIC_PACKET());
		
		World.getInstance().addObject(player);
		
		player.getPosition().set(template.getRandomSpawn());
		player.setTitle("");
		
		// Register shortcuts.
		player.registerShortCut(new L2ShortCut(0, 0, 3, 2, -1, 1)); // attack shortcut
		player.registerShortCut(new L2ShortCut(3, 0, 3, 5, -1, 1)); // take shortcut
		player.registerShortCut(new L2ShortCut(10, 0, 3, 0, -1, 1)); // sit shortcut
		
		// Equip or add items, based on template.
		for (int itemId : template.getItemIds())
		{
			final ItemInstance item = player.getInventory().addItem("Init", itemId, 1, player, null);
			if (itemId == 5588) // tutorial book shortcut
				player.registerShortCut(new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1));
			
			if (item.isEquipable())
			{
				if (player.getActiveWeaponItem() == null || !(item.getItem().getType2() != Item.TYPE2_WEAPON))
					player.getInventory().equipItemAndRecord(item);
			}
		}
		
		// Add skills.
		for (GeneralSkillNode skill : player.getAvailableAutoGetSkills())
		{
			if (skill.getId() == 1001 || skill.getId() == 1177)
				player.registerShortCut(new L2ShortCut(1, 0, 2, skill.getId(), 1, 1));
			
			if (skill.getId() == 1216)
				player.registerShortCut(new L2ShortCut(9, 0, 2, skill.getId(), 1, 1));
		}
		
		// Tutorial runs here.
		if (!Config.DISABLE_TUTORIAL)
		{
			if (player.getQuestState("Tutorial") == null)
			{
				final Quest quest = ScriptData.getInstance().getQuest("Tutorial");
				if (quest != null)
					quest.newQuestState(player).setState(Quest.STATE_STARTED);
			}
		}
		
		player.setOnlineStatus(true, false);
		player.deleteMe();
		
		final CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().getPlayOkID1());
		sendPacket(csi);
		getClient().setCharSelectSlot(csi.getCharacterSlots());
	}
}