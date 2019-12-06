/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.KeyValue;

import java.util.List;

public interface ISwitchRule
{
	SwitchRuleResult apply(KeyValue keyValue, List<KeyValue> keyValueList);
}
