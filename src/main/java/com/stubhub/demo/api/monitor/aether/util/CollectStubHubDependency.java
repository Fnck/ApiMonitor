package com.stubhub.demo.api.monitor.aether.util;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

import com.stubhub.demo.api.monitor.entity.ArtifactInfo;

public class CollectStubHubDependency implements DependencyVisitor {
	private List<ArtifactInfo> dependencyList;
	
	public CollectStubHubDependency(){
		this.dependencyList = new ArrayList<ArtifactInfo>();
	}
	
	@Override
	public boolean visitEnter(DependencyNode node) {
		String artifactName = node.getDependency().getArtifact().getArtifactId();
		if((artifactName.indexOf("sh-ecomm-platform-datamodel")!=-1)||((artifactName.indexOf("com.stubhub")>-1)&&(artifactName.indexOf("crypto")==-1))){
			ArtifactInfo currentArtifact = new ArtifactInfo();
			currentArtifact.setArtifactName(artifactName);
			currentArtifact.setGroupName(node.getDependency().getArtifact().getGroupId());
			currentArtifact.setExtension(node.getDependency().getArtifact().getExtension());
			currentArtifact.setVersion(node.getDependency().getArtifact().getVersion());
			this.dependencyList.add(currentArtifact);
		}		
		return true;
	}

	@Override
	public boolean visitLeave(DependencyNode node) {
		// TODO Auto-generated method stub
		return true;
	}

	public List<ArtifactInfo> getDependencyList(){
		return this.dependencyList;
	}
}
