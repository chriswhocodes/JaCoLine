/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.service.json;

import com.chrisnewland.jacoline.core.CommandLineSwitchParser;
import com.chrisnewland.jacoline.core.JaCoLineRequest;
import com.chrisnewland.jacoline.core.JaCoLineResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

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
}
