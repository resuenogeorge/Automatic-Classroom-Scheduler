/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autosched2;

/**
 *
 * @author Acer
 */
public class Subject {
    private String code;
    private String name;
    private String unitRaw;
    private String level;

    public Subject(String code, String name, String unitRaw, String level) {
        this.code = code;
        this.name = name;
        this.unitRaw = unitRaw;
        this.level = level;
    }
    
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getUnitRaw() { return unitRaw; }
    public String getLevel() { return level; }

}






