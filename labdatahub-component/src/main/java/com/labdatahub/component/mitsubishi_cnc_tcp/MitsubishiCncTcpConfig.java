//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三菱 CNC TCP（MELDAS MOCHA）连接配置（存组件表 other_config JSON）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MitsubishiCncTcpConfig {
    // IP地址
    private String ipAddr;
    // 端口，MOCHA 协议默认683
    private Integer port = 683;
    // 连接超时时间
    private Integer timeout = 3000;
    // 机床机型/NC型（MELDAS 系列，取值见前端 utils/cncTcpPoints.js 的 CNC_TCP_MODELS：
    // m70 = M70/M700 系、m80 = M80/M800 系、unknown = 未确认机型）。
    // ⚠️ 机型**不改变协议族**——同一套 MOCHA 帧吃多个机型，所以它是机型/连接参数而不是新协议；
    // 它只决定前端配置抽屉取哪张语义点位表（后端 MitsubishiCncDataReader 不按机型分支）。
    // 挂在网络组件（本类即组件表 other_config）而不是协议上：协议表是 jar 注册表、全局共享；
    // 也不是设备——改 IP 的入口在组件表单，机型与 IP 要一起改才不会漂移。
    private String model;
}
