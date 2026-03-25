package gui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        initComponents();
    }

    private void initComponents() {
        // you write everything here
          setTitle("Smart Liberary circulation system");
          setSize(1100, 650);
          setLayout(new BorderLayout());
          
          JPanel sidebarPanel = new JPanel();
          sidebarPanel.setBackground(new Color(13, 31, 60));
          sidebarPanel.setPreferredSize(new Dimension(190, 0));
          sidebarPanel.setLayout(new BoxLayout(sidebarPanel,BoxLayout.Y_AXIS));
          add(sidebarPanel, BorderLayout.WEST);
          JLabel titleLabel =  new JLabel("SLCAS");
          titleLabel.setForeground(Color.WHITE);
          titleLabel.setFont(new Font("Arial",Font.BOLD,14));
          titleLabel.setBorder(BorderFactory.createEmptyBorder(16,14,4,14));
          titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
          sidebarPanel.add(titleLabel);
          JLabel subtitleLabel = new JLabel("Smart Library System");
          subtitleLabel.setForeground(new Color(150,170,200));
          subtitleLabel.setFont(new Font("Arial", Font.PLAIN,10));
          subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
          subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
          sidebarPanel.add(subtitleLabel);
          
          //navigation Buttons
            //viewBtn
            JButton viewBtn = new JButton("VIEW ITEMS");
                viewBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                viewBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                viewBtn.setBorderPainted(false);
                viewBtn.setFocusPainted(false);
                viewBtn.setHorizontalAlignment(SwingConstants.LEFT);
                viewBtn.setFont(new Font("Arial", Font.PLAIN, 11));
                viewBtn.setBackground(new Color(40, 70, 110)); // active colour
                viewBtn.setForeground(Color.WHITE);
                sidebarPanel.add(viewBtn);
            
            //BORROWBtn
            JButton borrowBtn = new JButton("BORROW / RETURN");
                borrowBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
                borrowBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                borrowBtn.setBorderPainted(false);
                borrowBtn.setFocusPainted(false);
                borrowBtn.setHorizontalAlignment(SwingConstants.LEFT);
                borrowBtn.setFont(new Font("Arial", Font.PLAIN,11));
                borrowBtn.setBackground(new Color(13,31,61)); 
                borrowBtn.setForeground(new Color(150,170,200));
                sidebarPanel.add(borrowBtn);
            
            //ADMINBtn
              JButton adminBtn = new JButton("ADMIN");
                adminBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
                adminBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                adminBtn.setBorderPainted(false);
                adminBtn.setFocusPainted(false);
                adminBtn.setHorizontalAlignment(SwingConstants.LEFT);
                adminBtn.setFont(new Font("Arial", Font.PLAIN,11));
                adminBtn.setBackground(new Color(13,31,61)); 
                adminBtn.setForeground(new Color(150,170,200));
                sidebarPanel.add(adminBtn);
                
            //Search&sortBtn
            JButton searchBtn = new JButton("SEARCH&SORT");
                searchBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
                searchBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                searchBtn.setBorderPainted(false);
                searchBtn.setFocusPainted(false);
                searchBtn.setHorizontalAlignment(SwingConstants.LEFT);
                searchBtn.setFont(new Font("Arial", Font.PLAIN,11));
                searchBtn.setBackground(new Color(13,31,61)); 
                searchBtn.setForeground(new Color(150,170,200));
                sidebarPanel.add(searchBtn);
                
             //REPORT   
             JButton reportBtn = new JButton("REPORT");
                reportBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
                reportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                reportBtn.setBorderPainted(false);
                reportBtn.setFocusPainted(false);
                reportBtn.setHorizontalAlignment(SwingConstants.LEFT);
                reportBtn.setFont(new Font("Arial", Font.PLAIN,11));
                reportBtn.setBackground(new Color(13,31,60)); 
                reportBtn.setForeground(new Color(150,170,200));
                sidebarPanel.add(reportBtn);   
                
            //mainPanel
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(new Color(244,246,251));
            add(mainPanel,BorderLayout.CENTER);
            
            //TOPBAR
            JPanel topBar = new JPanel();
            topBar.setBackground(Color.WHITE);
            topBar.setLayout(new BorderLayout());
            topBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
            
            //TitlePanel
            JLabel pageTitle = new JLabel("Library Catalogue");
            pageTitle.setFont(new Font("Arial", Font.BOLD,14));
            pageTitle.setForeground(new Color(13,31,60));
            topBar.add(pageTitle,BorderLayout.WEST);
            
            mainPanel.add(topBar,BorderLayout.NORTH);
            
            
            //STATUS PANEL
            JPanel statusBar = new JPanel();
            statusBar.setBackground(new Color(13,31,60));
            statusBar.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));
            
            //status panel label
            JLabel statusLabel = new JLabel("Total:0 | Available:0 | Borrowed:0");
            statusLabel.setForeground(new Color(150, 170, 200));
            statusLabel.setFont(new Font("Arial", Font.PLAIN,15));
            statusBar.add(statusLabel,BorderLayout.WEST);
            
            mainPanel.add(statusBar,BorderLayout.SOUTH);
            
            
            // cards
            CardLayout cards = new CardLayout();
            JPanel contentPanel = new JPanel(cards);
            contentPanel.setBackground(new Color(244,246,251));
            mainPanel.add(contentPanel,BorderLayout.CENTER);
            
            //CARDS VIEW
            JPanel viewPanel =  new JPanel();
            JPanel borrowPanel = new JPanel();
            JPanel adminPanel = new JPanel();
            JPanel searchPanel = new JPanel();
            JPanel reportPanel = new JPanel();
            
            //content panel
            contentPanel.add(viewPanel,"VIEW");
            contentPanel.add(borrowPanel,"BORROW");
            contentPanel.add(adminPanel,"ADMIN");
            contentPanel.add(searchPanel,"SEARCH");
            contentPanel.add(reportPanel,"REPORT");
            
            
            //eventlistener
            viewBtn.addActionListener(e-> cards.show(contentPanel,"VIEW"));
            borrowBtn.addActionListener(e-> cards.show(contentPanel,"BORROW"));
            adminBtn.addActionListener(e-> cards.show(contentPanel,"ADMIN"));
            searchBtn.addActionListener(e-> cards.show(contentPanel,"SEARCH"));
            reportBtn.addActionListener(e-> cards.show(contentPanel,"REPORT"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}