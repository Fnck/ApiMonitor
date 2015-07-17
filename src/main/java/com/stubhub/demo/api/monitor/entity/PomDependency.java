package com.stubhub.demo.api.monitor.entity;

import java.util.ArrayList;
import java.util.List;

public class PomDependency {
	private List<ArtifactInfo> intfAndImpl = new ArrayList<ArtifactInfo>();
	private List<ArtifactInfo> dependencies = new ArrayList<ArtifactInfo>();	
	
	public List<ArtifactInfo> getIntfAndImpl() {
		return intfAndImpl;
	}
	public void setIntfAndImpl(List<ArtifactInfo> intfAndImpl) {
		this.intfAndImpl = intfAndImpl;
	}
	public void setDependencies(List<ArtifactInfo> dependencies) {
		this.dependencies = dependencies;
	}
	public List<ArtifactInfo> getDependencies() {
		return dependencies;
	}
	public void addDependencies(ArtifactInfo dependency) {
		this.dependencies.add(dependency);
	}	
}
