# UltraAntiCheat

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.13%2B-green.svg)]()
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)]()

> 这是一个全面的反作弊

## 特性

- **13个核心检测模块**：飞行、速度、变速、击杀光环、攻击距离、自动连点、搭路、穿墙、反击退、无减速、异常数据包、地面伪造、透视
- **GCD旋转分析**：通过最大公约数算法检测KillAura的机械旋转模式
- **置信度评分系统**：每次违规都有置信度，低置信度只告警不惩罚，大幅降低误判
- **多反作弊桥接**：自动检测并集成 GrimAC、NoCheatPlus、Vulcan、Matrix、Spartan
- **Web 监控仪表盘**：浏览器实时监控服务器反作弊状态
- **完全开源**：基于 GPL-3.0 协议

## 检测原理

### 击杀光环 (KillAura) 检测

```
GCD一致性分析 + 旋转模式检测 + 攻击频率统计
→ 机械旋转的GCD高度一致 → 标记KillAura
→ 水平旋转时俯仰角不变 → 标记AimAssist
→ 攻击频率超出人类极限 → 标记AutoClicker
```

### 攻击距离 (Reach) 检测

```
眼睛位置 → 射线投射 → 目标碰撞箱精确距离
→ 考虑Ping补偿 → 超过3.15格判定违规
```

### 飞行/速度检测

```
物理模拟：重力0.08/tick、摩擦系数、药水效果
→ 实际移动 vs 理论最大值
→ 超出阈值 + 置信度评分
```

## 安装

### 前置要求

- Spigot / Paper 1.13+
- Java 17+
- ProtocolLib (可选但强烈建议)

### 构建

```bash
git clone https://github.com/yourname/UltraAntiCheat.git
cd UltraAntiCheat
mvn clean package
```

构建产物位于 `target/UltraAntiCheat-1.0.0.jar`

### 安装

1. 将 `UltraAntiCheat-1.0.0.jar` 放入 `plugins/` 目录
2. （可选）安装 GrimAC / NoCheatPlus / Vulcan / Matrix / Spartan 增强检测
3. 重启服务器

## 命令

| 命令                | 说明       |
| ----------------- | -------- |
| `/uac alerts`     | 切换告警开关   |
| `/uac info <玩家>`  | 查看玩家检测信息 |
| `/uac status`     | 查看系统状态   |
| `/uac reset <玩家>` | 重置违规记录   |
| `/uac reload`     | 重载配置     |

## 配置

`plugins/UltraAntiCheat/config.yml`:

```yaml
# 置信度阈值 (0.0-1.0)
confidence-threshold: 0.5

# 惩罚配置
punishment:
  enabled: true
  threshold: 5.0
  command: "kick %player% &c作弊检测: %check%"

# Web仪表盘
web-dashboard:
  enabled: false
  port: 25586
```

## 架构

```
UltraAntiCheat/
├── api/              # 公开API
├── checks/           # 检测模块
│   ├── movement/     # 移动检测
│   ├── combat/       # 战斗检测
│   ├── block/        # 方块检测
│   └── packet/       # 数据包检测
├── integration/      # 反作弊桥接
├── listener/         # 事件监听
├── manager/          # 管理器
├── player/           # 玩家数据
├── util/             # 工具类
└── web/              # Web仪表盘
```

## 检测能力覆盖

| 作弊类型        | 检测模块             | 检测方式             |
| ----------- | ---------------- | ---------------- |
| Fly         | FlyCheck         | 物理模拟 + 空中速度分析    |
| Speed       | SpeedCheck       | 速度阈值 + 药水补偿      |
| Timer       | TimerCheck       | 数据包频率统计          |
| KillAura    | KillAuraCheck    | GCD分析 + 旋转模式     |
| Reach       | ReachCheck       | 碰撞箱精确距离 + Ping补偿 |
| AutoClicker | AutoClickerCheck | CPS统计 + 间隔一致性    |
| Scaffold    | ScaffoldCheck    | 俯仰角 + 旋转分析       |
| Phase       | PhaseCheck       | 方块碰撞检测           |
| Velocity    | VelocityCheck    | 击退比率分析           |
| BadPackets  | BadPacketsCheck  | 数据包频率限制          |
| GroundSpoof | GroundSpoofCheck | 地面状态一致性          |
| Xray        | XrayCheck        | 遮挡矿石挖掘 + 探索分析  |

## 开源协议

[GPL-3.0](LICENSE)
