package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SortEngine — implements all four sorting algorithms from scratch.
 * NO Java built-in sort (Collections.sort / Arrays.sort) is used anywhere.
 *
 * ✔ Selection Sort  — O(n²) — simple, finds minimum repeatedly
 * ✔ Insertion Sort  — O(n²) — efficient for small/nearly-sorted lists
 * ✔ Merge Sort      — O(n log n) — recommended by assignment, divide & conquer
 * ✔ Quick Sort      — O(n log n) average — partition-based
 *
 * Can sort by: title, author, or year.
 * GUI's Search & Sort Panel selects the algorithm via dropdown.
 */
public class SortEngine {

    // Sort field constants
    public static final String BY_TITLE  = "title";
    public static final String BY_AUTHOR = "author";
    public static final String BY_YEAR   = "year";
    public static final String BY_STATUS = "status";

    // Algorithm name constants (matches GUI dropdown labels)
    public static final String MERGE_SORT     = "Merge Sort";
    public static final String INSERTION_SORT = "Insertion Sort";
    public static final String SELECTION_SORT = "Selection Sort";
    public static final String QUICK_SORT     = "Quick Sort";

    // ─── Comparator Factory ───────────────────────────────────────────────────

    /**
     * Build a Comparator for LibraryItem based on the sort field.
     */
    private Comparator<LibraryItem> getComparator(String field) {
        switch (field.toLowerCase()) {
            case BY_TITLE:  return Comparator.comparing(i -> i.getTitle().toLowerCase());
            case BY_AUTHOR: return Comparator.comparing(i -> i.getAuthor().toLowerCase());
            case BY_YEAR:   return Comparator.comparingInt(LibraryItem::getYear);
            case BY_STATUS: return Comparator.comparing(i -> i.isAvailable() ? "Available" : "Borrowed");
            default:        return Comparator.comparing(i -> i.getTitle().toLowerCase());
        }
    }

    // ─── Public Entry Point ───────────────────────────────────────────────────

    /**
     * Sort a list of items by a given field using the chosen algorithm.
     * Returns a NEW sorted list (does not modify the original).
     *
     * @param items     the list to sort
     * @param field     the field to sort by (title, author, year, status)
     * @param algorithm the algorithm name (Merge Sort, Insertion Sort, etc.)
     * @return a new sorted list
     */
    public List<LibraryItem> sort(List<LibraryItem> items, String field, String algorithm) {
        // copy to array for sorting
        LibraryItem[] arr = items.toArray(new LibraryItem[0]);
        Comparator<LibraryItem> cmp = getComparator(field);

        switch (algorithm) {
            case MERGE_SORT:     mergeSort(arr, 0, arr.length - 1, cmp); break;
            case INSERTION_SORT: insertionSort(arr, cmp);                 break;
            case SELECTION_SORT: selectionSort(arr, cmp);                 break;
            case QUICK_SORT:     quickSort(arr, 0, arr.length - 1, cmp); break;
            default:             mergeSort(arr, 0, arr.length - 1, cmp); break;
        }

        // convert back to list and return
        List<LibraryItem> sorted = new ArrayList<>();
        for (LibraryItem item : arr) sorted.add(item);
        return sorted;
    }

    // ─── 1. Selection Sort ────────────────────────────────────────────────────

