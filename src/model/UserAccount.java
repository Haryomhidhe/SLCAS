package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserAccount class — represents a library user/student.
 * Tracks: personal info, full borrowing history, currently borrowed items, and due dates.
 * Uses ArrayList for borrowing history as required by the assignment.
 */
public class UserAccount {

    private String userId;
    private String name;
    private String email;

    // ArrayList of all item IDs this user has ever borrowed (borrowing history)
    private ArrayList<String> borrowingHistory;

    // Currently borrowed items: itemId -> due date
    private HashMap<String, LocalDate> currentlyBorrowed;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public UserAccount(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.borrowingHistory = new ArrayList<>();
        this.currentlyBorrowed = new HashMap<>();
    }

    // ─── Borrow / Return Tracking ─────────────────────────────────────────────

    /**
     * Record that this user has borrowed an item.
     * Adds to history and marks as currently borrowed with due date.
     */
    public void recordBorrow(String itemId, LocalDate dueDate) {
        borrowingHistory.add(itemId);
        currentlyBorrowed.put(itemId, dueDate);
    }

    /**
     * Record that this user has returned an item.
     * Removes from currently borrowed (stays in history).
     */
    public void recordReturn(String itemId) {
        currentlyBorrowed.remove(itemId);
    }

    /**
     * Check if this user currently has a specific item.
     */
    public boolean hasBorrowed(String itemId) {
        return currentlyBorrowed.containsKey(itemId);
    }

    /**
     * Get the due date for a currently borrowed item.
     */
    public LocalDate getDueDate(String itemId) {
        return currentlyBorrowed.get(itemId);
    }

    /**
     * Get list of overdue item IDs (due date is before today).
     */
    public List<String> getOverdueItems() {
        List<String> overdue = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Map.Entry<String, LocalDate> entry : currentlyBorrowed.entrySet()) {
            if (entry.getValue().isBefore(today)) {
                overdue.add(entry.getKey());
            }
        }
        return overdue;
    }

    /**
     * Compute overdue fine recursively.
     * Fine = R5 per day overdue per item.
     * @param overdueItems list of overdue item IDs
     * @param index        current index in recursion
     * @return total fine amount
     */
    public double computeOverdueFineRecursive(List<String> overdueItems, int index) {
        if (index >= overdueItems.size()) {
            return 0.0; // base case — no more items
        }
        String itemId = overdueItems.get(index);
        LocalDate dueDate = currentlyBorrowed.get(itemId);
        long daysLate = 0;
        if (dueDate != null) {
            daysLate = LocalDate.now().toEpochDay() - dueDate.toEpochDay();
            if (daysLate < 0) daysLate = 0;
        }
        double itemFine = daysLate * 5.0; // R5 per day
        return itemFine + computeOverdueFineRecursive(overdueItems, index + 1); // recursive call
    }

    /**
     * Convenience method — compute total fine for all overdue items.
     */
    public double getTotalFine() {
        List<String> overdue = getOverdueItems();
        return computeOverdueFineRecursive(overdue, 0);
    }

    /**
     * Check if this user has any overdue items.
     */
    public boolean hasOverdueItems() {
        return !getOverdueItems().isEmpty();
    }

    // ─── File Persistence ────────────────────────────────────────────────────

    /**
     * Convert to file string for saving.
     * Format: userId|name|email|history(comma-sep)|currentBorrowed(itemId:date,...)
     */
    public String toFileString() {
        // build borrowing history string
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < borrowingHistory.size(); i++) {
            history.append(borrowingHistory.get(i));
            if (i < borrowingHistory.size() - 1) history.append(",");
        }

        // build currently borrowed string
        StringBuilder current = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, LocalDate> entry : currentlyBorrowed.entrySet()) {
            if (!first) current.append(",");
            current.append(entry.getKey()).append(":").append(entry.getValue().toString());
            first = false;
        }

        return userId + "|" + name + "|" + email + "|"
                + history.toString() + "|" + current.toString();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ArrayList<String> getBorrowingHistory() { return borrowingHistory; }
    public void setBorrowingHistory(ArrayList<String> borrowingHistory) { this.borrowingHistory = borrowingHistory; }

    public HashMap<String, LocalDate> getCurrentlyBorrowed() { return currentlyBorrowed; }
    public void setCurrentlyBorrowed(HashMap<String, LocalDate> currentlyBorrowed) { this.currentlyBorrowed = currentlyBorrowed; }

    public int getTotalBorrowCount() { return borrowingHistory.size(); }

    @Override
    public String toString() {
        return "User[" + userId + "] " + name + " (" + email + ")"
                + " | Borrowed: " + currentlyBorrowed.size()
                + " | History: " + borrowingHistory.size();
    }
}
