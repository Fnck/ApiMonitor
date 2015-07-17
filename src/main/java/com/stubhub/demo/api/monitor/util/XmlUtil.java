package com.stubhub.demo.api.monitor.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class XmlUtil {
	
	final static Logger logger = LoggerFactory.getLogger(XmlUtil.class);

	public static String objToXmlStr(Object input, Class inputClass) throws Exception{
		JAXBContext context = JAXBContext.newInstance(inputClass);
		Marshaller marsh = context.createMarshaller();
		marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		StringWriter sw = new StringWriter();
		StringBuffer out = new StringBuffer();
		XMLOutputFactory output = XMLOutputFactory.newFactory();
		XMLStreamWriter xmlwr = output.createXMLStreamWriter(sw);
		
		marsh.marshal(input, xmlwr);
		
		out.append(sw);
		XmlFormatter xf = new XmlFormatter();
		String xmlStr = xf.format(out.toString());
		return xmlStr;
	}

	public static Object xmlStrToObj(String inputStr, Class inputClass) throws Exception{
		byte[] byteArray = inputStr.getBytes();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
		XMLInputFactory input = XMLInputFactory.newFactory();
		XMLStreamReader reader = input.createXMLStreamReader(byteStream);
		
		JAXBContext context = JAXBContext.newInstance(inputClass);
		Unmarshaller unmarsh = context.createUnmarshaller();
		Object result = unmarsh.unmarshal(reader);
				
		return result;
	}
	
	public static void main(String[] args){
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String ret = sdf.format(d);
		logger.info(ret);
		logger.debug("Hello world debugging!");
	}
}