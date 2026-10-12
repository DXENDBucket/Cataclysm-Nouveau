# 斯库拉位置动作特效修复（dev.43）

固定 Core：0.1.24，`08c307f3238b9b9aa7fd628b9771cd287160448d`，副本受版本控制文件未变化。隔离工作树 `parallel-workspaces/cross-audit-cataclysm-nouveau/Cataclysm-Nouveau`，分支 `fix/scylla-position-visuals`。

## 问题与改动

固定位置旋转挥锚的服务端分支只发通用冲击粒子，遗漏实体分支的三次 ScyllaSwing 弧光与九次 Storm 拖尾。位置雷电连段／跃落同样没有复用完整原生视觉发送。现在两种执行角色共用 ScyllaActionVisuals，原生伤害和动作时序保留。

实装灾变 3.33 的 StormParticle.tick 在找不到跟随实体时立即 remove，不能仅用 EntityId=-1 发原生粒子。新增位置专用 SimpleParticleType，客户端直接扩展本体 StormParticle 的位置读取，使用一个从不加入世界、不 tick、不渲染的私有视觉锚点；轨迹中心固定，旋转、原生材质、拖尾采样、淡出和自然到期沿用本体。不会给付款者创建执行角色或身体声明，也不改变伤害、费用或冷却。

## 验证

NeoForge 1.21.1 / Java 21 五项 GameTest 通过：固定位置挥锚、实体挥锚、固定位置连段的真实客户端粒子包；既有固定位置伤害／归属／费用／付款者状态回归；既有实体旋转伤害／友军／生命周期回归。视觉用例核对三次旋转弧光、九次旋风发送、连段两次弧光及实际位置和 yaw。涉及有延迟的 Arena 回归按独立 batch 执行，避免相邻测试的清理范围相互删除实体。

发行包、源码包构建及 verifyDistributions 通过，许可与测试类排除检查通过；没有包含灾变素材或本体实现代码。未启动客户端，实际旋风显示、多人观察仍待人工验收。原生粒子子类的客户端生命周期依据实际 JAR 的公开实现核对，服务端测试不能替代客户端运行。

## 产物

`cataclysm-nouveau-0.1.0-dev.43-mc1.21.1-neoforge.jar`

SHA-256：`1605eaab81b2742b2eb0b5dd955513e054f573013798799e50529685a8c53d07`

`cataclysm-nouveau-0.1.0-dev.43-sources.jar`

SHA-256：`56a647c472c78e288c37b9aaf9f405b7cd85cbcc5b19b17e3cde3cd9cb0b55d4`

## 本地部署与版本控制

修复提交 `8cee6caf9b79c99622cf42e6fa17641c349a32ef`，本地主分支及标签 `v0.1.0-dev.43`。已仅替换“万法归一”的 Cataclysm-Nouveau JAR，复制后 SHA-256 一致。旧包备份 `conflux-backups/20261013-074711-scylla-visuals/`。Core 和用户配置未修改。没有推送或公开发布。
