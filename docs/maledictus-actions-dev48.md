# 咒翼灵骸完整动作 dev.48

## 范围与基线

用户授权强化飞行重砸、空中四箭齐射、连环突进、地面冲击波、上挑及近战连段；抓取暂不制作。新增六个 Tier 1 效果魔符，仍按 MALEDICTUS 系列排列。本体仅运行时引用，没有移植其实现或素材。

- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cross-audit-cataclysm-nouveau/Cataclysm-Nouveau`，分支 `feat/maledictus-actions`。
- 固定 Core：`08c307f3238b9b9aa7fd628b9771cd287160448d` / 0.1.24，相邻独立副本；受版本控制文件无变化，无新 Core API。
- MC1.21.1、NeoForge21.1.250、Java21、Ars5.13.1、Cataclysm3.33。Cataclysm JAR SHA-256：`371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。本附属没有 Forge 版本。

## 最终内容

| ID / 魔符 | 默认额外 Mana | 主动冷却 | 原生内容 |
| --- | ---: | ---: | --- |
| maledictus_aerial_smash / 咒翼飞行重砸 |600|5秒|24→25，腾空、俯冲、实际碰撞落地，两次砸击；100螺旋＋30/48/68环形候选|
| phantom_aerial_volley / 空中幽箭齐射 |180|5秒|3→4→5，8tick升空、20tick悬停、50tick四箭、60tick解除飞行，落地收势|
| maledictus_rush / 咒翼连环突进 |220|4秒|10后撤；满血11一段，半血12→15两段，四分之一血12→16→17三段|
| maledictus_shockwave / 咒翼地面冲击 |160|5秒|9落地动作：4tick范围砸击，8/10/12/14/16tick半径2–6逐圈冲击，原生向前偏移2.5|
| maledictus_uppercut / 咒翼双上挑 |140|4秒|22，23/33tick两次上挑，首击挑飞、原生120tick破盾|
| maledictus_melee_combo / 咒翼近战连段 |200|4秒|19的12/24tick两击；第二击成功才转21追加两击，否则转20收势|

每份强化额外10Mana，Ars形态及其他魔符费用另算；完整动作只付一次，子箭、子戟、地面碎块无额外收费、无重复后续编排。后续 Ars 效果保持启动时立即继续。冷却沿用Conflux主动释放开始计时，原生Boss通常在结束后设置冷却，两者起点不同。近战连段独立冷却80tick为本附属平衡值；地面冲击采用飞行攻击100tick参考值。并未宣称所有动作具有独立原生费用或冷却。

## 原生数值与行为

调用本体注册 Goal.start/tick/stop 与 aiStep，不复制攻击、布阵或发射算法。`NativeInternalGoal`读取实际状态和持续时间，替代匿名内部类序号匹配。独立入口显式进入初始状态：InternalStateGoal.start假设状态此前已进入，不能用它独立进入9。

近战继续取 `13 × attackMultiplier × (1 + 0.20 × 怒气)`，保留各击的原生倍率与原生封顶最大生命比例项。成功近战增层，五层成功命中刷新衰减；地面逐圈伤害原生并不增加怒气，不能额外刷新。上挑、rush、近战连段的普通mobAttack和Maledictio来源都重绑真实damageOwner。增强/削弱仍是每击原生base的20%，限定2–10，再由Core施加Ars成长一次。

箭仍用原生四发、9°间隔、速度1.5和原生随机散布；本体条件追踪、拖尾、命中处理保留。基准5、每怒气+5%。四发仍是原生同一Goal发射，不能套一个通用投射公式省掉原生偏移和行为。实体射出位置按实际执行者眼高校准，位置幻影用1.62眼高；禁止拾取，实际owner和Ars上下文继承施法根。

强化砸击的25阶段保留4/37tick两次范围打击、39/40/42/43tick环阵。半血/四分之一血判断沿用共享调用帧的逻辑施法者生命，不改成付款对象以外的随意实体。普通Boss怒气、攻击和terrain行为不受影响；联动调用帧不毁地形，地面波的原生可视碎块保留。

## 共用锚点、移动和清理

`MaledictusCombatActions`一套驱动处理自身、友军与位置；非友军实体拒绝且不收费、不回退施法者。实体始终采样实际执行者视角／AI目标，用ActionGeometry共享转向；native goal不能另外lookAt改变冲量朝向。保留本体锁定朝向的时间窗口。

实体执行把原生冲量与飞行标记作用到实际actor，落地读取真实碰撞；退出恢复原有noGravity，清理行动锁并收敛水平速度。位置执行采用不加入世界的调用帧、普通碰撞和简化重力／阻尼来驱动幻影运动，攻击仍由同一个原生Goal执行；不是生成一个真实Boss。此位置运动不宣称与Boss完整travel逐帧相同。

