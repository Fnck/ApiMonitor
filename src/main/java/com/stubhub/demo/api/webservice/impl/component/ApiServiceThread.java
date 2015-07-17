package com.stubhub.demo.api.webservice.impl.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.JarInfo;
import com.stubhub.demo.api.monitor.entity.NexusArtifactObj;
import com.stubhub.demo.api.monitor.entity.PomDependency;
import com.stubhub.demo.api.monitor.entity.WadlGeneratorDescription;
import com.stubhub.demo.api.monitor.entity.dto.CommonCompareDocument;
import com.stubhub.demo.api.monitor.entity.dto.CommonCompareResult;
import com.stubhub.demo.api.monitor.entity.dto.CompareStatus;
import com.stubhub.demo.api.monitor.entity.dto.CompareType;
import com.stubhub.demo.api.monitor.util.HttpUtil;
import com.stubhub.demo.api.monitor.util.MongoDBUtil;
import com.stubhub.demo.api.monitor.util.NexusUtil;
import com.stubhub.demo.api.monitor.util.WadlUtil;
import com.stubhub.demo.api.monitor.util.XsdUtil;
import com.stubhub.demo.util.DiffLineByLine;

public class ApiServiceThread implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(ApiServiceThread.class);
	
	private static Properties prop = new Properties();
	private Map<String, JarInfo> Dependencies = new HashMap<String, JarInfo>();
	private NexusUtil nu = new NexusUtil();
	private String warName = null;
	private String prodVersion = null;
	private String latestVersion = null;
	private String taskId = null;
	private String role = null;
	private boolean isDetailed = false;
	
	public ApiServiceThread(String warName, String prodVersion,
			String latestVersion, String taskId, String role, boolean isDetailed){
		if(prop.isEmpty()){
			String osName = System.getProperty("os.name");
			if(osName.matches("^[W|w]indows.+")){
				prop.setProperty("rootFolder", "C:/Apimonitor/");
			}else{
				prop.setProperty("rootFolder", "/nas/home/hongfzhou/apimonitor/");
			}			
			prop.setProperty("isProd", "true");	        
	        URL targetUrl = Thread.currentThread().getContextClassLoader().getResource(".");
	        String targetPath = targetUrl.getPath();
			prop.setProperty("targetPath", targetPath);
		}
		this.warName = warName;
		this.prodVersion = prodVersion;
		this.latestVersion = latestVersion;
		this.taskId = taskId;
		this.role = role;
		this.isDetailed = isDetailed;
	}
	
	private boolean getIsProd(){
		if(prop.isEmpty()){
			prop.setProperty("rootFolder", "/nas/home/hongfzhou/apimonitor/");
			prop.setProperty("isProd", "true");
			URL targetUrl = Thread.currentThread().getContextClassLoader().getResource(".");
	        String targetPath = targetUrl.getPath();
			prop.setProperty("targetPath", targetPath);
		}
		return Boolean.parseBoolean(prop.getProperty("isProd"));
	}
	
	@Override
	public void run() {
		logger.info("Thread started: ");
		logger.info("target path: {}", prop.get("targetPath"));
		String dependencyFolder = prop.getProperty("rootFolder")+ "Dependencies/";
		try {
			loadDependencies(dependencyFolder);

			//Start production version
			List<ArtifactInfo> intfAndImplProd = loadIntfAndImpl(this.warName, this.prodVersion, true);
			PomDependency artifactPomInfoProd = nu.getIntfAndImplDependencies(intfAndImplProd, this.prodVersion, this.warName, true);
			HashMap<CompareType, HashMap<String, String>> prodData = downloadAndGenerateSchema(artifactPomInfoProd, this.warName, this.prodVersion);

			//Start latest version
			List<ArtifactInfo> intfAndImplLatest = loadIntfAndImpl(this.warName, this.latestVersion, false);
			PomDependency artifactPomInfoLatest = nu.getIntfAndImplDependencies(intfAndImplLatest, this.latestVersion, this.warName, false);
			HashMap<CompareType, HashMap<String, String>> latestData = downloadAndGenerateSchema(artifactPomInfoLatest, this.warName, this.latestVersion);

			//HashMap<String, CommonCompareResult> intfResult = new HashMap<String, CommonCompareResult>();
			List<CommonCompareResult> intfResult = new ArrayList<CommonCompareResult>();

			//HashMap<String, CommonCompareResult> schemaResult = new HashMap<String, CommonCompareResult>();
			List<CommonCompareResult> schemaResult = new ArrayList<CommonCompareResult>();
			
			//Start to compare intf
			for(String key : prodData.get(CompareType.intf).keySet()){
				String prodIntf = prodData.get(CompareType.intf).get(key);
				String latestIntf = latestData.get(CompareType.intf).get(key);
				if(latestIntf == null){
					logger.error("{} was missed in latest SHAPE war package!", key);
					continue;
				}

				List<String> original = getStringListFromString(prodIntf);
				List<String> revised = getStringListFromString(latestIntf);

				DiffLineByLine bl = new DiffLineByLine();
				CommonCompareResult compareResult = bl.getDiffHtml(original, revised, CompareType.intf, this.isDetailed);
				compareResult.setObjectName(key);
				compareResult.setObjectType(CompareType.intf);
				intfResult.add(compareResult);
			}


			//Start to compare schema
			for(String key : prodData.get(CompareType.schema).keySet()){
				String prodSchema = prodData.get(CompareType.schema).get(key);
				String latestSchema = latestData.get(CompareType.schema).get(key);
				if(latestSchema == null){
					logger.error("{} was missed in latest SHAPE war package!", key);
					continue;
				}

				List<String> original = getStringListFromString(prodSchema);
				List<String> revised = getStringListFromString(latestSchema);

				DiffLineByLine bl = new DiffLineByLine();
				CommonCompareResult compareResult = bl.getDiffHtml(original, revised,CompareType.schema, this.isDetailed);
				compareResult.setObjectName(key);
				compareResult.setObjectType(CompareType.schema);
				schemaResult.add(compareResult);
			}

			CommonCompareDocument compareDoc = new CommonCompareDocument();
			compareDoc.setWarName(warName);
			compareDoc.setProdWarVersion(prodVersion);
			compareDoc.setLatestWarVersion(latestVersion);
			compareDoc.setIntfResult(intfResult);
			compareDoc.setSchemaResult(schemaResult);
			compareDoc.setRole(role);

			MongoDBUtil mu = new MongoDBUtil();
			mu.insertCompareDocument(taskId, compareDoc);
			logger.info("task {} was completed!",taskId);
			
			clearDependencies();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	private HashMap<CompareType, HashMap<String, String>> downloadAndGenerateSchema(PomDependency artifactPomInfo, String warName, String version) throws Exception{
		List<JarInfo> jarPathList = downloadJarFile(artifactPomInfo, warName, version);
        
        String domainName = getDomainNameByWarName(warName);
        Map<String, String> binaryFilePath = new HashMap<String, String>();

        String targetPath = prop.getProperty("targetPath");
        logger.info("Current target path: {}", targetPath);
        logger.info("------------Start to extract jar file---------------");
        logger.info("thread name: {}", Thread.currentThread().getName());
        String threadFolder = Thread.currentThread().getName().replaceAll("-", "");
        String tmpClassFolder = targetPath+"/jarfile/"+threadFolder+"/";
        List<URL> jarFiles = new ArrayList<URL>();
        for(JarInfo s : jarPathList){
        	jarFiles.add(s.getJarUrl());
        	binaryFilePath.putAll(unZipJarFileIntoTargetPath(tmpClassFolder, s.getPath()));
        }
        
        URLClassLoader ucl = (URLClassLoader)URLClassLoader.newInstance(jarFiles.toArray(new URL[]{}),this.getClass().getClassLoader());
		Thread.currentThread().setContextClassLoader(ucl);
		
		HashMap<String, String> xsdMap = new HashMap<String, String>();
		HashMap<String, String> xadlMap = new HashMap<String, String>();
		
		logger.info("------------Start to scan jar file---------------");
		for(JarInfo jInfo : jarPathList){
			JarFile intfJar = new JarFile(jInfo.getPath());
			Enumeration<? extends JarEntry> enumeration = intfJar.entries();
			while(enumeration.hasMoreElements()){
				JarEntry entry = enumeration.nextElement();
				if (entry.getName().endsWith(".class")) {
		            String className = entry.getName();
		            className = className.replace(".class", "").replace("/", ".");		      
		            //temporarily set domain name to com.stubhub.domain.inventory.intf 
		            if((className.indexOf("com.stubhub.newplatform")>-1)||(className.indexOf(domainName)==-1)){
		            	continue;
		            }
		            
		            if((className.indexOf("intf")>-1)||(className.indexOf("impl")>-1)||(className.indexOf("dto") >-1)){
		            	logger.info("Load class:{}",className);
		            	Class<?> clazz = null;
		            	try{
		            		clazz = ucl.loadClass(className);
		            	}catch(Exception e){
		            		throw e;
		            	}		            	
			            
			            int classAnno = cxfAnnotatedClass(clazz);
			            if(classAnno == 0){
			            	continue;
			            }
			            if(classAnno % 2 == 0){
			            	//logger.info("--------CXF Web service interface--------");
				            //logger.info("From jar: "+className);       	
			            	//logger.info(clazz.getName());

			            	Class m = Thread.currentThread().getContextClassLoader().loadClass(className);
			            	logger.info("From threadcontextclassloader: "+m.getName());
			            	
			            	String binaryPath = binaryFilePath.get(className);
			            	File binary = new File(binaryPath);
			            	String fileName = binary.getName();
			            	String newFilePath = binary.getAbsolutePath().replaceAll(fileName, "").concat(fileName.replaceAll(".class", ""));
			            	logger.info(binary.getName());
			            	File newBinary = new File(newFilePath+"/"+fileName);
			            	FileUtils.copyFile(binary, newBinary);
			            	
			            	String xadl = convertClassToXadlStr(clazz, "jarfile."+threadFolder+".");
			            	xadlMap.put(clazz.getName(), xadl);
			            }else{
			            	//logger.info("--------CXF Payload class--------");
			            	//logger.info("From jar: "+className);
			            	//logger.info(clazz.getName());

			            	Class m = Thread.currentThread().getContextClassLoader().loadClass(className);
			            	logger.info("From threadcontextclassloader: "+m.getName());
			            	String xsd = convertClassToXmlStr(clazz);
			            	xsdMap.put(clazz.getName(), xsd);
			            }
		            }		            
				}
			}
		}
		File f = new File(tmpClassFolder);
		if(f.isDirectory()){
			FileUtils.deleteDirectory(f);
		}
		
		HashMap<CompareType, HashMap<String, String>> result = new HashMap<CompareType, HashMap<String, String>>();
		result.put(CompareType.schema, xsdMap);
		result.put(CompareType.intf ,xadlMap);
		return result;
	}
	
	private String getDomainNameByWarName(String warName){
		String[] tmp = warName.split("\\.");
		String ret = "";
		for(int i=0;i<4;i++){
			ret += tmp[i]+".";
		}
		return ret;
	}
	
	private List<ArtifactInfo> loadIntfAndImpl(String warName, String version,boolean isProd) throws Exception{		
		List<ArtifactInfo> intfAndImpl = nu.getIntfAndImplInfoByWar(warName, version, isProd);
        for(ArtifactInfo i : intfAndImpl){
        	logger.info(i.getArtifactName() + " : "+i.getVersion());
        }
        return intfAndImpl;
	}
	
	private void loadDependencies(String dependencyPath) throws MalformedURLException{
		logger.info("loading dependency from {}", dependencyPath);
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
	
	private void clearDependencies() throws MalformedURLException{
		Dependencies.clear();
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
	
	public String getRootFolder(){
		boolean isProd = Boolean.parseBoolean(prop.getProperty("isProd"));
		String rootFolder = prop.getProperty("rootFolder") + (isProd ? "/Production/" : "/Latest/");
		return rootFolder;
	}
	
	private List<JarInfo> downloadJarFile(PomDependency p, String warName, String warVer) throws Exception{
		boolean isProd = Boolean.parseBoolean(prop.getProperty("isProd"));
		String rootFolder = getRootFolder();
		String rootDependenctyFolder = prop.getProperty("rootFolder") + (isProd ? "/Dependencies/Production/" : "/Dependencies/Latest/");
		
		NexusUtil nu = new NexusUtil();
		HttpUtil hu = new HttpUtil();
		String jarRegex = "(.+)(\\.jar)$";
		List<JarInfo> jarPath = new ArrayList<JarInfo>();
		for(ArtifactInfo a : p.getIntfAndImpl()){
			NexusArtifactObj jar = null;
			if(a.getArtifactName().equalsIgnoreCase("sh-ecomm-platform-datamodel")){
				jar = nu.getExpectedDatamodel(a.getArtifactName(), a.getVersion());
			}else{
				jar = nu.getExpectedVersionJar(a.getArtifactName(), a.getVersion());
			}
			if(jar.getRepositoryPath().matches(jarRegex)){
				JarInfo j = new JarInfo();
				j.setPath(hu.HttpDownloadFile(jar.getRepositoryPath(), null, rootFolder + "Jar/" + warName + "/" + warVer + "/" + jar.getArtifactId() + "/" + jar.getVersion() + "/"));
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
				NexusArtifactObj dependencyJar = null;
				if(d.getArtifactName().equalsIgnoreCase("sh-ecomm-platform-datamodel")){
					dependencyJar = nu.getExpectedDatamodel(d.getArtifactName(), d.getVersion());
				}else{
					dependencyJar = nu.getExpectedVersionJar(d.getArtifactName(), d.getVersion());
				}
				if(dependencyJar.getRepositoryPath().matches(jarRegex)){
					JarInfo j = new JarInfo();
					j.setPath(hu.HttpDownloadFile(dependencyJar.getRepositoryPath(), null, rootDependenctyFolder + warName + "/" + warVer + "/" + dependencyJar.getArtifactId() + "/" + dependencyJar.getVersion() + "/"));
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
	
	private String convertClassToXadlStr(Class<?> clazz, String packagePrefix) throws Exception {		
		String packageName = packagePrefix + clazz.getName();
		WadlUtil util = new WadlUtil();
		util._baseUri = "http://example.com:8080/rest";

		util._formatWadlFile = true;
		util._packagesResourceConfig = new String[]{packageName};
		
		List<WadlGeneratorDescription> desc = new ArrayList<WadlGeneratorDescription>();
		util._wadlGenerators = desc;
		/*if(util._wadlFile.exists()){
			util._wadlFile.delete();
		}*/
		return util.generateWadl();
	}
	
	public String convertClassToXmlStr(final Class clazz) throws Exception{
		XsdUtil xu = new XsdUtil();
		String classTypeXml = xu.convertClassToXmlStr(clazz);
		if(classTypeXml == null){
			logger.error("!!Convert XSD got exception!!");
		}
		return classTypeXml;
	}
	
	/***
	 * 
	 * @param targetPath
	 * @param jarFilePath
	 * @return Map<Interface name or class name, class binary path>
	 * @throws Exception
	 */
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
	
	private List<String> getStringListFromString(String xmlDoc){
		String[] tmp = xmlDoc.split("\r?\n");
		List<String> ret = Arrays.asList(tmp);
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

}
