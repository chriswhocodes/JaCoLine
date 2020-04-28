package com.chrisnewland.jacoline.core;

import java.util.Collections;
import java.util.List;

public class JaCoLineResponse
{
	private String errorMessage = "OK";

	private List<AnalysedSwitchResult> switchesIdentified = Collections.emptyList();

	private JaCoLineRequest request;

	public JaCoLineRequest getRequest()
	{
		return request;
	}

	public void setRequest(JaCoLineRequest request)
	{
		this.request = request;
	}

	public List<AnalysedSwitchResult> getSwitchesIdentified()
	{
		return switchesIdentified;
	}

	public void setSwitchesIdentified(List<AnalysedSwitchResult> switchesIdentified)
	{
		this.switchesIdentified = switchesIdentified;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public static JaCoLineResponse createForRequest(JaCoLineRequest request)
	{
		JaCoLineResponse response = new JaCoLineResponse();

		response.setRequest(request);

		return response;
	}
}
