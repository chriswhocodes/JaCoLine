/*
 * Copyright (c) 2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

import com.chrisnewland.jacoline.rule.RuleIsValidSize;
import com.chrisnewland.jacoline.rule.SwitchRuleResult;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestRuleIsValidSize
{
	@Test
	public void testLinuxVariableIsValidSizeX()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-Xmx${MAX_HEAP}");

		assertEquals(SwitchInfo.PREFIX_X, keyValue.getPrefix());
		assertEquals("mx", keyValue.getKey());
		assertEquals("${MAX_HEAP}", keyValue.getValue());

		RuleIsValidSize rule = new RuleIsValidSize();

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testLinuxVariableIsValidSizeXX()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:FreqInlineSize=${MAX_HOT_SIZE}");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("FreqInlineSize", keyValue.getKey());
		assertEquals("${MAX_HOT_SIZE}", keyValue.getValue());

		RuleIsValidSize rule = new RuleIsValidSize();

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testWindowsVariableIsValidSizeX()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-Xmx%MAX_HEAP%");

		assertEquals(SwitchInfo.PREFIX_X, keyValue.getPrefix());
		assertEquals("mx", keyValue.getKey());
		assertEquals("%MAX_HEAP%", keyValue.getValue());

		RuleIsValidSize rule = new RuleIsValidSize();

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test
	public void testWindowsVariableIsValidSizeXX()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:FreqInlineSize=%MAX_HOT_SIZE%");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("FreqInlineSize", keyValue.getKey());
		assertEquals("%MAX_HOT_SIZE%", keyValue.getValue());

		RuleIsValidSize rule = new RuleIsValidSize();

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}
}
