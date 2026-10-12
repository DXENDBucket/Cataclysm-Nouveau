# 焰魔剑舞幻影样例 dev.32

- 工作树：`parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`。目标仅NeoForge1.21.1。
- Core固定 `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件保持干净；Cataclysm3.33依赖SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 范围：只为IGNIS_SWORD_COMBO提供纯客户端原生Boss模型／两段动画幻影，执行者与攻击朝向来自既有会话。没有注册真实Boss、伤害实体或新的玩法能力；费用、冷却、伤害与AI不变。细节见 [ignis-action-phantom.md](ignis-action-phantom.md)。
- 客户端 `[action_phantoms]` 默认开启，普通不透明度0.28、自己的第一人称0.14、尺度1.0；原纹理与渲染层运行时复用，不打包本体素材。
- `build-ignis-phantom-dev32-release.log`：77项required测试全通过，BUILD SUCCESSFUL。新增快照序列化／角色／维度／阶段、真正发送的两段动作／自然结束／取消、Mana不变和服务端没有Boss实体检查。
- 首次测试仅执行者移除后的关闭通知失败，原因是追踪表已经移除，不能再依赖trackingAndSelf；改用原攻击世界／最后位置的附近观察者通知，并单独通知玩家执行者，之后通过。
- `build-ignis-phantom-dev32-package.log`：最终包装成功，二进制与源码verifyDistributions通过；最终额外包装只修正配置注释，没有改变已测玩法／网络逻辑。
- 二进制：`build/libs/cataclysm-nouveau-0.1.0-dev.32-mc1.21.1-neoforge.jar`，SHA-256 `5e19bd5fe21792e5dc84adc68ee920ac2bf011ba958f1c41207dc09fe16ad0d6`。
- 源码：`build/libs/cataclysm-nouveau-0.1.0-dev.32-sources.jar`，SHA-256 `599924290bcdae5663f1a921a512caaa257d41e827cb85212f95fb88101c27c7`。
- 串行部署“万法归一”；dev.31备份至实例 `mod-backups/2026-10-12-ignis-phantom-dev32`。未替换Core或其他mod。
- 本次没有启动图形客户端，客户端代码编译与原生渲染／材质／只读字段审查不等于实际客户端Mixin、画面验收。待确认第一／第三人称遮挡、两段动作对齐、友军执行、结束淡出和优化模组共存。可先以“自身→焰魔剑舞”测试。
- 测试JVM已结束，没有停止用户游戏或其他Java；未推送／发布。其他Boss和咒翼灵骸仍未增加幻影或新魔符。
