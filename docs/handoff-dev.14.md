# dev.14：斯库拉动作与水面行走

- Minecraft 1.21.1 NeoForge；Cataclysm 3.33；附属 0.1.0-dev.14。
- 固定 Core 0.1.16：`b24f18d72e64a902ff6c966040b0b83ea3cc6226`。受版本控制文件未改；人工测试实例继续使用兼容的 0.1.17。
- 工作树：`parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`；分支 `agent/cataclysm-monstrosity-full`。功能提交以本文件所在提交为准。

## 最终行为

雷电重击连段、旋转挥锚、钩锚跃落、水面行走均为效果魔符，对自身／友军有效。原施法者付款、获得伤害归属，受益者执行动作。连段复用原生 WhipAndSpearGoal：两次前方重击、32 电火花、五支水矛。旋转调用原生状态 7 的三次环形攻击，期间逐 tick 旋风击退排除友军。

钩锚复用 Scylla_Flying 的起跳冲量，以原生 Player_Ceraunus_Entity 显示实际受益者的链条。附属控制碰撞受限牵引和落地衔接；原生 Boss 抓取／骑乘／镜头不适用于通用生物，因此不套用这些行为。落地复用状态 17 的 35 主落雷和两枚火花，火花落地子攻击继续执行。要求着地、非乘客、视线 32 格内至少两格外有方块；最多约 8 秒，异常结束清理锚和动作锁。

水面行走基础 30 秒，±50% 基准时效增强，只支撑水。沿用熔岩行走表面着地／浮起／潜行规则，不改变永久重力标记，也不提供熔岩保护。状态图标借用原版海豚的恩惠。挥锚与跃落魔符图标委托原生锚戟物品渲染。

额外 Mana / 主动冷却：700 / 18 秒、300 / 12 秒、600 / 15 秒、60 / 无。整段动作付一次，子攻击不重收；创造沿用 Conflux 费用与冷却绕过。连段消耗精华 1，其他三项各泪宝石 1；十六项相关魔符合计精华 7、泪宝石 13，含被消耗锚戟的间接材料，三次击杀保底足够；另保留锚戟需要精华 9、泪宝石 16，三次仍足够。

## 验证

- 最终 `:runGameTestServer :build --offline --console=plain --no-daemon` 通过，日志 `build/scylla-actions-verification-final.log`。
- 全部 37 项 GameTest 通过，新增四项覆盖动作完整次数、近战及子投射物真实归属、友军伤害／推力过滤、单次扣费与忙碌退款、所有者离开释放锁、水面时效／实际碰撞／跳跃／潜行／自然失效，以及实体真实起跳、牵引、链条清理、35 主落雷的落地爆发。
- 首次验证发现多目标 Mixin 的 Shadow 必须显式 remap=false，已修复；Stormknockback 的真实目标是 getEntitiesNearby。第二次剩余失败是测试将两枚火花落地的落雷并入 35 主落雷；最终按原生 delay 分开计数，没有删掉子攻击。
- 本地化检查：30 魔符的 en_us / zh_cn 均通过。
- verifyDistributions 同时检查发行与源码包，无上游媒体、实现源码或依赖 JAR。
- 未启动客户端人工验证；锚／链条／粒子与玩家操控手感仍待确认。未声明可选 Ars 附属全矩阵测试，也未改变 Core。

## 产物

- `build/libs/cataclysm-nouveau-0.1.0-dev.14-mc1.21.1-neoforge.jar`
  - SHA256 `5dbb20a1bd60583bec76196a083490781cac7c5b3e787623bb2fd6021e778f9a`
- `build/libs/cataclysm-nouveau-0.1.0-dev.14-sources.jar`
  - SHA256 `0578280fe04094c893f8ef9df14c9c052890bdd175c3a1d9c3826529f8c4a671`

由协调者串行部署“万法归一”；dev.13 备份至 `.codex-backups/cataclysm-nouveau/pre-dev.14`。本地提交并标记 `v0.1.0-dev.14`，不推送或发布远程。
