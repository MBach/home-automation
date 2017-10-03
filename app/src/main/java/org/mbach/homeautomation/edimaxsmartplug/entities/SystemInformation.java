package org.mbach.homeautomation.edimaxsmartplug.entities;

import org.w3c.dom.Document;

import java.util.Arrays;

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

public class SystemInformation {

	// Run
	private String cus;
	private String model;
	private String firmwareVersion;
	
	// Lan
	private String macAddress;
	
	// SMTP
	private String smtpServerAddress;
	private String smtpServerPort;
	private String smtpServerCertificate;
	private String smtpServerAuthEnable;
	private String smtpEmailSender;
	private String smtpEmailRecipient;
	private String smtpEmailNotifyEnable;
	private String smtpServerAuthName;
	private String smtpServerAuthPassword;
	
	// TimeZone
	private int timeZone;
	private String[] timeZoneServerAddresses = new String[5];
	private String timeZoneDaylightEnabled;
	
	// Name
	private String name;

	private SystemInformation(String cus, String model, String firmwareVersion,
			String macAddress, String smtpServerAddress, String smtpServerPort,
			String smtpServerCertificate, String smtpServerAuthEnable,
			String smtpEmailSender, String smtpEmailRecipient,
			String smtpEmailNotifyEnable, String smtpServerAuthName,
			String smtpServerAuthPassword, int timeZone,
			String[] timeZoneServerAddresses, String timeZoneDaylightEnabled,
			String name) {

		this.cus = cus;
		this.model = model;
		this.firmwareVersion = firmwareVersion;
		this.macAddress = macAddress;
		this.smtpServerAddress = smtpServerAddress;
		this.smtpServerPort = smtpServerPort;
		this.smtpServerCertificate = smtpServerCertificate;
		this.smtpServerAuthEnable = smtpServerAuthEnable;
		this.smtpEmailSender = smtpEmailSender;
		this.smtpEmailRecipient = smtpEmailRecipient;
		this.smtpEmailNotifyEnable = smtpEmailNotifyEnable;
		this.smtpServerAuthName = smtpServerAuthName;
		this.smtpServerAuthPassword = smtpServerAuthPassword;
		this.timeZone = timeZone;
		this.timeZoneServerAddresses = timeZoneServerAddresses;
		this.timeZoneDaylightEnabled = timeZoneDaylightEnabled;
		this.name = name;
	}
	
	public static SystemInformation createFromDocument(Document document) {
		String cusString = document.getElementsByTagName("Run.Cus").item(0).getTextContent();
		String modelString = document.getElementsByTagName("Run.Model").item(0).getTextContent();
		String firmwareVersionString = document.getElementsByTagName("Run.FW.Version").item(0).getTextContent();
		String macAddressString = document.getElementsByTagName("Run.LAN.Client.MAC.Address").item(0).getTextContent();
		
		String smtpServerAddressString = document.getElementsByTagName("Device.System.SMTP.0.Server.Address").item(0).getTextContent();
		String smtpServerPortString = document.getElementsByTagName("Device.System.SMTP.0.Server.Port").item(0).getTextContent();
		String smtpServerCertificateString = document.getElementsByTagName("Device.System.SMTP.0.Server.Certificate").item(0).getTextContent();
		String smtpServerAuthEnableString = document.getElementsByTagName("Device.System.SMTP.0.Server.Authorization.Enable").item(0).getTextContent();
		String smtpServerEmailSenderString = document.getElementsByTagName("Device.System.SMTP.0.Mail.Sender").item(0).getTextContent();
		String smtpEmailRecipientString = document.getElementsByTagName("Device.System.SMTP.0.Mail.Recipient").item(0).getTextContent();
		String smtpNotifyEnableString = document.getElementsByTagName("Device.System.SMTP.0.Mail.Action.Notify.Enable").item(0).getTextContent();
		String smtpServerAuthNameString = document.getElementsByTagName("Device.System.SMTP.0.Server.Authorization.Name").item(0).getTextContent();
		String smtpServerAuthPasswordString = document.getElementsByTagName("Device.System.SMTP.0.Server.Authorization.Password").item(0).getTextContent();
		
		String timeZoneString = document.getElementsByTagName("Device.System.TimeZone.Zone").item(0).getTextContent();
		String timeZoneServerAddress1String = document.getElementsByTagName("Device.System.TimeZone.Server.Address.0").item(0).getTextContent();
		String timeZoneServerAddress2String = document.getElementsByTagName("Device.System.TimeZone.Server.Address.1").item(0).getTextContent();
		String timeZoneServerAddress3String = document.getElementsByTagName("Device.System.TimeZone.Server.Address.2").item(0).getTextContent();
		String timeZoneServerAddress4String = document.getElementsByTagName("Device.System.TimeZone.Server.Address.3").item(0).getTextContent();
		String timeZoneServerAddress5String = document.getElementsByTagName("Device.System.TimeZone.Server.Address.4").item(0).getTextContent();
		
		String timeZoneDaylightEnabledString = document.getElementsByTagName("Device.System.TimeZone.Daylight.Enable").item(0).getTextContent();
		String name = document.getElementsByTagName("Device.System.Name").item(0).getTextContent();
		
		int timeZone = Integer.parseInt(timeZoneString);
		String[] timeZoneServerAddresses = new String[5];
		timeZoneServerAddresses[0] = timeZoneServerAddress1String;
		timeZoneServerAddresses[1] = timeZoneServerAddress2String;
		timeZoneServerAddresses[2] = timeZoneServerAddress3String;
		timeZoneServerAddresses[3] = timeZoneServerAddress4String;
		timeZoneServerAddresses[4] = timeZoneServerAddress5String;

		return new SystemInformation(
			cusString,
			modelString,
			firmwareVersionString,
			macAddressString,
			smtpServerAddressString,
			smtpServerPortString,
			smtpServerCertificateString,
			smtpServerAuthEnableString,
			smtpServerEmailSenderString,
			smtpEmailRecipientString,
			smtpNotifyEnableString,
			smtpServerAuthNameString,
			smtpServerAuthPasswordString,
			timeZone,
			timeZoneServerAddresses,
			timeZoneDaylightEnabledString,
			name
		);
	}

