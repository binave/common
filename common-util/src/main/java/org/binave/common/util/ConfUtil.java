package org.binave.common.util;

import org.binave.common.api.Version;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置加载工具
 *
 * @author by bin jin on 2020/9/2 23:10.
 */
public class ConfUtil {

    public static <ver extends Version> ver getConf(String ymlFilePreName, Class<ver> type) {
        return getConf(loadConf(ymlFilePreName), type);
    }

    public static <ver extends Version> ver getConf(String ymlFilePreName, Class<ver> type, String name) {
        return getConf(loadConf(ymlFilePreName), type, name);
    }

    public static <ver extends Version> ver getConf(Map<Class<ver>, Map<String, ver>> config, Class<ver> type) {
        Map<String, ver> verMap = config.get(type);
        if (verMap == null || verMap.isEmpty()) return null;
        return verMap.values().iterator().next();
    }

    public static <ver extends Version> ver getConf(Map<Class<ver>, Map<String, ver>> config, Class<ver> type, String name) {
        Map<String, ver> verMap = config.get(type);
        if (verMap == null || verMap.isEmpty()) return null;
        return verMap.get(name);
    }

    public static <ver extends Version> Map<Class<ver>, Map<String, ver>> loadConf(String ymlFilePreName) {
        return loadConf(getYamlConfigs(ymlFilePreName));
    }

    // 检测是否是 map 的序列化字符
    private static final String MAP_REGEX = "\\W*\\s+!!java\\.util\\.Map\\s+[\\s\\S]*";

    public static <ver extends Version> Map<Class<ver>, Map<String, ver>> loadConf(String... confContexts) {
        Map<String, String> aliasMap = null;
        Map<Class<ver>, Map<String, ver>> configs = new HashMap<>();

        for (String str : confContexts) {
            if (Pattern.matches(MAP_REGEX, str)) {
                aliasMap = new Yaml().load(str);
                break;
            }
        }

        if (aliasMap == null) aliasMap = new HashMap<>(0);

        for (String str : confContexts) {
            if (Pattern.matches(MAP_REGEX, str)) continue;
            Object o = yamlLoad(str, aliasMap);
            if (o instanceof Version) {
                ver v = (ver) o;
                configs.computeIfAbsent((Class<ver>) v.getClass(), map -> new HashMap<>()).put(v.getName(), v);
            } else System.err.printf(
                    "Instanceof 'Version' failed: %s",
                    o == null ? "NULL" : o.getClass()
            );
        }

        return configs;
    }


    private static final String CLASS_NAME_FROM_YAML_REGEX = "(?<=!!)[0-9A-Za-z_.]+";

    /**
     * @param str       用于反序列化的对象
     * @param aliasMap  别名表
     */
    private static <T> T yamlLoad(String str, Map<String, String> aliasMap) {
        Matcher matcher = Pattern.compile(CLASS_NAME_FROM_YAML_REGEX).matcher(str);
        boolean find = false;
        while (matcher.find()) { // 得到字符中的类名称，支持多个替换
            String key = matcher.group();
            String aliasKey = aliasMap.get(key);
            if (aliasKey != null && !Objects.equals(key, aliasKey)) {
                str = str.replaceAll(String.format("(?<=!!)%s", key), aliasKey);
            }
            find = true;
        }

        if (!find) {
            // 没找到，提示且忽略
            System.err.printf("ERROR format error %s\n", str);
            return null;
        }

        try {
            return new Yaml().load(str);
        } catch (RuntimeException e) {
            e.printStackTrace(); // 格式不正确
            System.err.printf("yaml deserialize error %s\n", str);
            return null;
        }
    }


    private static final String EXEC_REGEX = "[\\s\\S]*!![A-Za-z0-9_]+[\\s\\S]*[A-Za-z0-9_]+:[\\s\\S]*";
    private static final String SPLIT_REGEX = "[\n\r]-{3,}[\n\r]";

    /**
     * 返回可用于 foreach 的迭代器
     *
     * @param ymlFilePreName   yml 配置文件名前缀
     */
    public static String[] getYamlConfigs(String ymlFilePreName) {
        try {

            List<String> yamlFilePathList = new LinkedList<>();

            allInDir(
                    yamlFilePathList,
                    5,
                    EnvLoader.getLocalPath(),
                    String.format("%s.*\\.yml", ymlFilePreName)
            );

            if (yamlFilePathList.isEmpty()) {
                // 没找到要求的文件
                throw new RuntimeException(String.format(
                        "preName: %s not found", ymlFilePreName
                ));
            }

            List<String> yamlText = new LinkedList<>();

            // 多个文件
            for (String yamlPath : yamlFilePathList) {
                File file = new File(yamlPath);
                byte[] bytes = new byte[(int) file.length()]; // 设置和文件大小一致的字节数组
                try (FileInputStream in = new FileInputStream(file)) {
                    in.read(bytes); // 读取内容到字节数组，会自动关闭流
                }

                String context = new String(bytes, StandardCharsets.UTF_8).
                        replaceAll("#.*", "#"); // 干掉注释，'.*'遇到换行会停止

                int LFOffset = 0;
                for (String conf : envReplaceFilter(context).split(SPLIT_REGEX)) { // 替换变量之后，在后面追加文件名和行数范围
                    int LFCount = CharUtil.getStrCount(conf, "\n") + 2;

                    if (Pattern.matches(EXEC_REGEX, conf)) { // 去掉注释后，是否剩余可用
                        yamlText.add(String.format(
                                "%s\n#%s:%s-%s", conf, file.getName(), LFOffset, LFOffset + LFCount
                        ));
                    }
                    LFOffset += LFCount;
                }
            }

            // 根据 yaml 的 分隔符进行切分
            return yamlText.toArray(new String[0]);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据文件名过滤条件，递归获得所有文件
     *
     * @param collection  添加用的集合
     * @param maxDepth    最大递归深度
     * @param path        递归目标路径
     * @param filterRegex 名称过滤正则表达式
     */
    static void allInDir(Collection<String> collection, int maxDepth, String path, String filterRegex) {
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

    private static final String ENV_REGEX = "(?<=\\$\\{)[A-Za-z_][A-Za-z0-9_]+(?=})";

    /**
     * 将字符中的变量替换为对应环境变量的值
     * 也可以在启动参数中增加配置 ' -D{key}={value}'
     *
     * 为了兼容 linux 环境变量的命名方式，环境变量 {key} 仅允许使用字母数字下划线，并且首字符不可以是数字。
     * 此处不支持单个字符的环境变量名。
     */
    public static String envReplaceFilter(String src) {
        Matcher matcher = Pattern.compile(ENV_REGEX).matcher(src);
        while (matcher.find()) {
            String key = matcher.group();
            String value = System.getProperty(key, System.getenv(key));
            if (value == null) {
                throw new IllegalArgumentException(String.format(
                        "${%s} not found in environment variable.", key
                ));
            }
            src = src.replaceAll(
                    String.format("\\$\\{%s}", key),
                    value.replaceAll("\\\\", "\\\\\\\\")
            );
        }
        return src;
    }

}
