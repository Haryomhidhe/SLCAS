package utils;

import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileHandler — handles saving and loading all system data to/from text files.
 *
 * File formats:
 *   items.txt  — one item per line, pipe-delimited
 *   users.txt  — one user per line, pipe-delimited
 *
 * Item line format:
 *   BOOK|id|title|author|year|available|borrowCount|genre
 *   MAGAZINE|id|title|author|year|available|borrowCount|issueNumber
 *   JOURNAL|id|title|author|year|available|borrowCount|volume
 *
 * User line format:
 *   userId|name|email|borrowHistory(comma-sep)|currentlyBorrowed(itemId:date,...)
 *
 * Uses try-catch throughout for error tolerance as required by the assignment.
 */
public class FileHandler {

    private static final Logger logger = Logger.getLogger(FileHandler.class.getName());

    // Default file paths
    public static final String ITEMS_FILE = "data/items.txt";
    public static final String USERS_FILE = "data/users.txt";

    // ─── Save Items ───────────────────────────────────────────────────────────

    /**
     * Save all library items to the items file.
     * Creates the data/ directory if it does not exist.
     *
     * @param items    the list of items to save
     * @param filePath path to the output file (use ITEMS_FILE for default)
     * @return true if saved successfully, false on error
     */
    public static boolean saveItems(List<LibraryItem> items, String filePath) {
        try {
            ensureDirectoryExists(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (LibraryItem item : items) {
                    writer.write(item.toFileString());
                    writer.newLine();
                }
            }
            logger.info("Saved " + items.size() + " items to " + filePath);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save items to " + filePath, e);
            return false;
        }
    }

    /** Convenience — saves to default items file path */
    public static boolean saveItems(List<LibraryItem> items) {
        return saveItems(items, ITEMS_FILE);
    }

    // ─── Load Items ───────────────────────────────────────────────────────────

    /**
     * Load all library items from the items file.
     * Skips any malformed lines and continues loading.
     *
     * @param filePath path to the items file
     * @return list of loaded LibraryItems (empty list if file not found or error)
     */
    public static List<LibraryItem> loadItems(String filePath) {
        List<LibraryItem> items = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            logger.warning("Items file not found: " + filePath + ". Starting with empty catalogue.");
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    LibraryItem item = parseItemLine(line);
                    if (item != null) items.add(item);
                } catch (Exception e) {
                    logger.warning("Skipping malformed item on line " + lineNumber + ": " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load items from " + filePath, e);
        }

        logger.info("Loaded " + items.size() + " items from " + filePath);
        return items;
    }

    /** Convenience — loads from default items file path */
    public static List<LibraryItem> loadItems() {
        return loadItems(ITEMS_FILE);
    }

    // ─── Parse Item Line ──────────────────────────────────────────────────────

