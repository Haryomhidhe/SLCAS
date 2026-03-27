package model;

public class Book extends LibraryItem {
    private String genre;

    public Book(String id, String title, String author, int year, boolean available, String genre) {
        super(id, title, author, year, available);
        this.genre = genre;
    }

    @Override
    public String getItemType() {
        return "Book";
    }

    @Override
    public String toFileString() {
        return "BOOK|" + getId() + "|" + getTitle() + "|" + getAuthor() + "|"
                + getYear() + "|" + isAvailable() + "|" + getBorrowCount() + "|" + genre;
    }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    @Override
    public String toString() {
        return super.toString() + " | Genre: " + genre;
    }
}
