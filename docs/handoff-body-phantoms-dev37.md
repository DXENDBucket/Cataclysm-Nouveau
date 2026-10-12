# 身体行动幻影扩展交接（dev.37）

## 范围

- 用户要求：灾变其他既有体术补充原生幻影，第一人称更透明。
- 工作树：`C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-action-phantoms\Cataclysm-Nouveau`；分支 `feat/action-phantoms`。
- 固定 Conflux：`030880436d93ae104d6e14ba0240b6b1ba698513`（0.1.21）；副本 `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-action-phantoms\Ars-Conflux`，受版本控制文件无变化。
- NeoForge 1.21.1 / 21.1.250；灾变3.33；JDK21。未修改 Core、Iron 或其他附属；没有 Forge Cataclysm 移植。

## 已完成

新增十个身体行动的透明原生模型幻影，详见 [说明](body-action-phantoms-dev37.md)。客户端有每系列独立开关，默认开启。第一人称 opacity 从0.14降至0.10，其他视角0.28和比例1保留。服务器仅采样原生姿态，未注册渲染代理不 tick、不施法、不加入世界。

本体核对：实际发布JAR的 Scylla WhipAndSpearGoal / Scylla_Flying、Guardian 原生动画及模型、Harbinger模型和渲染层、Monstrosity内部动画。雷电连段状态为25，非24。两类原生动画分别同步。结束包先于原生goal清理，动画已清空则保留最后有效姿态。

下一步：在万法归一人工检查四系列动画、第一人称可见度和多层透明效果。暂不更改资源成本、冷却、伤害、选点或动作时机。

## 验证证据

命令均在上述工作树执行，`JAVA_HOME=C:/Program Files/Java/jdk-21`。

- `./gradlew.bat :compileJava :compileGameTestJava --offline --console=plain` 通过。
- 新测试namespace最初缺模板，后续误用带冒号模板名；这两次启动失败未执行测试，修正为各namespace独立empty模板。
- `./gradlew.bat :runGameTestServer :build -PgameTestNamespaces=cataclysm_nouveau_phantoms --offline --console=plain`：实际16项，15通过；雷电连段首次同步失败。记录 `build/phantom-validation.log`。
- 更正连段状态为原生25；补充动画清空和执行者移除的关闭检查。`./gradlew.bat :runGameTestServer :build -PgameTestNamespaces=cataclysm_nouveau_phantom_focused --offline --console=plain`：3/3通过，最终构建成功。记录 `build/phantom-focused-validation.log`。覆盖17个不同检查；未声称一次完整17/17运行。
- 验证内容：十个真实行动的启动、四tick更新、付费/占用不变、跨系列替换关闭、完整支持姿态协议、焰魔原功能、实际落地和碰撞回归。未重跑其他系列全矩阵。
- `verifyDistributions` 二进制包/源码包均通过，未打包灾变素材、源码实现或依赖；`git diff --check` 通过。
- 专用服务器启动并跑测，未发生客户端类加载问题；没有客户端人工画面验证。
- 固定 Core `git status --porcelain --untracked-files=no` 为空。

## 交付

版本 `0.1.0-dev.37`；代码提交 `91a4a495f73926b61fc013ba4b9423b18c29507a`，标签 `v0.1.0-dev.37`。

- `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-action-phantoms\Cataclysm-Nouveau\build\libs\cataclysm-nouveau-0.1.0-dev.37-mc1.21.1-neoforge.jar`
  SHA-256：`be0ba50e0b979223ba91bbcbef82ef0e04209de4afbab6025bcc022ebcac8c10`
- `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-action-phantoms\Cataclysm-Nouveau\build\libs\cataclysm-nouveau-0.1.0-dev.37-sources.jar`
  SHA-256：`fdefe1cbe98edf99ec4f76ed280eb8aa189241b613b01d098938596b040e961e`

已由根协调者串行部署到 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，只有dev.37的一个Cataclysm Nouveau JAR；复制后哈希一致。Core0.1.21与Iron0.17.18保留。原dev.36及旧客户端配置备份到实例 `codex-backups/cat-phantoms-dev37`。仅旧默认0.14的第一人称项改为0.10，第三人称及其他配置保留，并验证TOML可解析。未修改万途归零。

没有推送或发布。当前没有其他agent运行。测试服务已自然退出；只结束经所有构建工作目录确认属于本工作区的空闲Gradle daemon，不清理其他Java进程。

## 后续开场

> 继续本附属时先读 AGENTS.md、固定Core的开发流程和本记录；核对实际Git状态及Core基线。不要把服务端通过当成客户端画面验收，避免重复已通过检查。普通附属任务不改Core受版本控制文件，共享实例仅由协调者部署。
