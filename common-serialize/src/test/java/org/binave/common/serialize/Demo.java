package org.binave.common.serialize;


import com.google.flatbuffers.FlatBufferBuilder;
import org.binave.common.serialize.flatbuffers.Person;
import org.binave.common.serialize.flatbuffers.PersonData;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author by bin jin on 2017/2/6.
 */
public class Demo {


    @Test
    public void test2() {

//        Object a = jdk.internal.org.objectweb.asm.ClassVisitor.class;
//        a = com.sun.xml.internal.ws.org.objectweb.asm.ClassVisitor.class;

        Person[] persons = new Person[2];

        persons[0] = new Person();
        persons[0].setId(1);
        persons[0].setName("LiLei");

        persons[1] = new Person();
        persons[1].setId(2);
        persons[1].setName("WangXi");

        Person person = new Person();
        person.setId(3);
        person.setName("FeiFei");
        person.setFriends(persons);

        // decode
        byte[] data = getData(person);

        PersonData personData = PersonData.getRootAsPerson(ByteBuffer.wrap(data));
        System.out.println(personData.id());
        System.out.println(personData.name());

        for (int i = 0; i < personData.friendsLength(); i++) {
            System.out.println(personData.friends(i).id());
            System.out.println(personData.friends(i).name());
        }

    }

    public static byte[] getData(Person person) {
        // encode

        FlatBufferBuilder fbb = new FlatBufferBuilder(1);

        int size = person.getFriends().length;

        int[] f = new int[size];
        for (int i = 0; i < size; i++) {
            int j = fbb.createString(person.getFriends()[i].getName());
            PersonData.startPerson(fbb);
            PersonData.addName(fbb, j);
            PersonData.addId(fbb, person.getFriends()[i].getId());
            f[i] = PersonData.endPerson(fbb);
        }

        int i = fbb.createVectorOfTables(f);

        int j = fbb.createString(person.getName());
        PersonData.startPerson(fbb);
        PersonData.addName(fbb, j);
        PersonData.addId(fbb, person.getId());
        PersonData.addFriends(fbb, i);
        i = PersonData.endPerson(fbb);

        PersonData.finishPersonBuffer(fbb, i);

        return fbb.sizedByteArray(fbb.dataBuffer().position(), fbb.offset());

    }
}
