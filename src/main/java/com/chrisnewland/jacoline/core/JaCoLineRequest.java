package com.chrisnewland.jacoline.core;

public class JaCoLineRequest
{
	private String command;
	private String jvm;
	private String os;
	private String arch;
	private boolean debugJVM;

	public String getCommand()
	{
		return command;
	}

	public void setCommand(String command)
	{
		if (command != null)
		{
			command = command.replace("\n", " ").replace((char) 8209, '-');

		}

		this.command = command;
	}

	public String getJvm()
	{
		return jvm;
	}

	public void setJvm(String jvm)
	{
		this.jvm = jvm;
	}

	public String getOs()
	{
		return os;
	}

	public void setOs(String os)
	{
		this.os = os;
	}

	public String getArch()
	{
		return arch;
	}

	public void setArch(String arch)
	{
		this.arch = arch;
	}

	public boolean isDebugJVM()
	{
		return debugJVM;
	}

	public void setDebugJVM(boolean debugJVM)
	{
		this.debugJVM = debugJVM;
	}

	@Override public String toString()
	{
		return "JaCoLineRequest{" + "command='" + command + '\'' + ", jvm='" + jvm + '\'' + ", os='" + os + '\'' + ", arch='" + arch
				+ '\'' + ", debugJVM=" + debugJVM + '}';
	}
}
