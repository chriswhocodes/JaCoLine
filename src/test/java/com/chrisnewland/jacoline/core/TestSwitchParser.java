/*
 * Copyright (c) 2020 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestSwitchParser
{
	@Test public void testSeparatorIsColon()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:FlightRecorderOptions:stackdepth=256");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("FlightRecorderOptions", keyValue.getKey());
		assertEquals("stackdepth=256", keyValue.getValue());
	}

	@Test public void testSeparatorIsEquals()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:FreqInlineSize=400");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("FreqInlineSize", keyValue.getKey());
		assertEquals("400", keyValue.getValue());
	}

	@Test public void testParseBooleanTrue()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:+UnlockDiagnosticVMOptions");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("UnlockDiagnosticVMOptions", keyValue.getKey());
		assertEquals("true", keyValue.getValue());
	}

	@Test public void testParseBooleanFalse()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-XX:-UnlockDiagnosticVMOptions");

		assertEquals(SwitchInfo.PREFIX_XX, keyValue.getPrefix());
		assertEquals("UnlockDiagnosticVMOptions", keyValue.getKey());
		assertEquals("false", keyValue.getValue());
	}

	@Test public void testParseSize()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch("-Xmx512m");

		assertEquals(SwitchInfo.PREFIX_X, keyValue.getPrefix());
		assertEquals("mx", keyValue.getKey());
		assertEquals("512m", keyValue.getValue());
	}

	@Test public void testParseXlog()
	{
		CommandLineSwitchParser parser = new CommandLineSwitchParser();

		KeyValue keyValue = parser.parseSwitch(
				"-Xlog:os,safepoint*,gc,gc+refdebug,gc+ergo*=debug,gc+age*=debug,gc+phases*:file=/gclogs/%t-gc.log:time,uptime,tags:filecount=5,filesize=10M");

		assertEquals(SwitchInfo.PREFIX_X, keyValue.getPrefix());
		assertEquals("log", keyValue.getKey());
		assertEquals(
				"os,safepoint*,gc,gc+refdebug,gc+ergo*=debug,gc+age*=debug,gc+phases*:file=/gclogs/%t-gc.log:time,uptime,tags:filecount=5,filesize=10M",
				keyValue.getValue());
	}
}
