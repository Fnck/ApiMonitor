package com.stubhub.demo.api.monitor.util;

import java.io.File;
import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.stubhub.demo.api.monitor.aether.util.AetherUtil;
import com.stubhub.demo.api.monitor.aether.util.CollectStubHubDependency;
import com.stubhub.demo.api.monitor.entity.ArtifactHits;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.ArtifactLinks;
import com.stubhub.demo.api.monitor.entity.NexusArtifactData;
import com.stubhub.demo.api.monitor.entity.NexusArtifactObj;
import com.stubhub.demo.api.monitor.entity.NexusSearchObj;
import com.stubhub.demo.api.monitor.entity.PomDependency;
import com.stubhub.demo.api.monitor.entity.RepoData;
import com.stubhub.demo.api.monitor.entity.RepoDetails;


public class NexusUtil {
	
	private static Logger logger = LoggerFactory.getLogger(NexusUtil.class);

	private String nexusSearch = "https://mvnrepository.stubcorp.dev/nexus/service/local/lucene/search?a=_ARTIFACT_NAME_&collapseresults=true";
	private String nexusArtifactPath = "https://mvnrepository.stubcorp.dev/nexus/service/local/artifact/maven/resolve?r=_REPOSITORY_&g=_GROUP_ID_&a=_ARTIFACT_NAME_&v=_VERSION_&c=&e=_EXTENSION_&isLocal=true";
	private String nexusWarArchivePath = "https://mvnrepository.stubcorp.dev/nexus/service/local/repositories/stubhub-releases/archive/";
	private String nexusArtifactContentPath = "https://mvnrepository.stubcorp.dev/nexus/service/local/repositories/stubhub-releases/content/";
	private String nexusArtifactListAllJar = "https://mvnrepository.stubcorp.dev/nexus/service/local/repositories/" +
			"stubhub-releases/index_content/?groupIdHint=_GROUP_ID_&artifactIdHint=_ARTIFACT_ID_";
	
	public NexusArtifactData getNexusArtifactObj(String repository, String groupId, String artifactName, String version, String extension) throws Exception{
		String url = nexusArtifactPath.replaceAll("_REPOSITORY_", repository)
				.replaceAll("_GROUP_ID_", groupId)
				.replaceAll("_ARTIFACT_NAME_", artifactName)
				.replaceAll("_VERSION_", version)
				.replaceAll("_EXTENSION_", extension);
		//HttpUtil hu = new HttpUtil();
		Header[] headers = new Header[]{new BasicHeader("Accept", "application/json"),
				new BasicHeader("Content-Type", "application/json")};
		String resp = HttpUtil.httpsGetRequest(url, headers );
		Gson g = new Gson();
		NexusArtifactData artifactObj = g.fromJson(resp, NexusArtifactData.class);
		return artifactObj;
	}
	

	public NexusSearchObj searchArtifactFromNexus(String artifactName) throws Exception{
		String url = nexusSearch.replaceAll("_ARTIFACT_NAME_", artifactName);
		//HttpUtil hu = new HttpUtil();
		Header[] headers = new Header[]{new BasicHeader("Accept", "application/json"),
				new BasicHeader("Content-Type", "application/json")};
		String resp = HttpUtil.httpsGetRequest(url, headers );
		Gson g = new Gson();
		NexusSearchObj searchObj = g.fromJson(resp, NexusSearchObj.class);
		return searchObj;
	}
	
	public NexusArtifactObj getExpectedVersionJar(String artifactId, String version) throws Exception{
		NexusUtil nu = new NexusUtil();
		NexusArtifactObj artifact = null;
		NexusSearchObj obj = nu.searchArtifactFromNexus(artifactId);
		String nexusRepoPath = "";
		String nexusRepoName = "";
		String jarPath = null;
		for(RepoDetails s : obj.getRepoDetails()){
			if(version.indexOf("SNAPSHOT") > -1){
				if(s.getRepositoryName().equalsIgnoreCase("stubhub-snapshots")){
					nexusRepoPath = s.getRepositoryURL();
					nexusRepoName = s.getRepositoryName();
					break;
				}
			}else{
				if(s.getRepositoryName().equalsIgnoreCase("stubhub-releases")){
					nexusRepoPath = s.getRepositoryURL();
					nexusRepoName = s.getRepositoryName();
					break;
				}
			}			
		}

		for(RepoData d2 : obj.getData()){
			if(d2.getVersion().equalsIgnoreCase(version)){
				String ext = null;
				if(d2.getArtifactHits().size() > 0){
					for(ArtifactLinks l : d2.getArtifactHits().get(0).getArtifactLinks()){
						if((l.getExtension().equalsIgnoreCase("jar"))||(l.getExtension().equalsIgnoreCase("war"))||(l.getExtension().equalsIgnoreCase("tar"))){
							ext = l.getExtension();
							break;
						}
					}
				}
				
				NexusArtifactData artifactData = nu.getNexusArtifactObj(nexusRepoName, 
						d2.getGroupId(), d2.getArtifactId()
						, d2.getVersion(), ext);
				jarPath = nexusRepoPath + "/content" + artifactData.getObj().getRepositoryPath();
				artifact = artifactData.getObj();
				artifact.setRepositoryPath(jarPath);
				break;
			}
		}
		return artifact;
	}
	
