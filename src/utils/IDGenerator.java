package utils;

import model.LibraryItem;

import java.util.List;

/**
 * IDGenerator — generates unique IDs for library items.
 * Uses a simple incrementing counter based on existing IDs.
 */
public class IDGenerator {

    private static final String BOOK_PREFIX = "BK-";
    private static final String MAGAZINE_PREFIX = "MG-";
    private static final String JOURNAL_PREFIX = "JN-";
    private static final String USER_PREFIX = "U-";

    /**
     * Generate the next available book ID.
     * @param existingItems list of all existing items
     * @return a unique book ID (e.g., "BK-005")
     */
    public static String generateBookId(List<LibraryItem> existingItems) {
        return generateNextId(existingItems, BOOK_PREFIX);
    }

    /**
     * Generate the next available magazine ID.
     */
    public static String generateMagazineId(List<LibraryItem> existingItems) {
        return generateNextId(existingItems, MAGAZINE_PREFIX);
    }

    /**
     * Generate the next available journal ID.
     */
    public static String generateJournalId(List<LibraryItem> existingItems) {
        return generateNextId(existingItems, JOURNAL_PREFIX);
    }

    /**
     * Generate a user ID.
     */
    public static String generateUserId(List<String> existingUserIds) {
        int max = 0;
        for (String id : existingUserIds) {
            if (id.startsWith(USER_PREFIX)) {
                try {
                    int num = Integer.parseInt(id.substring(USER_PREFIX.length()));
                    if (num > max) max = num;
                } catch (NumberFormatException e) {
                    // ignore malformed IDs
                }
            }
        }
        return USER_PREFIX + String.format("%03d", max + 1);
    }

    /**
     * Generic method to generate the next ID for a given prefix.
     */
    private static String generateNextId(List<LibraryItem> existingItems, String prefix) {
        int max = 0;
        for (LibraryItem item : existingItems) {
            String id = item.getId();
            if (id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > max) max = num;
                } catch (NumberFormatException e) {
                    // ignore malformed IDs
                }
            }
        }
        return prefix + String.format("%03d", max + 1);
    }
}
