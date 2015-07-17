package com.stubhub.demo.api.monitor.entity.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.sun.jersey.core.util.Base64;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="mailRequest")
@JsonRootName(value="mailRequest")
@XmlType(name="", propOrder={"mailTile", "mailBody", "sender", "toList", "password"})
public class MailRequest {
	@XmlElement(name = "mailTitle", required = true)
	private String mailTitle;
	
	@XmlElement(name = "mailBody", required = true)
	private String mailBody;
	//private String contentType;
	
	@XmlElement(name = "sender", required = true)
	private String sender;
	
	@XmlElement(name = "toList", required = true)
	private String toList;
	
	@XmlElement(name = "password", required = true)
	private String password;
	
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = new String(Base64.decode(password));
	}
	public String getMailTitle() {
		return mailTitle;
	}
	public void setMailTitle(String mailTitle) {
		this.mailTitle = mailTitle;
	}
	public String getMailBody() {
		return mailBody;
	}
	public void setMailBody(String mailBody) {
		this.mailBody = mailBody;
	}
	/*
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}*/
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getToList() {
		return toList;
	}
	public void setToList(String toList) {
		this.toList = toList;
	}
	
}