	public NexusArtifactObj getExpectedVersionPom(String artifactId, String version) throws Exception{
		NexusUtil nu = new NexusUtil();
		NexusArtifactObj artifact = null;
		/*if(artifactId.equalsIgnoreCase("com.stubhub.domain.catalog.publish.v3.intf")){
			version = "3.0.8.7";
		}*/
		NexusSearchObj obj = nu.searchArtifactFromNexus(artifactId);
		String nexusRepoPath = null;
		String nexusRepoName = null;
		String jarPath = null;
		for(RepoDetails s : obj.getRepoDetails()){
			if(version.indexOf("SNAPSHOT") > -1){
				if(s.getRepositoryName().equalsIgnoreCase("stubhub-snapshots")){
					nexusRepoPath = s.getRepositoryURL();
					nexusRepoName = s.getRepositoryName();
					break;
				}
			}else{
				if(s.getRepositoryName().equalsIgnoreCase("stubhub-releases")){
					nexusRepoPath = s.getRepositoryURL();
					nexusRepoName = s.getRepositoryName();
					break;
				}
			}			
		}
		for(RepoData d2 : obj.getData()){
			if(d2.getVersion().equalsIgnoreCase(version)){
				String ext = null;
				if(d2.getArtifactHits().size() > 0){
					for(ArtifactLinks l : d2.getArtifactHits().get(0).getArtifactLinks()){
						if(l.getExtension().equalsIgnoreCase("pom")){
							ext = l.getExtension();
							break;
						}
					}
				}
				
				NexusArtifactData artifactData = nu.getNexusArtifactObj(nexusRepoName, 
						d2.getGroupId(), d2.getArtifactId()
						, d2.getVersion(), ext);
				jarPath = nexusRepoPath + "/content" + artifactData.getObj().getRepositoryPath();
				artifact = artifactData.getObj();
				artifact.setRepositoryPath(jarPath);
				break;
			}
		}
		return artifact;
	}
	
	public NexusArtifactObj getLatestReleaseVersionJar(String artifactId) throws Exception{
		NexusUtil nu = new NexusUtil();
		NexusArtifactObj artifact = null;
		NexusSearchObj obj = nu.searchArtifactFromNexus(artifactId);
		String nexusRepoPath = null;
		String nexusRepoName = null;
		String jarPath = null;
		for(RepoDetails s : obj.getRepoDetails()){
			if(s.getRepositoryName().equalsIgnoreCase("stubhub-releases")){
				nexusRepoPath = s.getRepositoryURL();
				nexusRepoName = s.getRepositoryName();
			}
		}
		String currentVersion = null;
		if(obj.getData().size() > 0){
			currentVersion = obj.getData().get(0).getLatestRelease();
		}
		for(RepoData d2 : obj.getData()){
			if(d2.getVersion().equalsIgnoreCase(currentVersion)){
				String ext = null;
				if(d2.getArtifactHits().size() > 0){
					for(ArtifactLinks l : d2.getArtifactHits().get(0).getArtifactLinks()){
						if((l.getExtension().equalsIgnoreCase("jar"))||(l.getExtension().equalsIgnoreCase("war"))||(l.getExtension().equalsIgnoreCase("tar"))){
							ext = l.getExtension();
							break;
						}
					}
				}
				
				NexusArtifactData artifactData = nu.getNexusArtifactObj(nexusRepoName, 
						d2.getGroupId(), d2.getArtifactId()
						, d2.getVersion(), ext);
				jarPath = nexusRepoPath + "/content" + artifactData.getObj().getRepositoryPath();
				artifact = artifactData.getObj();
				artifact.setRepositoryPath(jarPath);
				break;
			}
		}
		return artifact;
	}
	
