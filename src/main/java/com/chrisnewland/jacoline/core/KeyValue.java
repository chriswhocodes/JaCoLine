/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

import org.owasp.encoder.Encode;

import static com.chrisnewland.jacoline.core.SwitchInfo.PREFIX_X;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class KeyValue
{
	private String prefix;
	private String key;
	private String value;

	private boolean isBooleanSwitch = false;

	private static final Set<String> NO_SEPARATOR = new HashSet<>();

	static
	{
		NO_SEPARATOR.add("mx");
		NO_SEPARATOR.add("ms");
		NO_SEPARATOR.add("ss");
	}

	public KeyValue(String prefix, String key, String value)
	{
		this.prefix = prefix;
		this.key = key;
		this.value = value;

		System.out.println("Creating KeyValue (" + prefix + "," + key + "," + value + ")");
	}

	public void setIsBooleanSwitch()
	{
		this.isBooleanSwitch = true;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public String getKeyWithPrefix()
	{
		return prefix + key;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		KeyValue keyValue = (KeyValue) o;
		return Objects.equals(key, keyValue.key) && Objects.equals(value, keyValue.value);
	}

	@Override public int hashCode()
	{
		return Objects.hash(key, value);
	}

	public String toStringForDTO()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(prefix);

		if ("true".equals(value.toLowerCase()))
		{
			builder.append('+').append(key);
		}
		else if ("false".equals(value.toLowerCase()))
		{
			builder.append('-').append(key);
		}
		else if (PREFIX_X.equals(prefix))
		{
			if (value == null || NO_SEPARATOR.contains(key))
			{
				builder.append(key).append(value);
			}
			else
			{
				builder.append(key).append(':').append(value);
			}
		}
		else if (isBooleanSwitch)
		{
			builder.append(key);
		}
		else
		{
			builder.append(key).append('=').append(value);
		}

		return builder.toString();
	}

	public String toStringForHTML()
	{
		return Encode.forHtml(toStringForDTO());
	}

	@Override public String toString()
	{
		return toStringForHTML();
	}
}