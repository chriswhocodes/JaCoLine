/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.rule;

import com.chrisnewland.jacoline.core.SwitchStatus;

public class SwitchRuleResult
{
	public static final String MESSAGE_OK = "OK";

	public SwitchStatus getSwitchStatus()
	{
		return switchStatus;
	}

	private SwitchStatus switchStatus;

	private String message;

	public String getMessage()
	{
		return message;
	}

	public SwitchRuleResult(SwitchStatus switchStatus, String message)
	{
		this.switchStatus = switchStatus;
		this.message = message;
	}

	public static SwitchRuleResult ok()
	{
		return new SwitchRuleResult(SwitchStatus.OK, MESSAGE_OK);
	}
}
