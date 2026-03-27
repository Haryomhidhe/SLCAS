package controller;

import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;

import java.time.LocalDate;
import java.util.List;

/**
 * BorrowController — handles all borrowing and returning operations.
 *
 * Responsibilities:
 *   - Borrow an item for a user (or join reservation queue if unavailable)
 *   - Return an item and notify the next person in the queue
 *   - Validate user and item existence
 *   - Update user borrowing records and item availability
 *
 * This is the class the GUI's Borrow/Return Panel calls into.
 */
public class BorrowController {

    private LibraryDatabase database;

    // Default loan period in days
    private static final int DEFAULT_LOAN_DAYS = 14;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public BorrowController(LibraryDatabase database) {
        this.database = database;
    }

    // ─── Result Wrapper ───────────────────────────────────────────────────────

    /**
     * Wraps the result of a borrow/return operation.
     * Makes it easy for the GUI to show the right message.
     */
    public static class BorrowResult {
        public enum Status { SUCCESS, QUEUED, ALREADY_BORROWED, ITEM_NOT_FOUND, USER_NOT_FOUND, ALREADY_IN_QUEUE, ERROR }

        private Status status;
        private String message;

        public BorrowResult(Status status, String message) {
            this.status = status;
            this.message = message;
        }

        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return status == Status.SUCCESS; }

        @Override
        public String toString() { return "[" + status + "] " + message; }
    }

    // ─── Borrow Item ─────────────────────────────────────────────────────────

    /**
     * Attempt to borrow an item for a user.
     *
     * Logic:
     *   1. Validate user and item exist
     *   2. If item is available → borrow it, set due date, update user record
     *   3. If item is unavailable → add user to reservation queue (if not already in it)
     *
     * @param userId   the ID of the user borrowing
     * @param itemId   the ID of the item to borrow
     * @param dueDate  the due date (null = use default 14 days from today)
     * @return BorrowResult describing what happened
     */
    public BorrowResult borrowItem(String userId, String itemId, LocalDate dueDate) {
        // validate user
        UserAccount user = database.getUserById(userId);
        if (user == null) {
            return new BorrowResult(BorrowResult.Status.USER_NOT_FOUND,
                    "User ID \"" + userId + "\" not found in the system.");
        }

        // validate item
        LibraryItem item = database.getItemById(itemId);
        if (item == null) {
            return new BorrowResult(BorrowResult.Status.ITEM_NOT_FOUND,
                    "Item ID \"" + itemId + "\" not found in the library.");
        }

        // check if user already has this item
        if (user.hasBorrowed(itemId)) {
            return new BorrowResult(BorrowResult.Status.ALREADY_BORROWED,
                    "You already have \"" + item.getTitle() + "\" checked out.");
        }

        // set due date
        LocalDate due = (dueDate != null) ? dueDate : LocalDate.now().plusDays(DEFAULT_LOAN_DAYS);

        // attempt to borrow
        if (item.borrow()) {
            // success — update user record
            user.recordBorrow(itemId, due);
            return new BorrowResult(BorrowResult.Status.SUCCESS,
                    "Successfully borrowed \"" + item.getTitle() + "\". Due date: " + due);
        } else {
            // item not available — add to reservation queue
            if (database.isUserInQueue(itemId, userId)) {
                return new BorrowResult(BorrowResult.Status.ALREADY_IN_QUEUE,
                        "You are already in the waitlist for \"" + item.getTitle() + "\"."
                        + " Queue position: " + (database.getQueueSize(itemId)));
            }
            database.addToReservationQueue(itemId, userId);
            int position = database.getQueueSize(itemId);
            return new BorrowResult(BorrowResult.Status.QUEUED,
                    "\"" + item.getTitle() + "\" is currently borrowed. "
                    + "You have been added to the waitlist (position " + position + ").");
        }
    }

    /**
     * Overload — borrow with default due date (14 days from today).
     */
    public BorrowResult borrowItem(String userId, String itemId) {
        return borrowItem(userId, itemId, null);
    }

    // ─── Return Item ─────────────────────────────────────────────────────────

    /**
     * Process the return of an item by a user.
     *
     * Logic:
     *   1. Validate user and item
     *   2. Check user actually has this item
     *   3. Return the item (mark as available)
     *   4. Update user record
     *   5. If someone is in the reservation queue, notify them (auto-borrow for next user)
     *
     * @param userId the ID of the user returning
     * @param itemId the ID of the item being returned
     * @return BorrowResult describing what happened
     */
    public BorrowResult returnItem(String userId, String itemId) {
        // validate user
        UserAccount user = database.getUserById(userId);
        if (user == null) {
            return new BorrowResult(BorrowResult.Status.USER_NOT_FOUND,
                    "User ID \"" + userId + "\" not found in the system.");
        }

        // validate item
        LibraryItem item = database.getItemById(itemId);
        if (item == null) {
            return new BorrowResult(BorrowResult.Status.ITEM_NOT_FOUND,
                    "Item ID \"" + itemId + "\" not found in the library.");
        }

        // check user actually has this item
        if (!user.hasBorrowed(itemId)) {
            return new BorrowResult(BorrowResult.Status.ERROR,
                    "User " + user.getName() + " does not have \"" + item.getTitle() + "\" checked out.");
        }

        // process return
        item.returnItem();
        user.recordReturn(itemId);

        // check reservation queue — give item to next person in line
        String nextUserId = database.pollNextInQueue(itemId);
        if (nextUserId != null) {
            UserAccount nextUser = database.getUserById(nextUserId);
            if (nextUser != null) {
                // auto-borrow for next user
                LocalDate due = LocalDate.now().plusDays(DEFAULT_LOAN_DAYS);
                item.borrow();
                nextUser.recordBorrow(itemId, due);
                return new BorrowResult(BorrowResult.Status.SUCCESS,
                        "\"" + item.getTitle() + "\" returned successfully by " + user.getName() + ". "
                        + "Automatically assigned to next user in queue: " + nextUser.getName()
                        + " (due: " + due + ").");
            }
        }

        return new BorrowResult(BorrowResult.Status.SUCCESS,
                "\"" + item.getTitle() + "\" returned successfully by " + user.getName() + ". Item is now available.");
    }

    // ─── Queue Queries ────────────────────────────────────────────────────────

    /**
     * Get the reservation waitlist for a given item.
     * @return list of userIds waiting, in order
     */
    public List<String> getWaitlistForItem(String itemId) {
        return database.getReservationQueueAsList(itemId);
    }

    /**
     * Get number of people waiting for an item.
     */
    public int getQueueSize(String itemId) {
        return database.getQueueSize(itemId);
    }

    // ─── User Status Queries ──────────────────────────────────────────────────

    /**
     * Get fine amount for a user (based on overdue items).
     * Uses the recursive fine computation in UserAccount.
     */
    public double getUserFine(String userId) {
        UserAccount user = database.getUserById(userId);
        if (user == null) return 0.0;
        return user.getTotalFine();
    }

    /**
     * Get overdue item IDs for a user.
     */
    public List<String> getOverdueItemsForUser(String userId) {
        UserAccount user = database.getUserById(userId);
        if (user == null) return null;
        return user.getOverdueItems();
    }

    /**
     * Check if a user has any overdue items.
     */
    public boolean userHasOverdue(String userId) {
        UserAccount user = database.getUserById(userId);
        if (user == null) return false;
        return user.hasOverdueItems();
    }
}
