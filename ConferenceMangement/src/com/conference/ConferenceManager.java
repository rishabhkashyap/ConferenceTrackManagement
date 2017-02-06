package com.conference;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.conference.exceptions.InvalidTalkException;
import com.conference.model.Talk;

public class ConferenceManager {
	private ConferenceInput conferenceInput;
	/**
     * Constructor for ConferenceManager.
     */
    public ConferenceManager(ConferenceInput conferenceInput) {
    	this.conferenceInput=conferenceInput;
    }
    
    /**
     * public method to create and schedule conference.
     * @param fileName
     * @throws InvalidTalkException
     */
    public List<List<Talk>> scheduleConference(String fileName) throws Exception
    {
        List<String> talkList = conferenceInput.getTalkListFromFile(fileName);
        return scheduleConference(talkList);
    }
    
    /**
     * public method to create and schedule conference.
     * @param talkList
     * @throws InvalidTalkException
     */
    public List<List<Talk>> scheduleConference(List<String> talkList) throws Exception
    {
        List<Talk> talksList = validateAndCreateTalks(talkList);
        return getScheduleConferenceTrack(talksList);
    }
    
    /**
     * Load talk list from input file.
     * @param fileName
     * @return
     * @throws InvalidTalkException
     */
    
    /**
     * Validate talk list, check the time for talk and initilize Talk Object accordingly.
     * @param talkList
     * @throws Exception
     */
    private List<Talk> validateAndCreateTalks(List<String> talkList) throws Exception
    {
        // If talksList is null throw exception invaid list to schedule.
        if(talkList == null)
            throw new InvalidTalkException("Empty Talk List");
        
        List<Talk> validTalksList = new ArrayList<Talk>();
        int talkCount = -1;
        String minSuffix = "min";
        String lightningSuffix = "lightning";
        
        // Iterate list and validate time.
        for(String talk : talkList)
        {
            int lastSpaceIndex = talk.lastIndexOf(" ");
            // if talk does not have any space, means either title or time is missing.
            if(lastSpaceIndex == -1)
                throw new InvalidTalkException("Invalid talk, " + talk + ". Talk time must be specify.");
            
            String name = talk.substring(0, lastSpaceIndex);
            String timeStr = talk.substring(lastSpaceIndex + 1);
            // If title is missing or blank.
            if(name == null || "".equals(name.trim()))
                throw new InvalidTalkException("Invalid talk name, " + talk);
            // If time is not ended with min or lightning.
            else if(!timeStr.endsWith(minSuffix) && !timeStr.endsWith(lightningSuffix))
                throw new InvalidTalkException("Invalid talk time, " + talk + ". Time must be in min or in lightning");
            
            talkCount++;
            int time = 0;
            // Parse time from the time string .
            try{
                if(timeStr.endsWith(minSuffix)) {
                    time = Integer.parseInt(timeStr.substring(0, timeStr.indexOf(minSuffix)));
                }
                else if(timeStr.endsWith(lightningSuffix)) {
                    String lightningTime = timeStr.substring(0, timeStr.indexOf(lightningSuffix));
                    if("".equals(lightningTime))
                        time = 5;
                    else
                        time = Integer.parseInt(lightningTime) * 5;
                }
            }catch(NumberFormatException nfe) {
                throw new InvalidTalkException("Unbale to parse time " + timeStr + " for talk " + talk);
            }
            
            // Add talk to the valid talk List.
            validTalksList.add(new Talk(talk, name, time));
        }
        
        return validTalksList;
    }
    
