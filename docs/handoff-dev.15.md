# dev.15：Ars 伤害成长与钩锚修正

- 固定 Core 0.1.16：`b24f18d72e64a902ff6c966040b0b83ea3cc6226`，其受版本控制文件未改。
- MC 1.21.1 NeoForge；Cataclysm 3.33；Ars 5.13.1；真实可选环境 Ars ’n Spells 3.3.5／Iron 3.16.3。
- 本轮开始于 dev.14 提交 `47c39d7`；本文件所在提交为最终功能提交。

## 行为

原生联动伤害固定加入施法者 Ars SPELL_DAMAGE_BONUS，再经过 Ars Pre／成功 Post。来源类型、破盾、无敌帧与子攻击保留。只给明确绑定上下文的攻击加成，未修改普通 Boss／弹射物／爆炸。延迟子效果保留根施法者 Ars 上下文；事件可以修改或取消伤害，避免重复读取同一层属性／桥接。自然火／中毒及汲取回血未新增伤害属性成长。

数量及原生生命比例参数和上限不变；外部 Pre 如果倍增整次伤害，会包含原生比例分量。基础费用不改。不依赖 Iron 或 Ars ’n Spells；后者走自己的 Ars 事件，不手动重叠读取法强。

钩锚在实际抛出时重采样当前朝向，直线且无重力的原生玩家战锚，去掉旧落点导致的向下角度和抛物线补偿。默认 64 格可配置；只读取加载区域，等待钩锚时暂缓下降，牵引运动先完成再判断落地。空中受益者不再被着地拒绝，动作临时接管 Mob AI 与重力，结束／取消恢复原 noGravity，不改 NoAI 标记。

## 验证

`:runGameTestServer :build -PwithArsSpells=true --offline --console=plain --no-daemon` 最终通过，日志 `build/ars-growth-delivery.log`。

41 项 GameTest 全部通过。新增覆盖一次固定加伤／Pre／成功 Post／取消、普通原生攻击隔离、延迟原生喷柱上下文、真实 Ars ’n Spells 的海浪 9→18 单次桥接，以及真正 Iron SummonedSwordEntity 接受空中动作、抛出时朝向、链条清理和浮空恢复。原有连段／挥锚／落地雷暴等回归通过。

早期共存验证的失败主要来自测试魔力池被 Ars ’n Spells 默认统一模式压到 100，已在独立 run/gametest 配置关闭统一，保留伤害桥接；没有修改人工实例配置。一个落地雷暴测试的有限石地面不足以承载边缘落雷，改为更陡的下瞄角确保所有原生列都有地面；未改原生缺地面不生成规则。

本地化、发行／源码包 no-upstream-media/code 检查通过。客户端画面与实际玩家操控未人工确认；飞剑验证是服务端真实实体，不是将普通牛简单称为飞剑。

## 产物

- `build/libs/cataclysm-nouveau-0.1.0-dev.15-mc1.21.1-neoforge.jar`
  - SHA256 `2913b08d8cb181af91e0da0e97536adabf2c40bdfb91650f662a381f025d223c`
- `build/libs/cataclysm-nouveau-0.1.0-dev.15-sources.jar`
  - SHA256 `d7d4fd70282bc2b1b582f4fe0b917445c3931dd048d4d65cd1347b3eec395977`

工作树：`parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`。本地提交／标签后由协调者串行部署；不推送或发布远程。
