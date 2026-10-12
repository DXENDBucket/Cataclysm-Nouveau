# dev.30 交接

- 工作树：`parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`。
- NeoForge 1.21.1；Cataclysm 3.33，依赖 JAR SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- Core 固定 `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`，受版本控制文件保持干净。
- 规则／九种类审计见 [health-damage-cap.md](health-damage-cap.md)。默认开启，只有本附属上下文生效；Mana 和冷却不变。
- `build-health-cap-dev30.log`：69 项 required tests 全部通过。编译警告触发 Windows PowerShell NativeCommandError，因此 shell 状态为 1；实际 Gradle BUILD SUCCESSFUL，所有 GameTest 正常完成，未把 shell 状态误报为测试失败。
- `build-health-cap-dev30-package.log`：最终 `:build` 成功，二进制／源码包 verifyDistributions 通过。
- 二进制：`build/libs/cataclysm-nouveau-0.1.0-dev.30-mc1.21.1-neoforge.jar`，SHA-256 `21843d1119419f2c6e3c650da1bad61622c5aceefc23bde1bd2c92a5e5d72523`。
- 源码包：`build/libs/cataclysm-nouveau-0.1.0-dev.30-sources.jar`，SHA-256 `6ad1c0c4ed8f335ae16f88a60dfa47d5d72578eec9714cfa9c143419d0b007e3`。
- 串行部署到共享“万法归一”；dev.29 保存在实例 `mod-backups/2026-10-12-health-cap-dev30`。重启后服务端配置自动补入 `[damage] cap_health_damage = true`。
- 未启动客户端人工验证；未修改视觉代码。测试 JVM 已退出，没有停止用户游戏或其他 Java 进程；未推送或发布。