    /**
     * Schedule Conference tracks for morning and evening session.
     * @param talksList
     * @throws Exception
     */
    private List<List<Talk>> getScheduleConferenceTrack(List<Talk> talksList) throws Exception
    {
        // Find the total possible days.
        int perDayMinTime = 6 * 60;
        int totalTalksTime = getTotalTalksTime(talksList);
        int totalPossibleDays = totalTalksTime/perDayMinTime;
        
        // Sort the talkList.
        List<Talk> talksListForOperation = new ArrayList<Talk>(); 
        talksListForOperation.addAll(talksList);
        Collections.sort(talksListForOperation);
        
        // Find possible combinations for the morning session.
        List<List<Talk>> combForMornSessions = findPossibleCombSession(talksListForOperation, totalPossibleDays, true);
        
        // Remove all the scheduled talks for morning session, from the operationList.
        for(List<Talk> talkList : combForMornSessions) {
            talksListForOperation.removeAll(talkList);
        }
        
        // Find possible combinations for the evening session.
        List<List<Talk>> combForEveSessions = findPossibleCombSession(talksListForOperation, totalPossibleDays, false);
        
        // Remove all the scheduled talks for evening session, from the operationList.
        for(List<Talk> talkList : combForEveSessions) {
            talksListForOperation.removeAll(talkList);
        }
        
        // check if the operation list is not empty, then try to fill all the remaining talks in evening session. 
        int maxSessionTimeLimit = 240;
        if(!talksListForOperation.isEmpty()) {
            List<Talk> scheduledTalkList = new ArrayList<Talk>();
            for(List<Talk> talkList : combForEveSessions) {
                int totalTime = getTotalTalksTime(talkList);
                
                for(Talk talk : talksListForOperation) {
                    int talkTime = talk.getTimeDuration();
                    
                    if(talkTime + totalTime <= maxSessionTimeLimit) {
                        talkList.add(talk);
                        talk.setScheduled(true);
                        scheduledTalkList.add(talk);
                    }
                }
                
                talksListForOperation.removeAll(scheduledTalkList);
                if(talksListForOperation.isEmpty())
                    break;
            }
        }
        
        // If operation list is still not empty, its mean the conference can not be scheduled with the provided data.
        if(!talksListForOperation.isEmpty())
        {
            throw new Exception("Unable to schedule all task for conferencing.");
        }
        
        // Schedule the day event from morning session and evening session.
        return getScheduledTalksList(combForMornSessions, combForEveSessions);
    }
    
    /**
     * Find possible combination for the session.
     * If morning session then each session must have total time 3 hr.
     * if evening session then each session must have total time greater then 3 hr.
     * @param talksListForOperation
     * @param totalPossibleDays
     * @param morningSession
     * @return
     */
    private List<List<Talk>> findPossibleCombSession(List<Talk> talksListForOperation, int totalPossibleDays, boolean morningSession)
    {
        int minSessionTimeLimit = 180;
        int maxSessionTimeLimit = 240;
        
        if(morningSession)
            maxSessionTimeLimit = minSessionTimeLimit;
        
        int talkListSize = talksListForOperation.size();
        List<List<Talk>> possibleCombinationsOfTalks = new ArrayList<List<Talk>>();
        int possibleCombinationCount = 0;
        
        // Loop to get combination for total possible days.
        // Check one by one from each talk to get possible combination.
        for(int count = 0; count < talkListSize; count++) {
            int startPoint = count;
            int totalTime = 0;
            List<Talk> possibleCombinationList = new ArrayList<Talk>();
            
            // Loop to get possible combination.
            while(startPoint != talkListSize) {
                int currentCount = startPoint;
                startPoint++;
                Talk currentTalk = talksListForOperation.get(currentCount);
                if(currentTalk.isScheduled())
                    continue;
                int talkTime = currentTalk.getTimeDuration();
                // If the current talk time is greater than maxSessionTimeLimit or 
                // sum of the current time and total of talk time added in list  is greater than maxSessionTimeLimit.
                // then continue.
                if(talkTime > maxSessionTimeLimit || talkTime + totalTime > maxSessionTimeLimit) {
                    continue;
                }
                
                possibleCombinationList.add(currentTalk);
                totalTime += talkTime;
                
                // If total time is completed for this session than break this loop.
                if(morningSession) {
                    if(totalTime == maxSessionTimeLimit)
                        break;
                }else if(totalTime >= minSessionTimeLimit)
                    break;
            }
            
            // Valid session time for morning session is equal to maxSessionTimeLimit.
            // Valid session time for evening session is less than or eqaul to maxSessionTimeLimit and greater than or equal to minSessionTimeLimit.
            boolean validSession = false;
            if(morningSession)
                validSession = (totalTime == maxSessionTimeLimit);
            else
                validSession = (totalTime >= minSessionTimeLimit && totalTime <= maxSessionTimeLimit);
            
            // If session is valid than add this session in the possible combination list and set all added talk as scheduled.
            if(validSession) {
                possibleCombinationsOfTalks.add(possibleCombinationList);
                for(Talk talk : possibleCombinationList){
                    talk.setScheduled(true);
                }
                possibleCombinationCount++;
                if(possibleCombinationCount == totalPossibleDays)
                    break;
            }
        }
        
        return possibleCombinationsOfTalks;
    }
    
