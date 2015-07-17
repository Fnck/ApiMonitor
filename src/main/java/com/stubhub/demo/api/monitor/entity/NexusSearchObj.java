package com.stubhub.demo.api.monitor.entity;

import java.util.List;

public class NexusSearchObj {
	private int totalCount;
	private int from;
	private int count;
	private boolean tooManyResults;
	private boolean collapsed;
	private List<RepoDetails> repoDetails;
	private List<RepoData> data;
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public boolean isTooManyResults() {
		return tooManyResults;
	}
	public void setTooManyResults(boolean tooManyResults) {
		this.tooManyResults = tooManyResults;
	}
	public boolean isCollapsed() {
		return collapsed;
	}
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}
	public List<RepoDetails> getRepoDetails() {
		return repoDetails;
	}
	public void setRepoDetails(List<RepoDetails> repoDetails) {
		this.repoDetails = repoDetails;
	}
	public List<RepoData> getData() {
		return data;
	}
	public void setData(List<RepoData> data) {
		this.data = data;
	}
	
}
