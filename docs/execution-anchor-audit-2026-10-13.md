# 执行锚点与转向专项审计

后续进展：Iron 0.17.16 已修第1项Forge渲染注入；Core 0.1.20、Iron 0.17.17 和灾变dev.34处理了第2项实际射线范围，以及首批能力的实体占用、授权关系与持续复查。剑舞、Iron吐息和血吸光束已共用Core边界，并通过真实附属共存用例。以下是修复前的审计快照，不能据此认为上述项目仍未处理；其他能力的完整锚点迁移、其他附属及跨维度组合仍留后续验证。

剑舞的实体／位置执行模式可以继续使用，但推广前应先统一执行占用和授权关系。另发现血吸光束的远程查询缺陷，以及一份用户报告明确暴露的 Forge 客户端注入错误。本文记录问题和修复顺序，不代表已实施修复或已通过新的客户端验证。

## 审计范围与版本

2026-10-13，审查 Cataclysm & Nouveau dev.33（`b20984d`）、Iron & Nouveau 0.17.15（`c11b88a`）及 Ars Conflux 0.1.19（`9611f50`）。以剑舞、Iron 吐息和血吸光束为主，对照 Core 会话、上下文、友军 API、NEG 续接及既有测试。Iron 实际 JAR 为两平台 3.16.3；灾变仍为 NeoForge 3.33。

除用户崩溃外，下述结论来自静态调用链与实际发布 JAR 核对。没有启动游戏、重新运行测试矩阵、改动运行逻辑或部署新包。dev.33 的80项历史测试及用户视觉反馈仍有效，但不覆盖这里列出的全部边界。

## 应先处理的缺陷

### 1 Forge 血吸光束渲染 Mixin 注入目标不存在

