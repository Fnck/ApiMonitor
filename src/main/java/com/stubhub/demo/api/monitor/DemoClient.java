package com.stubhub.demo.api.monitor;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.JarInfo;
import com.stubhub.demo.api.monitor.entity.NexusArtifactObj;
import com.stubhub.demo.api.monitor.entity.PomDependency;
import com.stubhub.demo.api.monitor.entity.WadlGeneratorDescription;
import com.stubhub.demo.api.monitor.util.HttpUtil;
import com.stubhub.demo.api.monitor.util.MongoDBUtil;
import com.stubhub.demo.api.monitor.util.NexusUtil;
import com.stubhub.demo.api.monitor.util.PerforceUtil;
import com.stubhub.demo.api.monitor.util.VersionUtil;
import com.stubhub.demo.api.monitor.util.WadlUtil;
import com.stubhub.demo.api.monitor.util.XmlFormatter;
import com.stubhub.demo.api.monitor.util.XmlUtil;
import com.sun.jersey.server.wadl.generators.WadlGeneratorGrammarsSupport;

@SuppressWarnings({ "deprecation", "resource", "rawtypes" })
public class DemoClient {
	
	final static Logger logger = LoggerFactory.getLogger(DemoClient.class);
	
	public DemoClient(){
		if(prop.isEmpty()){
			initProperty();
		}
	}
	
	private static Map<String, JarInfo> Dependencies = new HashMap<String, JarInfo>();
	private static Properties prop = new Properties();
	
	public static void main(String[] args) throws Exception {
		initProperty();
		
		generatedAllSchemaDocuments();
		
		switchProd(false);
		
		generatedAllSchemaDocuments();
	}
	
	private void loadDependencies(String dependencyPath) throws MalformedURLException{
		List<File> l = new ArrayList<File>();
		getAllJarFiles(dependencyPath, l);

		for(File f : l){
			int lastSlash = f.getName().lastIndexOf("-");
			String fileName = f.getName().substring(0, lastSlash);
			JarInfo ji = new JarInfo();
			ji.setArtifactName(fileName);
			ji.setVersion(f.getName().substring(lastSlash+1, f.getName().length()).replaceAll(".jar", ""));
			ji.setPath(f.getPath());
			ji.setJarUrl(new URL("jar:"+f.toURL()+"!/"));
			String artifactKey = ji.getArtifactName()+":"+ji.getVersion();
			if(Dependencies.get(artifactKey)==null){
				Dependencies.put(artifactKey, ji);
			}
		}		
	}
	
	private void getAllJarFiles(String directory, List<File> fileList){
		File f  = new File(directory);
		
		for(File i : f.listFiles()){
			if(i.isDirectory()){
				getAllJarFiles(i.getPath(), fileList);
			}else{
				if(i.getName().endsWith(".jar")){
					fileList.add(i);
				}				
			}
		}
	}
	
	public static void generatedAllSchemaDocuments() throws Exception{

		
		DemoClient dc = new DemoClient();
		
		String previousArtifactName = "";
		
		MongoDBUtil md = new MongoDBUtil();
		NexusUtil nu = new NexusUtil();		
		Gson jsonUtil = new Gson();
		
		String subPath = Arrays.toString(dc.getDownloadedJar().toArray());		
		logger.info(subPath);
		
		List<DBObject> queryResult = md.quertyFirstArtifacts("com.stubhub.domain.search.war");
		
		List<ArtifactInfo> artifactList = new ArrayList<ArtifactInfo>();
		ArtifactInfo a1 = new ArtifactInfo();
		a1.setArtifactName("com.stubhub.domain.inventory.war");
		a1.setVersion("1.3.10");
		ArtifactInfo a2 = new ArtifactInfo();
		a2.setArtifactName(a1.getArtifactName());
		a2.setVersion("1.3.18");
		boolean isProd = Boolean.parseBoolean(prop.getProperty("isProd"));
		if(isProd){
			artifactList.add(a1);
		}else{
			artifactList.add(a2);			
		}
		
		/*
		for(DBObject o : queryResult){
			ArtifactInfo a = jsonUtil.fromJson(JSON.serialize(o), ArtifactInfo.class);
			if(subPath.indexOf(a.getArtifactName())> -1){
				continue;
			}
			if(previousArtifactName.equalsIgnoreCase(a.getArtifactName())){
				continue;
			}
			
			if(isProd){
				logger.info("Production version "+a.getArtifactName() + ":" + a.getVersion());
			}else{
				logger.info("Latest version "+a.getArtifactName() + ":" + a.getVersion());
			}
			
			artifactList.add(a);
			
			previousArtifactName = a.getArtifactName();
		}		
		*/
		for(ArtifactInfo a : artifactList){
			dc.generateSchemaFileBaseOnWar(a.getArtifactName(), a.getVersion());
		}
	}
	
