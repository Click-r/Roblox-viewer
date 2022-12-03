package loaders;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.*;

import java.io.IOException;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import classes.api.getInfo;
import loaders.base.*;

import ui.gui.err.ErrorHandler;
import ui.gui.main.MainWindow;

public class SearchSettings extends Setting {

    private boolean isLongValid(String numberString) {
        if (numberString.matches("\\d+")) {
            try {
                Long.valueOf(numberString);
                return true;
            } catch (NumberFormatException tooLarge) {
                System.out.println("Number is too large!");
            }
        }
        
        return false;
    }

    public SearchSettings() throws IOException {
        super(SettingId.SEARCH);
    }

    @Override
    public JPanel getSettingPanel(Rectangle bounds) {
        final Color highlighted = new Color(218, 218, 218);

        JPanel panel = new JPanel();
        panel.setBounds(bounds);
        panel.setLayout(null);

        JPanel idSetting = new JPanel();
        idSetting.setBounds(4, 5, 350, 90);
        idSetting.setLayout(null);
        idSetting.setBorder(new TitledBorder(new EtchedBorder(), "ID"));
        idSetting.setBackground(highlighted);

        JTextPane minText = new JTextPane();
        minText.setBounds(5, 20, 25, 20);
        minText.setText("Min:");
        minText.setBackground(highlighted);
        minText.setEditable(false);

        JTextField minInput = new JTextField();
        minInput.setBounds(minText.getX() + minText.getWidth() + 1, minText.getY() + 3, 110, minText.getHeight());
        minInput.setColumns(1);
        minInput.setEditable(true);
        minInput.setText(get("min_id"));
        minInput.setName("min_id");
        minInput.getDocument().putProperty("parentComponent", minInput);

        JTextPane maxText = new JTextPane();
        maxText.setBounds(minInput.getX() + minInput.getWidth() + 20, minText.getY(), 26, 20);
        maxText.setText("Max:");
        maxText.setBackground(highlighted);
        maxText.setEditable(false);

        JTextField maxInput = new JTextField();
        maxInput.setBounds(maxText.getX() + maxText.getWidth() + 1, maxText.getY() + 3, 110, maxText.getHeight());
        maxInput.setColumns(1);
        maxInput.setEditable(true);
        maxInput.setText(get("max_id"));
        maxInput.setName("max_id");
        maxInput.getDocument().putProperty("parentComponent", maxInput);

        JButton findNewest = new JButton("<html> <center> <p> Find newest<br> user </p> </center> </html>");
        findNewest.setBounds(maxInput.getX(), maxInput.getY() + maxInput.getHeight() + 3, maxInput.getWidth(), 34);
        findNewest.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findNewest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long highest = Math.max(Long.valueOf(get("max_id_DEFAULT")), Long.valueOf(get("max_id"))); // get the greatest ID between the two

                SwingWorker<Void, Void> rateLimitHandler = new SwingWorker<Void,Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        findNewest.setEnabled(false);
                        findNewest.setText("<html> <center> <p> Rate-limited<br>Slow down! </p> </center> </html>");

                        Thread.sleep(5000L);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (ExecutionException exec) {
                            ErrorHandler.report(exec);
                        } catch (InterruptedException interrupted) {
                            // do nothing
                        } finally {
                            findNewest.setText("<html> <center> <p> Find newest<br> user </p> </center> </html>");
                            findNewest.setEnabled(true);
                        }
                    }
                };

                SwingWorker<Long, Long> userFinder = new SwingWorker<Long,Long>() {
                    @Override
                    protected Long doInBackground() throws IOException {
                        return getInfo.getNewestUser(highest, 100_000_000L, this::publish);
                    }
                        
                    @Override
                    protected void process(List<Long> chunks) {
                        long largest = 0L;

                        for (long val : chunks)
                            if (val > largest)
                                largest = val;
                            
                        maxInput.setText(Long.toString(largest));
                    };

                    @Override
                    protected void done() {
                        try {
                            long id = get();

                            MainWindow.externalPlayerSearch(id);
                        } catch (ExecutionException exc) {
                            if (exc.getCause() instanceof IOException && exc.getCause().getMessage().contains("429"))
                                rateLimitHandler.execute();
                            else
                                ErrorHandler.report(exc);
                        } catch (InterruptedException interrupted) {}
                    }
                };

                userFinder.execute();
            }
        });

        idSetting.add(minText);
        idSetting.add(minInput);
        idSetting.add(maxText);
        idSetting.add(maxInput);
        idSetting.add(findNewest);

        JPanel timezoneSetting = new JPanel();
        timezoneSetting.setBounds(idSetting.getX(), idSetting.getY() + idSetting.getHeight() + 20, idSetting.getWidth(), idSetting.getHeight() + 130);
        timezoneSetting.setLayout(null);
        timezoneSetting.setBackground(highlighted);
        timezoneSetting.setBorder(new TitledBorder(new EtchedBorder(), "Time Zone"));

        Set<String> valid = new HashSet<>(Arrays.asList(TimeZone.getAvailableIDs()));
        valid.removeIf(zone -> zone.toUpperCase() != zone);

        String[] validZones = new String[valid.size()];
        byte ind = 0;

        for (String zone: valid) {
            validZones[ind] = zone;
            ind++;
        }

        Arrays.sort(validZones);

        boolean local = Boolean.valueOf(get("local"));

        JComboBox<String> pickZone = new JComboBox<>(validZones);
        pickZone.setBounds(8, 20, 250, 30);
        pickZone.setEditable(false);
        pickZone.setMaximumRowCount(7);
        pickZone.setName("timezone");
        pickZone.setEnabled(!local);
        pickZone.setSelectedItem(get("timezone"));
        
        JCheckBox useLocal = new JCheckBox("Local", local); // sets whether it uses local timezone
        useLocal.setToolTipText("Tells the program whether to use local time or the selected time.");
        useLocal.setBounds(pickZone.getX() + pickZone.getWidth() + 20, pickZone.getY(), 60, 20);
        useLocal.setBackground(highlighted);
        useLocal.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                pickZone.setEnabled(ie.getStateChange() != ItemEvent.SELECTED);
            }
        });
        useLocal.setName("local");

        timezoneSetting.add(pickZone);
        timezoneSetting.add(useLocal);

        ActionListener actListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComponent comp = (JComponent) e.getSource();

                String val = null;

                if (comp instanceof JCheckBox)
                    val = ((JCheckBox) comp).isSelected() ? "true" : "false";
                else if (comp instanceof JComboBox)
                    val = ((JComboBox<?>) comp).getSelectedItem().toString();

                components.get(comp.getName()).setValue(val);
                isModified();
            }
        };

        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                JTextComponent comp = ((JTextComponent) e.getDocument().getProperty("parentComponent"));

                String text = comp.getText();
                components.get(comp.getName()).setValue(text);

                isModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        };

        minInput.getDocument().addDocumentListener(docListener);
        maxInput.getDocument().addDocumentListener(docListener);
        pickZone.addActionListener(actListener);
        useLocal.addActionListener(actListener);

        panel.add(idSetting);
        panel.add(timezoneSetting);

        components.put(minInput.getName(), new SimpleEntry<JComponent, String>(minInput, get("min_id")));
        components.put(maxInput.getName(), new SimpleEntry<JComponent, String>(maxInput, get("max_id")));
        components.put(pickZone.getName(), new SimpleEntry<JComponent, String>(pickZone, get("timezone")));
        components.put(useLocal.getName(), new SimpleEntry<JComponent, String>(useLocal, get("local")));

        return panel;
    }

    @Override
    public boolean applyChanges() {
        boolean valid = true;

        String min_id = components.get("min_id").getValue();
        String max_id = components.get("max_id").getValue();

        if (isLongValid(min_id) && isLongValid(max_id)) {
            Long textA, textB;
            textA = Long.valueOf(min_id);
            textB = Long.valueOf(max_id);

            if ((textA > 0 && textB > 0) && (textA < textB)) {
                set("min_id", textA.toString());
                set("max_id", textB.toString());
            } else valid = false;
        } else valid = false;

        String local = components.get("local").getValue();
        String timezone = components.get("timezone").getValue();

        set("local", local);
        set("timezone", timezone);

        try {
            saveToFile();

            return valid;
        } catch (IOException writingexc) {
            ErrorHandler.report(writingexc);
        }

        return false; // if it goes past the try-catch block we return invalid
    }
}