    /**
     * Selection Sort — O(n²) time, O(1) space.
     * On each pass, finds the smallest remaining element and places it in position.
     *
     * Good for: small lists, minimizing the number of swaps.
     */
    public void selectionSort(LibraryItem[] arr, Comparator<LibraryItem> cmp) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(arr[j], arr[minIdx]) < 0) {
                    minIdx = j;
                }
            }
            // swap arr[i] and arr[minIdx]
            LibraryItem temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
        }
    }

    // ─── 2. Insertion Sort ────────────────────────────────────────────────────

    /**
     * Insertion Sort — O(n²) worst case, O(n) best case (nearly sorted).
     * Builds a sorted portion from left to right by inserting each element.
     *
     * Good for: small lists, nearly-sorted data.
     */
    public void insertionSort(LibraryItem[] arr, Comparator<LibraryItem> cmp) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            LibraryItem key = arr[i];
            int j = i - 1;
            while (j >= 0 && cmp.compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ─── 3. Merge Sort ────────────────────────────────────────────────────────

    /**
     * Merge Sort — O(n log n) time, O(n) space.
     * Divides list in half, sorts each half, then merges them together.
     * Divide and conquer — also demonstrates recursion.
     *
     * Good for: large lists, guaranteed O(n log n) performance.
     * Recommended by the assignment.
     */
    public void mergeSort(LibraryItem[] arr, int left, int right, Comparator<LibraryItem> cmp) {
        if (left >= right) return; // base case: one or zero elements

        int mid = (left + right) / 2;
        mergeSort(arr, left, mid, cmp);       // sort left half (recursive)
        mergeSort(arr, mid + 1, right, cmp);  // sort right half (recursive)
        merge(arr, left, mid, right, cmp);    // merge the two halves
    }

    /**
     * Merge helper — combines two sorted halves into one sorted array.
     */
    private void merge(LibraryItem[] arr, int left, int mid, int right, Comparator<LibraryItem> cmp) {
        int leftSize  = mid - left + 1;
        int rightSize = right - mid;

        // create temp arrays
        LibraryItem[] leftArr  = new LibraryItem[leftSize];
        LibraryItem[] rightArr = new LibraryItem[rightSize];

        // copy data to temp arrays
        for (int i = 0; i < leftSize; i++)  leftArr[i]  = arr[left + i];
        for (int j = 0; j < rightSize; j++) rightArr[j] = arr[mid + 1 + j];

        // merge temp arrays back into arr
        int i = 0, j = 0, k = left;
        while (i < leftSize && j < rightSize) {
            if (cmp.compare(leftArr[i], rightArr[j]) <= 0) {
                arr[k++] = leftArr[i++];
            } else {
                arr[k++] = rightArr[j++];
            }
        }
        while (i < leftSize)  arr[k++] = leftArr[i++];
        while (j < rightSize) arr[k++] = rightArr[j++];
    }

    // ─── 4. Quick Sort ────────────────────────────────────────────────────────

    /**
     * Quick Sort — O(n log n) average, O(n²) worst case.
     * Partitions around a pivot and recursively sorts each partition.
     *
     * Good for: large lists in practice, cache-efficient.
     */
    public void quickSort(LibraryItem[] arr, int low, int high, Comparator<LibraryItem> cmp) {
        if (low < high) {
            int pivotIdx = partition(arr, low, high, cmp);
            quickSort(arr, low, pivotIdx - 1, cmp);  // sort left of pivot (recursive)
            quickSort(arr, pivotIdx + 1, high, cmp); // sort right of pivot (recursive)
        }
    }

    /**
     * Partition helper for Quick Sort.
     * Places pivot in its correct position, elements less than pivot go left,
     * elements greater go right.
     */
    private int partition(LibraryItem[] arr, int low, int high, Comparator<LibraryItem> cmp) {
        LibraryItem pivot = arr[high]; // use last element as pivot
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (cmp.compare(arr[j], pivot) <= 0) {
                i++;
                LibraryItem temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        // place pivot in correct position
        LibraryItem temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    // ─── Sort Result Wrapper ──────────────────────────────────────────────────

    /**
     * Wraps the sorted list and the algorithm name used.
     * Allows the GUI to display which algorithm was used.
     */
    public static class SortResult {
        private List<LibraryItem> sortedItems;
        private String algorithmUsed;
        private String fieldSortedBy;

        public SortResult(List<LibraryItem> sortedItems, String algorithmUsed, String fieldSortedBy) {
            this.sortedItems = sortedItems;
            this.algorithmUsed = algorithmUsed;
            this.fieldSortedBy = fieldSortedBy;
        }

        public List<LibraryItem> getSortedItems() { return sortedItems; }
        public String getAlgorithmUsed() { return algorithmUsed; }
        public String getFieldSortedBy() { return fieldSortedBy; }

        @Override
        public String toString() {
            return "Sorted by " + fieldSortedBy + " using " + algorithmUsed
                    + " — " + sortedItems.size() + " item(s)";
        }
    }

    /**
     * Sort and return a SortResult wrapper (includes algorithm name for GUI display).
     */
    public SortResult sortWithResult(List<LibraryItem> items, String field, String algorithm) {
        List<LibraryItem> sorted = sort(items, field, algorithm);
        return new SortResult(sorted, algorithm, field);
    }
}
