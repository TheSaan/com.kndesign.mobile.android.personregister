package com.knoeflerdesign.keywest;

import java.util.Calendar;

public class DateCalculator {

	final int DAYS_IN_LEAP_YEAR = 366;
	final int DAYS_IN_YEAR = 365;
	final int FIRST_LEAP_YEAR = 1582;
	final int MILLENNIUM = 2000;
	public int currDay, currMonth, currYear, currHour, currMinute;
	public int[] allLeapYears = {};
	public int[] leapYears;
	public int[][] holidays;

	public DateCalculator() {
		Calendar cal = Calendar.getInstance();

		currDay = cal.get(Calendar.DAY_OF_MONTH);
		currMonth = cal.get(Calendar.MONTH) + 1;
		currYear = cal.get(Calendar.YEAR);

		currMinute = cal.get(Calendar.MINUTE);
		currHour = cal.get(Calendar.HOUR_OF_DAY);

		saveLeapYearsToArray(FIRST_LEAP_YEAR, currYear);
	}

	// //////////////////////////////////////////////////////////////////////////////////
	// PRIVATES
	/*
	 * The following four methods return the modulo integer for day, month and
	 * century plus decade to calculate the day of the week
	 * 
	 * @getDayOfWeekAt(day,month,year,language)
	 */
	private int getYearTModulo(int yearT) {
		int newYearT = (yearT + (yearT / 4)) % 7;
		return newYearT;
	}

	private int getYearHModulo(int yearH) {
		int newYearH = (3 - (yearH % 4)) * 2;
		return newYearH;
	}

	private int getDayModulo(int day) {
		int newDay = day % 7;
		return newDay;
	}

	private int getMonthModulo(int month) {

		// thats the modulo constants for each month
		final int[] MonthModulo = { 0, 3, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5 };
		if (month == 0)
			return 0;
		else
			return MonthModulo[month - 1];
	}

