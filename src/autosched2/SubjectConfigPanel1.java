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
public class SubjectConfigPanel1 extends javax.swing.JPanel {
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

    String[] twoHourMorningSlots = {"8AM-10AM", "10AM-12PM"};
    String[] twoHourAfternoonSlots = {"1PM-3PM", "3PM-5PM"};

    // Helper class to track assignment status per lab-week-day
    class LabBlockStatus {
        boolean morningFullBlockAssigned = false;
        boolean afternoonFullBlockAssigned = false;
        Set<String> morningTwoHourSlotsUsed = new HashSet<>();
        Set<String> afternoonTwoHourSlotsUsed = new HashSet<>();
    }

    Map<String, LabBlockStatus> labDayStatus = new HashMap<>();

    String[] blockLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    Object[][] tablesData = {
        {sem_1B, block_1_B.getValue(), "1", "", "B"},
        {sem_2B, block_2_B.getValue(), "2", "", "B"},
        {sem_3AB, block_3_A_B.getValue(), "3", "AP", "B"},
        {sem_3BB, block_3_B_B.getValue(), "3", "DD", "B"},
        {sem_4AB, block_4_A_B.getValue(), "4", "AP", "B"},
        {sem_4BB, block_4_B_B.getValue(), "4", "DD", "B"}
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

                if (level.equals("5")) {
                    outerLevel5:
                    for (String week : weeks) {
                        for (int labIndex = 0; labIndex < LABS; labIndex++) {
                            String labName = labNames[labIndex];
                            for (String day : days) {
                                String key = labName + "_" + week + "_" + day;
                                LabBlockStatus status = labDayStatus.getOrDefault(key, new LabBlockStatus());

                                // Morning full block, only if no 2-hour morning slots used
                                if (!status.morningFullBlockAssigned && status.morningTwoHourSlotsUsed.isEmpty()) {
                                    status.morningFullBlockAssigned = true;
                                    labDayStatus.put(key, status);
                                    finalSchedules.add(new LabSchedule(labName, week, day, "8AM-12PM", code, name, unitRaw, blockName));
                                    scheduled = true;
                                    break outerLevel5;
                                }

                                // Afternoon full block, only if no 2-hour afternoon slots used
                                if (!status.afternoonFullBlockAssigned && status.afternoonTwoHourSlotsUsed.isEmpty()) {
                                    status.afternoonFullBlockAssigned = true;
                                    labDayStatus.put(key, status);
                                    finalSchedules.add(new LabSchedule(labName, week, day, "1PM-5PM", code, name, unitRaw, blockName));
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

                                // Morning 2-hour slots, only if no morning full block assigned
                                for (String slot : twoHourMorningSlots) {
                                    if (!status.morningFullBlockAssigned && !status.morningTwoHourSlotsUsed.contains(slot)) {
                                        status.morningTwoHourSlotsUsed.add(slot);
                                        labDayStatus.put(key, status);
                                        finalSchedules.add(new LabSchedule(labName, week, day, slot, code, name, unitRaw, blockName));
                                        scheduled = true;
                                        break outerLevel2;
                                    }
                                }
                                if (scheduled) break;

                                // Afternoon 2-hour slots, only if no afternoon full block assigned
                                for (String slot : twoHourAfternoonSlots) {
                                    if (!status.afternoonFullBlockAssigned && !status.afternoonTwoHourSlotsUsed.contains(slot)) {
                                        status.afternoonTwoHourSlotsUsed.add(slot);
                                        labDayStatus.put(key, status);
                                        finalSchedules.add(new LabSchedule(labName, week, day, slot, code, name, unitRaw, blockName));
                                        scheduled = true;
                                        break outerLevel2;
                                    }
                                }
                            }
                        }
                    }
                }

                if (!scheduled) {
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
    public SubjectConfigPanel1() {
        initComponents();
        JSpinner[] spinners = {
            block_1_B, block_2_B, block_3_A_B, block_3_B_B, block_4_A_B, block_4_B_B
        };

        JTable[] tables = {
            sem_1B, sem_2B, sem_3AB, sem_3BB, sem_4AB, sem_4BB
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sem_1B = new javax.swing.JTable();
        block_1_B = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        sem_2B = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        block_2_B = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        sem_3BB = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        block_3_B_B = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        block_3_A_B = new javax.swing.JSpinner();
        jScrollPane9 = new javax.swing.JScrollPane();
        sem_3AB = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        sem_4AB = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        block_4_A_B = new javax.swing.JSpinner();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        sem_4BB = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        block_4_B_B = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 204, 255));

        jPanel1.setBackground(new java.awt.Color(255, 204, 204));
        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 36)); // NOI18N
        jLabel1.setText("Second Semester Configurations");

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 22)); // NOI18N
        jLabel3.setText("Year Levels");

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 153)));

        sem_1B.setBackground(new java.awt.Color(255, 204, 255));
        sem_1B.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"CC102", "Programming 2", "", "5"},
                {"ITSY101", "Introduction to IT Systems", "", "5"},
                {"CC103", "Data Structures and Algorithms", "", "5"},
                {"GE2", "Reading in Philippine History", "", "2"},
                {"GE6", "Art Appreciation", "", "2"},
                {"GE3", "The Contemporary Word / Ang Kasalukuyang Daigdig", "", "2"},
                {"GE10", "Kontekswalisadong Komunikasyon sa Filipino", "", "2"},
                {"PE2", "Rhythmic Activities", "", "2"}
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
        sem_1B.setRowHeight(20);
        sem_1B.setRowMargin(2);
        sem_1B.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(sem_1B);
        if (sem_1B.getColumnModel().getColumnCount() > 0) {
            sem_1B.getColumnModel().getColumn(0).setResizable(false);
            sem_1B.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_1B.getColumnModel().getColumn(1).setResizable(false);
            sem_1B.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_1B.getColumnModel().getColumn(2).setResizable(false);
            sem_1B.getColumnModel().getColumn(3).setResizable(false);
            sem_1B.getColumnModel().getColumn(3).setPreferredWidth(5);
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

        sem_2B.setBackground(new java.awt.Color(255, 204, 255));
        sem_2B.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"CC105", "Application Developments and Emerging Technologies", "", "5"},
                {"MS101", "Discrete Mathematics", null, "5"},
                {"NET102", "Networking 2", null, "5"},
                {"VD100", "Introduction to Visual Design", null, "5"},
                {"HCI101", "Introduction to Human Computer Interaction", null, "5"},
                {"GE11", "Filipino sa Iba't-Ibang Disiplina", null, "2"},
                {"GE14", "Philippine Popular Cultural", null, "2"},
                {"PE4", "Team Sports", null, "2"}
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
        sem_2B.setRowHeight(20);
        sem_2B.setRowMargin(2);
        sem_2B.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(sem_2B);
        if (sem_2B.getColumnModel().getColumnCount() > 0) {
            sem_2B.getColumnModel().getColumn(0).setResizable(false);
            sem_2B.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_2B.getColumnModel().getColumn(1).setResizable(false);
            sem_2B.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_2B.getColumnModel().getColumn(2).setResizable(false);
            sem_2B.getColumnModel().getColumn(3).setResizable(false);
            sem_2B.getColumnModel().getColumn(3).setPreferredWidth(5);
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

        sem_3BB.setBackground(new java.awt.Color(255, 204, 255));
        sem_3BB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"IPT101", "Integrative Programming Technologies", "", "5"},
                {"PT101", "Platform Technologies", "", "5"},
                {"PROG3", "Object Oriented Programming", "", "5"},
                {"TECH101", "Technical and Professional Communications", "", "5"},
                {"ITBusi101", "IT Business Ventures", "", "5"},
                {"GE17", "People and the Earht's Ecosystem", "", "2"},
                {"ITElec2A", "Fundamentals of Digital Sound Production", "", "5"},
                {"ITElec3A", "Video Processing", "", "5"}
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
        sem_3BB.setRowHeight(20);
        sem_3BB.setRowMargin(2);
        sem_3BB.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(sem_3BB);
        if (sem_3BB.getColumnModel().getColumnCount() > 0) {
            sem_3BB.getColumnModel().getColumn(0).setResizable(false);
            sem_3BB.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_3BB.getColumnModel().getColumn(1).setResizable(false);
            sem_3BB.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_3BB.getColumnModel().getColumn(2).setResizable(false);
            sem_3BB.getColumnModel().getColumn(3).setResizable(false);
            sem_3BB.getColumnModel().getColumn(3).setPreferredWidth(5);
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

        sem_3AB.setBackground(new java.awt.Color(255, 204, 255));
        sem_3AB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"IPT101", "Integrative Programming Technologies", "", "5"},
                {"PT101", "Platform Technologies", "", "5"},
                {"PROG3", "Objeect Oriented Programming", "", "5"},
                {"TECH101", "Technical and Professional Communications", "", "5"},
                {"ITBusi101", "IT Business Venture", "", "5"},
                {"GE17", "People and the Earth's  Ecosystem", "", "2"},
                {"ITElect2", "Open-Source Computing ", "", "5"},
                {"ITElect3", "Web Systems and Technologies", "", "5"}
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
        sem_3AB.setRowHeight(20);
        sem_3AB.setRowMargin(2);
        sem_3AB.getTableHeader().setReorderingAllowed(false);
        jScrollPane9.setViewportView(sem_3AB);
        if (sem_3AB.getColumnModel().getColumnCount() > 0) {
            sem_3AB.getColumnModel().getColumn(0).setResizable(false);
            sem_3AB.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_3AB.getColumnModel().getColumn(1).setResizable(false);
            sem_3AB.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_3AB.getColumnModel().getColumn(2).setResizable(false);
            sem_3AB.getColumnModel().getColumn(3).setResizable(false);
            sem_3AB.getColumnModel().getColumn(3).setPreferredWidth(5);
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

        sem_4AB.setBackground(new java.awt.Color(255, 204, 255));
        sem_4AB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Prac101", "Practicum", "", "0"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sem_4AB.setRowHeight(20);
        sem_4AB.setRowMargin(2);
        sem_4AB.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(sem_4AB);
        if (sem_4AB.getColumnModel().getColumnCount() > 0) {
            sem_4AB.getColumnModel().getColumn(0).setResizable(false);
            sem_4AB.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_4AB.getColumnModel().getColumn(1).setResizable(false);
            sem_4AB.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_4AB.getColumnModel().getColumn(2).setResizable(false);
            sem_4AB.getColumnModel().getColumn(3).setResizable(false);
            sem_4AB.getColumnModel().getColumn(3).setPreferredWidth(5);
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

        sem_4BB.setBackground(new java.awt.Color(255, 204, 255));
        sem_4BB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Prac101", "Practicum", "", "0"}
            },
            new String [] {
                "Course Code", "Name", "Prof Name", "Lab Requirement Level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sem_4BB.setRowHeight(20);
        sem_4BB.setRowMargin(2);
        sem_4BB.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(sem_4BB);
        if (sem_4BB.getColumnModel().getColumnCount() > 0) {
            sem_4BB.getColumnModel().getColumn(0).setResizable(false);
            sem_4BB.getColumnModel().getColumn(0).setPreferredWidth(10);
            sem_4BB.getColumnModel().getColumn(1).setResizable(false);
            sem_4BB.getColumnModel().getColumn(1).setPreferredWidth(200);
            sem_4BB.getColumnModel().getColumn(2).setResizable(false);
            sem_4BB.getColumnModel().getColumn(3).setResizable(false);
            sem_4BB.getColumnModel().getColumn(3).setPreferredWidth(5);
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

        jLabel11.setText("5 = Required Lab Hours");

        jLabel12.setText("4 = Not as required");

        jLabel13.setText("3 = Might require for some activities");

        jLabel14.setText("2 = Not needed");

        jLabel15.setText("1 = For Minor Subject");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 2, 16)); // NOI18N
        jLabel2.setText("Reminder: Lab Requirement Level means \"how much that subject will need a lab schedule\"");

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
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
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
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton1))
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 802, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 884, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 879, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private List<LabSchedule> finalSchedules = new ArrayList<>();

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        generateSchedules();
        List<LabSchedule> schedules = getFinalSchedules();
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
    private javax.swing.JTable sem_1B;
    private javax.swing.JTable sem_2B;
    private javax.swing.JTable sem_3AB;
    private javax.swing.JTable sem_3BB;
    private javax.swing.JTable sem_4AB;
    private javax.swing.JTable sem_4BB;
    private javax.swing.ButtonGroup semester_choice;
    // End of variables declaration//GEN-END:variables
}
