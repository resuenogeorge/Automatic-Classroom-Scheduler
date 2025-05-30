package autosched2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Acer
 */
public class SubjectConfigPanel extends javax.swing.JPanel {
    private AutoSchedulePanel autoSchedulePanel; 
public void generateSchedules() {
    finalSchedules.clear();

    String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    String[] weeks = {"A", "B"};
    int LABS = 9;

    String[] labNames = {
        "Lab 7", "ICTC Lobby", "NSTP Shed", "Smart Classroom/Lab 3",
        "Lab 2A", "Lab 2B", "Lab 5", "CSS Lab", "Lab 1"
    };

    // Time slots
    String[] fullBlocksMorning = {"8AM-12PM"};
    String[] fullBlocksAfternoon = {"1PM-5PM"};

    String[] twoHourMorningSlots = {"8AM-10AM", "10AM-12PM"};
    String[] twoHourAfternoonSlots = {"1PM-3PM", "3PM-5PM"};

    // Helper class to track per lab+week+day block assignment status
    class LabBlockStatus {
        boolean morningFullBlockAssigned = false; 
        boolean afternoonFullBlockAssigned = false; 
        Set<String> morningTwoHourSlotsUsed = new HashSet<>();
        Set<String> afternoonTwoHourSlotsUsed = new HashSet<>();
    }

    Map<String, LabBlockStatus> labDayStatus = new HashMap<>();

    // Helper method to get key for lab-day-week
    java.util.function.Function<String[], String> getLabDayWeekKey = (String[] params) -> {
        // params = {labName, week, day}
        return params[0] + "_" + params[1] + "_" + params[2];
    };

    String[] blockLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    Object[][] tablesData = {
        {sem_1A, block_1_B.getValue(), "1", "", "B"},
        {sem_2A, block_2_B.getValue(), "2", "", "B"},
        {sem_3AA, block_3_A_B.getValue(), "3", "AP", "B"},
        {sem_3BA, block_3_B_B.getValue(), "3", "DD", "B"},
        {sem_4AA, block_4_A_B.getValue(), "4", "AP", "B"},
        {sem_4BA, block_4_B_B.getValue(), "4", "DD", "B"}
    };

    for (Object[] row : tablesData) {
        JTable table = (JTable) row[0];
        int blockCount = (int) row[1];
        String year = (String) row[2];
        String major = (String) row[3];
        String sem = (String) row[4];

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            String code = model.getValueAt(i, 0).toString();
            String name = model.getValueAt(i, 1).toString();
            String unitRaw = model.getValueAt(i, 2).toString();
            Object levelObj = model.getValueAt(i, 3);
            String level = (levelObj != null) ? levelObj.toString() : "2";

            if (!level.equals("2") && !level.equals("5")) continue;

            for (int b = 0; b < blockCount; b++) {
                String blockName = "BSIT " + major + "-" + year + blockLetters[b];

                boolean scheduled = false;

                // Try scheduling
                if (level.equals("5")) {
                    outerLevel5:
                    for (String week : weeks) {
                        for (int labIndex = 0; labIndex < LABS; labIndex++) {
                            String labName = labNames[labIndex];
                            for (String day : days) {
                                String key = labName + "_" + week + "_" + day;
                                LabBlockStatus status = labDayStatus.getOrDefault(key, new LabBlockStatus());

                                // Check morning full block
                                if (!status.morningFullBlockAssigned
                                        && status.morningTwoHourSlotsUsed.isEmpty()) {
                                    // Assign morning full block
                                    status.morningFullBlockAssigned = true;
                                    labDayStatus.put(key, status);
                                    LabSchedule sched = new LabSchedule(labName, week, day, "8AM-12PM", code, name, unitRaw, blockName);
                                    finalSchedules.add(sched);
                                    scheduled = true;
                                    break outerLevel5;
                                }

                                // Check afternoon full block
                                if (!status.afternoonFullBlockAssigned
                                        && status.afternoonTwoHourSlotsUsed.isEmpty()) {
                                    status.afternoonFullBlockAssigned = true;
                                    labDayStatus.put(key, status);
                                    LabSchedule sched = new LabSchedule(labName, week, day, "1PM-5PM", code, name, unitRaw, blockName);
                                    finalSchedules.add(sched);
                                    scheduled = true;
                                    break outerLevel5;
                                }
                            }
                        }
                    }
                } else if (level.equals("2")) {
                    outerLevel2:
                    for (String week : weeks) {
                        for (int labIndex = 0; labIndex < LABS; labIndex++) {
                            String labName = labNames[labIndex];
                            for (String day : days) {
                                String key = labName + "_" + week + "_" + day;
                                LabBlockStatus status = labDayStatus.getOrDefault(key, new LabBlockStatus());

                                // Try morning 2-hour slots
                                for (String slot : twoHourMorningSlots) {
                                    // Can only assign if no full block assigned morning
                                    if (!status.morningFullBlockAssigned
                                            && !status.morningTwoHourSlotsUsed.contains(slot)) {
                                        status.morningTwoHourSlotsUsed.add(slot);
                                        labDayStatus.put(key, status);
                                        LabSchedule sched = new LabSchedule(labName, week, day, slot, code, name, unitRaw, blockName);
                                        finalSchedules.add(sched);
                                        scheduled = true;
                                        break outerLevel2;
                                    }
                                }

                                if (scheduled) break;

                                // Try afternoon 2-hour slots
                                for (String slot : twoHourAfternoonSlots) {
                                    if (!status.afternoonFullBlockAssigned
                                            && !status.afternoonTwoHourSlotsUsed.contains(slot)) {
                                        status.afternoonTwoHourSlotsUsed.add(slot);
                                        labDayStatus.put(key, status);
                                        LabSchedule sched = new LabSchedule(labName, week, day, slot, code, name, unitRaw, blockName);
                                        finalSchedules.add(sched);
                                        scheduled = true;
                                        break outerLevel2;
                                    }
                                }
                            }
                        }
                    }
                }

                if (!scheduled) {
                    // Fallback no lab assigned
                    LabSchedule sched = new LabSchedule("⚠️ Assigned NO LAB", "A", "Monday", "8AM-10AM", code, name, unitRaw, blockName);
                    finalSchedules.add(sched);
                    System.out.println("❌ No slot for " + code + " " + blockName);
                }
            }
        }
    }

