/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.KeyValue;
import com.chrisnewland.jacoline.core.SwitchStatus;

import java.util.ArrayList;
import java.util.List;

public class RulesEngine
{
	private List<ISwitchRule> rulesList;

	public RulesEngine()
	{
		rulesList = new ArrayList<>();
	}

	public void addRule(ISwitchRule rule)
	{
		rulesList.add(rule);
	}

	public List<SwitchRuleResult> applyRules(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		List<SwitchRuleResult> results = new ArrayList<>();

		for (ISwitchRule rule : rulesList)
		{
			System.out.println("Applying " + rule.getClass().getSimpleName());

			SwitchRuleResult ruleResult = rule.apply(keyValue, keyValueList);

			if (ruleResult.getSwitchStatus() != SwitchStatus.OK)
			{
				results.add(ruleResult);
			}
		}

		return results;
	}
}