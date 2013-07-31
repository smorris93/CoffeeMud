package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdPhaser extends StdElecWeapon
{
	public String ID(){	return "StdPhaser";}

	protected int state=0;
	
	public StdPhaser()
	{
		super();
		setName("a phaser");
		basePhyStats.setWeight(5);
		setDisplayText("a phaser");
		setDescription("There are three activation settings: stun, kill, and disintegrate.");
		baseGoldValue=500;
		basePhyStats().setLevel(1);
		basePhyStats().setDamage(20);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(false);
		super.setRawLogicalAnd(false);
		super.setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
	}
	
	public void setReadableText(String text)
	{
		miscText=text;
		state=CMath.s_int(text);
	}

	protected int getState(String s)
	{
		if("STUN".startsWith(s))
			return 0;
		else
		if("KILL".startsWith(s))
			return 1;
		else
		if("DISINTEGRATE".startsWith(s))
			return 2;
		else
			return -1;
	}
	
	protected String getStateName()
	{
		String stateName;
		switch(state)
		{
		default:
		case 0: stateName="stun"; break;
		case 1: stateName="kill"; break;
		case 2: stateName="disintegrate"; break;
		}
		return stateName;
	}
	
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		switch(state)
		{
		case 0: phyStats().setDamage(1); break;
		case 1: break;
		case 2: phyStats().setDamage(1+(phyStats().damage()*2)); break;
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					if(msg.targetMessage().length()>0)
					{
						List<String> V=CMParms.parse(msg.targetMessage());
						if(V.size()>0)
						{
							String s=V.get(0).toUpperCase().trim();
							int newState=this.getState(s);
							if(newState<0)
							{
								msg.source().tell("'"+s+"' is an unknown setting on "+name(msg.source()));
								return false;
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
				{
					msg.source().tell(name()+" is currently "+(activated()?("set to "+getStateName()):"deactivated")
							+" and is at "+Math.round(powerRemaining()/powerCapacity()*100)+"% power.");
				}
				break;
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					int newState=state;
					if(msg.targetMessage().length()>0)
					{
						List<String> V=CMParms.parse(msg.targetMessage());
						if(V.size()>0)
						{
							String s=V.get(0).toUpperCase().trim();
							newState=this.getState(s);
							if(newState<0) newState=state;
						}
					}
					state=newState;
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> set(s) <T-NAME> on "+this.getStateName()+".");
				}
				this.activate(true);
				break;
			}
		}
		else
		if((owner() instanceof MOB) && msg.amISource((MOB)owner()) && (!amWearingAt(Item.IN_INVENTORY)))
		{
			MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE:
				if(msg.tool() ==this)
				{
					switch(state)
					{
					default:
					case 0: {
						if(msg.value()>0)
						{
							Ability A=CMClass.getAbility("CombatSleep");
							if((A!=null)&&(msg.target() instanceof Physical))
								A.invoke(mob, (Physical)msg.target(), true, 0);
							else
							if((A==null)&&(msg.target() instanceof MOB))
							{
								msg.source().location().show((MOB)msg.target(), null, CMMsg.MSG_OK_VISUAL, "<S-NAME> go(es) unconscious!");
								((MOB)msg.target()).basePhyStats().setDisposition(((MOB)msg.target()).basePhyStats().disposition()|PhyStats.IS_SLEEPING);
								((MOB)msg.target()).phyStats().setDisposition(((MOB)msg.target()).phyStats().disposition()|PhyStats.IS_SLEEPING);
							}
						}
						break;
					}
					case 1: {
						// this is normal...
						break;
					}
					case 2: {
						Environmental targ=msg.target();
						if((msg.value()>(basePhyStats().damage()*1.5))
						&&((!(targ instanceof MOB))||(msg.value()>=((MOB)targ).curState().getHitPoints())))
						{
							Room R=CMLib.map().roomLocation(targ);
							if(R!=null)
							{
								if(targ instanceof MOB)
								{
									if(((MOB)targ).curState().getHitPoints()>0)
										CMLib.combat().postDamage(mob,(MOB)targ,this,(((MOB)targ).curState().getHitPoints()*100),CMMsg.MASK_ALWAYS|CMMsg.TYP_ELECTRIC,Weapon.TYPE_BURSTING,"^S<S-NAME> <DAMAGES> <T-NAME> with <O-NAME> and <T-HE-SHE> disintegrates!^?");
									else
									if(((MOB)targ).amDead())
										((MOB)targ).location().show(mob,targ,CMMsg.MSG_OK_ACTION,"<T-NAME> disintegrate(s)!");
									if(((MOB)targ).amDead())
									{
										for(int i=0;i<R.numItems();i++)
										{
											Item I=R.getItem(i);
											if((I!=null)&&(I instanceof DeadBody)&&(I.container()==null)&&(((DeadBody)I).savedMOB()==targ)
											&&(!((DeadBody)I).playerCorpse()))
											{
												I.destroy();
												break;
											}
										}
									}
								}
								else
								if((targ instanceof Item)
								&&((!(targ instanceof DeadBody)||(!((DeadBody)targ).playerCorpse()))))
								{
									((MOB)targ).location().show(mob,targ,CMMsg.MSG_OK_ACTION,"^S<S-NAME> fire(s) <O-NAME> at <T-NAME> and it disintegrates!^?");
									((Item)targ).destroy();
								}
								R.recoverRoomStats();
							}
						}
						break;
					}
					}
				}
				break;
			default:
				break;
			}
		}
	}
}
