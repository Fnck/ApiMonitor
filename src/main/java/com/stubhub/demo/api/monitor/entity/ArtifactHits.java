package com.stubhub.demo.api.monitor.entity;

import java.util.List;

public class ArtifactHits {
	private String repositoryId;
	private List<ArtifactLinks> artifactLinks;
	public String getRepositoryId() {
		return repositoryId;
	}
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	public List<ArtifactLinks> getArtifactLinks() {
		return artifactLinks;
	}
	public void setArtifactLinks(List<ArtifactLinks> artifactLinks) {
		this.artifactLinks = artifactLinks;
	}
	
}
