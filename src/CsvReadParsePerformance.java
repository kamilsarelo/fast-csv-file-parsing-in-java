package com.kamilsarelo.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class CsvReadParsePerformance {

	// main method /////////////////////////////////////////////////////////////////////////////////

	public static final void main(
			final String[] args) {

		// warm-up /////////////////////////////////////////////////////////////////////////////////

		Arrays.stream(Constants.PATHS)
				.forEach(path -> {
					readAndParseWithStatistics(
							path,
							new SummaryStatistics(),
							new SummaryStatistics(),
							new SummaryStatistics());
				});

		// read and parse //////////////////////////////////////////////////////////////////////////

		final SummaryStatistics statisticsRead = new SummaryStatistics();
		final SummaryStatistics statisticsParse = new SummaryStatistics();
		final SummaryStatistics statisticsTotal = new SummaryStatistics();

		for (int pass = 0; pass < 10; pass++) {
			for (final Path path : Constants.PATHS) {
				readAndParseWithStatistics(
						path,
						statisticsRead,
						statisticsParse,
						statisticsTotal);
			}
		}

		// benchmarks //////////////////////////////////////////////////////////////////////////////

		System.out.println("reading:");
		System.out.println("  min = " + (int) statisticsRead.getMin() + " ms");
		System.out.println("  max = " + (int) statisticsRead.getMax() + " ms");
		System.out.println("  avg = " + (int) statisticsRead.getMean() + " ms");
		System.out.println("parsing:");
		System.out.println("  min = " + (int) statisticsParse.getMin() + " ms");
		System.out.println("  max = " + (int) statisticsParse.getMax() + " ms");
		System.out.println("  avg = " + (int) statisticsParse.getMean() + " ms");
		System.out.println("total:");
		System.out.println("  min = " + (int) statisticsTotal.getMin() + " ms");
		System.out.println("  max = " + (int) statisticsTotal.getMax() + " ms");
		System.out.println("  avg = " + (int) statisticsTotal.getMean() + " ms");
	}

	// helper methods //////////////////////////////////////////////////////////////////////////////

	private static final void readAndParseWithStatistics(
			final Path path,
			final SummaryStatistics statisticsRead,
			final SummaryStatistics statisticsParse,
			final SummaryStatistics statisticsTotal) {

		try {

			// reading /////////////////////////////////////////////////////////////////////////////

			final long timeReadBegin = System.currentTimeMillis();

//			final List<String> lines = read1(path);
//			final List<String> lines = read2(path);
//			final List<String> lines = read3(path);
			final List<String> lines = read4(path);

			final long timeReadEnd = System.currentTimeMillis();

			// parsing /////////////////////////////////////////////////////////////////////////////

			lines.remove(0); // skip first line with column headers
			final ArrayList<Bar> data = new ArrayList<>(lines.size());
			final int[] lineIndeces = new int[10];

			final long timeParseBegin = System.currentTimeMillis();

			//for (final String line : lines) {
			for (final ListIterator<String> linesIterator = lines.listIterator(); linesIterator.hasNext();) {
				final String line = linesIterator.next();
				if (line.isBlank()) {
					continue;
				}
//				data.add(parse1(line));
//				data.add(parse2(line, lineIndeces));
//				data.add(parse3(line, lineIndeces));
//				data.add(parse4(line, lineIndeces));
//				data.add(parse5(line, lineIndeces));
//				data.add(parse6(line, lineIndeces));
//				data.add(parse7(line, lineIndeces));
//				data.add(parse8(line, lineIndeces));
//				data.add(parse9(line, lineIndeces));
				data.add(parse10(line, lineIndeces));
			}

			final long timeParseEnd = System.currentTimeMillis();

			// benchmarks //////////////////////////////////////////////////////////////////////////

			statisticsRead.addValue(timeReadEnd - timeReadBegin);
			statisticsParse.addValue(timeParseEnd - timeParseBegin);
			statisticsTotal.addValue(timeReadEnd - timeReadBegin + timeParseEnd - timeParseBegin);

		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}

	// reading methods /////////////////////////////////////////////////////////////////////////////

	public static final List<String> read1(
			final Path path) {

		try {
			return Files.readAllLines(path);
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return Collections.<String> emptyList();
	}

	public static final List<String> read2(
			final Path path) {

		try {
			return Files.lines(path).collect(Collectors.toList());
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return Collections.<String> emptyList();
	}

	public static final List<String> read3(
			final Path path) {

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return reader.lines().collect(Collectors.toList());
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return Collections.<String> emptyList();
	}

	public static final List<String> read4(
			final Path path) {

		final LinkedList<String> lines = new LinkedList<>();
		// slightly slower: final ArrayList<String> lines = new ArrayList<>(500_000);
		String line;
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()))) {
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return lines;
	}

	// parsing methods /////////////////////////////////////////////////////////////////////////////

	public static final Bar parse1(
			final String line)
			throws NumberFormatException, ParseException {

		final String[] strings = line.split(",", -1); // -1 to include empty strings
		return new Bar(
				Constants.FORMAT_DATE.parse(strings[0]).getTime(), // milliseconds since epoch
				Double.parseDouble(strings[1]), // open
				Double.parseDouble(strings[2]), // high
				Double.parseDouble(strings[3]), // low
				Double.parseDouble(strings[4]), // close
				Double.parseDouble(strings[5])); // volume
	}

	private static final void findIndeces(
			final String line,
			final int[] lineIndeces) {

		lineIndeces[0] = 20;
		int lineIndecesIndex = 1;
		for (int index = 20; index < line.length(); index++) {
			if (line.charAt(index) == 44) { // ',' is 44 in ASCII
				lineIndeces[lineIndecesIndex++] = index;
				lineIndeces[lineIndecesIndex++] = index + 1;
			}
		}
		lineIndeces[9] = line.length();
	}

	public static final Bar parse2(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				Constants.FORMAT_DATE.parse(line.substring(0, 19)).getTime(), // milliseconds since epoch
				Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
				Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
				Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
				Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
				Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
	}

	public static final Bar parse3(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch1(line), // milliseconds since epoch
				Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
				Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
				Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
				Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
				Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
	}

	public static final Bar parse4(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch2(line), // milliseconds since epoch
				Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
				Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
				Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
				Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
				Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
	}

	public static final Bar parse5(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
				Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
				Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
				Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
				Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
				Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
	}

	public static final Bar parse6(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
				parseStringToDouble1(line.substring(lineIndeces[0], lineIndeces[1])), // open
				parseStringToDouble1(line.substring(lineIndeces[2], lineIndeces[3])), // high
				parseStringToDouble1(line.substring(lineIndeces[4], lineIndeces[5])), // low
				parseStringToDouble1(line.substring(lineIndeces[6], lineIndeces[7])), // close
				parseStringToDouble1(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
	}

	public static final Bar parse7(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
				parseStringToDouble2(line, lineIndeces[0], lineIndeces[1]), // open
				parseStringToDouble2(line, lineIndeces[2], lineIndeces[3]), // high
				parseStringToDouble2(line, lineIndeces[4], lineIndeces[5]), // low
				parseStringToDouble2(line, lineIndeces[6], lineIndeces[7]), // close
				parseStringToDouble2(line, lineIndeces[8], lineIndeces[9])); // volume
	}

	public static final Bar parse8(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
				parseStringToDouble3(line, lineIndeces[0], lineIndeces[1]), // open
				parseStringToDouble3(line, lineIndeces[2], lineIndeces[3]), // high
				parseStringToDouble3(line, lineIndeces[4], lineIndeces[5]), // low
				parseStringToDouble3(line, lineIndeces[6], lineIndeces[7]), // close
				parseStringToDouble3(line, lineIndeces[8], lineIndeces[9])); // volume
	}

	public static final Bar parse9(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
				parseStringToDouble4(line, lineIndeces[0], lineIndeces[1]), // open
				parseStringToDouble4(line, lineIndeces[2], lineIndeces[3]), // high
				parseStringToDouble4(line, lineIndeces[4], lineIndeces[5]), // low
				parseStringToDouble4(line, lineIndeces[6], lineIndeces[7]), // close
				parseStringToDouble4(line, lineIndeces[8], lineIndeces[9])); // volume
	}

	public static final Bar parse10(
			final String line,
			final int[] lineIndeces)
			throws NumberFormatException, ParseException {

		findIndeces(line, lineIndeces);
		return new Bar(
				parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
				parseStringToDouble5(line, lineIndeces[0], lineIndeces[1]), // open
				parseStringToDouble5(line, lineIndeces[2], lineIndeces[3]), // high
				parseStringToDouble5(line, lineIndeces[4], lineIndeces[5]), // low
				parseStringToDouble5(line, lineIndeces[6], lineIndeces[7]), // close
				parseStringToDouble5(line, lineIndeces[8], lineIndeces[9])); // volume
	}

	// String to milliseconds parsing methods //////////////////////////////////////////////////////

	private static final long toMillisSinceEpoch(
			final int year,
			final int month,
			final int date,
			final int hrs,
			final int min,
			final int sec) {

		return (year - 1970) * Constants.MILLIS_PER_YEAR_365 // epoch is 1970-01-01 00:00:00 GMT
				+ Constants.LEAP_DAYS_SINCE_EPOCH_PER_YEAR_INCLUSIVE.get(year - 1) * Constants.MILLIS_PER_DAY // only till last year
				+ (month > 2 && Constants.LEAP_YEAR_INCLUSIVE.contains(year) ? Constants.MILLIS_PER_DAY : 0) // this year is leap year AND after February
				+ Constants.DAYS_SINCE_JANUARY_1ST_PER_MONTH_INCLUSIVE.get(month - 1) * Constants.MILLIS_PER_DAY
				+ (date - 1) * Constants.MILLIS_PER_DAY
				+ hrs * Constants.MILLIS_PER_HOUR
				+ min * Constants.MILLIS_PER_MINUTE
				+ sec * Constants.MILLIS_PER_SECOND;
	}

	private static final long parseStringToMillisSinceEpoch1(
			final String string) {

		return toMillisSinceEpoch(
				Integer.parseInt(string.substring(0, 4)), // year
				Integer.parseInt(string.substring(5, 7)), // month
				Integer.parseInt(string.substring(8, 10)), // date
				Integer.parseInt(string.substring(11, 13)), // hours
				Integer.parseInt(string.substring(14, 16)), // minutes
				Integer.parseInt(string.substring(17, 19))); // seconds
	}

	private static final long parseStringToMillisSinceEpoch2(
			final String string) {

		return toMillisSinceEpoch(
				parseStringToInteger1(string, 0, 4), // year
				parseStringToInteger1(string, 5, 7), // month
				parseStringToInteger1(string, 8, 10), // date
				parseStringToInteger1(string, 11, 13), // hours
				parseStringToInteger1(string, 14, 16), // minutes
				parseStringToInteger1(string, 17, 19)); // seconds
	}

	private static final long parseStringToMillisSinceEpoch3(
			final String string) {

		return toMillisSinceEpoch(
				parseStringToInteger2(string, 0, 4), // year
				parseStringToInteger2(string, 5, 7), // month
				parseStringToInteger2(string, 8, 10), // date
				parseStringToInteger2(string, 11, 13), // hours
				parseStringToInteger2(string, 14, 16), // minutes
				parseStringToInteger2(string, 17, 19)); // seconds
	}

	// String to Integer parsing methods ///////////////////////////////////////////////////////////

	private static final int parseStringToInteger1(
			final String string,
			final int indexBegin,
			final int indexEnd) {

		int number = 0;
		for (int index = indexBegin; index < indexEnd; index++) {
			number = number * 10 + string.charAt(index) - 48; // numbers start at 48 in ASCII
		}
		return number;
	}

	private static final int parseStringToInteger2(
			final String string,
			final int indexBegin,
			final int indexEnd) {

		int number = 0;
		for (int index = indexBegin; index < indexEnd; index++) {
			number = (number << 1) + (number << 3) + string.charAt(index) - 48; // numbers start at 48 in ASCII
		}
		return number;
	}

	// String to Double parsing methods ////////////////////////////////////////////////////////////

	private static final double parseStringToDouble1(
			final String string) {

		int indexOfDelimeter = string.indexOf(".");
		if (indexOfDelimeter < 1) {
			indexOfDelimeter = string.length();
		}

		long numberInteger = 0;
		int mul = 1;
		for (int index = indexOfDelimeter - 1; index >= 0; index--) {
			final char charAt = string.charAt(index);
			numberInteger += (charAt - 48) * mul; // numbers start at 48 in ASCII
			mul *= 10;
		}

		long numberDecimal = 0;
		mul = 1;
		int move = 0;
		for (int index = string.length() - 1; index > indexOfDelimeter; index--) {
			final char charAt = string.charAt(index);
			numberDecimal += (charAt - 48) * mul;
			mul *= 10;
			move++;
		}

		return new BigDecimal(numberDecimal).movePointLeft(move).add(new BigDecimal(numberInteger)).doubleValue();
	}

	private static final double parseStringToDouble2(
			final String string,
			final int indexBegin,
			final int indexEnd) {

		long dividend = 0;
		double divisor = 1;
		for (int index = indexBegin; index < indexEnd; index++) {
			final char character = string.charAt(index);
			if (character == 46) { // '.' is 46 in ASCII
				divisor = Math.pow(10, indexEnd - index - 1); // Math.pow solves double rounding problems
				continue;
			}
			dividend = dividend * 10 + (character - 48); // numbers start at 48 in ASCII
		}
		return dividend / divisor;
	}

	private static final double parseStringToDouble3(
			final String string,
			final int indexBegin,
			final int indexEnd) {

		long dividend = 0;
		double divisor = 1;
		for (int index = indexBegin; index < indexEnd; index++) {
			final char character = string.charAt(index);
			if (character == 46) { // '.' is 46 in ASCII
				divisor = 1;
				for (int indexDivisor = index + 1; indexDivisor < indexEnd; indexDivisor++) {
					divisor *= 10;
				}
				continue;
			}
			dividend = dividend * 10 + (character - 48); // numbers start at 48 in ASCII
		}
		return dividend / divisor;
	}

	private static final double parseStringToDouble4(
			final String string,
			final int indexBegin,
			final int indexEnd) {

		long dividend = 0;
		long divisor = 1;
		for (int index = indexBegin; index < indexEnd; index++) {
			final char character = string.charAt(index);
			if (character == 46) { // '.' is 46 in ASCII
				for (int indexDivisor = index + 1; indexDivisor < indexEnd; indexDivisor++) {
					divisor = (divisor << 1) + (divisor << 3);
				}
				continue;
			}
			dividend = (dividend << 1) + (dividend << 3) + (character - 48); // numbers start at 48 in ASCII
		}
		return (double) dividend / divisor;
	}

	private static final double parseStringToDouble5(
			final String string,
			final int indexBegin,
			final int indexEnd) {

		long dividend = 0;
		double divisor = 1;
		for (int index = indexBegin; index < indexEnd; index++) {
			final char character = string.charAt(index);
			if (character == 46) { // '.' is 46 in ASCII
				long a = 10;
				long b = indexEnd - index - 1;
				divisor = 1;
				while (b > 0) {
					if ((b & 1) == 1) {
						divisor *= a;
					}
					b >>= 1;
					a *= a;
				}
				continue;
			}
			dividend = (dividend << 1) + (dividend << 3) + (character - 48); // numbers start at 48 in ASCII
		}
		return dividend / divisor;
	}

}
