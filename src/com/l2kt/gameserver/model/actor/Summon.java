package com.l2kt.gameserver.model.actor;

import com.l2kt.Config;
import com.l2kt.gameserver.data.ItemTable;
import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.handler.IItemHandler;
import com.l2kt.gameserver.handler.ItemHandler;
import com.l2kt.gameserver.model.L2Skill;
import com.l2kt.gameserver.model.ShotType;
import com.l2kt.gameserver.model.WorldObject;
import com.l2kt.gameserver.model.actor.ai.CtrlIntention;
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI;
import com.l2kt.gameserver.model.actor.ai.type.SummonAI;
import com.l2kt.gameserver.model.actor.instance.Door;
import com.l2kt.gameserver.model.actor.instance.Pet;
import com.l2kt.gameserver.model.actor.instance.Player;
import com.l2kt.gameserver.model.actor.instance.Servitor;
import com.l2kt.gameserver.model.actor.stat.SummonStat;
import com.l2kt.gameserver.model.actor.status.SummonStatus;
import com.l2kt.gameserver.model.actor.template.NpcTemplate;
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType;
import com.l2kt.gameserver.model.base.Experience;
import com.l2kt.gameserver.model.group.Party;
import com.l2kt.gameserver.model.item.instance.ItemInstance;
import com.l2kt.gameserver.model.item.kind.Weapon;
import com.l2kt.gameserver.model.itemcontainer.PetInventory;
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager;
import com.l2kt.gameserver.network.SystemMessageId;
import com.l2kt.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import com.l2kt.gameserver.network.serverpackets.*;

import java.util.List;

import static com.l2kt.gameserver.model.item.type.ActionType.summon_soulshot;
import static com.l2kt.gameserver.model.item.type.ActionType.summon_spiritshot;

public abstract class Summon extends Playable
{
	private Player _owner;
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	
	private int _shotsMask = 0;
	
