/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.deserialiser;

import com.chrisnewland.jacoline.core.SwitchInfo;
import org.json.JSONArray;
import org.json.JSONObject;

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
		System.out.println("Deserialising " + pathToSerialisedSwitchInfo);

		JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(pathToSerialisedSwitchInfo), StandardCharsets.UTF_8));

		JSONArray jsonArray = jsonObject.getJSONArray("switches");

		List<SwitchInfo> result = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++)
		{
			SwitchInfo switchInfo = SwitchInfo.deserialise(jsonArray.getJSONObject(i));

			result.add(switchInfo);
		}

		return result;
	}
}
