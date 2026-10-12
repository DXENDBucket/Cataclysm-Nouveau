# 焰魔最后一批 dev.31

- 工作树：`parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`。
- 固定Core：`9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件干净；本次没有修改Core。
- NeoForge1.21.1，Cataclysm3.33依赖SHA-256：`371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 五项新效果：五火球阵650／8秒，地动三连600／12秒，强化跃砸850／15秒，终极焰斩900／20秒，烈焰打击280／6秒；均可配置，旧五项费用与冷却不变。
- 原生行为审计、角色、子载荷与客户端边界见 [ignis-abilities.md](ignis-abilities.md)；生命比例封顶沿用dev.30，新增烈焰也支持开关。
- 全套十项共八整锭＋两碎片，一锭拆九片，九锭覆盖整套，三次焰魔固定掉落足够。资源只有运行时路径引用，无本体素材或源码复制。
- 验证：`build-ignis-final-dev31-release.log` 中 **All75requiredtestspassed**、**BUILD SUCCESSFUL**；实际运行MixinExtras0.5.3，无注入／版本冲突。六项新GameTest覆盖五火球数量／延迟／组收费／根清理、三次重砸、普通／灵魂强化落地六发子弹、预备火环＋八道前方火焰、主体时效／脉冲／烙印／自然消退、Boss半径2结束爆炸及未绑定玩家半径1的原生差异。
- 初次测试编译失败是Mana double变量误声明float；第二次仅手动tick的烈焰测试漏推进tickCount，修正测试后全部通过。最终服务端输出可检查，未以编译成功冒充GameTest通过。
- 二进制：`build/libs/cataclysm-nouveau-0.1.0-dev.31-mc1.21.1-neoforge.jar`，SHA-256 `70538feb8f272d1566ccaef8b22ea9dea66a2867f2c6c0cb4ca96080d5f07ddd`。
- 源码：`build/libs/cataclysm-nouveau-0.1.0-dev.31-sources.jar`，SHA-256 `4a7b7c6da6960b7b4e76a7e80b290bd89c8764420edbc5b6a6de69a652a4cda3`。两包verifyDistributions均通过。
- 协调者串行部署至“万法归一”，旧dev.30存入实例 `mod-backups/2026-10-12-ignis-final-dev31`；没有修改其他modJAR或停止用户客户端。
- 未启动客户端人工验证画面／手感。本次测试JVM已结束；没有推送或发布。焰魔在用户当前授权下到十项收束，不继续增加普通体术。
