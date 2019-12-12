/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/JaCoLine/blob/master/LICENSE
 */
package com.chrisnewland.jacoline.core;

import com.chrisnewland.jacoline.deserialiser.Deserialiser;
import com.chrisnewland.jacoline.dto.RequestDTO;
import com.chrisnewland.jacoline.rule.*;
import com.chrisnewland.jacoline.spelling.DistanceCalculator;
import com.chrisnewland.jacoline.dto.VmSwitchDTO;
import com.chrisnewland.jacoline.web.service.form.FormService;

import static com.chrisnewland.jacoline.core.SwitchInfo.PREFIX_X;
import static com.chrisnewland.jacoline.core.SwitchInfo.PREFIX_XX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import static com.chrisnewland.jacoline.web.service.form.FormServiceUtil.OPTION_ANY;

public class CommandLineSwitchParser
{
	private static Map<String, List<SwitchInfo>> jdkSwitchMaps = new TreeMap<>();

	private static Map<String, List<String>> jdkArchitectureMap = new HashMap<>();

	private static Map<String, List<String>> jdkOperatingSystemMap = new HashMap<>();

	private static List<String> jdkList;

	private static List<String> osList;

	private static List<String> archList;

	private static DistanceCalculator distanceCalculator;

	private static final int FLAG_UNLOCK_DIAGNOSTIC = 1;

	private static final int FLAG_UNLOCK_EXPERIMENTAL = 2;

	private static final int FLAG_UNLOCK_DEVELOP = 4;

	private static final String AVAILABILITY_EXPERIMENTAL = "experimental";

	private static final String AVAILABILITY_DIAGNOSTIC = "diagnostic";

	private static final String AVAILABILITY_DEVELOP = "develop";

	public static void initialise(Path pathToSerialisationDir) throws IOException
	{
		File serialisationDir = pathToSerialisationDir.toFile();

		System.out.println("Loading SwitchInfoMaps from " + serialisationDir.getAbsolutePath());

		File[] switchInfoFiles = serialisationDir.listFiles();

		Set<String> switchDictionary = new TreeSet<>();

		for (File switchInfoFile : switchInfoFiles)
		{
			String jdkName = switchInfoFile.getName();

			if (jdkName.startsWith("JDK"))
			{
				List<SwitchInfo> switchInfoList = Deserialiser.deserialise(switchInfoFile.toPath());

				for (SwitchInfo switchInfo : switchInfoList)
				{
					switchDictionary.add(switchInfo.getName());
				}

				jdkSwitchMaps.put(jdkName, switchInfoList);

				buildComboMaps(jdkName, switchInfoList);

				System.out.println("Loaded SwitchInfoMap for " + jdkName);
			}
		}

		distanceCalculator = new DistanceCalculator(switchDictionary);

		jdkList = new ArrayList<>(jdkSwitchMaps.keySet());

		Collections.sort(jdkList, new Comparator<String>()
		{
			@Override public int compare(String o1, String o2)
			{
				return compareToJDKVersion(o1, o2);
			}
		});

		osList = getUniqueList(jdkOperatingSystemMap.values());

		archList = getUniqueList(jdkArchitectureMap.values());
	}

	private static void buildComboMaps(String jdkName, List<SwitchInfo> switchInfoList)
	{
		Set<String> architectureSet = new HashSet<>();
		architectureSet.add(OPTION_ANY);

		Set<String> operatingSystemSet = new HashSet<>();
		operatingSystemSet.add(OPTION_ANY);

		for (SwitchInfo switchInfo : switchInfoList)
		{
			String cpu = switchInfo.getCpu();

			if (!emptyOrNull(cpu))
			{
				architectureSet.add(cpu);
			}

			String os = switchInfo.getOs();

			if (!emptyOrNull(os))
			{
				if (os.length() == 3)
				{
					os = os.toUpperCase();
				}
				else
				{
					os = capitaliseFirst(os);
				}

				operatingSystemSet.add(os);
			}
		}

		jdkArchitectureMap.put(jdkName, new ArrayList<>(architectureSet));

		jdkOperatingSystemMap.put(jdkName, new ArrayList<>(operatingSystemSet));
	}

