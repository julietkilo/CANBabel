package com.github.canbabel.canio.ui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import com.github.canbabel.canio.dbc.DbcReader;


public class FileChooser extends JPanel
                             implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5937316678507532090L;
	static private final String newline = "\n";
    JButton openButton, saveButton;
    JTextArea log;
    JFileChooser fc;
    DbcReader reader;

    public FileChooser() {
        super(new BorderLayout());

        log = new JTextArea(5,15);
        log.setMargin(new Insets(1,1,1,1));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        fc = new JFileChooser();

        openButton = new JButton("Open a File...",null);          
        openButton.addActionListener(this);

        saveButton = new JButton("Save a File...",null);
        saveButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(); 
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(FileChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                log.append("Opening: " + file.getName() + "." + newline);
                reader = new DbcReader(file);
                // new
                reader.parseFile(file);
                // new
            } else {
                log.append("Open cancelled" + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());


        } else if (e.getSource() == saveButton) {
        	File file = new File("testj.kcd");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setSelectedFile(file);        	
            int returnVal = fc.showSaveDialog(FileChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                log.append("Saving: " + file.getName() + "." + newline);
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("CAN Babel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FileChooser());
        frame.pack();
        frame.setVisible(true);
    }

    
    public static void main(String[] args) {

        SwingUtilities.invokeLater(
        	new Runnable() {
	            public void run() { 
	                createAndShowGUI();
	            }
       		}
        );
    }
}