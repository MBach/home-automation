package org.mbach.homeautomation.edimaxsmartplug.smartplug;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
Special Strings:

SYSTEM_INFO
NOW_POWER
POWER_USAGE
POWER_HISTORY
SCHEDULE

Not so special strings:

Device.System.Time
Device.System.Power.State
Device.System.Power.NextToggle

Only write:

Device.System.Password.Password
*/
final class RequestTemplates {

	private static final String TIME_UNIT_HOUR = "HOUR";
	private static final String TIME_UNIT_DAY = "DAY";
	private static final String TIME_UNIT_MONTH = "MONTH";

	private static final String all = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='get'>\n"
			+ "        <SYSTEM_INFO></SYSTEM_INFO>\n"
			+ "        <NOW_POWER></NOW_POWER>\n"
			+ "        <SCHEDULE></SCHEDULE>\n"
			+ "        <POWER_USAGE></POWER_USAGE>\n"
			+ "        <Device.System.Time></Device.System.Time>\n"
			+ "        <Device.System.Power.State></Device.System.Power.State>\n"
			+ "        <Device.System.Power.NextToggle></Device.System.Power.NextToggle>\n"
			+ "    </CMD>\n"
			+ "</SMARTPLUG>\n";

	static String getAll() {
		return all;
	}

	/*
<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
    <CMD id="setup">FAILED</CMD>
</SMARTPLUG>
	 */
	private static final String switchOn = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='setup'>\n"
			+ "        <Device.System.Power.State>ON</Device.System.Power.State>\n"
			+ "    </CMD>\n"
			+ "</SMARTPLUG>\n";

	static String getSwitchOn() {
		return switchOn;
	}

	private static final String switchOff = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='setup'>\n"
			+ "        <!--<Device.System.Power.State>ON</Device.System.Power.State>-->\n"
			+ "        <Device.System.Power.State>OFF</Device.System.Power.State>\n"
			+ "    </CMD>\n"
			+ "</SMARTPLUG>\n";

	static String getSwitchOff() {
		return switchOff;
	}

	/*
<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax">
    <CMD id="get">
        <Device.System.Power.State>ON</Device.System.Power.State>
    </CMD>
</SMARTPLUG>
	 */
	private static final String getStatus = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='get'>\n"
			+ "        <Device.System.Power.State></Device.System.Power.State>\n"
			+ "    </CMD>\n"
			+ "</SMARTPLUG>\n";

	static String getGetStatus() {
		return getStatus;
	}
	/*
<?xml version="1.0" encoding="UTF-8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<POWER_HISTORY>
			<Device.System.Power.History.Energy unit="HOUR" date="2015122100-2015123020">=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-a-h-C-N-X-1k-1D-0-=-=-=-=-=-=-=-=-=-0-0-0-0-=-0-0-0-4-u-S</Device.System.Power.History.Energy>
		</POWER_HISTORY>
	</CMD>
</SMARTPLUG>
	 */
	private static String getHistory = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='get'>\n"
			+ "        <POWER_HISTORY>\n"
			+ "            <Device.System.Power.History.Energy unit='%s' date='%s' />\n"
			+ "        </POWER_HISTORY>\n"
			+ "    </CMD>\n"
			+ "</SMARTPLUG>\n";

	static String getGetHistory(SmartPlug.TimeUnit timeUnit, LocalDateTime from, LocalDateTime to) {

		String timeUnitString = RequestTemplates.TIME_UNIT_MONTH;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

		switch(timeUnit) {
		case HOUR: 
			timeUnitString = RequestTemplates.TIME_UNIT_HOUR;
			formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
			break;

		case DAY: 
			timeUnitString = RequestTemplates.TIME_UNIT_DAY;
			formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			break;

		case MONTH:
		default:
			timeUnitString = RequestTemplates.TIME_UNIT_MONTH;
			formatter = DateTimeFormatter.ofPattern("yyyyMM");
			break;
		}

		String fromString = from.format(formatter);

		if(to == null) {
			return String.format(RequestTemplates.getHistory, timeUnitString, fromString);
		} else {
			String toString = to.format(formatter);
			return String.format(RequestTemplates.getHistory, timeUnitString, fromString + "-" + toString);
		}
	}

