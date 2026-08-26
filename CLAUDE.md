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

### 本机环境（2026-08-25 搭建）

- JDK 8（Temurin 8u502）：`C:\Users\20357\dev\jdk8u502-b07`
- Maven 3.9.9：`C:\Users\20357\dev\apache-maven-3.9.9`
- 用户级环境变量 `JAVA_HOME` / `M2_HOME` / `Path` 已配置（**新开终端生效**）；旧 shell 需先 `set JAVA_HOME=C:\Users\20357\dev\jdk8u502-b07` 或用绝对路径调 `mvn.cmd`
- 构建：`mvn clean install -DskipTests`（依赖走阿里云镜像，见根 pom 的 `repositories`）

- 端口：`8085`，context-path：`/`
- 活跃配置：`spring.profiles.active: pgsql`（见 `labdatahub-admin/src/main/resources/application.yml`）
- 协议包位于 `protocol/` 目录，由 `scprotocol` 子项目产出，需先编译放入
- 轮询类协议（Modbus / S7 / Fins / Brother / 三菱 MC）的点位解析参数来自物模型 `labdatahub_properties` 的 `dataType`/`byteOrder`/`isSigned`/`scale`/`offset`（`identifier` 与协议 `code` 一一对应），由 `business.utils.ParseMetaUtils` 在构建 `XxxReadConfig` 时注入，随 decode JSONObject 传给协议库解析
- **PLC 协议（Modbus `MODBUS_TCP` / S7-1200 `S71200_TCP` / 欧姆龙 FINS `OMRONFINS_TCP` / 三菱 MC `MITSUBISHI_TCP`）**：四个协议共用同一"定时生产-消费"读链路（`scheduled/TimerTask#initXxxTcpRead` 启动 → `XxxMessageScheduler` 按 intervalTime 秒定时 → `XxxLoopConsumer` 单线程消费 → `XxxMessageConsumeService` → 协议库 decode → 广播+`MessageCache`+`device.up`）。`TimerTask` 四个 init 已与 Brother/FANUC 对齐：netType 门（只拉起本协议网络组件下的设备）+ `ParseMetaUtils.applyTo` 注入物模型解析参数（启动即生效）+ delayTime/intervalTime 空值兜底。四个 controller 的 add/edit 均有字段校验（`AjaxResult.error` 中文提示）；消费端均消费 `delayTime`（读取后延迟，毫秒）。**健壮性统一约定**：所有 `XxxMessageScheduler#addReadConfig/removeReadConfig` 对空 `componentId`（设备未绑定网络组件）直接跳过不抛异常（`takeMessage/pollMessage` 仍抛），关闭组件时 `removeMessageQueue` 会一并取消该组件全部定时任务（`configTaskMap` 按 `componentId_` 前缀清理，杜绝僵尸调度）；`readSwitchByDevice`/`readSwitchByProduct` 在 `syncConfigToDevice`/`readSwitchByDevice`/`readSwitchByProduct` 三处均对 delayTime/intervalTime 做空值兜底（防 NPE），且 `readSwitchByDevice` 对设备不存在返回 `AjaxResult.error("设备不存在")` 守卫（Modbus/S7/Fins/Mitsubishi/Db/Fanuc/Brother 七个控制器一致）。**配置保存即调度（2026-08-25）**：七个控制器的 `add/edit/remove` 也在保存/删除后经私有方法 `syncConfigToScheduler` 立即同步调度器（`device.modbusRead=="1"` 且已绑定组件时），新增/修改/删除点位无需重启服务或重开关读取即生效（详见 FANUC 段落）。`MessageCache.DEVICE_LAST_DATA` 为 `ConcurrentHashMap`（多协议消费线程并发写不同设备，避免普通 HashMap 并发写死循环/数据错乱）。
  - **Modbus（`labdatahub-component/.../modbus_tcp`，jamod 库）**：读按功能码分派 `ModbusDataReader#readByFunction`——01线圈 / 02离散输入 / 03保持寄存器 / 04输入寄存器（03/04 ≤125、01/02 ≤2000 上限校验，连接异常自动重建重连一次）；`registerRange` 多子区间由 `ModbusMessageConsumeService` 合并为 `[minStart, maxEnd]` 单块读取，保证一个 code 对应一块寄存器（decode 同 code 键不再互覆盖）。**递归重连上限**：`readByFunctionWithConn`/`readHoldingRegisters` 的连接异常重试带 `retryLeft` 参数（顶层传 1、递归传 `retryLeft-1`），防止"连接异常→重建→再异常→再重建"无限递归耗尽栈。表 `labdatahub_modbus_config` 新增 `function_code`（默认 '03'，迁移脚本 `sql/pgsql/plc_read_enhance.sql`）。
  - **S7-1200（`labdatahub-component/.../s7_tcp`，s7connector 库）**：区类型 `area_type`（DB数据块/M标志位/I输入区/Q输出区，默认 DB）→ `DaveArea`（DB/FLAGS/INPUTS/OUTPUTS）；块类型 DBW(16位整型)/DBX(位)/DBD/DBB；DBD 按物模型 dataType 区分——int 系列按 DINT 32位大端整型（**isSigned=1 按有符号 int 返回，否则 `Integer.toUnsignedLong` 返回无符号 Long**，避免 32 位负数读成垃圾大数）、否则按 32位浮点 Real；DBB 为西门子 STRING（前 2 字节 最大长度+实际长度 头，内容从第 3 字节起按 GBK 解析并跳过头部）。连接自愈修复：读异常 `S7MessageConsumeService` 调 `S7ConnectionManager#forceReconnect` 强制重连（原 checkAndReconnect 仅连接为空才重连，PLC 重启后读失败不自愈）；`buildConnector` 带 `withTimeout(config.getTimeout())`。表 `labdatahub_s71200_config` 新增 `area_type`。
  - **欧姆龙 FINS（`labdatahub-component/.../fins_tcp`）**：存储区 `areaCode` 白名单校验（DM 0x82 / CIO 0x30 / WR 0xB1 / H 0x32 / IR 0x88 / LR 0x98 / EM 0xA0），前端下拉录入。**区码勘误**：H 区标准码是 0x32（0x31 是 WR 的位码，旧配置误用会静默读到 WR 区）、IR 区非标准 0x80 改为 0x88，后端白名单与前端下拉均已同步。**FINS 协议层错误不触发重连**：PLC 已正常响应但命令被拒（结束码非0、响应头/长度/数据段异常）抛 `FinsResponseException`，消费端只记日志、不 `forceReconnect`（避免连接抖动）；传输层异常（超时/断流/EOF）仍触发强制重连自愈。死代码包 `omron_fins_tcp`（非标准 FINS 帧、未接线）已删除，活跃实现为 `fins_tcp`。
  - **三菱 MC（`labdatahub-component/.../mitsubishi_tcp`，MC QnA-3E 二进制帧，TCP 默认 5007）**：无第三方库，`MitsubishiDataReader` 自组帧——**帧内多字节字段均为小端**（已对照 pymcprotocol 与 xingshuangs/iot-communication 两个真机验证实现确认）。请求 21 字节：7 字节头（子头 `D0 00` / 网络 0 / PC FF / I-O `FF 03`（0x03FF 小端）/ 站 0）+ 2 字节长度 `0C 00`（12 = 监视定时器起的数据长度，小端）+ 12 字节数据（监视定时器 `10 00`（0x0010=4秒 小端）、命令 `01 04`（0x0401 批量读，字/位统一）、子命令 `00 00` 字读 / `01 00` 位读、起始地址 3 字节小端、软元件代码 1 字节、点数 2 字节小端）。响应：先读 9 字节头（长度字段小端解析），再读 length 体，2 字节结束码 `0x0000` 正常（小端解析）；**字读数据每点 2 字节小端**，**位读响应 2 位/字节**（第 i 点 = 字节[i/2] 的 bit4（i 偶数）或 bit0（i 奇数），数据区 `ceil(点数/2)` 字节）。软元件代码：字设备 D 0xA8 / W 0xB4 / R 0xAF / ZR 0xB0 / SD 0xA9；位设备 M 0x90 / L 0x92 / B 0xA0 / X 0x9C / Y 0x9D / S 0x98 / SM 0x91 / F 0x93。X/Y 地址为**八进制**（`MitsubishiDataReader` 用 `Integer.parseInt(Integer.toString(startAddr), 8)` 解析）。字读单次 ≤960 点、位读 ≤2000 点（controller `checkConfig` 校验）。MC 3E 帧无需握手，`MitsubishiConnectionManager` 建连即用 + 10s `sendUrgentData(0xFF)` 健康检查自动重连。表 `labdatahub_mitsubishi_config`（建表脚本 `sql/pgsql/mitsubishi_config.sql`），指令下发只读 stub（`DeviceDownUtils#mitsubishiTcpDown`）。**健壮性**：消费端对 areaCode/startAddress/length 任一为 null 直接跳过（避免 Integer 自动拆箱 NPE 触发无谓重连）；controller `checkConfig` 对 X/Y 八进制地址做配置期校验（十进制写法含 8/9 数字即拒绝，提示八进制仅允许 0-7）。**真机注意**：请求子头用 QnA 兼容 3E 的 `D0 00`；若个别 PLC 拒绝，可改标准 3E 请求子头 `50 00`（响应子头两种均为 `D0 00`）。
