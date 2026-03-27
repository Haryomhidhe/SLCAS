package model;

public abstract class LibraryItem implements Borrowable {
    private String id;
    private String title;
    private String author;
    private int year;
    private boolean available;
    private int borrowCount;

    public LibraryItem(String id, String title, String author, int year, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = available;
        this.borrowCount = 0;
    }

    public abstract String getItemType();
    public abstract String toFileString();

    @Override
    public boolean borrow() {
        if (available) {
            available = false;
            borrowCount++;
            return true;
        }
        return false;
    }

    @Override
    public boolean returnItem() {
        if (!available) {
            available = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public void setAvailable(boolean available) { this.available = available; }

    public int getBorrowCount() { return borrowCount; }
    public void setBorrowCount(int borrowCount) { this.borrowCount = borrowCount; }

    @Override
    public String toString() {
        return "[" + getItemType() + "] " + id + " | " + title + " | " + author
                + " | " + year + " | " + (available ? "Available" : "Borrowed");
    }
}
