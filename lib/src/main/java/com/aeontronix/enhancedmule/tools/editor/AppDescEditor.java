/*
 * Created by JFormDesigner on Wed May 19 21:21:35 PDT 2021
 */

package com.aeontronix.enhancedmule.tools.editor;

import java.awt.*;
import java.awt.event.*;

import com.aeontronix.commons.SwingUtils;
import com.aeontronix.commons.swing.PlaceholderTextField;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatDarkLaf;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static com.aeontronix.commons.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Yannick Menager
 */
public class AppDescEditor extends JPanel {
    private static final Logger logger = getLogger(AppDescEditor.class);
    private ApplicationDescriptor applicationDescriptor = new ApplicationDescriptor();
    private APIDescriptor apiDescriptor = new APIDescriptor();
    private ObjectMapper objectMapper = JsonHelper.createMapper();

    public AppDescEditor() {
        initComponents();
        jsonTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        apiEnabledCheckboxActionPerformed(null);
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        final AppDescEditor panel = new AppDescEditor();
        final JFrame frame = SwingUtils.createFrame("Enhanced Mule Application", panel,
                Preferences.userNodeForPackage(AppDescEditor.class), 50, 50);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void descriptorChanged(KeyEvent e) {
        descriptorChanged();
    }

    private void descriptorChanged() {
        try {
            applicationDescriptor.setId(getText(idTextField));
            applicationDescriptor.setName(getText(nameTextField));
            apiDescriptor.setConsumerUrl(getText(apiConsumerUrlTextField));
            apiDescriptor.setImplementationUrl(getText(apiImplementationUrlTextField));
            applicationDescriptor.setApi( apiEnabledCheckbox.isSelected() ? apiDescriptor : null);
            final String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(applicationDescriptor);
            jsonTextArea.setText(json);
        } catch (JsonProcessingException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Nullable
    private String getText(JTextField textField) {
        return isNotBlank(textField.getText()) ? textField.getText() : null;
    }

    private void apiEnabledCheckboxActionPerformed(ActionEvent e) {
        final boolean enabled = apiEnabledCheckbox.isSelected();
        applicationDescriptor.setApi(enabled ? apiDescriptor : null);
        apiTab.setEnabledAt(1,enabled);
        apiTab.setEnabledAt(2,enabled);
        apiTab.setEnabledAt(3,enabled);
        apiImplementationUrlTextField.setEnabled(enabled);
        apiConsumerUrlTextField.setEnabled(enabled);
        apiLabelTextField.setEnabled(enabled);
        descriptorChanged();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("com.aeontronix.enhancedmule.tools.Bundle"); //NON-NLS
        overrides = new JComboBox();
        overridesEditButton = new JButton();
        sections = new JTabbedPane();
        appDetails = new JPanel();
        idEditLabel = new JLabel();
        idTextField = new PlaceholderTextField();
        nameLabel = new JLabel();
        nameTextField = new PlaceholderTextField();
        descLabel = new JLabel();
        descTextField = new PlaceholderTextField();
        apiTab = new JTabbedPane();
        apiDetails = new JPanel();
        label6 = new JLabel();
        apiEnabledCheckbox = new JCheckBox();
        label5 = new JLabel();
        apiConsumerUrlTextField = new PlaceholderTextField();
        label4 = new JLabel();
        apiImplementationUrlTextField = new PlaceholderTextField();
        label7 = new JLabel();
        apiLabelTextField = new PlaceholderTextField();
        apiPoliciesPanel = new JPanel();
        apiSlaTiersPanel = new JPanel();
        apiExchangeTab = new ExchangeDescEditor();
        clientTab = new JPanel();
        label9 = new JLabel();
        checkBox4 = new JCheckBox();
        label3 = new JLabel();
        textField3 = new JTextField();
        deployentTab = new JPanel();
        propertiesTab = new JPanel();
        scrollPane1 = new JScrollPane();
        jsonTextArea = new RSyntaxTextArea();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3", //NON-NLS
            // columns
            "[grow,fill]" + //NON-NLS
            "[fill]", //NON-NLS
            // rows
            "[]" + //NON-NLS
            "[grow]")); //NON-NLS
        add(overrides, "cell 0 0"); //NON-NLS

        //---- overridesEditButton ----
        overridesEditButton.setText(bundle.getString("AppDescEditor.overridesEditButton.text")); //NON-NLS
        add(overridesEditButton, "cell 1 0"); //NON-NLS

        //======== sections ========
        {

            //======== appDetails ========
            {
                appDetails.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[right]" + //NON-NLS
                    "[grow,fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS

                //---- idEditLabel ----
                idEditLabel.setText(bundle.getString("AppDescEditor.idEditLabel.text")); //NON-NLS
                appDetails.add(idEditLabel, "cell 0 0"); //NON-NLS

                //---- idTextField ----
                idTextField.setPlaceholder("Inherited from POM"); //NON-NLS
                idTextField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        descriptorChanged(e);
                    }
                });
                appDetails.add(idTextField, "cell 1 0"); //NON-NLS

                //---- nameLabel ----
                nameLabel.setText(bundle.getString("AppDescEditor.nameLabel.text")); //NON-NLS
                appDetails.add(nameLabel, "cell 0 1"); //NON-NLS

                //---- nameTextField ----
                nameTextField.setPlaceholder("Inherited from POM"); //NON-NLS
                nameTextField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        descriptorChanged(e);
                    }
                });
                appDetails.add(nameTextField, "cell 1 1"); //NON-NLS

                //---- descLabel ----
                descLabel.setText(bundle.getString("AppDescEditor.descLabel.text")); //NON-NLS
                appDetails.add(descLabel, "cell 0 2"); //NON-NLS

                //---- descTextField ----
                descTextField.setPlaceholder("Inherited from POM"); //NON-NLS
                descTextField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        descriptorChanged(e);
                    }
                });
                appDetails.add(descTextField, "cell 1 2"); //NON-NLS
            }
            sections.addTab(bundle.getString("AppDescEditor.appDetails.tab.title"), appDetails); //NON-NLS

            //======== apiTab ========
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
                        "[]" + //NON-NLS
                        "[]" + //NON-NLS
                        "[]")); //NON-NLS

                    //---- label6 ----
                    label6.setText(bundle.getString("AppDescEditor.label6.text")); //NON-NLS
                    apiDetails.add(label6, "cell 0 0,alignx right,growx 0"); //NON-NLS

                    //---- apiEnabledCheckbox ----
                    apiEnabledCheckbox.addActionListener(e -> apiEnabledCheckboxActionPerformed(e));
                    apiDetails.add(apiEnabledCheckbox, "cell 1 0"); //NON-NLS

                    //---- label5 ----
                    label5.setText(bundle.getString("AppDescEditor.label5.text")); //NON-NLS
                    apiDetails.add(label5, "cell 0 1,alignx right,growx 0"); //NON-NLS

                    //---- apiConsumerUrlTextField ----
                    apiConsumerUrlTextField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            descriptorChanged(e);
                        }
                    });
                    apiDetails.add(apiConsumerUrlTextField, "cell 1 1"); //NON-NLS

                    //---- label4 ----
                    label4.setText(bundle.getString("AppDescEditor.label4.text")); //NON-NLS
                    apiDetails.add(label4, "cell 0 2,alignx right,growx 0"); //NON-NLS

                    //---- apiImplementationUrlTextField ----
                    apiImplementationUrlTextField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            descriptorChanged(e);
                        }
                    });
                    apiDetails.add(apiImplementationUrlTextField, "cell 1 2"); //NON-NLS

                    //---- label7 ----
                    label7.setText(bundle.getString("AppDescEditor.label7.text")); //NON-NLS
                    apiDetails.add(label7, "cell 0 3,alignx right,growx 0"); //NON-NLS

                    //---- apiLabelTextField ----
                    apiLabelTextField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            descriptorChanged(e);
                        }
                    });
                    apiDetails.add(apiLabelTextField, "cell 1 3"); //NON-NLS
                }
                apiTab.addTab(bundle.getString("AppDescEditor.apiDetails.tab.title"), apiDetails); //NON-NLS

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
                apiTab.addTab(bundle.getString("AppDescEditor.apiPoliciesPanel.tab.title"), apiPoliciesPanel); //NON-NLS

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
                apiTab.addTab(bundle.getString("AppDescEditor.apiSlaTiersPanel.tab.title"), apiSlaTiersPanel); //NON-NLS
                apiTab.addTab(bundle.getString("AppDescEditor.apiExchangeTab.tab.title"), apiExchangeTab); //NON-NLS
            }
            sections.addTab(bundle.getString("AppDescEditor.apiTab.tab.title"), apiTab); //NON-NLS

            //======== clientTab ========
            {
                clientTab.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[right]" + //NON-NLS
                    "[grow,fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS

                //---- label9 ----
                label9.setText(bundle.getString("AppDescEditor.label9.text")); //NON-NLS
                clientTab.add(label9, "cell 0 0"); //NON-NLS
                clientTab.add(checkBox4, "cell 1 0"); //NON-NLS

                //---- label3 ----
                label3.setText(bundle.getString("AppDescEditor.label3.text")); //NON-NLS
                clientTab.add(label3, "cell 0 1"); //NON-NLS
                clientTab.add(textField3, "cell 1 1"); //NON-NLS
            }
            sections.addTab(bundle.getString("AppDescEditor.clientTab.tab.title"), clientTab); //NON-NLS

            //======== deployentTab ========
            {
                deployentTab.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[fill]" + //NON-NLS
                    "[fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS
            }
            sections.addTab(bundle.getString("AppDescEditor.deployentTab.tab.title"), deployentTab); //NON-NLS

            //======== propertiesTab ========
            {
                propertiesTab.setLayout(new MigLayout(
                    "hidemode 3", //NON-NLS
                    // columns
                    "[fill]" + //NON-NLS
                    "[fill]", //NON-NLS
                    // rows
                    "[]" + //NON-NLS
                    "[]" + //NON-NLS
                    "[]")); //NON-NLS
            }
            sections.addTab(bundle.getString("AppDescEditor.propertiesTab.tab.title"), propertiesTab); //NON-NLS

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(jsonTextArea);
            }
            sections.addTab("JSON", scrollPane1); //NON-NLS
        }
        add(sections, "cell 0 1 2 1,dock center"); //NON-NLS
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JComboBox overrides;
    private JButton overridesEditButton;
    private JTabbedPane sections;
    private JPanel appDetails;
    private JLabel idEditLabel;
    private PlaceholderTextField idTextField;
    private JLabel nameLabel;
    private PlaceholderTextField nameTextField;
    private JLabel descLabel;
    private PlaceholderTextField descTextField;
    private JTabbedPane apiTab;
    private JPanel apiDetails;
    private JLabel label6;
    private JCheckBox apiEnabledCheckbox;
    private JLabel label5;
    private PlaceholderTextField apiConsumerUrlTextField;
    private JLabel label4;
    private PlaceholderTextField apiImplementationUrlTextField;
    private JLabel label7;
    private PlaceholderTextField apiLabelTextField;
    private JPanel apiPoliciesPanel;
    private JPanel apiSlaTiersPanel;
    private ExchangeDescEditor apiExchangeTab;
    private JPanel clientTab;
    private JLabel label9;
    private JCheckBox checkBox4;
    private JLabel label3;
    private JTextField textField3;
    private JPanel deployentTab;
    private JPanel propertiesTab;
    private JScrollPane scrollPane1;
    private RSyntaxTextArea jsonTextArea;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