	/*
<?xml version="1.0" encoding="UTF-8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<NOW_POWER>
			<Device.System.Power.LastToggleTime>20151230192542</Device.System.Power.LastToggleTime>
			<Device.System.Power.NowCurrent>0.1184</Device.System.Power.NowCurrent>
			<Device.System.Power.NowPower>25.53</Device.System.Power.NowPower>
			<Device.System.Power.NowEnergy.Day>0.017</Device.System.Power.NowEnergy.Day>
			<Device.System.Power.NowEnergy.Week>0.120</Device.System.Power.NowEnergy.Week>
			<Device.System.Power.NowEnergy.Month>0.120</Device.System.Power.NowEnergy.Month>
		</NOW_POWER>
	</CMD>
</SMARTPLUG>
	 */
	private static final String getPowerInfo = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='get'>\n"
			+ "        <NOW_POWER></NOW_POWER>\n"
			+ "    </CMD>\n"
			+"</SMARTPLUG>\n";

	static String getGetPowerInfo() {
		return getPowerInfo;
	}
	/*
<?xml version="1.0" encoding="UTF-8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<SYSTEM_INFO>
			<SUPPORT>
				<Device.System.SMTP.Support>1</Device.System.SMTP.Support>
				<Device.System.Power.Schedule.Support>1</Device.System.Power.Schedule.Support>
				<Device.System.FwUpgrade.Support>1</Device.System.FwUpgrade.Support>
			</SUPPORT>
			<Run.Cus>Edimax</Run.Cus>
			<Run.Model>SP2101W</Run.Model>
			<Run.FW.Version>1.03</Run.FW.Version>
			<Run.LAN.Client.MAC.Address>74DA3822CC0B</Run.LAN.Client.MAC.Address>
			<Device.System.SMTP.0.Server.Address />
			<Device.System.SMTP.0.Server.Port />
			<Device.System.SMTP.0.Server.Certificate />
			<Device.System.SMTP.0.Server.Authorization.Enable />
			<Device.System.SMTP.0.Mail.Sender />
			<Device.System.SMTP.0.Mail.Recipient />
			<Device.System.SMTP.0.Mail.Action.Notify.Enable />
			<Device.System.SMTP.0.Server.Authorization.Name />
			<Device.System.SMTP.0.Server.Authorization.Password />
			<Device.System.TimeZone.Zone>23</Device.System.TimeZone.Zone>
			<Device.System.TimeZone.Server.Address.0>pool.ntp.org</Device.System.TimeZone.Server.Address.0>
			<Device.System.TimeZone.Server.Address.1>europe.pool.ntp.org</Device.System.TimeZone.Server.Address.1>
			<Device.System.TimeZone.Server.Address.2>oceania.pool.ntp.org</Device.System.TimeZone.Server.Address.2>
			<Device.System.TimeZone.Server.Address.3>north-america.pool.ntp.org</Device.System.TimeZone.Server.Address.3>
			<Device.System.TimeZone.Server.Address.4>south-america.pool.ntp.org</Device.System.TimeZone.Server.Address.4>
			<Device.System.TimeZone.Daylight.Enable>OFF</Device.System.TimeZone.Daylight.Enable>
			<Device.System.Name>Schreibtisch</Device.System.Name>
		</SYSTEM_INFO>
	</CMD>
</SMARTPLUG>
	 */
	private static final String getSystemInfo = "<?xml version='1.0' encoding='UTF8'?>\n"
			+ "<SMARTPLUG id='edimax'>\n"
			+ "    <CMD id='get'>\n"
			+ "        <SYSTEM_INFO></SYSTEM_INFO>\n"
			+ "    </CMD>\n"
			+"</SMARTPLUG>\n";

	static String getGetSystemInfo() {
		return getSystemInfo;
	}

	/*
<?xml version="1.0" encoding="UTF-8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">FAILED</CMD>
</SMARTPLUG>

<?xml version="1.0" encoding="UTF-8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">OK</CMD>
</SMARTPLUG>
	 */
	private static String setName = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<SMARTPLUG id='edimax'>"
			+ "    <CMD id='setup'>"
			+ "        <SYSTEM_INFO>"
			+ "            <Device.System.Name>%s</Device.System.Name>"
			+ "        </SYSTEM_INFO>"
			+ "    </CMD>"
			+ "</SMARTPLUG>";

	static String getSetName(String name) {
		return String.format(RequestTemplates.setName, name);
	}

	/*
<?xml version="1.0" encoding="UTF-8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<Device.System.Time>20151230202311</Device.System.Time>
	</CMD>
</SMARTPLUG>
	 */

	private static final String getSystemTime = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<SMARTPLUG id='edimax'>"
			+ "    <CMD id='get'>"
			+ "        <Device.System.Time />"
			+ "    </CMD>"
			+ "</SMARTPLUG>";

	static String getGetSystemTime() {
		return getSystemTime;
	}

