# Fast CSV-file parsing in Java

As part of a bigger side-project I am working on, I investigated several ways to improve the reading- and parsing-performance of CSV-files with specific schemas in Java.

With using `java.io.BufferedReader.readLine()`, replacing `java.text.SimpleDateFormat.parse()`, `java.lang.Double.parseDouble()`, and `java.lang.Integer.parseInt()` and a few further tweaks I was able to significantly improve the reading- and parsing-performance of CSV-files with specific schemas. In my specfic case I reduced the total execution time of the code by almost impressive 80%:

methods: | `read1()` + `parse1()` | `read4()` + `parse10()`
--: | :--: | :--:
average read per pass: | 668 ms (worst) | **137 ms (best)**
percent change: | baseline | **-79,4%**

## Data

My CSV-files contain prices of financial instruments in a specific time resolution and have the following schema:
- date and time at close (`yyyy.MM.dd HH:mm:ss`)
- four prices for the time bucket: open, high, low, and close (`double`)
- traded volume during the time bucket (`double`)

Example:
```
2015.01.05 20:14:00,1.1942,1.19428,1.1942,1.19425,31.57
2015.01.05 20:15:00,1.19423,1.19429,1.19422,1.19429,53.1
2015.01.05 20:16:00,1.19429,1.19429,1.19422,1.19422,18.07
```

Each such line is time-series data that can be best thought of as **OHLC (Open-High-Low-Close) bar data**.

The goal was to keep all the data in these CSV-files, where it can easily be maintained and extended, omitting any additional database. This implied that the files must be read and parsed in the fastest possible manner. I divided the task into a reading-part and a parsing-part and investigated different performance-improvement-approaches for both.

## Benchmarking

For the benchmark numbers I used:
- 12 different CSV-files
- each file contains a full year of OHLC bar data for the EUR/USD currency pair in the 1-minute resolution
- hence each file is around 22 MB in size and contains around 373.500 of OHLC bars
- reading and parsing all these data was done in one pass during benchmarking
- preceding initial pass with the aim to warm up the JVM and let the JIT optimize things
- additional 10 passes in total after the initial pass to come up with the benchmark numbers

The corresponding Java code including the read/parse-optimizations can be found in the [CsvReadParsePerformance](/src/CsvReadParsePerformance.java) class.

## Dependencies

