# 焰魔首批交接（dev.29）

## 范围与基线

- 用户授权先做焰魔。首批两种火球替换、三发剑刃火球阵、跃砸、完整剑舞；招架、穿刺抓取、终极攻击、转阶段本身不在本批。
- 工作树 `C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`，父提交 `7dfdfbc`，标签 `v0.1.0-dev.29`。
- 相邻固定Core `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），tracked保持干净；不改Core、不新增附属硬依赖。
- MC1.21.1 NeoForge，JDK21，Cataclysm3.33 JAR SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。依据实际JAR的CMAbstractHurtingProjectile、两个Ignis火球、renderer、IgnisExplosion、Ignis目标Goal/aiStep/掉落表；审计输出仅在系统临时目录。

## 实现

- `IgnisCasting`提供发射、目标修正、原生爆炸上下文、子弹会话和两种动作；`SpellIgnis`为不注册到世界的调用帧，nativeGoal和原生攻击体拥有行为。
- 普通火球6＋7%最大生命，灵魂8＋7%，深渊10＋20%，成功直接命中回血5并叠加原生烙印。原生百分比不另加封顶，增强/削弱仍由本附属20%/2–10受限规则处理一次，再接Ars事件。
- 直接替换采用原生0.25发射初速、常规0.2/灵魂0.25 power；加速在转换时作用于初速和power，后续目标修正沿用同一倍率。保留native64点trail、CM原生运动/碰撞/惯性；特殊Ars轨迹只抑制decorative scope内第二套位移/碰撞/到期，不删native tick。
- 原生IgnisExplosion构造补真实伤害源、友军伤害与击退过滤、受限增强；explosion.explode在Core damage scope执行，finalizeExplosion继续原生粒子包。默认terrain_fire=false、KEEP地块；配置可允许留下火焰。
- 深渊普通墙面反弹六次，下一次爆炸；原生hurt可打回，但联动恢复Core显式damageOwner，保留运动改变、不偷偷转移付款/伤害角色。原生未绑定弹体不改。
- 剑刃火球21/23/25tick调用原生bladeFireball，delay30/28/26、同期释放；保存目标并接timer0/-40的原生修正时机，以免real player owner失去Mob.getTarget后停在原地。三发全部继承来源、增强和owner，母动作一次付费。
- 跃砸调用Air_Smash，19tick把原生跳跃冲量交给真实自身/友军，实际着地才SMASH，5/8/11/14tick完整打击及地面波；普通SMASH不点燃。完整Combo1→Combo2，包含冲量、原生横扫/冲撞、烙印与回血、收尾地面波。AI让出控制，动作互斥，退出恢复重力并清理。
- 阶段由施法开始时根施法者生命比例确定，≤2/3灵魂，≤1/3破盾；不按友军血量、不播放Boss转阶段无敌/音乐/破坏。
- 一焰锭＋一魔源宝石、27经验，各五项；总5焰锭，原生固定掉3，通常两次击杀覆盖。类别/排序复用现有Core呈现；模型/图标委托本体物品，未复制素材或本体源码。
- 默认额外Mana/主动冷却：火球120/40tick、深渊200/80、火球阵320/120、跃砸300/160、剑舞650/240。Boss百分比、烙印回血和多段输出决定其费用，保持原生机制再以费用平衡；可配置。

## 验证

- 最终命令 `:runGameTestServer :build --offline --console=plain --no-daemon`，日志 `build-ignis-dev29-release.log`：All67requiredtestspassed、BUILD SUCCESSFUL；52魔符双语检查、binary/sources verifyDistributions通过。
- 六项新增服务端检查：直接伤害/受限增强/回血/烙印/爆炸友军，原生初速和power，六次墙面反射与耗尽、打回归属，特殊轨迹原生trail/一次位移/保活/逐次扣费与空蓝，三发准备/玩家owner目标修正/根清理，真实跳跃/落地/非点燃/归属/重力恢复，两段剑舞/烙印回血归属/友军/地形。
- 首轮及诊断复跑均只剩跃砸断言错误：原生参数firetime=0，而测试误期待点燃；诊断证实健康394、fire0、mob来源，改成明确验证原生不点燃，业务未强行加点燃。最终补核对原生初始飞行参数后再次全部通过。没有为了省略失败而删除检查。
- 全部资源JSON解析，五个模型父路径在本体JAR存在，成本与取得数量检查通过。构建/测试会话已结束。
- 未人工启动客户端：原生拖尾/爆炸已经由代码和服务端检查覆盖接线，不等于客户端画面确认；混装特效、动作手感仍需用户重启确认。

## 交付

- 协调者串行部署万法归一mods，dev.28备份 `mod-backups/2026-10-12-ignis-dev29`，仅替换本附属；共享Core、既有配置和用户游戏进程不改。新配置由加载器补全；重启生效。
- 本地提交/标签，无远程推送，无Forge交付。后续先读AGENTS/共享流程及本体审计，不能把未验证客户端说成已完成画面验证。
- binary `cataclysm-nouveau-0.1.0-dev.29-mc1.21.1-neoforge.jar`：`87f8d9201d171332f886bd871e592a90fdca9d6b644fcf44cc1eafe519b19ca6`
- sources `cataclysm-nouveau-0.1.0-dev.29-sources.jar`：`60fe7fb87b6d571253fbed8b3670e62f7e6fd65e67457c8539965bd3f04b01bc`
