/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

public class AnalysedSwitchResult
{
	private KeyValue keyValue;

	private SwitchStatus switchStatus;

	private String analysis;

	private String type;

	private String description;

	private String defaultValue;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public KeyValue getKeyValue()
	{
		return keyValue;
	}

	public SwitchStatus getSwitchStatus()
	{
		return switchStatus;
	}

	public String getAnalysis()
	{
		return analysis;
	}

	public void setKeyValue(KeyValue keyValue)
	{
		this.keyValue = keyValue;
	}

	public void setSwitchStatus(SwitchStatus status)
	{
		this.switchStatus = status;
	}

	public void setAnalysis(String analysis)
	{
		this.analysis = analysis;
	}
}