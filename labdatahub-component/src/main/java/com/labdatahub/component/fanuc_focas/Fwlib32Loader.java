//由AI修改
package com.labdatahub.component.fanuc_focas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.jna.Native;

/**
 * FOCAS2 库（fwlib32）加载器：自动搜索库文件并按需加载，懒加载+幂等
 * 搜索优先级：组件配置 libPath → java.library.path → jar 同目录 lib/ → 常见系统路径
 * 找不到库时抛中文明确报错，避免 JNA 抛晦涩的 UnsatisfiedLinkError
 */
public class Fwlib32Loader {
    // 库实例缓存（线程安全，只加载一次）
    private static final AtomicReference<Fwlib32> INSTANCE = new AtomicReference<>();

    private Fwlib32Loader() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 获取 FOCAS2 库实例（默认路径搜索）
     * @return Fwlib32 接口代理实例
     * @throws RuntimeException 未找到库文件或加载失败
     */
    public static Fwlib32 get() {
        return get(null);
    }

    /**
     * 获取 FOCAS2 库实例（可指定库路径）
     * @param libPath 库文件路径或所在目录（可为null，按默认路径搜索）
     * @return Fwlib32 接口代理实例
     * @throws RuntimeException 未找到库文件或加载失败
     */
    public static Fwlib32 get(String libPath) {
        if (INSTANCE.get() == null) {
            synchronized (Fwlib32Loader.class) {
                if (INSTANCE.get() == null) {
                    INSTANCE.set(load(libPath));
                }
            }
        }
        return INSTANCE.get();
    }

    /**
     * 按优先级搜索并加载 fwlib32 库
     */
    private static Fwlib32 load(String libPath) {
        // 1. 组装候选绝对路径列表
        List<File> candidates = buildCandidates(libPath);
        // 2. 逐个尝试加载
        for (File file : candidates) {
            if (file.exists() && file.isFile()) {
                try {
                    Fwlib32 lib = Native.load(file.getAbsolutePath(), Fwlib32.class);
                    System.out.printf("[FOCAS2] 加载 fwlib32 库成功：%s%n", file.getAbsolutePath());
                    return lib;
                } catch (UnsatisfiedLinkError e) {
                    // 文件存在但加载失败（架构不匹配等），记录后尝试下一个
                    System.err.printf("[FOCAS2] 加载 fwlib32 库失败（%s）：%s%n", file.getAbsolutePath(), e.getMessage());
                }
            }
        }
        // 3. 全部失败：中文明确报错
        throw new RuntimeException("未找到 fwlib32 库（FANUC FOCAS2 通信库，Windows 为 fwlib32.dll / Linux 为 libfwlib32.so）。"
                + "请向机床厂（台丽）或 FANUC 应用工程师获取后，放入以下任一目录："
                + (libPath != null && !libPath.isEmpty() ? "组件配置 libPath=" + libPath + "；" : "")
                + "JVM 运行目录 lib/、java.library.path、系统库目录（如 /usr/local/lib）。");
    }

    /**
     * 组装候选库文件路径列表
     */
    private static List<File> buildCandidates(String libPath) {
        List<File> candidates = new ArrayList<>();
        // 组件配置的库路径：可能是文件也可能是目录
        if (libPath != null && !libPath.isEmpty()) {
            File lib = new File(libPath);
            if (lib.isFile()) {
                candidates.add(lib);
            } else {
                for (String name : libraryFileNames()) {
                    candidates.add(new File(lib, name));
                }
            }
        }
        // java.library.path 指定目录
        for (String dir : splitPaths(System.getProperty("java.library.path"))) {
            for (String name : libraryFileNames()) {
                candidates.add(new File(dir, name));
            }
        }
        // 运行目录 lib/（用户把库扔到 jar 同目录的 lib 子目录）
        candidates.addAll(collectFromDir(new File(System.getProperty("user.dir"), "lib")));
        // jar 所在目录 lib/
        try {
            String codeSource = Fwlib32Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            candidates.addAll(collectFromDir(new File(new File(codeSource).getParentFile(), "lib")));
        } catch (Exception e) {
            // code source 获取失败不影响后续搜索
        }
        // PATH 环境变量目录
        for (String dir : splitPaths(System.getenv("PATH"))) {
            for (String name : libraryFileNames()) {
                candidates.add(new File(dir, name));
            }
        }
        // 常见系统库目录
        if (isWindows()) {
            for (String name : libraryFileNames()) {
                candidates.add(new File("C:\\Windows\\System32", name));
                candidates.add(new File("C:\\Windows\\SysWOW64", name));
            }
        } else {
            for (String name : libraryFileNames()) {
                candidates.add(new File("/usr/local/lib", name));
                candidates.add(new File("/usr/lib", name));
                candidates.add(new File("/usr/lib64", name));
                candidates.add(new File("/opt", name));
            }
        }
        return candidates;
    }

    /**
     * 收集目录下的所有库文件候选
     */
    private static List<File> collectFromDir(File dir) {
        List<File> files = new ArrayList<>();
        if (dir != null && dir.isDirectory()) {
            for (String name : libraryFileNames()) {
                files.add(new File(dir, name));
            }
        }
        return files;
    }

    /**
     * 当前操作系统下 fwlib 库的可能文件名
     * 项目实际使用官方 FOCAS2 64 位库（Fwlib64.dll 可改名 fwlib32.dll 部署），
     * 四种文件名都搜，JNA 接口（Fwlib32）的函数名与 Fwlib64.dll 导出名一致，可通用加载
     */
    private static List<String> libraryFileNames() {
        List<String> names = new ArrayList<>();
        if (isWindows()) {
            // 由AI修改：FOCAS2 以太网建连首选 Ethernet 专用库 fwlibe64.dll。
            // 台丽 CNC（FANUC 0i-MF Plus）实测：fwlib32.dll（通用 Data Window Library x64）握手被机床拒
            // （EW_SOCKET -15），fwlibe64.dll（Data Window Library for Ethernet）建连成功。
            // fwlib30i64.dll 虽标注支持 0i-F，但其导出函数名与标准 fwlib 不同（无 cnc_allclibhndl3），JNA 加载必失败，故不再纳入候选。
            names.add("fwlibe64.dll");
            names.add("Fwlib64.dll");
            names.add("fwlib64.dll");
            names.add("fwlib32.dll");
            names.add("Fwlib32.dll");
        } else if (isMac()) {
            names.add("libfwlib32.dylib");
            names.add("fwlib32.dylib");
        } else {
            // Linux：大小写敏感，列出常见命名
            names.add("libfwlib32.so");
            names.add("libFwlib32.so");
            names.add("fwlib32.so");
            names.add("Fwlib32.so");
        }
        return names;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    private static boolean isMac() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("mac");
    }

    /**
     * 按系统路径分隔符拆分路径字符串
     */
    private static List<String> splitPaths(String paths) {
        List<String> dirs = new ArrayList<>();
        if (paths == null || paths.isEmpty()) {
            return dirs;
        }
        String separator = isWindows() ? ";" : ":";
        for (String p : paths.split(separator)) {
            if (p != null && !p.trim().isEmpty()) {
                dirs.add(p.trim());
            }
        }
        return dirs;
    }
}
