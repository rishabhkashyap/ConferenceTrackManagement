package com.conference;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.conference.exceptions.InvalidTalkException;

public class ConferenceInput {

	public List<String> getTalkListFromFile(String fileName) throws InvalidTalkException {
		List<String> talkList = new ArrayList<String>();
		try {
			BufferedReader br = null;
			InputStream fstream = null;

			// Open the file.
			if (fileName == null) {

				fstream = ConferenceInput.class.getResourceAsStream("input");
			} else {
				fstream = new FileInputStream(fileName);
			}

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();
			// Read File Line By Line
			while (strLine != null) {
				talkList.add(strLine);
				strLine = br.readLine();
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		return talkList;
	}

}