	/*
Response to get schedule

ON:
Monday from 14:19 to 15:18

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
<CMD id="get">
	<SCHEDULE>
		<Device.System.Power.Schedule.0.List></Device.System.Power.Schedule.0.List>
		<Device.System.Power.Schedule.1.List>ejfi1</Device.System.Power.Schedule.1.List>
		<Device.System.Power.Schedule.2.List></Device.System.Power.Schedule.2.List>
		<Device.System.Power.Schedule.3.List></Device.System.Power.Schedule.3.List>
		<Device.System.Power.Schedule.4.List></Device.System.Power.Schedule.4.List>
		<Device.System.Power.Schedule.5.List></Device.System.Power.Schedule.5.List>
		<Device.System.Power.Schedule.6.List></Device.System.Power.Schedule.6.List>
		<Device.System.Power.Schedule.0 value="OFF">000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.0>
		<Device.System.Power.Schedule.1 value="ON">00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001FFFFFFFFFFFFFFC0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.1>
		<Device.System.Power.Schedule.2 value="OFF">000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.2>
		<Device.System.Power.Schedule.3 value="OFF">000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.3>
		<Device.System.Power.Schedule.4 value="OFF">000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.4>
		<Device.System.Power.Schedule.5 value="OFF">000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.5>
		<Device.System.Power.Schedule.6 value="OFF">000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.6>
	</SCHEDULE>
</CMD>
</SMARTPLUG>
	 */
	private static final String getSchedule = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<SMARTPLUG id='edimax'>"
			+ "    <CMD id='get'>"
			+ "        <SCHEDULE />"
			+ "    </CMD>"
			+ "</SMARTPLUG>";

	static String getGetSchedule() {
		return getSchedule;
	}

	/*

Turn on Tuesday (Day 2)

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<SCHEDULE><Device.System.Power.Schedule.2 value="ON"></Device.System.Power.Schedule.2></SCHEDULE>
	</CMD>
</SMARTPLUG>
	 */

	private static final String getTurnScheduleDayOn = "<?xml version=\"1.0\" encoding=\"UTF8\"?>" +
			"<SMARTPLUG id=\"edimax\">\n" + 
			"	<CMD id=\"setup\">\n" + 
			"		<SCHEDULE><Device.System.Power.Schedule.2 value=\"ON\"></Device.System.Power.Schedule.2></SCHEDULE>\n" + 
			"	</CMD>\n" + 
			"</SMARTPLUG>";

	public static String getGetTurnScheduleDayOn() {
		return getTurnScheduleDayOn;
	}

	/*

Create new Schedule on Tuesday from 11:56 to 12:56

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<SCHEDULE><Device.System.Power.Schedule.2>00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFF0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.2></SCHEDULE>
	</CMD>
</SMARTPLUG>
	 */

	/*

Delete Schedule (On Tuesday from 11:56 to 12:56)

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
<CMD id="setup">
<SCHEDULE><Device.System.Power.Schedule.2>000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000</Device.System.Power.Schedule.2></SCHEDULE>
</CMD>
</SMARTPLUG>

	 */

	/*

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<SCHEDULE><Device.System.Power.Schedule.2.List></Device.System.Power.Schedule.2.List></SCHEDULE>
	</CMD>
</SMARTPLUG>

Response:

<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax">
<CMD id="setup">OK</CMD>
</SMARTPLUG>

	 */

/*

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<POWER_USAGE></POWER_USAGE>
	</CMD>
</SMARTPLUG>

Response:

<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax">
	<CMD id="get">
		<POWER_USAGE>
			<Device.System.Power.Budget.Day.UpperLimit>400.000</Device.System.Power.Budget.Day.UpperLimit>
			<Device.System.Power.Budget.Day.Enable>OFF</Device.System.Power.Budget.Day.Enable>
			<Device.System.Power.Budget.Day.Notify>OFF</Device.System.Power.Budget.Day.Notify>
			
			<Device.System.Power.Budget.Week.UpperLimit>40.000</Device.System.Power.Budget.Week.UpperLimit>
			<Device.System.Power.Budget.Week.Enable>OFF</Device.System.Power.Budget.Week.Enable>
			<Device.System.Power.Budget.Week.Notify>OFF</Device.System.Power.Budget.Week.Notify>
			
			<Device.System.Power.Budget.Month.UpperLimit>40.000</Device.System.Power.Budget.Month.UpperLimit>
			<Device.System.Power.Budget.Month.Enable>OFF</Device.System.Power.Budget.Month.Enable>
			<Device.System.Power.Budget.Month.Notify>OFF</Device.System.Power.Budget.Month.Notify>
			
			<Device.System.Power.Budget.UnitPrice>10000</Device.System.Power.Budget.UnitPrice>
			<Device.System.Power.OverPower.UpperLimit>3680</Device.System.Power.OverPower.UpperLimit>
			<Device.System.Power.OverCurrent.UpperLimit>16</Device.System.Power.OverCurrent.UpperLimit>
			<Device.System.Power.Toggle.Notify>OFF</Device.System.Power.Toggle.Notify>
			
			<Device.System.Power.Report.Energy.Day.Notify>OFF</Device.System.Power.Report.Energy.Day.Notify>
			<Device.System.Power.Report.Energy.Week.Notify>OFF</Device.System.Power.Report.Energy.Week.Notify>
			<Device.System.Power.Report.Energy.Month.Notify>OFF</Device.System.Power.Report.Energy.Month.Notify>
			
			<CountryCode>EU</CountryCode>
		</POWER_USAGE>
	</CMD>
</SMARTPLUG>
 */

