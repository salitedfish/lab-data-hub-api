# lab-data-hub-api

物联网平台后端服务（基于 RuoYi 3.9.0 二次开发），包名 `com.labdatahub`。

> 本文件只描述**当前状态**（架构、命令、契约、仍生效的约束）。改动过程、排查经历、历史事故按日期记录在仓库根 `开发日志/`（一天一个文件）；需要“为什么变成现在这样”时去那里查。

## 技术栈

- Java 1.8 + Maven 多模块
- Spring Boot 2.5.15
- MyBatis-Plus + PageHelper 分页
- Druid 数据库连接池
- Redis（spring-boot-starter-data-redis，lettuce）
- Springfox Swagger3
- JWT 鉴权 + Spring Security + Kaptcha 验证码
- fastjson2、POI、Quartz（labdatahub-quartz）
- 数据库：PostgreSQL（`application-pgsql.yml`）为主；`application-druid.yml`（MySQL）**不会被加载**（`spring.profiles.active: pgsql`），改它不生效

## Maven 模块

| 模块 | 作用 |
| --- | --- |
| `labdatahub-admin` | 启动模块（含 `LabdatahubApplication`）、配置文件、Web 层入口 |
| `labdatahub-framework` | 框架核心：Security、拦截器、注解、AOP |
| `labdatahub-system` | 系统管理：用户/角色/菜单/字典/日志等 |
| `labdatahub-business` | **业务主模块**：设备、产品、协议、告警、指令下发、规则引擎、消息解析 |
| `labdatahub-common` | 通用工具：常量、BaseEntity、工具类 |
| `labdatahub-component` | 网络组件接入与统一采集引擎 |
| `labdatahub-quartz` | 定时任务 |
| `labdatahub-generator` | 代码生成 |

`labdatahub-business` 包划分：`controller` / `service` / `mapper` / `domain` / `down`（指令下发）/ `warn`（告警）/ `engine`（规则引擎）/ `process` / `script` / `event` / `scheduled` / `task` / `utils`。

## 启动与本机构建

- 端口 `8085`，context-path `/`；活跃 profile `pgsql`（`labdatahub-admin/src/main/resources/application.yml`）
- 协议包放 `labdatahub-admin/protocol/`，运行期按 `user.dir/protocol` 查找，**库里的 `local_url` 不参与**；启动时 cwd 必须是 `labdatahub-admin`
- JDK 8 / Maven 3.9.9 的路径与构建命令见根 `CLAUDE.md`「环境」「打包」

**两个构建陷阱**：

1. **增量构建可能产出旧 jar**：`mvn install -pl labdatahub-admin` 源码没变时可能不重建。改动 `labdatahub-component` 后必须 `mvn clean install -pl labdatahub-component,labdatahub-admin`；构建后 `unzip -l target/labdatahub-admin.jar | grep labdatahub-business` 核对内嵌包时间戳，别只看 `BUILD SUCCESS`。
2. **cwd 不能在 `target/` 里**：Windows 目录句柄会让 `mvn clean` 删不掉空目录、直接 BUILD FAILURE。

## 采集主链路：统一采集引擎

八个协议族（`CollectionFamily`：MODBUS / S7 / FINS / MC3E / BROTHER / FANUC / CNC / DATABASE）全部走 `labdatahub-component/.../collect/`，**没有任何开关或 `labdatahub.collect.*` 配置项**。

**管道**：

```
TimerTask#initXxxTcpRead (@PostConstruct, 启动时一次)
  → CollectionEngine 注册各协议点位
  → CollectionScheduler 按「间隔 → adapter.unitKey(point)」分组成 ReadTask
  → PRODUCER.scheduleAtFixedRate
  → CollectionQueue（容量 200，按 taskKey 去重：同一任务未消费时再入队会被丢弃）
  → 每组件一条 CollectionLoopConsumer（线程名 <family 小写>-loop-consumer-<componentId>）
  → CollectionAdapter#execute 读设备
  → CollectionPublisher：组件频道广播 → ProtocolManager 反射 decode → MessageUtils.parseMessage
     → 设备 WS + MessageCache + EventBus device.up
```

各协议 `XxxMessageScheduler` / `XxxLoopConsumer` 只是**门面**（委托给 `CollectionEngine`），`XxxMessageConsumeService` 已不存在。新增协议实现 `CollectionAdapter` 并注册进 `CollectionRegistry` 即可。

**固定参数（不要调）**：

- `QUEUE_CAPACITY = 200`（一条任务覆盖一批点位，不需要旧链路的 1000）
- `MERGE_GAP = 0`（只合并重叠/相邻地址，绝不补读设备未实现的地址）
- `POLL_TIMEOUT_MS = 1000`（消费者到点回来重查连接，用于自愈）
- 不再消费 `delayTime`（字段保留，引擎不读）
- 消费线程连续异常时前 3 次打全量（含堆栈）、之后每 60 秒一条汇总；恢复时打一条 `消费已恢复正常`

**各协议合并粒度**：

| 协议族 | 采集单元 `unitKey` | 单次读上限 | 备注 |
|---|---|---|---|
| MODBUS | 设备 + 从站 + 寄存器区 + 功能码 | 125 寄存器 / 2000 位 | 连续/相邻地址才合并 |
| S7 | 设备 + DB 块/区 + 类型 | 200 字节 | DBD 4 字节/点 → 50 点/次 |
| FINS | 设备 + 区码 | 1000 项 | 位区逐点 |
| MC3E | 设备 + 区码 | 960 字 | 位设备逐点 |
| BROTHER | 设备 + 数据区 | 一个数据区一条 LOD | 最多 9 区 → ≤9 次/轮 |
| FANUC | 设备 + 点位 code | 无批量接口，**一点一次 FOCAS2 调用** | 机床侧上限 10 次/秒 |
| CNC | 设备 + 点位 code | 一点一次 | — |
| DATABASE | 设备 | 一次「取最新一条」查询 | 同设备点位共用 |

**热生效**：REST 的 `add/edit/remove` 即时 `CollectionEngine.addPoint/removePoint`（150ms 防抖重建）；**直接改数据库表不生效**，必须重启后端（`@PostConstruct` 是唯一注册入口）。

**已知瓶颈**：每个点位每周期产出一条 `labdatahub_device_logs` 行（`DeviceUpListener#saveMessage` → `LogProcessor`，队列 10000、批 1000、满则丢弃并按分钟汇总）。落库 TPS = 点位数 × 频率，是统一引擎后的第一瓶颈，不是协议 I/O。

### 实测容量（本机 `-Xmx1024m` + ProtoForge 模拟器）

