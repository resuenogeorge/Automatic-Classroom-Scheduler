package autosched2;

public class LabSchedule {
    String lab, week, day, time;
    String code, name, prof, block;

    public LabSchedule(String lab, String week, String day, String time, String code, String name, String prof, String block) {
        this.lab = lab;
        this.week = week;
        this.day = day;
        this.time = time;
        this.code = code;
        this.name = name;
        this.prof = prof;
        this.block = block;
    }

    @Override
    public String toString() {
        return String.format(
            "%s | Week %s | %s %s | %s - %s | %s | %s",
            lab, week, day, time, code, name, prof, block
        );
    }
    
    public String getLab() {
        return lab;
    }

    public String getWeek() {
        return week;
    }

    public String getBlock() {
        return block;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public String getCode() {
        return code;
    }
    public String getProf() {
        return prof;
    }

    public String getSubject() {
        return name;
    }
}
