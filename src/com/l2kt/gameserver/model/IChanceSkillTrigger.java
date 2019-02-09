package com.l2kt.gameserver.model;

/**
 * This interface provides method to handle triggered skills from other objects.<br>
 * For example, other skill, an effect, etc...
 * @author DrHouse
 */
public interface IChanceSkillTrigger
{
	public boolean triggersChanceSkill();
	
	public int getTriggeredChanceId();
	
	public int getTriggeredChanceLevel();
	
	public ChanceCondition getTriggeredChanceCondition();
}