package gui;

import controller.*;
import model.*;
import utils.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    // Backend components
    private LibraryDatabase database;
    private LibraryManager libraryManager;
    private BorrowController borrowController;
    private SearchEngine searchEngine;
    private SortEngine sortEngine;

    // GUI components
    private JLabel statusLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columns = {"ID", "Title", "Author", "Type", "Year", "Status"};

    // Panels
    private JPanel viewPanel, borrowPanel, adminPanel, searchPanel, reportPanel;

    public MainWindow() {
        initComponents();
        initializeBackend();
        loadTableData();
        updateStatusBar();
    }

    private void initializeBackend() {
        database = new LibraryDatabase();
        libraryManager = new LibraryManager(database);
        borrowController = new BorrowController(database);
        searchEngine = new SearchEngine();
        sortEngine = new SortEngine();

        // Load data from files if exists
        List<LibraryItem> items = FileHandler.loadItems();
        for (LibraryItem item : items) {
            database.addItem(item);
        }

        java.util.HashMap<String, UserAccount> users = FileHandler.loadUsers();
        for (UserAccount user : users.values()) {
            database.addUser(user);
        }

        // Seed default data if empty
        if (database.getTotalItemCount() == 0) {
            FileHandler.seedDefaultData();
            List<LibraryItem> defaults = FileHandler.loadItems();
            for (LibraryItem item : defaults) {
                database.addItem(item);
            }
            loadTableData();
        }
    }

    private void initComponents() {
        setTitle("Smart Library Circulation System");
        setSize(1100, 650);
        setLayout(new BorderLayout());

        // SIDEBAR
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

        // Navigation Buttons
        JButton viewBtn = createNavButton("VIEW ITEMS", true);
        JButton borrowBtn = createNavButton("BORROW / RETURN", false);
        JButton adminBtn = createNavButton("ADMIN", false);
        JButton searchBtn = createNavButton("SEARCH&SORT", false);
        JButton reportBtn = createNavButton("REPORT", false);

        sidebarPanel.add(viewBtn);
        sidebarPanel.add(borrowBtn);
        sidebarPanel.add(adminBtn);
        sidebarPanel.add(searchBtn);
        sidebarPanel.add(reportBtn);

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(244, 246, 251));
        add(mainPanel, BorderLayout.CENTER);

        // TOP BAR
        JPanel topBar = new JPanel();
        topBar.setBackground(Color.WHITE);
        topBar.setLayout(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel pageTitle = new JLabel("Library Catalogue");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 14));
        pageTitle.setForeground(new Color(13, 31, 60));
        topBar.add(pageTitle, BorderLayout.WEST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // STATUS BAR
        JPanel statusBar = new JPanel();
        statusBar.setBackground(new Color(13, 31, 60));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));

        statusLabel = new JLabel("Total:0 | Available:0 | Borrowed:0");
        statusLabel.setForeground(new Color(150, 170, 200));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        statusBar.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        // CARD LAYOUT
        CardLayout cards = new CardLayout();
        JPanel contentPanel = new JPanel(cards);
        contentPanel.setBackground(new Color(244, 246, 251));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Create panels
        viewPanel = new JPanel(new BorderLayout());
        borrowPanel = new JPanel(new BorderLayout());
        adminPanel = new JPanel(new BorderLayout());
        searchPanel = new JPanel(new BorderLayout());
        reportPanel = new JPanel(new BorderLayout());

        contentPanel.add(viewPanel, "VIEW");
        contentPanel.add(borrowPanel, "BORROW");
        contentPanel.add(adminPanel, "ADMIN");
        contentPanel.add(searchPanel, "SEARCH");
        contentPanel.add(reportPanel, "REPORT");

        // Setup each panel
        setupViewPanel();
        setupBorrowPanel();
        setupAdminPanel();
        setupSearchPanel();
        setupReportPanel();

        // Event listeners for navigation
        viewBtn.addActionListener(e -> {
            cards.show(contentPanel, "VIEW");
            setPageTitle("Library Catalogue");
            loadTableData();
            updateStatusBar();
        });
        borrowBtn.addActionListener(e -> {
            cards.show(contentPanel, "BORROW");
            setPageTitle("Borrow / Return Items");
        });
        adminBtn.addActionListener(e -> {
            cards.show(contentPanel, "ADMIN");
            setPageTitle("Admin Panel");
            loadTableData();
            updateStatusBar();
        });
        searchBtn.addActionListener(e -> {
            cards.show(contentPanel, "SEARCH");
            setPageTitle("Search & Sort");
        });
        reportBtn.addActionListener(e -> {
            cards.show(contentPanel, "REPORT");
            setPageTitle("Reports");
            loadReportPanel();
        });
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        if (active) {
            btn.setBackground(new Color(40, 70, 110));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(13, 31, 61));
            btn.setForeground(new Color(150, 170, 200));
        }
        return btn;
    }

    private void setPageTitle(String title) {
        Component[] components = ((Container) getContentPane()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getLayout() instanceof BorderLayout) {
                    Component north = panel.getComponent(0);
                    if (north instanceof JPanel) {
                        Component[] northComps = ((JPanel) north).getComponents();
                        for (Component c : northComps) {
                            if (c instanceof JLabel) {
                                ((JLabel) c).setText(title);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupViewPanel() {
        viewPanel.setBackground(new Color(244, 246, 251));

        // Button row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonRow.setBackground(new Color(244, 246, 251));

        JButton addBtn = new JButton("+ Add Item");
        addBtn.setBackground(new Color(26, 58, 108));
        addBtn.setForeground(Color.WHITE);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(192, 57, 43));
        deleteBtn.setForeground(Color.WHITE);

        JButton undoBtn = new JButton("Undo");
        undoBtn.setBackground(new Color(232, 238, 245));
        undoBtn.setForeground(new Color(68, 85, 102));

        buttonRow.add(deleteBtn);
        buttonRow.add(addBtn);
        buttonRow.add(undoBtn);
        viewPanel.add(buttonRow, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(table);
        viewPanel.add(scrollPane, BorderLayout.CENTER);

        // Add button action
        addBtn.addActionListener(e -> showAddItemDialog());

        // Delete button action
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an item to delete.");
                return;
            }
            String id = (String) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete item " + id + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                libraryManager.removeItem(id);
                FileHandler.saveAll(database.getItemsRaw(), database.getAllUsers());
                loadTableData();
                updateStatusBar();
                JOptionPane.showMessageDialog(this, "Item deleted successfully.");
            }
        });

        // Undo button action
        undoBtn.addActionListener(e -> {
            String result = libraryManager.undoLastAction();
            if (result != null) {
                FileHandler.saveAll(database.getItemsRaw(), database.getAllUsers());
                loadTableData();
                updateStatusBar();
                JOptionPane.showMessageDialog(this, result);
            } else {
                JOptionPane.showMessageDialog(this, "Nothing to undo.");
            }
        });
    }

    private void setupBorrowPanel() {
        borrowPanel.setBackground(new Color(244, 246, 251));
        borrowPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(244, 246, 251));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel formTitle = new JLabel("Borrow / Return Items");
        formTitle.setFont(new Font("Arial", Font.BOLD, 16));
        formTitle.setForeground(new Color(13, 31, 60));
        formPanel.add(formTitle, gbc);

        // User ID field
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(15);
        formPanel.add(userIdField, gbc);

        // Item ID field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        JTextField itemIdField = new JTextField(15);
        formPanel.add(itemIdField, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        JButton borrowBtn = new JButton("Borrow Item");
        borrowBtn.setBackground(new Color(26, 122, 60));
        borrowBtn.setForeground(Color.WHITE);
        formPanel.add(borrowBtn, gbc);

        gbc.gridx = 1;
        JButton returnBtn = new JButton("Return Item");
        returnBtn.setBackground(new Color(230, 126, 34));
        returnBtn.setForeground(Color.WHITE);
        formPanel.add(returnBtn, gbc);

        // Result label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(resultLabel, gbc);

        borrowPanel.add(formPanel, gbc);

        // Borrow action
        borrowBtn.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String itemId = itemIdField.getText().trim();
            if (userId.isEmpty() || itemId.isEmpty()) {
                resultLabel.setText("Please enter both User ID and Item ID.");
                resultLabel.setForeground(Color.RED);
                return;
            }
            BorrowController.BorrowResult result = borrowController.borrowItem(userId, itemId);
            resultLabel.setText(result.getMessage());
            resultLabel.setForeground(result.isSuccess() ? new Color(26, 122, 60) : Color.RED);
            if (result.isSuccess()) {
                FileHandler.saveAll(database.getItemsRaw(), database.getAllUsers());
                updateStatusBar();
            }
        });

        // Return action
        returnBtn.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String itemId = itemIdField.getText().trim();
            if (userId.isEmpty() || itemId.isEmpty()) {
                resultLabel.setText("Please enter both User ID and Item ID.");
                resultLabel.setForeground(Color.RED);
                return;
            }
            BorrowController.BorrowResult result = borrowController.returnItem(userId, itemId);
            resultLabel.setText(result.getMessage());
            resultLabel.setForeground(result.isSuccess() ? new Color(26, 122, 60) : Color.RED);
            if (result.isSuccess()) {
                FileHandler.saveAll(database.getItemsRaw(), database.getAllUsers());
                updateStatusBar();
            }
        });
    }

    private void setupAdminPanel() {
        adminPanel.setBackground(new Color(244, 246, 251));
        adminPanel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(244, 246, 251));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("<html><h2>Admin Panel</h2>"
                + "<p>Use the VIEW panel to add, delete, or undo item operations.</p>"
                + "<p>Current Statistics:</p>"
                + "<ul>"
                + "<li>Total Items: <span id='total'>0</span></li>"
                + "<li>Available: <span id='available'>0</span></li>"
                + "<li>Borrowed: <span id='borrowed'>0</span></li>"
                + "</ul></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        infoPanel.add(infoLabel);
        adminPanel.add(infoPanel, BorderLayout.CENTER);
    }

    private void setupSearchPanel() {
        searchPanel.setBackground(new Color(244, 246, 251));
        searchPanel.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBackground(new Color(244, 246, 251));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        controlPanel.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(15);
        controlPanel.add(searchField);

        controlPanel.add(new JLabel("By:"));
        String[] searchBy = {"Title", "Author", "Type", "Year"};
        JComboBox<String> searchByCombo = new JComboBox<>(searchBy);
        controlPanel.add(searchByCombo);

        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(26, 58, 108));
        searchBtn.setForeground(Color.WHITE);
        controlPanel.add(searchBtn);

        controlPanel.add(Box.createHorizontalStrut(20));

        controlPanel.add(new JLabel("Sort by:"));
        String[] sortBy = {"Title", "Author", "Year"};
        JComboBox<String> sortByCombo = new JComboBox<>(sortBy);
        controlPanel.add(sortByCombo);

        controlPanel.add(new JLabel("Algorithm:"));
        String[] algorithms = {"Merge Sort", "Insertion Sort", "Selection Sort", "Quick Sort"};
        JComboBox<String> algoCombo = new JComboBox<>(algorithms);
        controlPanel.add(algoCombo);

        JButton sortBtn = new JButton("Sort");
        sortBtn.setBackground(new Color(26, 122, 60));
        sortBtn.setForeground(Color.WHITE);
        controlPanel.add(sortBtn);

        searchPanel.add(controlPanel, BorderLayout.NORTH);

        // Results table
        DefaultTableModel searchModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable searchTable = new JTable(searchModel);
        JScrollPane scrollPane = new JScrollPane(searchTable);
        searchPanel.add(scrollPane, BorderLayout.CENTER);

        // Search action
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            String field = searchByCombo.getSelectedItem().toString().toLowerCase();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a search term.");
                return;
            }
            List<LibraryItem> results = searchEngine.linearSearch(database.getAllItems(), query, field);
            searchModel.setRowCount(0);
            for (LibraryItem item : results) {
                searchModel.addRow(new Object[]{
                        item.getId(), item.getTitle(), item.getAuthor(),
                        item.getItemType(), item.getYear(),
                        item.isAvailable() ? "Available" : "Borrowed"
                });
            }
        });

        // Sort action
        sortBtn.addActionListener(e -> {
            String field = sortByCombo.getSelectedItem().toString().toLowerCase();
            String algorithm = algoCombo.getSelectedItem().toString();
            List<LibraryItem> sorted = sortEngine.sort(database.getAllItems(), field, algorithm);
            searchModel.setRowCount(0);
            for (LibraryItem item : sorted) {
                searchModel.addRow(new Object[]{
                        item.getId(), item.getTitle(), item.getAuthor(),
                        item.getItemType(), item.getYear(),
                        item.isAvailable() ? "Available" : "Borrowed"
                });
            }
        });
    }

    private void setupReportPanel() {
        reportPanel.setBackground(new Color(244, 246, 251));
        reportPanel.setLayout(new BorderLayout());
    }

    private void loadReportPanel() {
        reportPanel.removeAll();
        reportPanel.setLayout(new GridLayout(1, 2, 20, 20));
        reportPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Most borrowed panel
        JPanel borrowedPanel = new JPanel();
        borrowedPanel.setLayout(new BoxLayout(borrowedPanel, BoxLayout.Y_AXIS));
        borrowedPanel.setBorder(BorderFactory.createTitledBorder("Most Borrowed Items"));
        borrowedPanel.setBackground(new Color(244, 246, 251));

        List<LibraryItem> mostBorrowed = libraryManager.getMostBorrowedItems(5);
        for (LibraryItem item : mostBorrowed) {
            JLabel label = new JLabel(item.getTitle() + " (Borrowed: " + item.getBorrowCount() + " times)");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            borrowedPanel.add(label);
        }

        // Category distribution panel
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Category Distribution"));
        categoryPanel.setBackground(new Color(244, 246, 251));

        java.util.Map<String, Integer> distribution = libraryManager.getCategoryDistribution();
        for (java.util.Map.Entry<String, Integer> entry : distribution.entrySet()) {
            JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue() + " items");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            categoryPanel.add(label);
        }

        reportPanel.add(borrowedPanel);
        reportPanel.add(categoryPanel);
        reportPanel.revalidate();
        reportPanel.repaint();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        for (LibraryItem item : database.getAllItems()) {
            tableModel.addRow(new Object[]{
                    item.getId(), item.getTitle(), item.getAuthor(),
                    item.getItemType(), item.getYear(),
                    item.isAvailable() ? "Available" : "Borrowed"
            });
        }
    }

    private void updateStatusBar() {
        int total = database.getTotalItemCount();
        int available = database.getAvailableCount();
        int borrowed = database.getBorrowedCount();
        statusLabel.setText("Total: " + total + " | Available: " + available + " | Borrowed: " + borrowed);
    }

    private void showAddItemDialog() {
        String[] types = {"Book", "Magazine", "Journal"};
        String selectedType = (String) JOptionPane.showInputDialog(this,
                "Select item type:", "Add New Item",
                JOptionPane.PLAIN_MESSAGE, null, types, types[0]);

        if (selectedType == null) return;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(15);
        JTextField titleField = new JTextField(15);
        JTextField authorField = new JTextField(15);
        JTextField yearField = new JTextField(5);
        JTextField extraField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; panel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; panel.add(yearField, gbc);

        String extraLabel = selectedType.equals("Book") ? "Genre:" :
                           selectedType.equals("Magazine") ? "Issue Number:" : "Volume:";
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel(extraLabel), gbc);
        gbc.gridx = 1; panel.add(extraField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add " + selectedType, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText().trim();
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                boolean added = false;
                if (selectedType.equals("Book")) {
                    added = libraryManager.addBook(id, title, author, year, extraField.getText().trim());
                } else if (selectedType.equals("Magazine")) {
                    added = libraryManager.addMagazine(id, title, author, year,
                            Integer.parseInt(extraField.getText().trim()));
                } else {
                    added = libraryManager.addJournal(id, title, author, year,
                            Integer.parseInt(extraField.getText().trim()));
                }

                if (added) {
                    FileHandler.saveAll(database.getItemsRaw(), database.getAllUsers());
                    loadTableData();
                    updateStatusBar();
                    JOptionPane.showMessageDialog(this, selectedType + " added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Item with this ID already exists.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for Year and " +
                        (selectedType.equals("Book") ? "Genre" : "Issue/Volume") + ".");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