**测法**：`开发测试脚本/perf-bench-sql.mjs <协议> <点位数> [间隔] [窗口秒]`（单组件）、`perf-bench-multi.mjs <每协议点数> [窗口秒]`（四组件并发）；直插点位表 + 重启后端（避开验证码），20 秒窗口同时统计落库行与 WS 上报。结果追加 `开发测试脚本/perf-results.jsonl`；收尾用 `... 0` 删除 `TEST_PERF_%` 点位与物模型属性并重启。

**单点间隔**：`interval_time` 是整数秒，UI 最小 1；FANUC 例外 —— 该组件全部点位 Σ(1/interval) ≤ **10 次/秒**（`LabdatahubFanucConfigController#validatePollRate`，保存即拦）。下表 FANUC 的 50/100 点 @1s 是绕过校验直插测的，页面配不出来。

| 协议 | 点位数 | 间隔 | WS 上报/秒（达标率） | 落库行/秒（达标率） | 每点周期间隔 中位/p95 |
|---|---|---|---|---|---|
| MODBUS_TCP | 3000 | 1s | 3080.5（105%） | 2379（80%） | 1s / 2.01s |
| MODBUS_TCP | 5000 | 10s | 500（100%） | 500（100%） | 9.91s / 9.98s |
| S71200_TCP | 1000 | 1s | 1085（110%） | 1050（105%） | 1s / 1.07s |
| S71200_TCP | 2000 | 1s | 2079（105%） | 2098（105%） | 1s / 1.05s |
| S71200_TCP | 3000 | 1s | 3100（105%） | 3082.6（105%） | 1s / 1.07s |
| OMRONFINS_TCP | 500 | 1s | 525（105%） | 500（100%） | 1s / 1.02s |
| OMRONFINS_TCP | 1000 | 1s | 1049（105%） | 1050（105%） | 1s / 1.01s |
| OMRONFINS_TCP | 2000 | 1s | 2073（105%） | 2017（100%） | 1s / 1.10s |
| OMRONFINS_TCP | 3000 | 1s | 3123（100%） | 2838（95%） | 1s / 1.26s |
| MITSUBISHI_MC3E_TCP | 500 | 1s | 525（105%） | 500（100%） | 1s / 1.01s |
| MITSUBISHI_MC3E_TCP | 1000 | 1s | 1000（100%） | 1050（105%） | 1s / 1.03s |
| MITSUBISHI_MC3E_TCP | 2000 | 1s | 2100（105%） | 2000（100%） | 1s / 1.12s |
| MITSUBISHI_MC3E_TCP | 3000 | 1s | 3121（105%） | 3106（105%） | 1s / 1.23s |
| BROTHER_TCP | 20 | 1s | 21（105%） | 20（100%） | 1s / 1.07s |
| BROTHER_TCP | 200 | 1s | 210（105%） | 200（100%） | 1s / 1.03s |
| FANUC_FOCAS | 50 | 1s | 52.5（105%） | 50（100%） | 1s / 1.02s |
| FANUC_FOCAS | 100 | 1s | 105（105%） | 100（100%） | 1s / 1.03s |

（达标率 >100% 是窗口边界多算一拍，属测量噪声。`MITSUBISHI_CNC_TCP` 没有模拟器，未测。）

**多组件合计（四组件各 N 点、全部 1 秒间隔）**：

| 合计点位 | WS 上报/秒（达标率） | 落库行/秒（达标率） | WS 每点间隔 中位/p95 |
|---|---|---|---|
| 4000 | 4187（105%） | **3901（100%）** | 1s / 1.04s |
| 5000 | 5095（100%） | 3625（90%，点间不均、有饿死） | 1s / 1.07s |
| 8000 | 7405（95%） | 1676（15%） | 1s / 1.60s |

**结论**：

1. **采集侧不设点位上限**：单协议 3000 点 @1s 全部跑满周期（中位 1s、p95 ≤1.3s）。能加多少点位由采集间隔与落库能力共同决定，不由队列/线程数决定。
2. **第一瓶颈是落库**：本机单组件 2400–3100 行/秒，四组件并发最高 3901 行/秒。**全平台合计 4000 点以内（1 秒间隔）是安全区**；5000 点开始丢行且不均匀，8000 点采集也掉到 95%。多点位推荐 5–10 秒/点。
3. **协议差异**：BROTHER 一条 LOD 返回整个数据区，点位多不增加往返；FANUC 一点一次调用；FINS/MC/S7/MODBUS 是块合并，成本随读任务数增长。
4. **统计口径**：WS 上报是采集是否达标的唯一依据；`labdatahub_device_logs` 积压时 `create_time` 仍是入队时间，不能用落库行数反推采集频率。

### 采集正确性验收

- `开发测试脚本/collect-correctness.mjs`（写值型：MODBUS / S7 / FINS / MC）：向模拟器写已知值 → 等一个采集周期 → 比对落库解码值。**等待必须 < 30 秒**（ProtoForge 外部写入只冻结 30 秒），脚本用 12 秒。六协议 34/34 全过。
- `开发测试脚本/collect-static-check.mjs`（只读型：BROTHER / FANUC）：模拟器 `fixed_value` ↔ 平台最新解码值，各 6/6。FANUC `op_state` 是平台按 `status` 折算的（`run==3 START → 1`），比对时需转换。
- 失败模式区分：**写值不落地先查模拟器写冻结**（根 `CLAUDE.md`「ProtoForge 外部写入传播」）；**相邻点位值相同**多半是模拟器点位用了同参数 `triangle`/`square` 生成器，不是合并读串位。

## 协议接入

### 通用约定

- 轮询类协议的点位解析参数来自物模型 `labdatahub_properties` 的 `dataType`/`byteOrder`/`isSigned`/`scale`/`offset`（`identifier` 与协议 `code` 一一对应），由 `business.utils.ParseMetaUtils` 构建 `CollectionPoint` 时注入，随 decode JSONObject 交给协议库
- 各 `XxxConfigController` 的 `add/edit/remove` 保存后立即 `syncConfigToScheduler`（`device.modbusRead=="1"` 且已绑组件时），新增/修改/删除点位无需重启或重开读取
- `readSwitchByDevice/Product` 切换内存调度后**同时把 `device.modbusRead` 落库**，刷新页面后开关状态一致
- `MessageCache.DEVICE_LAST_DATA` 是 `ConcurrentHashMap`（多协议消费线程并发写不同设备）
- 采集类组件（MODBUS_TCP / S71200_TCP / OMRONFINS_TCP / BROTHER_TCP / FANUC_TCP / MITSUBISHI_MC3E_TCP / MITSUBISHI_MC3E_UDP / MITSUBISHI_CNC_TCP）**必须绑定协议包**：`openComponent` 在入口校验 `protocolId`，未绑直接抛 `CommonWarnException`，什么都不启动。`bindProtocol` 只写库与缓存、不注册 `PROTOCOL_MAP`，绑完必须重开组件。