	public Summon(int objectId, NpcTemplate template, Player owner)
	{
		super(objectId, template);
		
		for (L2Skill skill : template.getSkills(SkillType.PASSIVE))
			addStatFuncs(skill.getStatFuncs(this));
		
		_showSummonAnimation = true;
		_owner = owner;
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new SummonStat(this));
	}
	
	@Override
	public SummonStat getStat()
	{
		return (SummonStat) super.getStat();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new SummonStatus(this));
	}
	
	@Override
	public SummonStatus getStatus()
	{
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new SummonAI(this);
				
				return _ai;
			}
		}
		return ai;
	}
	
	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}
	
	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();
	
	@Override
	public void updateAbnormalEffect()
	{
		for (Player player : getKnownType(Player.class))
			player.sendPacket(new SummonInfo(this, player, 1));
	}
	
	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else if (player == _owner)
		{
			// Calculate the distance between the Player and the L2Npc
			if (!canInteract(player))
			{
				// Notify the Player AI with INTERACT
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				// Stop moving if we're already in interact range.
				if (player.isMoving() || player.isInCombat())
					player.getAI().setIntention(CtrlIntention.IDLE);
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				player.sendPacket(new PetStatusShow(this));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			}
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (GeoEngine.INSTANCE.canSeeTarget(player, this))
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
				
				if (GeoEngine.INSTANCE.canSeeTarget(player, this))
					player.getAI().setIntention(CtrlIntention.FOLLOW, this);
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.isGM())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/admin/petinfo.htm");
			html.replace("%name%", getName() == null ? "N/A" : getName());
			html.replace("%level%", getLevel());
			html.replace("%exp%", getStat().getExp());
			html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + getActingPlayer().getName() + "\">" + getActingPlayer().getName() + "</a>");
			html.replace("%class%", getClass().getSimpleName());
			html.replace("%ai%", hasAI() ? getAI().getDesire().getIntention().name() : "NULL");
			html.replace("%hp%", (int) getStatus().getCurrentHp() + "/" + getStat().getMaxHp());
			html.replace("%mp%", (int) getStatus().getCurrentMp() + "/" + getStat().getMaxMp());
			html.replace("%karma%", getKarma());
			html.replace("%undead%", isUndead() ? "yes" : "no");
			
			if (this instanceof Pet)
			{
				html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + getActingPlayer().getObjectId() + "\">view</a>");
				html.replace("%food%", ((Pet) this).getCurrentFed() + "/" + ((Pet) this).getPetData().getMaxMeal());
				html.replace("%load%", ((Pet) this).getInventory().getTotalWeight() + "/" + ((Pet) this).getMaxLoad());
			}
			else
			{
				html.replace("%inv%", "none");
				html.replace("%food%", "N/A");
				html.replace("%load%", "N/A");
			}
			
			player.sendPacket(html);
		}
		super.onActionShift(player);
	}
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= Experience.INSTANCE.getLEVEL().length)
			return 0;
		
		return Experience.INSTANCE.getLEVEL()[getLevel()];
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= Experience.INSTANCE.getLEVEL().length - 1)
			return 0;
		
		return Experience.INSTANCE.getLEVEL()[getLevel() + 1];
	}
	
	@Override
	public final int getKarma()
	{
		return getOwner() != null ? getOwner().getKarma() : 0;
	}
	
	@Override
	public final byte getPvpFlag()
	{
		return getOwner() != null ? getOwner().getPvpFlag() : 0;
	}
	
	public final int getTeam()
	{
		return getOwner() != null ? getOwner().getTeam() : 0;
	}
	
	public final Player getOwner()
	{
		return _owner;
	}
	
	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}
	
	public int getMaxLoad()
	{
		return 0;
	}
	
	public int getSoulShotsPerHit()
	{
		return getTemplate().getSsCount();
	}
	
	public int getSpiritShotsPerHit()
	{
		return getTemplate().getSpsCount();
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Disable beastshots
		for (int itemId : getOwner().getAutoSoulShot())
		{
			switch (ItemTable.INSTANCE.getTemplate(itemId).getDefaultAction())
			{
				case summon_soulshot:
				case summon_spiritshot:
					getOwner().disableAutoShot(itemId);
					break;
			}
		}
		return true;
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		updateAndBroadcastStatus(1);
	}
	
	public void deleteMe(Player owner)
	{
		owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
		
		decayMe();
		owner.setPet(null);
		super.deleteMe();
	}
	
	public void unSummon(Player owner)
	{
		if (isVisible() && !isDead())
		{
			abortCast();
			abortAttack();
			
			stopHpMpRegeneration();
			getAI().stopFollow();
			
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
			
			store();
			owner.setPet(null);
			
			// Stop AI tasks
			if (hasAI())
				getAI().stopAITask();
			
			stopAllEffects();
			
			decayMe();
			
			setTarget(null);
			
			// Disable beastshots
			for (int itemId : owner.getAutoSoulShot())
			{
				switch (ItemTable.INSTANCE.getTemplate(itemId).getDefaultAction())
				{
					case summon_soulshot:
					case summon_spiritshot:
						owner.disableAutoShot(itemId);
						break;
				}
			}
		}
	}
	
	public int getAttackRange()
	{
		return 36;
	}
	
	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if (_follow)
			getAI().setIntention(CtrlIntention.FOLLOW, getOwner());
		else
			getAI().setIntention(CtrlIntention.IDLE, null);
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	public int getControlItemId()
	{
		return 0;
	}
	
	public Weapon getActiveWeapon()
	{
		return null;
	}
	
	@Override
	public PetInventory getInventory()
	{
		return null;
	}
	
	public void store()
	{
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	/**
	 * Return True if the L2Summon is invulnerable or if the summoner is in spawn protection.<BR>
	 * <BR>
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || getOwner().isSpawnProtected();
	}
	
	/**
	 * Return the Party of its owner, or null.
	 */
	@Override
	public Party getParty()
	{
		return (_owner == null) ? null : _owner.getParty();
	}
	
	/**
	 * Return True if the Summon owner has a Party in progress.
	 */
	@Override
	public boolean isInParty()
	{
		return (_owner == null) ? false : _owner.getParty() != null;
	}
	
	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the target is correct</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the summon owns enough HP and MP to cast the skill</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li><BR>
	 * <BR>
	 * <li>Check if the skill is active</li><BR>
	 * <BR>
	 * <li>Notify the AI with CAST and target</li><BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null || isDead())
			return false;
		
		// Check if the skill is active and ignore the passive skill request
		if (skill.isPassive())
			return false;
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used
		if (isCastingNow())
			return false;
		
		// Set current pet skill
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		
		// ************************************* Check Target *******************************************
		
		// Get the target for the skill
		WorldObject target = null;
		
		switch (skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(SystemMessageId.Companion.getTARGET_CANT_FOUND());
			return false;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (e.g. reuse time)
		if (isSkillDisabled(skill))
		{
			sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.Companion.getS1_PREPARED_FOR_REUSE()).addString(skill.getName()));
			return false;
		}
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the summon has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.Companion.getNOT_ENOUGH_MP());
			return false;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.Companion.getNOT_ENOUGH_HP());
			return false;
		}
		
		// ************************************* Check Summon State *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target))
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(SystemMessage.Companion.getSystemMessage(SystemMessageId.Companion.getTARGET_IN_PEACEZONE()));
				return false;
			}
			
			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				// if Player is in Olympia and the match isn't already start, send ActionFailed
				sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
				return false;
			}
			
			// Check if the target is attackable
			if (target instanceof Door)
			{
				if (!((Door) target).isAutoAttackable(getOwner()))
					return false;
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && !getOwner().getAccessLevel().allowPeaceAttack())
					return false;
				
				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_FRONT_AURA && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_BEHIND_AURA && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA_UNDEAD && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_CLAN && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_ALLY && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
					return false;
			}
		}
		
		// Notify the AI with CAST and target
		getAI().setIntention(CtrlIntention.CAST, skill, target);
		return true;
	}
	
	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);
		
		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			// if immobilized, disable follow mode
			if (_previousFollowStatus)
				setFollowStatus(false);
		}
		else
		{
			// if not more immobilized, restore follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}
	
	public void setOwner(Player newOwner)
	{
		_owner = newOwner;
	}
	
	@Override
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || getOwner() == null)
			return;
		
		// Prevents the double spam of system messages, if the target is the owning player.
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (pcrit || mcrit)
				if (this instanceof Servitor)
					sendPacket(SystemMessageId.Companion.getCRITICAL_HIT_BY_SUMMONED_MOB());
				else
					sendPacket(SystemMessageId.Companion.getCRITICAL_HIT_BY_PET());
				
			final SystemMessage sm;
			
			if (target.isInvul())
			{
				if (target.isParalyzed())
					sm = SystemMessage.Companion.getSystemMessage(SystemMessageId.Companion.getOPPONENT_PETRIFIED());
				else
					sm = SystemMessage.Companion.getSystemMessage(SystemMessageId.Companion.getATTACK_WAS_BLOCKED());
			}
			else
				sm = SystemMessage.Companion.getSystemMessage(SystemMessageId.Companion.getPET_HIT_FOR_S1_DAMAGE()).addNumber(damage);
			
			sendPacket(sm);
			
			if (getOwner().isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == getOwner().getOlympiadGameId())
			{
				OlympiadGameManager.INSTANCE.notifyCompetitorDamage(getOwner(), damage);
			}
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		final Player actingPlayer = getActingPlayer();
		if (!actingPlayer.checkPvpSkill(getTarget(), skill) && !actingPlayer.getAccessLevel().allowPeaceAttack())
		{
			// Send a System Message to the Player
			actingPlayer.sendPacket(SystemMessageId.Companion.getTARGET_IS_INCORRECT());
			
			// Send ActionFailed to the Player
			actingPlayer.sendPacket(ActionFailed.Companion.getSTATIC_PACKET());
			return;
		}
		
		super.doCast(skill);
	}
	
	@Override
	public boolean isOutOfControl()
	{
		return super.isOutOfControl() || isBetrayed();
	}
	
	@Override
	public boolean isInCombat()
	{
		return getOwner() != null ? getOwner().isInCombat() : false;
	}
	
	@Override
	public final boolean isAttackingNow()
	{
		return isInCombat();
	}
	
	@Override
	public Player getActingPlayer()
	{
		return getOwner();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "(" + getNpcId() + ") Owner: " + getOwner();
	}
	
	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if (getOwner() != null)
			getOwner().sendPacket(mov);
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (getOwner() != null)
			getOwner().sendPacket(id);
	}
	
	public int getWeapon()
	{
		return 0;
	}
	
	public int getArmor()
	{
		return 0;
	}
	
	public void updateAndBroadcastStatusAndInfos(int val)
	{
		sendPacket(new PetInfo(this, val));
		
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
		updateEffectIcons(true);
		
		updateAndBroadcastStatus(val);
	}
	
	public void sendPetInfosToOwner()
	{
		sendPacket(new PetInfo(this, 2));
		
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
		updateEffectIcons(true);
	}
	
	public void updateAndBroadcastStatus(int val)
	{
		sendPacket(new PetStatusUpdate(this));
		
		if (isVisible())
		{
			for (Player player : getKnownType(Player.class))
			{
				if (player == getOwner())
					continue;
				
				player.sendPacket(new SummonInfo(this, player, val));
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		// Need it only for "crests on summons" custom.
		if (Config.INSTANCE.getSHOW_SUMMON_CREST())
			sendPacket(new SummonInfo(this, getOwner(), 0));
		
		sendPacket(new RelationChanged(this, getOwner().getRelation(getOwner()), false));
		broadcastRelationsChanges();
	}
	
	@Override
	public void broadcastRelationsChanges()
	{
		for (Player player : getOwner().getKnownType(Player.class))
			player.sendPacket(new RelationChanged(this, getOwner().getRelation(player), isAutoAttackable(player)));
	}
	
	@Override
	public void sendInfo(Player activeChar)
	{
		// Check if the Player is the owner of the Pet
		if (activeChar == getOwner())
		{
			activeChar.sendPacket(new PetInfo(this, 0));
			
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			updateEffectIcons(true);
			
			if (this instanceof Pet)
				activeChar.sendPacket(new PetItemList((Pet) this));
		}
		else
			activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
			_shotsMask |= type.getMask();
		else
			_shotsMask &= ~type.getMask();
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		if (getOwner().getAutoSoulShot() == null || getOwner().getAutoSoulShot().isEmpty())
			return;
		
		for (int itemId : getOwner().getAutoSoulShot())
		{
			ItemInstance item = getOwner().getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				if (magic && item.getItem().getDefaultAction() == summon_spiritshot)
				{
					final IItemHandler handler = ItemHandler.INSTANCE.getHandler(item.getEtcItem());
					if (handler != null)
						handler.useItem(getOwner(), item, false);
				}
				
				if (physical && item.getItem().getDefaultAction() == summon_soulshot)
				{
					final IItemHandler handler = ItemHandler.INSTANCE.getHandler(item.getEtcItem());
					if (handler != null)
						handler.useItem(getOwner(), item, false);
				}
			}
			else
				getOwner().removeAutoSoulShot(itemId);
		}
	}
	
	@Override
	public int getSkillLevel(int skillId)
	{
		for (List<L2Skill> list : getTemplate().getSkills().values())
		{
			for (L2Skill skill : list)
				if (skill.getId() == skillId)
					return skill.getLevel();
		}
		return 0;
	}
	
	@Override
	public L2Skill getSkill(int skillId)
	{
		for (List<L2Skill> list : getTemplate().getSkills().values())
		{
			for (L2Skill skill : list)
				if (skill.getId() == skillId)
					return skill;
		}
		return null;
	}
}