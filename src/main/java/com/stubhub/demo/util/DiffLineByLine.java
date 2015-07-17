package com.stubhub.demo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;








import com.perforce.p4java.Log;
import com.stubhub.demo.api.monitor.entity.dto.CommonCompareResult;
import com.stubhub.demo.api.monitor.entity.dto.CompareResult;
import com.stubhub.demo.api.monitor.entity.dto.CompareStatus;
import com.stubhub.demo.api.monitor.entity.dto.CompareType;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

public class DiffLineByLine {
	private static Logger logger = LoggerFactory.getLogger(DiffLineByLine.class);
	
	public static void main(String[] args) throws IOException{
		String first = "C:\\ApiMonitor\\Latest\\WADL\\com.stubhub.domain.inventory.war" +
				"\\1.3.18\\com.stubhub.domain.inventory.intf\\1.3.18" +
				"\\com.stubhub.domain.inventory.listings.intf.ListingService" +
				"\\jarfile.com.stubhub.domain.inventory.listings.intf.ListingService.xml";
	
		String second = "C:\\ApiMonitor\\Production\\WADL\\com.stubhub.domain.inventory.war" +
				"\\1.3.10\\com.stubhub.domain.inventory.intf\\1.3.10" +
				"\\com.stubhub.domain.inventory.listings.intf.ListingService" +
				"\\jarfile.com.stubhub.domain.inventory.listings.intf.ListingService.xml";
		DiffLineByLine dl = new DiffLineByLine();
		
		CompareResult cr = new CompareResult();
		dl.diffFilesLineByLine(
				//"C:\\firstClass.xsd","C:\\secondClass.xsd",cr);
				first, second, cr, CompareType.intf);
		System.out.println(cr.getDiffHtml());
	}
	
	@SuppressWarnings("unchecked")
	public void diffFilesLineByLine(String firstFilePath, String secondFilePath, CompareResult cr, CompareType docType){
		
		List<String> original = fileToLines(firstFilePath);
		List<String> revised  = fileToLines(secondFilePath);
		
		CommonCompareResult result = getDiffHtml(original, revised, docType, true);
			
		cr.setDeleteNum(result.getDeleteNum());
		cr.setInsertNum(result.getInsertNum());
		cr.setDiffHtml(result.getDiffHtml());
	}
	
	public CommonCompareResult getDiffHtml(List<String> original, List<String> revised, CompareType docType, boolean isDetailed){
		final Map<String, String> generatedText = new ConcurrentHashMap<String, String>();
		CommonCompareResult result = new CommonCompareResult();
		int index= 0;
		for(String s : original){
			String text = s.replace("&", "&amp;").replace("<", "&lt;")
					.replace(">", "&gt;");
			generatedText.put(String.valueOf(index), text);
			index ++;
		}

		int previousChagnelineCount = 0;
		Patch patch = DiffUtils.diff(original, revised);
		
		int deleteNumber = 0;
		int insertNumber = 0;
		
		if(patch.getDeltas().size() > 0){
			int previousPosition = 0;
			//Generate diff html...			
			for (Delta delta: patch.getDeltas()) {
				logger.info(delta.toString());
				
				int pos = delta.getOriginal().getPosition();
				
				int orilen = delta.getOriginal().getLines().size();
				int revlen = delta.getRevised().getLines().size();
				
				if(previousChagnelineCount > 0){
					if((pos - previousPosition) <= previousChagnelineCount){
						pos = pos + previousChagnelineCount;
					}
				}
				previousPosition = pos;
				if(delta.getType() != TYPE.DELETE){
					extendMapByInsertElement(generatedText, pos - 1, revlen);
				}
				previousChagnelineCount += revlen;
				
				switch(delta.getType()){
				case CHANGE:
				{
					for(int i=0; i<revlen; i++){
						String text = ((String)delta.getRevised().getLines().get(i)).replace("&", "&amp;").replace("<", "&lt;")
								.replace(">", "&gt;");
						generatedText.put(String.valueOf(pos + i), text + "---CHANGE---");
						insertNumber++;
					}
					for(int i=0; i<orilen; i++){
						String text = ((String)delta.getOriginal().getLines().get(i)).replace("&", "&amp;").replace("<", "&lt;")
								.replace(">", "&gt;");
						generatedText.put(String.valueOf(pos + i + revlen), text + "---DELETE---");
						deleteNumber++;
					}
				}
				break;
				case DELETE:
				{
					for(int i=0; i<orilen; i++){
						String text = ((String)delta.getOriginal().getLines().get(i)).replace("&", "&amp;").replace("<", "&lt;")
								.replace(">", "&gt;");
						generatedText.put(String.valueOf(pos + i), text + "---DELETE---");
						deleteNumber++;
					}
				}
				break;
				case INSERT:
				{
					for(int i=0;i<revlen;i++){
						String text = ((String)delta.getRevised().getLines().get(i)).replace("&", "&amp;").replace("<", "&lt;")
								.replace(">", "&gt;");
						generatedText.put(String.valueOf(pos + i ), text + "---INSERT---");
						insertNumber++;
					}
				}
				break;
				
				default:
					break;
				}
			}

			List<String> keys = Arrays.asList(generatedText.keySet().toArray(new String[]{}));
			Collections.sort(keys, new Comparator<String>(){

				@Override
				public int compare(String o1, String o2) {
					int i1 = Integer.valueOf(o1);
					int i2 = Integer.valueOf(o2);
					return i1 - i2;
				}
				
			});
			boolean failedFlag = true;
			if(docType == CompareType.schema){
				int sumchange = insertNumber + deleteNumber;
				int searched = 0;
				if(sumchange > 0){
					for(String s : keys){
						String text = generatedText.get(s);
						if((text.indexOf("---DELETE---") >= 0)||(text.indexOf("---INSERT---") >= 0)){
							if(text.indexOf("minOccurs=\"0\"") > -1){
								searched ++;
							}
							if(text.indexOf("xs:enumeration") > -1){
								searched ++;
							}
						}
					}
				}
				if(searched == sumchange){
					failedFlag = false;
				}else{
					failedFlag = true;
				}
			}			
			
			for(String s : keys){
				String text = generatedText.get(s);
				StringBuilder tmp = new StringBuilder();
				if(text.indexOf("---DELETE---") >= 0){
					text = text.replaceAll("---DELETE---", "");
					if(text.length() == 0){
						tmp.append("<br>");
					}else{
						tmp.append("<del style=\"background:#ffe6e6;\">").append(text).append("</del>").append("<br>");
					}					
					generatedText.remove(s);
					generatedText.put(s, tmp.toString());
				}
				else if(text.indexOf("---INSERT---") >= 0){
					text = text.replaceAll("---INSERT---", "");;
					if(text.length() == 0){
						tmp.append("<br>");
					}else{
						tmp.append("<ins style=\"background:#e6ffe6;\">").append(text).append("</ins>").append("<br>");
					}
					generatedText.remove(s);
					generatedText.put(s, tmp.toString());
				}
				else if(text.indexOf("---CHANGE---") >= 0){
					text = text.replaceAll("---CHANGE---", "");
					if(text.length() == 0){
						tmp.append("<br>");
					}else{
						tmp.append("<ins style=\"background:#e6ffe6;\">").append(text).append("</ins>").append("<br>");
					}
					generatedText.remove(s);
					generatedText.put(s, tmp.toString());
				}
				else{
					if(isDetailed){
						tmp.append(text.replaceAll(" ", "&nbsp;")).append("<br>");
						generatedText.remove(s);
						generatedText.put(s, tmp.toString());
					}
					else{
						generatedText.put(s, "......<br>");
					}
				}
			}
			removeDuplicateElement(generatedText);
			String ret = printMap(generatedText);
			generatedText.clear();
			
			result.setDeleteNum(deleteNumber);
			result.setInsertNum(insertNumber);
			result.setDiffHtml(ret);
			if(failedFlag){
				result.setStatus(CompareStatus.failed);
			}else{
				result.setStatus(CompareStatus.warning);
			}
		}else{
			result.setDeleteNum(0);
			result.setInsertNum(0);
			result.setDiffHtml("");
			result.setStatus(CompareStatus.success);
		}
		return result;
	}
	
