package org.binave.common.bytecode;

import java.util.List;
import java.util.Map;

/**
 * @author by bin jin on 2017/5/8.
 * @since 1.8
 */
public class Ab {

    private int id;

    private String name;

    private List<String> strings;

    private Map<Integer, String> stringMap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public Map<Integer, String> getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map<Integer, String> stringMap) {
        this.stringMap = stringMap;
    }
}
