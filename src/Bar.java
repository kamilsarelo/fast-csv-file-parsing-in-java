package com.kamilsarelo.csv;

public class Bar {

	// fields //////////////////////////////////////////////////////////////////////////////////

	public final long time;
	public final double open;
	public final double high;
	public final double low;
	public final double close;
	public final double volume;

	// constructors ////////////////////////////////////////////////////////////////////////////

	public Bar(
			final long time,
			final double open,
			final double high,
			final double low,
			final double close,
			final double volume) {

		this.time = time;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}

	// methods /////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(
			final Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Bar)) {
			return false;
		}

		final Bar other = (Bar) obj;
		return time == other.time
				&& open == other.open
				&& high == other.high
				&& low == other.low
				&& close == other.close
				&& volume == other.volume;
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 31 * result + (int) (time ^ time >>> 32);

		final long openLong = Double.doubleToLongBits(open);
		result = 31 * result + (int) (openLong ^ openLong >>> 32);

		final long highLong = Double.doubleToLongBits(high);
		result = 31 * result + (int) (highLong ^ highLong >>> 32);

		final long lowLong = Double.doubleToLongBits(low);
		result = 31 * result + (int) (lowLong ^ lowLong >>> 32);

		final long closeLong = Double.doubleToLongBits(close);
		result = 31 * result + (int) (closeLong ^ closeLong >>> 32);

		final long volumeLong = Double.doubleToLongBits(volume);
		result = 31 * result + (int) (volumeLong ^ volumeLong >>> 32);

		return result;
	}

}
