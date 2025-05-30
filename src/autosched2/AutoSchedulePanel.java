package autosched2;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

class MultilineTableCellRenderer extends JTextArea implements TableCellRenderer {
    public MultilineTableCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public java.awt.Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        setText(value == null ? "" : value.toString());
        return this;
    }
}


public class AutoSchedulePanel extends javax.swing.JPanel {
    private List<LabSchedule> schedules;  
    
    public void updateTables() {
    dynamicTablesPanel.removeAll();
    String selected = (String) groupingSelector.getSelectedItem();
    Map<String, List<LabSchedule>> grouped = new TreeMap<>();

    for (LabSchedule s : schedules) {
        String key = switch (selected) {
            case "Group by Lab" -> s.getLab().toString();
            case "Group by Block" -> s.getBlock().toString();
            case "Group by Prof" -> s.getProf().toString();
            default -> "Unknown";
        };
        grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }

    grouped.forEach((title, list) -> {
        // Group further by Week A / B
        Map<String, List<LabSchedule>> weekGrouped = new TreeMap<>();
        for (LabSchedule s : list) {
            String weekKey = "Week " + s.getWeek();
            weekGrouped.computeIfAbsent(weekKey, k -> new ArrayList<>()).add(s);
        }

        weekGrouped.forEach((weekLabel, weekList) -> {
            JLabel lbl = new JLabel("📋 " + title + " - " + weekLabel);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
            dynamicTablesPanel.add(lbl);

            List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

            // Chronological time slots
            List<String> timeOrder = List.of(
                "8AM-10AM", "8AM-12PM", "10AM-12PM",
                "12PM-1PM", // Lunch break
                "1PM-3PM", "1PM-5PM", "3PM-5PM"
            );

            // Collect all used times in the current week group
            Set<String> usedTimes = new HashSet<>();
            for (LabSchedule ls : weekList) usedTimes.add(ls.getTime());

            // Final list of times to display (in order)
            List<String> times = new ArrayList<>();
            for (String t : timeOrder) {
                if (usedTimes.contains(t) || t.equals("12PM-1PM")) {
                    times.add(t);
                }
            }

            List<String> colHeaders = new ArrayList<>();
            colHeaders.add("Time");
            colHeaders.addAll(days);

            DefaultTableModel m = new DefaultTableModel(colHeaders.toArray(), 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (String time : times) {
                List<String> row = new ArrayList<>();
                row.add(time);

                for (String day : days) {
                    LabSchedule match = null;
                    for (LabSchedule ls : weekList) {
                        if (ls.getDay().equals(day) && ls.getTime().equals(time)) {
                            match = ls;
                            break;
                        }
                    }

                    if (time.equals("12PM-1PM")) {
                        row.add("⏸ Lunch Break");
                    } else if (match != null) {
                        row.add(match.getCode() + "\n" + match.getBlock() + "\n" + match.getLab() +
                                "\n\n" + match.getProf() + "\n\n" + match.getSubject());
                    } else {
                        row.add("");
                    }
                }

                m.addRow(row.toArray());
            }

            JTable tbl = new JTable(m);
            tbl.setRowHeight(250);
            for (int i = 0; i < tbl.getColumnCount(); i++) {
                tbl.getColumnModel().getColumn(i).setCellRenderer(new MultilineTableCellRenderer());
            }

            JScrollPane sp = new JScrollPane(tbl);
            sp.setPreferredSize(new Dimension(700, 500));
            dynamicTablesPanel.add(sp);
            dynamicTablesPanel.add(Box.createVerticalStrut(30));
        });
    });

    dynamicTablesPanel.revalidate();
    dynamicTablesPanel.repaint();
}


    
    public AutoSchedulePanel(List<LabSchedule> schedules) {
        this.schedules = schedules;
        initComponents();
        dynamicTablesPanel.setLayout(new BoxLayout(dynamicTablesPanel, BoxLayout.Y_AXIS));

        String[] groupOptions = {"Group by Lab", "Group by Block", "Group by Prof"};
        for (String opt : groupOptions) {
            groupingSelector.addItem(opt);
        }

    groupingSelector.addActionListener(e -> updateTables());
    updateTables();
}
 
    public void reloadSchedules(List<LabSchedule> newSchedules) {
    this.schedules = newSchedules;
    updateTables(); 
}

    
    public void updateSchedules(List<LabSchedule> newSched) {
    this.schedules = newSched;
}

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        dynamicTablesPanel = new javax.swing.JPanel();
        groupingSelector = new javax.swing.JComboBox<>();
        labPrint = new javax.swing.JButton();
        blockPrint = new javax.swing.JButton();
        blockPrint1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 255, 204));

        javax.swing.GroupLayout dynamicTablesPanelLayout = new javax.swing.GroupLayout(dynamicTablesPanel);
        dynamicTablesPanel.setLayout(dynamicTablesPanelLayout);
        dynamicTablesPanelLayout.setHorizontalGroup(
            dynamicTablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1066, Short.MAX_VALUE)
        );
        dynamicTablesPanelLayout.setVerticalGroup(
            dynamicTablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 735, Short.MAX_VALUE)
        );

        scrollPane.setViewportView(dynamicTablesPanel);

        labPrint.setBackground(new java.awt.Color(0, 204, 0));
        labPrint.setText("Print (Group by Lab)");
        labPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labPrintActionPerformed(evt);
            }
        });

        blockPrint.setBackground(new java.awt.Color(0, 204, 0));
        blockPrint.setText("Print (Group by Block)");
        blockPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blockPrintActionPerformed(evt);
            }
        });

        blockPrint1.setBackground(new java.awt.Color(0, 204, 0));
        blockPrint1.setText("Print (Group by Prof)");
        blockPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blockPrint1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupingSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labPrint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blockPrint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blockPrint1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1087, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupingSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labPrint)
                    .addComponent(blockPrint)
                    .addComponent(blockPrint1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 690, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void labPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labPrintActionPerformed
            Map<String, Map<String, List<LabSchedule>>> groupedForExport = new TreeMap<>();
            for (LabSchedule s : schedules) {
                groupedForExport
                    .computeIfAbsent(s.getLab().toString(), k -> new TreeMap<>())
                    .computeIfAbsent("Week " + s.getWeek(), k -> new ArrayList<>())
                    .add(s);
            }

            try {
                String documentsPath = System.getProperty("user.home") + "/Documents/AutoSched";
                new File(documentsPath).mkdirs(); // Ensure the folder exists

                String outputPath = documentsPath + "/BSIT_lab_schedules.docx";
                DocxScheduleExporter.exportSchedules(groupedForExport, outputPath);

                JOptionPane.showMessageDialog(this, "Schedule exported to:\n" + outputPath);
            } catch (IOException ex) {
                Logger.getLogger(AutoSchedulePanel.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Failed to export schedule.");
            }
    }//GEN-LAST:event_labPrintActionPerformed

    private void blockPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blockPrintActionPerformed
            Map<String, Map<String, List<LabSchedule>>> groupedForExport = new TreeMap<>();
            for (LabSchedule s : schedules) {
                groupedForExport
                    .computeIfAbsent(s.getBlock().toString(), k -> new TreeMap<>())
                    .computeIfAbsent("Week " + s.getWeek(), k -> new ArrayList<>())
                    .add(s);
            }
            try {
                String documentsPath = System.getProperty("user.home") + "/Documents/AutoSched";
                new File(documentsPath).mkdirs(); // Ensure the folder exists

                String outputPath = documentsPath + "/BSIT_block_schedules.docx";
                DocxScheduleExporter.exportSchedules(groupedForExport, outputPath);

                JOptionPane.showMessageDialog(this, "Schedule exported to:\n" + outputPath);
            } catch (IOException ex) {
                Logger.getLogger(AutoSchedulePanel.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Failed to export schedule.");
            }
    }//GEN-LAST:event_blockPrintActionPerformed

    private void blockPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blockPrint1ActionPerformed
        Map<String, Map<String, List<LabSchedule>>> groupedForExport = new TreeMap<>();
            for (LabSchedule s : schedules) {
                groupedForExport
                    .computeIfAbsent(s.getProf().toString(), k -> new TreeMap<>())
                    .computeIfAbsent("Week " + s.getWeek(), k -> new ArrayList<>())
                    .add(s);
            }
            try {
                String documentsPath = System.getProperty("user.home") + "/Documents/AutoSched";
                new File(documentsPath).mkdirs(); // Ensure the folder exists

                String outputPath = documentsPath + "/BSIT_prof_schedules.docx";
                DocxScheduleExporter.exportSchedules(groupedForExport, outputPath);

                JOptionPane.showMessageDialog(this, "Schedule exported to:\n" + outputPath);
            } catch (IOException ex) {
                Logger.getLogger(AutoSchedulePanel.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Failed to export schedule.");
            }
    }//GEN-LAST:event_blockPrint1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton blockPrint;
    private javax.swing.JButton blockPrint1;
    private javax.swing.JPanel dynamicTablesPanel;
    private javax.swing.JComboBox<String> groupingSelector;
    private javax.swing.JButton labPrint;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
