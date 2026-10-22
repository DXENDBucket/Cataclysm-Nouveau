# 炽燃狂魂三项体术（dev.59）

2026-10-23，范围为交叉双斩、狂魂剑舞和回旋绞杀。

## 基线与隔离

- 工作树`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-berserker-dev59-20261023/Cataclysm-Nouveau`，分支`feature/berserker-dev59`，起点`f8e99df59ff5135ec391b860a62460a3fb49e361`。
- 固定Core `ed44ebb86bd087f3546a1bc6a315d2692f70262c`（0.1.28），受版本控制文件干净；没有Core改动。
- MC1.21.1／NeoForge21.1.250／Java21，灾变实际发布JAR1.21.1-3.33，SHA256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 反编译审计仅在系统临时目录`cata-berserker-audit`，检查实际实体、注册Goal、模型／渲染器和掉落，不在仓库分发原生实现或素材。

## 原生行为和费用

| 魔符 | 行为 | 默认费用／冷却 |
| --- | --- | --- |
| 交叉双斩 `berserker_cross_slash` | state1、50tick动作；17tick窄角第一击，35tick宽角第二击，每击7.5×1.25＝9.375；第一击原生60tick禁盾。 | 整组90 Mana／3秒 |
| 狂魂剑舞 `berserker_sword_dance` | 每次随机选择state5或6，各50／55tick。左式13／20／25／30tick、右式17／25／28／35tick四次7.5伤害，保留各自原生前冲。 | 整组150 Mana／4秒 |
| 回旋绞杀 `berserker_spin` | state2起势30tick→state3旋转90tick→state4收势40tick。旋转每4tick尝试原生7.5接触伤害与推力。 | 整组180 Mana／8秒 |

双斩、剑舞的原生AreaAttack仅在伤害成功时叠加100tick炽热烙印，最多II；第一层恢复2生命，第二层及已有II时恢复4。通过帧的heal覆写恢复实际executor；位置执行没有实体executor，恢复原施法者。恢复金额保持原生，不随伤害增强另行放大。旋转接触不经过AreaAttack，没有烙印、恢复或禁盾，保持原样。

state6推力条件在实际3.33中是15／123／26／33tick，动作55tick结束，所以实际只有三次；不擅自改成15／23／26／33。两套仍各四次攻击。正常伤害无敌帧保留，短间隔连击与旋转不保证每次尝试都扣血。费用参考现有近战连段和持续旋转，未重新跑Iron高等级／装备的完整平衡矩阵。费用和冷却复用既有逐能力配置，没有新增吟唱；旋转时长本批保持原生，不新增时效修饰。

## 共同执行与两端

使用既有beneficial FEET anchor，只允许自身、Core友军或方块位置；敌方实体失败且不回退付款者、不收费。原施法者与damageOwner保留，伤害只在适配帧上通过NativeSpellDamage接入Ars装备属性／Pre–Post及现有有界20%增强（每级2～10）。普通原生狂魂保持原来的来源、伤害、AI和自我恢复。

同一driver处理三种锚点。动作读取实际执行者朝向，双斩与起势阶段按原生窗口转向，剑舞全程可转向；固定点保留触发方向。Goal.tick后恢复共同姿态，再调用完整原生aiStep，最后读取运动，确保aiStep中的push不被遗漏。实际实体接收原生冲量；位置剑舞由小碰撞帧自行移动／重力／阻挡，不吸回初始锚点，也不移动付款者。攻击调用期间临时使用原生狂魂身体包围盒，移动碰撞沿用实际实体或位置帧。

旋转维持totalAge供原生tickCount%4使用，phaseAge供attackTicks和各阶段Goal时长使用；完整执行30＋90＋40。实体执行接入Core可替换身体动作；付费成功的新动作可跨系列打断，空蓝／非法目标不打断。独立位置动作不占据付款者身体；所有者／执行者死亡、盟友资格撤销、区域未加载或禁用时停止并释放AI控制，不额外加载地形。帧、marker均不出生、不保存。

