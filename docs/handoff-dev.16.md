# Cataclysm & Nouveau dev.16：接入 Core 伤害 API

- 工作树：`parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`，分支 `agent/cataclysm-monstrosity-full`。
- 起点：`752431c`（dev.15）。
- 固定 Conflux：`dab8b9f6f1f92cfa0a78d56768f6a01a57901714`（0.1.18）。构建使用 `-PconfluxCheckout=../conflux-native-damage/Ars-Conflux` 的实际绝对路径；没有改旧副本的受版本控制文件。
- 公共伤害／事件／上下文附件与同步爆炸作用域改用 NativeSpellDamage；仅保留 `NativeDamageContexts` 对灾变原生施法帧的特有查询以及原生实体伤害钩子。删除 ArsDamage 和 ArsExplosionDamageMixin。
- 最低依赖提升到 Conflux 0.1.18；不改变基础伤害、百分比项、费用、冷却及动作时序。
- `runGameTestServer build -PwithArsSpells=true`：41/41 通过，实际 Ars ’n Spells 桥接海浪只翻倍一次；`verifyDistributions` 二进制与源码包均通过。日志 `build/core-damage-extraction.log`。
- 客户端外观未重新人工测试。本次只重构伤害通路。

## 产物 SHA-256

- `cataclysm-nouveau-0.1.0-dev.16-mc1.21.1-neoforge.jar`：`4b3c2f414fa1c589c65cc0111e9e5b2de5b37eb347c6609dc15f8da8d2c5b1d3`
- `cataclysm-nouveau-0.1.0-dev.16-sources.jar`：`c607e39c652b26fc4a6774b98bc95494190e27eb8918be02eca6a671843e8e19`