	private List<String> getDownloadedJar() throws IOException{		
		String rootFolder = getRootFolder();;
		
		File createdJar = new File(rootFolder + "Jar\\");
		String[] directories = createdJar.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		directories = directories==null?(new String[]{}):directories;
		List<String> jarPathes = new ArrayList<String>();
		for(String s : directories){
			String w = getDownloadedJar(rootFolder + "Jar\\" + s +"\\");
			File jarFile = new File(w);
			JarFile jar = new JarFile(jarFile.getPath());
			if(jar.size() > 0){
				jarPathes.add(jarFile.getName());
			}
		}
		return jarPathes;		
	}
	
	private String getDownloadedJar(String path){
		File folder = new File(path);
		String name = "";
		
		for(File f : folder.listFiles()){
			if(f.isDirectory()){
				name = getDownloadedJar(f.getPath());
			}
			else{
				return f.getPath();
			}
		}
		return name;
	}
	
	private void generateSchemaFileBaseOnWar(String warName, String version) throws Exception{
		boolean isProd = Boolean.parseBoolean(prop.getProperty("isProd"));
		URL targetUrl = Thread.currentThread().getContextClassLoader().getResource(".");        
        logger.info(targetUrl.getPath());
        
        String targetPath = targetUrl.getPath();
		DemoClient dc = new DemoClient();
		NexusUtil nu = new NexusUtil();
		
		String dependencyFolder = prop.getProperty("rootFolder")+ "\\Dependencies\\"+ (isProd ? "Production" : "Latest");		
		dc.loadDependencies(dependencyFolder);
		
		/*if(!isProd){
			NexusArtifactObj obj = nu.getLatestReleaseVersionJar(warName);
			version = obj.getVersion();
		}*/
		
        //The version of this intfArtifact is ${project.version}
        List<ArtifactInfo> intfAndImpl = nu.getIntfAndImplInfoByWar(warName, version, isProd);
        for(ArtifactInfo i : intfAndImpl){
        	logger.info(i.getArtifactName() + " : "+i.getVersion());
        }
        
        //Set it to null for get latest dependencies tree, sometimes the expected version artifact is missing in nexus
        PomDependency artifactPomInfo = nu.getIntfAndImplDependencies(intfAndImpl, version, warName, isProd);
        
        List<JarInfo> jarPathList = dc.downloadJarFile(artifactPomInfo, warName, version);
        
        String domainName = getDomainNameByWarName(warName);
        Map<String, String> binaryFilePath = new HashMap<String, String>();
        
        logger.info("------------Start to extract jar file---------------");
        List<URL> jarFiles = new ArrayList<URL>();
        for(JarInfo s : jarPathList){
        	jarFiles.add(s.getJarUrl());
        	binaryFilePath.putAll(dc.unZipJarFileIntoTargetPath(targetPath+"/jarfile/", s.getPath()));
        }

		URLClassLoader ucl = (URLClassLoader)URLClassLoader.newInstance(jarFiles.toArray(new URL[0]),this.getClass().getClassLoader());
		Thread.currentThread().setContextClassLoader(ucl);
		logger.info("------------Start to scan jar file---------------");
		for(JarInfo jInfo : jarPathList){			
			JarFile intfJar = new JarFile(jInfo.getPath());
			Enumeration<? extends JarEntry> enumeration = intfJar.entries();
			while(enumeration.hasMoreElements()){
				JarEntry entry = enumeration.nextElement();
				if (entry.getName().endsWith(".class")) {
		            String className = entry.getName();
		            className = className.replace(".class", "").replace("/", ".");		            
		            if((className.indexOf("com.stubhub.newplatform")>-1)||(className.indexOf(domainName)==-1)){
		            	continue;
		            }
		            if((className.indexOf("intf")>-1)||(className.indexOf("impl")>-1)||(className.indexOf("dto") >-1)){
		            	Class<?> clazz = ucl.loadClass(className);
			            int classAnno = dc.cxfAnnotatedClass(clazz);
			            if(classAnno == 0){
			            	continue;
			            }
			            if(classAnno % 2 == 0){
			            	logger.info("--------CXF Web service interface--------");
				            logger.info("From jar: "+className);       	
			            	logger.info(clazz.getName());

			            	Class m = Thread.currentThread().getContextClassLoader().loadClass(className);
			            	logger.info("From threadcontextclassloader: "+m.getName());
			            	
			            	String binaryPath = binaryFilePath.get(className);
			            	File binary = new File(binaryPath);
			            	String fileName = binary.getName();
			            	String newFilePath = binary.getAbsolutePath().replaceAll(fileName, "").concat(fileName.replaceAll(".class", ""));
			            	logger.info(binary.getName());
			            	File newBinary = new File(newFilePath+"\\"+fileName);
			            	FileUtils.copyFile(binary, newBinary);
			            	
			            	dc.convertClassToXadlFile(clazz,jInfo.getArtifactName() , jInfo.getVersion(), warName, version);
			            }else{
			            	logger.info("--------CXF Payload class--------");
			            	logger.info("From jar: "+className);		            	
			            	logger.info(clazz.getName());

			            	Class m = Thread.currentThread().getContextClassLoader().loadClass(className);
			            	logger.info("From threadcontextclassloader: "+m.getName());
			            	dc.convertClassToXsdFile(clazz, jInfo.getArtifactName() ,jInfo.getVersion(), warName, version);

			            }
		            }
		            
				}
			}
		}
		File f = new File(targetPath+"/jarfile/");
		if(f.isDirectory()){
			FileUtils.deleteDirectory(f);
		}
		Dependencies.clear();
	}
	