	// DE: Schaltjahr berechnen
	// EN: Calculate if it's a Leap Year or not
	public boolean isLeapYear(int year) {

		boolean rule1 = false;
		boolean rule2 = true;
		boolean rule3 = false;

		// rule1
		if (year % 4 == 0)
			rule1 = true;
		// rule2
		if (year % 100 != 0)
			rule2 = false;
		// rule3
		if (year % 400 == 0 && year >= 1582)
			rule3 = true;
		if (rule1 && !rule2 || rule3) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Saves all leap years between the start & end argument years of the method
	 * 
	 * @param year_start
	 *            The year from where to count.Doesn't have to be a leap year
	 * @param year_end
	 *            The year to where to count. Doesn't have to be a leap year
	 * */
	public void saveLeapYearsToArray(int year_start, int year_end) {
		int k = 0;
		int u = 0;

		int[] cache = new int[500];
		for (int i = year_start; i <= year_end; i++) {
			if (isLeapYear(i)) {
				cache[k] = i;
				k++;
			}
		}

		while (cache[u] != 0) {
			u++;
		}
		leapYears = new int[u];
		for (int i = 0; i < leapYears.length; i++) {
			leapYears[i] = cache[i];
		}
		// clear array "cache" to save memory
		cache = null;
	}

	/**
	 * Saves the all leap years from year 1584 to the current year to an array.
	 * Actually 1582 was the first leap year but nut based on the criteria of
	 * the today algorithm for the gregorian calendar
	 * 
	 */
	private void saveLeapYearsToArray() {
		int k = 0;
		int u = 0;

		int[] cache = new int[500];
		for (int i = FIRST_LEAP_YEAR; i <= currYear; i++) {
			if (isLeapYear(i)) {
				cache[k] = i;
				k++;
			}
		}

		while (cache[u] != 0) {
			u++;
		}
		allLeapYears = new int[u];
		for (int i = 0; i < allLeapYears.length; i++) {
			allLeapYears[i] = cache[i];
		}
		// clear array "cache" to save memory
		cache = null;
	}

	private int countLeapYearsFromBirthToToday(int yearOfBirth) {
		int amount = 0;
		int i = 0;
		int k;
		saveLeapYearsToArray(yearOfBirth, currYear);
		// if(leapYears.length == 0){
		// System.out.println("[leapYears.length] got set to 500");
		// leapYears = new int[500];
		// }else{
		// System.out.println("[leapYears] length = "+leapYears.length);
		// }

		// System.out.println("length: " + leapYears.length);
		// move leapYears index to the first leap year after the date of birth,
		// even if it's the same year
		// after index i the counting leap years starting

		if (leapYears.length > 0) {
			while (yearOfBirth >= leapYears[i]) {
				// System.out.println("leapYears["+i+"]:"+leapYears[i]);
				i++;
			}
			// k counts now the available/ leap years after the birth
			k = i;

			// System.out.println("k:  "+ k);
			for (int f = 0; f <= leapYears.length; f++) {
				// System.out.println(k);
				if (leapYears[k] >= currYear || leapYears[k] == 0)
					break;
				amount++;
			}
		} else {
			amount = 0;
		}
		// set to null to save memory
		leapYears = null;
		return amount;
	}

	// counts the years from the year of birth minus the leap years until today
	// ->this leap year value comes from the function which is named like the
	// argument
	private int countYearsWithoutLeapYearsToToday(int year,
			int countLeapYearsFromBirthToToday_FUNCTION_WITH_BIRTHYEAR_ARGUMENT) {

		int amountOfLeapYears = countLeapYearsFromBirthToToday_FUNCTION_WITH_BIRTHYEAR_ARGUMENT;

		int amountOfYears = currYear - year - amountOfLeapYears;
		// System.out.println("currYear:  "+currYear);
		// System.out.println("year:  "+year);
		// System.out.println("amountOfLeapYears:  "+amountOfLeapYears);
		// System.out.println("amountOfYears:  "+amountOfYears);
		return amountOfYears;
	}

	private int countDaysToTheEndOfTheYear(int day, int month, int year) {
		int m = 0;
		int restDaysOfTheYear = 0;
		int d = day;

		if (m != 12)
			m = month + 1;// get only the full month to the end of the year, the
							// rest will be added separately as int d ("day")
		else
			// if the month is december, it just calculates with the rest of the
			// days in december and not with a full month (31 days for dec)
			m = 0;
		if (m > 0) {
			while (m <= 12) {
				restDaysOfTheYear += getDaysOfMonth(m, year);
				m++;
			}
		}
		restDaysOfTheYear += getDaysOfMonth(month, year) - d;
		return restDaysOfTheYear;
	}

	private void calculateHolidays(int day, int month, int year) {

	}

	// //////////////////////////////////////////////////////////////////////////////////
	/*
	 * Input year to check if its a leap year to set days in february to 28 or
	 * 29
	 */
	public int getDaysOfMonth(int month, int year) {

		if (month > 0) {
			switch (month) {
			case 1:
				return 31;
			case 2: {
				if (isLeapYear(year))
					return 29;
				else
					return 28;
			}
			case 3:
				return 31;
			case 4:
				return 30;
			case 5:
				return 31;
			case 6:
				return 30;
			case 7:
				return 31;
			case 8:
				return 31;
			case 9:
				return 30;
			case 10:
				return 31;
			case 11:
				return 30;
			case 12:
				return 31;
			default: {
				System.err.println("(int) month Integer is not valid!");
				return -1;
			}
			}
		} else {
			System.out.println("Incorrect month number. Just use 1-12!");
			return -1;
		}
	}

	public boolean isHoliday(int day, int month, int year) {

		return false;
	}

	public String[] getCurrentHoliday() {
		String[] str = {};
		return str;
	}

	public int[] getLeapYearsFromTo(int fromYear, int toYear) {
		saveLeapYearsToArray(fromYear, toYear);
		return leapYears;
	}

	public String getMonthName(int monthIndex, String language) {

		if (language == "en") {
			switch (monthIndex) {
			case 1:
				return "January";
			case 2:
				return "February";
			case 3:
				return "March";
			case 4:
				return "April";
			case 5:
				return "May";
			case 6:
				return "June";
			case 7:
				return "July";
			case 8:
				return "August";
			case 9:
				return "September";
			case 10:
				return "October";
			case 11:
				return "November";
			case 12:
				return "December";
			default:
				return "Wrong value in Argument!";
			}
		}
		if (language == "de") {
			switch (monthIndex) {
			case 1:
				return "Januar";
			case 2:
				return "Februar";
			case 3:
				return "März";
			case 4:
				return "April";
			case 5:
				return "Mai";
			case 6:
				return "Juni";
			case 7:
				return "Juli";
			case 8:
				return "August";
			case 9:
				return "September";
			case 10:
				return "Oktober";
			case 11:
				return "November";
			case 12:
				return "Dezember";
			default:
				return "Falscher Wert in Argument!";
			}
		} else {
			return "Not available in language <" + language + ">!";
		}
	}

	public int[] getDate() {
		int[] date = new int[3];
		date[0] = currDay;
		date[1] = currMonth;
		date[2] = currYear;
		return date;
	}

	public int[] getTime() {
		int[] time = new int[2];
		time[0] = currHour;
		time[1] = currMinute;

		return time;
	}

	public int getAgeInYears(int day, int month, int year) {
		if (month - currMonth <= 0 && day - currDay <= 0) {
			return currYear - year;

		} else {
			System.out.println("month: " + "[" + month + "]" + "-" + "["
					+ currMonth + "]" + "=" + (month - currMonth));
			System.out.println("day: " + "[" + day + "]" + "-" + "[" + currDay
					+ "]" + "=" + (day - currDay));
			return currYear - (year+1);
		}

	}

	public int getAgeInDays(int day, int month, int year) {
		// variables for the actual formula
		int amountOfLeapYears = countLeapYearsFromBirthToToday(year);
		int amountOfYears = countYearsWithoutLeapYearsToToday(year,
				amountOfLeapYears);

		int DaysInFirstYear = countDaysToTheEndOfTheYear(day, month, year);
		int DaysInLastYear;

		// System.out.println("Days in First Year 1:  "+DaysInFirstYear);

		/*
		 * amountOfYears is taken -2 because the start and end year are not a
		 * full year. even if its the 31st of december it calculates the first
		 * and last year separately
		 */amountOfYears -= 2;

		if (isLeapYear(year)) {
			DaysInLastYear = DAYS_IN_LEAP_YEAR
					- countDaysToTheEndOfTheYear(currDay, currMonth, currYear);
		} else {
			DaysInLastYear = DAYS_IN_YEAR
					- countDaysToTheEndOfTheYear(currDay, currMonth, currYear);
		}
		// System.out.println("Days in Last Year 1:  "+ DaysInLastYear);
		/*
		 * Example of the calculation The birth date is 1st August 1990 or
		 * 1.8.1990: <DaysInFirstYear>: Days of Sep(30)+Oct(31)+Nov(30)+Dec(31)+
		 * 
		 * days of (Aug(31)-day of Birth(1)=30)
		 * 
		 * == 151 days in the first year
		 * ------------------------------------------------- <DaysOfLastYear>:
		 * For Example the current date is the 5th Sept 2014
		 * 
		 * Constant <DAYS_IN_YEAR(365)> or <DAYS_IN_LEAP_YEAR(366)> (minus)-
		 * <All days of the months to August (February 28 or 29, depends on leap
		 * year)>+ <days in september (5)>
		 * --------------------------------------------------
		 * 
		 * Plus all full years to the end date (mostly current date)
		 */

		int days = DaysInFirstYear + DaysInLastYear + amountOfLeapYears
				* DAYS_IN_LEAP_YEAR + amountOfYears * DAYS_IN_YEAR;
		// System.out.println(DaysInFirstYear+"+"
		// +DaysInLastYear+"+"
		// +amountOfLeapYears*DAYS_IN_LEAP_YEAR+"+"
		// +amountOfYears*DAYS_IN_YEAR+"="
		// +days);

		return days;
	}

	public int getAgeInHours(int day, int month, int year) {
		int days = getAgeInDays(day, month, year);
		int h = 24;
		int hours = 0;

		// last day (today) just added in hours, not as a whole day
		hours = h * (days - 1);
		hours += currHour;

		return hours;
	}

	public int getAgeInMinutes(int day, int month, int year) {
		int days = getAgeInDays(day, month, year);
		int m = 24 * 60;
		int minutes = 0;
		// calculate the minutes past today
		int todayMinutes = (currHour) * 60 + currMinute;
		minutes = m * (days - 1);
		minutes += todayMinutes;

		return minutes;
	}

	public int getAgeInSeconds(int day, int month, int year) {
		int days = getAgeInDays(day, month, year);
		int s = 24 * 3600;
		int seconds = 0;

		// calculate the minutes past today
		int todaySeconds = (currHour * 60 + currMinute) * 60;

		seconds = s * (days - 1);
		seconds += todaySeconds;
		return seconds;
	}

	public long getAgeInMilliseconds(int day, int month, int year) {
		int days = getAgeInDays(day, month, year);
		long mills = 24 * 3600000;
		long milliseconds = 0;

		int todayMilliSeconds = (currHour * 60 + currMinute) * 60 * 1000;
		milliseconds = mills * (days - 1);
		milliseconds += todayMilliSeconds;
		return milliseconds;
	}

	public String getDayOfWeekAt(int day, int month, int year, String language) {
		/*
		 * Get the century, this calculation makes sure that the calender can
		 * calculate from year 0 (gregorian calendar)
		 */
		String dayOfWeek;
		int century = 0;
		while (century < year) {
			century += 100;
		}
		int decade = year - MILLENNIUM;

		// System.out.println("century:\t"+century);
		// System.out.println("decade:\t"+decade);
		// System.out.println("year:\t"+year);
		//
		// System.out.println("getDayModulo(day):\t"+getDayModulo(day));
		// System.out.println("getMonthModulo(month):\t"+getMonthModulo(month));
		// System.out.println("getYearHModulo(century):\t"+getYearHModulo(century));
		// System.out.println("getYearTModulo(decade):\t"+getYearTModulo(decade));

		int dayOfWeekModulo = (getDayModulo(day) + getMonthModulo(month)
				+ getYearHModulo(century) + getYearTModulo(decade)) % 7;

		// System.out.println("dayOfWeekModulo:\t"+dayOfWeekModulo);
		if (language == "en") {
			switch (dayOfWeekModulo) {
			case 0:
				dayOfWeek = "Sunday";
				break;
			case 1:
				dayOfWeek = "Monday";
				break;
			case 2:
				dayOfWeek = "Tuesday";
				break;
			case 3:
				dayOfWeek = "Wednesday";
				break;
			case 4:
				dayOfWeek = "Thursday";
				break;
			case 5:
				dayOfWeek = "Friday";
				break;
			case 6:
				dayOfWeek = "Saturday";
				break;
			default:
				dayOfWeek = "Day of week couldn't be found by the given data.\nPlease check your date input!";
				break;
			}
		} else {
			dayOfWeek = "Has no Value!";
		}
		if (language == "de") {
			switch (dayOfWeekModulo) {
			case 0:
				dayOfWeek = "Sonntag";
				break;
			case 1:
				dayOfWeek = "Montag";
				break;
			case 2:
				dayOfWeek = "Dienstag";
				break;
			case 3:
				dayOfWeek = "Mittwoch";
				break;
			case 4:
				dayOfWeek = "Donnerstag";
				break;
			case 5:
				dayOfWeek = "Freitag";
				break;
			case 6:
				dayOfWeek = "Samstag";
				break;
			default:
				dayOfWeek = "Wochentag konnte mit den eingegebenen Daten nicht ermittelt werden.\nBitte ueberpruefen Sie ihre Eingabe erneut!";
				break;
			}
		} else {
			dayOfWeek = "Hat keinen Wert!";
		}
		return dayOfWeek;
	}

	// Assumption method
	/* This Method prints every Data you can calculate by the given date */
	public void printAgeInAllFormats(int day, int month, int year,
			String language) {
		/*
		 * preset to include new languages if(language == "default"){
		 * System.out.println(":\t"+); System.out.println(":\t"+);
		 * System.out.println(":\t"+); System.out.println(":\t"+);
		 * System.out.println(":\t"+); System.out.println(":\t"+); }
		 */
		if (language == "en") {
			System.out.println("Age in...\t" + getAgeInYears(day, month, year));
			System.out.println("Years:\t" + getAgeInYears(day, month, year));
			System.out.println("Days:\t" + getAgeInDays(day, month, year));
			System.out.println("Hours:\t" + getAgeInHours(day, month, year));
			System.out.println("Minuts:\t" + getAgeInMinutes(day, month, year));
			System.out
					.println("Seconds:\t" + getAgeInSeconds(day, month, year));
			System.out.println("Milliseconds:\t"
					+ getAgeInMilliseconds(day, month, year));
		}
		if (language == "de") {
			System.out.println("Alter in...");
			System.out.println("Jahren:\t" + getAgeInYears(day, month, year));
			System.out.println("Tagen:\t" + getAgeInDays(day, month, year));
			System.out.println("Stunden:\t" + getAgeInHours(day, month, year));
			System.out
					.println("Minuten:\t" + getAgeInMinutes(day, month, year));
			System.out.println("Sekunden:\t"
					+ getAgeInSeconds(day, month, year));
			System.out.println("Millisekunden:\t"
					+ getAgeInMilliseconds(day, month, year));
		}
	}
}