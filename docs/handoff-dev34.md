# 灾变dev34执行锚点迁移

2026-10-13，工作树 `parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`。固定Core `c4ed6f50f853b20da06246ed32e66cdb2880bb57`（0.1.20），源码副本未修改；灾变本体仍为NeoForge3.33。仅支持NeoForge1.21.1。

剑舞移除本地ActionAnchor，共用Core ExecutionAnchor和ExecutionClaim。自身／友军权限默认只参照原施法者，持续复查；位置模式executor=null，不占用付款者。保留水平转向窗口、20°/tick、调用框、两段原生动画、伤害和费用。准备失败不清理其他动作的actor状态。既有物理动作仅补实际actor声明，其他选点语义没有全部重构。

- `build-core-anchor-migration.log`：81项required通过，含旧80项和Core声明／独立伤害归属／解除友军／旧物理动作冲突／共享引导并存。
- `build-anchor-coexistence.log`：加载真实Iron0.17.17，再运行1项专门共存用例，通过；两种真实能力同一友军执行并共同取消。`-PwithIron=true -PgameTestNamespaces=cataclysm_nouveau_coexist`，依赖只在忽略的 `libs/iron-coexistence`。
- 最终固定c4ed6f5之后 `build-anchor-package-current.log` 构建成功；binary/source verifyDistributions均通过，无任何灾变素材或实现打包。最终Core新增中立授权入口未改本附属使用的便利方法。
- 二进制 `build/libs/cataclysm-nouveau-0.1.0-dev.34-mc1.21.1-neoforge.jar`，SHA256 `853a200e1c7d17b1898e7cd61f3ca504862bd8f9af3020cf37270f05fe9a33f2`。
- 源码 `build/libs/cataclysm-nouveau-0.1.0-dev.34-sources.jar`，SHA256 `71f8f1cbca2f7afa598c7982391110f258b53ba6959110bf00baf03b66c84331`。
- 串行部署万法归一，同时换Core／Iron；旧包保存至实例 `mod-backups/2026-10-13-execution-anchors`。没有修改万途归零实例，未推送或发布。

未重开图形客户端；用户对dev33幻影的反馈不等于dev34混合施法端点／动画已人工验收。所有本次启动的测试进程已结束。
