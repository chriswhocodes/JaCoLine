/*
 * Copyright (c) 2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.KeyValue;
import com.chrisnewland.jacoline.core.SwitchInfo;
import com.chrisnewland.jacoline.core.SwitchStatus;

import java.util.List;

public class RuleBooleanParameterHasPlusMinus extends AbstractSwitchRule
{
	private SwitchInfo switchInfo;

	public RuleBooleanParameterHasPlusMinus(SwitchInfo switchInfo)
	{
		this.switchInfo = switchInfo;
	}

	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		SwitchRuleResult result;

		if (keyValue.getValue().isEmpty() && "bool".equals(switchInfo.getType()))
		{
			keyValue.setIsBooleanSwitch();

			result = new SwitchRuleResult(SwitchStatus.ERROR, "Missing +/- on switch on boolean switch.");
		}
		else
		{
			result = SwitchRuleResult.ok();
		}

		return result;
	}
}
