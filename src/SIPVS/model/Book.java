package SIPVS.model;

public class Book {
    private String bookName;
    private String bookDescription;
    private String authorName;
    private Integer pages;
    private Integer firstPublished;
    private Integer printingYear;
    private String isbn;

    public Book(String bookName, String bookDescription, String authorName, Integer pages, Integer firstPublished, Integer printingYear, String isbn) {
        this.bookName = bookName;
        this.bookDescription = bookDescription;
        this.authorName = authorName;
        this.pages = pages;
        this.firstPublished = firstPublished;
        this.printingYear = printingYear;
        this.isbn = isbn;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getFirstPublished() {
        return firstPublished;
    }

    public void setFirstPublished(Integer firstPublished) {
        this.firstPublished = firstPublished;
    }

    public Integer getPrintingYear() {
        return printingYear;
    }

    public void setPrintingYear(Integer printingYear) {
        this.printingYear = printingYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
