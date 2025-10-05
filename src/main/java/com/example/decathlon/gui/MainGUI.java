package com.example.decathlon.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.decathlon.deca.InvalidResultException;

import com.example.decathlon.deca.*;
import com.example.decathlon.heptathlon.*;
import com.example.decathlon.excel.ExcelPrinter;

public class MainGUI {
    private JComboBox<String> modeBox;
    private static final String MODE_DEC = "Decathlon";
    private static final String MODE_HEP = "Heptathlon";
    private JTextField nameField;
    private JTextField resultField;
    private JComboBox<String> disciplineBox;
    private JTextArea outputArea;

    private JTable resultsTable;
    private DefaultTableModel tableModel;

    private static final String[] DEC_EVENTS = {
            "100m", "400m", "1500m", "110m Hurdles",
            "Long Jump", "High Jump", "Pole Vault",
            "Discus Throw", "Javelin Throw", "Shot Put"
    };
    private static final String[] HEP_EVENTS = {
            "100m Hurdles", "High Jump", "Shot Put",
            "200m", "Long Jump", "Javelin Throw", "800m"
    };

    public static void main(String[] args) {
        new MainGUI().createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Track and Field Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        JPanel panel = new JPanel(new GridLayout(0, 1));

        modeBox = new JComboBox<>(new String[]{MODE_DEC, MODE_HEP});
        panel.add(new JLabel("Select Mode:"));
        panel.add(modeBox);

        nameField = new JTextField(20);
        panel.add(new JLabel("Enter Competitor's Name:"));
        panel.add(nameField);

        disciplineBox = new JComboBox<>(DEC_EVENTS);
        panel.add(new JLabel("Select Discipline:"));
        panel.add(disciplineBox);

        resultField = new JTextField(10);
        panel.add(new JLabel("Enter Result:"));
        panel.add(resultField);

        JButton calculateButton = new JButton("Calculate Score");
        JButton exportButton = new JButton("Export to Excel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(calculateButton);
        buttons.add(exportButton);
        panel.add(buttons);

        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane);

        tableModel = new DefaultTableModel(getColumnsForMode(MODE_DEC), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 0 ? String.class : Integer.class; }
        };
        resultsTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(resultsTable);
        panel.add(new JLabel("Results Table:"));
        panel.add(tableScroll);

        modeBox.addActionListener(ev -> {
            String m = (String) modeBox.getSelectedItem();
            disciplineBox.removeAllItems();
            if (MODE_HEP.equals(m)) {
                for (String s : HEP_EVENTS) disciplineBox.addItem(s);
            } else {
                for (String s : DEC_EVENTS) disciplineBox.addItem(s);
            }
            rebuildTableForMode(m);
        });

        calculateButton.addActionListener(new CalculateButtonListener());
        exportButton.addActionListener(e -> exportToExcel());

