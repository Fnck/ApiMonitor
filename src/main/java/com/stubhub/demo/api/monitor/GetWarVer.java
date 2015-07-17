package com.stubhub.demo.api.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.gson.Gson;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.stubhub.demo.api.monitor.aether.util.AetherUtil;
import com.stubhub.demo.api.monitor.aether.util.CollectStubHubDependency;
import com.stubhub.demo.api.monitor.aether.util.ConsoleDependencyGraphDumper;
import com.stubhub.demo.api.monitor.aether.util.ConsoleRepositoryListener;
import com.stubhub.demo.api.monitor.aether.util.ConsoleTransferListener;
import com.stubhub.demo.api.monitor.aether.util.ManualWagonProvider;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.PomDependency;
import com.stubhub.demo.api.monitor.entity.dto.CompareResult;
import com.stubhub.demo.api.monitor.util.NexusUtil;
import com.stubhub.demo.api.monitor.util.PerforceUtil;


public class GetWarVer {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		GetWarVer g = new GetWarVer();
		List<ArtifactInfo> alist = g.printProductionWarAndVersion(false);
		MongoClient mclient = new MongoClient("mongodb-34895.phx-os1.stratus.dev.ebay.com", 27017);		
		DB db = mclient.getDB("dev_db");
		Gson jsonUtil = new Gson();
		
		List<DBObject> insertList = new ArrayList<DBObject>();
		for(ArtifactInfo artifact : alist){			
			DBObject dbo = (DBObject) JSON.parse(jsonUtil.toJson(artifact));
			insertList.add(dbo);
		}
		DBObject opt = BasicDBObjectBuilder.start().add("capped", false).get();;
		if(db.collectionExists("artifact")){
			db.getCollection("artifact").drop();
			db.createCollection("artifact", opt);
			db.getCollection("artifact").insert(insertList);
		}
	}
	
	public ArtifactInfo getWarString(String input){
		ArtifactInfo artifact = new ArtifactInfo();
		artifact.setExtension(input.substring(input.length()-3,input.length()));
		String tmp = input.substring(0, input.length()-4);
		int p = tmp.lastIndexOf("-");
		String packageName = tmp.substring(0, p);
		String version = tmp.substring(p+1, tmp.length());
		artifact.setArtifactName(packageName);
		artifact.setVersion(version);
		return artifact;		
	}
	
	public List<ArtifactInfo> printProductionWarAndVersion(boolean includLatestVersion) throws Exception{
		List<ArtifactInfo> artifactInProd = new ArrayList<ArtifactInfo>();
		
		File txt = new File("C:\\DownloadedPDF\\Production_Shape_Version.txt");
		BufferedReader br = new BufferedReader(new FileReader(txt));
		NexusUtil nu = new NexusUtil();
		String line;
		while ((line = br.readLine()) != null) {
		   String[] str = line.split(",");
		   String war = str[1];
		   ArtifactInfo warAndVer = getWarString(war);
		   warAndVer.setRole(str[0].toUpperCase());
		   artifactInProd.add(warAndVer);
		   if(warAndVer.getArtifactName().indexOf("com.stubhub.domain")>-1){
			   System.out.println(String.format("%s,%s,%s,%s",warAndVer.getRole(),warAndVer.getArtifactName(),warAndVer.getVersion(),
					   includLatestVersion ? nu.getLatestReleaseVersionJar(warAndVer.getArtifactName()).getVersion() : " "));
		   }
		}
		br.close();
		return artifactInProd;
	}
}
