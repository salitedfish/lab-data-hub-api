# lab-data-hub-api

物联网平台后端服务（基于 RuoYi 3.9.0 二次开发），包名 `com.labdatahub`。

## 技术栈

- Java 1.8 + Maven 多模块
- Spring Boot 2.5.15
- MyBatis-Plus + PageHelper 分页
- Druid 数据库连接池
- Redis（spring-boot-starter-data-redis，lettuce）
- Springfox Swagger3
- JWT 鉴权 + Spring Security + Kaptcha 验证码
- fastjson2、POI、Quartz（labdatahub-quartz）
- 数据库：PostgreSQL（`application-pgsql.yml`）为主，另有 `application-druid.yml`（MySQL 备用）

## Maven 模块

| 模块 | 作用 |
| --- | --- |
| `labdatahub-admin` | 启动模块（含 `LabdatahubApplication`）、配置文件、Web 层入口 |
| `labdatahub-framework` | 框架核心：Security、拦截器、注解、AOP |
| `labdatahub-system` | 系统管理：用户/角色/菜单/字典/日志等 |
| `labdatahub-business` | **业务主模块**：设备、产品、协议、告警、指令下发、规则引擎、消息解析 |
| `labdatahub-common` | 通用工具：常量、BaseEntity、工具类 |
| `labdatahub-component` | 网络组件接入 |
| `labdatahub-quartz` | 定时任务 |
| `labdatahub-generator` | 代码生成 |

`labdatahub-business` 业务包划分：`controller` / `service` / `mapper` / `domain` / `down`（指令下发）/ `warn`（告警）/ `engine`（规则引擎）/ `process` / `script` / `event` / `scheduled` / `task` / `utils`。

## 启动

```bash
# 在 lab-data-hub-api 根目录（含父 pom.xml）
mvn clean install -DskipTests        # 先装父依赖
mvn spring-boot:run -pl labdatahub-admin
# 或直接运行 LabdatahubApplication
```

- 端口：`8085`，context-path：`/`
- 活跃配置：`spring.profiles.active: pgsql`（见 `labdatahub-admin/src/main/resources/application.yml`）
- 协议包位于 `protocol/` 目录，由 `scprotocol` 子项目产出，需先编译放入
- 轮询类协议（Modbus / S7 / Fins / Brother）的点位解析参数来自物模型 `labdatahub_properties` 的 `dataType`/`byteOrder`/`isSigned`/`scale`/`offset`（`identifier` 与协议 `code` 一一对应），由 `business.utils.ParseMetaUtils` 在构建 `XxxReadConfig` 时注入，随 decode JSONObject 传给协议库解析
- **PLC 协议（Modbus `MODBUS_TCP` / S7-1200 `S71200_TCP` / 欧姆龙 FINS `OMRONFINS_TCP`）**：三个协议共用同一"定时生产-消费"读链路（`scheduled/TimerTask#initXxxTcpRead` 启动 → `XxxMessageScheduler` 按 intervalTime 秒定时 → `XxxLoopConsumer` 单线程消费 → `XxxMessageConsumeService` → 协议库 decode → 广播+`MessageCache`+`device.up`）。`TimerTask` 三个 init 已与 Brother/FANUC 对齐：netType 门（只拉起本协议网络组件下的设备）+ `ParseMetaUtils.applyTo` 注入物模型解析参数（启动即生效）+ delayTime/intervalTime 空值兜底。三个 controller 的 add/edit 均有字段校验（`AjaxResult.error` 中文提示）；消费端均消费 `delayTime`（读取后延迟，毫秒）。
  - **Modbus（`labdatahub-component/.../modbus_tcp`，jamod 库）**：读按功能码分派 `ModbusDataReader#readByFunction`——01线圈 / 02离散输入 / 03保持寄存器 / 04输入寄存器（03/04 ≤125、01/02 ≤2000 上限校验，连接异常自动重建重连一次）；`registerRange` 多子区间由 `ModbusMessageConsumeService` 合并为 `[minStart, maxEnd]` 单块读取，保证一个 code 对应一块寄存器（decode 同 code 键不再互覆盖）。表 `labdatahub_modbus_config` 新增 `function_code`（默认 '03'，迁移脚本 `sql/pgsql/plc_read_enhance.sql`）。
  - **S7-1200（`labdatahub-component/.../s7_tcp`，s7connector 库）**：区类型 `area_type`（DB数据块/M标志位/I输入区/Q输出区，默认 DB）→ `DaveArea`（DB/FLAGS/INPUTS/OUTPUTS）；块类型 DBW(16位整型)/DBX(位)/DBD/DBB(GBK字符串)；DBD 按物模型 dataType 区分——int 系列按 DINT 32位大端整型、否则按 32位浮点 Real。连接自愈修复：读异常 `S7MessageConsumeService` 调 `S7ConnectionManager#forceReconnect` 强制重连（原 checkAndReconnect 仅连接为空才重连，PLC 重启后读失败不自愈）；`buildConnector` 带 `withTimeout(config.getTimeout())`。表 `labdatahub_s71200_config` 新增 `area_type`。
  - **欧姆龙 FINS（`labdatahub-component/.../fins_tcp`）**：存储区 `areaCode` 白名单校验（DM 0x82 / CIO 0x30 / WR 0xB1 / H 0x31 / IR 0x80 / LR 0x98 / EM 0xA0），前端下拉录入。死代码包 `omron_fins_tcp`（非标准 FINS 帧、未接线）已删除，活跃实现为 `fins_tcp`。
