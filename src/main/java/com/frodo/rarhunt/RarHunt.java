/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.frodo.rarhunt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import model.Connect;

public class RarHunt extends JFrame {

private JLabel nameLabel;
private JTextField nameField;
private JButton searchButton;
private JButton processButton;
private final JTable resultTable;
private JComboBox<String> categoryComboBox;

private final Connect con;
private final DefaultTableModel tableModel;

public RarHunt() {
    super("RarHunt");

    // Set up the components
    nameLabel = new JLabel("Name:");
    nameField = new JTextField(20);
    searchButton = new JButton("Search");
    processButton = new JButton("Sort");

    // Set up the layout with padding
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(5, 5, 5, 5); // Padding

    // Add components to the frame
    add(nameLabel, gbc);

    // Update the GridBagConstraints to move to the next column
    gbc.gridx++;
    add(nameField, gbc);

    gbc.gridx++;
    add(searchButton, gbc);

    // Create a JComboBox for categories
    String[] categories = {"All", "ebooks", "games_pc_iso", "games_pc_rip", "games_ps3", "games_ps4", "games_xbox360", "movies", "movies_bd_full", "movies_bd_remux", "movies_x264", "movies_x264_3d", "movies_x264_4k", "movies_x264_720", "movies_x265", "movies_x265_4k", "movies_x265_4k_hdr", "movies_xvid", "movies_xvid_720", "music_flac", "music_mp3", "software_pc_iso", "tv", "tv_sd", "tv_uhd", "xxx"};
    categoryComboBox = new JComboBox<>(categories);
    gbc.gridx++;
    add(categoryComboBox, gbc);

    // Add the processButton
    gbc.gridx++;
    add(processButton, gbc);

    // Create a table model with labeled columns
    tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Hash", "Title", "DT", "Category", "Size", "IMDB Tag"}
    ) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Make all cells non-editable
    }
    };

    // Create a table with the model
    resultTable = new JTable(tableModel);
    resultTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering
    resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow selecting only one row
    JScrollPane scrollPane = new JScrollPane(resultTable);
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    add(scrollPane, gbc);

    // Set up the event listener for the search button
    searchButton.addActionListener((ActionEvent e) -> {
        // Perform the search and update the table accordingly
        String searchName = nameField.getText();
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        String searchImdb = nameField.getText();

        String modifiedSearchName = searchName.replaceAll(" ", ".");
        query(modifiedSearchName, selectedCategory, searchImdb);
    });

    // Set up the event listener for the process button
    processButton.addActionListener((ActionEvent e) -> {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        processResultsByCategory(selectedCategory);
    });

    // Set frame properties
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1500, 850);
    setLocationRelativeTo(null);
    setResizable(false); // Set the window not resizable
    setVisible(true);

    // Initialize the Connect class and establish a database connection
    con = new Connect(); // Initialize appropriately with your connection details
    con.conectar(); // Open the database connection

    // Set the "All" option as the default selection
    categoryComboBox.setSelectedItem("All");

    nameField.addActionListener((ActionEvent e) -> {
        // Perform the search and update the table accordingly
        String searchName = nameField.getText();
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        String searchImdb = nameField.getText();

        String modifiedSearchName = searchName.replaceAll(" ", ".");
        query(modifiedSearchName, selectedCategory, searchImdb);
    });
}

public void query(String title, String category, String imdb) {
    try {
        String query = "SELECT * FROM items WHERE title LIKE '%" + title + "%'";
        if (!category.equals("All")) {
            query += " AND cat = '" + category + "'";
        }
        if (!imdb.isEmpty()) {
            title = "*";
            query += " OR imdb = '" + imdb + "'";
        }

        ResultSet info = con.leer(query);

        // Clear the existing table data
        tableModel.setRowCount(0);

        while (info.next()) {
            double id = info.getDouble("id");
            double size = info.getDouble("size");
            String titleFull = info.getString("title");
            String hash = info.getString("hash");
            String dt = info.getString("dt");
            String cat = info.getString("cat");
            String movieImdb = info.getString("imdb");
            double gigs = Math.ceil(size / (1024 * 1024 * 1024));
            Object[] rowData = {id, hash, titleFull, dt, cat, gigs, movieImdb};
            tableModel.addRow(rowData);
        }

        resultTable.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int selectedRow = resultTable.getSelectedRow();
                String torrentHash = (String) tableModel.getValueAt(selectedRow, 1); // Assuming the hash is at column index 1
                launchQBittorrent(torrentHash);
            }
        }
        });

        // Display a popup message if no results are found after 7 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
        @Override
        public void run() {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(RarHunt.this, "No matching results found.", "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        }, 7000);

    } catch (SQLException ex) {
        Logger.getLogger(RarHunt.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void processResultsByCategory(String selectedCategory) {
    // Create a temporary table model to hold the filtered results
    DefaultTableModel filteredTableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Hash", "Title", "DT", "Category", "Size", "IMDB Tag"}
    ) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Make all cells non-editable
    }
    };

    for (int i = 0; i < tableModel.getRowCount(); i++) {
        String category = (String) tableModel.getValueAt(i, 4); // Assuming the category is at column index 4
        if (category.equals(selectedCategory) || selectedCategory.equals("All")) {
            // Add the row to the filtered table model
            Object[] rowData = {
                tableModel.getValueAt(i, 0), // ID
                tableModel.getValueAt(i, 1), // Hash
                tableModel.getValueAt(i, 2), // Title
                tableModel.getValueAt(i, 3), // DT
                tableModel.getValueAt(i, 4), // Category
                tableModel.getValueAt(i, 5), // Size
                tableModel.getValueAt(i, 6) // IMDB Tag
            };
            filteredTableModel.addRow(rowData);
        }
    }

    // Set the filtered table model as the new table model
    resultTable.setModel(filteredTableModel);
}

private void launchQBittorrent(String torrentHash) {
    try {
        String qbittorrentPath = "C:\\Program Files\\qBittorrent\\qbittorrent.exe";
        String command = qbittorrentPath + " " + torrentHash;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.start();
    } catch (IOException ex) {
    }
}

public static void main(String[] args) {
    try {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));
    } catch (UnsupportedLookAndFeelException e) {
    }

    SwingUtilities.invokeLater(RarHunt::new);
}
}