**接入一台新设备的顺序**（顺序错了常见的表现就是「组件开启成功、设备永远离线」）：

1. 协议管理页上传 scprotocol 产物，网络类型选对应 netType（写 `labdatahub_protocol`，`MAIN_CLASS_PATH` 映射见 `LabdatahubProtocolServiceImpl`）
2. 新建网络组件 → 「协议绑定」里选中该协议（下拉按 netType 过滤）→ 填 IP/端口（真源是 `other_config` 的 `ipAddr`/`port`）→ 保存并启动
3. 新建产品 → 选网络组件（协议随之带出）→ 设备类型选直连设备
4. 新建设备 → 关联产品（协议与组件继承）
5. 设备详情页对应协议的配置 tab 加点位（S7 的 M 区点位 DB 块号也必须填正整数）
6. 物模型 tab 填属性，**`identifier` 必须与点位 `code` 一致**
7. 打开该 tab 的「定时读取」开关（`readSwitchByDevice`，同时把 `device.modbusRead` 落库）

### Modbus（`MODBUS_TCP`，jamod）

- 功能码 01 线圈 / 02 离散输入 / 03 保持寄存器 / 04 输入寄存器；03/04 ≤125、01/02 ≤2000（controller `checkConfig` 校验）
- `registerRange` 多子区间合并为 `[minStart, maxEnd]` 单块读，保证一个 code 对应一块寄存器
- 连接异常重试带 `retryLeft`（顶层 1、递归减 1），防「重连→再异常→再重连」无限递归
- **jamod 1.2 没有建连超时**（`setTimeout` 是 SO_TIMEOUT），黑洞设备一次建连要等满系统超时（Windows 约 21 秒）。`ModbusConnectionManager` 因此在建连前跑 `probeReachable()`（短超时 socket 探针），重连路径与 `addConnection` 首次建连都有；探针不通即失败，不再阻塞调用线程
- 重连：无上限重试，靠健康检查每 10 秒一轮限速；`reconnectInFlight` 保证每组件同一时刻只有一个重连任务（任务体在 `finally` 里移除）；重连/读失败日志每组件每分钟一条
- `getValidConnection()` 跑在消费线程上，同步重建限流 10 秒（`SYNC_RECONNECT_MIN_INTERVAL_MS`）

### S7-1200（`S71200_TCP`，s7connector）

- 区类型 `area_type`：DB 数据块 / M 标志位 / I 输入区 / Q 输出区 → `DaveArea`
- 块类型：DBW 16 位整型 / DBX 位 / DBD / DBB
- DBD 按物模型 `dataType`：int 系列按 DINT 32 位大端（`isSigned=1` 有符号，否则无符号 Long）；其余按 32 位浮点 Real
- DBB 是西门子 STRING：前 2 字节「最大长度 + 实际长度」头，内容从第 3 字节起按 GBK 解析
- 读异常调 `S7ConnectionManager#forceReconnect`（PLC 重启后读失败要自愈）；`buildConnector` 带 `withTimeout`
- 写方向：库无位写 API（DBX 只能读-改-写、不原子）；写无回显（「未抛异常 ≈ 成功」）；>96 字节会被拆成多条请求，中途失败即部分写入

### 欧姆龙 FINS（`OMRONFINS_TCP`，TCP 默认 9600）

- 存储区区码白名单，**字区码与位区码成对**：CIO `0xB0`/`0x30`、WR `0xB1`/`0x31`、HR `0xB2`/`0x32`、AR `0xB3`/`0x33`、DM `0x82`/`0x02`、EM 当前库 `0x98`/`0x18`、EM 库 0-15 `0xA0-0xAF`/`0x20-0x2F`。**区码填错不报错，只会静默读写到另一个区**
- 位区码下地址第 3 字节是位号（0-15）；请求/响应每点 1 字节（字区每点 2 字节大端）
- **`isBitArea` 判据有四处各一份**：api 的 `FinsDataReader`、协议库 `OmronFinsTcpDeal`、controller `checkConfig`、前端 `BIT_AREA_ITEMS`（两个编译单元共享不了）；改一处必须同步其余几处
- 协议层错误（PLC 已响应但结束码非 0 / 响应结构异常）抛 `FinsResponseException`，**不重连**；传输层异常（超时/断流/EOF）才 `forceReconnect`

### 三菱 MC（`MITSUBISHI_MC3E_TCP` / `MITSUBISHI_MC3E_UDP`，QnA-3E 二进制帧）

- 无第三方库，`MitsubishiMc3eDataReader` 自组帧，**帧内多字节字段均小端**
- 请求 21 字节：7 字节头（子头 `50 00` / 网络 0 / PC `FF` / I-O `FF 03` / 站 0）+ 2 字节长度 `0C 00` + 12 字节数据（监视定时器 `10 00`、命令 `01 04`、子命令 `00 00` 字读 / `01 00` 位读、起始地址 3 字节、软元件代码 1 字节、点数 2 字节）
- 响应：9 字节头（长度字段小端）+ length 体 + 2 字节结束码；**响应子头是 `D0 00`**（请求 `50 00`，别写反）。字读每点 2 字节小端；位读 2 位/字节（第 i 点 = 字节 `[i/2]` 的 bit4（偶数）/ bit0（奇数））
- 软元件码：字设备 D `0xA8` / W `0xB4` / R `0xAF` / ZR `0xB0` / SD `0xA9`；位设备 M `0x90` / L `0x92` / B `0xA0` / X `0x9C` / Y `0x9D` / S `0x98` / SM `0x91` / F `0x93`。**X/Y 地址八进制**（controller 配置期校验，含 8/9 即拒绝）
- 上限：字读 ≤960 点、位读 ≤2000 点。无握手；健康检查 `sendUrgentData(0xFF)`，10 秒一轮，自动重连
- **多字点位（int32/float32/double64）物模型必须显式选「小端」**（库默认 `big`，MC 低字在前）
- 表 `labdatahub_mitsubishi_mc3e_config`（`sql/pgsql/mitsubishi_mc3e_config.sql` 建表；老库升级 `mitsubishi_mc3e_config_rename.sql`，RENAME 不丢点位，二者不要同时执行）

