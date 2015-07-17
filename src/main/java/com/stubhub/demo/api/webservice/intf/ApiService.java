package com.stubhub.demo.api.webservice.intf;

import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.rs.security.cors.LocalPreflight;

import com.stubhub.demo.api.monitor.entity.dto.MailRequest;

@Path("/api")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface ApiService {

		
		@GET
		@Path("/checkDiff/{warName}/productionVersion/{prodVersion}/latestVersion/{latestVersion}")
		public Response uploadFile(@PathParam("warName") String warName, 
				@PathParam("prodVersion") String prodVersion,
				@PathParam("latestVersion") String latestVersion,
				@QueryParam("role") String role, @QueryParam("detailed") String isDetailed) throws Exception;
		
		@GET
		@Path("/getDiff/{taskId}")
		public Response getDiffInfo(@PathParam("taskId") String taskId) throws Exception;
		
		@GET
		@Path("/syncShapeList")
		public Response syncProdShapeList() throws UnknownHostException;
		
		@POST
		@Path("/sendMail")
		public Response sendMail(MailRequest request) throws Exception;
		
		/*
		@OPTIONS
	    @Path("/checkDiff/{warName}/productionVersion/{prodVersion}/latestVersion/{latestVersion}")
	    @LocalPreflight
	    public Response options();*/
}