	private static String capitaliseFirst(String input)
	{
		return Character.toUpperCase(input.charAt(0)) + input.substring(1);
	}

	public static List<String> getJDKList()
	{
		return jdkList;
	}

	private synchronized static List<String> getUniqueList(Collection<List<String>> lists)
	{
		Set<String> uniqueValues = new HashSet<>();

		for (List<String> list : lists)
		{
			uniqueValues.addAll(list);
		}

		List<String> sorted = new ArrayList<>(uniqueValues);

		Collections.sort(sorted);

		return sorted;
	}

	public synchronized static List<String> getOperatingSystemList()
	{
		return osList;
	}

	public synchronized static List<String> getArchitectureList()
	{
		return archList;
	}

	public synchronized static List<String> getOperatingSystemList(String jdkName)
	{
		List<String> listOperatingSystem = jdkOperatingSystemMap.get(jdkName);

		Collections.sort(listOperatingSystem);

		return listOperatingSystem;
	}

	public synchronized static List<String> getArchitectureList(String jdkName)
	{
		List<String> listArch = jdkArchitectureMap.get(jdkName);

		Collections.sort(listArch);

		return listArch;
	}

	private static String getSanitisedRequestForDTO(List<KeyValue> switches)
	{
		StringBuilder builder = new StringBuilder();

		for (KeyValue keyValue : switches)
		{
			builder.append(keyValue.toStringForDTO()).append(' ');
		}

		return builder.toString();
	}

	public static JaCoLineResponse buildReport(JaCoLineRequest request)
	{
		return buildReport(request, true);
	}