	public static void main(String[] args) throws Exception{
		PerforceUtil pu = new PerforceUtil();
		MongoDBUtil md = new MongoDBUtil();
		NexusUtil nu = new NexusUtil();
		
		logger.info(nu.getExpectedVersionWarArchivePath("com.stubhub.domain.catalog.zvents-datapush.war","1.0.1"));
		logger.info(nu.getExpectedVersionPomForArtifact("com.stubhub.domain.inventory.war", "1.3.18", true));
		/*
		Gson jsonUtil = new Gson();
		DBCollection dcol = md.getSpecificCollection("artifact");
		DBCursor cursor = dcol.find();
		List<DBObject> allElement = new ArrayList<DBObject>();
		while(cursor.hasNext()){
			allElement.add(cursor.next());
		}
		
		List<ArtifactInfo> artifactList = new ArrayList<ArtifactInfo>();
		for(DBObject o : allElement){
			ArtifactInfo a = jsonUtil.fromJson(JSON.serialize(o), ArtifactInfo.class);
			if(a.getArtifactName().indexOf("com.stubhub.domain") > -1){			
				artifactList.add(a);				
				String m = nu.getExpectedVersionPomFromWar(a.getArtifactName(),a.getVersion(),false);
				//List<ArtifactInfo> a = pu.getIntfAndImplInfoByWar("com.stubhub.domain.infrastructure.notifications.messages-war","1.0.1");
				logger.info(m);
			}
		}
		*/
	}

	private String getDomainNameFromWarName(String warName){
		String[] domain = warName.split("\\.");
		String domainStr = "";
		for(int i=0;i<domain.length;i++){
			if((warName.indexOf("notifications")>-1)||(warName.indexOf("zvents-datapush")>-1)){
				if(i>4){
					break;
				}else{
					domainStr += domain[i]+".";
				}
			}else{
				if(i>3){
					break;
				}else{
					domainStr += domain[i]+".";
				}
			}
		}
		return domainStr.substring(0, domainStr.length()-1);
	}
	
	
	
	private String getExpectedVersionWarArchivePath(String warName, String version){
		String[] domain = warName.split("\\.");
		String domainPath = getDomainNameFromWarName(warName).replaceAll("\\.", "/") + "/";
		domainPath += warName + "/";
		domainPath += version + "/";
		domainPath += warName + "-" + version + ".war/!/";
		return domainPath;
	}
	
	private String getExpectedVersionArtifactPomPath(String artifactName, String version){
		String[] domain = artifactName.split("\\.");
		String domainPath = getDomainNameFromWarName(artifactName).replaceAll("\\.", "/") + "/";
		domainPath += artifactName + "/";
		domainPath += version + "/";
		domainPath += artifactName + "-" + version + ".pom";
		return domainPath;
	}
	
	private String getRootFolder(boolean isProd){
		String folder = null;
		String osName = System.getProperty("os.name");
		if(osName.matches("^[W|w]indows.+")){
			folder = isProd ? "C:/ApiMonitor/Production/" : "C:/ApiMonitor/Latest/";
		}else{
			folder = isProd ? "/nas/home/hongfzhou/apimonitor/Production/" : "/nas/home/hongfzhou/apimonitor/Latest/";
		}
		
		return folder;
	}

	public String getExpectedVersionPomFromWar(String warName, String version, boolean isProd) throws Exception {
		String rootFolder = getRootFolder(isProd);
		String domainPath = getExpectedVersionWarArchivePath(warName,version);
		domainPath += "META-INF/maven/"+ getDomainNameFromWarName(warName) +"/"+warName+"/pom.xml";
		String warRootPath = nexusWarArchivePath + domainPath;
		logger.info(warRootPath);
		HttpUtil hu = new HttpUtil();
		String localPomPath = hu.HttpDownloadFile(warRootPath, null, rootFolder + "Pom/"+ warName +"/"+version+"/");
		return localPomPath;
	}

	public String getExpectedVersionPomForArtifact(String artifactName, String version, boolean isProd) throws Exception{
		String rootFolder = getRootFolder(isProd);
		String domainPath = getExpectedVersionArtifactPomPath(artifactName,version);
		String pomPath = nexusArtifactContentPath + domainPath;
		logger.info("download artifact POM from: {}", pomPath);
		HttpUtil hu = new HttpUtil();
		String localPomPath = hu.HttpDownloadFile(pomPath, null, rootFolder + "Pom/"+ artifactName +"/"+version+"/");
		return localPomPath;
	}