	private String getDomainNameByWarName(String warName){
		String[] tmp = warName.split("\\.");
		String ret = "";
		for(int i=0;i<4;i++){
			ret += tmp[i]+".";
		}
		return ret;
	}
	
	private int cxfAnnotatedClass(Class clazz){
		int annotated = 0;
		for(Annotation anno : clazz.getAnnotations()){
			if(anno.annotationType().getName().equalsIgnoreCase(XmlRootElement.class.getName()))
			{
				annotated +=1;
				break;
			}
			if((anno.annotationType().getName().equalsIgnoreCase(Path.class.getName()))
					||(anno.annotationType().getName().equalsIgnoreCase(Produces.class.getName()))
					||(anno.annotationType().getName().equalsIgnoreCase(Provider.class.getName()))){
				annotated +=2;
				break;
			}
		}
		return annotated;
	}
	
	private Map<String, String> unZipJarFileIntoTargetPath(String targetPath, String jarFilePath) throws Exception{
		Map<String, String> binaryFileInfo = new HashMap<String, String>();
		logger.info("Exacting: " + jarFilePath);
		JarFile jar = new JarFile(jarFilePath);
		Enumeration e = jar.entries();
		while (e.hasMoreElements()) {
		    JarEntry file = (JarEntry) e.nextElement();
		    if(jar.getName().indexOf("sh-ecomm-platform-datamodel") > -1){
	    		if(file.getName().indexOf("com/stubhub/")==-1){
	    			continue;
	    		}
	    	}
		    File f = new File(targetPath + File.separator + file.getName());
		    if ((file.isDirectory())&&(file.getName().indexOf("com/stubhub/") > -1)) {
		    	f.mkdirs();
		    	continue;
		    }
		    if ((file.getName().endsWith(".class")&&(file.getName().indexOf("com/stubhub/") > -1))){
		    	
		    	InputStream is = jar.getInputStream(file);
			    FileOutputStream fos = new FileOutputStream(f);
			    while (is.available() > 0) {
			    	fos.write(is.read());
			    }
			    fos.close();
			    is.close();
			    
			    binaryFileInfo.put(file.getName().replaceAll(".class", "").replaceAll("/", "."), f.getPath());
		    }		    
		}
		
		return binaryFileInfo;
	}
	
