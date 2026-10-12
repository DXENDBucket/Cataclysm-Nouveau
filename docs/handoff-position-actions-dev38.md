# 位置身体行动交接（dev.38）

## 范围

- 用户指出上次新增幻影的动作仍不能以位置＋方向执行；已补齐同批十个动作的执行入口、原生攻击和客户端位置锚点。
- 分支：`feat/position-actions`；工作树 `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-position-actions\Cataclysm-Nouveau`。
- 固定Core：`030880436d93ae104d6e14ba0240b6b1ba698513`，副本 `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-position-actions\Ars-Conflux`，受版本控制文件无变化。
- NeoForge1.21.1/21.1.250、灾变实际发布3.33、JDK21。只修改Cataclysm附属，没有Forge移植或Core/API改动。

## 实现与后续

详见[固定位置说明](position-body-actions-dev38.md)。`PositionBodyActions`使用现有ExecutionAnchor创建无执行实体的固定原生攻击帧。旧实体路径保留，方块目标优先进入位置路径。入射方向优先，位置/方向冻结，不移动/传送付款者，不取得身体声明，独立位置可以并存。跃落/空降结束准备后在该点执行原生落地攻击；没有实际身体的位移/抛锚/拉近不执行。持续状态增益仍需要实体。

BodyPhantomPayload协议2新增positionAnchored，固定点ID -1/UUID零；空executor不再回退为付款者。守卫原生传送仅对显式固定位置帧跳过，实体传送事件/安全落点逻辑保留。中英文十种魔符描述补充位置触发说明。

下一步为客户端画面确认，以及后续按实际新需求继续收拢身体行动。不要把位置版移动省略误认为服务端攻击未执行，也不要把服务端通过当成客户端已验收。

## 验证证据

- 工作目录为上述隔离工作树，JAVA_HOME `C:/Program Files/Java/jdk-21`。
- `./gradlew.bat :compileJava :compileGameTestJava --offline --console=plain` 通过，记录 `build/position-compile.log`。
- `./gradlew.bat :runGameTestServer :build -PgameTestNamespaces=cataclysm_nouveau_positions --offline --console=plain`：17/17一次通过；记录 `build/position-validation.log`。
- 覆盖十个真实位置动作在远处造成伤害、归属原施法者、保护友军、单次付费、冻结位置/朝向、付款者运动/重力不变、结束包与协议往返；位置/实体并存、无效点退款、不加载新区域及根死亡清理；守卫实体闪现/落地、跨系列打断、焰魔位置与实体视觉协议回归。
- 后续只补本地化，`./gradlew.bat :build --offline --console=plain` 通过，未重复GameTest；记录 `build/position-final-package.log`。
- 二进制包与源码包verifyDistributions通过，无本体素材、实现源码或依赖打包。固定Core git status为空，git diff --check通过。
- 无客户端人工画面验证。没有运行额外完整联动矩阵。

## 交付

版本0.1.0-dev.38；实现提交4cb2376及说明文本提交 `0d9aba597f98b06ad244bb6ab8dbf0cb57e41897`，本地标签v0.1.0-dev.38指向后者。未推送或发布。

- `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-position-actions\Cataclysm-Nouveau\build\libs\cataclysm-nouveau-0.1.0-dev.38-mc1.21.1-neoforge.jar`
  SHA-256：`7a2400fe42d9e8d2653f47444b6a367c197a86bb409567905e2571dbce40da0b`
- `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-position-actions\Cataclysm-Nouveau\build\libs\cataclysm-nouveau-0.1.0-dev.38-sources.jar`
  SHA-256：`e5f1ba3f40627cc665dc74354fa5334869c63fc2a0ae897b34ee0a69d5f8ba07`

由根协调者部署到万法归一mods，仅一个dev.38，复制哈希一致。dev.37备份在实例codex-backups/cat-position-actions-dev38。Core0.1.21、Iron0.17.18及客户端10%第一人称配置保留。没有修改万途归零。测试服务器已退出，只结束经所有构建工作目录确认属于本任务的空闲Gradle daemon。

后续agent先读AGENTS.md、固定Core开发流程与本记录，核对Git和基线；普通附属任务不改Core受版本控制文件，独立工作树构建，共享实例由协调者部署。仅新改动或明确疑点补验证。
