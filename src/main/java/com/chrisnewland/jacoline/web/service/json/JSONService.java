/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.service.json;

import com.chrisnewland.jacoline.core.CommandLineSwitchParser;
import com.chrisnewland.jacoline.core.JaCoLineRequest;
import com.chrisnewland.jacoline.core.JaCoLineResponse;
import com.chrisnewland.jacoline.web.WebServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/json/") public class JSONService
{
	@POST @Path("inspect") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON) public JaCoLineResponse handleCommand(
			JaCoLineRequest request)
	{
		System.out.println("JSONService received request:" + request);

		JaCoLineResponse response;

		try
		{
			response = CommandLineSwitchParser.buildReport(request, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			response = JaCoLineResponse.createForRequest(request);

			response.setErrorMessage(e.getMessage());
		}

		return response;
	}

	@GET @Path("data/{file}") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON) public String fetchJSON(
			@PathParam("file") String file)
	{
		System.out.println("JSONService received file request:" + file);

		if (!file.contains(".") && !file.contains("/"))
		{
			try
			{
				java.nio.file.Path toFetch = WebServer.getSerialisedPath().resolve(Paths.get(file + ".json"));

				if (toFetch.toFile().exists())
				{
					System.out.println("JSONService fetching:" + toFetch);

					return new String(Files.readAllBytes(toFetch));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		System.out.println("JSONService bad request:" + file);

		return "bad request";
	}
}