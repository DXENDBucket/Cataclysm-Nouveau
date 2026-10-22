# dev.61：第一批横向平衡调整

日期：2026-10-23。基于 MinecraftDev/Ars-Balance-Baselines/reports/2026-10-23-cataclysm-dev60-overall-audit.md 的本地审计。

| 能力 | 原默认额外 Mana | 新默认额外 Mana | 主动冷却 |
| --- | ---: | ---: | ---: |
| 燃光弹 | 300 | 180 | 8 秒 |
| 骨骸风暴 | 240 | 140 | 8 秒 |
| 沙碑阵 | 600 | 400 | 16 秒 |
| 雷暴云 | 900 | 650 | 20 秒 |

这四项的子攻击分散于较大区域，不能把总弹体数量直接当作单目标命中数。费用调整保留原生伤害、运动、范围、生命周期和冷却，也保留每组一次收费及 Ars 本身的编排费用。存量服务器已经填写的 mana 配置不会被新默认值覆盖。

横向比较计入 Iron 的法术等级与原生法强、Goety 的聚晶附魔与原生属性成长，以及灾变的 Ars 伤害成长。持续激光按实际伤害窗口及无敌帧估算，不按每 tick 尝试次数直接累加。死亡激光和虚空散射箭本轮维持现值，后续若要改动，应先取得真实命中与持续输出样本。

## 构建与验证

固定 Core：ed44ebb86bd087f3546a1bc6a315d2692f70262c（0.1.28）；目标 NeoForge 1.21.1，灾变 3.33。没有修改 Core，也不增加平台支持。

仅四个常量改变；同步修正两个已有服务端检查中三处硬编码费用预期。已通过 `:compileGameTestJava :jar :sourcesJar :verifyDistributions --offline`；102 项双语魔符键、二进制与源码发行边界通过。静态核对确认 102 项中仅上述四项改价，冷却配置源文件未变。本轮不重复整套战斗测试，未进行客户端实战复测。

构建日志：隔离工作树 `build-balance-dev61.log`。JAR SHA-256：

- `cataclysm-nouveau-0.1.0-dev.61-mc1.21.1-neoforge.jar`：`81bd0d656a974e9d89290087c5dbb54c9e70380ca271e5c89efdaa39591218b4`
- `cataclysm-nouveau-0.1.0-dev.61-sources.jar`：`dd95a9b45ecb92e0469a9ee554cb0b93f5d0167c72fdfde4c1b10ae00cf007d8`
