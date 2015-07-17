package com.stubhub.demo.api.monitor.entity;

import java.net.URL;

public class JarInfo {
	private String path;
	private URL jarUrl;
	private String version;
	private String artifactName;
	
	
	
	public String getArtifactName() {
		return artifactName;
	}
	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public URL getJarUrl() {
		return jarUrl;
	}
	public void setJarUrl(URL jarUrl) {
		this.jarUrl = jarUrl;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
