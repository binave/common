package org.binave.common.serialize.flatbuffers;

/**
 * @author by bin jin on 2017/2/6.
 */
public class Person {

    private long id;
    private String name;
    private Person[] friends;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person[] getFriends() {
        return friends;
    }

    public void setFriends(Person[] friends) {
        this.friends = friends;
    }
}
