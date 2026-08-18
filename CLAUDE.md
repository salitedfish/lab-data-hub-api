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

- 端口：`8081`，context-path：`/`
- 活跃配置：`spring.profiles.active: pgsql`（见 `labdatahub-admin/src/main/resources/application.yml`）
- 协议包位于 `protocol/` 目录，由 `scprotocol` 子项目产出，需先编译放入
- 轮询类协议（Modbus / S7 / Fins）的点位解析参数来自物模型 `labdatahub_properties` 的 `dataType`/`byteOrder`/`isSigned`/`scale`/`offset`（`identifier` 与协议 `code` 一一对应），由 `business.utils.ParseMetaUtils` 在构建 `XxxReadConfig` 时注入，随 decode JSONObject 传给协议库解析
- Redis 配置在 `application.yml` 的 `spring.redis`

## 编码规范

1. **AI 修改/创建的 Java 文件**，文件开头添加注释 `//由AI修改`；XML、YAML、properties 等非 Java 文件不加。
2. **所有 class 字段必须加中文注释**。
3. 列表循环尽量用增强 for（`for (Item item : list)`），少用下标循环。
4. 接口返回字段使用现有 `AjaxResult` / 分页 `TableDataInfo` 约定（RuoYi 体系）。
