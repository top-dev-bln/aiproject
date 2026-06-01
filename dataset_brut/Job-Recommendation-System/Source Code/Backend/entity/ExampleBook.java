// ExampleBook.java
// This class represents a book entity with basic bibliographic information.
// Used for demonstration in ExampleBookServlet for RESTful book management.
//
// Author: [Your Name]
// Date: [Date]
package com.example.jobrec.entity;

/**
 * ExampleBook represents a book with title, author, publication date, price, etc.
 */
public class ExampleBook {
    /** Book title */
    public String title;
    /** Book author */
    public String author;
    /** Publication date */
    public String date;
    /** Book price */
    public double price;
    /** Currency of the price */
    public String currency;
    /** Number of pages */
    public int pages;
    /** Book series name */
    public String series;
    /** Language code */
    public String language;
    /** ISBN number */
    public String isbn;

    /**
     * Constructor for ExampleBook.
     * @param title Book title
     * @param author Book author
     * @param date Publication date
     * @param price Book price
     * @param currency Currency of the price
     * @param pages Number of pages
     * @param series Book series name
     * @param language Language code
     * @param isbn ISBN number
     */
    public ExampleBook(String title, String author, String date, double price, String currency, int pages, String series, String language, String isbn) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.price = price;
        this.currency = currency;
        this.pages = pages;
        this.series = series;
        this.language = language;
        this.isbn = isbn;
    }
}

