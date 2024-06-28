package org.binave.common.bytecode;


import jdk.internal.org.objectweb.asm.*;
import org.binave.common.util.TypeUtil;
import org.binave.common.serialize.Foo;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author by nidnil@outlook.com on 2016/12/27.
 */
public class VisitorDemo extends ClassVisitor implements Opcodes {

    public VisitorDemo() {
        super(ASM4);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("[visit] version: " + version + ", access:" + access + ", name: " + name + ", signature: " + signature + ", superName: " + superName + ", interfaces: " + Arrays.toString(interfaces));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        System.out.println("[visitAnnotation] desc: " + desc + ", visible" + visible);
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        System.out.println("[visitField] access: " + access + ", name: " + name + ", desc: " + desc + ", signature: " + signature + ", value: " + value);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("[visitMethod] access: " + access + ", name: " + name + ", desc: " + desc + ", signature: " + signature + ", exceptions: " + Arrays.toString(exceptions));
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        System.out.println("==[visitEnd]==");
        super.visitEnd();
    }

    @Test
    public void test1() throws IOException {
        try {
            ClassReader cr = new ClassReader(Foo.class.getName());
            VisitorDemo cp = new VisitorDemo();
            cr.accept(cp, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(
                TypeUtil.getFieldMap(Foo.class).toString().replaceAll(",", ",\n")
        );

        System.out.println(
                TypeUtil.prefixPublicMethods(Foo.class, "get", "set").
                        toString().replaceAll("],","],\n")
        );

    }
}
