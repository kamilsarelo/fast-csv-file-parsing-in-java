package com.kamilsarelo.csv;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class Constants {

	// constants ///////////////////////////////////////////////////////////////////////////////////

	private static final Path DIRECTORY = Path.of(
			System.getProperty("user.dir"),
			"CSV");

	public static final Path[] PATHS = new Path[] {
			DIRECTORY.resolve("EURUSD_1 Min_Ask_2005.01.01_2006.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Bid_2005.01.01_2006.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Ask_2006.01.01_2007.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Bid_2006.01.01_2007.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Ask_2007.01.01_2008.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Bid_2007.01.01_2008.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Ask_2008.01.01_2009.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Bid_2008.01.01_2009.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Ask_2009.01.01_2010.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Bid_2009.01.01_2010.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Ask_2010.01.01_2011.01.01.csv"),
			DIRECTORY.resolve("EURUSD_1 Min_Bid_2010.01.01_2011.01.01.csv"),
	};

	public static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	static {
		FORMAT_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static final int HOURS_PER_DAY = 24;
	public static final int MINUTES_PER_HOUR = 60;
	public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
	public static final int SECONDS_PER_MINUTE = 60;
	public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
	public static final long MILLIS_PER_SECOND = 1000L;
	public static final long MILLIS_PER_MINUTE = SECONDS_PER_MINUTE * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_HOUR = SECONDS_PER_HOUR * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_DAY = SECONDS_PER_DAY * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_YEAR_365 = 365 * MILLIS_PER_DAY;

	public static final Set<Integer> LEAP_YEAR_INCLUSIVE;
	public static final Map<Integer, Integer> LEAP_DAYS_SINCE_EPOCH_PER_YEAR_INCLUSIVE;
	static {
		final HashSet<Integer> leapYears = new HashSet<>();
		final HashMap<Integer, Integer> leapDaysSince = new HashMap<>();
		int leapYearCounter = 0;
		for (int year = 1970; year <= 2100; year++) {
			if (Year.isLeap(year)) {
				leapYears.add(year);
				leapYearCounter++;
			}
			leapDaysSince.put(year, leapYearCounter);
		}
		LEAP_YEAR_INCLUSIVE = Collections.unmodifiableSet(leapYears);
		LEAP_DAYS_SINCE_EPOCH_PER_YEAR_INCLUSIVE = Collections.unmodifiableMap(leapDaysSince);
	}

	public static final Map<Integer, Integer> DAYS_SINCE_JANUARY_1ST_PER_MONTH_INCLUSIVE;
	static {
		final HashMap<Integer, Integer> map = new HashMap<>();
		map.put(0, 0);
		map.put(1, 31);
		map.put(2, map.get(1) + 28); // 29 handled by leap years
		map.put(3, map.get(2) + 31);
		map.put(4, map.get(3) + 30);
		map.put(5, map.get(4) + 31);
		map.put(6, map.get(5) + 30);
		map.put(7, map.get(6) + 31);
		map.put(8, map.get(7) + 31);
		map.put(9, map.get(8) + 30);
		map.put(10, map.get(9) + 31);
		map.put(11, map.get(10) + 30);
		map.put(12, map.get(11) + 31);
		DAYS_SINCE_JANUARY_1ST_PER_MONTH_INCLUSIVE = Collections.unmodifiableMap(map);
	}

}