    /**
     * Print the scheduled talks with the expected text msg.
     * @param combForMornSessions
     * @param combForEveSessions
     */
    private List<List<Talk>> getScheduledTalksList(List<List<Talk>> combForMornSessions, List<List<Talk>> combForEveSessions)
    {
        List<List<Talk>> scheduledTalksList = new ArrayList<List<Talk>>();
        int totalPossibleDays = combForMornSessions.size();
        
        // for loop to schedule event for all days.
        for(int dayCount = 0; dayCount < totalPossibleDays; dayCount++) {
            List<Talk> talkList = new ArrayList<Talk>();
            
            // Create a date and initialize start time 09:00 AM.
            Date date = new Date( );
            SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mma ");
            date.setHours(9);
            date.setMinutes(0);
            date.setSeconds(0);
            
            int trackCount = dayCount + 1;
            String scheduledTime = dateFormat.format(date);
            
            System.out.println("Track " + trackCount + ":");
            
            // Morning Session - set the scheduled time in the talk and get the next time using time duration of current talk.
            List<Talk> mornSessionTalkList = combForMornSessions.get(dayCount);
            for(Talk talk : mornSessionTalkList) {
                talk.setScheduledTime(scheduledTime);
                System.out.println(scheduledTime + talk.getTitle());
                scheduledTime = getNextScheduledTime(date, talk.getTimeDuration());
                talkList.add(talk);
            }
            
            // Scheduled Lunch Time for 60 mins.
            int lunchTimeDuration = 60;
            Talk lunchTalk = new Talk("Lunch", "Lunch", 60);
            lunchTalk.setScheduledTime(scheduledTime);
            talkList.add(lunchTalk);
            System.out.println(scheduledTime + "Lunch");
            
            // Evening Session - set the scheduled time in the talk and get the next time using time duration of current talk.
            scheduledTime = getNextScheduledTime(date, lunchTimeDuration);
            List<Talk> eveSessionTalkList = combForEveSessions.get(dayCount);
            for(Talk talk : eveSessionTalkList) {
                talk.setScheduledTime(scheduledTime);
                talkList.add(talk);
                System.out.println(scheduledTime + talk.getTitle());
                scheduledTime = getNextScheduledTime(date, talk.getTimeDuration());
            }
            
            // Scheduled Networking Event at the end of session, Time duration is just to initialize the Talk object.
            Talk networkingTalk = new Talk("Networking Event", "Networking Event", 60);
            networkingTalk.setScheduledTime(scheduledTime);
            talkList.add(networkingTalk);
            System.out.println(scheduledTime + "Networking Event\n");
            scheduledTalksList.add(talkList);
        }
        
        return scheduledTalksList;
    }
    
    /**
     * To get total time of talks of the given list.
     * @param talksList
     * @return
     */
    public static int getTotalTalksTime(List<Talk> talksList)
    {
        if(talksList == null || talksList.isEmpty())
            return 0;
        
        int totalTime = 0;
        for(Talk talk : talksList) {
            totalTime += talk.getTimeDuration();
        }
        return totalTime;
    }
    
    /**
     * To get next scheduled time in form of String.
     * @param date
     * @param timeDuration
     * @return
     */
    private String getNextScheduledTime(Date date, int timeDuration)
    {
        long timeInLong  = date.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mma ");
        
        long timeDurationInLong = timeDuration * 60 * 1000;
        long newTimeInLong = timeInLong + timeDurationInLong;
        
        date.setTime(newTimeInLong);
        String str = dateFormat.format(date);
        return str;
    }
    
   
   
    
   

}
