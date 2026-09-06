package com.library.model;

import java.io.Serializable;

/**
 * Represents a Book in the library catalog.
 */
public class Book implements Serializable {

    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private int categoryId;
    private String categoryName;
    private String publisher;
    private int publicationYear;
    private int totalQuantity;
    private int availableQuantity;

    public Book() {
    }

    public Book(int bookId, String isbn, String title, String author, int categoryId,
                String categoryName, String publisher, int publicationYear,
                int totalQuantity, int availableQuantity) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}
