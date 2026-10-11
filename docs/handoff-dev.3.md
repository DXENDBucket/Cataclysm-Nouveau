# dev.3 交接

- 目标：Minecraft 1.21.1 NeoForge，Cataclysm 3.33。
- 附属分支：`agent/cataclysm-ground-effects`，完成后快进合入主分支；发行提交见 `v0.1.0-dev.3`。
- 固定 Core：`3eccd65fc569cd43ef960ef3eee77963ef201ac8`（API 0.1.15）。Core 受版本控制文件无变化。
- 独立构建工作区：`parallel-workspaces/cataclysm-ground-effects/`。
- 新增：虚空符文与烈焰喷柱。原生实体保留完整 tick/event 生命周期，附属只负责选点、参数和费用。
- 验证命令：Java 21，`./gradlew.bat :runGameTestServer :jar :sourcesJar :verifyDistributions --offline --no-daemon --console=plain`。
- 结果：6 项 GameTest 全部通过；5 个魔符中英文名称/描述检查通过；二进制和源码包无本体媒体、实现或依赖 JAR。
- 构建日志：工作区 `Cataclysm-Nouveau/build/ground-validation.log`（忽略文件）。
- 构建产物：工作区 `Cataclysm-Nouveau/build/libs/cataclysm-nouveau-0.1.0-dev.3-mc1.21.1-neoforge.jar`。
- SHA-256：`535cd1cac075ead96f6a218de851e19d3e1098e0022c2815497e557407b116ce`。
- 由协调者部署到 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，替换 dev.2。旧版移到实例 `.codex-backups/cataclysm-nouveau/`。
- 客户端外观未人工验证；本体资源路径、实体事件和注册均已核对实际 JAR。需要重启游戏加载新版本。
- 本批无 Forge 移植、无远程推送、无 Core 修改。锚与海浪的下一批边界见 `next-native-abilities.md`，尚未实现。
