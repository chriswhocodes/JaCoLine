/*
 * Copyright (c) 2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

import com.chrisnewland.jacoline.rule.RuleBooleanParameterHasPlusMinus;
import com.chrisnewland.jacoline.rule.RuleIsValidSize;
import com.chrisnewland.jacoline.rule.SwitchRuleResult;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestRuleIsValidBoolean
{
	@Test public void testBooleanParamWithPlus()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:+UseG1GC");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("UseG1GC", keyValue.getKey());
		assertEquals("true", keyValue.getValue());

		JSONObject jsonObject = new JSONObject(
				"{\"component\":\"runtime\",\"prefix\":\"- XX:\",\"defaultValue\":\"false\",\"definedIn\":\"src / share / vm / runtime / globals.hpp\",\"name\":\"UseG1GC\",\"description\":\"Use the Garbage - First garbage collector\",\"availability\":\"product\",\"type\":\"bool\"}");

		SwitchInfo switchInfo = SwitchInfo.deserialise(jsonObject);

		RuleBooleanParameterHasPlusMinus rule = new RuleBooleanParameterHasPlusMinus(switchInfo);

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test public void testBooleanParamWithMinus()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:-UseG1GC");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("UseG1GC", keyValue.getKey());
		assertEquals("false", keyValue.getValue());

		JSONObject jsonObject = new JSONObject(
				"{\"component\":\"runtime\",\"prefix\":\"- XX:\",\"defaultValue\":\"false\",\"definedIn\":\"src / share / vm / runtime / globals.hpp\",\"name\":\"UseG1GC\",\"description\":\"Use the Garbage - First garbage collector\",\"availability\":\"product\",\"type\":\"bool\"}");

		SwitchInfo switchInfo = SwitchInfo.deserialise(jsonObject);

		RuleBooleanParameterHasPlusMinus rule = new RuleBooleanParameterHasPlusMinus(switchInfo);

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.OK, result.getSwitchStatus());
	}

	@Test public void testBooleanParamWithMissingPlusMinus()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:UseG1GC");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("UseG1GC", keyValue.getKey());
		assertEquals("", keyValue.getValue());

		JSONObject jsonObject = new JSONObject(
				"{\"component\":\"runtime\",\"prefix\":\"- XX:\",\"defaultValue\":\"false\",\"definedIn\":\"src / share / vm / runtime / globals.hpp\",\"name\":\"UseG1GC\",\"description\":\"Use the Garbage - First garbage collector\",\"availability\":\"product\",\"type\":\"bool\"}");

		SwitchInfo switchInfo = SwitchInfo.deserialise(jsonObject);

		RuleBooleanParameterHasPlusMinus rule = new RuleBooleanParameterHasPlusMinus(switchInfo);

		SwitchRuleResult result = rule.apply(keyValue, new ArrayList<>());

		assertEquals(SwitchStatus.ERROR, result.getSwitchStatus());
	}
}