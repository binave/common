package org.binave.common.bytecode;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.junit.Test;

import java.io.FileOutputStream;
import java.lang.reflect.Method;

/**
 * @author by bin jin on 2017/5/3.
 */
public class CreateDemo extends ClassLoader implements Opcodes {

    @Test
    public void main() throws Exception {

        IHello iHello = (IHello) MakeClass(IHello.class);

        iHello.MethodA();
        iHello.MethodB();
        iHello.Abs();
    }

    private static Object MakeClass(Class clazz) throws Exception {

        String name = clazz.getSimpleName(); // 短名

        String className = name + "$impl";

        // 要实现的接口名称，将 . 替换成 /
        String interfacesName = clazz.getName().replaceAll("\\.", "/");

        // 创建写
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        // 写类 @ public class IHello$impl implements org/binave/common/bytecode/IHello  {
        classWriter.visit(V1_7,
                ACC_PUBLIC + ACC_SUPER,
                className,
                null, // 泛型
                "java/lang/Object", // 默认继承 Object
                new String[]{interfacesName} // 接口
        );

        //空构造 @ public <init>()V
        MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );

        /*

           @ public <init>()V
           @ ALOAD 0
           @ INVOKESPECIAL java/lang/Object.<init> ()V
           @ RETURN
           @ MAXSTACK = 1
           @ MAXLOCALS = 1

        把本地变量加载到操作数栈当中
        iload int
        lload long
        fload float
        dload double
        aload 引用
        saload 数组
         */
        methodVisitor.visitVarInsn(ALOAD, 0); // this, （ALOAD 引用变量，0 偏移量，第一个变量是 this）

        /*
        invokevirtual   调用对象的成员方法
        invokeinterface 调用接口上的方法
        invokespecial   调用类内部的private方法
        invokestatic    调用指定类的静态方法
         */
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        /*
        去栈中load数据
        ireturn int
        lreturn long    弹出两位
        freturn float
        dreturn double  弹出两位
        areturn 引用
        return void     不弹出
         */
        methodVisitor.visitInsn(RETURN);

        methodVisitor.visitMaxs(1, 1);
        //设置ClassWriter.COMPUTE_MAXS 或 ClassWriter.COMPUTE_FRAMES，
        // 此处的值会被忽略，但此方法必需显示调用一下！！！
        /*
ClassWriter(0) 将不会自动进行计算。你必须自己计算帧、局部变量和操作数栈的大小。
new ClassWriter(ClassWriter.COMPUTE_MAXS) 局部变量和操作数栈的大小就会自动计算。但是，你仍然需要自己调用visitMaxs方法，尽管你可以使用任何参数：实际上这些参数会被忽略，然后重新计算。使用这个选项，你仍然需要计算帧的大小。
new ClassWriter（ClassWriter.COMPUTE_FRAMES） 所有的大小都将自动为你计算。你也不许要调用visitFrame方法，但是你仍然需要调用visitMaxs方法（参数将被忽略然后重新计算）。
        */
        methodVisitor.visitEnd();

        // 循环建立方法
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            makeMethod(classWriter, method.getName(), className);
        }

        classWriter.visitEnd();

        /*
         * 写入文件
         */
        byte[] code = classWriter.toByteArray();
        // 写出到 工作目录了
        FileOutputStream fos = new FileOutputStream(className + ".class");
        fos.write(code);
        fos.close();

        /*
         * 从文件加载类
         */
        CreateDemo loader = new CreateDemo();

        Class exampleClass = loader.defineClass(className,
                code,
                0,
                code.length);

        /*
         * 反射生成实例
         */

        Object obj = exampleClass.getConstructor(null).newInstance(null);

        return obj;
    }


    /*

  // access flags 0x1
  public MethodB()V
   L0
    LINENUMBER 8 L0
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    LDC "\u8c03\u7528\u65b9\u6cd5 [MethodB]"
    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
   L1
    LINENUMBER 9 L1
    RETURN
   L2
    LOCALVARIABLE this LIHello$impl; L0 L2 0
    MAXSTACK = 2
    MAXLOCALS = 1


  // access flags 0x1
  public MethodA()V
   L0
    LINENUMBER 8 L0
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    LDC "\u8c03\u7528\u65b9\u6cd5 [MethodA]"
    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
   L1
    LINENUMBER 9 L1
    RETURN
   L2
    LOCALVARIABLE this LIHello$impl; L0 L2 0
    MAXSTACK = 2
    MAXLOCALS = 1


  // access flags 0x1
  public Abs()V
   L0
    LINENUMBER 8 L0
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    LDC "\u8c03\u7528\u65b9\u6cd5 [Abs]"
    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
   L1
    LINENUMBER 9 L1
    RETURN
   L2
    LOCALVARIABLE this LIHello$impl; L0 L2 0
    MAXSTACK = 2
    MAXLOCALS = 1
     */
    private static void makeMethod(ClassWriter classWriter, String MethodName, String className) {

        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC,
                MethodName,
                "()V",
                null,
                null);

        methodVisitor.visitCode(); // == start ==

        Label l00 = new Label();
        methodVisitor.visitLabel(l00);
        methodVisitor.visitLineNumber(8, l00);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("调用方法 [" + MethodName + "]");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");

        Label l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLineNumber(9, l1);
        methodVisitor.visitInsn(RETURN);

        Label l2 = new Label();
        methodVisitor.visitLabel(l2);

        methodVisitor.visitLocalVariable("this", "L" + className + ";", null, l00, l2, 0);
        methodVisitor.visitMaxs(2, 1); // == end ==

        methodVisitor.visitEnd();
    }
}
