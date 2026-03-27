package model;

public interface Borrowable {
    boolean borrow();
    boolean returnItem();
    boolean isAvailable();
}