**UDP 传输**：独立 netType `MITSUBISHI_MC3E_UDP`（同一套 3E 帧，`DatagramSocket`），与 TCP 共用协议 jar。`transport` 由后端按 netType 写入 `other_config`，不手填。UDP 无握手，建连/重连靠发一帧真实「读 D0」探针判可达；`exchangeUdp` 发前 `drainUdp`、收包校验副头 `D0 00` 与长度，迟到/重复包丢弃继续等。

**字内位点位 `D150.0`**：三菱字设备不支持按位读（返回 `0xC05C`），平台「读整字 + 取位」。`labdatahub_mitsubishi_mc3e_config.bit_index`（0-15，NULL=整字/整位），物模型自动建 `bool`。`checkConfig`：只能用于字设备、0-15、`length=1`。**不支持写入**（写返回 422）。

**MC1E / A-1E**：`MITSUBISHI_MC1E_TCP` / `MITSUBISHI_A1E_TCP` 只有 scprotocol 侧协议文件，api 侧帧层与平台接线未做（`NetType`/`MAIN_CLASS_PATH`/component case/建表/前端下拉都没有），既上传不了也选不到。

### 三菱 CNC（`MITSUBISHI_CNC_TCP`，MELDAS MOCHA）

TCP **683**，只读。GIOP 帧：

- 请求 = `[GIOPHead 12B][MessageHead 24B][操作名块][GetDataBody 7×uint32=28B]`，全小端
- GIOPHead：`"GIOP"` + **Version u16**（`01 00`）+ LittenEnd u8(1) + ReplyType u8(0 请求/1 应答) + MessageSize u32（= 24 + 操作名块长 + 参数体长）。**Version 是 2 字节，写成 1 字节会让整帧左移、机床不应答**
- MessageHead：serviceContextList u4 + requestId u4 + isExpectResponse u1 + reserved u1[3] + Length u4 + Key u4 + OperationLength u4（= 操作名长度 + 1）
- 操作名块 = ASCII 操作名 + NUL，补 0 到 4 的整数倍
- 响应 = `[GIOPHead 12B][ResponseHead 16B][DataResponseHead 8B][数据]`；`body[8:12]`=code、`body[16:20]`=respDataType、`body[20:24]`=dataLength、数据自偏移 24 起
- 建连后发 `mochaCancelModal2` 握手（失败不阻断）；参数体必须 **≥5 个 u32**（代码用 7 个 0），否则回 `code=2` + MARSHAL 错误

**点位寻址**：非轴键 `pst`/`opm`/`al`/`fre`/`pn`/`spn`/`cc`/`sl1`/`ss1`/`tn`/`stn`/`po`/`opt`/`cut`/`ct`/`sv`/`fv`/`st`（外加与 `pst` 同址的 `axc`，仅为老配置兼容）；轴点 `mechpos`/`currpos`/`remapos`/`cu`/`sp`，axisNo 1-6 = X/Y/Z/A/B/C。地址 = `数据区.行号.字段序号`（如 `PDSP.4.1`）。datatype 3/5/6 取数据区末尾 8 字节 double；datatype 4 按 4/8 字节读 int/long。

**机床忽略请求指定的 DataType**：回包 `respDataType` 是数据区自己的类型，`ADDRESS_MAP` 的 dataType 只影响请求帧。

**时间点位统一输出秒**：`po`/`opt`/`cut` 上电/运行/切削时间机床回**毫秒**（÷1000）；`ct` 计时回 **MMSS 打包值**（`808` = 8分08秒，`159→200` 即 1分59秒→2分00秒，按 `分×60+秒` 折算）。`MitsubishiCncDataReader#convertTimeToSeconds` 统一折算成总秒数，物模型属性单位一律配「秒」。FANUC 四个 timer 点位同理（见 FANUC 段）。

**未证实项**：`al` 回 u8 且本机恒 0；`axc` 与 `pst` 回包逐字节相同。`cur`（程序内容）走 `mochaGetCurrentPrgBlockFirst`，**尚未破解**。

**机型参数**：网络组件 `other_config.model`（`m70`/`m80`/`unknown`），**后端只存不用**，只决定前端取哪张语义点位表。`m80` 当前用的仍是 M70 表（`CNC_TCP_MODELS` 里 `hasOwnTable=false`），不能当“已支持 M80”。判据：**帧或传输层真的不同才新开 netType**（如 C70），型号差异给现有协议加参数。

### Brother NC（`BROTHER_TCP`，LOD ASCII 文本帧）

- 点位地址 = `dataArea.rowNumber.fieldIndex`；表 `labdatahub_brother_config`（data_area/row_number/field_index）
- PDSP 语义点位表在**前端内置**（`lab-data-hub-web/src/utils/brotherTcpPoints.js`），后端不感知，只做地址合法性校验（标识/数据区非空、行号/字段序号/间隔 >0）
- **计时点位（真机实测 2026-11-05，SX1 机型）**：数据区名是 **`MONTR`**（`MONT` 回状态 08 不支持）。`MONTR.T01` 字段1 总运行 / 字段2 上电 / 字段3 运行时间，单位**秒**（空闲时字段2 每秒 +1，字段1/3 只在运转时累加）。`PRDC2.L01` 字段1 循环时间 / 字段2 切削时间，单位**秒**（`L01:00002/00003` 为第二组，本机为 0）。**非切削时间**机床不直接给，点位配派生数据区 `PRDC2NC`，`BrotherCollectionAdapter` 读同一块 PRDC2 后按「循环 − 切削」输出；`unitKey` 把 `PRDC2NC` 归并到 `PRDC2`，两者共用一次整块读取。时间点位一律输出秒，不做二次换算
- **当日操作次数（真机实测 2026-11-07，192.168.151.140）**：机床「7511 发送已完成(生产数据3) → 操作时间日志」画面不直接提供可读字段，只有 `OPLOG` 数据区（A001 汇总头 + B001~B132 + C001~C200，每行若干「时间戳,值」对，共约 10000 条、新→旧、约一个月、单帧 225KB）。条目值 **257 = 一次操作（程序）结束**，按天计数即画面「操作次数」（20 天逐日与画面核对一致，个别 ±1~5 为抓帧时刻差）。点位配派生数据区 **`OPLOGCNT`**（`realArea` 映射到 `OPLOG`，`unitKey` 归并，共用一次整块读）：按「服务器本地日期」统计当天值 257 的条数；机床把本地时间写成 epoch 数值，格式化按 UTC 规则即机床墙上时间；机床时钟快于服务器时跨零点以最新记录的机床日期为准，停机日无记录则计 0。⚠️ 读 OPLOG 期间机床只接受一路连接，外部抓帧会超时，核对前先停组件
- 连接自愈：机床会周期性掐断长连接，`readDataArea` 遇 broken pipe / connection reset / 对端关闭自动重连一次并重发；健康检查只查本地 socket 状态，**不发 OOB 紧急字节**（避免打乱机床协议栈）；`reconnect/forceReconnect` 先关旧连接再建新连接

