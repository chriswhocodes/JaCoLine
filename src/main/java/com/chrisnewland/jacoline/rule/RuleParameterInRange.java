/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.KeyValue;
import com.chrisnewland.jacoline.core.SwitchStatus;

import java.math.BigDecimal;
import java.util.List;

public class RuleParameterInRange extends AbstractSwitchRule
{
	private String range;

	public RuleParameterInRange(String range)
	{
		this.range = range;
	}

	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		SwitchRuleResult result;

		String value = keyValue.getValue();

		System.out.println("Checking parameter " + value + " in " + range + " for parameter " + keyValue.getKeyWithPrefix());

		if (inRange(value))
		{
			result = SwitchRuleResult.ok();
		}
		else
		{
			result = new SwitchRuleResult(SwitchStatus.ERROR, "Value " + value + " outside allowed " + range + ".");
		}

		return result;
	}

	private boolean inRange(String value)
	{
		String trimmedRange = range.replace("(", "").replace(")", "").replace("range", "").replace(" ", "");

		String[] rangeParts = trimmedRange.split(",");

		boolean result = true;

		try
		{
			BigDecimal min = new BigDecimal(rangeParts[0]);
			BigDecimal max = new BigDecimal(rangeParts[1]);

			BigDecimal val = new BigDecimal(value);

			if (val.compareTo(min) < 0 || val.compareTo(max) > 0)
			{
				result = false;
			}
		}
		catch (Exception e)
		{
			System.out.println("Could not parse range check");
		}

		return result;
	}
}