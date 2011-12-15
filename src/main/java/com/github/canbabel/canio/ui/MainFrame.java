
package com.github.canbabel.canio.ui;

import com.github.canbabel.canio.dbc.DbcReader;
import com.github.canbabel.canio.kcd.NetworkDefinition;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class MainFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = -6772467633506915053L;
    private JFileChooser fc = new JFileChooser();
    private FileList list = new FileList();
    private Thread convertThread;

    private FileFilter directoryFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    private FileFilter dbcFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.getName().endsWith(".dbc");
        }
    };

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();

        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                if(f.getName().endsWith(".dbc")) {
                    return true;
                } else if(f.isDirectory()) {
                    return true;
                }

                return false;
            }

            @Override
            public String getDescription() {
                return ".dbc files and directories";
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
    }

    private List<File> filesForDirectory(File directory) {
        ArrayList<File> files = new ArrayList<File>();

        File[] dbcFiles = directory.listFiles(dbcFilter);
        files.addAll(Arrays.asList(dbcFiles));

        File[] dirs = directory.listFiles(directoryFilter);

        for(File dir : dirs) {
            files.addAll(filesForDirectory(dir));
        }



        return files;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton2 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CANBabel");
        setMinimumSize(new java.awt.Dimension(640, 480));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jButton3.setText("Convert");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jButton3, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText("Gzipped Output");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        jPanel2.add(jCheckBox1, gridBagConstraints);

        jCheckBox2.setSelected(true);
        jCheckBox2.setText("Pretty print XML");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.2;
        jPanel2.add(jCheckBox2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel3.add(jScrollPane2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.5;
        getContentPane().add(jPanel3, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Input"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Add files or folders");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jButton1, gridBagConstraints);

        jList1.setModel(list);
        jScrollPane1.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jButton2.setText("Remove");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        jProgressBar1.setEnabled(false);
        jProgressBar1.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jProgressBar1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();

            for(File f : files) {
                if(f.isDirectory()) {
                    List<File> dirFiles = filesForDirectory(f);

                    for(File fi : dirFiles) {
                        list.addFile(fi);
                    }

                } else {
                    list.addFile(f);
                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int[] selection = jList1.getSelectedIndices();

        Arrays.sort(selection);

        for(int i=0;i<selection.length;i++) {
            list.remove(selection[i] - i);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if(convertThread != null && convertThread.isAlive()) {
            convertThread.interrupt();
            try {
                convertThread.join();
            } catch (InterruptedException ex) {

            }
        } else if(list.getSize() > 0) {
            convertThread = new Thread(convertRunnable);
            convertThread.start();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    OutputStream logOutput = new OutputStream() {

        private StringBuilder string = new StringBuilder();
        @Override
        public void write(int b) throws IOException {
            this.string.append((char) b );
        }

        @Override
        public String toString(){
            return this.string.toString();
        }

        @Override
        public void flush() throws IOException {
            jTextArea1.setText(string.toString());
        }

    };

    PrintWriter logWriter = new PrintWriter(logOutput);
    private Runnable convertRunnable = new Runnable() {

        @Override
        public void run() {
            jButton1.setEnabled(false);
            jButton2.setEnabled(false);
            jButton3.setText("Abort");

            jProgressBar1.setEnabled(true);
            jProgressBar1.setMinimum(0);
            jProgressBar1.setMaximum(list.getSize());
            jProgressBar1.setValue(0);

            jCheckBox1.setEnabled(false);
            jCheckBox2.setEnabled(false);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream resourceAsStream = NetworkDefinition.class.getResourceAsStream("Definition.xsd");
            Source s = new StreamSource(resourceAsStream);
            Schema schema;
            Validator val = null;
            try {
                schema = schemaFactory.newSchema(s);
                val = schema.newValidator();
            } catch (SAXException ex) {
                ex.printStackTrace(logWriter);
            }

            ErrorHandler handler = new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    exception.printStackTrace(logWriter);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    exception.printStackTrace(logWriter);
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    exception.printStackTrace(logWriter);
                }
            };
            val.setErrorHandler(handler);

            for(File f : list.getFiles()) {

                String filename = f.getPath();

                if(jCheckBox1.isSelected()) {
                    filename = filename.substring(0, filename.length()-4) + ".kcd.gz";
                } else {
                    filename = filename.substring(0, filename.length()-4) + ".kcd";
                }

                File newFile = new File(filename);

                if(newFile.exists()) {
                    int answer = JOptionPane.showConfirmDialog(jButton1, "File " + filename + " already exists. Overwrite?");

                    if(answer == JOptionPane.NO_OPTION) {
                        jProgressBar1.setValue(jProgressBar1.getValue()+1);
                        continue;
                    }
                }

                if(Thread.interrupted()) {
                    break;
                }

                jProgressBar1.setString("Converting " + (jProgressBar1.getValue()+1) + " of " + jProgressBar1.getMaximum() + ": " + f.getName());
                logWriter.write("### Converting " + f.getName() + " ###\n");
                logWriter.flush();
                try {
                    DbcReader reader = new DbcReader();
                    if(reader.parseFile(f, logOutput)) {
                        reader.writeKcdFile(newFile, jCheckBox2.isSelected(), jCheckBox1.isSelected());

                        /* Validate the result */
                        StreamSource source;

                        if(jCheckBox1.isSelected()) {
                            source = new StreamSource(new GZIPInputStream(new FileInputStream(newFile)));
                        } else {
                            source = new StreamSource(newFile);
                        }
                        val.validate(source);

                    }

                    if(Thread.interrupted()) {
                        break;
                    }
                } catch(Exception ex) {
                    ex.printStackTrace(logWriter);
                }

                logWriter.flush();
                jProgressBar1.setValue(jProgressBar1.getValue()+1);
            }

            list.clear();

            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
            jButton3.setText("Convert");

            jProgressBar1.setValue(0);
            jProgressBar1.setEnabled(false);

            jCheckBox1.setEnabled(true);
            jCheckBox2.setEnabled(true);
        }

    };

}