	public List<String> getAavaiableDomainPomFiles(
			List<ArtifactInfo> intfAndImpl, String warName, String version, boolean isProd) throws Exception {
		String rootFolder = getRootFolder(isProd);
		HttpUtil hu = new HttpUtil();
		List<String> pomPath = new ArrayList<String>();
		for(ArtifactInfo a : intfAndImpl){
			NexusArtifactObj obj = getExpectedVersionPom(a.getArtifactName(),a.getVersion());
			String pomNexusPath = obj.getRepositoryPath();
			String localPomPath = hu.HttpDownloadFile(pomNexusPath, null, rootFolder + "Pom/"+ warName +"/"+version+"/" 
								+ a.getArtifactName() + "/" + a.getVersion() + "/");
			pomPath.add(localPomPath);
		}
		return pomPath;
	}
	
	public List<ArtifactInfo> getIntfAndImplInfoByWar(String warName, String version, boolean isProd) throws Exception{
		List<ArtifactInfo> intfAndImpl = new ArrayList<ArtifactInfo>();
		NexusUtil nu = new NexusUtil();
		logger.info("Start to download pom.xml for {}, ver {}", warName, version);
		String pomPath = nu.getExpectedVersionPomForArtifact(warName, version, isProd);		
		File pomFile = new File(pomPath);	
		if(pomFile.exists()){
			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(new FileReader(pomFile));
			if(model.getArtifactId().indexOf(warName)> -1){
				final String groupId = "${project.groupId}";
				for(org.apache.maven.model.Dependency d : model.getDependencies()){
					if(((d.getGroupId().equalsIgnoreCase(groupId))||(d.getGroupId().indexOf("com.stubhub")>-1))&&((d.getArtifactId().indexOf(".intf")>-1)||((d.getArtifactId().indexOf(".impl")>-1)))){
						String artifactStr = d.getArtifactId().replaceAll("\\$\\{project.groupId\\}", model.getParent().getGroupId());
						ArtifactInfo info = new ArtifactInfo();
						info.setArtifactName(artifactStr);
						String artifactVer = "";
						if(d.getVersion()!=null){
							artifactVer = d.getVersion().equalsIgnoreCase("${project.version}") ? model.getVersion() : d.getVersion();
						}else{
							//find the project pom.xml, like com.stubhub.domain.catalog.zvents-datapush
							String projectPomPath = nu.getExpectedVersionPomForArtifact(model.getParent().getArtifactId(), model.getParent().getVersion(), isProd);
							Model projectModel = xpp3Reader.read(new FileReader(new File(projectPomPath)));
							for(org.apache.maven.model.Dependency d2 : projectModel.getDependencies()){
								if(d2.getArtifactId().equalsIgnoreCase(artifactStr)){
									if(d2.getVersion().indexOf("${") == -1){
										artifactVer = d2.getVersion();
									}else{
										String versionPlaceOrder = d2.getVersion().replaceAll("\\$\\{",	"").replaceAll("\\}", "");
										Properties dependenciesVer = projectModel.getProperties();
										artifactVer = dependenciesVer.get(versionPlaceOrder).toString();
									}
									break;
								}
								
							}
							for(org.apache.maven.model.Dependency d2 : projectModel.getDependencyManagement().getDependencies()){
								if(d2.getArtifactId().equalsIgnoreCase(artifactStr)){
									if(d2.getVersion().indexOf("${") == -1){
										artifactVer = d2.getVersion();
									}else{
										String versionPlaceOrder = d2.getVersion().replaceAll("\\$\\{","").replaceAll("\\}", "");
										Properties dependenciesVer = projectModel.getProperties();
										artifactVer = dependenciesVer.get(versionPlaceOrder).toString();
									}
									break;
								}
								
							}
							
						}
						info.setVersion(artifactVer == null ? model.getParent().getVersion() : artifactVer);
						String artifactGroupName = d.getGroupId().equalsIgnoreCase("${project.groupId}") ? model.getGroupId() : d.getGroupId();
						info.setGroupName(artifactGroupName == null ? model.getParent().getGroupId() : artifactGroupName);
						intfAndImpl.add(info);
					}				
				}
			}				
		}
		
		return intfAndImpl;
	}
	
