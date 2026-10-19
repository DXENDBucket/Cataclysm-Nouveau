# 远古遗魂第二批（dev.55）

本批按上一条回复的优先建议实现沙碑阵、三连震地、诅咒沙暴弹。冲撞、横扫、双重践踏未在本批制作。开发始于2026-10-19，验证与交付在2026-10-20。

## 基线与隔离

- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-remnant-dev55-20261019/Cataclysm-Nouveau`
- 分支：`feature/remnant-second-dev55`，起点 `9a784af111ae61f091b8c02fe8ba3d5eff5c229a`。
- Core固定：`ed44ebb86bd087f3546a1bc6a315d2692f70262c`，0.1.28，隔离副本受版本控制内容未修改。
- Cataclysm实际发布JAR：1.21.1-3.33，SHA256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`；JDK21、NeoForge21.1.250。未新增其他附属硬依赖。
- 原生反编译审计仅在系统临时目录 `cata-remnant-audit`；发行与源码包都不含本体素材或实现。

## 最终行为

| 魔符 | 原生入口 | 默认费用／冷却 |
| --- | --- | --- |
| 沙碑阵 `desert_stele_array` | 注册的 `RemnantMonolithAttackGoal.start/tick`，20tick出生；八臂每臂16碑，另有朝目标的16碑直线。保留原生警告、延迟、上寻天花板、坠落贯穿、落地冲击、诅咒及碎裂。 | 整阵600 Mana／16秒 |
| 三连震地 `remnant_triple_quake` | `Ancient_Remnant_Entity.aiStep` state15，在26/55/85tick各砸一次，完整调用 `EarthQuakeSummon`。每次22～29原生地震波，保留直接砸击、地震波对着地目标的伤害、击退和尘浪。 | 整段420 Mana／10秒 |
| 诅咒沙暴弹 `cursed_sandstorm` | 原生 `Cursed_Sandstorm_Entity` 与 `CustomAbstractHurtingProjectile`，15tick后追踪，可成功伤害两次，附加沙漠诅咒II，再进入原生消退。基础伤害读取沙暴之怒配置（默认7.5，按满蓄力单弹）。 | 普通发射一次50 Mana，重复形态每触发50／无冷却 |

原生伤害仍应用现有有界增强、Ars装备与事件一次。三连直接砸击默认25＋min(25,最大生命×5%)，子地震波默认11；修正 dev.54 原先只为单践踏准备的 `.9×攻击伤害` 封顶基准，新砸击按1.0倍。没有新增吟唱。

### 几何与执行

两个阵列使用 Core 的 beneficial `ExecutionAnchor` 与现有 `ActionGeometry`：只允许自身、友军或方块位置作为执行中心，敌方实体拒绝且退款；实体可在瞄准窗口转向，固定点保持原方向，生物执行者可读攻击目标。沙碑的定向列由原生 Goal 对实际目标确定，未选到敌人时使用未注册标记提供当前前方落点。

两个范围能力不取得身体动作声明，允许相互或与其他技能并行；父会话只付一份费用，子沙碑／地震波重绑真实 damageOwner 和上下文，不独立收费或执行整段后续 Ars 编排。子地震波有正确来源能力、数值修饰与瞬态标记。

父会话负责全部子实体，根／执行者消失、友军资格撤销、禁用、卸载或自然结束时清理。只使用已加载区域，启动检查覆盖整个原生阵列；不会主动加载新区块。父会话上限205tick，保留全部原生攻击与子弹体寿命。

### 弹射物与两端表现

诅咒沙暴弹复用现有 `HarbingerCasting` 原生弹射物驱动，加入已有伤害、友军、临时状态和轨迹接口。普通发射保留原生加速／追踪，Ars加速同时应用于初始power与原生追踪上限。伤害成功才续接；准备／消退状态和友军接触不产生错误续接。原生两次命中不改为单次。

特殊形态运行完整 native tick 以保留绿色风暴粒子、动画和水下粒子，但由 Ars 独占位移／碰撞。CustomAbstractHurtingProjectile 的碰撞发生在具体子类之前，必须在该基类补 opt-in 钩子；仅包装具体类tick会漏掉原生碰撞。重复形态每次触发付款，成功攻击后复位两次命中状态并持续刷新原生寿命，防止长时间环绕无声失效；普通／非重复形态保留原生消退，空蓝在下一次攻击前结束。

两个阵列扩展既有 `BodyPhantom` 协议家族：服务器snapshot、校验白名单、客户端原生proxy、字面量动画key `monolith`／`ground_tail` 和客户端 `action_phantoms.remnant_actions` 开关（默认true）。幻影只渲染，不tick／生成Boss。模型透明度沿用0.28／第一人称0.10，特效沿用独立配置。

原生 roar 粒子同 StompParticle 一样在客户端分支，脱离世界的帧通过服务器发送原参数；原生实体绑定声音包改为实际位置广播，避免引用不存在的Boss实体ID。原生沙碑／沙暴弹仍使用真实注册实体的客户端渲染、实体事件与复杂出生数据。沙暴之怒 builtin 图标委托其原生物品渲染器并传原生物品身份；只引用资源路径。

## 获取与排序

三项分别消耗遗魂头骨1、遗魂头骨1、瓶中沙暴1，另各需魔源宝石1和27经验。已有配方不改。

全七项总计遗魂头骨3、瓶中沙暴2、远古金属锭2；本体每只Boss必掉头骨1、瓶1、金属块1（可拆九锭），三次保底击杀覆盖全套，不额外要求沙暴之怒或其他Boss材料。图标可与配方材料不同。仍按远古遗魂系列相邻排列，弹射物替换在增强类末组。

## 验证与限制

- 编译初次发现新增 Kind 后一个旧 exhaustive switch 需要明确分流；按原有 Maledictus 的共享驱动分流方式补齐，随后编译通过。
- 一次集中运行 `:runGameTestServer :verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_remnant_second --offline --console=plain`，**9/9通过**，工作区 `dev55-tests.log`。
- 覆盖144碑/20tick/原生延迟/固定方向/子归属/整组付费；三次砸击26/55/85与各22～29波/原生直接和子伤害/一次增强/友军/清理；友军移动与瞄准；失败退款/创造零蓝/并行；沙暴弹15tick追踪/两次成功命中/诅咒/消退；310tick重复形态无双驱动/逐次付款/空蓝停止；完整既有幻影协议含新增状态专用服务器往返；单碑及共用深渊灵珠回归。
- 88种中英名称、描述通过；二进制与源码分发边界检查通过。
- 未开客户端、未重复完整NEG/Hex矩阵。原生幻影模型、绿色拖尾、咆哮／碎裂听感仍待人工确认，服务端通过不等于外观验收。

## 产物

- `cataclysm-nouveau-0.1.0-dev.55-mc1.21.1-neoforge.jar`
  SHA256 `0b44ad351323a54c0df49e0dad98fdd0cd2839f95ae157e4c94d259778f23a6f`
- `cataclysm-nouveau-0.1.0-dev.55-sources.jar`
  SHA256 `fc21c91642222b96e62a77dc5adace666e7c7c2201be2178166f09eededa3043`
