package com.stubhub.demo.api.monitor.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XsdUtil {
	private static Logger logger = LoggerFactory.getLogger(XsdUtil.class);;
	
	public static void main(String[] args) {
		XsdUtil x = new XsdUtil();
		
		//x.convertClassToXmlStr(GetSRSForBarcodesResponse.class);
	}
	
	public String convertClassToXmlStr(final Class clazz){
		JAXBContext context;
		final Set<String> files = new HashSet<String>();
		try {
			//Class c = this.getClass().getClassLoader().loadClass(clazz.getName());
			Class c = Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());
			logger.info("{} was loaded by {}!",c.getName(), Thread.currentThread().getContextClassLoader());
			
			context = JAXBContext.newInstance(clazz);
			
			Unmarshaller u = context.createUnmarshaller();

		    // generate the schemas
		    final List<ByteArrayOutputStream> schemaStreams = new ArrayList<ByteArrayOutputStream>();
		    context.generateSchema(new SchemaOutputResolver(){
		        @Override
		        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
		            ByteArrayOutputStream out = new ByteArrayOutputStream();
		            schemaStreams.add(out);
		            StreamResult streamResult = new StreamResult(out);
		            streamResult.setSystemId("");
		            return streamResult;
		        }});

		    // convert to a list of string
		    List<String> schemas = new ArrayList<String>();
		    for( ByteArrayOutputStream os : schemaStreams )
		    {
		        schemas.add(os.toString());
		        //System.out.println( os.toString());
		    }
		    
			StringBuilder tmpStr = new StringBuilder();
			for(String s : schemas){
				tmpStr.append(s);
			}
			XmlFormatter xf = new XmlFormatter();
			String formattedXml = xf.format(tmpStr.toString());
			//logger.info(formattedXml);
			return formattedXml;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public List<String> fileToLines(String tmpStr) {
		List<String> lines = new LinkedList<String>();
		String[] mm = tmpStr.split("\\r?\\n");
		for(String s : mm){
			lines.add(s);
		}
		return lines;
	}
}
