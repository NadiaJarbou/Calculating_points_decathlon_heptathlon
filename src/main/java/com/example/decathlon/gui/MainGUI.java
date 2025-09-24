package com.example.decathlon.gui;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import java.awt.*;

import com.example.decathlon.deca.*;
import com.example.decathlon.heptathlon.*;

public class MainGUI {
    private JComboBox<String> modeBox; // "Decathlon" / "Heptathlon"

    private static final String MODE_DEC = "Decathlon";
    private static final String MODE_HEP = "Heptathlon";
    private JTextField nameField;
    private JTextField resultField;
    private JComboBox<String> disciplineBox;
    private JTextArea outputArea;
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
        frame.setSize(500, 400);

        JPanel panel = new JPanel(new GridLayout(7, 1));

        // Mode select
        modeBox = new JComboBox<>(new String[]{MODE_DEC, MODE_HEP});
        panel.add(new JLabel("Select Mode:"));
        panel.add(modeBox);

        // Input for competitor's name
        nameField = new JTextField(20);
        panel.add(new JLabel("Enter Competitor's Name:"));
        panel.add(nameField);

        disciplineBox = new JComboBox<>(DEC_EVENTS);
        panel.add(new JLabel("Select Discipline:"));
        panel.add(disciplineBox);

        // Input for result
        resultField = new JTextField(10);
        panel.add(new JLabel("Enter Result:"));
        panel.add(resultField);

        // Button to calculate and display result
        JButton calculateButton = new JButton("Calculate Score");
        calculateButton.addActionListener(new CalculateButtonListener());
        panel.add(calculateButton);

        // Output area
        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane);

        modeBox.addActionListener(ev -> {
            String m = (String) modeBox.getSelectedItem();
            disciplineBox.removeAllItems();
            if (MODE_HEP.equals(m)) {
                for (String s : HEP_EVENTS) disciplineBox.addItem(s);
            } else {
                for (String s : DEC_EVENTS) disciplineBox.addItem(s);
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String discipline = (String) disciplineBox.getSelectedItem();
            String mode = (String) modeBox.getSelectedItem();

            try {
                double result = Double.parseDouble(resultField.getText());
                int score = 0;

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
                } else { // MODE_HEP
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
                outputArea.append("Mode: " + mode + "\n");
                outputArea.append("Competitor: " + name + "\n");
                outputArea.append("Discipline: " + discipline + "\n");
                outputArea.append("Result: " + result + "\n");
                outputArea.append("Score: " + score + "\n\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number for the result.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
