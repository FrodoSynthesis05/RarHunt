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
private final JTable resultTable;
private JComboBox<String> categoryComboBox;

private final Connect con; // Assuming Connect class exists
private final DefaultTableModel tableModel;

public RarHunt() {
    super("RarHunt");

    // Set up the components
    nameLabel = new JLabel("Name:");
    nameField = new JTextField(20);
    searchButton = new JButton("Search");

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

    // Create a JLabel for the category ComboBox
    JLabel categoryLabel = new JLabel("Category:");
    gbc.gridx++;
    //add(categoryLabel, gbc);

    // Create a JComboBox for categories
    String[] categories = {"All", "ebooks", "games_pc_iso", "games_pc_rip", "games_ps3", "games_ps4", "games_xbox360", "movies", "movies_bd_full", "movies_bd_remux", "movies_x264", "movies_x264_3d", "movies_x264_4k", "movies_x264_720", "movies_x265", "movies_x265_4k", "movies_x265_4k_hdr", "movies_xvid", "movies_xvid_720", "music_flac", "music_mp3", "software_pc_iso", "tv", "tv_sd", "tv_uhd", "xxx"};
    categoryComboBox = new JComboBox<>(categories);
    gbc.gridx++;
    add(categoryComboBox, gbc);

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

            Object[] rowData = {id, hash, titleFull, dt, cat, size, movieImdb};
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
    } catch (SQLException ex) {
        Logger.getLogger(RarHunt.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void launchQBittorrent(String torrentHash) {
    try {
        String qbittorrentPath = "C:\\Program Files\\qBittorrent\\qbittorrent.exe";
        String command = qbittorrentPath + " " + torrentHash;
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    } catch (IOException | InterruptedException ex) {
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
