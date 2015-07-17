package com.stubhub.demo.api.monitor.entity.dto;

import java.util.HashMap;
import java.util.List;

public class CommonCompareDocument {
	List<CommonCompareResult> intfResult;	
	List<CommonCompareResult> schemaResult;
	
	private String warName;
	
	private String latestWarVersion;	
	
	private String prodWarVersion;
	
	private String role;

	
	public List<CommonCompareResult> getIntfResult() {
		return intfResult;
	}

	public void setIntfResult(List<CommonCompareResult> intfResult) {
		this.intfResult = intfResult;
	}

	public List<CommonCompareResult> getSchemaResult() {
		return schemaResult;
	}

	public void setSchemaResult(List<CommonCompareResult> schemaResult) {
		this.schemaResult = schemaResult;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getWarName() {
		return warName;
	}

	public void setWarName(String warName) {
		this.warName = warName;
	}

	public String getLatestWarVersion() {
		return latestWarVersion;
	}

	public void setLatestWarVersion(String latestWarVersion) {
		this.latestWarVersion = latestWarVersion;
	}

	public String getProdWarVersion() {
		return prodWarVersion;
	}

	public void setProdWarVersion(String prodWarVersion) {
		this.prodWarVersion = prodWarVersion;
	}
	
	
}