    /**
     * Parse one line from items.txt into a LibraryItem.
     * Format: TYPE|id|title|author|year|available|borrowCount|extraField
     */
    private static LibraryItem parseItemLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 8) return null;

        String type        = parts[0].trim().toUpperCase();
        String id          = parts[1].trim();
        String title       = parts[2].trim();
        String author      = parts[3].trim();
        int    year        = Integer.parseInt(parts[4].trim());
        boolean available  = Boolean.parseBoolean(parts[5].trim());
        int borrowCount    = Integer.parseInt(parts[6].trim());
        String extraField  = parts[7].trim();

        LibraryItem item;

        switch (type) {
            case "BOOK":
                item = new Book(id, title, author, year, available, extraField);
                break;
            case "MAGAZINE":
                item = new Magazine(id, title, author, year, available, Integer.parseInt(extraField));
                break;
            case "JOURNAL":
                item = new Journal(id, title, author, year, available, Integer.parseInt(extraField));
                break;
            default:
                logger.warning("Unknown item type: " + type);
                return null;
        }

        item.setBorrowCount(borrowCount);
        return item;
    }

    // ─── Save Users ───────────────────────────────────────────────────────────

    /**
     * Save all user accounts to the users file.
     *
     * @param users    map of userId -> UserAccount
     * @param filePath path to the output file
     * @return true if saved successfully
     */
    public static boolean saveUsers(HashMap<String, UserAccount> users, String filePath) {
        try {
            ensureDirectoryExists(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (UserAccount user : users.values()) {
                    writer.write(user.toFileString());
                    writer.newLine();
                }
            }
            logger.info("Saved " + users.size() + " users to " + filePath);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save users to " + filePath, e);
            return false;
        }
    }

    /** Convenience — saves to default users file path */
    public static boolean saveUsers(HashMap<String, UserAccount> users) {
        return saveUsers(users, USERS_FILE);
    }

    // ─── Load Users ───────────────────────────────────────────────────────────

    /**
     * Load all user accounts from the users file.
     *
     * @param filePath path to the users file
     * @return map of userId -> UserAccount (empty map if file not found)
     */
    public static HashMap<String, UserAccount> loadUsers(String filePath) {
        HashMap<String, UserAccount> users = new HashMap<>();
        File file = new File(filePath);

        if (!file.exists()) {
            logger.warning("Users file not found: " + filePath + ". Starting with no users.");
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    UserAccount user = parseUserLine(line);
                    if (user != null) users.put(user.getUserId(), user);
                } catch (Exception e) {
                    logger.warning("Skipping malformed user on line " + lineNumber + ": " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load users from " + filePath, e);
        }

        logger.info("Loaded " + users.size() + " users from " + filePath);
        return users;
    }

    /** Convenience — loads from default users file path */
    public static HashMap<String, UserAccount> loadUsers() {
        return loadUsers(USERS_FILE);
    }

    // ─── Parse User Line ──────────────────────────────────────────────────────

    /**
     * Parse one line from users.txt into a UserAccount.
     * Format: userId|name|email|history(comma-sep)|currentBorrowed(itemId:date,...)
     */
    private static UserAccount parseUserLine(String line) {
        String[] parts = line.split("\\|", -1); // -1 to keep trailing empty parts
        if (parts.length < 3) return null;

        String userId = parts[0].trim();
        String name   = parts[1].trim();
        String email  = parts[2].trim();

        UserAccount user = new UserAccount(userId, name, email);

        // parse borrowing history (comma-separated item IDs)
        if (parts.length > 3 && !parts[3].trim().isEmpty()) {
            String[] historyIds = parts[3].split(",");
            ArrayList<String> history = new ArrayList<>();
            for (String id : historyIds) {
                if (!id.trim().isEmpty()) history.add(id.trim());
            }
            user.setBorrowingHistory(history);
        }

        // parse currently borrowed (itemId:date,itemId:date,...)
        if (parts.length > 4 && !parts[4].trim().isEmpty()) {
            String[] currentEntries = parts[4].split(",");
            HashMap<String, LocalDate> currentBorrowed = new HashMap<>();
            for (String entry : currentEntries) {
                String[] kv = entry.split(":");
                if (kv.length == 2) {
                    try {
                        String itemId  = kv[0].trim();
                        LocalDate date = LocalDate.parse(kv[1].trim());
                        currentBorrowed.put(itemId, date);
                    } catch (Exception e) {
                        logger.warning("Could not parse borrow entry: " + entry);
                    }
                }
            }
            user.setCurrentlyBorrowed(currentBorrowed);
        }

        return user;
    }

    // ─── Save Everything ─────────────────────────────────────────────────────

    /**
     * Save both items and users in one call.
     * This is what the GUI calls on exit or after each major operation.
     *
     * @return true if both saved successfully
     */
    public static boolean saveAll(List<LibraryItem> items, HashMap<String, UserAccount> users) {
        boolean itemsSaved = saveItems(items);
        boolean usersSaved = saveUsers(users);
        return itemsSaved && usersSaved;
    }

    // ─── Seed Default Data ────────────────────────────────────────────────────

    /**
     * Creates sample data files if no data files exist yet.
     * This gives the system something to show on first run.
     * Called by the GUI on startup if data files are missing.
     */
    public static void seedDefaultData() {
        File itemsFile = new File(ITEMS_FILE);
        if (!itemsFile.exists()) {
            List<LibraryItem> defaults = new ArrayList<>();
            defaults.add(new Book("BK-001", "Introduction to Java", "Herbert Schildt", 2020, true, "Programming"));
            defaults.add(new Book("BK-002", "Clean Code", "Robert C. Martin", 2008, false, "Software Engineering"));
            defaults.add(new Book("BK-003", "Data Structures & Algorithms", "Thomas Cormen", 2009, true, "Computer Science"));
            defaults.add(new Book("BK-004", "Design Patterns", "Gang of Four", 1994, true, "Software Engineering"));
            defaults.add(new Magazine("MG-001", "National Geographic", "Various Authors", 2023, true, 45));
            defaults.add(new Journal("JN-001", "Nature Journal", "Editorial Team", 2022, false, 12));
            saveItems(defaults);
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Ensure the directory for the given file path exists.
     * Creates it if it doesn't.
     */
    private static void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File dir  = file.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }
}
