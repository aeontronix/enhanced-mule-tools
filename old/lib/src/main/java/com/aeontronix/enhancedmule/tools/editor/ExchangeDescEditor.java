/*
 * Created by JFormDesigner on Thu May 20 00:19:35 PDT 2021
 */

package com.aeontronix.enhancedmule.tools.editor;

import com.aeontronix.commons.swing.PlaceholderTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * @author Yannick Menager
 */
public class ExchangeDescEditor extends JPanel {
    public ExchangeDescEditor() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("com.aeontronix.enhancedmule.tools.Bundle"); //NON-NLS
        tabbedPane1 = new JTabbedPane();
        label2 = new JLabel();
        comboBox1 = new JComboBox<>();
        label11 = new JLabel();
        comboBox2 = new JComboBox();
        label8 = new JLabel();
        placeholderTextField7 = new PlaceholderTextField();
        label9 = new JLabel();
        placeholderTextField8 = new PlaceholderTextField();
        label10 = new JLabel();
        placeholderTextField9 = new PlaceholderTextField();
        label5 = new JLabel();
        placeholderTextField4 = new PlaceholderTextField();
        label6 = new JLabel();
        placeholderTextField5 = new PlaceholderTextField();
        label7 = new JLabel();
        placeholderTextField6 = new PlaceholderTextField();

        //======== tabbedPane1 ========
        {

            //======== this ========
            {
                this.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[right]" + //NON-NLS
                    "[grow,fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS

                //---- label2 ----
                label2.setText(bundle.getString("ExchangeDescEditor.label2.text")); //NON-NLS
                this.add(label2, "cell 0 0"); //NON-NLS

                //---- comboBox1 ----
                comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
                    "Exchange", //NON-NLS
                    "In Project" //NON-NLS
                }));
                this.add(comboBox1, "cell 1 0"); //NON-NLS

                //---- label11 ----
                label11.setText(bundle.getString("ExchangeDescEditor.label11.text")); //NON-NLS
                this.add(label11, "cell 0 1"); //NON-NLS
                this.add(comboBox2, "cell 1 1"); //NON-NLS

                //---- label8 ----
                label8.setText(bundle.getString("ExchangeDescEditor.label8.text")); //NON-NLS
                this.add(label8, "cell 0 2"); //NON-NLS
                this.add(placeholderTextField7, "cell 1 2"); //NON-NLS

                //---- label9 ----
                label9.setText(bundle.getString("ExchangeDescEditor.label9.text")); //NON-NLS
                this.add(label9, "cell 0 3"); //NON-NLS
                this.add(placeholderTextField8, "cell 1 3"); //NON-NLS

                //---- label10 ----
                label10.setText(bundle.getString("ExchangeDescEditor.label10.text")); //NON-NLS
                this.add(label10, "cell 0 4"); //NON-NLS
                this.add(placeholderTextField9, "cell 1 4"); //NON-NLS

                //---- label5 ----
                label5.setText(bundle.getString("ExchangeDescEditor.label5.text")); //NON-NLS
                this.add(label5, "cell 0 5"); //NON-NLS
                this.add(placeholderTextField4, "cell 1 5"); //NON-NLS

                //---- label6 ----
                label6.setText(bundle.getString("ExchangeDescEditor.label6.text")); //NON-NLS
                this.add(label6, "cell 0 6"); //NON-NLS
                this.add(placeholderTextField5, "cell 1 6"); //NON-NLS

                //---- label7 ----
                label7.setText(bundle.getString("ExchangeDescEditor.label7.text")); //NON-NLS
                this.add(label7, "cell 0 7"); //NON-NLS
                this.add(placeholderTextField6, "cell 1 7"); //NON-NLS
            }
            tabbedPane1.addTab(bundle.getString("ExchangeDescEditor.this.tab.title"), this); //NON-NLS
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane tabbedPane1;
    private JLabel label2;
    private JComboBox<String> comboBox1;
    private JLabel label11;
    private JComboBox comboBox2;
    private JLabel label8;
    private PlaceholderTextField placeholderTextField7;
    private JLabel label9;
    private PlaceholderTextField placeholderTextField8;
    private JLabel label10;
    private PlaceholderTextField placeholderTextField9;
    private JLabel label5;
    private PlaceholderTextField placeholderTextField4;
    private JLabel label6;
    private PlaceholderTextField placeholderTextField5;
    private JLabel label7;
    private PlaceholderTextField placeholderTextField6;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