### FANUC FOCAS2（`FANUC_TCP`）

机床直连采集，FOCAS2 二进制 RPC over TCP **8193**，只读。Java 经 **JNA** 调 FANUC 官方 C 库（Windows `fwlibe64.dll` 以太网建连用这个；`Fwlib64.dll` 官方原名部署、不改名；32 位库名不列候选）。库文件专有，向台丽/FANUC 获取，平台不重新分发。`FwlibLoader` 按 `other_config.libPath` → `java.library.path` → jar 同目录 `lib/` → 常见路径搜索。

- 点位地址 = `采集项类型.参数1.参数2`（如 `axis.1.1` = X 轴机械坐标）。`readType` 取值：axis/spindle/feed/mode/status/prgnum/exeprgname/alarm/tcode/macro/timer/count/diag/override/pmc；表 `labdatahub_fanuc_config`
- 建连 `cnc_allclibhndl3`，断开 `cnc_freelibhndl`（无 `cnc_close`）
- **句柄绑定建连线程**：`cnc_allclibhndl3` 在哪个线程建连，就只有该线程能读（其他线程 `EW_BUSY -8`）。建连只能由 `FanucCollectionAdapter#ensureConnection`（跑在该组件消费线程上）发起；健康检查另建临时句柄、自己建自己用，**不在健康检查线程重连**
- 主要函数：`cnc_statinfo`→`ODBST`（机床模式 = `aut`，运行状态 = `run`，急停 = `emergency`）；`cnc_rdaxisdata`（cls=1，type 0 绝对/1 机械/2 相对/3 剩余，值 = `data/10^dec`）；主轴 `cnc_acts` 转速、`cnc_rdspload` 负载（只认 `spNo=0`）、主轴倍率读 PMC G30；进给 `cnc_actf`（单位 0.1mm/min，读值 ÷10）；`cnc_rdprgnum`→`ODBPRO.data`、`cnc_rdseqnum`→偏移 4、`cnc_exeprgname`（33 字节 GBK）；报警 `cnc_rdalmmsg(type=-1)`，条数 = `num` 输出；刀具 `cnc_rdmacro` 读 #3901 当前 / #3902 下一把，**值 = `mcr_val/10^dec_val`**；计时 `cnc_rdtimer`（`IODBTIME{minute, msec}`，平台按 `minute*60 + msec/1000` 折算成**总秒数**返回，四个 timer 点位单位均为秒）；PMC `pmc_rdpmcrng`
- **产量 count 不用 fwlib**（机床拒绝 `cnc_rdcount`/`cnc_rdparam`），改用自研 `FanucFocas2TcpClient` 读系统参数 **6711 当日 / 6712 总 / 6713 目标**。报文要点：双连接握手（第 1 连接建会话后关、第 2 连接保持，seq 从 0001 起）；请求 `0001 001c 0001 0001 008D` + 20 字节负载；负载 = 参数号连写两份 + 4 字节 0 + 8 字节 0，**长度/计数字段必须为 0**；响应在负载中按参数号回显定位 idx，值 = `load[idx+8..idx+12]`
- **运行约束**：轮询 ≤10 次/秒、并发客户端 ≤5。`validatePollRate` 在 add/edit 时按 `belongSn` 汇总 Σ(1/interval)，超过 10 直接拦
- **三个已知平台侧缺陷（模拟器侧不做迁就）**：① `Fwlib.ODBSEQNUM` 缺 4 字节 dummy 前缀，值落在结构体偏移 4，平台读偏移 0 恒得 0；② `cnc_diagnoss` JNA 声明 3 参，官方 4 参 `(handle, number, axis, buf)`，线上第 3 槽是轴号、平台传栈垃圾 → 恒 `EW_LENGTH=2`；③ `cnc_rdspload` 传 `spNo=1` 被 DLL 在客户端拒（`EW_NUMBER=3`，一个帧都不发），只有 `spNo=0` 可读
- **错误码**：`-17 = EW_PROTOCOL`（对面不是真 FOCAS2），`-16 = EW_SOCKET`（端口没人听）。见到 -17 先确认对面是不是真 FOCAS2 服务

### HTTP_SERVER（接入侧协议）

外部系统主动 POST 上报。开启组件时按 `other_config.port` 起组件专属内嵌 Tomcat（`HttpServerManager`），把**原始 HTTP 请求**整包交给绑定协议 jar 的 `decode` —— 接口形状由 jar 决定，平台不定义。出厂 `HttpServerDeal`：`POST /任意路径`，body 为扁平 JSON，顶层 `deviceSn` 标识设备，所有 key（含 `deviceSn`）都进 `properties`。

- 合格报文 → `200` 空 body；缺/空 `deviceSn`、非法 JSON、解析不出数据 → `400` + `{"code":400,"msg":…}`；组件未绑协议、协议未加载、内部错误 → `500`。**被拒报文一条都不落库**
- 解码与校验必须在**请求线程**上完成，不能挪进线程池（否则响应已提交、异常只能进日志）
- `needReply` 只决定下游同步/异步，不影响响应码
- **不存在的 deviceSn 照收 200**（组件模块不依赖 business，拿不到设备是否存在）；但不再为其写上下线日志、不污染 `DEVICE_STATUS`
- 写入不支持：`PointWriteMetaResolver` 只放行四个可写 netType，HTTP 设备写值返回 409
- **MQTT 出口发的是「设备全量缓存」不是「本次报文」**：`DeviceUpListener#buildFullDataPayload` 取 `MessageCache.getDeviceLastData(deviceSn)`（各消费线程在发 `device.up` 前已把本点位合并进缓存），所以早前推过的点位会一直跟着后续消息走；缓存是进程内的，重启才清。`MqttForwardThrottler` 按设备节流（`mqtt.forward.flush-interval-seconds`，默认 3 秒），同一设备一个窗口只发一条全量
- 在线/离线是心跳倒计时：`device_type='2'`（页面叫「无状态设备」）任何上报置在线，超过 `timeout_seconds` 自动离线（`DeviceHeartbeatManager`，进程内 `DelayQueue`，不走 Redis）

