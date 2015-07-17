package com.stubhub.demo.api.monitor.util;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.option.client.SyncOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.p4java.server.ServerFactory;
import com.stubhub.demo.api.monitor.aether.util.AetherUtil;
import com.stubhub.demo.api.monitor.aether.util.CollectStubHubDependency;
import com.stubhub.demo.api.monitor.aether.util.ConsoleDependencyGraphDumper;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.PomDependency;

public class PerforceUtil {
	public IServer defaultServer = null;
	
	private IServer loginPerforce() throws Exception{
		String userName = "bqiao";
		String pwd = "stubhub123";
		String serverUriString = "p4java://10.249.72.107:1666";
		IServer server = ServerFactory.getServer(serverUriString , null);
		server.connect();
		server.setUserName(userName);
		server.login(pwd);

		IServerInfo info = server.getServerInfo();
		List<IClientSummary> clientList = server.getClients(
                userName, null, 0);
		IClient client = null;
		if(clientList != null){
			for(IClientSummary clientSummary : clientList){
				client = server.getClient(clientSummary);
				break;
			}
		}
		
		server.setCurrentClient(client);
		this.defaultServer = server;
		return server;
	}
	
	public List<String> getAavaiableDomainIntfPomFiles(String domain) throws Exception{
		List<String> pomPath = getAavaiablePomFiles("intf", domain);
		return pomPath;
	}
	
	public List<String> getAavaiableDomainImplPomFiles(String domain) throws Exception{
		List<String> pomPath = getAavaiablePomFiles("impl", domain);
		return pomPath;
	}
	
	public List<String> getAavaiableDomainWarPomFiles(String domain) throws Exception{
		List<String> pomPath = getAavaiablePomFiles("war", domain);
		return pomPath;
	}
	
	private List<String> getAavaiablePomFiles(String pattern, String domain) throws Exception{
		IServer server = this.defaultServer == null? loginPerforce() : this.defaultServer;
		List<IFileSpec> fileList = server.getDepotFiles(
				FileSpecBuilder.makeFileSpecList(new String[]{"//stubhub/domain/"+domain+"/main/..."+ pattern +"/pom.xml"}), false);
		IClient client = server.getCurrentClient();
		SyncOptions SyncOptions = new SyncOptions();
		SyncOptions.setForceUpdate(true);
		SyncOptions.setQuiet(true);
		client.sync(fileList, SyncOptions);
		
		List<String> pomPath = new ArrayList<String>();
		
		if (fileList != null) {
			for (IFileSpec fileSpec : fileList) {
				if (fileSpec != null) {
					if ((fileSpec.getOpStatus() == FileSpecOpStatus.VALID)&&(fileSpec.getAction() == FileAction.EDIT)) {
						pomPath.add(client.getRoot() + fileSpec.getDepotPathString().substring(1));
					}
				}
			}
		}
		
		return pomPath;
	}
	
}

