/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.KeyValue;
import com.chrisnewland.jacoline.core.SwitchStatus;

import java.util.List;

public class RuleDetectDuplicatesAfterSwitch extends AbstractSwitchRule
{
	private int currentSwitchIndex;

	public RuleDetectDuplicatesAfterSwitch(int currentSwitchIndex)
	{
		this.currentSwitchIndex = currentSwitchIndex;
	}

	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		SwitchRuleResult result = SwitchRuleResult.ok();

		if (currentSwitchIndex < keyValueList.size() - 1)
		{
			for (int laterIndex = currentSwitchIndex + 1; laterIndex < keyValueList.size(); laterIndex++)
			{
				KeyValue laterKeyValue = keyValueList.get(laterIndex);

				if (keyValue.getKeyWithPrefix().equals(laterKeyValue.getKeyWithPrefix()))
				{
					result = new SwitchRuleResult(SwitchStatus.WARNING,"Duplicate switch. This is overridden by " + laterKeyValue.toStringForHTML());
				}
			}
		}

		return result;
	}
}