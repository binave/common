//package org.binave.util.serialize;
//
//import io.protostuff.LinkedBuffer;
//import io.protostuff.ProtostuffIOUtil;
//import io.protostuff.Schema;
//import io.protostuff.runtime.RuntimeSchema;
//import org.junit.Test;
//
//import java.net.MalformedURLException;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.common.HashMap;
//import java.common.Map;
//
///**
// * @author by bin jin on 2017/2/8.
// */
//public class ProtoTest {
//
//    static class A {
//
//
//        private Map<Integer, Integer> k;
//        private int i;
//        private String j;
//
//        public A(int i, String j, Map<Integer, Integer> k) {
//            this.i = i;
//            this.j = j;
//            this.k = k;
//        }
//
//        public int getI() {
//            return i;
//        }
//
////        public void setI(int i) {
////            this.i = i;
////        }
//
//        public String getJ() {
//            return j;
//        }
//
////        public void setJ(String j) {
////            this.j = j;
////        }
//
//        public Map<Integer, Integer> getK() {
//            return k;
//        }
//    }
//
//
//    @Test
//    public void FormatTest() throws RemoteException, NotBoundException, MalformedURLException {
//
//        Map<Integer, Integer> k = new HashMap<>();
//        k.put(1, 0);
//        k.put(2, 1);
//        A a = new A(12, "ff", k);
//
//        // encode
//        Schema schema = RuntimeSchema.getSchema(A.class);
//        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
//        byte[] d = ProtostuffIOUtil.toByteArray(a, schema, buffer);
//
//        // decode
//        A b = (A) schema.newMessage();
//        ProtostuffIOUtil.mergeFrom(d, b, schema);
//        System.out.println(b.getI());
//        System.out.println(b.getJ());
//        System.out.println(b.getK());
//
//
////        Remote i = Naming.lookup("");
//
//
////        Assembly assembly = EasyMock.createMock(Assembly.class);
////
////        Bytecode bytecode = assembly.implement(Object.class);
////        System.out.println(bytecode);
//    }
//
//}
