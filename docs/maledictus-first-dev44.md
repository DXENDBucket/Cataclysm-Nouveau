# 咒翼灵骸首批 dev.44

- 范围：幽灵之箭、单体幻影戟、咒翼怒气主动魔符；未接入咒翼近战、戟阵、灵魂爆发和抓取。
- 固定 Core：`08c307f3238b9b9aa7fd628b9771cd287160448d`，0.1.24，受版本控制文件保持只读。
- 实际本体：Cataclysm 1.21.1-3.33，SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 游戏／加载器：Minecraft1.21.1、NeoForge21.1.250；自动检查Ars5.13.1。没有新增Forge版本。
- 工作分支：`feat/maledictus-first`，独立工作树与测试目录；交付后由根协调者串行部署万法归一，仅更新本附属。

## 行为与平衡

怒气是beneficial MobEffect，玩家和其他LivingEntity共享。每次主动魔符加一层，Amplify增加层数，Dampen降低本次增加量；增加量为0则失败退款。上限五层，增层刷新200tick递减间隔（可配置）。原版hiddenEffect链保留下一层、衰减和序列化；用同一状态实现未来成功近战叠层与爆发清空。当前普通攻击不会加层，也未提供咒翼近战能力。

| 能力 | 0／3／5层原生基准伤害 | Mana | 主动冷却 |
| --- | --- | ---: | ---: |
| 幽灵之箭 | 5／5.75／6.25 |45|0|
| 幻影戟（每次成功伤害） |11／14.3／16.5|120|60tick|
| 咒翼怒气 | 无伤害，默认增加一层 |30|0|

表中使用本体默认配置，未计Ars修饰／装备。箭原生类型绕过hurt冷却，戟类型绕过护甲／盾牌但保留hurt冷却；戟攻击窗口可能产生多次成功伤害。因此不能仅把单次5或11与所有其他弹体直接比较。增强／减弱在含怒气的原生基准上按现有受限20%公式计算，再走Core原生法术伤害桥接。

原生Registered EntityType和原生客户端renderer均保留。箭普通形态速度1.8、支持条件追踪；特殊形态由ArsTrajectory独占运动／碰撞，仍tick完整本体尾部视觉。玩家箭不可拾取，失败伤害也不会生成免费箭；重复地面碰撞不锁死grounded状态。单次普通箭保留原生命中后的飞行／落地生命周期，会话仍受Ars载体寿命和根资格约束。

单戟投影至目标附近地面，预备10tick后本体选择四种动画；warmup=-13至-33攻击，lifeTicks>70移除。本体同步字段、事件4、客户端时间／声音／命中纹章／消退粒子不重新模拟。Core管理根死亡、离开维度、区块／能力资格与期限清理，managed实例不保存。未绑定的本体实例不读取怒气状态或Ars修饰。

三个魔符各使用cursium_ingot×1＋source_gem×1，27经验；本体每次必掉3–4咒魂锭，一次击杀即覆盖首批。组别MALEDICTUS排序保持同Boss相邻，弹射物替换仍排在其余增强之后。

## 验证

Java21，`runGameTestServer jar sourcesJar verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_maledictus --offline`。

最终六项required GameTest全部通过：敌方／方块怒气失败不扣费、玩家增层与强化、友军生物共享状态、自然逐层衰减与刷新、原版保存／加载与全链递减；原生箭速度／直接伤害／怒气系数／拥有者／重复扣费／原生无敌帧穿透／友军过滤／失败伤害与拾取／空蓝／根清理；戟预备／随机同步state／多次伤害／护甲穿透／友军／拥有者／无地面退款／自然结束与真实抄写台配方。

首跑5/6通过，失败原因是伪ServerPlayer未进入PlayerList，不会正常doTick；改用真实生物自然tick检查后6/6。业务逻辑未因该测试修改。未运行客户端，不能将服务端state／原生路径检查表述为客户端动画或粒子已确认。

`verifyGlyphLocalization`核对60个魔符双语键，`verifyDistributions`核对二进制和源码包均无灾变素材／实现／依赖。Core副本受版本控制文件保持不变。

## 产物SHA-256

- `cataclysm-nouveau-0.1.0-dev.44-mc1.21.1-neoforge.jar`：`144b86bcc2682a05395a112cdfb093fd424e3fb016aefc6a1f7f344fa7d950c6`
- `cataclysm-nouveau-0.1.0-dev.44-sources.jar`：`9f88a866b972e114e608dfd1c2e310d87156b96b7b02a6e9d9b3827d7f72efb0`
