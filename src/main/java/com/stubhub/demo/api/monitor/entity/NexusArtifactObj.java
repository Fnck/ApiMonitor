package com.stubhub.demo.api.monitor.entity;

public class NexusArtifactObj {
	private boolean presentLocally;
	private String groupId;
	private String artifactId;
	private String version;
	private String extension;
	private boolean snapshot;
	private String snapshotBuildNumber;
	private String snapshotBuildTimeStamp;
	private String repositoryPath;
	public boolean isPresentLocally() {
		return presentLocally;
	}
	public void setPresentLocally(boolean presentLocally) {
		this.presentLocally = presentLocally;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
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
	public boolean isSnapshot() {
		return snapshot;
	}
	public void setSnapshot(boolean snapshot) {
		this.snapshot = snapshot;
	}
	public String getSnapshotBuildNumber() {
		return snapshotBuildNumber;
	}
	public void setSnapshotBuildNumber(String snapshotBuildNumber) {
		this.snapshotBuildNumber = snapshotBuildNumber;
	}
	public String getSnapshotBuildTimeStamp() {
		return snapshotBuildTimeStamp;
	}
	public void setSnapshotBuildTimeStamp(String snapshotBuildTimeStamp) {
		this.snapshotBuildTimeStamp = snapshotBuildTimeStamp;
	}
	public String getRepositoryPath() {
		return repositoryPath;
	}
	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}	
}
