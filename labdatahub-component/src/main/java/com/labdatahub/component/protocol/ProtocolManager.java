//由AI修改
package com.labdatahub.component.protocol;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.common.utils.spring.SpringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Description: 协议管理
 * @Author: labdatahub
 * @CreateTime: 2025-09-19
 */
public class ProtocolManager {
    // 由AI修改：以下五个容器一律用 ConcurrentHashMap，不能用 HashMap。
    // 写值发生在「启动/停止网络组件」「上传协议包」的 HTTP 请求线程里（addProtocol / removeProtocol），
    // 与之并发的读取发生在另外两条线程上：① 每个协议的消费线程，每条消息都要查 DECODE_METHOD 做解码
    // （13 个 XxxMessageConsumeService）；② 写值路径的 DeviceDownUtils / ProtocolEncodeInvoker 查
    // ENCODE_METHOD。HashMap 在扩容（rehash）期间并发 get 可能返回 null —— key 明明在却读不到，
    // 表现为那段窗口内该设备偶发解码失败/丢数，且完全不可复现。并发启动多个网络组件正是最容易撞上
    // 这个窗口的场景（同时也可能读到只复制了一半的桶数组）。
    // 注意：换容器只解决「单个操作原子」，跨多次操作的一致性（如先 containsKey 再 put）仍需外部串行，
    // 见 LabdatahubComponentServiceImpl#initProtocol。

