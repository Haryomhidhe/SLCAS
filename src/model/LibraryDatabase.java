package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * LibraryDatabase — the central data store for the entire system.
 *
 * Uses ALL required data structures from the assignment:
 *   ✔ ArrayList      — stores all library items
 *   ✔ Queue          — reservation/waitlist per item (LinkedList as Queue)
 *   ✔ Stack          — undo last admin operation
 *   ✔ Array          — fixed-size cache for Most Frequently Accessed Items
 *
 * This class does NOT contain business logic — it is purely a data container.
 * Business logic lives in the controller layer (LibraryManager, BorrowController).
 */
public class LibraryDatabase {

    // ─── Data Structures ──────────────────────────────────────────────────────

    /** ArrayList of all library items in the system */
    private ArrayList<LibraryItem> items;

    /** HashMap of all user accounts: userId -> UserAccount */
    private HashMap<String, UserAccount> users;

    /**
     * Reservation queues: itemId -> Queue of userIds waiting for that item.
     * When a user tries to borrow an unavailable item, their ID joins the queue.
     */
    private HashMap<String, Queue<String>> reservationQueues;

    /**
     * Undo stack — stores the last admin operations so they can be reversed.
     * Each entry is an UndoOperation (type + item affected).
     */
    private Stack<UndoOperation> undoStack;

    /**
     * Fixed-size array cache for Most Frequently Accessed Items.
     * Always holds the top CACHE_SIZE items by borrow count.
     * Array is used here as required by the assignment.
     */
    private static final int CACHE_SIZE = 5;
    private LibraryItem[] frequentCache;

    // ─── Inner Class: UndoOperation ───────────────────────────────────────────

    /**
     * Represents an admin action that can be undone.
     * Stores the operation type and the item it affected.
     */
    public static class UndoOperation {
        public enum Type { ADD, DELETE }

        private Type type;
        private LibraryItem item;

        public UndoOperation(Type type, LibraryItem item) {
            this.type = type;
            this.item = item;
        }

        public Type getType() { return type; }
        public LibraryItem getItem() { return item; }

        @Override
        public String toString() {
            return type + ": " + item.getTitle();
        }
    }

    // ─── Constructor ─────────────────────────────────────────────────────────

    public LibraryDatabase() {
        items = new ArrayList<>();
        users = new HashMap<>();
        reservationQueues = new HashMap<>();
        undoStack = new Stack<>();
        frequentCache = new LibraryItem[CACHE_SIZE];
    }

    // ─── Item Operations ─────────────────────────────────────────────────────

    /** Add an item to the database */
    public void addItem(LibraryItem item) {
        items.add(item);
        updateFrequentCache();
    }

