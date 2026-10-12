# 先驱者首批接入交接（dev.17）

## 范围与基线

- 用户授权先驱者首批五项，并要求原生半血机制可接入；死亡激光平衡以基础＋百分比封顶的最高 10 为参考。
- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`；分支 `planning/scylla-close-harbinger`。
- 代码起点 `ab6782d`；交付标签 `v0.1.0-dev.17`。
- 固定 Core：`9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），相邻独立构建副本；受版本控制文件保持干净。
- NeoForge 1.21.1，JDK 21，Ars 5.13.1，Cataclysm 3.33；本附属暂无 Forge 版本。
- 不改 Core、不新增跨附属硬依赖、不推远程。

## 已完成

- `HarbingerCasting`：四项原生 Projectile 载荷；原生运动和 Ars 轨迹分开，原生装饰 tick 保留。
- `HarbingerLaser` 与 opt-in mixins：原生死亡激光、发射位置与朝向同步、逐输出秒结算、空蓝/自然结束清理、半血附加点燃。
- 原生伤害经 Core NativeSpellDamage，烟云继承上下文，直接命中与爆炸/云使用统一友军判定。
- 保留原生直接击杀回复；普通本体弹体不接入 Ars 加成。
- 五项真实 Scribe 配方，共七个凋灵合金锭＋五颗魔源宝石；排序按先驱者系列分组。图标仅运行时委托两把原生武器的 renderer。
- 默认费用 40/90/120/200；死亡激光每输出秒 120，三秒 360。默认主动冷却 1/3/4/8/10 秒。
- 原生半血条件 `health <= maxHealth/2` 在光束生成时取根施法者快照。半血齐射/冲锋动作未接入，也不凭此强化单发载荷。
- 默认不破玻璃、不重置 EMP、不开地形火；可独立开启地形火。普通 Boss 行为不改变。

## 验证证据

- 工作树运行 `./gradlew.bat :runGameTestServer :build --offline --console=plain --no-daemon`。
- 最终记录：忽略文件 `build-harbinger-release.log`；45 项检查通过，35 个魔符中英文键验证通过；两种 JAR 的 verifyDistributions 通过。
- 四项新集成检查覆盖：普通/环绕载荷、原生速度、重复扣费与空蓝结束、续接、原生直接伤害与击杀回复、普通实体隔离、烟云伤害/归属/友军/死亡清理、半血光束、百分比封顶、逐秒付费、创造免费、执行实体发射姿态、自然结束。
- 测试百分比封顶时临时提高测试光束的 hp 系数，避免 Minecraft 默认生命属性上限影响；另断言正式系数从原生配置读取。
- 初轮发现重复载荷清理时 resolving 标志阻止 discard，已修正；两项测试设置问题（生命上限截断、复用生存模式的资源快照）已纠正。
- 基础独立环境，无可选 Ars ’n Spells。原有桥接测试缺少可选依赖时按设计跳过；没有宣称完整共存矩阵验证。
- 客户端图标/粒子/拖尾/第一人称画面未人工验证。NEG 特定形式复用 Core 现有 carrier 规则，本轮未另启动其客户端。

## 产物与部署

- 二进制：`build/libs/cataclysm-nouveau-0.1.0-dev.17-mc1.21.1-neoforge.jar`
- SHA-256：`fff9407b5dba0d3c296fcdb32eff3216e64345c5fbb2ab436793c28602739f8d`
- 源码：`build/libs/cataclysm-nouveau-0.1.0-dev.17-sources.jar`
- SHA-256：`44edf849c012d8d6ae0be4794d4d0b3e8e962b963d8de958abdcfae85c7c8ba0`
- 许可：本附属 MIT；发行包与源码包均不含灾变素材、源码实现或依赖 JAR。
- 协调者部署位置：`C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，旧版移到该实例 `mod-backups/2026-10-12-harbinger-dev17`；Core 保持原安装版本。
- 本次 Gradle/GameTest 的进程已自然结束；未终止其他任务的 Java 进程。

## 后续

先在客户端验证普通发射、溅射/环绕/回响、激光朝向和第一人称表现；以实际运行结果补检查，不重跑无关完整矩阵。其他先驱者能力仍是候选，没有自动授权。
