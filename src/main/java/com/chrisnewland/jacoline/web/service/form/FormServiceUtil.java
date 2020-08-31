/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.web.service.form;

import com.chrisnewland.jacoline.core.*;
import com.chrisnewland.jacoline.dto.RequestDTO;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FormServiceUtil
{
	private static Path resourcesPath;

	public static void initialise(Path resourcesPath)
	{
		FormServiceUtil.resourcesPath = resourcesPath;

		System.out.println("Initialised resources: " + FormServiceUtil.resourcesPath);
	}

	public static final String OPTION_ANY = "- Any -";

	private static String loadTemplate(String pageName, String title, String h1) throws IOException
	{
		String template = new String(Files.readAllBytes(Paths.get(resourcesPath.toString(), "template.html")),
				StandardCharsets.UTF_8);

		String page = new String(Files.readAllBytes(Paths.get(resourcesPath.toString(), pageName)), StandardCharsets.UTF_8);

		return template.replace("%BODY%", page).replace("%TITLE%", title).replace("%H1%", h1);
	}

	public static String loadCompareTemplate() throws IOException
	{
		return new String(Files.readAllBytes(Paths.get(resourcesPath.toString(), "compare.html")), StandardCharsets.UTF_8);
	}

	public static String loadForm() throws IOException
	{
		return loadTemplate("form.html", "Inspect your Java command line", "Java Command Line Inspector");
	}

	public static String loadError() throws IOException
	{
		return loadTemplate("error.html", "An error occurred", "Bad Input");
	}

	public static String loadStats() throws IOException
	{
		return loadTemplate("stats.html", "Java Command Line Statistics", "Java Command Line Statistics");
	}

	public static String loadRetrieve(long id) throws IOException
	{
		return loadTemplate("retrieve.html", "View Historical Command Line", "Viewing Historical Command Line #" + id);
	}

	public static String showAbout() throws IOException
	{
		return loadTemplate("about.html", "About Java Command Line Inspector", "About");
	}

	public static String showAPI() throws IOException
	{
		return loadTemplate("api.html", "JaCoLine JSON/REST API", "JSON/REST API");
	}

	public static String showPrivacy() throws IOException
	{
		return loadTemplate("privacy.html", "Privacy Policy", "Privacy Policy");
	}

	public static String buildComboJVM(String selectedJVM)
	{
		return buildCombo("jvm", CommandLineSwitchParser.getJDKList(), selectedJVM);
	}

	public static String buildComboOperatingSystem(String jdkName, String selectedOS)
	{
		return buildCombo("os", CommandLineSwitchParser.getOperatingSystemList(), selectedOS);
	}

	public static String buildComboArchitecture(String jdkName, String selectedArch)
	{
		return buildCombo("arch", CommandLineSwitchParser.getArchitectureList(), selectedArch);
	}

	public static String buildCombo(String name, List<String> options, String defaultValue)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<select id=\"").append(name).append("\" name=\"").append(name).append("\">");

		for (String option : options)
		{
			if (defaultValue != null && defaultValue.equalsIgnoreCase(option))
			{
				builder.append("<option selected=\"selected\">");
			}
			else
			{
				builder.append("<option>");
			}

			builder.append(option).append("</option>");
		}

		builder.append("</select>");

		return builder.toString();
	}

	public static String buildCheckBox(boolean debugSelected)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<input type=\"checkbox\" name=\"debug\" value=\"debug\"");

		if (debugSelected)
		{
			builder.append(" checked");
		}

		builder.append(">");

		return builder.toString();
	}

	public static String buildForm(String selectedJVM, String selectedOS, String selectedArch, boolean debugSelected)
	{
		try
		{
			String form = loadForm();

			return form.replace("%COMBO_OS%", buildComboOperatingSystem(selectedJVM, selectedOS))
					   .replace("%COMBO_ARCH%", buildComboArchitecture(selectedJVM, selectedArch))
					   .replace("%COMBO_JDK%", buildComboJVM(selectedJVM))
					   .replace("%CHECK_DEBUG%", buildCheckBox(debugSelected));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	public static String buildRetrievedRequest(long requestId)
	{
		try
		{
			String template = loadRetrieve(requestId);

			RequestDTO requestDTO = RequestDTO.loadById(requestId);

			if (requestDTO != null)
			{
				JaCoLineRequest request = new JaCoLineRequest();

				request.setCommand(requestDTO.getRequest());
				request.setJvm(requestDTO.getJvm());
				request.setOs(requestDTO.getOs());
				request.setArch(requestDTO.getArch());
				request.setDebugJVM(requestDTO.isDebugVm());

				JaCoLineResponse response = CommandLineSwitchParser.buildReport(request, false);

				String resultHTML = renderHTML(response);

				return template.replace("%REQUEST_DATE%", requestDTO.getRecordedAt().toString())
							   .replace("%REQUEST_OS%", Encode.forHtml(requestDTO.getOs()))
							   .replace("%REQUEST_ARCH%", Encode.forHtml(requestDTO.getArch()))
							   .replace("%REQUEST_JDK%", Encode.forHtml(requestDTO.getJvm()))
							   .replace("%REQUEST_COMMAND%", Encode.forHtml(requestDTO.getRequest()))
							   .replace("%REQUEST_DEBUG%", requestDTO.isDebugVm() ? "Y" : "N")
							   .replace("%RESULT%", resultHTML);
			}

			return "requestId not found";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "An error occurred! Tell @chriswhocodes!";
		}
	}

	public static String renderHTML(JaCoLineResponse response)
	{
		StringBuilder reportBuilder = new StringBuilder();

		if (!"OK".equals(response.getErrorMessage()))
		{
			reportBuilder.append(createTagWithClassAndContent("div", "error", response.getErrorMessage()));
		}

		StringBuilder summaryBuilder = new StringBuilder();

		summaryBuilder.append(createTagWithClassAndContent("div", "section_header", "Switches Identified"));
		summaryBuilder.append(createOpenTagWithClass("div", "summary_container"));

		reportBuilder.append(createTagWithClassAndContent("div", "section_header_nbm", "Switch Analysis"));

		int cssId = 0;

		List<AnalysedSwitchResult> analysedSwitches = response.getSwitchesIdentified();

		for (AnalysedSwitchResult result : analysedSwitches)
		{
			String identity = "id" + cssId++;

			summaryBuilder.append(getSummarySwitchLink(result, "", identity));

			reportBuilder.append(getHTMLForAnalysedSwitchResult(result, identity));
		}

		summaryBuilder.append(createCloseTag("div"));
		summaryBuilder.append(createTagWithClassAndContent("div", "divclear", ""));

		reportBuilder.insert(0, summaryBuilder.toString());

		reportBuilder.insert(0, createTagWithClassAndContent("div", "section", "Results"));

		return reportBuilder.toString();
	}

	private static String createOpenTagWithClass(String tagName, String classCSS)
	{
		StringBuilder builder = new StringBuilder();

		builder.append('<').append(tagName).append(" class=\"").append(classCSS).append("\">");

		return builder.toString();
	}

	private static String createCloseTag(String tagName)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("</").append(tagName).append('>');

		return builder.toString();
	}

	private static String createTagWithClassAndContent(String tagName, String classCSS, String content)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(createOpenTagWithClass(tagName, classCSS));
		builder.append(content);
		builder.append(createCloseTag(tagName));

		return builder.toString();
	}

	public static String getSummarySwitchLink(AnalysedSwitchResult analysedSwitchResult, String targetUrl, String id)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<div class=\"summary_switch ")
			   .append(analysedSwitchResult.getSwitchStatus().getCssClass())
			   .append("\"><a href=\"")
			   .append(targetUrl);

		if (!id.isEmpty())
		{
			builder.append('#').append(id);
		}

		builder.append("\" title=\"")
			   .append(analysedSwitchResult.getAnalysis())
			   .append("\"")
			   .append(">")
			   .append(analysedSwitchResult.getKeyValue().toStringForHTML())
			   .append("</a></div> ");

		return builder.toString();
	}

	public static String getHTMLForAnalysedSwitchResult(AnalysedSwitchResult result, String cssId)
	{
		String html;

		try
		{
			html = FormServiceUtil.loadCompareTemplate();
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Could not load template", ioe);
		}

		html = html.replace("%IDENTITY%", cssId);
		html = html.replace("%STATUS_CLASS%", result.getSwitchStatus().getCssClass());
		html = html.replace("%NAME%", result.getKeyValue().getKey());
		html = html.replace("%TYPE%", Encode.forHtml(convertTypeName(result.getType())));

		String description = result.getDescription().replace("<br>", "\n");

		description = Encode.forHtml(description);

		description = description.replace("\n", "<br>");

		html = html.replace("%DESCRIPTION%", description);
		html = html.replace("%DEFAULT%", result.getDefaultValue());
		html = html.replace("%VALUE%", Encode.forHtml(result.getKeyValue().getValue()));
		html = html.replace("%ANALYSIS%", Encode.forHtml(result.getAnalysis()));

		return html;
	}

	private static String convertTypeName(String input)
	{
		if (input == null)
		{
			return "";
		}

		switch (input)
		{
		case "intx":
		case "uintx":
			return "int";
		case "ccstr":
			return "string";
		case "bool":
			return "boolean";
		case "ccstrlist":
			return "list of strings";
		default:
			return input;
		}
	}
}