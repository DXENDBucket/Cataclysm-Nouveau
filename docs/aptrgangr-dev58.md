# 冥行武弁两项能力（dev.58）

2026-10-21，范围为飞旋斧刃和砸地重击；不制作重斧横扫、抓取冲撞。

## 基线与隔离

- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-aptrgangr-dev58-20261021/Cataclysm-Nouveau`，分支`feature/aptrgangr-dev58`，起点`0373db0ce40f48bcfe44c3084b4364d2d170a196`。
- Core固定`ed44ebb86bd087f3546a1bc6a315d2692f70262c`（0.1.28），其受版本控制内容未修改；无新增Core API需求。
- MC1.21.1 NeoForge21.1.250，Java21；本体实际JAR 1.21.1-3.33，SHA256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 原生行为／渲染器审计位于系统临时目录`cata-aptr-audit`，不在仓库或发行包包含本体实现、模型或贴图。

## 行为与费用

| 魔符 | 行为 | 默认费用／冷却 |
| --- | --- | --- |
| 飞旋斧刃 `axe_blade` | 弹射物替换增强，使用原生Axe_Blade_Entity。配置基础伤害8，初速0、方向推力0.1与惯性0.85，保留穿透实体／方块和80tick淡出。加速修饰推力。 | 普通发射40 Mana，重复形态每触发40，无冷却 |
| 砸地重击 `aptrgangr_smash` | 效果，调用原生state2注册Goal和aiStep，40tick动作。第15tick近身重击：原生6.5范围／60度扇区、18基础伤害、120tick禁盾，同时在前方5格放出五道原生斧刃，各8伤害。 | 整组180 Mana，4秒冷却 |

伤害读取本体配置，既有20%有界增强（每级2～10）、Ars装备属性／伤害事件和Core友军关系均适用。单体理论18＋五刃之和不是保证输出，保留原生受伤无敌帧。费用参考同级穿透弹和现有砸地动作，未重新进行完整高等级Iron／装备平衡矩阵。

砸地共用beneficial FEET锚点，允许自身／友军／固定点；敌方实体不是执行者，不回退到付款者且不收费。准备前10tick可追随实际执行者朝向，攻击阶段锁定；固定点保留命中方向。真实caster、damageOwner与付款账户不变。原生扇形仍是-75／-45／-15／15／45度，不复制方向表，直接让原生aiStep生成。身体动作接入Core replaceable action claim，已付费新动作可跨系列打断，失败或未付费不打断；位置执行不占据施法者身体。

动作正常结束后，独立子会话让五道斧刃继续80tick原生寿命；中断时清理所属子弹。所有者失效、友军资格撤销、加载范围或启用状态失效均停止。子刃继承真实owner／增强上下文，不另收费，也不额外执行Ars编排。

## 弹射物与两端

Axe_Blade直接继承Entity，不是Projectile；附属提供专用opt-in碰撞入口和同步mode，沿用Core ArsTrajectory及CarrierProfile，无附属两两兼容。普通斧刃原生控制移动、碰撞、穿透和消退；特殊形态由Ars独占移动及碰撞，继续原生客户端动画／粒子tick。重复形态刷新原生lifetick与透明度，空蓝停止双方实体。

普通单刃仅在伤害成功时续接Ars，同一刃同一目标去重，但不取消之后的原生接触伤害。不同目标仍可各续接一次；重复载体逐次付费／伤害成功续接。碰方块不收费、不执行后续效果，也不令穿透弹消失。所有筛选只针对本附属绑定实例，原生冥行武弁不变。

图像使用原生全亮五帧透明斧刃渲染器和原生粒子。砸地扩展现有BodyPhantom协议、snapshot、白名单、客户端原生proxy和完整协议往返夹具，默认`action_phantoms.aptrgangr_actions=true`。原生Makeparticle的地块尘与Ring参数通过服务端位置发送；声音沿原生位置。没有运行客户端Boss AI或生成Boss音乐／防御。

## 获取

两项各使用`cataclysm:strange_key`1＋魔源宝石1，27经验的真正Ars抄写配方。实际冥行武弁固定掉钥匙1，两次击杀学齐；第三次可保留钥匙用于本体进度。图标分别运行时引用`black_steel_axe`和`strange_key`模型，两者均已核对路径存在，不复制素材。按新APTRGANGR系列分组，斧刃仍属于增强末尾的弹射物替换组。

## 验证

- 集中命令`:runGameTestServer :verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_aptrgangr --offline --console=plain`，**7/7通过，BUILD SUCCESSFUL**，证据位于工作区`dev58-tests.log`。
- 覆盖原生斧刃推力／惯性／透明度／寿命，普通穿透伤害／真实来源／友军／无敌帧失败／同目标续接去重，重复形态180tick原生视觉不失效／无双重位移／逐次扣费／空蓝停止。
- 覆盖砸地第15tick原生近战与五刃、原生Ring粒子包、一次收费、增强、真实owner、自然动作结束后子刃继续；友军实际视角、跨系列打断与失败不打断、资格撤销、拒绝敌方、创造零蓝、所有者消失清理。
- 全体原生幻影协议往返与已有炽燃骨弹共享伤害／运动回归通过。95种魔符中英文名称／描述及发行／源码包检查通过，没有上游媒体或实现。
- 固定Core工作树干净；测试服务已退出，仅剩Gradle daemon（本次检查PID11384）。没有启动客户端，也没有重复无关附属矩阵。斧刃外观、姿态、砸地幻影和粒子／声音仍需人工确认。

## 产物

- `cataclysm-nouveau-0.1.0-dev.58-mc1.21.1-neoforge.jar`
  SHA256 `c1fef917ad5821bc7d4c7c66c75a26afd29733f938450df4e39cfb83ab5cf461`
- `cataclysm-nouveau-0.1.0-dev.58-sources.jar`
  SHA256 `8b1462adb78f2177c44bc07d2d06cf2c5b7d541e70c40397376a217807aa94d7`
