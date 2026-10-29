# Forge 正式命名回归修复 / 0.1.3

## 范围与原因

用户反馈 Forge 发布包中的 `WaveStateMixin` 和 `IgnisHealthDamageMixin` 找不到正式方法。直接检查 0.1.2 发行包确认：refmap 存在，但缺少海浪 `move` 调用和焰魔 `positionRider` 选择器的映射。

Forge 源码生成器原先只为列出的 Minecraft 覆盖方法启用重映射。这份列表漏项，使注入保留开发名；开发运行环境会重新映射依赖，因此不能发现相同的正式包错误。

本批修复：

- 海浪 `move` → `m_6478_`；焰魔 `positionRider` → `m_19956_`。
- 烟云 `onSyncedDataUpdated` → `m_7350_`；利维坦目标的 `start` → `m_8056_`。
- 焰魔火球、深渊火球与烈焰冲击的 `IgnisExplosion.explode` 调用显式启用映射，目标为 `m_46061_`。不把 `explode` 机械套进所有原生选择器：深渊水雷同名的自定义方法仍保持原名。
- 正式客户端首次启动另外暴露海锚的 `@Invoker` 缺映射。Forge 覆盖接口改为在 `AbstractArrow` 上生成受保护 `onHitBlock` 的代理，映射为 `m_8060_`；虚调用继续分派到海锚自身的原生实现。没有为普通箭新增攻击或 tick 行为。
- 编译任务登记原始 refmap、归档副本和额外 TSRG 输出，确保 Gradle 缓存恢复完整。

未修改法术规则、费用、冷却、配方、模型或原生素材。固定 Core 为 `d8431a722bf439c937ae8e3e355a9688a33e698d`（0.1.38），其受版本控制文件保持不变。

## 可持续的检查

`forge/tools/ProductionMixinAudit.java` 是构建工具，不进入模组 JAR。它用 ASM 读取本附属最终重混淆包、实际原模组发布 JAR 和 Forge 的 SRG Minecraft，应用本附属 refmap 后检查：

- 111 个配置中的 Mixin；
- 420 组方法选择器；
- 272 处 INVOKE／FIELD 注入点；
- 49 处 invoker、accessor 与 shadow 字段。

该检查已接入 `verifyDistributions`。多实体适配中显式 `require=0` 的未匹配挂钩单独记录；它们不被冒充为已成功应用。检查不执行完整 Mixin 转换，不覆盖 NEW 的构造器签名、局部变量捕获、切片、所有动态分支或客户端画面，仍需要实际启动。

## 实际验证

- 原 0.1.2 包被生产命名检查拒绝，包含反馈中的两个位置及烟云、爆炸和利维坦同类遗漏。
- 修复包生产检查与素材／许可／102 个魔符配方检查通过。
- 清空本附属构建目录后，明确出现 `compileJava FROM-CACHE`，映射、重混淆包和生产检查均通过。
- 已用**最终 Forge 发行 JAR**启动 Java 17 / Forge 47.4.26 / Minecraft 1.20.1 客户端，加载灾变 3.31、Ars 4.12.7、Conflux 0.1.38 及所需前置。该环境使用正式 `srg` 命名，未启用开发 refmap 重映射。
- 客户端成功进入主菜单并进入新单人世界；日志确认玩家加入、世界正常保存和正常退出。首次失败的海锚访问器日志亦保留，最终启动日志没有相同 Mixin 崩溃。
- 测试实例位于隔离工作区的 `test-instance`，使用本地测试身份；本次启动的 Java 进程已退出。没有改动共享“万法归一”或原 Forge 测试实例。
- NeoForge 版本号同步为 0.1.3，打包与发行内容检查通过；本轮没有另开 NeoForge 客户端，也没有逐一战斗测试 102 个法术。

产物哈希、命名检查和客户端证据路径见 [构建记录](forge-production-mappings-build.json)。修复交付时尚未上传 CurseForge，之后已获用户授权上传；未推送 Git。


## 发布回执

2026-10-29，复用已验证的双版本发行包上传项目 `1767646`。NeoForge 1.21.1 文件 ID `9359036`，Forge 1.20.1 文件 ID `9359037`，两项台账均为 `uploaded`，Release，审核通过后自动发布。API 接收不等于审核已完成，见 [发布回执](curseforge-0.1.3.json)。上传阶段没有重建产物、重复测试或修改共享游戏实例。
