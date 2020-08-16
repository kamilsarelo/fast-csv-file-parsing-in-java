package com.kamilsarelo.csv;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class CsvReadParsePerformanceTest {

	// constants ///////////////////////////////////////////////////////////////////////////////////

	private static final Logger LOGGER = Logger.getLogger(CsvReadParsePerformanceTest.class.getClass().getName());

	// fields //////////////////////////////////////////////////////////////////////////////////////

	private static List<String> linesExpected;
	private static List<Bar> dataExpected;

	// helper methods //////////////////////////////////////////////////////////////////////////////

	private static final String toString(
			final Bar bar) {

		return Constants.FORMAT_DATE.format(new Date(bar.time)) + ","
				+ bar.open + ","
				+ bar.high + ","
				+ bar.low + ","
				+ bar.close + ","
				+ bar.volume;
	}

	private static final void assertRead(
			final List<String> linesActual) {

		linesActual.remove(0);
		assertEquals(linesExpected.size(), linesActual.size());

		// access via index is very slow when using LinkedList in read4():
		//for (int index = 0; index < linesExpected.size(); index++) {
		//	assertEquals(linesExpected.get(index), linesActual.get(index));
		//}

		// use List.listIterator() instead:
		final ListIterator<String> linesExpectedIterator = linesExpected.listIterator();
		final ListIterator<String> linesActualIterator = linesActual.listIterator();
		while (linesExpectedIterator.hasNext()) {
			assertEquals(linesExpectedIterator.next(), linesActualIterator.next());
		}
	}

	private static final void assertParse(
			final BiFunction<String, int[], Bar> function) {

		final ArrayList<Bar> dataActual = new ArrayList<>(linesExpected.size());
		final int[] lineIndeces = new int[10];
		for (final String line : linesExpected) {
			if (line.isBlank()) {
				continue;
			}
			dataActual.add(function.apply(line, lineIndeces));
		}

		for (int index = 0; index < linesExpected.size(); index++) {
			assertEquals(toString(dataExpected.get(index)), toString(dataActual.get(index)));
		}
	}

	// test methods ////////////////////////////////////////////////////////////////////////////////

	@BeforeClass
	public static final void setUp() {
		try {
			final List<String> lines = CsvReadParsePerformance.read1(Constants.PATHS[0]);
			lines.remove(0);

			final List<Bar> data = new ArrayList<>(lines.size());
			for (final String line : lines) {
				if (line.isBlank()) {
					continue;
				}
				data.add(CsvReadParsePerformance.parse1(line));
			}

			linesExpected = Collections.unmodifiableList(lines);
			dataExpected = Collections.unmodifiableList(data);

			LOGGER.info("read expected lines and parsed expected data");
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}

	@Test
	public final void testRead2() {
		assertRead(CsvReadParsePerformance.read2(Constants.PATHS[0]));
	}

	@Test
	public final void testRead3() {
		assertRead(CsvReadParsePerformance.read3(Constants.PATHS[0]));
	}

	@Test
	public final void testRead4() {
		assertRead(CsvReadParsePerformance.read4(Constants.PATHS[0]));
	}

	@Test
	public final void testParse2() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse2(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse3() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse3(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse4() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse4(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse5() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse5(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse6() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse6(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse7() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse7(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse8() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse8(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse9() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse9(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

	@Test
	public final void testParse10() {
		assertParse((line, lineIndeces) -> {
			try {
				return CsvReadParsePerformance.parse10(line, lineIndeces);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			return null;
		});
	}

}