    /**
     * key:协议id value:解码方法
     */
    public static Map<String,Method> DECODE_METHOD = new ConcurrentHashMap<>();
    /**
     * key:协议id value:指令方法
     */
    public static Map<String,Method> ENCODE_METHOD = new ConcurrentHashMap<>();
    /**
     * key:协议id value:协议包实例
     */
    public static Map<String,Object> CLASS_INSTANCE = new ConcurrentHashMap<>();
    /**
     * key:组件id value:协议id列表
     */
    public static Map<String,String> PROTOCOL_MAP = new ConcurrentHashMap<>();
    /**
     * 类加载器
     */
    public static Map<String,URLClassLoader> CLASSLOADER_MAP = new ConcurrentHashMap<>();
    public static String redisPath = "com.labdatahub.protocol.utils.RedisContextHolder";
    public static boolean addProtocol(String id,String protocolType,String path,String mainClass) throws Exception {
        File file = new File(path);
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};
        // 由AI修改：URLClassLoader 的 parent 改用 ProtocolManager 自身的加载器，而非线程上下文加载器。
        // devtools（RestartClassLoader）与 Tomcat 线程池下，线程上下文加载器可能与 ProtocolManager 的加载器不同，
        // 导致 fastjson2.JSONObject 被两个加载器各加载一份，Class.getMethod 按 class identity 比较失败，
        // 上传协议时抛 NoSuchMethodException: ...decode(com.alibaba.fastjson2.JSONObject)。改为自身加载器后，
        // 协议 jar 内的依赖解析（fastjson2 等）与后端一致，getMethod/invoke 全链路类一致。
        URLClassLoader classLoader = new URLClassLoader(urls, ProtocolManager.class.getClassLoader());
        Class<?> interfaceClass = classLoader.loadClass(mainClass);
        List<Class<?>> implementations = findImplementations(file, interfaceClass, classLoader);
        RedisTemplate<String, Object> redisTemplate = SpringUtils.getBean("redisTemplate");
        try {
            Class<?> redisContextClass = classLoader.loadClass(redisPath);
            Method setRedisTemplateMethod = redisContextClass.getMethod(
                    "setRedisTemplate",
                    RedisTemplate.class  // 方法的参数类型
            );
            setRedisTemplateMethod.invoke(redisContextClass, redisTemplate);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (!implementations.isEmpty()) {
            // 使用第一个找到的实现类
            Class<?> implementationClass = implementations.get(0);
            Object instance = implementationClass.getDeclaredConstructor().newInstance();
            Method decodeMethod = null;
            Method encodeMethod = null;
            try {
                decodeMethod = implementationClass.getMethod("decode", JSONObject.class);
            } catch (NoSuchMethodException e) {
                // 由AI修改：改为自身加载器后协议 jar 与后端共用同一份 fastjson2，正常应直接匹配；
                // 仍失败则抛明确异常，便于排查协议包依赖版本不兼容
                throw new RuntimeException("反射获取 decode 方法失败，implementationClass=" + implementationClass
                        + "，请检查协议包内依赖（fastjson2 等）是否与后端版本冲突", e);
            }
            encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class,Map.class);
//            // 反射调用方法
//            switch (protocolType){
//                case "MQTT_BROKER":
//                    decodeMethod = implementationClass.getMethod("decode",String.class,String.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                case "MQTT_CLIENT":
//                    decodeMethod = implementationClass.getMethod("decode",String.class,String.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                case "TCP_SERVER":
//                    decodeMethod = implementationClass.getMethod("decode",String.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                case "UDP_SERVER":
//                    decodeMethod = implementationClass.getMethod("decode",String.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                case "COAP_SERVER":
//                    decodeMethod = implementationClass.getMethod("decode",String.class,String.class,String.class,List.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                case "HTTP_SERVER":
//                    decodeMethod = implementationClass.getMethod("decode",String.class,String.class,String.class,Map.class,Map.class,String.class,Map.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class,Integer.class,String.class);
//                    break;
//                case "WEBSOCKET_SERVER":
//                    decodeMethod = implementationClass.getMethod("decode",Integer.class,String.class,String.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                case "MODBUS_TCP":
//                    decodeMethod = implementationClass.getMethod("decode",String.class,Integer.class,String.class,String.class,JSONArray.class);
//                    encodeMethod = implementationClass.getMethod("encode",String.class,String.class,Map.class,String.class,String.class);
//                    break;
//                default:
//            }
            CLASS_INSTANCE.put(id,instance);
            DECODE_METHOD.put(id,decodeMethod);
            ENCODE_METHOD.put(id,encodeMethod);
            CLASSLOADER_MAP.put(id, classLoader); // 存储 ClassLoader 引用
        } else {
            System.out.println("未找到实现类");
        }
        return true;
    }

    // 卸载方法
    public static boolean removeProtocol(String protocolId) {
        URLClassLoader classLoader = CLASSLOADER_MAP.remove(protocolId);
        if (classLoader != null) {
            try {
                classLoader.close(); // 关闭 ClassLoader（Java 7+）
                // 同时清理其他相关资源
                CLASS_INSTANCE.remove(protocolId);
                DECODE_METHOD.remove(protocolId);
                ENCODE_METHOD.remove(protocolId);
                return true;
            } catch (IOException e) {
                System.err.println("关闭 ClassLoader 失败: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // 卸载通过组件id
    public static boolean removeProtocolByComponentId(String componentId) {
        String protocolId = PROTOCOL_MAP.remove(componentId);
        if(protocolId==null){
            return true;
        }
        URLClassLoader classLoader = CLASSLOADER_MAP.remove(protocolId);
        if (classLoader != null) {
            try {
                classLoader.close(); // 关闭 ClassLoader（Java 7+）
                // 同时清理其他相关资源
                CLASS_INSTANCE.remove(protocolId);
                DECODE_METHOD.remove(protocolId);
            } catch (IOException e) {
                System.err.println("关闭 ClassLoader 失败: " + e.getMessage());
            }
        }
        return true;
    }

    // 查找JAR包中所有实现指定接口的类
    private static List<Class<?>> findImplementations(File jarFile, Class<?> interfaceClass, URLClassLoader classLoader)
            throws Exception {
        List<Class<?>> implementations = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (!clazz.isInterface() &&
                                interfaceClass.isAssignableFrom(clazz) &&
                                !clazz.equals(interfaceClass)) {
                            implementations.add(clazz);
                        }
                    } catch (NoClassDefFoundError | Exception e) {
                        // 忽略无法加载的类
                    }
                }
            }
        }

        return implementations;
    }
}
