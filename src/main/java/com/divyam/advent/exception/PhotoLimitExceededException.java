package com.divyam.advent.exception;

public class PhotoLimitExceededException extends RuntimeException {

    private final long count;
    private final int cap;

    public PhotoLimitExceededException(long count, int cap) {
        super("Monthly photo limit reached (" + count + "/30). Limit resets on the 1st of next month.");
        this.count = count;
        this.cap = cap;
    }

    public long getCount() {
        return count;
    }

    public int getCap() {
        return cap;
    }
}
