package controller;

import model.Book;
import model.Journal;
import model.LibraryDatabase;
import model.LibraryDatabase.UndoOperation;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LibraryManager — the main controller for managing library items and users.
 *
 * Responsibilities:
 *   - Add new items (pushes to undo stack)
 *   - Delete items (pushes to undo stack)
 *   - Undo last admin action (pops from undo stack)
 *   - Retrieve items and users
 *   - Generate reports (most borrowed, category distribution)
 *
 * This is the class the GUI's Admin Panel and View Items Panel call into.
 */
public class LibraryManager {

    private LibraryDatabase database;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public LibraryManager(LibraryDatabase database) {
        this.database = database;
    }

    // ─── Item Management ─────────────────────────────────────────────────────

    /**
     * Add a new library item to the system.
     * Pushes an ADD operation to the undo stack.
     *
     * @param item the item to add
     * @return true if added successfully, false if ID already exists
     */
    public boolean addItem(LibraryItem item) {
        if (database.itemExists(item.getId())) {
            return false; // duplicate ID
        }
        database.addItem(item);
        database.pushUndo(new UndoOperation(UndoOperation.Type.ADD, item));
        return true;
    }

    /**
     * Remove an item from the system by ID.
     * Pushes a DELETE operation to the undo stack so it can be restored.
     *
     * @param id the item ID to remove
     * @return true if removed, false if item not found
     */
    public boolean removeItem(String id) {
        LibraryItem item = database.getItemById(id);
        if (item == null) {
            return false; // not found
        }
        database.removeItem(id);
        database.pushUndo(new UndoOperation(UndoOperation.Type.DELETE, item));
        return true;
    }

    /**
     * Undo the last admin action.
     * - If last action was ADD: removes the item that was added
     * - If last action was DELETE: re-adds the item that was deleted
     *
     * @return description of what was undone, or null if nothing to undo
     */
    public String undoLastAction() {
        if (!database.canUndo()) {
            return null; // nothing to undo
        }

        UndoOperation operation = database.popUndo();

        if (operation.getType() == UndoOperation.Type.ADD) {
            // undo an ADD = delete the item
            database.removeItem(operation.getItem().getId());
            return "Undone: Removed \"" + operation.getItem().getTitle() + "\"";

        } else if (operation.getType() == UndoOperation.Type.DELETE) {
            // undo a DELETE = re-add the item
            database.addItem(operation.getItem());
            return "Undone: Restored \"" + operation.getItem().getTitle() + "\"";
        }

        return null;
    }

    /**
     * Check if there is something to undo.
     */
    public boolean canUndo() {
        return database.canUndo();
    }

    /**
     * Get the description of the last undoable operation (without removing it).
     */
    public String getLastActionDescription() {
        UndoOperation op = database.peekUndo();
        if (op == null) return "Nothing to undo";
        return op.toString();
    }

    // ─── Item Retrieval ───────────────────────────────────────────────────────

    public List<LibraryItem> getAllItems() {
        return database.getAllItems();
    }

    public LibraryItem getItemById(String id) {
        return database.getItemById(id);
    }

    public int getTotalItemCount() {
        return database.getTotalItemCount();
    }

    public int getAvailableCount() {
        return database.getAvailableCount();
    }

    public int getBorrowedCount() {
        return database.getBorrowedCount();
    }

    // ─── User Management ─────────────────────────────────────────────────────

    /**
     * Add a user account.
     * @return true if added, false if userId already exists
     */
    public boolean addUser(UserAccount user) {
        if (database.userExists(user.getUserId())) {
            return false;
        }
        database.addUser(user);
        return true;
    }

    public UserAccount getUserById(String userId) {
        return database.getUserById(userId);
    }

    public Map<String, UserAccount> getAllUsers() {
        return database.getAllUsers();
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    /**
     * Get items sorted by borrow count descending (most borrowed first).
     * Uses a simple insertion sort for the report — small list, no need for complex sort.
     * @param topN how many top items to return (e.g., 5)
     */
    public List<LibraryItem> getMostBorrowedItems(int topN) {
        ArrayList<LibraryItem> all = new ArrayList<>(database.getAllItems());

        // Insertion sort by borrowCount descending
        for (int i = 1; i < all.size(); i++) {
            LibraryItem key = all.get(i);
            int j = i - 1;
            while (j >= 0 && all.get(j).getBorrowCount() < key.getBorrowCount()) {
                all.set(j + 1, all.get(j));
                j--;
            }
            all.set(j + 1, key);
        }

        // return top N
        int limit = Math.min(topN, all.size());
        return all.subList(0, limit);
    }

    /**
     * Count how many items exist per category (Book, Magazine, Journal).
     * @return map of itemType -> count
     */
    public Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        for (LibraryItem item : database.getAllItems()) {
            String type = item.getItemType();
            distribution.put(type, distribution.getOrDefault(type, 0) + 1);
        }
        return distribution;
    }

    /**
     * Get all users who currently have overdue items.
     * @return list of UserAccounts with at least one overdue item
     */
    public List<UserAccount> getUsersWithOverdueItems() {
        List<UserAccount> overdueUsers = new ArrayList<>();
        for (UserAccount user : database.getAllUsers().values()) {
            if (user.hasOverdueItems()) {
                overdueUsers.add(user);
            }
        }
        return overdueUsers;
    }

    /**
     * Count total overdue items across all users.
     */
    public int getTotalOverdueCount() {
        int count = 0;
        for (UserAccount user : database.getAllUsers().values()) {
            count += user.getOverdueItems().size();
        }
        return count;
    }

    /**
     * Get the frequent access cache — top 5 most borrowed items.
     */
    public List<LibraryItem> getMostFrequentItems() {
        return database.getMostFrequentItems();
    }

    /**
     * Recursively count total items by category.
     * Demonstrates the required recursive algorithm.
     *
     * @param items   the list of all items
     * @param type    the category to count (e.g., "Book")
     * @param index   current index (start with 0)
     * @return total count of items matching the type
     */
    public int countByTypeRecursive(List<LibraryItem> items, String type, int index) {
        if (index >= items.size()) {
            return 0; // base case
        }
        int match = items.get(index).getItemType().equalsIgnoreCase(type) ? 1 : 0;
        return match + countByTypeRecursive(items, type, index + 1); // recursive call
    }

    // ─── Convenience Factory Methods (GUI calls these to create items) ────────

    /**
     * Create and add a Book.
     * @return true if added successfully
     */
    public boolean addBook(String id, String title, String author, int year, String genre) {
        Book book = new Book(id, title, author, year, true, genre);
        return addItem(book);
    }

    /**
     * Create and add a Magazine.
     * @return true if added successfully
     */
    public boolean addMagazine(String id, String title, String author, int year, int issueNumber) {
        Magazine mag = new Magazine(id, title, author, year, true, issueNumber);
        return addItem(mag);
    }

    /**
     * Create and add a Journal.
     * @return true if added successfully
     */
    public boolean addJournal(String id, String title, String author, int year, int volume) {
        Journal journal = new Journal(id, title, author, year, true, volume);
        return addItem(journal);
    }
}