优先级高。用户报告：[日志集合](https://paste.kostromdan.dev/mclogsarray/sIQ6Z3fUO7FBeA0gISoW3pWK)，[崩溃原文](https://api.mclo.gs/1/raw/lfLKgTZ)。环境为 MC 1.20.1、Forge 47.4.21、Iron 3.16.3、Iron & Nouveau 0.17.15、Conflux 0.1.19。

直接失败为 `NativeSiphonRenderingMixin` 的 `ironsNouveau$endpoint` 注入0次。`Irons-Nouveau/forge/src/main/java/dev/ironsnouveau/mixin/NativeSiphonRenderingMixin.java:17` 要求 `renderRayOfSiphoning` 内出现 `RaycastBuilder.build`，但实际 Forge Iron JAR 的此方法使用 `Utils.raycastForEntity`。客户端在渲染亡灵术士时首次加载原生渲染辅助类，因此即使没有主动使用联动光束也会崩溃。

这是可由本地同版本发布 JAR 确认的双平台实现差异，不能把报告顶部的 Embeddium/Oculus 提示或栈中的 MixinExtras 0.5.4 当作原因。修复应使用 Forge 的真实调用入口；不应只把 `require` 改为0而留下失效的联动几何替换。至少验证客户端目标类能够转换，并保留普通 Iron 原生渲染的回退路径。服务端 GameTest 不会加载此客户端 Mixin。

### 2 血吸光束的远程命中范围仍依赖错误实体

优先级高，确认 NeoForge 路径。`Irons-Nouveau/src/main/java/dev/ironsnouveau/casting/SiphonRayExecution.java:51` 将正确的远端 `aim.origin` 传给原生 RaycastBuilder，却仍用根施法者作为 `originEntity`。

实际 NeoForge Iron 3.16.3 的 `RaycastBuilder.performRaycast` 以 `originEntity.getBoundingBox().expandTowards(end-start)` 圈选候选实体；显式设置 start 并不会替换此碰撞箱。施法者和锚点横向分离时，位于真正射线上的敌人可能根本不进入候选集合，表现为远程光束没有命中。

客户端 `Irons-Nouveau/src/main/java/dev/ironsnouveau/client/SiphonRayVisuals.java:84` 在固定位置模式下选本地观察玩家作为查询实体，同一原理会让不同观察者得到不同的实体端点。实体锚点模式使用实际锚点，情况较好，但应共用以实际射线为范围的查询。

对照 `NativeRayGeometry.frost` 已专门处理这一原生约束。推荐共用空间查询能力，输入真实 origin/end、排除实体与过滤策略，保留原生方块裁剪和交点算法。最小回归场景：根施法者与位置／友军锚点在不同Z坐标，敌人在远端射线中；另外两位不同位置的观察者应看到一致端点。不能移动真实施法者来迁就查询。

### 3 执行占用查询只认根施法者

优先级高，推广前必须解决。`Ars-Conflux/.../ExecutionSessions.java:21` 查询 `session.caster()==actor && occupiesCaster()`，没有表达实际被占用的执行实体。

- Iron `BreathExecution.java:24` 和 `SiphonRayExecution.java:19` 无条件返回true。A让友军B或固定点喷吐时，A会被标记占用；固定点也会阻挡A的绑定施法工具（`BoundSpellSupport.java:76`）和使用该查询的灾变动作。
- 剑舞仅在实体执行者等于根施法者时返回true。A让B剑舞时，Core查询B和A都不占用，灾变靠本地ACTORS补充。然而旧 `IgnisCasting.action:159` 没有检查自己的ACTORS；同一个B再接受旧跃砸等动作时，会漏掉已运行的远程剑舞。其他系列各自维护动作集合，也不能自然覆盖新增系列或其他附属。

建议 Core 表达“占用某个实体的某类执行资源”，例如实体动作或持续引导，不再把付款者当作被占用对象。位置输出默认没有实体动作占用；同时是否占用施法者的引导能力应由能力显式声明。具体哪些动作互斥仍可由附属决定，不应把所有持续法术一律互斥。结束一个会话只释放它自己的占用。

## 授权与上下文需要统一的约定

### 4 友军查询与授权参照不同

`Iron Targeting.alliedExecutor:15` 只检查自身及双方原生 `isAlliedTo`；剑舞通过 `HarbingerCasting.allied:35` 使用 Core `Allies.areAllies`，支持所有者关系和其他附属注册的关系提供者。因此 Core认定的友军可能通过“周围友军”形态选中，却被 Iron 的执行入口拒绝。既有 Iron测试主要通过原版记分板结盟，未覆盖仅由关系提供者认定的友军。

另一处差异是 `HarbingerCasting.allied` 同时合并caster、damageOwner和executor的友军关系。它适合伤害保护，却也被 `ActionAnchor.beneficial:11` 用于执行授权。显式将伤害归属设为不友好的C时，即使C与根施法者A不友好，C本身及其友军也会被当前剑舞授权为执行实体；Iron明文要求原施法者到目标的关系。

建议共用 Core 友军查询，但分开两个问题：谁允许成为实际执行者，以及攻击应保护谁。执行授权默认参照根施法者，不能因改变damageOwner自动取得控制实体的权限；伤害保护可以继续保护根施法者、执行者及相应友军。普通治疗的接受目标规则也不应隐式套用“强制某实体执行动作”的授权。

### 5 持续动作的授权复查不同

`Iron BreathPose.valid:29` 每次检查实际执行者是否仍为友军，取消后由Core和驱动清理。剑舞 `ActionAnchor.present:26` 检查生命、世界、骑乘和已加载区域，但不检查友军关系或旁观者状态。双方解除结盟后，吐息终止，剑舞仍继续。

推荐把实体锚点的基础有效性与可复查授权独立表达。根所有者、实际执行者死亡／移除／换维度和失去授权都应有明确取消路径；骑乘是否允许、是否要求站立应由具体动作决定，不能把剑舞的限制扩散到吐息。中途取消只清理本会话的实体、视觉和占用。

## 组合和公共 API 边界

已有 `CastContexts` 将caster、damageOwner、account保留在Ars克隆附件中，刻意不把上一命中的executor或target沿用给下一命中。这一点应保留。下一次效果依据新HitResult选择实体或位置锚点，不能在根法术创建时选定锚点后一直传播。

NEG的改自身会生成新的实体命中，改脚下会生成位置命中；锚点应遵守最终命中几何，不应只根据最初的castType选择模式。传播、散射、延迟及重复形态中的每次实际触发都应独立建立执行与按能力付费。Core和Iron已有派生／传播／上下文测试，但尚没有把新剑舞、Iron持续输出和真实NEG改形态放在同一测试中。

`TriggerGeometry.write` 忽略零向量，不清除已存的入射方向，`scoped` 的零向量也不会遮蔽外层方向。当前“写零表示什么”没有可表达的重置语义；在推广前应提供明确clear／继承规则，并验证新弹射物覆盖旧方向、非弹射物转换有意继承或重置，而不是因存储细节决定。这里未将所有保留旧方向的情况判作bug。

值得进入 Core 的是：实体／位置锚点身份与实时姿态采样、显式方向优先级、授权钩子、占用查询和已有会话取消机制。保留在附属的应是：嘴部／脚下偏移、三维或水平动作、转向窗口与速度、真实运动／着地、原生伤害与增强、费用节拍以及Boss模型。不能把剑舞的0.6×1.8调用框、20°/tick或两段时间表变成所有附属的默认行为。

资源池选择仍由来源和附属策略决定，锚点不会转移付款者。Iron沿用已有显式account，灾变默认取Ars账户；两者的策略差异需另行约定，不能仅因共用锚点就强制同一种资源。已有生命比例阶段仍取根施法者，不随锚点位置或执行者变化。

## 建议推进顺序

1. 先修Forge直接崩溃，以及血吸光束远程查询，并加针对性的客户端类转换／远程射线验证。
2. 在独立Core任务中定义实际执行占用和中立锚点接口；同时统一授权参照与友军查询。双平台共用语义，平台入口只处理映射差异。
3. 迁移剑舞和Iron吐息作两种对照，再验证真实NEG改形态、延迟、重复触发与解除友军关系。
4. 约定稳定后逐批推广其他灾变动作及附属，不先对所有能力批量替换。本体动画、模型和地形运动仍需分别核对。

本次只新增审计记录，没有调整Core、Iron或灾变运行代码，也没有宣称报告中的问题已修复。