	public PomDependency getIntfAndImplDependencies(List<ArtifactInfo> intfAndImpl, String version, String warName, boolean isProd) throws Exception{
		NexusUtil nu = new NexusUtil();
		List<String> pomPath = nu.getAavaiableDomainPomFiles(intfAndImpl,warName, version, isProd);
		PomDependency expectedPomInfo = new PomDependency();
		CollectStubHubDependency theNew = new CollectStubHubDependency();
		//remove the duplicate elements
		HashSet hs = new HashSet();
		hs.addAll(pomPath);
		pomPath.clear();
		pomPath.addAll(hs);
		
		Map<String,ArtifactInfo> dependencies = new HashMap<String,ArtifactInfo>();
		Map<String,ArtifactInfo> intfAndImplMap = new HashMap<String,ArtifactInfo>();
		List<ArtifactInfo> intfAndImplFull = new ArrayList<ArtifactInfo>();
		for(String pom : pomPath){
			logger.info(pom);
			File pomFile = new File(pom);
			if(pomFile.exists()){
				MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
				Model model = xpp3Reader.read(new FileReader(pomFile));
				
				ArtifactInfo current = new ArtifactInfo();
				current.setArtifactName(model.getArtifactId());
				current.setExtension(model.getPackaging());
				current.setGroupName(model.getParent().getGroupId());
				current.setVersion(model.getParent().getVersion());
				intfAndImplFull.add(current);
				if(!intfAndImplMap.containsKey(current.getArtifactName())){
					intfAndImplMap.put(current.getArtifactName(), current);
				}
				
				RepositorySystem system = AetherUtil.newRepositorySystem();

				RepositorySystemSession session = AetherUtil.newRepositorySystemSession(system);

				RemoteRepository publicRepo = AetherUtil.newStubhubPublicRepository();
				RemoteRepository repo = AetherUtil.newCentralRepository();
				RemoteRepository snapshotRepo = AetherUtil.newStubhubSnapshotRepository();			
				
				String artifactName = current.getGroupName() + ":" + current.getArtifactName() +":"+ current.getVersion();
				logger.info(artifactName);


				Artifact artifact = new DefaultArtifact( artifactName );

				CollectRequest collectRequest = new CollectRequest();
				collectRequest.setRoot( new Dependency( artifact, "" ) );
				
				collectRequest.addRepository(publicRepo);
				collectRequest.addRepository(snapshotRepo);
				collectRequest.addRepository( repo );

				CollectResult collectResult = system.collectDependencies( session, collectRequest );

				collectResult.getRoot().accept(theNew);
				
				for(ArtifactInfo a : theNew.getDependencyList()){
					if(intfAndImplMap.containsKey(a.getArtifactName())||(dependencies.containsKey(a.getArtifactName()))){
						continue;
					}else{
						dependencies.put(a.getArtifactName(), a);
					}
				}
			}
		}
		
		expectedPomInfo.setIntfAndImpl(intfAndImplFull);
		expectedPomInfo.setDependencies(new ArrayList<ArtifactInfo>(dependencies.values()));
		return expectedPomInfo;
	}


	public NexusArtifactObj getExpectedDatamodel(String artifactName,
			String version) throws Exception {
		String url = nexusArtifactListAllJar.replaceAll("_ARTIFACT_ID_", artifactName)
				.replaceAll("_GROUP_ID_", "com.stubhub.ecomm");
		Header[] headers = new Header[]{new BasicHeader("Accept", "application/json"),
				new BasicHeader("Content-Type", "application/json")};
		String resp = HttpUtil.httpsGetRequest(url, headers );
		JsonParser parser = new JsonParser();
		JsonArray indexBrowserTreeNodes = parser.parse(resp).getAsJsonObject().get("data")
				.getAsJsonObject().get("children").getAsJsonArray().get(0).getAsJsonObject()
				.get("children").getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject()
				.get("children").getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject()
				.get("children").getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject()
				.get("children").getAsJsonArray();
		for(int i=0;i<indexBrowserTreeNodes.size();i++){
			JsonElement je = indexBrowserTreeNodes.get(i);
			JsonObject currentArtifactInfo = je.getAsJsonObject()
			.get("children").getAsJsonArray().get(0).getAsJsonObject();
			if((currentArtifactInfo.get("artifactId").getAsString().equalsIgnoreCase(artifactName))
					&&(currentArtifactInfo.get("version").getAsString().equalsIgnoreCase(version))){
				NexusArtifactObj aObj = new NexusArtifactObj();
				aObj.setArtifactId(artifactName);
				aObj.setExtension(currentArtifactInfo.get("extension").getAsString());
				aObj.setGroupId(currentArtifactInfo.get("groupId").getAsString());
				//aObj.setRepositoryPath(currentArtifactInfo.get("artifactUri").getAsString());
				aObj.setRepositoryPath("https://mvnrepository.stubcorp.dev/nexus/service/local/repositories/stubhub-releases/content/"
						+ currentArtifactInfo.get("path").getAsString());
				aObj.setVersion(version);
				return aObj;
			}
			
		}		
		return null;
	}
}
