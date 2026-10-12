# 末影守卫第二批动作交接（dev.28）

## 范围

- 用户授权上勾拳连段、闪现突袭、空降重砸、火箭拳，明确不做震退爆发与转阶段震爆。
- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`；分支 `planning/scylla-close-harbinger`；父提交 `7f74db2`；标签 `v0.1.0-dev.28`。
- 固定Core副本：相邻Ars-Conflux，SHA `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19）；不修改受版本控制文件。此任务不新增附属间依赖。
- NeoForge1.21.1／Cataclysm3.33／JDK21；本体JAR SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。

## 实现

- `GuardianActions`调度原生UppercutAndBulletGoal/RageUppercut、HugmeGoal、TeleportStrikeGoal、RocketPunchGoal及aiStep，不注册临时Boss到世界。
- 上勾26tick承接真实冲量，27tick原生挑飞/眩晕/禁盾，54tick全九枚准备炮弹；半血84tick追加原生重击和70符文扇形阵。费用420、冷却200tick。
- 闪现保留原生移动预测，20tick安全落地传送、38tick原生打击。费用180、冷却120tick。
- 空降40tick真正执行者移到敌方上方4格，半血8格；超过48tick且真实onGround才通过原生goal切AIR_STRIKE2，第3tick原生范围冲击及64/80/96枚放射符文。费用450、冷却240tick。超时直接清理，无伪造落地攻击。
- 火箭拳24tick原生距离×0.3水平冲量，28tick原生重拳/击退/禁盾。费用160、冷却100tick。
- 对友军/自身执行时搜索执行者视线敌人；选中敌人由根施法者执行。闪现/空降需要实体敌人，另外两项可朝方块点/朝向。默认目标32格，可配置。
- 传送由实际执行者触发EnderTeleport取消事件、体积/世界边界/无液体/已加载区域检查；小范围地面搜索。原生Boss不改。动作占用时暂停生物AI；与现有肩撞/钩锚等运动互斥，根/执行者离开或死亡清理，空降恢复重力状态。
- `SpellGuardian.ability`让符文/炮弹子攻击继承正确来源、真实所有者和同一增强值。子攻击生命周期由Core会话清理，只有母动作一次扣费，不伪造Ars续接。MassDestruction也替换为真实伤害来源；挑起速度同步客户端。
- AnimationHandler只对未注册调用帧内部切状态，不广播不存在的Boss id。粒子/声音来自原模组/原版运行时路径，服务器发送预备/打击视觉；不复制素材/实现。
- 四项真实抄写配方各需虚空残片＋魔源宝石，27经验。守卫八项合计七残片＋一傀儡核心，护手拆九枚仍余两枚。原有类别内保持Boss系列相邻。

## 验证

- 收拢后一次 `:runGameTestServer :build --offline --console=plain --no-daemon`，日志 `build-guardian-actions-dev28.log` 明确 `All 61 required tests passed`、`BUILD SUCCESSFUL`。
- 五项新增测试覆盖真实冲量/传送，九发子弹与来源/强化/一次费用，半血70符文，眩晕/挑起/友军过滤，真实落地触发64/80/96符文，根来源，重力恢复，中断/运动冲突退款，真实传送事件取消，堵塞空间不扣费。
- 47魔符双语描述检查、binary/sources verifyDistributions通过。资源JSON解析、模型父路径、获取数量检查通过。
- Core tracked干净。未人工启动客户端；动作外观、手感和混装客户端表现需要用户重启确认，不用61服务端检查代替画面验证。

## 交付

- 协调者串行部署万法归一mods；dev.27备份 `mod-backups/2026-10-12-guardian-actions-dev28`。共享Core、配置及用户游戏进程不改；模组新配置值由加载器正常补全，重启生效。
- 本地提交/标签，无远程推送，无Forge交付；后续制作遵守本批明确不做的动作边界。
- binary：`cataclysm-nouveau-0.1.0-dev.28-mc1.21.1-neoforge.jar`，SHA-256 `1256f68f245892c5cdfee771f629d17c7923ddb841a5d40525b9f44d2b0570f6`。
- sources：`cataclysm-nouveau-0.1.0-dev.28-sources.jar`，SHA-256 `7f047ca61d0a62be056b4f6e6c7b68676b0a044a18eaa78cde78ffca9da56557`。
