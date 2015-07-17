package com.stubhub.demo.api.monitor.entity;

public class ArtifactInfo {
	private String artifactName;
	private String groupName;
	private String version;
	private String extension;
	private String source;
	private String role;
	private String pool;
	
	public String getPool() {
		return pool;
	}
	public void setPool(String pool) {
		this.pool = pool;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getArtifactName() {
		return artifactName;
	}
	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
