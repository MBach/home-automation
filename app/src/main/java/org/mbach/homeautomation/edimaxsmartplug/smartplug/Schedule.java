package org.mbach.homeautomation.edimaxsmartplug.smartplug;

import org.mbach.homeautomation.edimaxsmartplug.entities.ScheduleDay;
import org.mbach.homeautomation.edimaxsmartplug.entities.ScheduleTime;
import org.w3c.dom.Document;

import java.time.LocalTime;

public class Schedule {

	public static ScheduleDay[] createFromDocument(Document document) throws Exception {

		ScheduleDay[] scheduleDays = new ScheduleDay[7];
		
		for(int x = 0; x <= 6; x++) {
			
			String scheduleString = document.getElementsByTagName("Device.System.Power.Schedule."+x+".List").item(0).getTextContent();
			String scheduleStatus = document.getElementsByTagName("Device.System.Power.Schedule."+x).item(0).getAttributes().getNamedItem("value").getTextContent();
			
			scheduleDays[x] = decodeScheduleDay(scheduleString);
			scheduleDays[x].isActive = scheduleStatus.trim().equals("ON");
		}
		return scheduleDays;
	}

	public static ScheduleDay decodeScheduleDay(String scheduleDayStr) throws Exception {

		ScheduleDay scheduleDay = new ScheduleDay();

		if(scheduleDayStr.isEmpty()) {
			return scheduleDay;
		}

		String[] sched_items = scheduleDayStr.split("-");

		for(int i = 0; i < sched_items.length; i++) {

			ScheduleTime scheduleTime = new ScheduleTime();
			String timeString = sched_items[i];
			boolean isActiveTime = timeString.endsWith("1");

			int start_min = EdimaxUtil.numberFromCharCode(Character.codePointAt(timeString, 0)) * 60 + EdimaxUtil.numberFromCharCode(Character.codePointAt(timeString, 1));
			int end_min = EdimaxUtil.numberFromCharCode(Character.codePointAt(timeString, 2)) * 60 + EdimaxUtil.numberFromCharCode(Character.codePointAt(timeString, 3));

			scheduleTime.isActive = isActiveTime;
			//scheduleTime.start = LocalTime.ofSecondOfDay(start_min * 60);
			//scheduleTime.end = LocalTime.ofSecondOfDay(end_min * 60);

			scheduleDay.isActive = false;
			scheduleDay.scheduleTimes.add(scheduleTime);
		}
		return scheduleDay;
	}
}