升空阶段达到超时而未实际落地时取消，不强制播放落地伤害。只在已加载区域执行，260tick总上限。身体行动可被同actor的新行动替换；位置行动不占付款者身体。怒气、资源、damageOwner和executor角色独立保留。

子戟保持现有120tick上限；齐射箭240tick、可视碎块80tick。正常动作结束允许子攻击按自身原生时序继续；被打断仅取消该动作的子攻击，不取消别的阵列／区域。所有子攻击都继续受根失效、执行者失效／失去友军关系、所在区块卸载和能力禁用约束。

## 显示与获取

全部扩展现有咒翼Boss透明幻影与原生AnimationState，沿用第一人称0.10、常规0.28与开关。原生环粒子、砸地块粒子、声音、屏幕震动，以及原生箭和戟的renderer保留；位置／实体共用传输。砸地碎粒采用服务端批量分布。原生AfterImage粒子依赖真实Boss实体ID，未伪造不存在的ID；此版移动尾迹使用原生翼焰和连续Boss幻影，并非完全相同的额外残像模型。客户端画面尚未人工确认。

新咒魂碎片：咒魂锭1→碎片9，可九合一还原。飞行重砸用一锭，其余五个新动作各一碎片；另需魔源宝石1及27经验。旧七个配方不改。全系列13魔符共8锭＋5碎片，实际拆解需9锭，本体每次保底3锭，因此三次保底击杀学齐。图标仍运行时引用诅咒弓／断魂战戟；碎片引用咒魂锭纹理。

## 验证证据

最终命令（工作树根目录，Java21）：

```text
./gradlew.bat :runGameTestServer :jar :sourcesJar :verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_maledictus_combat,cataclysm_nouveau_maledictus_formations --offline --console=plain
```

日志：`build/maledictus-actions-dev48-final.log`，15项required全部通过、BUILD SUCCESSFUL。包含七项新动作检查与八项上轮回归：真实位置升空和碰撞落地、130原生候选、一份费用、箭数量/原生伤害/速度及散布/owner/拾取限制/根清理、满血/半血/四分之一血突进分支、上挑与连段逐击数值/命中分支/怒气/友军过滤、实际地面远处伤害及碎块清理、非友军失败不收费、实际友军飞行冲量与打断恢复、真实配方，以及全部身体幻影状态编解码。

首次编译修正final调用帧到Mixin接口的Object中转。首次启动修正普通接口误放Mixin包导致类加载拒绝，随后补新测试namespace的empty模板。第一轮战斗14/15通过，冲击波失败；排查时曾怀疑区块激活/夹具Mana，等待激活及重设Mana均未解决，包追踪发现根本未进入状态9。明确进入初始状态后解决；保留正常区块激活等待和费用/实际动画包断言。没有改伤害或几何去适应夹具。最终收拢共享朝向修复和逐击数值断言后15/15通过，不继续重复矩阵。

双语70个魔符键及发行/源码包边界检查通过。没有启动客户端；不将服务器包断言当作视觉验收。固定Core受版本控制文件干净，未修改其他附属。服务端已自然退出，仅保留Gradle daemon。

## 交付

版本0.1.0-dev.48，代码提交见本地tag `v0.1.0-dev.48`。

- `cataclysm-nouveau-0.1.0-dev.48-mc1.21.1-neoforge.jar`：`ce2272b148f7e9509069ff4c4ac64a8360e1058d76c68d2c4ff6a0abc9ab2f5e`
- `cataclysm-nouveau-0.1.0-dev.48-sources.jar`：`fce2194e0e35e016d149ca7417f2ce67aa1e5a657ae04427b89760ed83403afc`

串行部署记录在下方补充；共享实例为万法归一，万途归零不是本任务部署位置。

最终仅将后撤准备期的客户端武器显示校正为原生双锤（随后突进才持戟）；`build/maledictus-actions-dev48-package.log`重编译、双语键和发行边界通过，不重复战斗矩阵。


## 串行部署记录

代码提交：`52c1d617f269fe1c90084cdf9f89dcea2d942288`，本地tag `v0.1.0-dev.48`，已快进合入main。协调者已部署到 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，仅一个dev.48附属JAR，复制后SHA-256与上方一致。旧dev.47及原全局配置保存在 `codex-backups/cataclysm-dev48`。

全局 `config/cataclysm_nouveau-server.toml`仅补六项Mana与六项主动冷却；原配置值全部保留，幻影戟30/0、虚空符文20/0再次核对。其他mods名称、长度与时间戳快照一致，Core和其他附属JAR未替换。此记录不代表已有独立服务器世界配置已更新。没有启动客户端进行画面验收。
