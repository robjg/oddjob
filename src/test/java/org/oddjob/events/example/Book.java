package org.oddjob.events.example;

import java.util.List;

public class Book {

    private String bookName;

    private List<Trade> trades;

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public List<Trade> getTrades() {
		return trades;
	}

	public void setTrades(List<Trade> trades) {
		this.trades = trades;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + bookName;
	}
}