### 树根解包参考

`C:\树根平台\msi_out\`（RootDCService 安装包解包）含全部协议驱动 DLL（`*.dll_<hash>` 为同文件副本，任选一个）。FANUC 看 `RootLink.DC.Protocol.Focas.dll` / `RootLink.UI.Protocol.Focas.dll`；Brother 看 `RootLink.DC.Protocol.BrotherTcp.dll` / `...UI...`；三菱 CNC 看 `MitsubishiNcTcpPacket.dll`。挖配置值：dnfile 解析 .NET 元数据，P/Invoke 真名在 ImplMap ImportName，反查调用方方法体 `ldc` 常量。

**机型 vs 协议**：树根按「报文/传输族」分驱动，不按型号铺协议 —— 同族多型号 = 一个驱动 + 配置参数（`MitsubishiNcTcp` 自标 `MELDAS700m`，即我们 `MITSUBISHI_CNC_TCP` 的出处）；跨族才换驱动（`MitsubishiCncC70` 是另一条 UDP/共享内存线；`MitsubishiSeries` 是 PLC 线，即 `MITSUBISHI_MC3E_TCP` 的出处）。`M80` 在树根里没有点位表，现有 M70 表在同一台 M80 上 24 个地址全部读通，但**没有 M70 真机对照，不能当“M80 已支持”**。

## 设备日志查询

`LabdatahubDeviceLogsController#list` 支持 `propertyName`（物模型标识符）过滤：对 `properties` 字段（DecodeMessage JSON）用**引号包裹的标识符**做 LIKE，避免误命中数值。`startTime`/`endTime` 都为空时默认回看 **24 小时**；`export` 支持时间与 `propertyName`、按 `create_time` 倒序、上限 **5 万行**（`ExcelUtil` 整批进内存）。「取设备最新一条」的查询按 `create_time` 排序（`create_time` 有 `(device_sn, create_time DESC)` 索引）。

## 点位写值（下行）

两个入口共用一条链路，**来源由入口标明、不取请求体**：

- `OpenApiPointValueController#pointValue`（`POST /openapi/v1/device/pointValue`，`@Anonymous` 绕过 JWT，**不做签名/鉴权**）→ `PointWriteSource.API`
- `LabdatahubDeviceController#pointValue`（页面「写值」）→ `PointWriteSource.MANUAL`

四步：① `PointWriteMetaResolver.resolve(deviceSn, code, requestId)` 匹配点位与取地址；② `ProtocolEncodeInvoker.encode` 反射调协议 jar 的 `encode` 得载荷；③ `PointWriter.write` 按 netType 分派（`ModbusPointWriter` / `S7PointWriter` / `FinsPointWriter` / `MitsubishiMc3ePointWriter`，其余抛 `PointConnectionException`）；④ `OpenApiPointValueServiceImpl#saveRecord` 落审计（**进到 service 的请求全记一条**，含被拒的）。service 无协议判断，协议分叉只在 resolver 准入与 `PointWriter` 分派。

**错误码契约**：HTTP 一律 200，码值在响应体 `code`。

| code | 含义 |
|---|---|
| 400 | 参数缺失 / value 非标量 |
| 404 | 设备或点位不存在 |
| 409 | 准入类：未绑组件、netType 不在四个可写协议内、other_config 缺地址、MODBUS 无 slaveId、组件被多设备共用 |
| 422 | 值与配置不兼容（越界、类型不符、位设备 length≠1、带 bitIndex 点位写入等） |
| 500 | 平台侧意外（含协议未注册） |
| 503 | 平台侧没连上或等锁超时（**可重试**） |
| 504 | 设备侧异常，见下 |

**504 两种子情况，文案必须区分**：① **设备明确拒绝、本次未写入**（FINS / MC 的写响应带结束码，非 0 即此；S7 拿不到回执，做不到）；② **结果不确定、值可能已写入**（超时/断流，请求已发出）。两种情况重试代价不同，调用方要能分辨。

失败响应也带 `data`（`PointWriteResult`，`@JsonInclude(NON_NULL)`），`address`/`count`/`registers` **只从协议层回传的载荷来**，503/504 也照样回传，便于区分「地址算错」与「地址对但连不上」。

`PointWriteResult` 20 个字段，关键新增：`netType`（决定 address/count/registers 的单位口径：MODBUS=16 位字、S7=字节、FINS/MC=字或软元件）、`pointName`、`unit`、`dbNumber`（S7）、`areaCode`（FINS/MC）、`deviceOnline`、`componentEnabled`、`costMs`。

**`deviceOnline` / `componentEnabled` 只回显、不当判据**：`deviceOnline` 取 `CacheUtils.getDeviceStatusBySn`（与设备列表同源；别用 `DeviceHeartbeatManager`，它只对无状态设备有效）；`componentEnabled` 取 `labdatahub_component.status`，语义是组件有没有被拉起，不是连得上。

**503 / 500 文案分流**（只改文案，不改码值）：503 在 `componentEnabled` 明确为 false 时前置「网络组件当前为停用状态…」，为 null/true 时保留协议层原文（避免把「等读周期超时」误解释成组件停用）；500 在组件明确停用时换成「协议未加载，请先开启组件」，null/true 时保留「重新上传协议包」原文。

**写值回显解析**（`ProtocolEncodeInvoker.parseWriteEcho`）：① `[` 开头 = MODBUS `RangeItem` 数组（16 位字）；② 含 `"words"` 键 = FINS 与 MC 共用（`startAddress` + 字/位值列表，16 位字）；③ 其余对象 = S7 `address`/`bytes[]`（字节）。MC 刻意复用 FINS 键名，让第 ② 支不必分 netType。

**协议差异**：

- **S7**：`count` = 字节数；载荷 `{areaType,dbNumber,address,bitOffset,blockType,dataType,bytes[]}`，`bytes` 是无符号 0-255 列表（fastjson2 默认 base64）
- **FINS**：`count` = 项数（位区恒 1）；载荷 `{areaCode,startAddress,bitAddress,count,words[],dataType}`，`words` 已是最终字值/位值，writer 不做转换。位写有原生命令（无需读-改-写），写响应带结束码
- **MC3E**：`count` = 字设备字数（取 `length`）/ 位设备恒 1；载荷同 FINS；结束码可判 504 子情况

**锁策略**：每组件一把 `ReentrantLock`，读侧无界 `lock()`、写侧有界 `tryLock(5000ms)`（超时 503）；`forceReconnect` **必须在 `unlock()` 之后调**（内部真建 TCP + 退避 sleep）。

