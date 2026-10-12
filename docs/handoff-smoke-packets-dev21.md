# 烟云显示再次处理（dev.21）

- 用户反馈dev.20仍看不到烟云；dev.19的noCulling同步不是已确认的原因修复。
- 工作树parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau，分支planning/scylla-close-harbinger，起点e09ce47，标签v0.1.0-dev.21。
- 固定Core 9611f5087e3dd5adad6cd21517bb9563dd9a83dd（0.1.19），受版本控制文件未改。Minecraft1.21.1/NeoForge21.1.250/Cataclysm3.33/Java21，无Forge交付，无推送。

## 实现与范围

HarbingerSmokeParticles在原生云服务器tick结束后每两tick显式发送一批20个原版SMOKE，等待、半径、收缩和移除仍读取本体状态。批量散布使用半径的0.45倍，中心抬高0.2格，避免粒子正好卡在命中平面。附近32格玩家收到原版粒子包，不强制覆盖客户端粒子设置。榴弹、齐射子云和独立烟云都走同一原生实体Mixin；联动实例原有客户端粒子调用被抑制，避免重复，普通Cataclysm烟云不改。不增加伤害、费用或持续时间，不复制本体素材和粒子实现。

这条传输路径不再依赖客户端云实体是否被正常tick，但并未证明旧路径失败的精确原因。批量粒子包与原生圆盘逐粒子采样的分布有轻微差别。真正画面仍待用户重启后的客户端验证。

## 验证与部署

- 一次`:runGameTestServer :build --offline --console=plain --no-daemon`，build-smoke-packets-dev21.log明确记录All51requiredtestspassed，构建成功。
- 新测试捕获实际ServerPlayer连接收到的SMOKE粒子包：两种入口、等待期、发送间隔、粒子类型/数量、不强制粒子设置、抬高位置、半径随本体缩小、移除停止、普通本体隔离。既有烟云伤害/友军/归属/清理检查继续通过。
- 发行包/源码包verifyDistributions通过。无新增可选矩阵；没有人工启动客户端。本次测试Java进程自然结束，不终止用户游戏。
- 协调者部署万法归一mods，旧dev.20备份mod-backups/2026-10-12-smoke-packets-dev21。Core与配置不改。用户需要重启游戏加载新代码。

## SHA-256

- `build/libs/cataclysm-nouveau-0.1.0-dev.21-mc1.21.1-neoforge.jar`: `0cdbfb3cfc63e9d6b14dbfa5cc1eab0f796c3de156ee9f48e0febe8f16d93649`
- `build/libs/cataclysm-nouveau-0.1.0-dev.21-sources.jar`: `8cf7dc470b2b2fe936ec86b025d710f87f61199815546f374f6b5b093a5d0b81`