- **Brother NC 协议（`BROTHER_TCP`）**：机床直连平台采集，TCP 10000 只读。点位地址 = `数据区.行号.字段序号`（如 `PDSP.4.1` = 机械坐标 P01 行第 1 字段 X 轴），`component` 模块 `com.labdatahub.component.brother_tcp` 包实现连接/调度/消费，点位配置存 `labdatahub_brother_config` 表（data_area/row_number/field_index），由 `scheduled/TimerTask#initBrotherTcpRead` 启动轮询、`utils/ProtocolReadConfigRebuilder#rebuildBrother` 重建，指令下发只读 stub（`DeviceDownUtils#brotherTcpDown`）。PDSP 语义点位表为**前端内置**（`lab-data-hub-web/src/utils/brotherTcpPoints.js`，语义点位→地址映射，选点后 code 填点位 key），后端不感知点表，仅做地址合法性校验：`LabdatahubBrotherConfigController#add/edit` 校验 标识/数据区 非空、行号/字段序号/间隔 >0，`syncConfigToDevice`/`readSwitchByDevice`/`readSwitchByProduct` 对 delayTime/intervalTime 做空值兜底防 NPE。**定时读取开关**：`readSwitchByDevice`/`readSwitchByProduct`（Brother/Modbus/S7/Fins/Db 五个控制器相同）切换内存调度器后**同时把 `device.modbusRead` 落库**（`setModbusRead(isOpen)` + `updateById`），保证刷新页面后开关状态与实际读取一致。连接自愈：机床会周期性掐断长连接，`BrotherTcpDataReader#readDataArea` 遇连接级异常（broken pipe / connection reset / 对端关闭）自动重连一次并重发；健康检查 `isConnectionValid` 只查本地 socket 状态（**不发送 OOB 紧急字节** 0xFF 探测，避免打乱机床协议栈），`reconnect`/`forceReconnect` 先关旧连接再建新连接，避免与机床单活动连接冲突。消费端消费 `delayTime`（读取后延迟，与 PLC 协议一致）；`addConnection` 幂等启动消费线程（已消费则不重复 `startConsume`，避免 IllegalStateException 静默失败）
- **FANUC FOCAS2 协议（`FANUC_TCP`，台丽 CNC / FANUC 0i-MF Plus）**：机床直连平台采集，FOCAS2 二进制 RPC over TCP **8193**，只读。Java 通过 **JNA** 调用 FANUC 官方 C 库 `fwlib32`（Windows `fwlib32.dll` / Linux `libfwlib32.so`，**专有软件，向台丽/FANUC 获取，平台不重新分发**）。JNA 绑定在 `labdatahub-component` 模块 `com.labdatahub.component.fanuc_focas` 包（正常 Maven 依赖，`pom.xml` 已加 `net.java.dev.jna`）；scprotocol 只做解码（不感知地址、无 JNA）。点位地址 = `采集项类型.参数1.参数2`（如 `axis.1.1` = X 轴机械坐标；`readType` 取值 axis/spindle/feed/mode/status/prgnum/alarm/tcode/macro/timer/pmc），配置存 `labdatahub_fanuc_config` 表（read_type/param1/param2），由 `scheduled/TimerTask#initFanucTcpRead` 启动轮询、`utils/ProtocolReadConfigRebuilder#rebuildFanuc` 重建。**FOCAS2 官方函数族（2026-08-25 按官方 Fwlib64.h 逐字核对修正）**：库文件为官方 FOCAS2 64 位库（`lib/fwlib32.dll` = 改名的 `Fwlib64.dll`，另含 fwlibe64.dll/fwlib30i64.dll，MD5 与官方分发包一致）。建连 `cnc_allclibhndl3`，断开用 `cnc_freelibhndl`（**无 cnc_close**）。`cnc_statinfo`→`ODBST`（9 个 short：hdck/tmmode/aut/run/motion/mstb/emergency/alarm/edit）：**机床模式 = aut**（0=MDI 1=MEM 2=*** 3=EDIT 4=HANDLE 5=JOG 6=Teach JOG 7=Teach HANDLE 8=INC 9=REF 10=RMT 11=TEST；`cnc_rdopmode` 读的是主轴调整操作模式，非机床模式），运行状态 = run（0=复位 1=STOP 2=HOLD 3=START 4=MSTR）、急停 = emergency。`cnc_rdaxisdata` 官方 6 参 `(handle, cls, type[], num, len*, ODBAXDT[])`：cls=1 坐标值、type：0=绝对 1=机械 2=相对 3=剩余、从第 1 轴开始按 len 读 N 轴，读第 axis 轴传 len=axis 取 `axdata[axis-1]`，值 = `data/10^dec`；前端 `axis` param2=1 机械 2 绝对 3 相对 4 剩余 → fwlib type 1/0/2/3。主轴：转速 `cnc_acts`、负载 `cnc_rdspload`（data[0]）、报警 `cnc_rdalmmsg type=9`（**无 cnc_rdspindle**；倍率无直接函数需 PMC）。进给 `cnc_actf`（**无 cnc_rdact**；单位 0.1mm/min，2026-08-26 起读取值 ÷10 换算 mm/min）。程序号 `cnc_rdprgnum`→`ODBPRO.data`（**顺序号无直接函数**）。报警 `cnc_rdalmmsg(handle, type=-1, num[], ODBALMMSG[])`，**条数 = num 输出值**（param1=1 报警数量），文本取 `alms[num-1].alm_msg`（GBK 解码，多条取最后一条）。刀具 `cnc_rdmacro` 读系统宏变量 **#3901 当前刀具 / #3902 下一把**（**无 cnc_rdtcode**），length=10，**值 = `mcr_val/10^dec_val`（2026-08-26 真机修复：刀具 44 实测 #3901 存为 mcr_val=440000000/dec=7，此前 tcode 分支漏换算返回放大值 440000000）**。宏变量 `cnc_rdmacro(handle, number, 10, ODBM)`，值 = `mcr_val/10^dec_val`。计时 `cnc_rdtimer(handle, type, IODBTIME)`（0上电/1运行/2切削/3循环，分钟）。PMC `pmc_rdpmcrng(handle, adr_type, data_type, s, e, length, IODBPMC)`（adr_type 0=G 1=F 2=Y 3=X；字节单地址读 length=9；**无 pmc_rdpmc**）。消费端消费 `delayTime`（读取后延迟）。**fwlib32 库加载**：`Fwlib32Loader` 自动搜索（`other_config.libPath` → `java.library.path` → jar 同目录 `lib/` → 常见路径），未找到抛中文明确报错；部署 = 把库文件放进任一被搜索目录即可。**线程绑定根因（2026-08-25 修复并真机验证）**：FOCAS2 fwlib 连接**绑定创建线程**——`cnc_allclibhndl3` 在哪个线程建连，就只有该线程能读（其它线程读同一句柄返回 `EW_BUSY` -8，实测：addConnection 线程读=0、消费/健康检查线程读=-8）。**连接必须由消费线程在其本线程建立**：`FanucFocasConnectionManager#establishConnection`（在调用线程 cnc_allclibhndl3）由 `FanucFocasLoopConsumer` 循环顶部在句柄缺失时调用（失败退避 `CONNECT_RETRY_INTERVAL_MS=5s` 重试）；`addConnection` 只存配置+启动消费线程+同步等待建连结果（≤8s）；健康检查线程每 10s 建**临时探测连接**自查（建连→statinfo→释放都在健康检查线程，不读/不建共享句柄），**不在健康检查线程重连**（重连统一由消费线程自愈）；读失败 `FanucFocasDataReader` 触发 `forceReconnect`（在消费线程执行 `establishConnection`）重试一次；每 component 一把 `ReentrantLock` 串行化同句柄并发调用。**配置保存即调度（2026-08-25，七个协议 controller 统一）**：`LabdatahubFanucConfigController#add/edit/remove` 等七个控制器，保存/删除配置后若 `device.modbusRead=="1"` 且设备已绑定网络组件，立即 `removeReadConfig`+`addReadConfig`（私有方法 `syncConfigToScheduler(belongSn, config, removeOnly)`，参照 readSwitchByDevice 写法 + `ParseMetaUtils.applyTo` 注入物模型参数；DB 协议每设备单调度、code=null、删除后按剩余配置重建）——新增/修改/删除点位**无需重启服务或重开关读取即生效**（产品模板按 belongSn 查不到设备自动跳过，由 syncConfigToDevice 下发）。JNA 结构体字段偏移为 FOCAS2 对接最关键环节，**第一次真机/NCGuide 验证后需核对修正**。**真机验证（2026-08-26 台丽 0i-MF Plus 直连 192.168.1.2:8193）**：受控轴 4 根 X/Y/Z/A（`cnc_rdaxisdata` len 输出确认；B/C 读回 null，点位保留）、四类坐标均有效（dec=3）、mode/status/prgnum/timer/alarm/spindle 全部按预期、#500-509 未使用宏返回 0 不报错、F1/F0/G8 实测字节 0x80/0x40/0x31（位含义需按机床 PMC 梯形图确认，pmc 分组为占位示例）。读开关落库同其他协议（`readSwitchByDevice/readSwitchByProduct` 持久化 `device.modbusRead`）；指令下发只读 stub（`DeviceDownUtils#fanucFocasDown`）。**运行约束**：轮询频率 ≤10 次/秒（FOCAS2 走机床内嵌以太网，共享 CNC CPU），并发客户端 ≤5。
- **设备日志查询**：`LabdatahubDeviceLogsController#list` 支持 `propertyName`（物模型标识符）过滤，对 `properties` 字段（存 DecodeMessage JSON，`{"properties":{identifier:value}}`）用引号包裹的标识符做 LIKE 匹配，避免误命中数值
- Redis 配置在 `application.yml` 的 `spring.redis`

## 编码规范

1. **AI 修改/创建的 Java 文件**，文件开头添加注释 `//由AI修改`；XML、YAML、properties 等非 Java 文件不加。
2. **所有 class 字段必须加中文注释**。
3. 列表循环尽量用增强 for（`for (Item item : list)`），少用下标循环。
4. 接口返回字段使用现有 `AjaxResult` / 分页 `TableDataInfo` 约定（RuoYi 体系）。
