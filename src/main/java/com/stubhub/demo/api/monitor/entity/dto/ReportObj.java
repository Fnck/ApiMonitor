package com.stubhub.demo.api.monitor.entity.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ReportObj {
	private static Logger logger = LoggerFactory.getLogger(ReportObj.class);
	
	private String createdDate;
	private int totalExecuted;
	private int totalPassed;
	private int totalFailed;
	private String failedRoles;
	
	
	public String getFailedRoles() {
		return failedRoles;
	}
	public void setFailedRoles(String failedRoles) {
		this.failedRoles = failedRoles;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	public int getTotalExecuted() {
		return totalExecuted;
	}
	public void setTotalExecuted(int totalExecuted) {
		this.totalExecuted = totalExecuted;
	}
	public int getTotalPassed() {
		return totalPassed;
	}
	public void setTotalPassed(int totalPassed) {
		this.totalPassed = totalPassed;
	}
	public int getTotalFailed() {
		return totalFailed;
	}
	public void setTotalFailed(int totalFailed) {
		this.totalFailed = totalFailed;
	}
	
	
	public static void main(String[] args){
		ReportObj o = new ReportObj();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-ss-dd hh:MM:ss");
		Date d = new Date();
		
		o.setCreatedDate(sdf.format(d));
		o.setFailedRoles("API, MYX, BRX");
		o.setTotalExecuted(33);
		o.setTotalFailed(12);
		o.setTotalPassed(153);
		
		Gson g = new Gson();
		logger.info(g.toJson(o));
		
	}
	
}