新增BERSERKER幻影家族，复用现有snapshot／payload／原生proxy／通用透明渲染，白名单涵盖state1、2、3、4、5、6。原生动画key分别x_slash、mixer_start、mixer_idle、mixer_finish、sword_dance_left、sword_dance_right；本地`action_phantoms.berserker_actions=true`默认开启。实际原生renderer提供模型与glow层，没有独立挥砍弹体或粒子可另接。原生挥击声音通过帧的位置发送。无客户端Boss AI、音乐或防御。

## 获取

三项都是真正的Ars抄写配方：将熄余烬`cataclysm:dying_ember`×1＋魔源宝石×1、27经验。总计3份余烬。原生掉落0–2份，平均约3次击杀覆盖，不能宣称3次保底；抢夺保留原样。没有额外要求饰品或稀有掉落。图标引用本体dying_ember模型，路径已核对，不复制贴图。三项按IGNITED_BERSERKER系列相邻排列，不影响原有类别。

## 验证

- 集中命令`:runGameTestServer :verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_berserker --offline --console=plain`，最终**8/8通过，BUILD SUCCESSFUL**。工作区日志`dev59-tests-final.log`。
- 实测双斩17／35tick、一次费用、9.375基础＋一次有界增强＋Ars属性加3、原生烙印／5秒／叠层／2与4回血、真实owner、友军过滤及50tick结束。
- 剑舞实测完整四击时序、原生冲量、友军实际方向与回血、付款者不回血、一次费用及结束解锁；位置剑舞实际前移且不拖动付款者。
- 旋转完整三阶段、每4tick接触尝试、推力、无烙印／恢复、朋友保护和整组收费；测试仅在需要计数时重置目标原生无敌帧，业务不绕过它。
- 敌方执行拒绝、空蓝不打断、跨系列替换、盟友资格撤销、创造零蓝、所有者消失清理；未绑定原生狂魂伤害／来源／恢复隔离。
- 扩展全体原生身体动画协议往返，含六种新state；冥行砸地原生五刃／寿命／粒子包回归通过。
- 初次7/8：友军测试误用未跟踪该实体的伪玩家收包来识别动作，空集合getLast失败；改为观测真实命中时序／运动／占用状态，业务未因该夹具问题改变。完整协议另有独立覆盖。
- 98种魔符中英文名称／描述、运行时图标路径、发行包和源码包verifyDistributions通过，无上游实现／素材。
- 固定Core干净，测试服务已退出，仅余Gradle daemon（本次PID54992）。未启动客户端，模型／glow透明度、动画连续性、转向与声音仍需人工验收；未重复无关NEG／Hex／其他附属矩阵。

## 产物

- `cataclysm-nouveau-0.1.0-dev.59-mc1.21.1-neoforge.jar`
  SHA256 `f32ba9db7da60dfc68fee858d66667fc68c7924aed2115f8d23fef34f41be27e`
- `cataclysm-nouveau-0.1.0-dev.59-sources.jar`
  SHA256 `be821964d778980fc5469cfc93df47dc8aaffac8b391a505240d64c8e4482e8a`

## 提交与部署

实现提交`cfb4909c7d99385073e1436985b914be4a586f7e`，标签`v0.1.0-dev.59`，主仓库已快进合并。未推送、未上传CurseForge。

已部署“万法归一”mods，仅以dev.59替换dev.58；其余173文件和活跃服务端配置哈希不变，Core仍为0.1.27。旧JAR与部署清单位于实例`codex-backups/cataclysm-dev59-20261023`，主仓库build/libs已同步发行／源码包。未修改存档或其他实例；新配置项下次启动补齐默认值。

人工验收重点：三类动作原生幻影与glow、双斩和剑舞朝向／前冲／命中恢复、固定位置剑舞连续移动、回旋完整三阶段与音效。本次没有客户端人工验收。
