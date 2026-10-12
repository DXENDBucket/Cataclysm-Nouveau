# 先驱者第二批与受限比例增强（dev.20）

## 范围与基线

- 用户授权：冲锋撞击、榴弹齐射、追踪导弹连射、宽松的独立烟云；明确不做死亡自爆、半血免疫与自动回血。后续要求所有灾变伤害增强/削弱改为受限百分比，最终比例20%、下限2、上限10。
- 工作树 `parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 planning/scylla-close-harbinger；起点8b73425；交付标签v0.1.0-dev.20。
- 固定 Core `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件未改。NeoForge1.21.1、Ars5.13.1、Cataclysm3.33、Java21，无Forge交付。无远程推送。

## 实现

- HarbingerActions / SpellHarbinger：原生goal调用帧、实际角色与资源分离、原生齐射时序和冲锋碰撞、半血快照、子载荷独立生命周期。
- 原生goal及动画Mixin均只对脱离世界的SpellHarbinger生效。发射重绑所有者；捕获动画请求避免发送未出生BossID。碰撞保留NativeSpellDamage与友军判定，阻止地形破坏。
- 独立烟云沿用原生烟云和dev.19同步noCulling保护；40Mana/1秒，支持时效修饰。子云权限由真正来源能力决定，不硬依赖单发榴弹魔符是否启用。
- 新四项默认费用180/400/240/40，冷却4/4/3/1秒。先驱者全九项13锭，两次击杀保底可完成，包括保留两把武器时的17锭。
- DamageAugmented / DamageAugmentationStateMixin：服务器端单份修饰倍率，原生基础字段不预先增强；NativeArsDamageMixin统一按请求基准结算，再交Core属性/事件链。水矛、雷矛具体类也拦截；爆炸计算器同样用自身基准修饰。
- Raw增长计数与伤害分离；移除旧SpikeDamageMixin及旧Boss平坦加法，子实体直接继承倍率。状态等级/治疗规则未改，不影响原版非联动实体。

## 验证

- `:runGameTestServer :build --offline --console=plain --no-daemon`，最终build-harbinger-actions-final.log：50项服务端检查通过，39个魔符本地化验证、发行与源码包verifyDistributions通过。
- 新用例：伤害下限/比例/上限/多次加算/削弱归零和装备顺序；两轮10/14榴弹计数/间隔/炮口/归属；六枚导弹普通与半血成对时序；友军冲锋实际运动/原生伤害事件/半血接续/不破坏地形/运动清理；独立烟云时效/扣费/增强与根所有者清理。
- 修正验证中发现的水矛具体类hurt入口漏拦截；调整旧测试区分原生基础字段与实际增强伤害。临时配置先有15%，最终隔离实例改为20%后再验证通过。
- 基础独立环境，无新增可选矩阵。现有桥接测试未加载可选依赖时按设计跳过。未启动客户端人工验证；普通Boss与伤害原生隔离已有检查。
- 最终构建/测试进程自然结束，不终止用户游戏或其他任务的Java。

## 交付

- 部署万法归一mods，旧dev.19备份到实例mod-backups/2026-10-12-harbinger-dev20。Core保持原安装版本。
- 许可仍MIT；发行与源码包不含灾变素材、源码实现或依赖JAR。

产物SHA-256：
- `build/libs/cataclysm-nouveau-0.1.0-dev.20-mc1.21.1-neoforge.jar`: `c0bbca3a7384f99bafb1035385f7d86d7dad5ed1438d34e707636ee331428bf1`
- `build/libs/cataclysm-nouveau-0.1.0-dev.20-sources.jar`: `69d1df347b813b84402f35b9db9b5ebb78910287044023c7504a6eed22b0799d`
