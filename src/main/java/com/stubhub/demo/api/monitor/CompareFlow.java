package com.stubhub.demo.api.monitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.JarInfo;
import com.stubhub.demo.api.monitor.entity.NexusArtifactObj;
import com.stubhub.demo.api.monitor.entity.PomDependency;
import com.stubhub.demo.api.monitor.entity.WadlGeneratorDescription;
import com.stubhub.demo.api.monitor.util.HttpUtil;
import com.stubhub.demo.api.monitor.util.MongoDBUtil;
import com.stubhub.demo.api.monitor.util.NexusUtil;
import com.stubhub.demo.api.monitor.util.WadlUtil;

public class CompareFlow {
	private boolean isProd = true;
	
	private static Logger logger = LoggerFactory.getLogger(CompareFlow.class);

	public static void main(String[] args) throws Exception{
		String warName = "com.stubhub.domain.inventory.war";
		String role = "SLX";
		String prodVersion = "1.3.20";
		updateWarProductionVersion(warName, role, prodVersion);
		
		DemoClient.initProperty();
		
		DemoClient.generatedAllSchemaDocuments();
		
		DemoClient.switchProd(false);
		
		DemoClient.generatedAllSchemaDocuments();
	}
	
	public static void updateWarProductionVersion(String warName, String role , String prodVersion) throws UnknownHostException{
		MongoClient mclient = new MongoClient("mongodb-34895.phx-os1.stratus.dev.ebay.com", 27017);
		DB db = mclient.getDB("dev_db");
		DBCollection dc = MongoDBUtil.getSpecificCollection("artifact");
		BasicDBObject searchDoc = new BasicDBObject().append("artifactName", warName).append("role", role);					
		
		DBObject old = MongoDBUtil.findOneObj(searchDoc);
		
		if(old.get("version").toString().equalsIgnoreCase(prodVersion)){
			logger.info("Version didn't change in production!");
		}
		else{
			BasicDBObject newDoc = new BasicDBObject();
			newDoc.append("$set", new BasicDBObject().append("version", prodVersion));
			
			dc.update(searchDoc, newDoc);
			logger.info("Update completed");
		}
		
	}
}
