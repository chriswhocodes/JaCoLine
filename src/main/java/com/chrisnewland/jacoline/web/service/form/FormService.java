/*
 * Copyright (c) 2019-2021 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.service.form;

import com.chrisnewland.jacoline.core.CommandLineSwitchParser;
import com.chrisnewland.jacoline.core.JaCoLineRequest;
import com.chrisnewland.jacoline.core.JaCoLineResponse;
import com.chrisnewland.jacoline.core.SwitchStatus;
import com.chrisnewland.jacoline.web.service.form.report.ReportBuilder;
import org.owasp.encoder.Encode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.List;

@Path("/") public class FormService
{
	private static final String DEFAULT_JDK = "OpenJDK8";

	private static boolean statsCacheIsValid = false;

	private static String generatedStatsHTML = null;

	public static void invalidateStatsCache()
	{
		statsCacheIsValid = false;
	}

	@GET @Path("inspect") @Produces(MediaType.TEXT_HTML) public String handleForm()
	{
		return FormServiceUtil.buildForm(DEFAULT_JDK, "linux", "x86", false)
							  .replace("%COMMAND%", "")
							  .replace("%RESULT%", "")
							  .replace("%STORED%", "");
	}

	@POST @Path("inspect") @Consumes(MediaType.APPLICATION_FORM_URLENCODED) @Produces(MediaType.TEXT_HTML) public String handleCommand(
			@FormParam("commandline") String command, @FormParam("jvm") String jvm, @FormParam("os") String os,
			@FormParam("arch") String arch, @FormParam("debug") List<String> debug)
	{
		boolean debugJVM = (debug != null) && !debug.isEmpty();

		try
		{
			boolean storeDTO = !command.contains("com.chrisnewland.someproject.SomeApplication");

			JaCoLineRequest request = new JaCoLineRequest();

			request.setCommand(command);
			request.setJvm(jvm);
			request.setOs(os);
			request.setArch(arch);
			request.setDebugJVM(debugJVM);

			JaCoLineResponse response = CommandLineSwitchParser.buildReport(request, storeDTO);

			if (storeDTO)
			{
				statsCacheIsValid = false;
			}

			String form = FormServiceUtil.buildForm(jvm, os, arch, debugJVM);

			String storedMessage = "";

			if (!storeDTO)
			{
				storedMessage = "Not updating statistics database when command line contains the example class 'com.chrisnewland.someproject.SomeApplication'";
			}

			form = form.replace("%STORED%", storedMessage)
					   .replace("%COMMAND%", Encode.forHtml(command).replace("-", "&#8209;"))
					   .replace("%RESULT%", FormServiceUtil.renderHTML(response));

			return form;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("retrieve/{requestId}") @Produces(MediaType.TEXT_HTML) public String retrieve(@PathParam("requestId") long requestId)
	{
		return FormServiceUtil.buildRetrievedRequest(requestId);
	}

	@GET @Path("about") @Produces(MediaType.TEXT_HTML) public String about()
	{
		try
		{
			return FormServiceUtil.showAbout();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("api") @Produces(MediaType.TEXT_HTML) public String api()
	{
		try
		{
			return FormServiceUtil.showAPI();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("privacy") @Produces(MediaType.TEXT_HTML) public String privacy()
	{
		try
		{
			return FormServiceUtil.showPrivacy();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	@GET @Path("stats") @Produces(MediaType.TEXT_HTML) public String stats()
	{
		long now = System.currentTimeMillis();

		if (generatedStatsHTML == null || !statsCacheIsValid)
		{
			try
			{
				String template = FormServiceUtil.loadStats();

				StringBuilder builder = new StringBuilder();

				builder.append(ReportBuilder.getLastRequests(10));
				builder.append("<div class=\"divclear\"></div>");

				builder.append(ReportBuilder.getReportJVMCounts());
				builder.append(ReportBuilder.getReportOperatingSystemCounts());
				builder.append(ReportBuilder.getReportArchitectureCounts());
				builder.append(ReportBuilder.getReportSwitchNameCounts());
				builder.append(ReportBuilder.getReportSwitchNameCountsWithStatus(SwitchStatus.OK, "No errors"));
				builder.append(ReportBuilder.getReportSwitchNameCountsWithStatus(SwitchStatus.WARNING, "With warnings"));
				builder.append(ReportBuilder.getReportSwitchNameCountsWithStatus(SwitchStatus.ERROR, "With errors"));

				builder.append("<div class=\"divclear\"></div>");
				builder.append(ReportBuilder.getTopErrors());

				builder.append("<div class=\"divclear\"></div>");
				builder.append(ReportBuilder.getTopWarnings());

				template = template.replace("%REPORTS%", builder.toString());

				generatedStatsHTML = template;
				statsCacheIsValid = true;

				return template;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return "An error occurred! Tell @chriswhocodes!";
			}
		}
		else
		{
			System.out.println("Returning cached stats page");
			return generatedStatsHTML;
		}
	}
}
