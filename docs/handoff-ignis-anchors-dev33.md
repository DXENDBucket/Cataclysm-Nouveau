# 焰魔剑舞转向与位置执行 dev.33

- 工作树：`parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`；仅 NeoForge 1.21.1。
- Core 固定 `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件干净；Cataclysm 3.33 依赖 SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 本次只迁移剑舞，魔符数量仍为57。`ActionAnchor` 为附属内共享姿态，区分实体和位置，未新增 Core API 或假的 LivingEntity executor。
- 增益实体只能是自身／友军；位置执行允许真实方块触发，敌方实体拒绝且不扣费。玩家窗口内跟随镜头，生物跟随有效敌方攻击目标，20°/tick，出手锁向。位置剑舞静止且锁向，不移动／占用根施法者，也不在结束时削减他的速度。
- 原生两段 Goal、aiStep、攻击体继续使用；服务端攻击与视觉共用 frame pose，所有权、回血、Mana/CD 和友军保护保留。客户端载荷协议2支持没有实体的固定锚点，实体和位置朝向都有短暂插值。
- `build-ignis-anchors-dev33.log`：`:runGameTestServer :build --offline --console=plain --no-daemon`，80项required全部通过，BUILD SUCCESSFUL（40s）；二进制与源码 `verifyDistributions` 均通过。本次收拢后仅运行这一轮。
- 新测试验证玩家准备转向／挥击锁向及真实伤害方向，位置协议往返／两段结束／根取消／付费／友军／根运动保持，敌方拒绝退款以及友军生物跟随自身攻击目标。原有77项同次通过。
- 二进制 `build/libs/cataclysm-nouveau-0.1.0-dev.33-mc1.21.1-neoforge.jar`，SHA-256 `2232c07940526a5600e568c5a9ff6254801eb9d7fee9c3911d267af014523463`。
- 源码 `build/libs/cataclysm-nouveau-0.1.0-dev.33-sources.jar`，SHA-256 `97711086d0c178f1ace1100a1ad0174f0656186125dc599e170d00a58ff804f6`。
- 串行部署“万法归一”，dev.32保存至实例 `mod-backups/2026-10-12-ignis-anchors-dev33`；未替换Core或其他mod。协议变化要求客户端／服务器使用同一dev.33版本。
- 未启动图形客户端。用户对dev.32视觉效果的反馈不等于dev.33位置绘制／转向体验已验收；可测试自身→剑舞在准备窗口转视角，以及弹射物→剑舞命中方块的固定幻影。需确认落点的模型／攻击对齐、优化模组共存和远处观察者。
- 测试Java已退出，未停止用户游戏或其他Java。没有推送／发布，其他动作未全量迁移，咒翼灵骸仍未实现。
