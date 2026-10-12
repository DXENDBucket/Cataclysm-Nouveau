# 焰魔审计与完成边界

实际依据Cataclysm1.21.1-3.33发布JAR，SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。审计产物在系统临时目录 `cataclysm-ignis-audit-20261012`，不复制本体源码或素材进入仓库。

## 原生入口

- Ignis_Fireball_Entity、Ignis_Abyss_Fireball_Entity继承本体CMAbstractHurtingProjectile，不能按vanilla AbstractHurtingProjectile或ThrowableProjectile处理。完整tick包含power/inertia位移、碰撞、timer/fired、两次目标修正、64点trail历史。完整renderer依赖hasTrail/getTrailPosition，跳过tick会失去原生拖尾。
- 普通直接伤害6＋7%最大生命，灵魂为8＋7%，成功命中回血5，烙印初次II，后续叠加最高V，默认200tick。爆炸半径1，IgnisExplosion.finalizeExplosion保留原生粒子包。
- 深渊直接伤害10＋20%，半径2，普通模式六次墙面反射后下一次爆炸移除；本体hurt可打回并改owner，联动保留打回方向但按Core显式伤害角色恢复真实owner，付款者不随之转移。普通本体不改。
- 焰魔生命在2/3和1/3切阶段，第三阶段破盾。首批只读取施法根的阶段快照，不附加无敌、Boss音乐、破坏地形或完整AI。
- bladeFireball原生三发在强化空中攻击21／23／25tick产生，位置/高度不同，delay30／28／26，均在约51tick进入攻击。原生target修正依赖owner为Mob；把owner替换为玩家后必须保存目标并在timer0/-40时补接，不能只换owner便称完整。
- Air_Smash原生19tick水平距离×0.15、竖直1.3，着地才切SMASH；普通SMASH在5tick前方伤害及5/8/11/14四段地面冲击。签名中的整数参数依次为禁盾、点燃、烙印、回复；此处firetime=0，不因看到最后的5便误认为点燃5秒。
- Combo1在19/38/61/76打击，84切Combo2；后者包含27tick预测冲量、32冲撞、59/74小横扫、108收尾打击及108–116递进地面波。不能截成第一段而宣称完整剑舞。

## 已接入

两种弹射物替换、三发剑刃火球阵、跃砸、完整剑舞；费用与边界见README焰魔首批。Native调用帧不注册到世界、不运行Boss tick、不向客户端广播不存在的实体id。原生攻击体/爆炸继续执行，补真实归属、友军/物理过滤、增强一次、服务端视觉传输及会话清理。

dev.31 扩展后共有十项；两种基础火球各一焰锭碎片，其余八项各一焰锭，均另需一魔源宝石。焰锭可拆九片，整套用九枚焰锭（三次固定掉落），不使用低概率唱片。

## 最后一批完整行为

- MAGIC_ATTACK 第5tick调用原生随机布局：四枚普通／灵魂火球45／61／77／93tick后进入攻击，一枚深渊109tick；不能用已有三发bladeFireball阵冒充。shootFireball/shootAbyssFireball沿用准备、初始上抛、原生不准确度和真实目标修正。子会话允许最长400tick，不能沿用240tick截掉109＋160的原生寿命。
- EARTH_SHUDDERS在32／73／117重砸，第二轮远到近收拢、第三轮近到远外扩。Goal只在对应窗口更新朝向并停止水平位移，不能用重复三次普通跃砸代替。
- REINFORCED_SMASH_IN_AIR普通／灵魂分支均含23／53挥击和21／23／25三发bladeFireball；84tick起跳，真实onGround才转REINFORCED_SMASH／SOUL。落地5tick区域重砸及第一轮地面波，46tick第二次打击、两普通／灵魂＋一深渊准备弹；继续到115tick，不截断后半段。
- ULTIMATE_ATTACK的匿名AttackAnimationGoal1.start会先生成半径5的预备灵魂火环，不能只调用aiStep漏掉它。74tick重击、八道前方普通烈焰区域与4–37格宽地面波；全量调用本体，地形破坏对调用帧关闭。
- Flame_Strike_Entity每5tick搜索且damage内要求偶数tick，因此实际每10tick尝试伤害。damage和hpDamage是同步字段，hpDamage=6代表6%（与落雷的小数比例不同）；默认受本附属生命比例封顶。DATA_SEE、DATA_WAITING、radius和soul必须沿用原生同步；客户端完整tick产生火焰和燃烧音效，结束entityEvent4产生消退粒子。
- 普通烈焰duration结束后逐tick缩半径0.1，缩完才爆炸；灵魂不爆炸。Boss原生爆炸半径2、玩家武器版1，替换真实owner后仍保留Boss版2。爆炸构造引用当前烈焰的上下文，不能从owner实体误取另一法术的上下文；结束爆炸不留下地面火焰。
- 独立烈焰使用同一spawnFlameStrike与载荷，只改变触发位置、主体持续时间和预备时间；三个动作使用既有执行者锁与恢复机制。原生子载荷都保留母能力归属，不另扣费。

## 本次未接入

招架/反击、穿刺抓取/骑乘、转阶段本身及更多普通横扫/上挑/冲撞暂不接入。用户明确这一批之后焰魔差不多完成，不需要继续扩充体术。保留本体普通行为，不复制Boss完整AI、免伤或模型。75项服务端验证通过不等于客户端人工验收。
