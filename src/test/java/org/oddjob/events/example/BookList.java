package org.oddjob.events.example;

import java.util.List;

public class BookList {

	private String bookList;

	private List<String> books;

	public String getBookList() {
		return bookList;
	}

	public void setBookList(String bookList) {
		this.bookList = bookList;
	}

	public List<String> getBooks() {
		return books;
	}

	public void setBooks(List<String> books) {
		this.books = books;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + bookList;
	}
}
