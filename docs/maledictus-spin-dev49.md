# 咒翼双段旋斩 dev.49

## 范围与基线

2026-10-19，用户授权接入双段旋斩。仅新增一个 Tier 1 效果魔符 `glyph_maledictus_spin_slashes`，中文名「咒翼双段旋斩」，归入既有 MALEDICTUS 系列。未接入普通横扫、地面齐射、抓取或武器专属行为。

- 附属基线：`52af01bfd37d0ad67a018c7e46ffb85aa9861b07` / dev.48。
- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-spin-dev49/Cataclysm-Nouveau`，分支 `feat/maledictus-spin`。
- 固定 Core：`ed44ebb86bd087f3546a1bc6a315d2692f70262c` / 0.1.28；相邻独立工作树的受版本控制文件只读，未改 Core。没有新增 Core API 或提升附属的最低前置范围。
- MC 1.21.1 / NeoForge 21.1.250 / Java 21 / Ars 5.13.1 / Cataclysm 3.33。灾变 JAR SHA-256：`371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。

## 原生行为与接入

实际发布 JAR 的状态18使用 `MaledictusSpinSlashes`，持续68 tick；直接调用原生 Goal.start/tick/stop 和 aiStep／AreaAttack，没有复制前冲、攻击或模型实现。

- 15、29 tick 前冲，原生按目标距离在2.5–6.5范围计算冲量；20、34 tick 两次220度近战。第二击原生破盾160 tick。
- 共享 ActionGeometry 只在原生允许的 `<15` 与 `>23 && <29` 窗口采样执行者朝向，打击期锁定。玩家看实际执行者视角，生物沿用已有目标判定；没有另写友军方向分支。
- 每击沿用 `DMG = 13 × attackMultiplier × (1 + 0.20 × 怒气)`，再加原生 `min(DMG, 目标最大生命 × HpDamage)`。成功伤害增长共享施法者怒气，后续挥击读取新层数；盾挡、失败和友军不会凭空增长怒气。
- 增强／削弱沿用已有受限20%公式和 Ars 成长入口，每击仅应用一次。真实 damageOwner 保留，未给本体 Boss 的普通动作添加联动规则。
- 自身、友军与位置使用既有 MaledictusCombatActions。实体执行有真实冲量；位置执行用未注册调用帧进行碰撞和简化运动，不生成 Boss，也不移动／占用付款者。保留实际身体行动替换、资格失效、死亡／换维度与卸载清理。
- 幻影协议白名单增加状态18，客户端使用原生 `spin_slashes` 动画和双锤形态，沿用透明度及 `action_phantoms.maledictus_actions` 配置。原生挥击声音保留，实际执行者在20／34 tick挥手。没有为本体没有独立粒子调用的旋斩新增任意粒子。
- 后续 Ars 效果保持既有动作魔符的启动后立即继续规则，不等两次挥击结束，也不因每击重复执行整段编排。

## 数值与获取

- 整组额外180 Mana，强化每份额外10；Ars形态及其他效果费用另算。
- 主动冷却100 tick（5秒），通过既有Conflux provider处理。原生AI在动作结束时设置100 tick，附属仍按既有主动施法起点计时，两者起点不同。
- 配置：`mana.maledictus_spin_slashes`、`cooldowns.ticks.maledictus_spin_slashes`。旧配置加载时由配置规范补充新默认项。
- Ars抄写台：咒魂碎片×1＋魔源宝石×1，27经验。图标运行时引用断魂战戟，并加入原生物品渲染委托，未打包上游素材。
- 全系列14种，累计8锭＋6碎片；实际需取得9锭，本体每次保底3锭，仍为三次保底击杀学齐。全附属共71种魔符。

## 验证

先运行新增旋斩与既有咒翼动作，共11项定向服务端测试：既有7项及新增3项通过，真实盾挡测试失败。排查确认两个伪玩家共用 `CatStudent` 记分板名，盾挡者被误算友军，因此根本未受攻击；仅给该夹具增加独立的 `SpinGuard` 身份和非友军前置断言，未调整业务逻辑去适应测试。

修正后只重跑新增旋斩namespace的4项，全部通过，不重复此前已通过的7项回归。

```powershell
./gradlew.bat :runGameTestServer :jar :sourcesJar :verifyDistributions '-PgameTestNamespaces=cataclysm_nouveau_maledictus_spin,cataclysm_nouveau_maledictus_combat' --offline --console=plain
./gradlew.bat :runGameTestServer :jar :sourcesJar :verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_maledictus_spin --offline --console=plain
```

- 日志：`build/spin-dev49-validation.log`、`build/spin-dev49-final.log`，最终构建成功。PowerShell重定向日志为UTF-16，读取时应识别BOM，不按UTF-8强行解码。
- 验证真实两次命中时序、逐击原生数值和增强／怒气、前冲方向和两击间转向、友军保护、实际伤害归属及整段费用；位置幻影的两次前冲和实际伤害，不移动付款者；敌方／空蓝拒绝、真实配方；原版盾第一击不被禁用、第二击成功禁盾；根失效清理。
- 全部支持的身体幻影状态进行专用服务器snapshot与编解码往返，包含新增18。双语71个魔符键及发行／源码包 `verifyDistributions` 通过，两包没有灾变素材或实现类。
- 未启动客户端，不把服务端协议／伤害检查称作动画或手感验收。未复制本体素材；依赖版本变化后的行为不自动视为已验证。
- 本次GameTest JVM已自然退出，Core受版本控制文件干净。没有远程推送或CurseForge发布。

## 交付

- 版本：`0.1.0-dev.49`，提交见本地tag `v0.1.0-dev.49`。
- `build/libs/cataclysm-nouveau-0.1.0-dev.49-mc1.21.1-neoforge.jar`：`f3c4bd6e8f38c95b3d70d1d87305763dd551eda329376171ed7384c1f0393dc1`。
- `build/libs/cataclysm-nouveau-0.1.0-dev.49-sources.jar`：`6115718b8cabe0146be51c0b686484e53140de2183338a6c0c546465961866ee`。

串行部署完成后在下方补充记录；目标仍为「万法归一」，不是当前会话入口的「万途归零」。