- **Brother NC 协议（`BROTHER_TCP`）**：机床直连平台采集，TCP 10000 只读。点位地址 = `数据区.行号.字段序号`（如 `PDSP.4.1` = 机械坐标 P01 行第 1 字段 X 轴），`component` 模块 `com.labdatahub.component.brother_tcp` 包实现连接/调度/消费，点位配置存 `labdatahub_brother_config` 表（data_area/row_number/field_index），由 `scheduled/TimerTask#initBrotherTcpRead` 启动轮询、`utils/ProtocolReadConfigRebuilder#rebuildBrother` 重建，指令下发只读 stub（`DeviceDownUtils#brotherTcpDown`）。PDSP 语义点位表为**前端内置**（`lab-data-hub-web/src/utils/brotherTcpPoints.js`，语义点位→地址映射，选点后 code 填点位 key），后端不感知点表，仅做地址合法性校验：`LabdatahubBrotherConfigController#add/edit` 校验 标识/数据区 非空、行号/字段序号/间隔 >0，`syncConfigToDevice`/`readSwitchByDevice`/`readSwitchByProduct` 对 delayTime/intervalTime 做空值兜底防 NPE。**定时读取开关**：`readSwitchByDevice`/`readSwitchByProduct`（Brother/Modbus/S7/Fins/Db 五个控制器相同）切换内存调度器后**同时把 `device.modbusRead` 落库**（`setModbusRead(isOpen)` + `updateById`），保证刷新页面后开关状态与实际读取一致。连接自愈：机床会周期性掐断长连接，`BrotherTcpDataReader#readDataArea` 遇连接级异常（broken pipe / connection reset / 对端关闭）自动重连一次并重发；健康检查 `isConnectionValid` 只查本地 socket 状态（**不发送 OOB 紧急字节** 0xFF 探测，避免打乱机床协议栈），`reconnect`/`forceReconnect` 先关旧连接再建新连接，避免与机床单活动连接冲突
- **FANUC FOCAS2 协议（`FANUC_TCP`，台丽 CNC / FANUC 0i-MF Plus）**：机床直连平台采集，FOCAS2 二进制 RPC over TCP **8193**，只读。Java 通过 **JNA** 调用 FANUC 官方 C 库 `fwlib32`（Windows `fwlib32.dll` / Linux `libfwlib32.so`，**专有软件，向台丽/FANUC 获取，平台不重新分发**）。JNA 绑定在 `labdatahub-component` 模块 `com.labdatahub.component.fanuc_focas` 包（正常 Maven 依赖，`pom.xml` 已加 `net.java.dev.jna`）；scprotocol 只做解码（不感知地址、无 JNA）。点位地址 = `采集项类型.参数1.参数2`（如 `axis.1.1` = X 轴机械坐标；`readType` 取值 axis/spindle/feed/mode/status/prgnum/alarm/tcode/macro/timer/pmc），配置存 `labdatahub_fanuc_config` 表（read_type/param1/param2），由 `scheduled/TimerTask#initFanucTcpRead` 启动轮询、`utils/ProtocolReadConfigRebuilder#rebuildFanuc` 重建。**fwlib32 库加载**：`Fwlib32Loader` 自动搜索（`other_config.libPath` → `java.library.path` → jar 同目录 `lib/` → 常见路径），未找到抛中文明确报错；部署 = 把库文件放进任一被搜索目录即可。**连接自愈**：FOCAS2 句柄（`unsigned short`，JNA 必须用 `ShortByReference` 接收）即连接，`FanucFocasConnectionManager` 每 10s 用 `cnc_statinfo` 探测健康、失效自动重连；读失败 `FanucFocasDataReader` 触发 `forceReconnect` 重试一次；每 component 一把 `ReentrantLock` 串行化同句柄并发调用。JNA 结构体字段偏移为 FOCAS2 对接最关键环节，**第一次真机/NCGuide 验证后需核对修正**。读开关落库同其他协议（`readSwitchByDevice/readSwitchByProduct` 持久化 `device.modbusRead`）；指令下发只读 stub（`DeviceDownUtils#fanucFocasDown`）。**运行约束**：轮询频率 ≤10 次/秒（FOCAS2 走机床内嵌以太网，共享 CNC CPU），并发客户端 ≤5。
- **设备日志查询**：`LabdatahubDeviceLogsController#list` 支持 `propertyName`（物模型标识符）过滤，对 `properties` 字段（存 DecodeMessage JSON，`{"properties":{identifier:value}}`）用引号包裹的标识符做 LIKE 匹配，避免误命中数值
- Redis 配置在 `application.yml` 的 `spring.redis`

## 编码规范

1. **AI 修改/创建的 Java 文件**，文件开头添加注释 `//由AI修改`；XML、YAML、properties 等非 Java 文件不加。
2. **所有 class 字段必须加中文注释**。
3. 列表循环尽量用增强 for（`for (Item item : list)`），少用下标循环。
4. 接口返回字段使用现有 `AjaxResult` / 分页 `TableDataInfo` 约定（RuoYi 体系）。