	private static final String getPowerUsage = "<?xml version=\"1.0\" encoding=\"UTF8\"?>\n" +
			"<SMARTPLUG id=\"edimax\">\n" + 
			"	<CMD id=\"get\">\n" + 
			"		<POWER_USAGE></POWER_USAGE>\n" + 
			"	</CMD>\n" + 
			"</SMARTPLUG>";

	static String getPowerUsage() {
		return getPowerUsage;
	}
	
	/*
Change password

Password MTIzNA== is 1234 base64 encoded

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<Device.System.Password.Password>MTIzNA==</Device.System.Password.Password>
	</CMD>
</SMARTPLUG>

	 */

	/*

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<POWER_USAGE></POWER_USAGE>
	</CMD>
</SMARTPLUG>

RESPONSE

<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax">
<CMD id="get">
	<POWER_USAGE>
		<Device.System.Power.Budget.Day.UpperLimit>0</Device.System.Power.Budget.Day.UpperLimit>
		<Device.System.Power.Budget.Day.Enable>OFF</Device.System.Power.Budget.Day.Enable>
		<Device.System.Power.Budget.Day.Notify>OFF</Device.System.Power.Budget.Day.Notify>
		<Device.System.Power.Budget.Week.UpperLimit>0</Device.System.Power.Budget.Week.UpperLimit>
		<Device.System.Power.Budget.Week.Enable>OFF</Device.System.Power.Budget.Week.Enable>
		<Device.System.Power.Budget.Week.Notify>OFF</Device.System.Power.Budget.Week.Notify>
		<Device.System.Power.Budget.Month.UpperLimit>0</Device.System.Power.Budget.Month.UpperLimit>
		<Device.System.Power.Budget.Month.Enable>OFF</Device.System.Power.Budget.Month.Enable>
		<Device.System.Power.Budget.Month.Notify>OFF</Device.System.Power.Budget.Month.Notify>
		<Device.System.Power.Budget.UnitPrice>10000</Device.System.Power.Budget.UnitPrice>
		<Device.System.Power.OverPower.UpperLimit>3680</Device.System.Power.OverPower.UpperLimit>
		<Device.System.Power.OverCurrent.UpperLimit>16</Device.System.Power.OverCurrent.UpperLimit>
		<Device.System.Power.Toggle.Notify>OFF</Device.System.Power.Toggle.Notify>
		<Device.System.Power.Report.Energy.Day.Notify>OFF</Device.System.Power.Report.Energy.Day.Notify>
		<Device.System.Power.Report.Energy.Week.Notify>OFF</Device.System.Power.Report.Energy.Week.Notify>
		<Device.System.Power.Report.Energy.Month.Notify>OFF</Device.System.Power.Report.Energy.Month.Notify>
		<CountryCode>EU</CountryCode>
	</POWER_USAGE>
</CMD>
</SMARTPLUG>
	 */

