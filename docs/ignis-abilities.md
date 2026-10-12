# 焰魔审计与首批边界

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

五项各一焰锭＋魔源宝石，累计5焰锭；原生固定掉3，因此两次击杀覆盖，不使用低概率唱片。后续扩展仍须复核整套预算，不能无限追加每项一整锭。

## 本次未接入

招架/反击、穿刺抓取/骑乘、终极攻击、转阶段本身及更多普通横扫/强化重砸，仅作为后续候选。授权先做焰魔不代表机械注册每个动画。当前首批由用户上一轮讨论的火球、剑刃阵、跃砸和代表性完整剑术组成。
