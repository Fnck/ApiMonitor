package com.stubhub.demo.api.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.demo.api.monitor.entity.dto.CompareResult;
import com.stubhub.demo.api.monitor.entity.dto.CompareStatus;
import com.stubhub.demo.api.monitor.entity.dto.CompareType;
import com.stubhub.demo.api.monitor.util.MongoDBUtil;
import com.stubhub.demo.util.DiffLineByLine;

public class CompareSchema {
	final static Logger logger = LoggerFactory.getLogger(CompareSchema.class);
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Map<String, File> latestIntf = new HashMap<String, File>();
		Map<String, File> productionIntf = new HashMap<String, File>();
		
		Map<String, File> latestSchema = new HashMap<String, File>();
		Map<String, File> productionSchema = new HashMap<String, File>();
		
		CompareSchema cs = new CompareSchema();
		
		List<File> latestSchemaList = new ArrayList<File>();
		
		DemoClient dc = new DemoClient();
		dc.getRootFolder();
		DemoClient.switchProd(false);
		
		cs.getAllXmlFiles(dc.getRootFolder() + "\\WADL\\",latestSchemaList);
		cs.getAllXmlFiles(dc.getRootFolder() + "\\XSD\\",latestSchemaList);
		
		for(File f : latestSchemaList){
			String intfDocKey = "";
			String schemaDocKey = "";
			String[] fileKeys = f.getPath().split("\\\\");
			if(f.getPath().indexOf("WADL")>-1){
				intfDocKey = fileKeys[fileKeys.length-2]+":"+fileKeys[fileKeys.length-1];
				//System.out.println("Latest :"+schemaDoc);
				latestIntf.put(intfDocKey, f);
			}else{
				schemaDocKey = fileKeys[fileKeys.length-3]+":"+fileKeys[fileKeys.length-1];
				latestSchema.put(schemaDocKey, f);
			}
		}
		
		List<File> productSchema = new ArrayList<File>();
		DemoClient.switchProd(true);
		
		
		cs.getAllXmlFiles(dc.getRootFolder() + "\\WADL\\",productSchema);
		cs.getAllXmlFiles(dc.getRootFolder() + "\\XSD\\",productSchema);

		for(File f : productSchema){
			String intfDocKey = "";
			String schemaDocKey = "";
			String[] fileKeys = f.getPath().split("\\\\");
			if(f.getPath().indexOf("WADL")>-1){
				intfDocKey = fileKeys[fileKeys.length-2]+":"+fileKeys[fileKeys.length-1];
				productionIntf.put(intfDocKey, f);
			}
			else{
				schemaDocKey = fileKeys[fileKeys.length-3]+":"+fileKeys[fileKeys.length-1];
				productionSchema.put(schemaDocKey, f);
			}
			
			
			
		}
		
		cs.compareIntf(latestIntf, productionIntf);
		