	/*

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="get">
		<SYSTEM_INFO></SYSTEM_INFO>
		<Device.System.Time></Device.System.Time>
		<Device.System.Power.State></Device.System.Power.State>
		<Device.System.Power.NextToggle></Device.System.Power.NextToggle>
	</CMD>
</SMARTPLUG>

RESPONSE

<?xml version="1.0" encoding="UTF8"?><SMARTPLUG id="edimax">
	<CMD id="get">
		<SYSTEM_INFO>
			<SUPPORT>
				<Device.System.SMTP.Support>1</Device.System.SMTP.Support>
				<Device.System.Power.Schedule.Support>1</Device.System.Power.Schedule.Support>
				<Device.System.FwUpgrade.Support>1</Device.System.FwUpgrade.Support>
			</SUPPORT>
			<Run.Cus>Edimax</Run.Cus>
			<Run.Model>SP2101W</Run.Model>
			<Run.FW.Version>1.03</Run.FW.Version>
			<Run.LAN.Client.MAC.Address>74DA3822CC0B</Run.LAN.Client.MAC.Address>
			<Device.System.SMTP.0.Server.Address></Device.System.SMTP.0.Server.Address>
			<Device.System.SMTP.0.Server.Port></Device.System.SMTP.0.Server.Port>
			<Device.System.SMTP.0.Server.Certificate></Device.System.SMTP.0.Server.Certificate>
			<Device.System.SMTP.0.Server.Authorization.Enable></Device.System.SMTP.0.Server.Authorization.Enable>
			<Device.System.SMTP.0.Mail.Sender></Device.System.SMTP.0.Mail.Sender>
			<Device.System.SMTP.0.Mail.Recipient></Device.System.SMTP.0.Mail.Recipient>
			<Device.System.SMTP.0.Mail.Action.Notify.Enable></Device.System.SMTP.0.Mail.Action.Notify.Enable>
			<Device.System.SMTP.0.Server.Authorization.Name></Device.System.SMTP.0.Server.Authorization.Name>
			<Device.System.SMTP.0.Server.Authorization.Password></Device.System.SMTP.0.Server.Authorization.Password>
			<Device.System.TimeZone.Zone>28</Device.System.TimeZone.Zone>
			<Device.System.TimeZone.Server.Address.0>pool.ntp.org</Device.System.TimeZone.Server.Address.0>
			<Device.System.TimeZone.Server.Address.1>europe.pool.ntp.org</Device.System.TimeZone.Server.Address.1>
			<Device.System.TimeZone.Server.Address.2>oceania.pool.ntp.org</Device.System.TimeZone.Server.Address.2>
			<Device.System.TimeZone.Server.Address.3>north-america.pool.ntp.org</Device.System.TimeZone.Server.Address.3>
			<Device.System.TimeZone.Server.Address.4>south-america.pool.ntp.org</Device.System.TimeZone.Server.Address.4>
			<Device.System.TimeZone.Daylight.Enable>ON</Device.System.TimeZone.Daylight.Enable>
			<Device.System.Name>Schreibtisch</Device.System.Name>
		</SYSTEM_INFO>
		<Device.System.Time>20160416152035</Device.System.Time>
		<Device.System.Power.State>OFF</Device.System.Power.State>
		<Device.System.Power.NextToggle>-1</Device.System.Power.NextToggle>
	</CMD>
</SMARTPLUG>
	 */

	/*

Set Timezone

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<SYSTEM_INFO>
			<Device.System.TimeZone.Zone>23</Device.System.TimeZone.Zone>
		</SYSTEM_INFO>
	</CMD>
</SMARTPLUG>
	 */

	/*

Sommerzeit aus

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<SYSTEM_INFO>
			<Device.System.TimeZone.Daylight.Enable>OFF</Device.System.TimeZone.Daylight.Enable>
		</SYSTEM_INFO>
	</CMD>
</SMARTPLUG>
	 */


	/*

Set Power Usage

<?xml version="1.0" encoding="UTF8"?>
<SMARTPLUG id="edimax">
	<CMD id="setup">
		<POWER_USAGE>
			<Device.System.Power.Budget.Day.UpperLimit>40.000</Device.System.Power.Budget.Day.UpperLimit>
			<Device.System.Power.Budget.Day.Enable>ON</Device.System.Power.Budget.Day.Enable>
			<Device.System.Power.Budget.Day.Notify>OFF</Device.System.Power.Budget.Day.Notify>
			<Device.System.Power.Budget.Week.UpperLimit>0.000</Device.System.Power.Budget.Week.UpperLimit>
			<Device.System.Power.Budget.Week.Enable>OFF</Device.System.Power.Budget.Week.Enable>
			<Device.System.Power.Budget.Week.Notify>OFF</Device.System.Power.Budget.Week.Notify>
			<Device.System.Power.Budget.Month.UpperLimit>0.000</Device.System.Power.Budget.Month.UpperLimit>
			<Device.System.Power.Budget.Month.Enable>OFF</Device.System.Power.Budget.Month.Enable>
			<Device.System.Power.Budget.Month.Notify>OFF</Device.System.Power.Budget.Month.Notify>
		</POWER_USAGE>
	</CMD>
</SMARTPLUG>
	 */

}