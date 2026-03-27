package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchEngine — implements all three required search algorithms from scratch.
 *
 * ✔ Linear Search   — checks every item one by one, works on unsorted lists
 * ✔ Binary Search   — fast search on a sorted list (sorted by the search field)
 * ✔ Recursive Search — recursive version of linear search, required by assignment
 *
 * All searches support searching by: title, author, type, or year.
 * All searches are case-insensitive and support partial matches.
 *
 * The GUI's Search & Sort Panel calls these methods.
 */
public class SearchEngine {

    // Search field constants — GUI passes one of these strings
    public static final String BY_TITLE  = "title";
    public static final String BY_AUTHOR = "author";
    public static final String BY_TYPE   = "type";
    public static final String BY_YEAR   = "year";

    // ─── Helper: Get Field Value ──────────────────────────────────────────────

    /**
     * Extract the search field from an item based on the field name.
     * Returns the value as a lowercase string for case-insensitive comparison.
     */
    private String getField(LibraryItem item, String field) {
        switch (field.toLowerCase()) {
            case BY_TITLE:  return item.getTitle().toLowerCase();
            case BY_AUTHOR: return item.getAuthor().toLowerCase();
            case BY_TYPE:   return item.getItemType().toLowerCase();
            case BY_YEAR:   return String.valueOf(item.getYear());
            default:        return item.getTitle().toLowerCase();
        }
    }

    // ─── 1. Linear Search ─────────────────────────────────────────────────────

    /**
     * Linear Search — O(n) time complexity.
     * Iterates through every item and checks if the query matches.
     * Works on unsorted lists. Supports partial matches.
     *
     * Best used when: the list is not sorted, or the list is small.
     *
     * @param items  the full list of library items to search
     * @param query  the search term (partial match supported)
     * @param field  the field to search by (title, author, type, year)
     * @return list of matching items
     */
    public List<LibraryItem> linearSearch(List<LibraryItem> items, String query, String field) {
        List<LibraryItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (LibraryItem item : items) {
            String fieldValue = getField(item, field);
            if (fieldValue.contains(lowerQuery)) {
                results.add(item);
            }
        }
        return results;
    }

    // ─── 2. Binary Search ─────────────────────────────────────────────────────

    /**
     * Binary Search — O(log n) time complexity.
     * Requires the list to be sorted by the same field being searched.
     * Only returns EXACT matches (or items that start with the query).
     *
     * Best used when: the list is already sorted by the search field.
     *
     * @param sortedItems list that is already sorted by the search field
     * @param query       the search term (checks if field starts with query)
     * @param field       the field that the list is sorted by
     * @return list of matching items (may be empty)
     */
    public List<LibraryItem> binarySearch(List<LibraryItem> sortedItems, String query, String field) {
        List<LibraryItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        int low = 0;
        int high = sortedItems.size() - 1;

        // find ONE match using binary search
        int matchIndex = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            String midValue = getField(sortedItems.get(mid), field);

            if (midValue.startsWith(lowerQuery)) {
                matchIndex = mid;
                break; // found a match
            } else if (midValue.compareTo(lowerQuery) < 0) {
                low = mid + 1; // search right half
            } else {
                high = mid - 1; // search left half
            }
        }

        if (matchIndex == -1) return results; // no match found

        // expand left from matchIndex to catch all adjacent matches
        int left = matchIndex - 1;
        while (left >= 0 && getField(sortedItems.get(left), field).startsWith(lowerQuery)) {
            left--;
        }

        // expand right from matchIndex to catch all adjacent matches
        int right = matchIndex + 1;
        while (right < sortedItems.size() && getField(sortedItems.get(right), field).startsWith(lowerQuery)) {
            right++;
        }

        // collect all matches found between left+1 and right-1
        for (int i = left + 1; i < right; i++) {
            results.add(sortedItems.get(i));
        }

        return results;
    }

    // ─── 3. Recursive Search ─────────────────────────────────────────────────

    /**
     * Recursive Search — O(n) time, but implemented recursively.
     * Works exactly like linear search but uses recursion instead of a loop.
     * Demonstrates the required recursive algorithm from the assignment.
     *
     * Best used when: demonstrating recursion is required, or for nested data.
     *
     * @param items  the full list of library items
     * @param query  the search term (partial match supported)
     * @param field  the field to search by
     * @param index  the current index in recursion — always call with 0
     * @return list of matching items
     */
    public List<LibraryItem> recursiveSearch(List<LibraryItem> items, String query, String field, int index) {
        // base case: reached end of list
        if (index >= items.size()) {
            return new ArrayList<>();
        }

        // recursive call first to get results from the rest of the list
        List<LibraryItem> results = recursiveSearch(items, query, field, index + 1);

        // check current item and add to results if it matches
        String lowerQuery = query.toLowerCase().trim();
        String fieldValue = getField(items.get(index), field);
        if (fieldValue.contains(lowerQuery)) {
            results.add(0, items.get(index)); // add at front to maintain order
        }

        return results;
    }

    /**
     * Convenience overload — starts recursive search from index 0.
     */
    public List<LibraryItem> recursiveSearch(List<LibraryItem> items, String query, String field) {
        return recursiveSearch(items, query, field, 0);
    }

    // ─── Smart Search (Auto-selects algorithm) ────────────────────────────────

    /**
     * Automatically selects the best search algorithm based on context.
     * - If list is sorted by the field → use Binary Search
     * - Otherwise → use Linear Search
     *
     * @param items    the list of items
     * @param query    the search term
     * @param field    the field to search by
     * @param isSorted true if the list is already sorted by this field
     * @return list of matching items, and the algorithm name used
     */
    public SearchResult smartSearch(List<LibraryItem> items, String query, String field, boolean isSorted) {
        if (isSorted && !query.isEmpty()) {
            List<LibraryItem> results = binarySearch(items, query, field);
            return new SearchResult(results, "Binary Search");
        } else {
            List<LibraryItem> results = linearSearch(items, query, field);
            return new SearchResult(results, "Linear Search");
        }
    }

    // ─── SearchResult Wrapper ─────────────────────────────────────────────────

    /**
     * Wraps search results along with the algorithm name that was used.
     * The GUI can display both the results and which algorithm ran.
     */
    public static class SearchResult {
        private List<LibraryItem> items;
        private String algorithmUsed;

        public SearchResult(List<LibraryItem> items, String algorithmUsed) {
            this.items = items;
            this.algorithmUsed = algorithmUsed;
        }

        public List<LibraryItem> getItems() { return items; }
        public String getAlgorithmUsed() { return algorithmUsed; }
        public int getCount() { return items.size(); }

        @Override
        public String toString() {
            return algorithmUsed + " — " + items.size() + " result(s) found";
        }
    }
}
