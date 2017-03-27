package com.conference.main;

import java.util.Scanner;

import com.conference.ConferenceInput;
import com.conference.ConferenceManager;
import com.conference.exceptions.InvalidTalkException;
import com.conference.model.Conference;

public class ConferenceMain {
public static void main(String[] args) {
		

		String fileName = null;
		System.out.println("Please enter file path = ");

		Scanner scanner=new Scanner(System.in);
		fileName=scanner.nextLine();		
		if(fileName.length()>0){
			fileName=fileName.replaceAll("\\\\", "/");
			
		}else{
			System.out.println("Data will be read from default file ");
			fileName=null;
		}
				
		ConferenceManager conferenceManager = new ConferenceManager(new ConferenceInput(),new Conference(240,180));
		try {
			conferenceManager.scheduleConference(fileName);
		} catch (InvalidTalkException ite) {
			ite.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
