/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.KeyValue;
import com.chrisnewland.jacoline.core.SwitchStatus;

import java.util.List;

import static com.chrisnewland.jacoline.core.SwitchInfo.PREFIX_X;

public class RuleXmsNotGreaterThanXmx extends AbstractSwitchRule
{
	@Override public SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList)
	{
		String keyXms = PREFIX_X + "ms";

		String keyXmx = PREFIX_X + "mx";

		SwitchRuleResult result = SwitchRuleResult.ok();

		if (keyXms.equals(keyValue.getKeyWithPrefix()) || keyXmx.equals(keyValue.getKeyWithPrefix()))
		{
			KeyValue keyValueXms = getLastOccurrence(PREFIX_X + "ms", keyValueList);

			KeyValue keyValueXmx = getLastOccurrence(PREFIX_X + "mx", keyValueList);

			if (keyValueXms != null && keyValueXmx != null)
			{
				long ms = parseSize(keyValueXms.getValue());
				long mx = parseSize(keyValueXmx.getValue());

				System.out.println("ms:" + ms + " mx:" + mx);

				if (ms > mx)
				{
					result = new SwitchRuleResult(SwitchStatus.ERROR, "Xmx must be >= Xms");
				}
			}
		}

		return result;
	}
}