	@Override
	public String toString() {
		return "SystemInformation [\n\tcus: " + cus + ", \n\tmodel: " + model
				+ ", \n\tfirmwareVersion: " + firmwareVersion
				+ ", \n\tmacAddress: " + macAddress
				+ ", \n\tsmtpServerAddress: " + smtpServerAddress
				+ ", \n\tsmtpServerPort: " + smtpServerPort
				+ ", \n\tsmtpServerCertificate: " + smtpServerCertificate
				+ ", \n\tsmtpServerAuthEnable: " + smtpServerAuthEnable
				+ ", \n\tsmtpEmailSender: " + smtpEmailSender
				+ ", \n\tsmtpEmailRecipient: " + smtpEmailRecipient
				+ ", \n\tsmtpEmailNotifyEnable: " + smtpEmailNotifyEnable
				+ ", \n\tsmtpServerAuthName: " + smtpServerAuthName
				+ ", \n\tsmtpServerAuthPassword: " + smtpServerAuthPassword
				+ ", \n\ttimeZone: " + timeZone
				+ ", \n\ttimeZoneServerAddresses: "
				+ Arrays.toString(timeZoneServerAddresses)
				+ ", \n\ttimeZoneDaylightEnabled: " + timeZoneDaylightEnabled
				+ ", \n\tname: " + name + "\n]";
	}

	public String getCus() {
		return cus;
	}

	public String getModel() {
		return model;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getSmtpServerAddress() {
		return smtpServerAddress;
	}

	public String getSmtpServerPort() {
		return smtpServerPort;
	}

	public String getSmtpServerCertificate() {
		return smtpServerCertificate;
	}

	public String getSmtpServerAuthEnable() {
		return smtpServerAuthEnable;
	}

	public String getSmtpEmailSender() {
		return smtpEmailSender;
	}

	public String getSmtpEmailRecipient() {
		return smtpEmailRecipient;
	}

	public String getSmtpEmailNotifyEnable() {
		return smtpEmailNotifyEnable;
	}

	public String getSmtpServerAuthName() {
		return smtpServerAuthName;
	}

	public String getSmtpServerAuthPassword() {
		return smtpServerAuthPassword;
	}

	public int getTimeZone() {
		return timeZone;
	}

	public String[] getTimeZoneServerAddresses() {
		return timeZoneServerAddresses;
	}

	public String isTimeZoneDaylightEnabled() {
		return timeZoneDaylightEnabled;
	}

	public String getName() {
		return name;
	}
}
