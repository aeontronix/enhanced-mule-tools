/*
 * Created by JFormDesigner on Thu May 20 00:28:39 PDT 2021
 */

package com.aeontronix.enhancedmule.tools.editor;

import com.aeontronix.commons.swing.PlaceholderTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * @author Yannick Menager
 */
public class APIDescEditor extends JPanel {
    public APIDescEditor() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("com.aeontronix.enhancedmule.tools.Bundle"); //NON-NLS
        tabbedPane1 = new JTabbedPane();
        apiDetails = new JPanel();
        checkBox3 = new JCheckBox();
        label5 = new JLabel();
        placeholderTextField4 = new PlaceholderTextField();
        label4 = new JLabel();
        placeholderTextField5 = new PlaceholderTextField();
        label7 = new JLabel();
        placeholderTextField7 = new PlaceholderTextField();
        panel2 = new JPanel();
        checkBox1 = new JCheckBox();
        label8 = new JLabel();
        placeholderTextField8 = new PlaceholderTextField();
        apiPoliciesPanel = new JPanel();
        apiSlaTiersPanel = new JPanel();
        panel1 = new JPanel();

        //======== this ========
        setLayout(new BorderLayout());

        //======== tabbedPane1 ========
        {

            //======== apiDetails ========
            {
                apiDetails.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[fill]" + //NON-NLS
                    "[grow,fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS

                //---- checkBox3 ----
                checkBox3.setText(bundle.getString("APIDescEditor.checkBox3.text")); //NON-NLS
                apiDetails.add(checkBox3, "cell 0 0 2 1"); //NON-NLS

                //---- label5 ----
                label5.setText(bundle.getString("APIDescEditor.label5.text")); //NON-NLS
                apiDetails.add(label5, "cell 0 1,alignx right,growx 0"); //NON-NLS
                apiDetails.add(placeholderTextField4, "cell 1 1"); //NON-NLS

                //---- label4 ----
                label4.setText(bundle.getString("APIDescEditor.label4.text")); //NON-NLS
                apiDetails.add(label4, "cell 0 2,alignx right,growx 0"); //NON-NLS
                apiDetails.add(placeholderTextField5, "cell 1 2"); //NON-NLS

                //---- label7 ----
                label7.setText(bundle.getString("APIDescEditor.label7.text")); //NON-NLS
                apiDetails.add(label7, "cell 0 3,alignx right,growx 0"); //NON-NLS
                apiDetails.add(placeholderTextField7, "cell 1 3"); //NON-NLS

                //======== panel2 ========
                {
                    panel2.setBorder(new TitledBorder(bundle.getString("APIDescEditor.panel2.border"))); //NON-NLS
                    panel2.setLayout(new MigLayout(
                        "insets 1,hidemode 3", //NON-NLS
                        // columns
                        "[fill]" + //NON-NLS
                        "[grow,fill]", //NON-NLS
                        // rows
                        "[]" + //NON-NLS
                        "[]")); //NON-NLS

                    //---- checkBox1 ----
                    checkBox1.setText(bundle.getString("APIDescEditor.checkBox1.text")); //NON-NLS
                    panel2.add(checkBox1, "cell 0 0 2 1"); //NON-NLS

                    //---- label8 ----
                    label8.setText(bundle.getString("APIDescEditor.label8.text")); //NON-NLS
                    panel2.add(label8, "cell 0 1"); //NON-NLS
                    panel2.add(placeholderTextField8, "cell 1 1"); //NON-NLS
                }
                apiDetails.add(panel2, "cell 0 4 2 1"); //NON-NLS
            }
            tabbedPane1.addTab(bundle.getString("APIDescEditor.apiDetails.tab.title"), apiDetails); //NON-NLS

            //======== apiPoliciesPanel ========
            {
                apiPoliciesPanel.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[fill]" + //NON-NLS
                    "[fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS
            }
            tabbedPane1.addTab(bundle.getString("APIDescEditor.apiPoliciesPanel.tab.title"), apiPoliciesPanel); //NON-NLS

            //======== apiSlaTiersPanel ========
            {
                apiSlaTiersPanel.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[fill]" + //NON-NLS
                    "[fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS
            }
            tabbedPane1.addTab(bundle.getString("APIDescEditor.apiSlaTiersPanel.tab.title"), apiSlaTiersPanel); //NON-NLS

            //======== panel1 ========
            {
                panel1.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[fill]" + //NON-NLS
                    "[fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS
            }
            tabbedPane1.addTab(bundle.getString("APIDescEditor.panel1.tab.title"), panel1); //NON-NLS
        }
        add(tabbedPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane tabbedPane1;
    private JPanel apiDetails;
    private JCheckBox checkBox3;
    private JLabel label5;
    private PlaceholderTextField placeholderTextField4;
    private JLabel label4;
    private PlaceholderTextField placeholderTextField5;
    private JLabel label7;
    private PlaceholderTextField placeholderTextField7;
    private JPanel panel2;
    private JCheckBox checkBox1;
    private JLabel label8;
    private PlaceholderTextField placeholderTextField8;
    private JPanel apiPoliciesPanel;
    private JPanel apiSlaTiersPanel;
    private JPanel panel1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
