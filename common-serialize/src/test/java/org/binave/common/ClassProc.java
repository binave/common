package org.binave.common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * 2016/12/29.
 */
public class ClassProc<Type> {

    //        sun.misc.Launcher.getLauncher();

    private Class<? extends Annotation>[] annotations;

    private Set<Class<? extends Type>> classes = new LinkedHashSet<Class<? extends Type>>();

    /**
     * 从包 package 中获取所有的Class
     */
    public Set<Class<? extends Type>> getClassSet(String packageName, Class<? extends Annotation>... annotations)
            throws IOException {

        if (packageName == null || packageName.length() == 0)
            return null;

        this.annotations = annotations;

        // 获取包的名字 并进行替换
        String packagePathName = packageName.replace('.', '/');

        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread
                    .currentThread()
                    .getContextClassLoader()
                    .getResources(packagePathName);

            // 循环迭代下去
            while (dirs.hasMoreElements()) {

                // 获取下一个元素
                URL url = dirs.nextElement();

                // 得到协议的名称
                String protocol = url.getProtocol();

                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {

                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(
                            packageName,
                            URLDecoder.decode(url.getFile(), "UTF-8") // 获取包的物理路径
                    );

                } else if ("jar".equals(protocol)) findAndAddClassesInPackageByJar(url, packagePathName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    private void addClass(Class<?> clazz) {

        // 如果注释为空，或者类中存在目标注释，存入 Set
        if (annotations == null || isAnnotationsPresent(clazz))
            classes.add((Class<? extends Type>) clazz);
    }

    private boolean isAnnotationsPresent(Class<?> clazz) {
        for (Class<? extends Annotation> ann : annotations)
            if (clazz.isAnnotationPresent(ann))
                return true;
        return false;
    }

    /**
     * 处理 jar 文件
     * todo 这里没有处理嵌套 jar
     */
    private void findAndAddClassesInPackageByJar(URL url, String packageDirName) {

        // 如果是jar包文件，定义一个JarFile

        JarFile jar;
        try {

            // 获取jar
            jar = ((JarURLConnection) url.openConnection())
                    .getJarFile();

            // 从此jar包 得到一个枚举类
            Enumeration<JarEntry> entries = jar.entries();

            // 同样的进行循环迭代
            while (entries.hasMoreElements()) {

                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                JarEntry entry = entries.nextElement();

                String name = entry.getName();

                // 如果是以'/'开头的，获取后面的字符串
                if (name.charAt(0) == '/') name = name.substring(1);

                // 如果前半部分和定义的包名相同
                if (name.startsWith(packageDirName)) {

                    int idx = name.lastIndexOf('/');

                    // 如果可以迭代下去 并且是一个包
                    if (idx != -1) {

                        // 如果以'/'结尾，是一个包，获取包名 把'/'替换成'.'
                        String packageName = name.substring(0, idx)
                                .replace('/', '.');

                        // 如果是一个.class文件 而且不是目录
                        if (name.endsWith(".class")
                                && !entry.isDirectory()) {

                            // 去掉后面的".class" 获取真正的类名
                            String className = name.substring(
                                    packageName.length() + 1,
                                    name.length() - 6);

                            try {
                                addClass(
                                        Class.forName(packageName + '.' + className)
                                );
                            } catch (ClassNotFoundException e) {

                                // 添加用户自定义视图类错误，找不到此类的.class文件
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 在扫描用户定义视图时从jar包获取文件出错
            e.printStackTrace();
        }
    }


    /**
     * 以文件的形式来获取包下的所有Class
     */
    private void findAndAddClassesInPackageByFile(String packageName, String packagePath) {

        File dir = new File(packagePath);

        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) return;

        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dir.listFiles(new FileFilter() {

            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith(".class");
            }
        });

        if (dirFiles != null)
            // 循环所有文件
            for (File file : dirFiles) {

                // 如果是目录 则继续扫描
                if (file.isDirectory()) {

                    findAndAddClassesInPackageByFile(
                            packageName + "." + file.getName(),
                            file.getAbsolutePath()
                    );

                } else {

                    // 如果是java类文件 去掉后面的.class 只留下类名
                    String className = file.getName().substring(0, file.getName().length() - 6);

                    try {
                        // 添加到集合中去
                        // addClass(Class.forName(packageName + '.' + className)) 会触发static方法
                        addClass(
                                Thread.currentThread()
                                        .getContextClassLoader()
                                        .loadClass(packageName + '.' + className)
                        );
                    } catch (ClassNotFoundException e) {

                        // 添加用户自定义视图类错误，找不到此类的.class文件
                        e.printStackTrace();
                    }
                }
            }
    }


    /**
     * 根据类全名获得类型，支持数组类型
     * get class type, support array type
     */
    private static Class getClassTypeByName(String classPath) {
        if (classPath == null || classPath.length() < 2)
            return null;

        // 获取数组标志
        int index = classPath.indexOf("[L");
        int beginIndex = index > -1 ? index + 2 : 0;

        // 去掉类名称后部带有的 ";" 符号
        int endIndex = index > -1 ? classPath.indexOf(";") : classPath.length();

        // 为了防止恶作剧……
        if (endIndex <= beginIndex)
            return null;

        Class type = null;
        try {
            type = Class.forName(
                    classPath.substring(beginIndex, endIndex)
            );
            if (type != null && index > -1) {
                int[] dim = new int[index + 1];
                // 获得类的多维数组类型
                type = Array.newInstance(type, dim).getClass();
            }
        } catch (Exception ignored) {
        }

        return type;
    }

    /**
     * 获得 classloader
     */
    public static ClassLoader getClassLoader(final Class<?> clazz) {
        if (System.getSecurityManager() == null)
            return clazz.getClassLoader();
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return clazz.getClassLoader();
            }
        });
    }


    /**
     * 获取类对应的数据库字段
     * 会忽略集合属性
     */
    public static Map<String, Field> getFields(Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException("empty args");

        Set<Field> fieldSet = new HashSet<>();
        /*
         * 获得私有属性
         */
        Collections.addAll(fieldSet, clazz.getDeclaredFields());
        /*
         * 获得继承属性
         */
        Collections.addAll(fieldSet, clazz.getFields());

        Map<String, Field> fieldMap = new HashMap<>();

        for (Field field : fieldSet) {
            Class type = field.getType();
            String name = field.getName();
            /*
             * 跳过集合类属性
             */
// todo           if (Collection.class.isAssignableFrom(type)
//                    || Map.class.isAssignableFrom(type)
//                    || name.contains("this$")
//                    || field.getAnnotation(Skip.class) != null) continue;
            /*
             * 禁用权限检查
             */
            field.setAccessible(true);
            fieldMap.put(name, field);

        }
        return fieldMap;
    }


    /**
     * 测试 class 是否是实体类
     */
    public static boolean isEntry(Class clazz) {

        return clazz == null || clazz.isInterface() // 是不是接口
                || Modifier.isAbstract(clazz.getModifiers()); // 是不是抽象类
    }

    public static boolean canSerialization(Field field) {
        int mod = field.getModifiers();
        return !Modifier.isStatic(mod) &&
                !Modifier.isTransient(mod) && !isTag(field);
    }

    private static boolean isTag(Field field) {
        for (Annotation a : field.getAnnotations()) {
            if ("skip".equalsIgnoreCase(a.getClass().getSimpleName()))
                return true;
        }
        return false;
    }

    static void fill(Map<String, Field> fieldMap, Class<?> typeClass) {
        if (Object.class != typeClass.getSuperclass())
            fill(fieldMap, typeClass.getSuperclass());

        for (java.lang.reflect.Field f : typeClass.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod))
                fieldMap.put(f.getName(), f);
        }
    }

}
