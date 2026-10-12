# 身体行动打断交付

2026-10-13，灾变dev.36，代码 `4a1b3f14b7ecd6497f844065b4ca24a8cec05fbd`，本地标签 `v0.1.0-dev.36`，工作树 `parallel-workspaces/cataclysm-action-interruption/Cataclysm-Nouveau`。固定Core `884fd49b81b8d30e56432cbdef93060705bc1613`（0.1.21），受控副本干净。本体NeoForge1.21.1灾变3.33；没有制作Forge灾变附属。

已有各系列身体动作选择Core startReplacing，只切换实际actor的冲突会话。有效性和设备条件先预检，费用按旧事务预留；失败保留旧行动。旧清理之后才采样重力初值，准备对象不解除别人的锁或改变速度。巨兽肩撞增加明确身体声明；共享引导及其他actor独立。旧费用不退、新费用独立，原冷却和原生攻击不调整。连续Ars身体效果以后触发者为准，仍逐次收费。

钩锚选点另修一处实际漏判：扫描完全没有命中时返回MISS，却被作为有效表面启动动作并扣费。现在必须有真实BLOCK；发射时重采样同样遵守此规则。

## 验证

- `build-action-interruption.log`：28项运行，27项required通过；含原动作／剑舞回归、跨系列切换、费用、状态所有权、共享引导、其他actor、真实Iron0.17.18绑定武器API。无效钩锚用例失败后隔离了场景，继续定位到上述MISS漏判，没有放宽断言使代码通过。
- 修复后 `build-action-interruption-hooks.log`：无效目标保留旧动作的1项通过。`build-action-interruption-hook-regression.log`：无效选点及真实落地链路通过，额外真实飞剑用例失败；其旧fixture缺少跳跃后视线范围内的真实地面，并允许原生AI改写受控瞄准。固定测试AI、补地面后 `build-action-interruption-flying.log`：1项真实Iron飞剑跳跃／重采样／重力恢复通过；没有修改生产AI或恢复MISS行为。
- 恢复完整29项wrapper后 `build-interruption-test-layout.log`：测试编译成功，没有再重跑已经通过的无关项目。完整29项不是在单次最终运行中全部重测，以上日志合起来记录当前覆盖范围。
- 最终发行／源码verifyDistributions均通过，未打包原模组资源、实现、依赖或测试类。可选测试依赖仅在忽略的libs/iron-coexistence；普通发行不新增Iron硬依赖。
- 未启动图形客户端；快速打断时的淡出和新幻影混合仍需人工观察。斯库拉等系列的完整位置锚点／幻影推广仍留后续，不由此次打断自动重写。

## 产物与部署

- 二进制 `build/libs/cataclysm-nouveau-0.1.0-dev.36-mc1.21.1-neoforge.jar` SHA256 `b1c9491285846bba64f53e94b5311f3c9b75b67145f12fa0759022937992aa60`。
- 源码 `build/libs/cataclysm-nouveau-0.1.0-dev.36-sources.jar` SHA256 `bbceb6748bf78b9cfb368f1ac1cd3202ab2d86fb27ce6210219915d50b9b30f8`。
- 已与Core0.1.21／Iron0.17.18串行部署万法归一，旧包保存实例 `mod-backups/2026-10-13-action-interruption`，复制哈希一致。Forge实例只更新Core／Iron；没有修改万途归零。
- 本地提交与标签已经创建，未推送或发布。所有本次GameTest工具会话已收取。
