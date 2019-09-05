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

//import javax.servlet.ServletContext;

import java.io.*;
import java.util.*;

/**
 * 全局配置加载器，读取本地配置
 *
 * @author bin jin on 2017/4/13.
 * @since 1.8
 */
public class EnvLoader {

//    /**
//     * 从 web.xml 的 <context-param> 中取出所有的 key value
//     */
//    public static Map<String, List<String>> getProperties(ServletContext context) {
//        Enumeration<String> keys = context.getInitParameterNames();
//        Map<String, List<String>> map = new HashMap<>();
//        while (keys.hasMoreElements()) {
//            String key = keys.nextElement();
//            map.put(
//                    key,
//                    splitString(
//                            context.getInitParameter(key)
//                    )
//            );
//        }
//        return map;
//    }

    /**
     * 获得项目所在路径，用于读取配置文件
     *
     * 因为不会变，所以可以保存下来使用
     */
    public static String getLocalPath() {
        String[] classPath = System.getProperty("java.class.path").
                split("" + File.pathSeparatorChar);

        if (classPath.length > 1) {
            for (String path : classPath) {
                // 返回第一个
                if (!path.endsWith("jar")) {
                    return path;
                }
            }
        } else {
            return new File(classPath[0]).
                    getAbsolutePath(). // 绝对路径
                    replaceFirst("[\\\\]{2}", "\\\\"). // 在 windows 下，有一种情况会导致形如 'C:\\Windows\System32'
                    replaceFirst("[\\\\/][^\\\\/]+\\.jar", ""); // 去掉最后一个路径分隔符及后面的内容。
        }

        throw new IllegalArgumentException("local path not found.");
    }

    /**
     * 从环境变量中获得配置
     */
    public static Map<String, List<String>> envProperties() {
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            map.put(
                    entry.getKey(),
                    CharUtil.splitString(entry.getValue(), ',', ';', '|')
            );
        }
        return map;
    }

    /**
     * 从 System.Properties 获得配置
     */
    public static Map<String, List<String>> getProperties() {
        return getProperties(System.getProperties());
    }

    /**
     * 从文件中读取配置
     */
    public static Map<String, List<String>> getProperties(String fileName) {
        return getProperties(loadProperties(fileName));
    }

    private static Map<String, List<String>> getProperties(Properties pp) {
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry entry : pp.entrySet()) {
            map.put(
                    String.valueOf(entry.getKey()),
                    CharUtil.splitString(String.valueOf(entry.getValue()), ',', ';', '|')
            );
        }
        return map;
    }

    /**
     * 加载 classpath 下的配置文件信息
     * 使用上下文类加载器
     *
     * @param fileName 配置文件名称
     */
    public static Properties loadProperties(String fileName) {
        if (fileName == null) throw new IllegalArgumentException("item is empty");
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {
            properties.load(new InputStreamReader(is, "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new RuntimeException(new FileNotFoundException(fileName));
        }
        return properties;
    }


}