    /** Remove an item from the database by ID. Returns removed item or null. */
    public LibraryItem removeItem(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                LibraryItem removed = items.remove(i);
                updateFrequentCache();
                return removed;
            }
        }
        return null;
    }

    /** Find an item by its ID */
    public LibraryItem getItemById(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /** Check if an item ID already exists */
    public boolean itemExists(String id) {
        return getItemById(id) != null;
    }

    /** Get all items as a list */
    public ArrayList<LibraryItem> getAllItems() {
        return new ArrayList<>(items); // return a copy for safety
    }

    /** Get total item count */
    public int getTotalItemCount() {
        return items.size();
    }

    /** Count available items */
    public int getAvailableCount() {
        int count = 0;
        for (LibraryItem item : items) {
            if (item.isAvailable()) count++;
        }
        return count;
    }

    /** Count borrowed items */
    public int getBorrowedCount() {
        return items.size() - getAvailableCount();
    }

    // ─── User Operations ─────────────────────────────────────────────────────

    public void addUser(UserAccount user) {
        users.put(user.getUserId(), user);
    }

    public UserAccount getUserById(String userId) {
        return users.get(userId);
    }

    public boolean userExists(String userId) {
        return users.containsKey(userId);
    }

    public HashMap<String, UserAccount> getAllUsers() {
        return users;
    }

    // ─── Reservation Queue ────────────────────────────────────────────────────

    /**
     * Add a user to the reservation queue for a specific item.
     * Creates the queue for that item if it doesn't exist yet.
     */
    public void addToReservationQueue(String itemId, String userId) {
        reservationQueues.computeIfAbsent(itemId, k -> new LinkedList<>()).add(userId);
    }

    /**
     * Get the next user in line for an item. Removes them from the queue.
     * Returns null if no one is waiting.
     */
    public String pollNextInQueue(String itemId) {
        Queue<String> queue = reservationQueues.get(itemId);
        if (queue == null || queue.isEmpty()) return null;
        return queue.poll();
    }

    /**
     * Peek at who is next without removing them.
     */
    public String peekNextInQueue(String itemId) {
        Queue<String> queue = reservationQueues.get(itemId);
        if (queue == null || queue.isEmpty()) return null;
        return queue.peek();
    }

    /**
     * Get the full reservation queue for an item (as a list for display).
     */
    public List<String> getReservationQueueAsList(String itemId) {
        Queue<String> queue = reservationQueues.get(itemId);
        if (queue == null) return new ArrayList<>();
        return new ArrayList<>(queue);
    }

    /**
     * Check if a user is already in the queue for an item.
     */
    public boolean isUserInQueue(String itemId, String userId) {
        Queue<String> queue = reservationQueues.get(itemId);
        if (queue == null) return false;
        return queue.contains(userId);
    }

    /**
     * Get queue size for an item.
     */
    public int getQueueSize(String itemId) {
        Queue<String> queue = reservationQueues.get(itemId);
        if (queue == null) return 0;
        return queue.size();
    }

    // ─── Undo Stack ───────────────────────────────────────────────────────────

    /** Push an undo operation onto the stack */
    public void pushUndo(UndoOperation operation) {
        undoStack.push(operation);
    }

    /** Pop the last undo operation from the stack. Returns null if empty. */
    public UndoOperation popUndo() {
        if (undoStack.isEmpty()) return null;
        return undoStack.pop();
    }

    /** Peek at the last operation without removing it */
    public UndoOperation peekUndo() {
        if (undoStack.isEmpty()) return null;
        return undoStack.peek();
    }

    /** Check if there's anything to undo */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    // ─── Frequent Access Cache (Fixed-size Array) ─────────────────────────────

    /**
     * Rebuilds the frequent access cache from the current item list.
     * Finds the top CACHE_SIZE items by borrow count.
     * Uses a simple selection approach — cache is small so performance is fine.
     */
    public void updateFrequentCache() {
        // copy all items into a temp list
        ArrayList<LibraryItem> temp = new ArrayList<>(items);

        // simple selection sort to find top CACHE_SIZE by borrowCount
        for (int i = 0; i < Math.min(CACHE_SIZE, temp.size()); i++) {
            int maxIdx = i;
            for (int j = i + 1; j < temp.size(); j++) {
                if (temp.get(j).getBorrowCount() > temp.get(maxIdx).getBorrowCount()) {
                    maxIdx = j;
                }
            }
            // swap
            LibraryItem tmp = temp.get(i);
            temp.set(i, temp.get(maxIdx));
            temp.set(maxIdx, tmp);

            // place into fixed-size array
            frequentCache[i] = temp.get(i);
        }

        // fill remaining slots with null if fewer than CACHE_SIZE items exist
        for (int i = temp.size(); i < CACHE_SIZE; i++) {
            frequentCache[i] = null;
        }
    }

    /**
     * Get the frequent access cache (top 5 most borrowed items).
     * Returns a copy of the array.
     */
    public LibraryItem[] getFrequentCache() {
        return frequentCache.clone();
    }

    /**
     * Get frequent cache as a list (filters out null slots).
     */
    public List<LibraryItem> getMostFrequentItems() {
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : frequentCache) {
            if (item != null) result.add(item);
        }
        return result;
    }

    // ─── Direct Access to Raw Structures (for FileHandler) ───────────────────

    /** Direct access to the internal items list (used by FileHandler for saving) */
    public ArrayList<LibraryItem> getItemsRaw() {
        return items;
    }

    /** Replace items list entirely (used by FileHandler when loading) */
    public void setItemsRaw(ArrayList<LibraryItem> items) {
        this.items = items;
        updateFrequentCache();
    }

    /** Replace users map entirely (used by FileHandler when loading) */
    public void setUsersRaw(HashMap<String, UserAccount> users) {
        this.users = users;
    }
}
