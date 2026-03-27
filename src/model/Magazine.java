package model;

public class Magazine extends LibraryItem {
    private int issueNumber;

    public Magazine(String id, String title, String author, int year, boolean available, int issueNumber) {
        super(id, title, author, year, available);
        this.issueNumber = issueNumber;
    }

    @Override
    public String getItemType() {
        return "Magazine";
    }

    @Override
    public String toFileString() {
        return "MAGAZINE|" + getId() + "|" + getTitle() + "|" + getAuthor() + "|"
                + getYear() + "|" + isAvailable() + "|" + getBorrowCount() + "|" + issueNumber;
    }

    public int getIssueNumber() { return issueNumber; }
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }

    @Override
    public String toString() {
        return super.toString() + " | Issue #" + issueNumber;
    }
}
