package com.conference.model;

import java.util.ArrayList;
import java.util.List;

public class Conference {
	private List<List<Talk>> morningTalkList;
	private List<List<Talk>> eveningTalkList;
	private int maxSessionTime;
	private int minSessionTime;

	public Conference(int maxSessionTime, int minSessionTime) {
		this.morningTalkList = new ArrayList<>();
		this.eveningTalkList = new ArrayList<>();
		this.maxSessionTime = maxSessionTime;
		this.minSessionTime = minSessionTime;
	}

	public List<List<Talk>> getMorningTalkList() {
		return morningTalkList;
	}

	public void setMorningTalkList(List<List<Talk>> morningTalkList) {
		this.morningTalkList = morningTalkList;
	}

	public List<List<Talk>> getEveningTalkList() {
		return eveningTalkList;
	}

	public void setEveningTalkList(List<List<Talk>> eveningTalkList) {
		this.eveningTalkList = eveningTalkList;
	}

	public int getMaxSessionTime() {
		return maxSessionTime;
	}

	public void setMaxSessionTime(int maxSessionTime) {
		this.maxSessionTime = maxSessionTime;
	}

	public int getMinSessionTime() {
		return minSessionTime;
	}

	public void setMinSessionTime(int minSessionTime) {
		this.minSessionTime = minSessionTime;
	}
	
	

}
