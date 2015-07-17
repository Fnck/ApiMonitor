package com.stubhub.demo.api.monitor.entity.dto;


public class CompareResult {
	private String objectName;
	
	private String warName;
	private String artifactName;
	
	private String latestWarVersion;	
	private String latestArtifactVersion;
	
	private String prodWarVersion;
	private String prodArtifactVersion;
	
	private int deleteNum;
	private int insertNum;
	
	private String diffHtml;
	private String status;
	private String generationException;
	
	private String compareDate;
	
	private CompareType type;
	
	private String role;
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public CompareType getType() {
		return type;
	}
	public void setType(CompareType type) {
		this.type = type;
	}
	public String getCompareDate() {
		return compareDate;
	}
	public void setCompareDate(String compareDate) {
		this.compareDate = compareDate;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getWarName() {
		return warName;
	}
	public void setWarName(String warName) {
		this.warName = warName;
	}
	public String getArtifactName() {
		return artifactName;
	}
	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}
	public String getLatestWarVersion() {
		return latestWarVersion;
	}
	public void setLatestWarVersion(String latestWarVersion) {
		this.latestWarVersion = latestWarVersion;
	}
	public String getLatestArtifactVersion() {
		return latestArtifactVersion;
	}
	public void setLatestArtifactVersion(String latestArtifactVersion) {
		this.latestArtifactVersion = latestArtifactVersion;
	}
	public String getProdWarVersion() {
		return prodWarVersion;
	}
	public void setProdWarVersion(String prodWarVersion) {
		this.prodWarVersion = prodWarVersion;
	}
	public String getProdArtifactVersion() {
		return prodArtifactVersion;
	}
	public void setProdArtifactVersion(String prodArtifactVersion) {
		this.prodArtifactVersion = prodArtifactVersion;
	}
	public int getDeleteNum() {
		return deleteNum;
	}
	public void setDeleteNum(int deleteNum) {
		this.deleteNum = deleteNum;
	}
	public int getInsertNum() {
		return insertNum;
	}
	public void setInsertNum(int insertNum) {
		this.insertNum = insertNum;
	}
	public String getDiffHtml() {
		return diffHtml;
	}
	public void setDiffHtml(String diffHtml) {
		this.diffHtml = diffHtml;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}	
}