        frame.add(panel);
        frame.setVisible(true);
    }

    private Object[] getColumnsForMode(String mode) {
        String[] events = MODE_HEP.equals(mode) ? HEP_EVENTS : DEC_EVENTS;
        Object[] cols = new Object[events.length + 2];
        cols[0] = "Competitor";
        System.arraycopy(events, 0, cols, 1, events.length);
        cols[cols.length - 1] = "Total";
        return cols;
    }

    private void rebuildTableForMode(String mode) {
        DefaultTableModel newModel = new DefaultTableModel(getColumnsForMode(mode), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 0 ? String.class : Integer.class; }
        };
        resultsTable.setModel(newModel);
        tableModel = newModel;
    }

    private int findRowByName(String name) {
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            Object v = tableModel.getValueAt(r, 0);
            if (v != null && v.toString().equalsIgnoreCase(name)) return r;
        }
        return -1;
    }

    private int eventColumnIndex(String discipline) {
        for (int c = 1; c < tableModel.getColumnCount() - 1; c++) {
            if (tableModel.getColumnName(c).equals(discipline)) return c;
        }
        return -1;
    }

    private void ensureRowExists(String name) {
        if (findRowByName(name) == -1) {
            Object[] row = new Object[tableModel.getColumnCount()];
            row[0] = name;
            for (int i = 1; i < tableModel.getColumnCount(); i++) row[i] = null;
            tableModel.addRow(row);
        }
    }

    private void recalcTotal(int row) {
        int total = 0;
        for (int c = 1; c < tableModel.getColumnCount() - 1; c++) {
            Object v = tableModel.getValueAt(row, c);
            if (v instanceof Integer) total += (Integer) v;
        }
        tableModel.setValueAt(total, row, tableModel.getColumnCount() - 1);
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String discipline = (String) disciplineBox.getSelectedItem();
            String mode = (String) modeBox.getSelectedItem();

            if(tableModel.getRowCount()>=40){
                JOptionPane.showMessageDialog(null,"Too many competitors", "Error", JOptionPane.INFORMATION_MESSAGE);
            } else{
                try {
                    double result = Double.parseDouble(resultField.getText());
                    int score = 0;

                    try {
                        if (MODE_DEC.equals(mode)) {
                            switch (discipline) {
                                case "100m":            score = new Deca100M().calculateResult(result); break;
                                case "400m":            score = new Deca400M().calculateResult(result); break;
                                case "1500m":           score = new Deca1500M().calculateResult(result); break;
                                case "110m Hurdles":    score = new Deca110MHurdles().calculateResult(result); break;
                                case "Long Jump":       score = new DecaLongJump().calculateResult(result); break;
                                case "High Jump":       score = new DecaHighJump().calculateResult(result); break;
                                case "Pole Vault":      score = new DecaPoleVault().calculateResult(result); break;
                                case "Discus Throw":    score = new DecaDiscusThrow().calculateResult(result); break;
                                case "Javelin Throw":   score = new DecaJavelinThrow().calculateResult(result); break;
                                case "Shot Put":        score = new DecaShotPut().calculateResult(result); break;
                            }
                        } else {
                            switch (discipline) {
                                case "100m Hurdles":    score = new Hep100MHurdles().calculateResult(result); break;
                                case "High Jump":       score = new HeptHightJump().calculateResult(result); break;
                                case "Shot Put":        score = new HeptShotPut().calculateResult(result); break;
                                case "200m":            score = new Hep200M().calculateResult(result); break;
                                case "Long Jump":       score = new HeptLongJump().calculateResult(result); break;
                                case "Javelin Throw":   score = new HeptJavelinThrow().calculateResult(result); break;
                                case "800m":            score = new Hep800M().calculateResult(result); break;
                            }
                        }
                    } catch (InvalidResultException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Invalid Result", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    outputArea.append("Mode: " + mode + "\n");
                    outputArea.append("Competitor: " + name + "\n");
                    outputArea.append("Discipline: " + discipline + "\n");
                    outputArea.append("Result: " + result + "\n");
                    outputArea.append("Score: " + score + "\n\n");

                    ensureRowExists(name);
                    int row = findRowByName(name);
                    int col = eventColumnIndex(discipline);
                    if (row != -1 && col != -1) {
                        tableModel.setValueAt(score, row, col);
                        recalcTotal(row);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid number for the result.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }


        }
    }

    private void exportToExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "No rows to export.", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int rows = tableModel.getRowCount();
        int cols = tableModel.getColumnCount();
        Object[][] data = new Object[rows + 1][cols];
        for (int c = 0; c < cols; c++) data[0][c] = tableModel.getColumnName(c);
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) data[r + 1][c] = tableModel.getValueAt(r, c);
        String excelName = "results_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        try {
            ExcelPrinter printer = new ExcelPrinter(excelName);
            printer.add(data, "Results");
            printer.write();
            JOptionPane.showMessageDialog(null, "Exported to: C:/Eclipse/resultat_" + excelName + ".xlsx", "Export OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