	public static JaCoLineResponse buildReport(JaCoLineRequest request, boolean storeDTO)
	{
		String command = request.getCommand();
		String jvm = request.getJvm();
		String os = request.getOs();
		String arch = request.getArch();
		boolean debugJVM = request.isDebugJVM();

		JaCoLineResponse response = JaCoLineResponse.createForRequest(request);

		List<KeyValue> parsedSwitches = parse(command);

		if (parsedSwitches.isEmpty())
		{
			response.setErrorMessage("Error - I did not recognise any JVM switches in your command line.");
			return response;
		}

		RequestDTO requestDTO = new RequestDTO(getSanitisedRequestForDTO(parsedSwitches), jvm, os, arch, debugJVM);

		try
		{
			if (storeDTO)
			{
				requestDTO.insert();
				FormService.invalidateStatsCache();
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}

		long requestId = requestDTO.getId();

		int unlockFlags = getUnlockFlags(parsedSwitches, debugJVM);

		final int switchCount = parsedSwitches.size();

		List<AnalysedSwitchResult> analysedSwitchResults = new ArrayList<>(switchCount);

		int cssId = 0;

		for (int index = 0; index < switchCount; index++)
		{
			String identity = "id" + cssId++;

			AnalysedSwitchResult result = getSwitchAnalysis(identity, jvm, index, parsedSwitches, os, arch, unlockFlags);

			analysedSwitchResults.add(result);

			if (requestId != -1)
			{
				KeyValue keyValue = parsedSwitches.get(index);

				VmSwitchDTO vmSwitchDTO = new VmSwitchDTO(requestId, keyValue, result.getSwitchStatus(), result.getAnalysis());

				try
				{
					if (storeDTO)
					{
						vmSwitchDTO.insert();
					}
				}
				catch (SQLException sqle)
				{
					sqle.printStackTrace();
				}
			}
		}

		response.setSwitchesIdentified(analysedSwitchResults);

		return response;
	}

	public static List<KeyValue> parse(String commandLine)
	{
		List<String> parts = getParts(commandLine);

		System.out.println("Request has " + parts.size() + " parts");

		List<KeyValue> switches = new ArrayList<>();

		for (String part : parts)
		{
			if (isSwitch(part))
			{
				KeyValue keyValue = parseSwitch(part);

				if (keyValue != null)
				{
					switches.add(keyValue);

					System.out.println("Parsed: " + keyValue.toStringForDTO());
				}
			}
		}

		return switches;
	}

	private static String getSwitchPrefix(String input)
	{
		if (input.startsWith(PREFIX_XX))
		{
			return PREFIX_XX;
		}
		else if (input.startsWith(PREFIX_X))
		{
			return PREFIX_X;
		}

		return null;
	}

	private static KeyValue parseSwitch(String part)
	{
		String prefix = getSwitchPrefix(part);

		if (prefix == null)
		{
			return null;
		}

		part = part.substring(prefix.length());

		System.out.println("part after prefix:" + part);

		int equalsPos = part.indexOf('=');

		KeyValue keyValue = null;

		if (PREFIX_X.equals(prefix) && part.startsWith("log") && !part.startsWith("loggc"))
		{
			String key = "log";

			String value = "";

			if (part.length() > key.length())
			{
				value = part.substring(key.length());
			}

			keyValue = new KeyValue(prefix, key, value);
		}
		else if (equalsPos != -1)
		{
			String key = part.substring(0, equalsPos);

			String value = part.substring(equalsPos + 1);

			keyValue = new KeyValue(prefix, key, value);
		}
		else if (part.startsWith("+"))
		{
			String key = part.substring(1);
			String value = "true";

			keyValue = new KeyValue(prefix, key, value);
		}
		else if (part.startsWith("-"))
		{
			String key = part.substring(1);
			String value = "false";

			keyValue = new KeyValue(prefix, key, value);
		}
		else
		{
			// Xmx512m ?

			StringBuilder keyBuilder = new StringBuilder();
			StringBuilder valueBuilder = new StringBuilder();

			boolean inKey = true;

			for (int i = 0; i < part.length(); i++)
			{
				char c = part.charAt(i);

				if (!Character.isAlphabetic(c))
				{
					inKey = false;
				}

				if (inKey)
				{
					keyBuilder.append(c);
				}
				else
				{
					valueBuilder.append(c);
				}
			}

			String key = keyBuilder.toString();
			String value = valueBuilder.toString();

			keyValue = new KeyValue(prefix, key, value);
		}

		return keyValue;
	}

	private static List<String> getParts(String input)
	{
		List<String> parts = new ArrayList<>();

		boolean inQuotes = false;

		StringBuilder builder = new StringBuilder();

		int length = input.length();

		for (int i = 0; i < length; i++)
		{
			char c = input.charAt(i);

			if (c == '"')
			{
				inQuotes = !inQuotes;
			}
			else if (c == ' ')
			{
				if (!inQuotes)
				{
					String part = builder.toString();
					parts.add(part.trim());
					builder.setLength(0);
				}
			}

			builder.append(c);
		}

		if (builder.length() > 0)
		{
			String part = builder.toString();
			parts.add(part.trim());
			builder.setLength(0);
		}

		return parts;
	}

	private static boolean isSwitch(String part)
	{
		return part.startsWith(PREFIX_X);
	}

	private static String findEarliestJDKForSwitch(String switchName)
	{
		String result = null;

		for (String jdkName : getJDKList())
		{
			List<SwitchInfo> switchInfoList = jdkSwitchMaps.get(jdkName);

			if (switchFoundInList(switchName, switchInfoList))
			{
				result = jdkName;
				break;
			}
		}

		return result;
	}

	private static String findLatestJDKForSwitch(String switchName)
	{
		String result = null;

		for (String jdkName : getJDKList())
		{
			List<SwitchInfo> switchInfoList = jdkSwitchMaps.get(jdkName);

			if (switchFoundInList(switchName, switchInfoList))
			{
				result = jdkName;

				//System.out.println("found " + switchName + " in " + jdkName);
			}
		}

		return result;
	}

	private static boolean switchFoundInList(String switchName, List<SwitchInfo> switchInfoList)
	{
		boolean result = false;

		for (SwitchInfo switchInfo : switchInfoList)
		{
			if (switchName.equals(switchInfo.getName()))
			{
				result = true;
				break;
			}
		}

		return result;
	}

	public static AnalysedSwitchResult getSwitchAnalysis(String cssId, String currentJDK, int index, List<KeyValue> switches,
			String os, String arch, int unlockFlags)
	{
		KeyValue keyValue = switches.get(index);

		String switchName = keyValue.getKey();

		List<SwitchInfo> switchInfoList = getSwitchInfoListForName(switchName, currentJDK);

		SwitchStatus switchStatus = SwitchStatus.OK;

		String analysis = "OK";

		RulesEngine rulesEngine = new RulesEngine();

		rulesEngine.addRule(new RuleXmsNotGreaterThanXmx());

		boolean inError = false;

		//TODO refactor - make each check a separate ISwitchRule
		//================================================================
		if (switchInfoList.isEmpty())
		{
			System.out.println(switchName + " not found in " + currentJDK);

			inError = true;

			switchStatus = SwitchStatus.ERROR;

			String earliest = findEarliestJDKForSwitch(switchName);

			System.out.println("Found " + switchName + " in earliest " + earliest);

			if (earliest != null)
			{
				if (compareToJDKVersion(currentJDK, earliest) < 0)
				{
					analysis = "This switch is not available until " + earliest + ". Your analysis was for " + currentJDK;

					switchInfoList = getSwitchInfoListForName(switchName, earliest);
				}
				else
				{
					String latest = findLatestJDKForSwitch(switchName);

					if (latest != null && compareToJDKVersion(currentJDK, latest) > 0)
					{
						analysis = "This switch was removed after " + latest + ". Your analysis was for " + currentJDK;

						switchInfoList = getSwitchInfoListForName(switchName, latest);
					}
				}
			}
			else
			{
				analysis = "Switch not found in any JDK";

				String closestWord = distanceCalculator.findClosestWord(switchName);

				if (closestWord != null)
				{
					analysis += ". Did you mean '" + closestWord + "' ?";
				}
			}
		}

		//================================================================
		if (!inError)
		{
			SwitchInfo firstDefinedSwitchInfo = switchInfoList.get(0);

			String correctPrefix = firstDefinedSwitchInfo.getPrefix();

			if (correctPrefix != null && !correctPrefix.equals(keyValue.getPrefix()))
			{
				inError = true;
				switchStatus = SwitchStatus.ERROR;
				analysis = "Wrong prefix used " + keyValue.getPrefix() + " it should be " + correctPrefix;
			}
			else
			{
				String availability = getAvailability(switchInfoList);

				//TODO availability rule

				if (availability != null)
				{
					switch (availability)
					{
					case AVAILABILITY_DIAGNOSTIC:
						if ((unlockFlags & FLAG_UNLOCK_DIAGNOSTIC) == 0)
						{
							switchStatus = SwitchStatus.ERROR;
							inError = true;
							analysis = "Requires -XX:+UnlockDiagnosticVMOptions";
						}
						break;
					case AVAILABILITY_EXPERIMENTAL:
						if ((unlockFlags & FLAG_UNLOCK_EXPERIMENTAL) == 0)
						{
							switchStatus = SwitchStatus.ERROR;
							inError = true;
							analysis = "Requires -XX:+UnlockExperimentalVMOptions";
						}
						break;
					case AVAILABILITY_DEVELOP:
						if ((unlockFlags & FLAG_UNLOCK_DEVELOP) == 0)
						{
							switchStatus = SwitchStatus.ERROR;
							inError = true;
							analysis = "Requires a debug JVM";
						}
						break;
					}
				}
			}
		}

		//================================================================
		if (!inError)
		{
			String deprecation = getDeprecation(switchInfoList, currentJDK);

			if (!emptyOrNull(deprecation))
			{
				switchStatus = SwitchStatus.WARNING;
				inError = true;

				analysis = "This switch will be removed in the future. It will be " + deprecation.replace("<br>", ", ") + ".";
			}
		}

		//================================================================
		if (!inError)
		{
			rulesEngine.addRule(new RuleDetectDuplicatesAfterSwitch(index));
		}
		//================================================================

		String empty = "";

		String type = empty;
		String defaultValue = empty;
		String description = empty;

		String myValue = keyValue.getValue();

		for (SwitchInfo switchInfo : switchInfoList)
		{
			// take the first type definition found
			if (type.isEmpty())
			{
				type = switchInfo.getType();
			}

			if (description.isEmpty() && switchInfo.getDescription() != null && !switchInfo.getDescription().isEmpty())
			{
				description = switchInfo.getDescription();
			}
		}

		description = description.replace("<pre>", empty).replace("</pre>", empty);

		//================================================================

		System.out.println("type:" + type + " value:" + myValue);

		if ("<size>".equals(type))
		{
			rulesEngine.addRule(new RuleIsValidSize());
		}

		//================================================================

		List<SwitchInfo> filtered = filterByOperatingSystemAndArchitecture(switchInfoList, os, arch);

		//System.out.println("filtered size: " + filtered.size() + " for " + keyValue.getKey());

		if (!filtered.isEmpty())
		{
			if (filtered.size() == 1)
			{
				SwitchInfo switchInfo = filtered.get(0);

				defaultValue = switchInfo.getDefaultValue();

				String range = switchInfo.getRange();

				if (!emptyOrNull(range))
				{
					defaultValue += " in " + range;

					rulesEngine.addRule(new RuleParameterInRange(range));
				}
			}
			else
			{
				defaultValue = buildDefaultsTable(filtered);
			}
		}

		if (!inError)
		{
			System.out.println("Applying RulesEngine");

			List<SwitchRuleResult> ruleResults = rulesEngine.applyRules(keyValue, switches);

			if (!ruleResults.isEmpty())
			{
				analysis = empty;

				switchStatus = SwitchStatus.OK;

				for (SwitchRuleResult ruleResult : ruleResults)
				{
					analysis += ruleResult.getMessage();

					SwitchStatus resultStatus = ruleResult.getSwitchStatus();

					if (resultStatus.compareTo(switchStatus) > 0)
					{
						System.out.println("Updated SwitchStatus for " + keyValue.toString() + " from " + switchStatus + " to "
								+ resultStatus);

						switchStatus = resultStatus;
					}
				}
			}
		}

		return buildResult(keyValue, cssId, switchStatus, switchName, type, description, defaultValue, myValue, analysis);
	}

	private static AnalysedSwitchResult buildResult(KeyValue keyValue, String cssId, SwitchStatus switchStatus, String switchName,
			String type, String description, String defaultValue, String myValue, String analysis)
	{
		AnalysedSwitchResult result = new AnalysedSwitchResult();

		result.setKeyValue(keyValue);
		result.setDefaultValue(defaultValue);
		result.setDescription(description);
		result.setAnalysis(analysis);
		result.setType(type);
		result.setSwitchStatus(switchStatus);

		return result;
	}

	private static int compareToJDKVersion(String first, String second)
	{
		first = first.replace("JDK", "");
		second = second.replace("JDK", "");

		int result = 0;

		try
		{
			result = Integer.compare(Integer.parseInt(first), Integer.parseInt(second));
		}
		catch (NumberFormatException nfe)
		{
			nfe.printStackTrace();
		}

		return result;
	}

	private static List<SwitchInfo> filterByOperatingSystemAndArchitecture(List<SwitchInfo> switchInfoList, String os, String arch)
	{
		List<SwitchInfo> keep = new ArrayList<>();

		boolean checkOS = !emptyOrAny(os);

		boolean checkArch = !emptyOrAny(arch);

		for (SwitchInfo switchInfo : switchInfoList)
		{
			String switchOS = switchInfo.getOs();

			String switchCPU = switchInfo.getCpu();

			String switchDefault = switchInfo.getDefaultValue();

			if (!emptyOrNull(switchDefault))
			{
				if (!checkOS || os.equalsIgnoreCase(switchOS) || emptyOrNull(switchOS))
				{
					if (!checkArch || arch.equalsIgnoreCase(switchCPU) || emptyOrNull(switchCPU))
					{
						keep.add(switchInfo);
					}
				}
			}
		}

		return keep;
	}

	private static String buildDefaultsTable(List<SwitchInfo> switchInfoList)
	{
		boolean showOS = false;
		boolean showArch = false;
		boolean showComponent = false;

		for (SwitchInfo switchInfo : switchInfoList)
		{
			if (!emptyOrNull(switchInfo.getOs()))
			{
				showOS = true;

				if (!emptyOrNull(switchInfo.getComponent()))
				{
					showComponent = true;
				}
			}

			if (!emptyOrNull(switchInfo.getCpu()))
			{
				showArch = true;

				if (!emptyOrNull(switchInfo.getComponent()))
				{
					showComponent = true;
				}
			}
		}

		StringBuilder result = new StringBuilder();

		result.append("<table class=\"defaults\">");
		result.append("<tr>");

		if (showComponent)
		{
			result.append("<th>Component</th>");
		}
		if (showOS)
		{
			result.append("<th>OS</th>");
		}
		if (showArch)
		{
			result.append("<th>CPU</th>");
		}

		result.append("<th>Default</th>");

		result.append("</tr>");

		for (SwitchInfo switchInfo : switchInfoList)
		{
			String switchComponent = switchInfo.getComponent();
			String switchOS = switchInfo.getOs();
			String switchCPU = switchInfo.getCpu();

			//System.out.println(String.format("'%s' '%s' '%s'", switchComponent, switchOS, switchCPU));

			if (emptyOrNull(switchOS) && emptyOrNull(switchCPU))
			{
				continue;
			}

			result.append("<tr>");

			if (showComponent)
			{
				result.append("<td>").append(switchComponent).append("</td>");
			}

			if (showOS)
			{
				result.append("<td>").append(switchOS).append("</td>");
			}

			if (showArch)
			{
				result.append("<td>").append(switchCPU).append("</td>");
			}

			String switchDefault = switchInfo.getDefaultValue();
			result.append("<td>").append(switchDefault).append("</td>");

			result.append("</tr>");
		}

		result.append("</table>");

		return result.toString();
	}

	private static boolean emptyOrAny(String input)
	{
		return input == null || input.isEmpty() || OPTION_ANY.equalsIgnoreCase(input);
	}

	private static boolean emptyOrNull(String... strings)
	{
		boolean empty = true;

		for (String str : strings)
		{
			if (str != null && !str.isEmpty())
			{
				empty = false;
				break;
			}
		}

		return empty;
	}

	public static int getUnlockFlags(List<KeyValue> keyValueList, boolean debugVM)
	{
		int flags = 0;

		for (KeyValue keyValue : keyValueList)
		{
			switch (keyValue.getKey())
			{
			case "UnlockDiagnosticVMOptions":
				flags |= FLAG_UNLOCK_DIAGNOSTIC;
				break;
			case "UnlockExperimentalVMOptions":
				flags |= FLAG_UNLOCK_EXPERIMENTAL;
				break;
			}
		}

		if (debugVM)
		{
			flags |= FLAG_UNLOCK_DEVELOP;
		}

		return flags;
	}

	private static List<SwitchInfo> getSwitchInfoListForName(String switchName, String jdkName)
	{
		List<SwitchInfo> switchInfoList = jdkSwitchMaps.get(jdkName);

		List<SwitchInfo> result = new ArrayList<>();

		//System.out.println("Looking for " + switchName);

		if (switchInfoList != null)
		{
			for (SwitchInfo switchInfo : switchInfoList)
			{
				if (switchName.equals(switchInfo.getName()))
				{
					result.add(switchInfo);
				}
			}
		}

		return result;
	}

	private static String getAvailability(List<SwitchInfo> switchInfoList)
	{
		String availability = null;

		for (SwitchInfo switchInfo : switchInfoList)
		{
			availability = switchInfo.getAvailability();

			if (!emptyOrNull(availability))
			{
				break;
			}
		}

		return availability;
	}

	private static String getDeprecation(List<SwitchInfo> switchInfoList, String currentJDK)
	{
		String deprecation = null;

		for (SwitchInfo switchInfo : switchInfoList)
		{
			deprecation = switchInfo.getDeprecation();

			if (!emptyOrNull(deprecation))
			{
				break;
			}
		}

		if (deprecation != null)
		{
			deprecation = deprecation.replace("<span style=\"white-space:nowrap\">", "").replace("</span>", "");
		}

		return deprecation;
	}
}
