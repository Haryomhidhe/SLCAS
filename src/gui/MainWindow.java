package gui;

import controller.BorrowController;
import controller.SearchEngine;
import controller.SortEngine;
import controller.LibraryManager;
import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;
import utils.IDGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {

    // ─── Backend instances ────────────────────────────────────────────────────
    private LibraryDatabase  database;
    private LibraryManager   libraryManager;
    private BorrowController borrowController;
    private SearchEngine     searchEngine;
    private SortEngine       sortEngine;

    // ─── Shared table models ──────────────────────────────────────────────────
    private DefaultTableModel viewTableModel;
    private DefaultTableModel adminTableModel;
    private DefaultTableModel searchTableModel;
    private DefaultTableModel mostBorrowedModel;
    private DefaultTableModel overdueModel;
    private DefaultTableModel categoryModel;

    // ─── Shared UI references ─────────────────────────────────────────────────
    private JLabel statusLabel;
    private String selectedAlgo = "Merge Sort";

    public MainWindow() {
        database         = new LibraryDatabase();
        libraryManager   = new LibraryManager(database);
        borrowController = new BorrowController(database);
        searchEngine     = new SearchEngine();
        sortEngine       = new SortEngine();

        // Sample data so tables aren't empty on launch
        libraryManager.addBook("BK-001", "Clean Code", "Robert Martin", 2008, "Programming");
        libraryManager.addBook("BK-002", "The Pragmatic Programmer", "David Thomas", 1999, "Programming");
        libraryManager.addMagazine("MG-001", "National Geographic", "Various", 2023, 201);
        libraryManager.addJournal("JN-001", "Nature", "Various", 2022, 45);
        database.addUser(new UserAccount("U-001", "Ayomide", "ayomide@miva.edu.ng"));

        initComponents();
    }

    private void initComponents() {
        setTitle("Smart Library Circulation System");
        setSize(1100, 650);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ─── SIDEBAR ──────────────────────────────────────────────────────────
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(13, 31, 60));
        sidebarPanel.setPreferredSize(new Dimension(190, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        add(sidebarPanel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("SLCAS");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(16, 14, 4, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Smart Library System");
        subtitleLabel.setForeground(new Color(150, 170, 200));
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(subtitleLabel);

        JButton viewBtn   = makeSidebarBtn("VIEW ITEMS",      true);
        JButton borrowBtn = makeSidebarBtn("BORROW / RETURN", false);
        JButton adminBtn  = makeSidebarBtn("ADMIN",           false);
        JButton searchBtn = makeSidebarBtn("SEARCH & SORT",   false);
        JButton reportBtn = makeSidebarBtn("REPORT",          false);
        sidebarPanel.add(viewBtn);
        sidebarPanel.add(borrowBtn);
        sidebarPanel.add(adminBtn);
        sidebarPanel.add(searchBtn);
        sidebarPanel.add(reportBtn);

        // ─── MAIN PANEL ───────────────────────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(244, 246, 251));
        add(mainPanel, BorderLayout.CENTER);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel pageTitle = new JLabel("Library Catalogue");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 14));
        pageTitle.setForeground(new Color(13, 31, 60));
        topBar.add(pageTitle, BorderLayout.WEST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(13, 31, 60));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));
        statusLabel = new JLabel("Total: 0  |  Available: 0  |  Borrowed: 0");
        statusLabel.setForeground(new Color(150, 170, 200));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusBar.add(statusLabel);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        CardLayout cards = new CardLayout();
        JPanel contentPanel = new JPanel(cards);
        contentPanel.setBackground(new Color(244, 246, 251));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // ═════════════════════════════════════════════════════════════════════
        // VIEW PANEL
        // ═════════════════════════════════════════════════════════════════════
        JPanel viewPanel = new JPanel(new BorderLayout());
        viewPanel.setBackground(new Color(244, 246, 251));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonRow.setBackground(new Color(244, 246, 251));
        JButton addBtn        = makeBtn("+ Add Item",    new Color(26, 58, 108),   Color.WHITE);
        JButton borrowItemBtn = makeBtn("Borrow",        new Color(26, 122, 60),   Color.WHITE);
        JButton returnItemBtn = makeBtn("Return Item",   new Color(230, 126, 34),  Color.WHITE);
        JButton deleteBtn     = makeBtn("Delete",        new Color(192, 57, 43),   Color.WHITE);
        JButton undoBtn       = makeBtn("Undo",          new Color(232, 238, 245), new Color(68, 85, 102));
        buttonRow.add(addBtn); buttonRow.add(borrowItemBtn); buttonRow.add(returnItemBtn);
        buttonRow.add(deleteBtn); buttonRow.add(undoBtn);
        viewPanel.add(buttonRow, BorderLayout.NORTH);

        viewTableModel = new DefaultTableModel(new String[]{"ID","Title","Author","Type","Year","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable viewTable = new JTable(viewTableModel);
        styleTable(viewTable);
        viewPanel.add(new JScrollPane(viewTable), BorderLayout.CENTER);

        // Add Item dialog
        addBtn.addActionListener(e -> {
            String[] types = {"Book", "Magazine", "Journal"};
            String type = (String) JOptionPane.showInputDialog(this, "Select item type:",
                    "Add Item", JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
            if (type == null) return;
            String title  = JOptionPane.showInputDialog(this, "Title:");
            if (title == null || title.trim().isEmpty()) return;
            String author = JOptionPane.showInputDialog(this, "Author:");
            if (author == null || author.trim().isEmpty()) return;
            String yearStr = JOptionPane.showInputDialog(this, "Year:");
            if (yearStr == null || yearStr.trim().isEmpty()) return;
            try {
                int year = Integer.parseInt(yearStr.trim());
                List<LibraryItem> all = libraryManager.getAllItems();
                boolean added = false;
                if ("Book".equals(type)) {
                    String genre = JOptionPane.showInputDialog(this, "Genre:");
                    if (genre == null) return;
                    added = libraryManager.addBook(IDGenerator.generateBookId(all), title.trim(), author.trim(), year, genre.trim());
                } else if ("Magazine".equals(type)) {
                    String iss = JOptionPane.showInputDialog(this, "Issue Number:");
                    if (iss == null) return;
                    added = libraryManager.addMagazine(IDGenerator.generateMagazineId(all), title.trim(), author.trim(), year, Integer.parseInt(iss.trim()));
                } else {
                    String vol = JOptionPane.showInputDialog(this, "Volume:");
                    if (vol == null) return;
                    added = libraryManager.addJournal(IDGenerator.generateJournalId(all), title.trim(), author.trim(), year, Integer.parseInt(vol.trim()));
                }
                if (added) { refreshAllTables(); JOptionPane.showMessageDialog(this, "Item added!"); }
                else JOptionPane.showMessageDialog(this, "Failed — duplicate ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year and number fields must be numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = viewTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an item to delete."); return; }
            String id = (String) viewTableModel.getValueAt(row, 0);
            int ok = JOptionPane.showConfirmDialog(this, "Delete " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { libraryManager.removeItem(id); refreshAllTables(); }
        });

        undoBtn.addActionListener(e -> {
            String r = libraryManager.undoLastAction();
            JOptionPane.showMessageDialog(this, r != null ? r : "Nothing to undo.");
            refreshAllTables();
        });

        borrowItemBtn.addActionListener(e -> {
            int row = viewTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an item first."); return; }
            String itemId = (String) viewTableModel.getValueAt(row, 0);
            String userId = JOptionPane.showInputDialog(this, "Enter Student ID:");
            if (userId == null || userId.trim().isEmpty()) return;
            BorrowController.BorrowResult res = borrowController.borrowItem(userId.trim(), itemId);
            JOptionPane.showMessageDialog(this, res.getMessage());
            refreshAllTables();
        });

        returnItemBtn.addActionListener(e -> {
            int row = viewTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an item first."); return; }
            String itemId = (String) viewTableModel.getValueAt(row, 0);
            String userId = JOptionPane.showInputDialog(this, "Enter Student ID:");
            if (userId == null || userId.trim().isEmpty()) return;
            BorrowController.BorrowResult res = borrowController.returnItem(userId.trim(), itemId);
            JOptionPane.showMessageDialog(this, res.getMessage());
            refreshAllTables();
        });

        // ═════════════════════════════════════════════════════════════════════
        // BORROW / RETURN PANEL
        // ═════════════════════════════════════════════════════════════════════
        JPanel borrowPanel = new JPanel(new BorderLayout());
        borrowPanel.setBackground(new Color(244, 246, 251));
        JPanel borrowForms = new JPanel();
        borrowForms.setLayout(new BoxLayout(borrowForms, BoxLayout.Y_AXIS));
        borrowForms.setBackground(new Color(244, 246, 251));
        borrowForms.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Borrow form
        JPanel borrowForm = new JPanel(new GridBagLayout());
        borrowForm.setBackground(Color.WHITE);
        borrowForm.setBorder(BorderFactory.createTitledBorder("Borrow Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField borrowUserField = new JTextField(20);
        JTextField borrowItemField = new JTextField(20);
        JTextField borrowDueField  = new JTextField("YYYY-MM-DD", 20);
        JButton confirmBorrowBtn   = makeBtn("Confirm Borrow", new Color(26, 122, 60), Color.WHITE);
        JLabel borrowResultLabel   = new JLabel(" ");

        gbc.gridx=0; gbc.gridy=0; borrowForm.add(new JLabel("Student ID:"), gbc);
        gbc.gridx=1;              borrowForm.add(borrowUserField, gbc);
        gbc.gridx=0; gbc.gridy=1; borrowForm.add(new JLabel("Item ID:"), gbc);
        gbc.gridx=1;              borrowForm.add(borrowItemField, gbc);
        gbc.gridx=0; gbc.gridy=2; borrowForm.add(new JLabel("Due Date:"), gbc);
        gbc.gridx=1;              borrowForm.add(borrowDueField, gbc);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; borrowForm.add(confirmBorrowBtn, gbc);
        gbc.gridy=4;              borrowForm.add(borrowResultLabel, gbc);

        confirmBorrowBtn.addActionListener(e -> {
            String userId = borrowUserField.getText().trim();
            String itemId = borrowItemField.getText().trim();
            String dateStr = borrowDueField.getText().trim();
            if (userId.isEmpty() || itemId.isEmpty()) {
                borrowResultLabel.setForeground(Color.RED);
                borrowResultLabel.setText("Student ID and Item ID are required.");
                return;
            }
            try {
                LocalDate due = dateStr.equals("YYYY-MM-DD") || dateStr.isEmpty() ? null : LocalDate.parse(dateStr);
                BorrowController.BorrowResult res = borrowController.borrowItem(userId, itemId, due);
                borrowResultLabel.setForeground(res.isSuccess() ? new Color(26,122,60) : Color.RED);
                borrowResultLabel.setText(res.getMessage());
                refreshAllTables();
                if (res.isSuccess()) { borrowUserField.setText(""); borrowItemField.setText(""); borrowDueField.setText("YYYY-MM-DD"); }
            } catch (DateTimeParseException ex) {
                borrowResultLabel.setForeground(Color.RED);
                borrowResultLabel.setText("Invalid date. Use YYYY-MM-DD.");
            }
        });

        // Return form
        JPanel returnForm = new JPanel(new GridBagLayout());
        returnForm.setBackground(Color.WHITE);
        returnForm.setBorder(BorderFactory.createTitledBorder("Return Item"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(8, 10, 8, 10);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JTextField returnItemField = new JTextField(20);
        JTextField returnUserField = new JTextField(20);
        JButton confirmReturnBtn   = makeBtn("Confirm Return", new Color(230, 126, 34), Color.WHITE);
        JLabel returnResultLabel   = new JLabel(" ");

        gbc2.gridx=0; gbc2.gridy=0; returnForm.add(new JLabel("Item ID:"), gbc2);
        gbc2.gridx=1;               returnForm.add(returnItemField, gbc2);
        gbc2.gridx=0; gbc2.gridy=1; returnForm.add(new JLabel("Student ID:"), gbc2);
        gbc2.gridx=1;               returnForm.add(returnUserField, gbc2);
        gbc2.gridx=0; gbc2.gridy=2; gbc2.gridwidth=2; returnForm.add(confirmReturnBtn, gbc2);
        gbc2.gridy=3;               returnForm.add(returnResultLabel, gbc2);

        confirmReturnBtn.addActionListener(e -> {
            String itemId = returnItemField.getText().trim();
            String userId = returnUserField.getText().trim();
            if (itemId.isEmpty() || userId.isEmpty()) {
                returnResultLabel.setForeground(Color.RED);
                returnResultLabel.setText("Both fields are required.");
                return;
            }
            BorrowController.BorrowResult res = borrowController.returnItem(userId, itemId);
            returnResultLabel.setForeground(res.isSuccess() ? new Color(230,126,34) : Color.RED);
            returnResultLabel.setText(res.getMessage());
            refreshAllTables();
            if (res.isSuccess()) { returnItemField.setText(""); returnUserField.setText(""); }
        });

        borrowForms.add(borrowForm);
        borrowForms.add(Box.createRigidArea(new Dimension(0, 20)));
        borrowForms.add(returnForm);
        borrowPanel.add(new JScrollPane(borrowForms), BorderLayout.CENTER);

        // ═════════════════════════════════════════════════════════════════════
        // ADMIN PANEL
        // ═════════════════════════════════════════════════════════════════════
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(new Color(244, 246, 251));

        JPanel adminContent = new JPanel(new GridBagLayout());
        adminContent.setBackground(Color.WHITE);
        adminContent.setBorder(BorderFactory.createTitledBorder("Add New Item"));
        GridBagConstraints agbc = new GridBagConstraints();
        agbc.insets = new Insets(8, 10, 8, 10);
        agbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField adminTitleField  = new JTextField(20);
        JTextField adminAuthorField = new JTextField(20);
        JTextField adminYearField   = new JTextField(20);
        JTextField adminExtraField  = new JTextField(20);
        JLabel     adminExtraLabel  = new JLabel("Genre:");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
        JButton adminAddBtn    = makeBtn("Add Item",         new Color(26, 58, 108),   Color.WHITE);
        JButton adminDeleteBtn = makeBtn("Delete Selected",  new Color(192, 57, 43),   Color.WHITE);
        JButton adminUndoBtn   = makeBtn("Undo Last Action", new Color(232, 238, 245), new Color(68, 85, 102));
        JLabel adminResultLabel = new JLabel(" ");

        typeCombo.addActionListener(e -> {
            String s = (String) typeCombo.getSelectedItem();
            adminExtraLabel.setText("Magazine".equals(s) ? "Issue #:" : "Journal".equals(s) ? "Volume:" : "Genre:");
        });

        agbc.gridx=0; agbc.gridy=0; adminContent.add(new JLabel("Type:"), agbc);
        agbc.gridx=1;               adminContent.add(typeCombo, agbc);
        agbc.gridx=0; agbc.gridy=1; adminContent.add(new JLabel("Title:"), agbc);
        agbc.gridx=1;               adminContent.add(adminTitleField, agbc);
        agbc.gridx=0; agbc.gridy=2; adminContent.add(new JLabel("Author:"), agbc);
        agbc.gridx=1;               adminContent.add(adminAuthorField, agbc);
        agbc.gridx=0; agbc.gridy=3; adminContent.add(new JLabel("Year:"), agbc);
        agbc.gridx=1;               adminContent.add(adminYearField, agbc);
        agbc.gridx=0; agbc.gridy=4; adminContent.add(adminExtraLabel, agbc);
        agbc.gridx=1;               adminContent.add(adminExtraField, agbc);
        agbc.gridx=0; agbc.gridy=5; agbc.gridwidth=2;
        JPanel adminBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        adminBtnRow.setBackground(Color.WHITE);
        adminBtnRow.add(adminAddBtn); adminBtnRow.add(adminDeleteBtn); adminBtnRow.add(adminUndoBtn);
        adminContent.add(adminBtnRow, agbc);
        agbc.gridy=6; adminContent.add(adminResultLabel, agbc);

        adminTableModel = new DefaultTableModel(new String[]{"ID","Title","Author","Type","Year","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable adminTable = new JTable(adminTableModel);
        styleTable(adminTable);

        adminAddBtn.addActionListener(e -> {
            String title  = adminTitleField.getText().trim();
            String author = adminAuthorField.getText().trim();
            String yearStr = adminYearField.getText().trim();
            String extra  = adminExtraField.getText().trim();
            String type   = (String) typeCombo.getSelectedItem();
            if (title.isEmpty() || author.isEmpty() || yearStr.isEmpty() || extra.isEmpty()) {
                adminResultLabel.setForeground(Color.RED);
                adminResultLabel.setText("All fields are required.");
                return;
            }
            try {
                int year = Integer.parseInt(yearStr);
                List<LibraryItem> all = libraryManager.getAllItems();
                boolean added = false;
                if ("Book".equals(type))     added = libraryManager.addBook(IDGenerator.generateBookId(all), title, author, year, extra);
                else if ("Magazine".equals(type)) added = libraryManager.addMagazine(IDGenerator.generateMagazineId(all), title, author, year, Integer.parseInt(extra));
                else                         added = libraryManager.addJournal(IDGenerator.generateJournalId(all), title, author, year, Integer.parseInt(extra));
                if (added) {
                    adminResultLabel.setForeground(new Color(26, 122, 60));
                    adminResultLabel.setText("Added successfully!");
                    adminTitleField.setText(""); adminAuthorField.setText("");
                    adminYearField.setText("");  adminExtraField.setText("");
                    refreshAllTables();
                } else {
                    adminResultLabel.setForeground(Color.RED);
                    adminResultLabel.setText("Failed — duplicate ID.");
                }
            } catch (NumberFormatException ex) {
                adminResultLabel.setForeground(Color.RED);
                adminResultLabel.setText("Year and number fields must be numbers.");
            }
        });

        adminDeleteBtn.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row == -1) { adminResultLabel.setText("Select an item to delete."); return; }
            String id = (String) adminTableModel.getValueAt(row, 0);
            libraryManager.removeItem(id);
            adminResultLabel.setForeground(new Color(192, 57, 43));
            adminResultLabel.setText("Deleted: " + id);
            refreshAllTables();
        });

        adminUndoBtn.addActionListener(e -> {
            String r = libraryManager.undoLastAction();
            adminResultLabel.setForeground(new Color(26, 58, 108));
            adminResultLabel.setText(r != null ? r : "Nothing to undo.");
            refreshAllTables();
        });

        JPanel adminWrapper = new JPanel(new BorderLayout());
        adminWrapper.setBackground(new Color(244, 246, 251));
        adminWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        adminWrapper.add(adminContent, BorderLayout.NORTH);
        adminWrapper.add(new JScrollPane(adminTable), BorderLayout.CENTER);
        adminPanel.add(adminWrapper, BorderLayout.CENTER);

        // ═════════════════════════════════════════════════════════════════════
        // SEARCH & SORT PANEL
        // ═════════════════════════════════════════════════════════════════════
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(244, 246, 251));

        JPanel searchWrapper = new JPanel();
        searchWrapper.setLayout(new BoxLayout(searchWrapper, BoxLayout.Y_AXIS));
        searchWrapper.setBackground(new Color(244, 246, 251));
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel searchForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchForm.setBackground(Color.WHITE);
        searchForm.setBorder(BorderFactory.createTitledBorder("Search"));
        JComboBox<String> searchByCombo = new JComboBox<>(new String[]{"Title","Author","Type","Year"});
        JTextField searchField = new JTextField(20);
        JButton searchGoBtn = makeBtn("Search", new Color(26, 58, 108), Color.WHITE);
        JLabel searchAlgoLabel = new JLabel(" ");
        searchAlgoLabel.setForeground(new Color(100, 100, 100));
        searchForm.add(new JLabel("Search By:")); searchForm.add(searchByCombo);
        searchForm.add(searchField); searchForm.add(searchGoBtn); searchForm.add(searchAlgoLabel);

        JPanel sortForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        sortForm.setBackground(Color.WHITE);
        sortForm.setBorder(BorderFactory.createTitledBorder("Sort"));
        JButton mergeSortBtn     = makeBtn("Merge Sort",     new Color(13, 31, 60),  Color.WHITE);
        JButton insertionSortBtn = makeBtn("Insertion Sort", new Color(26, 58, 108), Color.WHITE);
        JButton selectionSortBtn = makeBtn("Selection Sort", new Color(26, 58, 108), Color.WHITE);
        JButton quickSortBtn     = makeBtn("Quick Sort",     new Color(26, 58, 108), Color.WHITE);
        JComboBox<String> sortByCombo = new JComboBox<>(new String[]{"Title","Author","Year","Status"});
        JButton applySortBtn = makeBtn("Apply Sort", new Color(26, 122, 60), Color.WHITE);
        JLabel sortAlgoLabel = new JLabel("Algorithm: Merge Sort");
        sortAlgoLabel.setForeground(new Color(100, 100, 100));

        ActionListener algoListener = e -> {
            mergeSortBtn.setBackground(new Color(26, 58, 108));
            insertionSortBtn.setBackground(new Color(26, 58, 108));
            selectionSortBtn.setBackground(new Color(26, 58, 108));
            quickSortBtn.setBackground(new Color(26, 58, 108));
            JButton clicked = (JButton) e.getSource();
            clicked.setBackground(new Color(13, 31, 60));
            selectedAlgo = clicked.getText();
            sortAlgoLabel.setText("Algorithm: " + selectedAlgo);
        };
        mergeSortBtn.addActionListener(algoListener);
        insertionSortBtn.addActionListener(algoListener);
        selectionSortBtn.addActionListener(algoListener);
        quickSortBtn.addActionListener(algoListener);

        sortForm.add(new JLabel("Sort By:")); sortForm.add(sortByCombo);
        sortForm.add(mergeSortBtn); sortForm.add(insertionSortBtn);
        sortForm.add(selectionSortBtn); sortForm.add(quickSortBtn);
        sortForm.add(applySortBtn); sortForm.add(sortAlgoLabel);

        searchTableModel = new DefaultTableModel(new String[]{"ID","Title","Author","Type","Year","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable searchTable = new JTable(searchTableModel);
        styleTable(searchTable);

        searchGoBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            String field = ((String) searchByCombo.getSelectedItem()).toLowerCase();
            SearchEngine.SearchResult res = searchEngine.smartSearch(libraryManager.getAllItems(), query, field, false);
            populateTable(searchTableModel, res.getItems());
            searchAlgoLabel.setText("  Used: " + res.getAlgorithmUsed() + "  |  " + res.getCount() + " result(s)");
        });

        applySortBtn.addActionListener(e -> {
            String field = ((String) sortByCombo.getSelectedItem()).toLowerCase();
            List<LibraryItem> sorted = sortEngine.sort(libraryManager.getAllItems(), field, selectedAlgo);
            populateTable(searchTableModel, sorted);
        });

        searchWrapper.add(searchForm);
        searchWrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        searchWrapper.add(sortForm);
        searchPanel.add(searchWrapper, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        // ═════════════════════════════════════════════════════════════════════
        // REPORTS PANEL
        // ═════════════════════════════════════════════════════════════════════
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBackground(new Color(244, 246, 251));

        JPanel reportCards = new JPanel(new GridLayout(1, 3, 16, 0));
        reportCards.setBackground(new Color(244, 246, 251));
        reportCards.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel mostBorrowedCard = makeReportCard("Most Borrowed Items");
        mostBorrowedModel = new DefaultTableModel(new String[]{"Title","Borrows"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        mostBorrowedCard.add(new JScrollPane(new JTable(mostBorrowedModel)), BorderLayout.CENTER);
        mostBorrowedCard.add(makeBtn("Export", new Color(26, 58, 108), Color.WHITE), BorderLayout.SOUTH);

        JPanel overdueCard = makeReportCard("Overdue Items");
        overdueModel = new DefaultTableModel(new String[]{"User","Item ID","Due Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        overdueCard.add(new JScrollPane(new JTable(overdueModel)), BorderLayout.CENTER);
        overdueCard.add(makeBtn("Export", new Color(192, 57, 43), Color.WHITE), BorderLayout.SOUTH);

        JPanel categoryCard = makeReportCard("Category Distribution");
        categoryModel = new DefaultTableModel(new String[]{"Type","Count"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        categoryCard.add(new JScrollPane(new JTable(categoryModel)), BorderLayout.CENTER);
        categoryCard.add(makeBtn("Export", new Color(26, 122, 60), Color.WHITE), BorderLayout.SOUTH);

        JButton refreshReportBtn = makeBtn("Refresh Reports", new Color(13, 31, 60), Color.WHITE);
        JPanel refreshRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        refreshRow.setBackground(new Color(244, 246, 251));
        refreshRow.add(refreshReportBtn);
        refreshReportBtn.addActionListener(e -> refreshReports());

        reportCards.add(mostBorrowedCard); reportCards.add(overdueCard); reportCards.add(categoryCard);
        reportPanel.add(refreshRow, BorderLayout.NORTH);
        reportPanel.add(reportCards, BorderLayout.CENTER);

        // ─── ADD CARDS ────────────────────────────────────────────────────────
        contentPanel.add(viewPanel,   "VIEW");
        contentPanel.add(borrowPanel, "BORROW");
        contentPanel.add(adminPanel,  "ADMIN");
        contentPanel.add(searchPanel, "SEARCH");
        contentPanel.add(reportPanel, "REPORT");

        // ─── NAV LISTENERS ────────────────────────────────────────────────────
        viewBtn.addActionListener(e   -> { cards.show(contentPanel,"VIEW");   pageTitle.setText("Library Catalogue"); });
        borrowBtn.addActionListener(e -> { cards.show(contentPanel,"BORROW"); pageTitle.setText("Borrow / Return"); });
        adminBtn.addActionListener(e  -> { cards.show(contentPanel,"ADMIN");  pageTitle.setText("Admin — Manage Items"); });
        searchBtn.addActionListener(e -> { cards.show(contentPanel,"SEARCH"); pageTitle.setText("Search & Sort"); });
        reportBtn.addActionListener(e -> { cards.show(contentPanel,"REPORT"); pageTitle.setText("Reports"); refreshReports(); });

        refreshAllTables();
    }

    // ─── Refresh all tables + status bar ──────────────────────────────────────
    private void refreshAllTables() {
        List<LibraryItem> all = libraryManager.getAllItems();
        populateTable(viewTableModel, all);
        populateTable(adminTableModel, all);
        populateTable(searchTableModel, all);
        statusLabel.setText("Total: " + libraryManager.getTotalItemCount()
                + "  |  Available: " + libraryManager.getAvailableCount()
                + "  |  Borrowed: "  + libraryManager.getBorrowedCount());
    }

    private void populateTable(DefaultTableModel model, List<LibraryItem> items) {
        model.setRowCount(0);
        for (LibraryItem item : items) {
            model.addRow(new Object[]{
                item.getId(), item.getTitle(), item.getAuthor(),
                item.getItemType(), item.getYear(),
                item.isAvailable() ? "Available" : "Borrowed"
            });
        }
    }

    private void refreshReports() {
        mostBorrowedModel.setRowCount(0);
        for (LibraryItem item : libraryManager.getMostBorrowedItems(5))
            mostBorrowedModel.addRow(new Object[]{item.getTitle(), item.getBorrowCount()});

        overdueModel.setRowCount(0);
        for (UserAccount user : libraryManager.getUsersWithOverdueItems())
            for (String itemId : user.getOverdueItems())
                overdueModel.addRow(new Object[]{user.getName(), itemId, user.getDueDate(itemId)});

        categoryModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : libraryManager.getCategoryDistribution().entrySet())
            categoryModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(13, 31, 60));
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private JButton makeSidebarBtn(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setBackground(active ? new Color(40, 70, 110) : new Color(13, 31, 61));
        btn.setForeground(active ? Color.WHITE : new Color(150, 170, 200));
        return btn;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        return btn;
    }

    private JPanel makeReportCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder(title));
        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