**对外文档**：`开发文档/外部平台HTTP写入PLC点位-对接文档.md`（仓库根）。改响应字段或错误码文案必须同步它。

## 写值记录查询

`LabdatahubPointWriteRecordController#list`（`/business/pointWriteRecord`）只读，无 add/edit/remove/export。数据来自 `labdatahub_point_write_record`，支持 `deviceSn`/`code`/`source`/`isSuccess`/时间范围/分页，`create_time` 倒序。前端入口是设备详情页物模型 tab 的「写值记录」，所有协议共用一个按钮/弹窗/接口（`netType` 只作数据列）。

## 规则引擎

- **输入侧设备范围不决定输出服务起不起**：`productConfig` 为 null 或范围为空时 `deviceList` 保持空，MQTT/Kafka/RocketMQ/RabbitMQ/HTTP 出口照常启动，只打 `log.warn` 说明「仅启动输出服务、不绑定设备」。controller 按返回值给 200/500
- **MQTT 出口持久化关闭**（`MqttBrokerUtils.addBroker` 设 `persistence_enabled=false`，走 moquette 内存分支）：断线期间消息不补发；broker 重启后订阅不由 broker 记住（对接方在 `on_connect` 里订阅，可自愈）。**不要加 `persistent_store` 键** —— `startServer` 见到它会强制把持久化改回 true。见到 `The file is locked` / `Can't track for expiration` 时：停进程 → 把 `data/mqtt/<engineId>_<outputId>/` 整个挪开 → 重启后端，三件一起做
- **列表页「停用」= 停掉整个 8181 MQTT broker**，外部接收方在停用期间全部断线（用户明确接受，停完尽快启用）。详情页「保存」只写库、不碰 broker
- **启停留痕**：表 `labdatahub_rule_engine_control_log`（`engine_id`/`engine_name`/`action`/`oper_name`/`oper_time`/`status`/`message`），`startRuleEngine`/`stopRuleEngine` 各包一层；删除规则引擎也留一条 `stop`，无登录态记 `system`；写日志失败只 `log.error`、不影响启停。接口 `GET /business/ruleEngine/controlLog?engineId=&pageNum=&pageSize=`

## 运行时特性与已知陷阱

### 组件启动自愈

重启后首轮开启失败的 `status=1` 组件，由 `ComponentStartupRetryer` 每 60 秒重试、窗口 10 分钟（从自愈器启动算）；全部开启或窗口到期即停，之后保持停用。手动关闭会立即取消该组件的重试（静态方法 `onManualClose`）。配置 `labdatahub.component.startup-retry.*`（默认 60/600；生产走外部 `application.yml`，里面没有这三项，用默认值）。自愈器与 `LabdatahubComponentServiceImpl` 不能互相 `@Autowired`（循环依赖）。

### 组件连不上时的「静默零数据」

页面显示已开启、日志干净，但 MQTT 一条数据都没有 —— 组件连接不通时平台就是这个样子。链路是：`addConnection` 先起消费线程、再建连接，连接失败只移除连接、保留 `configMap`（健康检查的监控名单）；调度已注册、队列已建、消费线程活着，但每次读都拿不到连接，消息在读取处被丢，日志被分钟节流压住。

排查三处一起看：① MQTT 出口有没有出数；② 连接管理器的 `<clinit>` 横幅（`Xxx 连接健康检查任务已启动`）有没有出现；③ `labdatahub_device_logs` 有没有增长。**`已添加Xxx定时配置` 只证明调度注册了**。开启失败的组件仍留在监控名单里，设备恢复后会自动重连、数据照常上报，而 `labdatahub_component.status` 仍是 0 —— 要真正停掉必须走页面「关闭」（`closeConnection` 才清 `configMap`）。

### 设备缓存与状态缓存

- `CacheUtils.DEVICE_MAP` 是**全平台**设备表，只用来判定离线与下发。`updateDeviceCacheByProductSn` **只替换本产品的条目，不能 `clear()`**（清空会让其他产品的设备永久失去离线判定）。`updateAllDeviceCache(list)` 才是重建
- 设备状态闸门是内存 `CacheUtils.DEVICE_STATUS`：增/删/改设备必须同步它；心跳设备只有缓存为 false 时才写 `status=1`；不存在的 SN 不写状态缓存、不写上下线日志（否则幽灵 SN 会把后来注册的同名设备永久卡成离线）
- 排查「页面还显示在线」：`jcmd <pid> GC.class_histogram | grep business.domain.LabdatahubDevice` 的实例数就是 `DEVICE_MAP` 条目数，与 `labdatahub_device` 行数对比

### 物模型删点位会级联删协议点位

物模型「全部保存」是整份替换（`remove by belongSn` + `saveBatch`）。`saveBatch`/`remove` 先留旧属性算差集，再调 `ProtocolPointModelSync.afterPropertiesRemoved`：设备归属 → 删 8 张协议点位表命中行、取消对应 scheduler、清 `MessageCache`、刷新物模型树；产品归属 → 只删产品模板行。`DeviceUpListener#buildFullDataPayload` 再按 `PropertyToJson.PROPERTY_TREE` 过滤一道兜底。`labdatahub_properties.id` 每次保存都会换新，做快照比对别按 id 认行。

### 物模型/协议点位的归属规则与去重

- **设备 SN 唯一**：`saveDevice` / `saveDeviceByProductSn` 在插入前查同 `device_sn` 是否已有设备行，有则拒绝（`设备sn已存在`）。复制设备时把 SN 改成已有设备的，是历史上重复配置的第一来源。
- **创建前清遗留行**：设备删除不清理 `labdatahub_properties` 与 8 张协议点位表（`LabdatahubDeviceController#remove` 只删设备行、告警、功能与缓存）。所以两个创建入口在确认 SN 无在用设备后，先调 `ProtocolPointCopyUtil#clearBelongPoints` 清掉同 SN 的物模型与协议点位，再做产品继承与复制；否则同 SN 重建会继承上一轮遗留行、复制再叠一层。
- 设备详情「全部保存」保留**已落库行**的 `belong_type`/`from_type`，只有新加的行取 DTO 默认值（`0`）。产品继承到设备的属性必须继续是 `belong_type=1`；一旦被改写成 0，设备就多出一份"设备自定义"副本，复制设备时会再撞一次。
- 复制时去重：`copyProperties` 按 `identifier` 对目标已有属性去重；`copyConfigTable` 按 `code + belong_type` 对**源列表**去重（源设备自己带重复行时不再传播给新设备）。
- `labdatahub_properties` **没有** `(belong_sn, identifier)` 唯一约束，协议点位表也没有 `(belong_sn, code, belong_type)` 唯一约束，重复只能靠代码保证。排查：
  `SELECT belong_sn, identifier, count(*) FROM labdatahub_properties GROUP BY 1,2 HAVING count(*)>1;`
  各协议点位表同理按 `belong_sn, code, belong_type` 分组。一次物模型「全部保存」是 `remove by belongSn` + 全量重插。

