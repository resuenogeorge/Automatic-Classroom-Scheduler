package autosched2;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocxScheduleExporter {

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] TIME_SLOTS = {"8AM-10AM", "10AM-12PM", "12PM-1PM", "1PM-3PM", "3PM-5PM"};

    public static void exportSchedules(Map<String, Map<String, List<LabSchedule>>> groupedSchedules, String outputPath) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            for (String group : groupedSchedules.keySet()) {
                XWPFParagraph pGroup = doc.createParagraph();
                pGroup.setStyle("Heading1");
                XWPFRun rGroup = pGroup.createRun();
                rGroup.setText(group);
                rGroup.setBold(true);
                rGroup.setFontSize(18);

                Map<String, List<LabSchedule>> weekMap = groupedSchedules.get(group);
                for (String week : weekMap.keySet()) {
                    XWPFParagraph pWeek = doc.createParagraph();
                    pWeek.setStyle("Heading2");
                    XWPFRun rWeek = pWeek.createRun();
                    rWeek.setText(week);
                    rWeek.setBold(true);
                    rWeek.setFontSize(14);

                    XWPFTable table = doc.createTable(TIME_SLOTS.length + 1, DAYS.length + 1);
                    table.setWidth("100%");

                    // Ensure all rows have the correct number of cells
                    for (int rowIdx = 0; rowIdx < TIME_SLOTS.length + 1; rowIdx++) {
                        ensureRowHasCells(table, rowIdx, DAYS.length + 1);
                    }

                    // Header row
                    XWPFTableRow header = table.getRow(0);
                    setCellText(header.getCell(0), "Time", true);
                    for (int i = 0; i < DAYS.length; i++) {
                        setCellText(header.getCell(i + 1), DAYS[i], true);
                    }

                    // Time slot labels
                    for (int i = 0; i < TIME_SLOTS.length; i++) {
                        setCellText(table.getRow(i + 1).getCell(0), TIME_SLOTS[i], true);
                    }

                    // Merge lunch break row (12PM-1PM) Mon-Fri (cols 1 to 5)
                    int lunchRow = 3; // row index for 12PM-1PM slot (TIME_SLOTS index 2 + 1)
                    setCellText(table.getRow(lunchRow).getCell(1), "⏸ Lunch Break", true);
                    mergeCellsHorizontally(table, lunchRow, 1, DAYS.length - 1);

                    List<LabSchedule> schedules = weekMap.get(week);
                    boolean hasYear1 = schedules.stream().anyMatch(s -> s.getBlock().contains("1"));
                    if (hasYear1) {
                        // Ensure rows 1 and 2 have all cells before merge
                        ensureRowHasCells(table, 1, DAYS.length + 1);
                        ensureRowHasCells(table, 2, DAYS.length + 1);
                        fillMerge(table, DAYS.length, 1, 2, "NSTP - National Service Training Program");
                    }

                    Set<LabSchedule> placed = new HashSet<>();
                    for (LabSchedule sched : schedules) {
                        if (placed.contains(sched)) continue;
                        String day = sched.getDay();
                        String time = sched.getTime();
                        int col = dayToCol(day);
                        if (col < 1) continue;

                        if ("8AM-12PM".equals(time)) {
                            fillMerge(table, col, 1, 2, formatText(sched));
                            placed.add(sched);
                        } else if ("1PM-5PM".equals(time)) {
                            fillMerge(table, col, 4, 5, formatText(sched));
                            placed.add(sched);
                        } else {
                            int idx = timeToSlotIndex(time);
                            if (idx >= 0) {
                                setCellText(table.getRow(idx + 1).getCell(col), formatText(sched), false);
                                placed.add(sched);
                            }
                        }
                    }

                    doc.createParagraph().setPageBreak(true);
                }
            }
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                doc.write(out);
            }
        }
    }

    private static void ensureRowHasCells(XWPFTable table, int rowIndex, int expectedCellCount) {
        XWPFTableRow row = table.getRow(rowIndex);
        while (row.getTableCells().size() < expectedCellCount) {
            row.addNewTableCell();
        }
    }

    private static int dayToCol(String day) {
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(day)) return i + 1;
        }
        return -1;
    }

    private static int timeToSlotIndex(String time) {
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].equalsIgnoreCase(time)) return i;
        }
        return -1;
    }

    private static void fillMerge(XWPFTable table, int col, int startRow, int endRow, String text) {
        XWPFTableCell first = table.getRow(startRow).getCell(col);
        setCellText(first, text, false);
        CTTcPr pr = getTcPr(first);
        pr.addNewVMerge().setVal(STMerge.RESTART);
        for (int r = startRow + 1; r <= endRow; r++) {
            XWPFTableCell cell = table.getRow(r).getCell(col);
            setCellText(cell, "", false);
            CTTcPr pr2 = getTcPr(cell);
            pr2.addNewVMerge().setVal(STMerge.CONTINUE);
        }
    }

    private static CTTcPr getTcPr(XWPFTableCell cell) {
        CTTc ct = cell.getCTTc();
        return ct.isSetTcPr() ? ct.getTcPr() : ct.addNewTcPr();
    }

    private static String formatText(LabSchedule s) {
        return s.getCode() + "\n" + s.getBlock() + "\n" + s.getProf() + "\n" + s.getSubject();
    }

    private static void setCellText(XWPFTableCell cell, String text, boolean center) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(center ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);
        XWPFRun run = p.createRun();
        run.setFontSize(10);
        for (String line : text.split("\\n")) {
            run.setText(line);
            run.addBreak();
        }
    }

    private static void mergeCellsHorizontally(XWPFTable table, int row, int fromCol, int toCol) {
        XWPFTableRow tblRow = table.getRow(row);
        for (int col = toCol; col > fromCol; col--) {
            tblRow.removeCell(col);
        }
        XWPFTableCell cell = tblRow.getCell(fromCol);
        CTTcPr tcPr = getTcPr(cell);
        CTDecimalNumber gridSpan = tcPr.isSetGridSpan() ? tcPr.getGridSpan() : tcPr.addNewGridSpan();
        gridSpan.setVal(BigInteger.valueOf(toCol - fromCol + 1));
    }
}
