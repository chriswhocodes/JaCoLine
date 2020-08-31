/*
 * Copyright (c) 2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

import com.chrisnewland.jacoline.rule.RuleIsValidSize;
import com.chrisnewland.jacoline.rule.RuleXmsNotGreaterThanXmx;
import com.chrisnewland.jacoline.rule.SwitchRuleResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestRuleXmsNotGreaterThanXmx
{
	@Test
	public void testMxGreaterThanMs()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValueMs = parser.parseSwitch("-Xms256m");

		KeyValue keyValueMx = parser.parseSwitch("-Xmx512m");

		RuleXmsNotGreaterThanXmx rule = new RuleXmsNotGreaterThanXmx();

		List<KeyValue> allParamsList = new ArrayList<>();

		allParamsList.add(keyValueMs);
		allParamsList.add(keyValueMx);

		SwitchRuleResult result = rule.apply(keyValueMs, allParamsList);

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testMxSameAsMs()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValueMs = parser.parseSwitch("-Xms512m");

		KeyValue keyValueMx = parser.parseSwitch("-Xmx512m");

		RuleXmsNotGreaterThanXmx rule = new RuleXmsNotGreaterThanXmx();

		List<KeyValue> allParamsList = new ArrayList<>();

		allParamsList.add(keyValueMs);
		allParamsList.add(keyValueMx);

		SwitchRuleResult result = rule.apply(keyValueMs, allParamsList);

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testMxLessThanMs()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValueMs = parser.parseSwitch("-Xms512m");

		KeyValue keyValueMx = parser.parseSwitch("-Xmx256m");

		RuleXmsNotGreaterThanXmx rule = new RuleXmsNotGreaterThanXmx();

		List<KeyValue> allParamsList = new ArrayList<>();

		allParamsList.add(keyValueMs);
		allParamsList.add(keyValueMx);

		SwitchRuleResult result = rule.apply(keyValueMs, allParamsList);

		assertEquals(SwitchStatus.ERROR, result.getSwitchStatus());
	}

	@Test
	public void testMxIsEnvironmentVariable()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValueMs = parser.parseSwitch("-Xms512m");

		KeyValue keyValueMx = parser.parseSwitch("-Xmx${MAX_HEAP}");

		RuleXmsNotGreaterThanXmx rule = new RuleXmsNotGreaterThanXmx();

		List<KeyValue> allParamsList = new ArrayList<>();

		allParamsList.add(keyValueMs);
		allParamsList.add(keyValueMx);

		SwitchRuleResult result = rule.apply(keyValueMs, allParamsList);

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testMsIsEnvironmentVariable()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValueMs = parser.parseSwitch("-Xms${INITIAL_HEAP}");

		KeyValue keyValueMx = parser.parseSwitch("-Xmx512m");

		RuleXmsNotGreaterThanXmx rule = new RuleXmsNotGreaterThanXmx();

		List<KeyValue> allParamsList = new ArrayList<>();

		allParamsList.add(keyValueMs);
		allParamsList.add(keyValueMx);

		SwitchRuleResult result = rule.apply(keyValueMs, allParamsList);

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testMsMxAreBothEnvironmentVariables()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValueMs = parser.parseSwitch("-Xms${INITIAL_HEAP}");

		KeyValue keyValueMx = parser.parseSwitch("-Xmx${MAX_HEAP}");

		RuleXmsNotGreaterThanXmx rule = new RuleXmsNotGreaterThanXmx();

		List<KeyValue> allParamsList = new ArrayList<>();

		allParamsList.add(keyValueMs);
		allParamsList.add(keyValueMx);

		SwitchRuleResult result = rule.apply(keyValueMs, allParamsList);

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

}
