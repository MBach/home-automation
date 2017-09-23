package org.mbach.homeautomation.edimaxsmartplug.entities;

import org.w3c.dom.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PowerInformation {

	private LocalDate lastToggleTime;
	private double nowCurrent;
	private double nowPower;
	private double nowEnergyDay;
	private double nowEnergyWeek;
	private double nowEnergyMonth;
	
	public PowerInformation(LocalDate lastToggleTime, double nowCurrent, double nowPower, double nowEnergyDay, double nowEnergyWeek, double nowEnergyMonth) {
		
		this.lastToggleTime = lastToggleTime;
		this.nowCurrent = nowCurrent;
		this.nowPower = nowPower;
		this.nowEnergyDay = nowEnergyDay;
		this.nowEnergyWeek = nowEnergyWeek;
		this.nowEnergyMonth = nowEnergyMonth;
	}
	
	public static PowerInformation createFromDocument(Document document) {
		
		String lastToggleTimeString = document.getElementsByTagName("Device.System.Power.LastToggleTime").item(0).getTextContent();
		String nowCurrentString = document.getElementsByTagName("Device.System.Power.NowCurrent").item(0).getTextContent();
		String nowPowerString = document.getElementsByTagName("Device.System.Power.NowPower").item(0).getTextContent();
		String nowEnergyDayString = document.getElementsByTagName("Device.System.Power.NowEnergy.Day").item(0).getTextContent();
		String nowEnergyWeekString = document.getElementsByTagName("Device.System.Power.NowEnergy.Week").item(0).getTextContent();
		String nowEnergyMonthString = document.getElementsByTagName("Device.System.Power.NowEnergy.Month").item(0).getTextContent();
		
		// lastToggleTimeString example 20160103142501
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		LocalDate lastToggleTime = LocalDate.parse(lastToggleTimeString, formatter);
		
		Double nowCurrent = Double.parseDouble(nowCurrentString);
		Double nowPower = Double.parseDouble(nowPowerString);
		Double nowEnergyDay = Double.parseDouble(nowEnergyDayString);
		Double nowEnergyWeek = Double.parseDouble(nowEnergyWeekString);
		Double nowEnergyMonth = Double.parseDouble(nowEnergyMonthString);
		
		PowerInformation powerInfo = new PowerInformation(lastToggleTime, nowCurrent, nowPower, nowEnergyDay, nowEnergyWeek, nowEnergyMonth);
		return powerInfo;
	}

	public LocalDate getLastToggleTime() {
		return lastToggleTime;
	}

	public double getNowCurrent() {
		return nowCurrent;
	}

	public double getNowPower() {
		return nowPower;
	}

	public double getNowEnergyDay() {
		return nowEnergyDay;
	}

	public double getNowEnergyWeek() {
		return nowEnergyWeek;
	}

	public double getNowEnergyMonth() {
		return nowEnergyMonth;
	}

	@Override
	public String toString() {
		return "PowerInformation [\n\tlastToggleTime: " + lastToggleTime
				+ ", \n\tnowCurrent: " + nowCurrent + ", \n\tnowPower: "
				+ nowPower + ", \n\tnowEnergyDay: " + nowEnergyDay
				+ ", \n\tnowEnergyWeek: " + nowEnergyWeek
				+ ", \n\tnowEnergyMonth: " + nowEnergyMonth + "\n]";
	}
}
