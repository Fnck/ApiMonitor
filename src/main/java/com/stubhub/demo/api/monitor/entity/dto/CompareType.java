package com.stubhub.demo.api.monitor.entity.dto;

public enum CompareType {
	intf("intf"),
	schema("schema");
	
	private String value;
	CompareType(String input){
		this.value = input;
	}
	
	public String getValue(){
		return this.value;
	}
}
