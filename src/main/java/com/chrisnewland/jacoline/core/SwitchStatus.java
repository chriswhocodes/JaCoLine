/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

public enum SwitchStatus
{
	OK, WARNING, ERROR;

	public String getCssClass()
	{
		return "status_" + this.toString().toLowerCase();
	}
}