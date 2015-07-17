package com.stubhub.demo.api.monitor.entity.dto;

public class CommonCompareResult {
	private int deleteNum;
	private int insertNum;
	
	private String diffHtml;
	private String status;
	
	private String objectName;
	private CompareType objectType;
	
	
	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public CompareType getObjectType() {
		return objectType;
	}

	public void setObjectType(CompareType objectType) {
		this.objectType = objectType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getDeleteNum() {
		return deleteNum;
	}

	public void setDeleteNum(int deleteNum) {
		this.deleteNum = deleteNum;
	}

	public int getInsertNum() {
		return insertNum;
	}

	public void setInsertNum(int insertNum) {
		this.insertNum = insertNum;
	}

	public String getDiffHtml() {
		return diffHtml;
	}

	public void setDiffHtml(String diffHtml) {
		this.diffHtml = diffHtml;
	}

}