		cs.compareSchema(latestSchema, productionSchema);
	}
	
	private void compareSchema(Map<String, File> latestSchema,
			Map<String, File> productionSchema) throws FileNotFoundException, UnknownHostException {
		List<CompareResult> diffObjs = new ArrayList<CompareResult>();
		MongoDBUtil mu = new MongoDBUtil();
		
		Map<String, String> warAndRole = mu.loadWarNameAndRole();
		
		int schemaCount = 0;
		Set<String> warNames = new HashSet<String>();
		Set<String> artifactNames = new HashSet<String>();
		
		for(String s : productionSchema.keySet()){
			if(latestSchema.containsKey(s)){				
				CompareResult id = new CompareResult();
				String[] latestPathes = latestSchema.get(s).getPath().split("\\\\");
				int len = latestPathes.length;

				id.setObjectName(latestPathes[len-1].replaceAll(".xml", ""));

				id.setArtifactName(latestPathes[len-3]);
				id.setWarName(latestPathes[len-5]);
				
				id.setLatestArtifactVersion(latestPathes[len-2]);				
				id.setLatestWarVersion(latestPathes[len-4]);

				if(!warNames.contains(id.getWarName())){
					warNames.add(id.getWarName());
				}
				
				if(!artifactNames.contains(id.getArtifactName())){
					artifactNames.add(id.getArtifactName());
				}
				
				String[] prodPathes = productionSchema.get(s).getPath().split("\\\\");
				len = prodPathes.length;

				id.setProdArtifactVersion(prodPathes[len-2]);
				id.setProdWarVersion(prodPathes[len-4]);

				id.setType(CompareType.schema);
				id.setRole(warAndRole.get(id.getWarName()));
				id.setCompareDate(getCurDateStr());
				
				DiffLineByLine bl = new DiffLineByLine();
				bl.diffFilesLineByLine(productionSchema.get(s).getPath(), latestSchema.get(s).getPath(), id, CompareType.schema);
				
				if((id.getDeleteNum()==0)&&(id.getInsertNum()==0)){
					id.setStatus(CompareStatus.success);
				}else{
					id.setStatus(CompareStatus.failed);
				}

				diffObjs.add(id);
				schemaCount++;
			}
			else{
				logger.info("Schema element was not found: key " + s);
			}
		}
		/*
		PrintWriter out = new PrintWriter("C:\\generatedXSDCompare.txt");
		out.println("Compared: " + schemaCount + " schemas in " + warNames.size() + " war package, " + artifactNames.size() + " jar.");
		out.println("<table><tr><td>Schema name</td>" +
				"<td>Artifact name</td>" +
				"<td>Latest version</td>" +
				"<td>Prod version</td>" +
				"<td>Difference</td></tr>");
		int z = 0;
		for(SchemaDifference dobj : diffObjs){
			//if(dobj.getDiffHtml() != null){
				out.println("<tr><td>"+dobj.getSchemaName()+"</td>");
				out.println("<td>"+dobj.getLatestArtifactName()+"</td>");
				out.println("<td>"+dobj.getLatestArtifactVersion()+"</td>");
				out.println("<td>"+dobj.getProdArtifactVersion()+"</td>");
				out.println("<td>");

				if((dobj.getDiffHtml() != null)&&(dobj.getDiffHtml().length()>0)){
					out.println("<input type=\"button\" value=\"click\" id=\"show"+z+"\" onclick=\"showDiv('"+z+"');\" /><div id=\"div"+z+"\" style=\"display:none\">");
					out.println(dobj.getDiffHtml());
				}			
				z++;
				out.println("</div></td></tr>");
			//}			
		}
		out.println("</table>");
		
		out.println();
		out.flush();
		out.close();
		*/
		
		mu.insertListObject(diffObjs);
		logger.info("Insert {} object into mongodb", diffObjs.size());
	}

	private void getAllXmlFiles(String directory, List<File> files){
		File dir = new File(directory);
		File[] fList = dir.listFiles();
		for(File subf: fList){
			if(subf.isDirectory()){
				getAllXmlFiles(subf.getAbsolutePath(), files);
			}
			else{
				if((subf.isFile())&&(subf.getName().endsWith(".xml"))){
					files.add(subf);
				}				
			}
		}
	}

	private void compareIntf(Map<String, File> latestIntf, Map<String, File> productionIntf) throws FileNotFoundException, UnknownHostException{
		List<CompareResult> diffObjs = new ArrayList<CompareResult>();
		MongoDBUtil mu = new MongoDBUtil();
		
		Map<String, String> warAndRole = mu.loadWarNameAndRole();
		
		int intfCount = 0;
		Set<String> warNames = new HashSet<String>();
		Set<String> artifactNames = new HashSet<String>();
		for(String s : productionIntf.keySet()){
			if(latestIntf.containsKey(s)){
				CompareResult cr = new CompareResult();
				
				String[] latestPathes = latestIntf.get(s).getPath().split("\\\\");
				int len = latestPathes.length;
				
				cr.setObjectName(latestPathes[len-2]);
				cr.setArtifactName(latestPathes[len-4]);
				cr.setWarName(latestPathes[len-6]);
				cr.setLatestArtifactVersion(latestPathes[len-3]);
				cr.setLatestWarVersion(latestPathes[len-5]);
				
				if(!warNames.contains(cr.getWarName())){
					warNames.add(cr.getWarName());
				}
				
				if(!artifactNames.contains(cr.getArtifactName())){
					artifactNames.add(cr.getArtifactName());
				}
				
				String[] prodPathes = productionIntf.get(s).getPath().split("\\\\");
				len = prodPathes.length;
				
				cr.setProdArtifactVersion(prodPathes[len-3]);
				cr.setProdWarVersion(prodPathes[len-5]);
				
				cr.setType(CompareType.intf);
				cr.setRole(warAndRole.get(cr.getWarName()));
				cr.setCompareDate(getCurDateStr());
				
				DiffLineByLine bl = new DiffLineByLine();
				bl.diffFilesLineByLine(productionIntf.get(s).getPath(), latestIntf.get(s).getPath(), cr, CompareType.intf);
				
				if((cr.getDeleteNum()==0)&&(cr.getInsertNum()==0)){
					cr.setStatus(CompareStatus.success);
				}else{
					cr.setStatus(CompareStatus.failed);
				}
				
				diffObjs.add(cr);
				intfCount ++;
			}
			else{
				logger.info("Interface element was not found: key " + s);
			}
		}
		/*
		PrintWriter out = new PrintWriter("C:\\generatedWADLCompare.txt");
		out.println("Compared: " + intfCount + " interfaces in " + warNames.size() + " war package, " + artifactNames.size() + " jar.");
		out.println("<table><tr><td>Interface name</td>" +
				"<td>Artifact name</td>" +
				"<td>Latest version</td>" +
				"<td>Prod version</td>" +
				"<td>Difference</td></tr>");
		int z = 0;
		for(InterfaceDifference dobj : diffObjs){
			if(dobj.getDiffHtml() != null){
				out.println("<tr><td>"+dobj.getInterfaceName()+"</td>");
				out.println("<td>"+dobj.getLatestArtifactName()+"</td>");
				out.println("<td>"+dobj.getLatestArtifactVersion()+"</td>");
				out.println("<td>"+dobj.getProdArtifactVersion()+"</td>");
				out.println("<td>");

				if((dobj.getDiffHtml() != null)&&(dobj.getDiffHtml().length()>0)){
					out.println("<input type=\"button\" value=\"click\" id=\"show"+z+"\" onclick=\"showDiv('"+z+"');\" /><div id=\"div"+z+"\" style=\"display:none\">");
					out.println(dobj.getDiffHtml());
				}			
				z++;
				out.println("</div></td></tr>");
			}			
		}
		out.println("</table>");
		
		out.println();
		out.flush();
		out.close();
		*/
		
		mu.insertListObject(diffObjs);
		logger.info("Insert {} object into mongodb", diffObjs.size());
	}
	
	private String getCurDateStr(){
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String ret = sdf.format(d);
		return ret;
	}
}