- the [Apache Commons Mathematics Library](https://commons.apache.org/proper/commons-math/) for calculating the benchmark numbers
- other than that everything is pure Java standard library

---

## Improving reading performance

**Reading performance comparison:**

methods: | `read1()` | `read2()` | `read3()` | `read4()`
--: | :--: | :--: | :--: | :--:
average read per pass: | 79 ms | 82 ms | 82 ms | **56 ms**
percent change: | baseline | +3,8% | +3,8% | **-29,1%**

### Step 1: Establish a baseline with `java.nio.file.Files.readAllLines()`

For the reading-part let's start with a straightforward approach provided by the Java NIO's `Files` class:

```java
private static final List<String> read1(final Path path) {
	try {
		return Files.readAllLines(path);
	} catch (final Throwable t) {
		t.printStackTrace();
	}
	return Collections.<String> emptyList();
}
```

**`read1()`** returns an average reading performance of **79 ms per pass** and this will be the baseline for further comparison.

### Step 2 & 3: Use `java.nio.file.Files.lines()` or `java.nio.file.Files.newBufferedReader()`

However, the Java NIO's `Files` class offers other methods for reading all lines in a file, so let's check them out too:

```java
private static final List<String> read2(final Path path) {
	try {
		return Files.lines(path).collect(Collectors.toList());
	} catch (final Throwable t) {
		t.printStackTrace();
	}
	return Collections.<String> emptyList();
}
```

```java
private static final List<String> read3(final Path path) {
	try (BufferedReader reader = Files.newBufferedReader(path)) {
		return reader.lines().collect(Collectors.toList());
	} catch (final Throwable t) {
		t.printStackTrace();
	}
	return Collections.<String> emptyList();
}
```

**`read2()`** and **`read3()`** both return an average reading performance of **82 ms per pass** which is a regression compared to **`read1()`** so skip these approaches.

###  Step 4: Use `java.io.BufferedReader.readLine()`

Finally, let's also have a look at good old buffered reading using Java IO's `BufferedReader`:

```java
private static final List<String> read4(final Path path) {
	final LinkedList<String> lines = new LinkedList<>();
	String line;
	try (FileReader reader = new FileReader(path.toFile()); BufferedReader bufferedReader = new BufferedReader(reader)) {
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
	} catch (final Throwable t) {
		t.printStackTrace();
	}
	return lines;
}
```

**`read4()`** returns an average reading performance of **56 ms per pass** and is the winner here so stick with this approach.

---

## Improving parsing performance

The goal in the parsing-part is to convert every read line from the CSV-file to an instance of the OHLC `Bar` class in the fastest possible manner and return a list of these - corresponding to the CSV-file's OHLC bar data. The full Java code can be found in the [CsvReadParsePerformance](/src/CsvReadParsePerformance.java) class.

```java
private static final class Bar {

	public final long time;
	public final double open;
	public final double high;
	public final double low;
	public final double close;
	public final double volume;

	...
}
```
**Parsing performance comparison:**

methods: | `parse1()` | `parse2()` | `parse3()` | `parse4()` | `parse5()` | `parse6()` | `parse7()` | `parse8()` | `parse9()` | `parse10()`
--: | :--: | :--: | :--: | :--: | :--: | :--: | :--: | :--: | :--: | :--:
average parse per pass: | 559 ms | 552 ms | 203 ms | 171 ms | 174 ms | 160 ms | 116 ms | 74 ms | 75 ms | **69 ms**
percent change: | baseline | -1,2% | -63,6% | -69,4% | -68,8% | -71,3% | -79,2% | -86,7% | -86,5% | **-87,6%**

### Step 1: Establish a baseline with `java.text.SimpleDateFormat.parse()` and `java.lang.Double.parseDouble()`

Let's start with a straightforward approach provided by the Java standard library's `SimpleDateFormat` and `Double` classes:

```java
private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
static {
	FORMAT_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
}
```
The parsing method `parse1()` splits the read line by commas and then uses `SimpleDateFormat` and `Double` to parse the time, prices, and volume:

```java
private static final Bar parse1(final String line) throws NumberFormatException, ParseException {
	final String[] strings = line.split(",", -1); // -1 to include empty strings
	return new Bar(
			FORMAT_DATE.parse(strings[0]).getTime(), // milliseconds since epoch
			Double.parseDouble(strings[1]), // open
			Double.parseDouble(strings[2]), // high
			Double.parseDouble(strings[3]), // low
			Double.parseDouble(strings[4]), // close
			Double.parseDouble(strings[5])); // volume
}
```

**`parse1()`** returns an average parsing performance of **559 ms per pass** and this will be the baseline for further comparison.

### Step 2: Replace `java.lang.String.split()` with `java.lang.String.substring()`

**First, let's focus on optimizing the parsing of the time**, let's check if using a different `String` method can improve the performance somehow. Instead of splitting the read line let's find out at which positions the particular data is located. Note that the date and time substring has always the same length and position. It is only necessary to find out where the prices and the volume are located in the read line:

```java
private static final void findIndeces(final String line, final int[] lineIndeces) {
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
```

After the positions are located, the substrings are passed to the parsing methods in the new parsing method `parse2()`:

```java
private static final Bar parse2(final String line, final int[] lineIndeces) throws NumberFormatException, ParseException {
	findIndeces(line, lineIndeces);
	return new Bar(
			FORMAT_DATE.parse(line.substring(0, 19)).getTime(), // milliseconds since epoch
			Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
			Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
			Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
			Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
			Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
}
```

**`parse2()`** returns an average parsing performance of **522 ms per pass** which is a slight improvement compared to **`parse1()`** so stick with this approach.

### Step 3: Replace `java.text.SimpleDateFormat`

Instead of using `SimpleDateFormat.parse()` let's introduce a new method `parseStringToMillisSinceEpoch1()`. It simply extracts and parses the numbers from the date and time string (since always the same length and position) and passes them to another new method `toMillisSinceEpoch()`:

```java
private static final long parseStringToMillisSinceEpoch1(final String string) {
	return toMillisSinceEpoch(
			Integer.parseInt(string.substring(0, 4)), // year
			Integer.parseInt(string.substring(5, 7)), // month
			Integer.parseInt(string.substring(8, 10)), // date
			Integer.parseInt(string.substring(11, 13)), // hours
			Integer.parseInt(string.substring(14, 16)), // minutes
			Integer.parseInt(string.substring(17, 19))); // seconds
}
```

`toMillisSinceEpoch()` calculates the time since epoch in milliseconds from the given numbers in a performant way:

```java
private static final long toMillisSinceEpoch(final int year, final int month, final int date, final int hrs, final int min, final int sec) {
	return (year - 1970) * MILLIS_PER_YEAR_365 // epoch is 1970-01-01 00:00:00 GMT
			+ LEAP_DAYS_SINCE_EPOCH_PER_YEAR_INCLUSIVE.get(year - 1) * MILLIS_PER_DAY // only till last year
			+ (month > 2 && LEAP_YEAR_INCLUSIVE.contains(year) ? MILLIS_PER_DAY : 0) // this year is leap year AND after February
			+ DAYS_SINCE_JANUARY_1ST_PER_MONTH_INCLUSIVE.get(month - 1) * MILLIS_PER_DAY
			+ (date - 1) * MILLIS_PER_DAY
			+ hrs * MILLIS_PER_HOUR
			+ min * MILLIS_PER_MINUTE
			+ sec * MILLIS_PER_SECOND;
}
```

To do so `toMillisSinceEpoch()` requires a bunch constants that are defined and calculated only once at the initilization of the class. The constants enable performant time calculation and are especially important regarding leap years:

```java
private static final int HOURS_PER_DAY = 24;
private static final int MINUTES_PER_HOUR = 60;
private static final int SECONDS_PER_MINUTE = 60;
private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
private static final long MILLIS_PER_SECOND = 1000L;
private static final long MILLIS_PER_MINUTE = SECONDS_PER_MINUTE * MILLIS_PER_SECOND;
private static final long MILLIS_PER_HOUR = SECONDS_PER_HOUR * MILLIS_PER_SECOND;
private static final long MILLIS_PER_DAY = SECONDS_PER_DAY * MILLIS_PER_SECOND;
private static final long MILLIS_PER_YEAR_365 = 365 * MILLIS_PER_DAY;

private static final Set<Integer> LEAP_YEAR_INCLUSIVE;
private static final Map<Integer, Integer> LEAP_DAYS_SINCE_EPOCH_PER_YEAR_INCLUSIVE;
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

private static final Map<Integer, Integer> DAYS_SINCE_JANUARY_1ST_PER_MONTH_INCLUSIVE;
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
```

The new parsing method `parse3()` calls `parseStringToMillisSinceEpoch1()` to parse the time:

```java
private static final Bar parse3(final String line, final int[] lineIndeces) throws NumberFormatException, ParseException {
	findIndeces(line, lineIndeces);
	return new Bar(
			parseStringToMillisSinceEpoch1(line), // milliseconds since epoch
			Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
			Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
			Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
			Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
			Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
}
```

**`parse3()`** returns an average parsing performance of **203 ms per pass** which is an **outstanding improvement** compared to **`parse2()`** so definitely stick with this approach.

### Step 4: Replace `java.lang.Integer.parseInt()`

Let's check if replacing `Integer.parseInt()` can improve the performance somehow. The new method `parseStringToMillisSinceEpoch2()` will parse the numbers from the date and time string using another new method `parseStringToInteger1()` instead of `Integer.parseInt()` before passing them to `toMillisSinceEpoch()`:

```java
private static final long parseStringToMillisSinceEpoch2(final String string) {
	return toMillisSinceEpoch(
			parseStringToInteger1(string, 0, 4), // year   
			parseStringToInteger1(string, 5, 7), // month  
			parseStringToInteger1(string, 8, 10), // date   
			parseStringToInteger1(string, 11, 13), // hours  
			parseStringToInteger1(string, 14, 16), // minutes
			parseStringToInteger1(string, 17, 19)); // seconds
}
```

`parseStringToInteger1()` parses the string to integer by using ASCII character numbers and decimal power multiplication:

```java
private static int parseStringToInteger1(final String string, final int indexBegin, final int indexEnd) {
	int number = 0;
	for (int index = indexBegin; index < indexEnd; index++) {
		number = number * 10 + string.charAt(index) - 48; // numbers start at 48 in ASCII
	}
	return number;
}
```

The new parsing method `parse4()` calls `parseStringToMillisSinceEpoch2()` to parse the time:

```java
private static final Bar parse4(final String line, final int[] lineIndeces) throws NumberFormatException, ParseException {
	findIndeces(line, lineIndeces);
	return new Bar(
			parseStringToMillisSinceEpoch2(line), // milliseconds since epoch
			Double.parseDouble(line.substring(lineIndeces[0], lineIndeces[1])), // open
			Double.parseDouble(line.substring(lineIndeces[2], lineIndeces[3])), // high
			Double.parseDouble(line.substring(lineIndeces[4], lineIndeces[5])), // low
			Double.parseDouble(line.substring(lineIndeces[6], lineIndeces[7])), // close
			Double.parseDouble(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
}
```

**`parse4()`** returns an average parsing performance of **171 ms per pass** which is a further improvement compared to **`parse3()`** so stick with this approach.

### Step 5: Replace multiplication operator

Based on **Step 4** let's check if [replacing the multiplication operator](https://www.geeksforgeeks.org/multiply-number-10-without-using-multiplication-operator/) in `number = number * 10 + string.charAt(index) - 48;` can improve the performance somehow:

```java
private static int parseStringToInteger2(final String string, final int indexBegin, final int indexEnd) {
	int number = 0;
	for (int index = indexBegin; index < indexEnd; index++) {
		number = (number << 1) + (number << 3) + string.charAt(index) - 48; // numbers start at 48 in ASCII
	}
	return number;
}
```

The new parsing method `parse5()` calls another new method `parseStringToMillisSinceEpoch3()` to parse the time and `parseStringToMillisSinceEpoch3()` uses the new `parseStringToInteger2()`.

**`parse5()`** returns an average parsing performance of **174 ms per pass** which is basically the same as in **`parse4()`**.

### Step 6: Replace `java.lang.Double.parseDouble()` with `java.math.BigDecimal`

**Now let's focus on optimizing the parsing of the prices and volume (`double`)**, let's check if replacing `Double.parseDouble()` can improve the performance somehow. Since there are know [Java issues](https://stackoverflow.com/questions/179427/how-to-resolve-a-java-rounding-double-issue) [and errors with rounding doubles](https://www.geeksforgeeks.org/rounding-off-errors-java/) and using [double for monetary calculations is troublesome](https://dzone.com/articles/never-use-float-and-double-for-monetary-calculatio) let's first try to use `BigDecimal` instead of `Double.parseDouble()` in the new method `parseStringToDouble1()`. The method builds two variables (`long`) from the passed string (integer part and decimal part) and uses decimal power multiplication and `BigDecimal` to return the actual combined number (`double`):

```java
private static double parseStringToDouble1(final String string) {
	int indexOfDelimeter = string.indexOf(".");
	if (indexOfDelimeter < 1) {
		indexOfDelimeter = string.length();
	}

	long numberInteger = 0;
	int mul = 1;
	for (int index = indexOfDelimeter - 1; index >= 0; index--) {
		final char charAt = string.charAt(index);
		numberInteger += (charAt - 48) * mul; // 0 starts at 48 in ASCII
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
```

The new parsing method `parse6()` calls `parseStringToDouble1()` to parse the prices and volume:

```java
private static final Bar parse6(final String line, final int[] lineIndeces) throws NumberFormatException, ParseException {
	findIndeces(line, lineIndeces);
	return new Bar(
			parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
			parseStringToDouble1(line.substring(lineIndeces[0], lineIndeces[1])), // open
			parseStringToDouble1(line.substring(lineIndeces[2], lineIndeces[3])), // high
			parseStringToDouble1(line.substring(lineIndeces[4], lineIndeces[5])), // low
			parseStringToDouble1(line.substring(lineIndeces[6], lineIndeces[7])), // close
			parseStringToDouble1(line.substring(lineIndeces[8], lineIndeces[9]))); // volume
}
```

**`parse6()`** returns an average parsing performance of **160 ms per pass** which is a slight improvement compared to **`parse4()`** and **`parse5()`** so stick with this approach.

### Step 7: Replace `java.math.BigDecimal`

Based on **Step 6** let's check if replacing `BigDecimal` can improve the performance somehow. The new parsing method `parseStringToDouble2()` builds a dividend (`long`) and a divisor (`double`) from the passed string by using ASCII character numbers and decimal power multiplication. The dividend is the number without any decimal part. The divisor represents the decimal power of the dividend. The retuned actual number (`double`) is the quotient of the dividend and divisor:

```java
private static double parseStringToDouble2(final String string, final int indexBegin, final int indexEnd) {
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
```

The new parsing method `parse7()` calls `parseStringToDouble2()` to parse the prices and volume:

```java
private static final Bar parse7(final String line, final int[] lineIndeces) throws NumberFormatException, ParseException {
	findIndeces(line, lineIndeces);
	return new Bar(
			parseStringToMillisSinceEpoch3(line), // milliseconds since epoch
			parseStringToDouble2(line, lineIndeces[0], lineIndeces[1]), // open
			parseStringToDouble2(line, lineIndeces[2], lineIndeces[3]), // high
			parseStringToDouble2(line, lineIndeces[4], lineIndeces[5]), // low
			parseStringToDouble2(line, lineIndeces[6], lineIndeces[7]), // close
			parseStringToDouble2(line, lineIndeces[8], lineIndeces[9])); // volume
}
```

**`parse7()`** returns an average parsing performance of **116 ms per pass** which is **another great improvement** compared to **`parse6()`** so stick with this approach.

### Step 8: Replace `java.lang.Math.pow()`

Based on **Step 7** let's check if replacing `Math.pow()` with decimal power multiplication can improve the performance somehow:

```java
private static double parseStringToDouble3(final String string, final int indexBegin, final int indexEnd) {
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
```

The new parsing method `parse8()` calls `parseStringToDouble3()` to parse the prices and volume.

**`parse8()`** returns an average parsing performance of **74 ms per pass** which is **one more outstanding improvement** compared to **`parse7()`** so definitely stick with this approach.

### Step 9: Replace multiplication operator

Based on **Step 8** let's check if [replacing the multiplication operator](https://www.geeksforgeeks.org/multiply-number-10-without-using-multiplication-operator/) in `divisor *= 10;` and `dividend = dividend * 10 + (character - 48);` can improve the performance somehow:

```java
private static double parseStringToDouble4(final String string, final int indexBegin, final int indexEnd) {
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
```

The new parsing method `parse9()` calls `parseStringToDouble4()` to parse the prices and volume.

**`parse9()`** returns an average parsing performance of **75 ms per pass** which is basically the same as in **`parse8()`**.

### Step 10: Using faster integer power

Based on **Step 8** let's finally check if replacing decimal power multiplication with a [faster approach](https://codingforspeed.com/using-faster-integer-power-in-java/) can improve the performance somehow:

```java
private static double parseStringToDouble5(final String string, final int indexBegin, final int indexEnd) {
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
```

The new parsing method `parse10()` calls `parseStringToDouble5()` to parse the prices and volume.

**`parse10()`** returns an average parsing performance of **69 ms per pass** which is a final slight improvement compared to **`parse8()`** and **`parse9()`** so stick with this approach.

**Step 10** was my final step in the series of parsing improvements. However, another idea to even further bring down the execution time would be for example reading and parsing the CSV-files in multiple parallel threads.