	private List<JarInfo> downloadJarFile(PomDependency p, String warName, String warVer) throws Exception{
		boolean isProd = Boolean.parseBoolean(prop.getProperty("isProd"));
		String rootFolder = getRootFolder();
		String rootDependenctyFolder = prop.getProperty("rootFolder") + (isProd ? "\\Dependencies\\Production\\" : "\\Dependencies\\Latest\\");
		
		NexusUtil nu = new NexusUtil();
		HttpUtil hu = new HttpUtil();
		String jarRegex = "(.+)(\\.jar)$";
		List<JarInfo> jarPath = new ArrayList<JarInfo>();
		for(ArtifactInfo a : p.getIntfAndImpl()){
			NexusArtifactObj jar = nu.getExpectedVersionJar(a.getArtifactName(), a.getVersion());
			if(jar.getRepositoryPath().matches(jarRegex)){
				JarInfo j = new JarInfo();
				j.setPath(hu.HttpDownloadFile(jar.getRepositoryPath(), null, rootFolder + "Jar\\" + warName + "\\" + warVer + "\\" + jar.getArtifactId() + "\\" + jar.getVersion() + "\\"));
				j.setJarUrl(new URL("jar:"+(new File(j.getPath())).toURL()+"!/"));
				j.setVersion(jar.getVersion());
				j.setArtifactName(jar.getArtifactId());
				jarPath.add(j);
			}
		}		
		for(ArtifactInfo d : p.getDependencies()){
			String artifactKey = d.getArtifactName()+":"+d.getVersion();
			
			logger.info("Dependency :::: " + artifactKey);
			JarInfo ji = Dependencies.get(artifactKey);
			if(ji == null){
				NexusArtifactObj dependencyJar = nu.getExpectedVersionJar(d.getArtifactName(), d.getVersion());
				if(dependencyJar.getRepositoryPath().matches(jarRegex)){
					JarInfo j = new JarInfo();
					j.setPath(hu.HttpDownloadFile(dependencyJar.getRepositoryPath(), null, rootDependenctyFolder + warName + "\\" + warVer + "\\" + dependencyJar.getArtifactId() + "\\" + dependencyJar.getVersion() + "\\"));
					j.setJarUrl(new URL("jar:"+(new File(j.getPath())).toURL()+"!/"));
					j.setVersion(dependencyJar.getVersion());
					j.setArtifactName(dependencyJar.getArtifactId());
					jarPath.add(j);
					Dependencies.put(artifactKey, j);
				}
			}else{
				jarPath.add(ji);
			}
		}
		return jarPath;
	}
	
	private void convertClassToXadlFile(Class<?> clazz, String artifactName, String ver, String warName, String version) throws Exception {
		String rootFolder = getRootFolder();
		
		String packageName = "jarfile."+clazz.getName();
		WadlUtil util = new WadlUtil();
		util._baseUri = "http://example.com:8080/rest";
		File path = new File(rootFolder + "WADL\\"+ warName +"\\" + version+ "\\" + artifactName + "\\" + ver + "\\" + clazz.getName()+"\\");
		if(!path.exists()){
			path.mkdirs();
		}
		util._wadlFile = new File(path.getAbsolutePath() +"\\" + packageName+".xml");
		util._formatWadlFile = true;
		util._packagesResourceConfig = new String[]{packageName};
		
		List<WadlGeneratorDescription> desc = new ArrayList<WadlGeneratorDescription>();
		util._wadlGenerators = desc;
		if(util._wadlFile.exists()){
			util._wadlFile.delete();
		}
		util.generateWadlDoc();
	}

	public void convertClassToXsdFile(final Class clazz, String artifactName, String ver, String warName, String version) throws IOException{
		String rootFolder = getRootFolder();
		
		String generatedFile = "";
		final File baseDir = new File(rootFolder + "XSD\\"+ warName +"\\" + version+ "\\"+ artifactName +"\\"+ver+"\\");
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}
		class FirstSchemaOutputResolver extends SchemaOutputResolver {
		    public Result createOutput( String namespaceUri, String suggestedFileName ) throws IOException {
		    	File f = new File(baseDir,clazz.getSimpleName()+".xml");
		    	if(f.exists()){
		    		f.delete();
		    	}
		        return new StreamResult(f);
		    }
		}
		
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(clazz);
			context.generateSchema(new FirstSchemaOutputResolver());
		} catch (Exception e) {
			logger.error("Exception while parser XSD for class {}:", clazz.getName(), e);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\XsdGeneratedIssue.txt", true)));
			out.println("war: " + warName);
			out.println("artifact: " + artifactName);
			out.println("ver: " + ver);
			out.println("isProd: " + prop.getProperty("isProd"));
			out.println("class: " + clazz.getName());
			out.println(ExceptionUtils.getFullStackTrace(e));
			out.println("------------------end------------------------");
			out.flush();
			out.close();
		} 		
	}
	
	public String getRootFolder(){
		boolean isProd = Boolean.parseBoolean(prop.getProperty("isProd"));
		String rootFolder = prop.getProperty("rootFolder") + (isProd ? "\\Production\\" : "\\Latest\\");
		return rootFolder;
	}
	
	public String getFilePathFromJarUrl(URL jarUrl) throws MalformedURLException{
		String path = jarUrl.getFile().split("!")[0];
		URL jarPath = new URL(path);
		return jarPath.getFile();
	}
	
	public static void initProperty(){
		prop.setProperty("rootFolder", "C:\\ApiMonitor");
		prop.setProperty("isProd", "true");
	}
	
	public static void switchProd(boolean isProd){
		prop.setProperty("isProd", String.valueOf(isProd));
	}
}
