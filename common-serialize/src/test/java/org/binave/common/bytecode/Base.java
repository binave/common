package org.binave.common.bytecode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描时，将只检测 get set 方法对应的属性，没有的，则忽略
 * 对于枚举，则记录枚举状态
 *
 * @author by bin jin on 2017/5/4.
 * @since 1.8
 */
public class Base implements iBase {

    private int i;

    private long l;

    private String s;

    private Object o;

    private List<Ab> abList;

    private Map<Long, Object> oMap;

    @Override
    public int getI() {
//        TraceClassVisitor
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setIL(int i, long l, String s) {
        this.i = i;
        this.s = s;
    }

    public long getL() {
        return l;
    }

    public void setL(long l) {
        this.l = l;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public Object getO() {
        return o;
    }

    public void setO(Object o) {
        this.o = o;
    }

    public List<Ab> getAbList() {
        return abList;
    }

    public void setAbList(List<Ab> abList) {
        this.abList = abList;
    }

    public Map<Long, Object> getoMap() {
        return oMap;
    }

    public void setoMap(Map<Long, Object> oMap) {
        this.oMap = oMap;
    }

    /**
     * 如果是引用属性
     * 判断是否为 基本类型包装类、数组、枚举、集合、字典、自己（引用相同则暂时不支持）
     */
    public Base copy(Base base) {
        Base newBase = new Base();
        newBase.setI(base.getI());

        // 判断泛型类型是否是通用类型，是则 addAll，不是则进行转换迭代
        List<Ab> abList = getAbList();
        if (abList != null) {
            List<Ab> newAbList = new ArrayList<>();
            for (Ab ab : abList) {
                newAbList.add(copy(ab));
            }
            newBase.setAbList(newAbList);
        }

        newBase.setL(base.getL());
        newBase.setO(base.getO());

        return newBase;
    }

    public Ab copy(Ab ab) {
        Ab newAb = new Ab();
        newAb.setId(ab.getId());
        newAb.setName(ab.getName());

        List<String> strings = ab.getStrings();
        if (strings != null) {
            List<String> newStrings = new ArrayList<>();
            for (String str : strings) {
                newStrings.add(str);
            }
            newAb.setStrings(newStrings);
        }

        Map<Integer, String> stringMap = ab.getStringMap();
        if (stringMap != null) {
            Map<Integer, String> newStringMap = new HashMap<>();
            for (Map.Entry<Integer, String> entry : stringMap.entrySet()) {
                newStringMap.put(entry.getKey(), entry.getValue());
            }
            newAb.setStringMap(newStringMap);
        }

        return newAb;
    }

    @Test
    public void test2() {
        System.out.println(Integer.TYPE.isPrimitive());
    }
}