    System.out.println("\n✅ FINAL SCHEDULES:");
    for (LabSchedule sched : finalSchedules) {
        System.out.println(sched);
    }
}

    
    public SubjectConfigPanel() {
        initComponents();
        JSpinner[] spinners = {
            block_1_B, block_2_B, block_3_A_B, block_3_B_B, block_4_A_B, block_4_B_B
        };

        JTable[] tables = {
            sem_1A, sem_2A, sem_3AA, sem_3BA, sem_4AA, sem_4BA
        };

        String[] professorList = {
            "TBA", "Christopher Marmol", "Meneleo Roa", "Christian Roxas", "Jan Leo Crisanto",
            "Mayreen Amazona", "Cristelita Lombres", "Edna Napoles", "Von Gerald Macose",
            "Don Sean Arvie Buencamino", "Austin Carl Enriquez", "Alexis Bautista",
            "Keyvenz Portera", "King Alvin Grospe", "Engelbert Babiera", "Ryan Christian Nono"
        };

        String[] labLevels = {"0", "1", "2", "3", "4", "5"};

        for (int i = 0; i < tables.length; i++) {
            JTable table = tables[i];
             JComboBox<String> profCombo = new JComboBox<>(professorList);
            JComboBox<String> levelCombo = new JComboBox<>(labLevels);

            table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(profCombo));
            table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(levelCombo));

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int row = 0; row < model.getRowCount(); row++) {
                if (model.getValueAt(row, 2) == null || model.getValueAt(row, 2).toString().trim().isEmpty()) {
                    model.setValueAt("TBA", row, 2);
                }
                if (model.getValueAt(row, 3) == null || model.getValueAt(row, 3).toString().trim().isEmpty()) {
                    model.setValueAt("1", row, 3);
                }
            }

            spinners[i].setModel(new SpinnerNumberModel(1, 1, 10, 1));
        }

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        semester_choice = new javax.swing.ButtonGroup();
        jCheckBox1 = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sem_1A = new javax.swing.JTable();
        block_1_B = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        sem_2A = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        block_2_B = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        sem_3BA = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        block_3_B_B = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        block_3_A_B = new javax.swing.JSpinner();
        jScrollPane9 = new javax.swing.JScrollPane();
        sem_3AA = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        sem_4AA = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        block_4_A_B = new javax.swing.JSpinner();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        sem_4BA = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        block_4_B_B = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();

        jCheckBox1.setText("jCheckBox1");

        setBackground(new java.awt.Color(255, 204, 255));

        jPanel1.setBackground(new java.awt.Color(255, 204, 204));
        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 36)); // NOI18N
        jLabel1.setText("First Semester Configurations");

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 22)); // NOI18N
        jLabel3.setText("Year Levels");

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 153)));

        sem_1A.setBackground(new java.awt.Color(255, 204, 255));
        sem_1A.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"CC100", "Introduction to Computing", null, "5"},
                {"CC101", "Programming 1", null, "5"},
                {"IM101", "Fundamentals of Database System", null, "5"},
                {"GE5", "Purposive Communication / Malayuning Komunikasyon", null, "2"},
                {"GE1", "Understanding the Self /  Pang Unawa sa Sarili", null, "2"},
                {"GE4", "Mathematics in the Modern Word /  Matematika sa Makaqbagong Daigdig", null, "2"},
                {"PE1", "Self-Testing Activities", null, "2"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        sem_1A.setRowHeight(20);
        sem_1A.setRowMargin(2);
        sem_1A.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(sem_1A);
        if (sem_1A.getColumnModel().getColumnCount() > 0) {
            sem_1A.getColumnModel().getColumn(0).setResizable(false);
            sem_1A.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_1A.getColumnModel().getColumn(1).setResizable(false);
            sem_1A.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_1A.getColumnModel().getColumn(2).setResizable(false);
            sem_1A.getColumnModel().getColumn(3).setResizable(false);
            sem_1A.getColumnModel().getColumn(3).setPreferredWidth(5);
        }

        jLabel5.setText("How many blocks?");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 765, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(block_1_B, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(block_1_B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(334, 334, 334))
        );

        jTabbedPane1.addTab("1st Year", jPanel3);

        sem_2A.setBackground(new java.awt.Color(255, 204, 255));
        sem_2A.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"NET101", "Networking 1", "", "5"},
                {"DP101", "Deskstop Publishing", "", "5"},
                {"ITSYS102", "Web and multimedia Systems", "", "5"},
                {"CC104", "Information Management", "", "5"},
                {"GE8", "Ethics / Etika", "", "2"},
                {"GE7", "Science, Technology & Society / Agham, Teknolohiya at Lipunan", "", "2"},
                {"GE9", "Life and Works of Rizal", "", "2"},
                {"PE3", "Individual and Dual Sports", "", "2"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        sem_2A.setRowHeight(20);
        sem_2A.setRowMargin(2);
        sem_2A.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(sem_2A);
        if (sem_2A.getColumnModel().getColumnCount() > 0) {
            sem_2A.getColumnModel().getColumn(0).setResizable(false);
            sem_2A.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_2A.getColumnModel().getColumn(1).setResizable(false);
            sem_2A.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_2A.getColumnModel().getColumn(2).setResizable(false);
            sem_2A.getColumnModel().getColumn(3).setResizable(false);
            sem_2A.getColumnModel().getColumn(3).setPreferredWidth(5);
        }

        jLabel6.setText("How many blocks?");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(block_2_B, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(block_2_B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("2nd Year", jPanel4);

        sem_3BA.setBackground(new java.awt.Color(255, 204, 255));
        sem_3BA.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"IAS101", "Information Assurance and Security", "", "5"},
                {"MS102", "Quantitative Methods", "", "5"},
                {"ITTrends100", "Current Trends in IT", "", "5"},
                {"SA101", "System Administration and Maintenance", "", "5"},
                {"ITElec1A", "Fundamental of Video production", "", "5"},
                {"MST100", "Mathematics and Statistic for IT", "", "5"},
                {"VD101", "Reading Visual Arts", "", "5"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        sem_3BA.setRowHeight(20);
        sem_3BA.setRowMargin(2);
        sem_3BA.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(sem_3BA);
        if (sem_3BA.getColumnModel().getColumnCount() > 0) {
            sem_3BA.getColumnModel().getColumn(0).setResizable(false);
            sem_3BA.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_3BA.getColumnModel().getColumn(1).setResizable(false);
            sem_3BA.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_3BA.getColumnModel().getColumn(2).setResizable(false);
            sem_3BA.getColumnModel().getColumn(3).setResizable(false);
            sem_3BA.getColumnModel().getColumn(3).setPreferredWidth(5);
        }

        jLabel8.setText("How many blocks?");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(block_3_B_B, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(block_3_B_B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("3rd Year - DD", jPanel2);

        jLabel7.setText("How many blocks?");

        sem_3AA.setBackground(new java.awt.Color(255, 204, 255));
        sem_3AA.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"IAS101", "Information Assurance and Security", "", "5"},
                {"MS102", "Quantitative Methods", "", "5"},
                {"ITTrends100", "Current Trends in IT", "", "5"},
                {"SA101", "System Administation and Maintenance", "", "5"},
                {"ITElec1", "Agile Programming", "", "5"},
                {"MST100", "Mathematics and Statistics", "", "5"},
                {"VD101", "Reading Visual Arts", "", "5"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        sem_3AA.setRowHeight(20);
        sem_3AA.setRowMargin(2);
        sem_3AA.getTableHeader().setReorderingAllowed(false);
        jScrollPane9.setViewportView(sem_3AA);
        if (sem_3AA.getColumnModel().getColumnCount() > 0) {
            sem_3AA.getColumnModel().getColumn(0).setResizable(false);
            sem_3AA.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_3AA.getColumnModel().getColumn(1).setResizable(false);
            sem_3AA.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_3AA.getColumnModel().getColumn(2).setResizable(false);
            sem_3AA.getColumnModel().getColumn(3).setResizable(false);
            sem_3AA.getColumnModel().getColumn(3).setPreferredWidth(5);
        }

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(block_3_A_B, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(block_3_A_B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("3rd Year - AP", jPanel5);

        sem_4AA.setBackground(new java.awt.Color(255, 204, 255));
        sem_4AA.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"CAP102", "Capstone Project and Researh 2", "", "5"},
                {"SP101", "Social and Professional Issue in IT", "", "5"},
                {"ITElec4", "Mobile and Wireless Computing ", "", "5"},
                {"ITElec5", "Game Development", "", "5"},
                {"IAS102", "Cybersecurity", "", "5"},
                {"ITEngl100", "English for IT", "", "5"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        sem_4AA.setRowHeight(20);
        sem_4AA.setRowMargin(2);
        sem_4AA.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(sem_4AA);
        if (sem_4AA.getColumnModel().getColumnCount() > 0) {
            sem_4AA.getColumnModel().getColumn(0).setResizable(false);
            sem_4AA.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_4AA.getColumnModel().getColumn(1).setResizable(false);
            sem_4AA.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_4AA.getColumnModel().getColumn(2).setResizable(false);
            sem_4AA.getColumnModel().getColumn(3).setResizable(false);
            sem_4AA.getColumnModel().getColumn(3).setPreferredWidth(5);
        }

        jLabel9.setText("How many blocks?");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(block_4_A_B, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(block_4_A_B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("4th Year - AP", jPanel6);

        sem_4BA.setBackground(new java.awt.Color(255, 204, 255));
        sem_4BA.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"CAP102", "Capstone Project and Research 2", "", "5"},
                {"SP101", "Social and Propessional Issues in IT", "", "5"},
                {"ITElec4A", "Image Processing ", "", "5"},
                {"ITElec5A", "Organic Modeling", "", "5"},
                {"IAS102", "Cybersecurity", "", "5"},
                {"ITEngl100", "English for IT", "", "5"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        sem_4BA.setRowHeight(20);
        sem_4BA.setRowMargin(2);
        sem_4BA.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(sem_4BA);
        if (sem_4BA.getColumnModel().getColumnCount() > 0) {
            sem_4BA.getColumnModel().getColumn(0).setResizable(false);
            sem_4BA.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_4BA.getColumnModel().getColumn(1).setResizable(false);
            sem_4BA.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_4BA.getColumnModel().getColumn(2).setResizable(false);
            sem_4BA.getColumnModel().getColumn(3).setResizable(false);
            sem_4BA.getColumnModel().getColumn(3).setPreferredWidth(5);
        }

        jLabel10.setText("How many blocks?");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(block_4_B_B, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(block_4_B_B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("4th Year - DD", jPanel7);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 0, 102));
        jLabel4.setText("Subject Probability Editor");

        jButton1.setBackground(new java.awt.Color(51, 255, 51));
        jButton1.setText("Run the Scheduler");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 2, 16)); // NOI18N
        jLabel2.setText("Reminder: Lab Requirement Level means \"how much that subject will need a lab schedule\"");

        jLabel11.setText("5 = Required Lab Hours");

        jLabel12.setText("4 = Not as required");

        jLabel13.setText("3 = Might require for some activities");

        jLabel14.setText("2 = Not needed");

        jLabel15.setText("1 = For Minor Subject");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(30, 30, 30)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel14)
                                        .addComponent(jLabel15)
                                        .addComponent(jLabel11)
                                        .addComponent(jLabel12)
                                        .addComponent(jLabel13))))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1))
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 802, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel15)))
                .addContainerGap())
        );

        jScrollPane2.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 855, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private List<LabSchedule> finalSchedules = new ArrayList<>();

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        generateSchedules();
        if (autoSchedulePanel != null) {
            autoSchedulePanel.reloadSchedules(getFinalSchedules());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

public javax.swing.JButton getGenerateButton() {
    return jButton1;
}

public void setAutoSchedulePanel(AutoSchedulePanel panel) {
    this.autoSchedulePanel = panel;
}
    
public List<LabSchedule> getFinalSchedules() {
    return finalSchedules;
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner block_1_B;
    private javax.swing.JSpinner block_2_B;
    private javax.swing.JSpinner block_3_A_B;
    private javax.swing.JSpinner block_3_B_B;
    private javax.swing.JSpinner block_4_A_B;
    private javax.swing.JSpinner block_4_B_B;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable sem_1A;
    private javax.swing.JTable sem_2A;
    private javax.swing.JTable sem_3AA;
    private javax.swing.JTable sem_3BA;
    private javax.swing.JTable sem_4AA;
    private javax.swing.JTable sem_4BA;
    private javax.swing.ButtonGroup semester_choice;
    // End of variables declaration//GEN-END:variables
}
