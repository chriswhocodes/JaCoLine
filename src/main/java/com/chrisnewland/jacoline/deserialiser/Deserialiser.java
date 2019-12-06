/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.deserialiser;

import com.chrisnewland.jacoline.core.SwitchInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Deserialiser
{
	public static List<SwitchInfo> deserialise(Path pathToSerialisedSwitchInfo) throws IOException
	{
		List<String> lines = Files.readAllLines(pathToSerialisedSwitchInfo, StandardCharsets.UTF_8);

		List<SwitchInfo> result = new ArrayList<>(lines.size());

		for (String line : lines)
		{
			SwitchInfo switchInfo = SwitchInfo.deserialise(line);

			result.add(switchInfo);
		}

		return result;
	}
}
