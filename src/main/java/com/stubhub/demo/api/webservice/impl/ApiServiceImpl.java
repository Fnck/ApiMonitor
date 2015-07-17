package com.stubhub.demo.api.webservice.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.rs.security.cors.CorsHeaderConstants;
import org.apache.cxf.rs.security.cors.LocalPreflight;
import org.apache.geronimo.mail.util.Base64;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.stubhub.demo.api.monitor.DemoClient;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;
import com.stubhub.demo.api.monitor.entity.JarInfo;
import com.stubhub.demo.api.monitor.entity.NexusArtifactObj;
import com.stubhub.demo.api.monitor.entity.PomDependency;
import com.stubhub.demo.api.monitor.entity.WadlGeneratorDescription;
import com.stubhub.demo.api.monitor.entity.dto.CommonCompareDocument;
import com.stubhub.demo.api.monitor.entity.dto.MailRequest;
import com.stubhub.demo.api.monitor.util.HttpUtil;
import com.stubhub.demo.api.monitor.util.JschRoleUtil;
import com.stubhub.demo.api.monitor.util.JschUtil;
import com.stubhub.demo.api.monitor.util.MongoDBUtil;
import com.stubhub.demo.api.monitor.util.MyUserInfo;
import com.stubhub.demo.api.monitor.util.NexusUtil;
import com.stubhub.demo.api.monitor.util.WadlUtil;
import com.stubhub.demo.api.monitor.util.XmlFormatter;
import com.stubhub.demo.api.monitor.util.XsdUtil;
import com.stubhub.demo.api.webservice.impl.component.ApiServiceThread;
import com.stubhub.demo.api.webservice.impl.component.JschThreadForQa;
import com.stubhub.demo.api.webservice.intf.ApiService;

@Component("apiMonitorService")
public class ApiServiceImpl implements ApiService {
	
	private ExecutorService executor;
	
	public ApiServiceImpl(){
		executor = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 100l, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(10, false));
	}

	private static Logger logger = LoggerFactory.getLogger(ApiServiceImpl.class);
	
	@Override
	public Response uploadFile(String warName,String prodVersion,String latestVersion, String role, String isDetailed) throws Exception {
		logger.info("compare {}, {}, {}", warName, prodVersion,latestVersion );
		MongoDBUtil mu = new MongoDBUtil();
		String taskId = mu.creatTask();
		boolean isDetailedFlag = false;
		if(isDetailed!=null){
			isDetailedFlag = Boolean.parseBoolean(isDetailed);
		}
		Runnable myRunnable = createRunnable(warName,prodVersion,latestVersion, taskId, role, isDetailedFlag);
		
		Map<String, String> resp = new HashMap<String, String>();
		resp.put("taskId", taskId);
		Gson jsonUtil = new Gson();
		executor.submit(myRunnable);
		return Response.status(Status.OK).entity("handleResp("+jsonUtil.toJson(resp)+")").build();
	}
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException{
		MailRequest req = new MailRequest();
		req.setMailBody("this is a testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
				"testhahahahahahahahahhahahahahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		req.setMailTitle("test mail");
		req.setPassword("cUAyMTY0Mjg=");
		req.setSender("bqiao@ebay.com");
		req.setToList("bqiao@ebay.com");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				Feature.WRAP_ROOT_VALUE,
				true);
		logger.info(mapper.writeValueAsString(req));
		/*MyUserInfo user = new MyUserInfo();
		user.set_userName("bqiao");
		user.setPassword(new String(Base64.decode("cUAyMTY0Mjg=")));
		String m = JschUtil.readRemoteFile("/nas/home/bqiao/Shape_package_list_.csv", "srwd00reg011.stubcorp.dev", user);
		logger.info(m);
		
		String warName = "com.stubhub.domain.inventory.services.sellereligibility.v1.war";
		String prodVersion = "1.2.18";
		String latestVersion = "1.3.22";
		logger.info("compare {}, {}, {}", warName, prodVersion,latestVersion );
		MongoDBUtil mu = new MongoDBUtil();
		String taskId = mu.creatTask();
		logger.info("taskId: ", taskId);
		ApiServiceImpl api = new ApiServiceImpl();
		Runnable myRunnable = api.createRunnable(warName,prodVersion,latestVersion, taskId, "");
		Thread t = new Thread(myRunnable);
		t.start();*/
	}	

	private Runnable createRunnable(String warName, String prodVersion,
			String latestVersion, String taskId, String role, boolean isDetailed) {
		ApiServiceThread t = new ApiServiceThread(warName, prodVersion, latestVersion, taskId, role, isDetailed);
		return t;
	}
	
	
	public Response getDiffInfo(String taskId) throws Exception {		
		MongoDBUtil mu = new MongoDBUtil();
		Gson jsonUtil = new Gson();
		CommonCompareDocument result = mu.getCompareDocument(taskId);
		if((result == null)||(result.getWarName().isEmpty())){
			Map<String, String> resp = new HashMap<String, String>();
			resp.put("status", "pending data");			
			return Response.status(Status.OK).entity("handleResp("+jsonUtil.toJson(resp)+")").build();
		}
		return Response.status(Status.OK).entity("handleResp("+jsonUtil.toJson(result)+")").build();
	}
/*
	public Response options() {
		return Response.ok()
                           .header(CorsHeaderConstants.HEADER_AC_ALLOW_METHODS, "DELETE, PUT, POST, GET, OPTIONS")
                           .header(CorsHeaderConstants.HEADER_AC_ALLOW_HEADERS, "content-type")
                           .header(CorsHeaderConstants.HEADER_AC_ALLOW_ORIGIN, "*")
                           .build();
	}*/

	public Response syncProdShapeList() throws UnknownHostException {
		MyUserInfo user = new MyUserInfo();
		user.set_userName("bqiao");
		user.setPassword(new String(Base64.decode("bmVkQDIxNjQyOA==")));
		String m = JschUtil.readRemoteFile("/nas/home/bqiao/Shape_package_list_.csv", "srwd00reg011.stubcorp.dev", user);
		List<String> roleList = Arrays.asList(m.split("\\r?\\n"));
		logger.info("document has {} line",m.split("\\r?\\n").length);
		List<ArtifactInfo> artifactInProd = new ArrayList<ArtifactInfo>();
		for(String s : roleList){
			String[] str = s.split(",");
			String war = str[1];
			ArtifactInfo warAndVer = JschUtil.getArtifactByWarString(war);
			warAndVer.setRole(str[0].substring(2, 5).toUpperCase());
			warAndVer.setPool(str[0].substring(0, 2));
			if(warAndVer.getArtifactName().indexOf("com.stubhub.domain") > -1){
				artifactInProd.add(warAndVer);
			}
		}
		MongoDBUtil mu = new MongoDBUtil();
		mu.updateProductionArtifactList(artifactInProd);
		
		JschThreadForQa syncThread = new JschThreadForQa();
		
		executor.submit(syncThread);

		return Response.status(Status.OK).entity(artifactInProd.size()).build();
	}

	@Override
	@POST
	@Path("/sendMail")
	public Response sendMail(MailRequest request) throws Exception {
		String command = " echo \"" + request.getMailBody() + "\"|mail -s \""+ request.getMailTitle() +
				"\nContent-Type: text/html\" " + request.getToList() + " -c " + request.getSender();
		MyUserInfo user = new MyUserInfo();
		user.set_userName(request.getSender().split("@")[0]);
		user.setPassword(request.getPassword());
		JschRoleUtil.runCommandInRemoteMachine("srwd00reg003.stubcorp.dev", user, command);
		return Response.status(Status.OK).build();
	}
}
