package com.conference.main;

import com.conference.ConferenceInput;
import com.conference.ConferenceManager;
import com.conference.exceptions.InvalidTalkException;

public class ConferenceMain {
	public static void main(String[] args) {

		String fileName = "input";
		ConferenceManager conferenceManager = new ConferenceManager(new ConferenceInput());
		try {
			conferenceManager.scheduleConference(fileName);
		} catch (InvalidTalkException ite) {
			ite.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
