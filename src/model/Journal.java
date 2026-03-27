package model;

public class Journal extends LibraryItem {
    private int volume;

    public Journal(String id, String title, String author, int year, boolean available, int volume) {
        super(id, title, author, year, available);
        this.volume = volume;
    }

    @Override
    public String getItemType() {
        return "Journal";
    }

    @Override
    public String toFileString() {
        return "JOURNAL|" + getId() + "|" + getTitle() + "|" + getAuthor() + "|"
                + getYear() + "|" + isAvailable() + "|" + getBorrowCount() + "|" + volume;
    }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    @Override
    public String toString() {
        return super.toString() + " | Volume: " + volume;
    }
}
