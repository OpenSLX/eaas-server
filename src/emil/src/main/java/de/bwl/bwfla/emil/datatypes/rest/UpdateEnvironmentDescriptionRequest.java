package de.bwl.bwfla.emil.datatypes.rest;

import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@JsonIgnoreProperties(ignoreUnknown = false)
public class UpdateEnvironmentDescriptionRequest extends EmilRequestType
{
	private String envId;
	private String title;
	private String author;
	private String description;
	private String helpText;
	private String time;
	private String userTag;
	private String os;
	private String nativeConfig;

	private String serverPort;
	private String serverIp;
	private String gwPrivateIp;
	private String gwPrivateMask;

	private boolean enablePrinting;
	private boolean enableRelativeMouse;
	private boolean shutdownByOs;
	private boolean useXpra;
	private boolean enableInternet;
	private boolean connectEnvs;
	private boolean serverMode;
	private boolean enableSocks;
	private boolean canProcessAdditionalFiles;

	public String getEnvId() {
		return envId;
	}
	public void setEnvId(String envId) {
		this.envId = envId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHelpText() {
		return helpText;
	}
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	public String getTime() { return time;}
	public void setTime(String time) {this.time = time; }

	public boolean isEnablePrinting() {
		return enablePrinting;
	}

	public boolean canProcessAdditionalFiles() {
		return canProcessAdditionalFiles;
	}

	public void setProcessAdditionalFiles(boolean canProcessAdditionalFiles) {
		this.canProcessAdditionalFiles = canProcessAdditionalFiles;
	}

	public void setEnablePrinting(boolean enablePrinting) {
		this.enablePrinting = enablePrinting;
	}

	public boolean isEnableRelativeMouse() {
		return enableRelativeMouse;
	}

	public void setEnableRelativeMouse(boolean enableRelativeMouse) {
		this.enableRelativeMouse = enableRelativeMouse;
	}

	public boolean isShutdownByOs() {
		return shutdownByOs;
	}

	public void setShutdownByOs(boolean shutdownByOs) {
		this.shutdownByOs = shutdownByOs;
	}

	public String getUserTag() {
		return userTag;
	}

	public void setUserTag(String userTag) {
		this.userTag = userTag;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

    public String getNativeConfig() {
		return nativeConfig;
    }

	public void setNativeConfig(String nativeConfig) {
		this.nativeConfig = nativeConfig;
	}

	public boolean isUseXpra() {
		return useXpra;
	}

	public void setUseXpra(boolean useXpra) {
		this.useXpra = useXpra;
	}

	public boolean isEnableInternet() {
		return enableInternet;
	}

	public void setEnableInternet(boolean enableInternet) {
		this.enableInternet = enableInternet;
	}

	public boolean isServerMode() {
		return serverMode;
	}

	public void setServerMode(boolean serverMode) {
		this.serverMode = serverMode;
	}

	public boolean isEnableSocks() {
		return enableSocks;
	}

	public void setEnableSocks(boolean enableSocks) {
		this.enableSocks = enableSocks;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getGwPrivateIp() {
		return gwPrivateIp;
	}

	public void setGwPrivateIp(String gwPrivateIp) {
		this.gwPrivateIp = gwPrivateIp;
	}


	public String getGwPrivateMask() {
		return gwPrivateMask;
	}

	public void setGwPrivateMask(String gwPrivateMask) {
		this.gwPrivateMask = gwPrivateMask;
	}

	public boolean canConnectEnvs() {
		return connectEnvs;
	}

	public void setConnectEnvs(boolean connectEnvs) {
		this.connectEnvs = connectEnvs;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}