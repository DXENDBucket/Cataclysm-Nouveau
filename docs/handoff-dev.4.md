# dev.4 交接

- 目标：Minecraft 1.21.1 NeoForge，Cataclysm 3.33；版本 0.1.0-dev.4。
- 分支：`agent/cataclysm-anchor-wave`，完成后快进 main；发行提交见 `v0.1.0-dev.4`。
- 固定 Core：`3eccd65fc569cd43ef960ef3eee77963ef201ac8`（API 0.1.15），受版本控制文件无变化。
- 隔离工作区：`parallel-workspaces/cataclysm-anchor-wave/`。
- 新增雷霆战锚和海浪弹射物替换、配置化冷却、海浪延长时效。默认战锚 17 伤害／60 Mana／2 秒冷却；海浪 9 伤害／4 秒／90 Mana／5 秒冷却。
- 验证：Java 21，`./gradlew.bat :runGameTestServer :verifyDistributions --offline --no-daemon --console=plain`；11 项服务端测试全部通过，7 魔符双语检查通过。
- 随后收紧无载荷的新增修饰判定，执行 `:verifyDistributions` 完成最终编译和二进制／源码包检查；没有再次启动完整服务端测试。
- 日志：工作区 `build-dev4-verification.log`、`build-dev4-package.log`（忽略文件）。
- 二进制：`build/libs/cataclysm-nouveau-0.1.0-dev.4-mc1.21.1-neoforge.jar`，SHA-256 `7f92dddbdb514eb20ffbef8244723f6ab48b5b4c03de3044e8f4f86a0f6c5e8e`。
- 源码：`build/libs/cataclysm-nouveau-0.1.0-dev.4-sources.jar`，SHA-256 `5d21d68d00806a99ad79f958b6db2a90420e2aa565d382587c3a9ff84cdb6dc0`。
- 协调者部署：`C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，旧 dev.3 移到实例 `.codex-backups/cataclysm-nouveau/`；需要重启。
- 平衡记录：`MinecraftDev/Ars-Balance-Baselines/reports/2026-10-12-cataclysm-dev4.md`。
- 未验证：客户端锁链、方块粒子事件、海浪消退/朝向、与各可选附属在客户端的组合，以及带装备的实际跨系统法强增长。
- 原生方法与实际发布 JAR 核对；仅调用实体和资源路径，未复制灾变方法体、素材或打包依赖。
- 无 Forge 版本、无远程推送；本轮测试 JVM 已正常退出，其他任务的 Java 进程未处理。