	private void extendMapByInsertElement(Map<String, String> extendedMap ,int insertIndex, int insertLen){
		int originalSize = extendedMap.keySet().size();
		String tmp = "";
		for(int i=originalSize+insertLen-1;i>insertIndex;i--){
			int cur = i-insertLen;
			if(cur<=insertIndex){
				tmp = extendedMap.get(String.valueOf(insertIndex));
				extendedMap.put(String.valueOf(i), tmp == null ? "" : tmp);
				continue;
			}
			tmp = extendedMap.get(String.valueOf(cur));
			extendedMap.put(String.valueOf(i), tmp == null ? "" : tmp);
		}
	}
	
	private void removeDuplicateElement(final Map<String, String> generatedText){
		List<String> sortedKeys=new ArrayList<String>(generatedText.keySet());
		Collections.sort(sortedKeys, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				int i1 = Integer.valueOf(o1.split("\\,")[0]);
				int i2 = Integer.valueOf(o2.split("\\,")[0]);
				return i1-i2;
			}
			
		});
		String previousVal = "";
		for(String s : sortedKeys){			
			if(generatedText.get(s).equalsIgnoreCase(previousVal)){
				generatedText.remove(s);
				continue;
			}
			previousVal = generatedText.get(s);
		}		
	}
	
	private String printMap(Map<String, String> generatedText){
		StringBuilder sum = new StringBuilder();
		List<String> sortedKeys=new ArrayList<String>(generatedText.keySet());
		Collections.sort(sortedKeys, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				int i1 = Integer.valueOf(o1.split("\\,")[0]);
				int i2 = Integer.valueOf(o2.split("\\,")[0]);
				return i1-i2;
			}
			
		});
		
		for(String s : sortedKeys){
			sum.append("line "+s+" : "+generatedText.get(s));
			//logger.info("line "+s + " : " +generatedText.get(s));
		}
		return sum.toString();
	}
	
	private static List<String> fileToLines(String filename) {
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	

	public static List<String> getDeltaText(Delta delta) {
        List<String> buffer = new ArrayList<String>();
        if(delta.getOriginal()!=null){
        	for (Object line : delta.getOriginal().getLines()) {
                buffer.add("-" + ((String)line).trim());
            }
        }
        if(delta.getRevised() != null){
        	for (Object line : delta.getRevised().getLines()) {
                buffer.add("+" + ((String)line).trim());
            }
        }       
        return buffer;
    }
}
