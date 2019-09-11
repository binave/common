/*
 * Copyright (c) 2017 bin jin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.common.util;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * 对 class 的一些处理
 *
 * @author bin jin
 * @since 1.8
 */
public class TypeUtil {


    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        return getFieldMap(clazz, null, false);
    }

    /**
     * 获得目标类的属性（包括继承属性）
     *
     * @param clazz 扫描的类
     * @param annotation 注解
     * @param contain true ? 找带目标注解的属性 : 跳过带目标注解的属性
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz, Class<? extends Annotation> annotation, boolean contain) {

        if (clazz == null) throw new IllegalArgumentException("empty conf");

        Map<String, Field> fieldMap = new TreeMap<>();
        getFieldMap(fieldMap, clazz, annotation, contain);

        return fieldMap;
    }

    /**
     * 递归查找属性
     */
    private static void getFieldMap(Map<String, Field> fieldMap, Class<?> clazz, Class<? extends Annotation> annotation, boolean contain) {

        // 获得所有属性，但不包括继承的公有属性
        Field[] fields = clazz.getDeclaredFields();

        if (contain) {
            // 寻找包含目标注解的属性
            if (annotation == null) return;
            for (Field field : fields) {
                String name = field.getName();
                // 如果没有对应注解，则忽略
                if (name.contains("this$") || field.getAnnotation(annotation) == null) continue;
                field.setAccessible(true);
                if (!fieldMap.containsKey(name)) fieldMap.put(name, field);
            }
        } else {
            // 跳过目标注解的属性
            for (Field field : fields) {
                String name = field.getName();
                // 疑似判断内部类属性
                if (name.contains("this$") ||
                        (annotation != null && field.getAnnotation(annotation) != null)) continue;
                field.setAccessible(true);
                //  覆盖父类属性
                if (!fieldMap.containsKey(name)) fieldMap.put(name, field);
            }
        }

        Class superClass = clazz.getSuperclass();

        // 递归调用
        if (superClass != null && superClass != Object.class)
            getFieldMap(fieldMap, superClass, annotation, contain);
    }

    /**
     * 获取方法内全部参数名称
     * todo 需要考虑 classloader 的问题
     */
    @Deprecated
    public static String[] getFieldsInMethod(String className, String methodName) {
        ClassReader reader;
        try {
            // 获得上下文的 ClassLoader
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream in = loader.getResourceAsStream(convertResourceName(className))) {
                reader = new ClassReader(in);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        List<String> fieldNameList = new ArrayList<>();
        reader.accept(new ClassVisitor(Opcodes.ASM4) {
            // 框架在遍历到方法时，会调用此方法
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc,
                                             final String signature, final String[] exceptions) {
                // 拿到当前的方法 Visitor 对象
                MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
                // 判断是否是想要的方法名
                if (!name.equals(methodName)) return visitor;
                // 需要返回方法，同时访问方法相关属性
                return new MethodVisitor(Opcodes.ASM4, visitor) {
                    // 访问方法中的变量
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        // 无差别顺序存入局部变量名称
                        fieldNameList.add(name);
                        super.visitFieldInsn(opcode, owner, name, desc);
                    }
                };
            }
        }, 0);
        if (fieldNameList.isEmpty()) throw new IllegalArgumentException();

        // 去掉两端的 [] 字符，应对未来的变化
        return fieldNameList.toArray(new String[fieldNameList.size()]);
    }

    /**
     * 将 class 名称替换成 / 分割格式
     * 用来代替正则替换
     */
    private static String convertResourceName(String fullClassName) {
        StringBuilder sb = new StringBuilder(fullClassName);
        int index;
        if ((index = sb.indexOf(".")) == -1)
            throw new IllegalArgumentException("class name error: " + fullClassName);
        while (index != -1) {
            sb.replace(index, index + 1, "/");
            index = sb.indexOf(".", index + 1);
        }
        return sb.append(".class").toString();
    }

    /**
     * 获得类型的所有实现接口，包括继承类型
     */
    public static Class<?>[] getInterfaces(Class<?> type) {
        List<Class> classes = new ArrayList<>();
        getInterfaces(type, classes);
        return classes.toArray(new Class[classes.size()]);
    }

    private static void getInterfaces(Class<?> type, List<Class> classes) {
        Collections.addAll(classes, type.getInterfaces());
        Class superClass = type.getSuperclass();
        if (superClass.equals(Object.class)) return; // 中断递归
        getInterfaces(superClass, classes);
    }

    /**
     * 获得基本类型的包装类
     */
    public static Class getPrimitiveType(Class type) {
        if (type == null || !type.isPrimitive()) return type;

        if (type == Integer.TYPE)
            return Integer.class;
        else if (type == Boolean.TYPE)
            return Boolean.class;
        else if (type == Byte.TYPE)
            return Byte.class;
        else if (type == Long.TYPE)
            return Long.class;
        else if (type == Character.TYPE)
            return Character.class;
        else if (type == Double.TYPE)
            return Double.class;
        else if (type == Short.TYPE)
            return Byte.class;
        else if (type == Float.TYPE)
            return Float.class;
        throw new RuntimeException();
    }

    /**
     * 获得目标属性的泛型
     */
    public static Type[] getGenericTypes(Field field) {
        return getGenericTypes((ParameterizedType) field.getGenericType());
    }

    /**
     * 获得目标的泛型
     */
    public static Type[] getGenericTypes(Type type) {
        return getGenericTypes((ParameterizedType) type);
    }

    private static Type[] getGenericTypes(ParameterizedType pt) {
        return pt.getActualTypeArguments();
    }

    /**
     * 通过方法名前缀获得匹配的 public 方法集合
     *
     * @param prefix 方法名前缀如果为空，则默认所有方法
     */
    public static Map<String, List<Method>> prefixPublicMethods(Class clazz, String... prefix) {
        Map<String, List<Method>> methodMap = new HashMap<>();
        if (prefix == null || prefix.length == 0) {

            // 认为获得全部 public 方法
            for (Method me : clazz.getMethods()) {
                String name = me.getName();
                if (!methodMap.containsKey(name)) {
                    methodMap.put(name, new ArrayList<>());
                }
                methodMap.get(name).add(me);
            }
        } else {
            // 仅获得匹配前缀的 public 方法
            for (Method me : clazz.getMethods()) {
                for (String p : prefix) {
                    String name = me.getName();
                    if (name.startsWith(p)) {
                        if (!methodMap.containsKey(name)) {
                            methodMap.put(name, new ArrayList<>());
                        }
                        methodMap.get(name).add(me);
                    }
                }
            }
        }
        return methodMap;
    }

    /**
     * 通过字节编码，获得类名称
     *
     */
    public static String getClassName(byte[] clazz) {
        ClassReader cr = new ClassReader(clazz);
        cr.accept(cv, 0);
        return cv.getName();
    }

    private final static ClassNameVisitor cv = new ClassNameVisitor();

    private static class ClassNameVisitor
            extends ClassVisitor implements Opcodes {
        private String name;

        String getName() {
            return name;
        }

        private ClassNameVisitor() {
            super(ASM4);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.name = name;
        }
    }

    /**
     * 返回一个目录下的所有资源
     */
    public static List<String> listSources(String absolutePath) {
        ClassLoader loader = ClassLoader.getSystemClassLoader();

        Enumeration<URL> dirs;
        try {
            dirs = loader.getResources(absolutePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        // 当前路径
        URL pwdUrl = loader.getResource(".");

        List<String> list = new ArrayList<>();
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol(); // 得到协议的名称

            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                allInDir(list, 65536, url.getFile(), null);

            } else if ("jar".equals(protocol)) {
                // jar 包内容
                allInJar(list, url, absolutePath);
            }
        }

        // 可以获得当前路径
        if (pwdUrl != null) {
            List<String> newList = new ArrayList<>();
            for (String resource : list) {
                newList.add(
                        resource.startsWith(pwdUrl.getPath())
                                ? resource.substring(pwdUrl.getPath().length())
                                : resource
                );
            }
            list = newList;
        }
        return list;
    }


    /**
     * 获得 jar 文件
     */
    private static void allInJar(Collection<String> collection, URL url, String prefix) {
        JarFile jar;
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
        } catch (IOException e) {
            throw new RuntimeException(e); // 在扫描用户定义视图时从jar包获取文件出错
        }

        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) { // 同样的进行循环迭代。获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            String name = entries.nextElement().getName();
            if (name.charAt(0) == '/') name = name.substring(1); // 如果是以'/'开头的，获取后面的字符串
            if (name.startsWith(prefix) && '/' != name.charAt(name.length() - 1)) {
                collection.add(name); // 显示文件，如果以 '/' 结尾，是一个包，跳过
            }
        }
    }

    /**
     * 递归获得所有文件
     *
     * @param collection  添加用的集合
     * @param maxDepth    最大递归深度
     * @param path        递归目标路径
     * @param filterRegex 名称过滤正则表达式
     */
    public static void allInDir(Collection<String> collection, int maxDepth, String path, String filterRegex) {
        if (maxDepth <= 0) {
            return; // 深度
        }

        File fileOrDir = new File(path);
        if (!fileOrDir.exists()) {
            return; // 文件不存在
        }

        if (fileOrDir.isDirectory()) { // 文件夹
            File[] fileList = fileOrDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) {
                        allInDir(collection, maxDepth - 1, path + File.separator + file.getName(), filterRegex);
                    } else if (filterRegex == null || Pattern.matches(filterRegex, file.getName())) {
                        collection.add(path + File.separator + file.getName());
                    }
                }
            }
        } else if (filterRegex == null || Pattern.matches(filterRegex, fileOrDir.getName())) {
            collection.add(path + File.separator + fileOrDir.getName());
        }
    }

}