### 网络组件不允许重复端点

`LabdatahubComponentController#add/edit` 最前面调 `checkEndpointNotUsed`：同 IP + 同端口即拒（**不限 netType**，TCP/UDP 同端口也拦；数据库类组件再加库名比较，同服务器同端口不同库放行），报错点名占用方组件与启用状态。同 IP 不同端口不算重复。没有 `ipAddr` 的组件（各类 `*_SERVER`、MQTT broker/client）不参与；编辑排除自己；停用的组件也算占用。**一台设备一个网络组件**，改设备网络组件时先查有没有 `device_cnt=0` 的孤儿组件。

### 协议包缺失时的「半启动」

`openComponent` 在 switch 之前 `initProtocol(protocolId)`：协议记录不存在 / jar 文件不存在 / 包解析失败都直接抛 `CommonWarnException`，什么都不启动（`closeComponent` 刻意不校验，协议缺包也要能关）。`initProtocol` 对已加载的协议幂等短路，并用静态锁 `PROTOCOL_LOAD_LOCK` 防止并发重复加载（锁内只放 `loadProtocol`，别绕过 `initProtocol` 直调）。

`ProtocolManager` 的五个容器（`DECODE_METHOD`/`ENCODE_METHOD`/`CLASS_INSTANCE`/`PROTOCOL_MAP`/`CLASSLOADER_MAP`）都是 `ConcurrentHashMap` —— 消费线程每条消息都在读，启动/停止组件与上传协议在写。别改回 `HashMap`。

### 日志文件

- `logs/` 下 `sys-info.log` / `sys-error.log` / `sys-user.log` 按天轮转，保留 30 天（`logback-spring.xml` 三处 `maxHistory`）。`file_info` 用 `ThresholdFilter(INFO)`（WARN 起全收，ERROR 会同时进 `sys-error.log`），四个 appender 均固定 UTF-8
- `api_run.log` / `api_err.log` 是启动命令的 shell 重定向产物，**每次启动截断、不受轮转管理**；清理必须停进程。组件层存量 `System.out/err` 按平台默认 GBK 写，所以 `api_run.log` 可能是混编，别整文件按一种编码解
- moquette 每转发一条消息一行的 INFO 已用 `<logger name="io.moquette.broker.PostOffice" level="warn"/>` 压掉

### TimescaleDB

- `labdatahub_device_logs` 是超表：`create_time` 时间分区（1 天/chunk）+ `device_sn` 空间分区。**`UPDATE ... SET device_sn` 会被分区约束拒绝**；改设备 SN 只能删日志重新累积或重建超表
- 保留策略在 `application-pgsql.yml`：`retentionInterval: 7 days`、`compressionInterval: 1 day`（压缩作用在保留窗口内，窗口必须小于保留窗口）。由 `TimescaleDbInitializer` 启动时下发
- **改 yml 改不动已有 job**（`add_*_policy` 带 `if_not_exists => TRUE`），必须 `alter_job(<jobId>, config => '...')`；`config` 是整体替换，`hypertable_id` 要一起写回。缩短保留期不可逆
- 查 job 用 `proc_name`（`policy_retention`/`policy_compression`）或 `job_id`，别拿 `application_name` 过滤（那是显示名）。本版本 `timescaledb_information.jobs` 没有 `hypertable_id` 列
- 连续聚合视图（`device_logs_hourly_mat` / `device_logs_daily_mat`）需建成；`GROUP BY` 必须用别名 `bucket`，不是函数名 `time_bucket`。日志参数末位传异常对象本身，别传 `e.getMessage()`（SLF4J 不打印堆栈）
- 启动时 Druid 对 TimescaleDB 语句刷的 `ParserException` 段属**已知噪音**，语句照常执行

### 设备日志容量

- 生产策略 compress 1 day / drop 7 days；冗余索引 `idx_device_logs_report_time` / `idx_device_sn` 已删（`others/device_logs_drop_redundant_index.sql`；超表不支持 `DROP INDEX CONCURRENTLY`，挑低峰）
- PG 内存参数：生产 `shared_buffers` 8GB、`effective_cache_size` 24GB（64GB 机器）；离线包 `C:\postgre` 同步这两个值。业务账号不是 superuser，改 `postgresql.conf` 后重启服务
- 增删索引/策略脚本：`sql/pgsql/others/device_logs_*.sql`；结构改动必须同时落 `init.sql`

### MyBatis-Plus 条件包装器

`queryWrapper.ge(condition, col, LocalDateTime.parse(startTime))` 在条件为 false 时**也会先求值**，`startTime=null` 抛 NPE（message 是 `text`），页面只看到 `Error: text`。凡条件成立才算的实参里有调用（解析/查库/建对象），一律提到显式 `if` 里。当前 `LabdatahubPointWriteRecordController#list`、`LabdatahubDeviceLogsController#list` 已按此写法。

### 100 台设备容量加固

- 两段式启动：顺序注册协议（反射加载有共享状态）→ `threadPoolTaskExecutor` 并发开组件；`openComponentSafely` 全部兜住异常，失败置 `status=0` 不中断整轮
- `LogProcessor` 批量落库：队列 10000、批 1000、最多等 200ms、满则丢弃并按分钟汇总；开关在 Redis `sys_config:device.log.batch`，格式 `"true,batchSize,maxQueueSize"`，`TimerTask#updateRegisterSwitch` 每 10 秒下发
- 连接池 `maxActive: 50`（`application-pgsql.yml`）
- `ParallelHealthCheck` 按协议配线程数（`PROTOCOL_THREADS`：FANUC 80，其余 16），实例按协议各建一个；`inflight` 防上一轮未跑完叠下一轮，但超过 3× 超时强制开新一轮

## 编码规范

1. 所有 class 字段必须加中文注释
2. 列表循环尽量用增强 for（`for (Item item : list)`），少用下标循环
3. 接口返回字段使用现有 `AjaxResult` / 分页 `TableDataInfo` 约定
4. 时间一律本地时间：`LocalDate` / `LocalDateTime` / `LocalTime.now()`，不用 `Instant.now()`、`ZonedDateTime.now(ZoneOffset.UTC)`；库里 datetime 也按本地时间写
