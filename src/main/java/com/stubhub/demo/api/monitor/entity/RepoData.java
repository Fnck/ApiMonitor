package com.stubhub.demo.api.monitor.entity;

import java.util.List;

public class RepoData {
	private String groupId;
	private String artifactId;
	private String version;
	private String latestSnapshot;
	private String latestSnapshotRepositoryId;
	private String latestRelease;
	private String latestReleaseRepositoryId;
	private String highlightedFragment;
	private List<ArtifactHits> artifactHits;
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
	public String getLatestSnapshot() {
		return latestSnapshot;
	}
	public void setLatestSnapshot(String latestSnapshot) {
		this.latestSnapshot = latestSnapshot;
	}
	public String getLatestSnapshotRepositoryId() {
		return latestSnapshotRepositoryId;
	}
	public void setLatestSnapshotRepositoryId(String latestSnapshotRepositoryId) {
		this.latestSnapshotRepositoryId = latestSnapshotRepositoryId;
	}
	public String getLatestRelease() {
		return latestRelease;
	}
	public void setLatestRelease(String latestRelease) {
		this.latestRelease = latestRelease;
	}
	public String getLatestReleaseRepositoryId() {
		return latestReleaseRepositoryId;
	}
	public void setLatestReleaseRepositoryId(String latestReleaseRepositoryId) {
		this.latestReleaseRepositoryId = latestReleaseRepositoryId;
	}
	public String getHighlightedFragment() {
		return highlightedFragment;
	}
	public void setHighlightedFragment(String highlightedFragment) {
		this.highlightedFragment = highlightedFragment;
	}
	public List<ArtifactHits> getArtifactHits() {
		return artifactHits;
	}
	public void setArtifactHits(List<ArtifactHits> artifactHits) {
		this.artifactHits = artifactHits;
	}
	
	
}
