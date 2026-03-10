package com.labdatahub.business.script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-12-02
 */
public class ScriptUtils {
    private final ScriptEngine engine;
    private final Invocable invocable;
    private final ScheduledExecutorService executorService;

    public ScriptUtils() {
        this.engine = new ScriptEngineManager().getEngineByName("javascript");
        this.invocable = (Invocable) engine;
        this.executorService = Executors.newScheduledThreadPool(5);
    }

    /**
     * 初始化JavaScript环境
     */
    public void initialize() throws ScriptException {
        // 加载工具函数库
        String utils =
                "var ScriptUtils = {\n" +
                        "    formatDate: function(date) {\n" +
                        "        return date.getFullYear() + '-' + \n" +
                        "               (date.getMonth() + 1) + '-' + \n" +
                        "               date.getDate();\n" +
                        "    },\n" +
                        "    calculateTax: function(amount, rate) {\n" +
                        "        return amount * rate;\n" +
                        "    },\n" +
                        "    validateEmail: function(email) {\n" +
                        "        var re = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/;\n" +
                        "        return re.test(email);\n" +
                        "    }\n" +
                        "};\n" +
                        "\n" +
                        "// 日志函数\n" +
                        "function log(message) {\n" +
                        "    print('[JS] ' + message);\n" +
                        "}";

        engine.eval(utils);
    }

    /**
     * 执行规则验证
     */
    public boolean validateRule(String ruleScript, Map<String, Object> data)
            throws ScriptException {
        try {
            // 绑定数据
            engine.put("data", data);

            // 执行验证规则
            String script =
                    "var result = false;\n" +
                            "try {\n" +
                            "    result = (" + ruleScript + ");\n" +
                            "} catch(e) {\n" +
                            "    log('规则执行错误: ' + e.message);\n" +
                            "    result = false;\n" +
                            "}\n" +
                            "result;";

            Object result = engine.eval(script);
            return Boolean.TRUE.equals(result);

        } finally {
            // 清理绑定
            engine.put("data", null);
        }
    }

    /**
     * 执行数据转换脚本
     */
    public Object transformData(String transformScript, Map<String, Object> sourceData)
            throws ScriptException {
        engine.put("input", sourceData);

        String script =
                "var output = {};\n" +
                        "try {\n" +
                        "    " + transformScript + "\n" +
                        "} catch(e) {\n" +
                        "    log('转换失败: ' + e.message);\n" +
                        "    output = { error: e.message };\n" +
                        "}\n" +
                        "output;";

        Object result = engine.eval(script);
        engine.put("input", null);
        return result;
    }

    /**
     * 异步执行JavaScript脚本
     */
    public CompletableFuture<Object> executeAsync(String jsCode, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 绑定参数
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    engine.put(entry.getKey(), entry.getValue());
                }

                Object result = engine.eval(jsCode);

                // 清理绑定
                for (String key : params.keySet()) {
                    engine.put(key, null);
                }

                return result;

            } catch (ScriptException e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    /**
     * 定时执行JavaScript任务
     */
    public ScheduledFuture<?> scheduleTask(String taskScript, long initialDelay,
                                           long period, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(() -> {
            try {
                engine.eval(taskScript);
            } catch (ScriptException e) {
                System.err.println("定时任务执行失败: " + e.getMessage());
            }
        }, initialDelay, period, unit);
    }

    /**
     * 创建JavaScript函数并缓存
     */
    public <T> T createFunction(String jsCode, String functionName, Class<T> returnType)
            throws ScriptException {
        // 执行JS代码定义函数
        engine.eval(jsCode);

        // 获取函数引用
        Object function = engine.get(functionName);
        if (function == null) {
            throw new ScriptException("函数未定义: " + functionName);
        }

        // 转换为Java接口
        return invocable.getInterface(function, returnType);
    }

    /**
     * 示例：业务规则验证
     */
    public void businessExample() throws ScriptException {
        initialize();

        // 规则1：验证用户年龄
        String ageRule = "data.age >= 18 && data.age <= 60";

        Map<String, Object> user1 = new HashMap<>();
        user1.put("name","张三");
        user1.put("age","25");
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name","李四");
        user2.put("age","17");

        System.out.println("用户1年龄验证: " + validateRule(ageRule, user1)); // true
        System.out.println("用户2年龄验证: " + validateRule(ageRule, user2)); // false

        // 规则2：验证订单金额
        String orderRule =
                "data.totalAmount > 0 && \n" +
                        "data.items.length > 0 && \n" +
                        "data.totalAmount <= data.maxLimit";

        Map<String, Object> order = new HashMap<>();
        order.put("totalAmount","1000.0");
        order.put("age","17");
        order.put("items",new Object[]{"item1", "item2"});
        order.put("maxLimit","5000.0");
        System.out.println("订单验证: " + validateRule(orderRule, order)); // true

        // 数据转换示例
        String transformScript =
                "output.userId = input.id;\n" +
                        "output.userName = input.firstName + ' ' + input.lastName;\n" +
                        "output.fullAddress = input.address.city + ', ' + input.address.street;\n" +
                        "output.createdTime = ScriptUtils.formatDate(new Date());";

        Map<String, Object> sourceData = new HashMap<>();
        Map<String, Object> address = new HashMap<>();
        address.put("city","北京");
        address.put("street","长安街");
        sourceData.put("id","USER001");
        sourceData.put("firstName","John");
        sourceData.put("lastName","Doe");
        sourceData.put("address",address);

        Object transformed = transformData(transformScript, sourceData);
        System.out.println("转换结果: " + transformed);
    }

    /**
     * 示例：创建和使用JavaScript函数接口
     */
    public void functionInterfaceExample() throws ScriptException, NoSuchMethodException {
        // 定义JavaScript计算器
        String calculatorScript =
                "function Calculator() {\n" +
                        "    this.add = function(a, b) { return a + b; };\n" +
                        "    this.subtract = function(a, b) { return a - b; };\n" +
                        "    this.multiply = function(a, b) { return a * b; };\n" +
                        "    this.divide = function(a, b) { return b !== 0 ? a / b : NaN; };\n" +
                        "}\n" +
                        "\n" +
                        "// 创建计算器实例\n" +
                        "var calculator = new Calculator();";

        engine.eval(calculatorScript);

        // 获取计算器对象
        Object calculator = engine.get("calculator");

        // 调用JavaScript方法
        Object result = invocable.invokeMethod(calculator, "add", 10, 20);
        System.out.println("10 + 20 = " + result);

        result = invocable.invokeMethod(calculator, "multiply", 5, 6);
        System.out.println("5 * 6 = " + result);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            executorService.shutdown();
        } finally {
            super.finalize();
        }
    }

    public static void main(String[] args) {
        try {
            ScriptUtils executor = new ScriptUtils();

            System.out.println("=== 业务规则验证示例 ===");
            executor.businessExample();

            System.out.println("\n=== 函数接口示例 ===");
            executor.functionInterfaceExample();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
