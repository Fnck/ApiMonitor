package com.stubhub.demo.api.monitor.util;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.dto.CommonCompareDocument;
import com.stubhub.demo.api.monitor.entity.dto.CompareResult;
import com.stubhub.demo.api.monitor.entity.dto.ReportObj;

public class MongoDBUtil {
	
	private static Logger logger = LoggerFactory.getLogger(MongoDBUtil.class);
	
	public List<DBObject> quertyFirstArtifacts(String artifactName) throws UnknownHostException{
		MongoClient mclient = new MongoClient("srwd00tlk001.stubcorp.dev", 27017);
		DB db = mclient.getDB("dev_db");
		
		List<DBObject> result = new ArrayList<DBObject>();
		if(db.collectionExists("artifact")){			
			DBObject artifactMatch = new BasicDBObject("$regex",artifactName);
			DBObject query = new BasicDBObject("artifactName", artifactMatch);
			DBCursor dbCursor = db.getCollection("artifact").find(query);
			if (dbCursor != null) {
                while (dbCursor.hasNext()) {
                	result.add(dbCursor.next());
                }
            }
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception{
		MongoDBUtil mu = new MongoDBUtil();
		ReportObj o = mu.getTaskSummaryData();
		Gson g = new Gson();
		logger.info(g.toJson(o));
	}
	
	public void insertCompareDocument(String taskId, CommonCompareDocument compareDoc ) throws UnknownHostException{
		DBCollection compareCol = getSpecificCollection("apiCompareResult");
		DBObject searchQuery = BasicDBObjectBuilder.start().add("_id", Integer.parseInt(taskId)).get();

		Gson jsonUtil = new Gson();		
		DBObject newDoc = (DBObject) JSON.parse(jsonUtil.toJson(compareDoc));
		logger.info(jsonUtil.toJson(compareDoc));
		DBObject insertNewDoc = BasicDBObjectBuilder.start().add("document", newDoc).get();
		//newDoc.put("_id", taskId);
		compareCol.update(searchQuery, insertNewDoc);
	}
	
	public CommonCompareDocument getCompareDocument(String taskId) throws Exception {

		DBCollection compareCol = getSpecificCollection("apiCompareResult");
		DBObject searchQuery = BasicDBObjectBuilder.start().add("_id", Integer.parseInt(taskId)).get();
		
		Gson jsonUtil = new Gson();
		DBCursor dc = compareCol.find(searchQuery);
		if(dc.hasNext()){
			String objStr = JSON.serialize(compareCol.find(searchQuery).next().get("document"));
			CommonCompareDocument ccd = jsonUtil.fromJson(objStr, CommonCompareDocument.class);
			return ccd;
		}else{
			throw new Exception("Nothing found for this ID!");
		}		
	}
	
	public String creatTask() throws UnknownHostException{
		int newId = 0;
		DBCollection compareCol = getSpecificCollection("apiCompareResult");

		DBObject orderBy = BasicDBObjectBuilder.start().add("_id", -1).get();
		DBObject query = BasicDBObjectBuilder.start().add("_id", "true").get();
		DBCursor dc = compareCol.find(new BasicDBObject(), query).sort(orderBy).limit(1);		

		if(dc.hasNext()){
			DBObject id = compareCol.find(new BasicDBObject(), query).sort(orderBy).limit(1).next();
			newId= Integer.parseInt(id.get("_id").toString()) + 1;

		}else{
			newId = 1;
		}
		DBObject newItem = BasicDBObjectBuilder.start().add("_id", newId).get();
		compareCol.insert(newItem);
		logger.info("new ID: {}", newId);

		
		return String.valueOf(newId);
	}
	
	public ReportObj getTaskSummaryData() throws UnknownHostException{
		ReportObj ro = new ReportObj();
		DBCollection compareCol = getSpecificCollection("apiCompareResult");
		
		Map<String, List<String>> artifactAndRole = new HashMap<String, List<String>>();
		DBCursor dc = compareCol.find(new BasicDBObject());
		while(dc.hasNext()){
			DBObject obj = dc.next();
			DBObject doc = ((DBObject) obj.get("document"));
			if(doc != null){
				String str =doc.get("warName").toString();
				if(!artifactAndRole.containsKey(str)){
					artifactAndRole.put(str, new ArrayList<String>());
				}
			}
		}
		
		for(String s : artifactAndRole.keySet()){
			DBCollection artifactAndRoleCol = getSpecificCollection("artifact");
			List<String> roles = new ArrayList<String>();
			DBObject queryArtifact = new BasicDBObject();
			queryArtifact.put("artifactName", s);
			DBCursor dcRole = artifactAndRoleCol.find(queryArtifact);
			while(dcRole.hasNext()){
				String roleStr = dcRole.next().get("role").toString();
				if(!roles.contains(roleStr)){
					roles.add(roleStr);
				}
			}
			artifactAndRole.get(s).addAll(roles);
		}
		String roleStr = "";
		int totalExecuted = 0, totalPassed = 0, totalFailed = 0;
		for(String s : artifactAndRole.keySet()){
			DBObject taskObj = new BasicDBObject();
			taskObj.put("document.warName", s);
			DBObject orderBy = BasicDBObjectBuilder.start().add("_id", -1).get();
			DBCursor dcLatestTask = compareCol.find(taskObj, new BasicDBObject()).sort(orderBy).limit(1);
			while(dcLatestTask.hasNext()){
				DBObject thisTask = dcLatestTask.next();
				DBObject document = (DBObject) thisTask.get("document");
				List<DBObject> intfResult = (List<DBObject>) document.get("intfResult");
				List<DBObject> schemaResult = (List<DBObject>) document.get("schemaResult");
				for(DBObject o : intfResult){
					if(o.get("status").toString().equalsIgnoreCase("success")){
						totalPassed++;
					}
					if(o.get("status").toString().equalsIgnoreCase("failed")){
						totalFailed++;
					}
				}
				for(DBObject o : schemaResult){
					if(o.get("status").toString().equalsIgnoreCase("success")){
						totalPassed++;
					}
					if(o.get("status").toString().equalsIgnoreCase("failed")){
						totalFailed++;
					}
				}
			}
			for(String role : artifactAndRole.get(s)){
				if(roleStr.indexOf(role) == -1){
					roleStr += role + " ,";
				}
			}
			
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ro.setCreatedDate(sdf.format(new Date()));
		ro.setTotalPassed(totalPassed);
		ro.setTotalFailed(totalFailed);
		ro.setTotalExecuted(totalPassed+totalFailed);
		ro.setFailedRoles(roleStr.substring(0, roleStr.length()-2));
		return ro;
	}
	
	public Map<String, String> loadWarNameAndRole() throws UnknownHostException{
		MongoClient mclient = new MongoClient("srwd00tlk001.stubcorp.dev", 27017);
		DB db = mclient.getDB("dev_db");
		
		Map<String, String> warAndRole = new HashMap<String, String>();
		
		Gson jsonUtil = new Gson();
		if(db.collectionExists("artifact")){
			DBCursor dbCursor = db.getCollection("artifact").find();
			if(dbCursor != null){
				while(dbCursor.hasNext()){
					DBObject cur = dbCursor.next();
					String warName = (String) cur.get("artifactName");
					String role = (String) cur.get("role");
					if(!warAndRole.containsKey(warName)){
						warAndRole.put(warName, role);
					}						
				}
			}
		}
		
		return warAndRole;
	}
	
	public void updateProductionArtifactList(List<ArtifactInfo> alist) throws UnknownHostException{
		DBCollection artifactCol = getSpecificCollection("artifact");
		Gson jsonUtil = new Gson();
		
		List<DBObject> insertList = new ArrayList<DBObject>();
		for(ArtifactInfo artifact : alist){			
			DBObject dbo = (DBObject) JSON.parse(jsonUtil.toJson(artifact));
			insertList.add(dbo);
		}
		artifactCol.remove(new BasicDBObject());
		artifactCol.insert(insertList);
	}
	
	public void updateQaArtifactList(List<ArtifactInfo> alist) throws UnknownHostException{
		DBCollection artifactCol = getSpecificCollection("artifactQa");
		Gson jsonUtil = new Gson();
		
		List<DBObject> insertList = new ArrayList<DBObject>();
		for(ArtifactInfo artifact : alist){			
			DBObject dbo = (DBObject) JSON.parse(jsonUtil.toJson(artifact));
			insertList.add(dbo);
		}
		artifactCol.remove(new BasicDBObject());
		artifactCol.insert(insertList);
	}
	
	public void insertListObject(List<CompareResult> objs) throws UnknownHostException{
		MongoClient mclient = new MongoClient("srwd00tlk001.stubcorp.dev", 27017);
		DB db = mclient.getDB("dev_db");
		Gson jsonUtil = new Gson();
		
		List<DBObject> insertList = new ArrayList<DBObject>();
		for(CompareResult intf : objs){			
			DBObject dbo = (DBObject) JSON.parse(jsonUtil.toJson(intf));
			insertList.add(dbo);
		}
		
		if(db.collectionExists("api_compare_result")){
			db.getCollection("api_compare_result").insert(insertList);
		}
	}
	
	public static DBObject findOneObj(DBObject searchDoc) throws UnknownHostException{
		DBCursor dc = getSpecificCollection("artifact").find(searchDoc);
		if(dc != null){
			while(dc.hasNext()){
				DBObject cur = dc.next();
				return cur;
			}
		}
		return null;
	}
	
	public static DBCollection getSpecificCollection(String collectionName) throws UnknownHostException{
		MongoClient mclient = new MongoClient("srwd00tlk001.stubcorp.dev", 27017);
		DB db = mclient.getDB("dev_db");
		
		DBCollection expectedCol = null;
		DBObject opt = BasicDBObjectBuilder.start().add("capped", false).get();;
		if(db.collectionExists(collectionName)){			
			expectedCol = db.getCollection(collectionName);
		}else{
			expectedCol = db.createCollection(collectionName, opt);
		}
		return expectedCol;
	}
}
