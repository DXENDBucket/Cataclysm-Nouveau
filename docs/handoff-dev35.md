# 焰魔动作推广交付

2026-10-13，Cataclysm & Nouveau `0.1.0-dev.35`，Minecraft1.21.1 NeoForge。代码提交 `00dd727814e4a7d05119fc960f1dc93bfe1d7ac5`，本地标签 `v0.1.0-dev.35`。独立工作树 `parallel-workspaces/cataclysm-ignis-actions/Cataclysm-Nouveau`，分支 `feat/ignis-action-anchors`。固定Core `a14acc36b10c933eb3447afb0f7ed540e7c3365b`（0.1.20），源码副本受控文件未修改，本体仍为灾变NeoForge3.33。

四个既有物理动作迁移到Core锚点：普通跃砸、强化跃砸、地动三连和终极焰斩。剑舞与它们共享附属内的水平瞄准辅助，不将Boss窗口和运动规则塞入Core。实体继续原生跳跃／着地，位置输出固定点准备及打击；自身／友军授权持续复查，拒绝敌方实体和MISS。所有阶段复用原生Goal／aiStep，费用、冷却、伤害、阶段与材料不调整。幻影覆盖十个原生动画阶段，独立新开关默认开启；空中等待允许显示末帧直到实际落地或动作过期。

取消只清理已取得的本会话动作状态及本调用帧的子火球／火场，不清理同一玩家独立创建的其他法术；自然结束的子攻击保留原生寿命。子火球追踪真实目标，临时瞄准输入不会成为追踪敌人。

## 验证

- 实际灾变3.33发布JAR的 `Air_Smash`、`Reinforced_Air_Smash`、`Earth_Shudders`、终极Goal与动画常量经javap核对；没有复制灾变实现或素材。
- `build-ignis-actions-release.log`：14项required全部通过，含10项选定的原生动作／剑舞回归及4项新集成用例。覆盖四种动作的固定点伤害／友军／单次费用／自然结束、原生子火球阶段、授权取消、实际占用、独立共享引导、取消仅删自身子攻击、玩家窗口转向／锁向与视觉载荷往返。
- 初轮12/14通过。后续修正两处测试预期：震地原生冲击也伤及后方，用前方额外近战伤害验证方向；假玩家需登记原版广播接收列表才能验证远端关闭包。一次错误使用只读玩家列表的测试补丁已撤回，最终测试专用登记按作用域清理，不进入发行包。
- 最后客户端等待进度校验只改接收上限，不改变服务端；`build-ignis-actions-package.log`编译／打包成功，没有为此重启完整服务器。
- 两份 `verifyDistributions` 均通过，无灾变资源、代码、依赖或测试类打包。未启动图形客户端，实际四种幻影的模型、插值及优化模组环境仍需人工确认；服务端与包检查不等于视觉验收。
- Core副本 `git status --short` 干净，全部本次测试工具会话已收取，未留下Java进程。

## 产物与部署

- 二进制 `build/libs/cataclysm-nouveau-0.1.0-dev.35-mc1.21.1-neoforge.jar`，SHA256 `f31b8aaf1a1baf901074a7f85ee2fe4d2485456feff1610ce4f39e0744ecb44a`。
- 源码 `build/libs/cataclysm-nouveau-0.1.0-dev.35-sources.jar`，SHA256 `3563c5c354f2065ea65fa81d78ce0c9182d02d813181535634a4cd75da2f23a3`。
- 已串行部署到万法归一，旧dev.34保存至实例 `mod-backups/2026-10-13-ignis-actions`；复制哈希和单包数量核对通过。Core0.1.20／Iron0.17.17及其他附属保持已有包。
- 没有修改万途归零或Forge测试实例，没有推送或发布。灾变附属仍未实现Forge版本。

斯库拉、先驱者、末影守卫完整动作迁移以及其他附属／跨维度组合继续留后续。独立剑刃阵、五火球阵和烈焰打击选点语义不在本批推广范围。
