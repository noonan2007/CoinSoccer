package com.samsung.android.sample.coinsoccer.settings;

public enum Which {

	FIRST,
	SECOND;

	private static final Which[] values = Which.values();

	public static Which getTheOppositeOne(Which which) {
		switch (which) {
			case FIRST: return SECOND;
			case SECOND: return FIRST;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public static Which getRandom() {
		return Math.random() < 0.5 ? FIRST : SECOND;
	}

	public static Which forOrdinal(int ordinal) {
		return values[ordinal];
	}
}
