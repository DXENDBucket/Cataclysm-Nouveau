# dev.6 巨兽系列交接

## 范围与基线

- 用户：将剩余七项巨兽系列能力接入；没有施法物品来源则用 Boss 直接掉落物制作，相似能力优先采用 Boss 版本。
- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`，分支 `agent/cataclysm-monstrosity-full`。
- 附属起始提交 `8aa0b6e8c152a58e6740261270085fd52e78c62a`，交付 `0.1.0-dev.6`，提交由标签 `v0.1.0-dev.6` 定位。
- 固定 Core `b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16），相邻 `../Ars-Conflux`，源码及资源未改；因为采用 Allies API，依赖最低版本明确为 0.1.16。
- NeoForge 1.21.1 / 21.1.250，Cataclysm 3.33、Ars Nouveau 5.13.1、Java 21。未制作 Forge 版本。

## 实现与差异

- 接入熔岩炸弹、巨兽震地、撼地裂波、环形焰柱、巨兽肩撞、熔岩汲取、骇人之恶；现在 15 个魔符。
- Boss 私有行为通过 Invoker 和不生成到世界的调用帧复用；正常 Boss 和普通原生投射物保持原有逻辑。只改 opt-in 的所有者、增强、友军及执行边界，没有复制本体实现/素材。
- 震地使用 Boss 的基础攻击及受限最大生命比例伤害，不用炼狱锻锤版本。地裂调用原生六段推进；环形焰柱保留两侧来源及半血模式；肩撞使用原生碰撞、骨裂及击退分支，玩家/友军运动由附属驱动，碰墙停止且不拆方块。
- 汲取执行原生三波地形操作，波次实际移除熔岩才恢复；只对实际熔岩执行权限事件。骇人之恶直接使用原生状态效果。
- 熔岩炸弹保留原生弹体与落地实体、爆炸，附属禁止自己产生的源熔岩扩散。每个重复命中有独立池，一次付费，空蓝停止载体。生命周期独立默认 200 tick，不采用 Boss 的随机配置时长。
- SavedData 保存源位置和临时实体标识；重启/区块重新加载后清理。改块解除记录，不误删后来替换的方块。无区块强制加载。
- 制作：四种 Boss 攻击用恶兽犄角；熔岩炸弹与汲取用熔岩能量单元；骇人之恶用恶兽头盔。各加魔源宝石，27 经验。全部为真实抄写台配方。
- 伤害增强读取 Ars Harm 参数，默认每份 +2；骇人之恶提升效果等级。持续动作和状态支持延长/缩短时效。费用、冷却及部分参数有配置。
- 详见 `docs/monstrosity-abilities.md`、README；下一步先人工确认视觉与平衡，再选择其他 Boss 系列。

## 验证

工作树中 Java 21：

```powershell
./gradlew.bat :runGameTestServer :verifyDistributions --offline --no-daemon --console=plain
```

最终 `build-dev6-final-check.log`：23 项必需 GameTest 全通过，15 个魔符中英校验通过，二进制/源码包资源边界检查通过。之后只将元数据 Core 最低版本改为 0.1.16，使用 `:verifyDistributions` 再打包（`build-dev6-package.log`），无行为代码变化。

前期问题已解决：super 调用 WrapOperation 的接收者签名必须为目标 Boss 类型；熔岩原生 hit 路由需先进入原有回调才进行池转换；恢复清理时改块钩子会修改同一索引，改用快照迭代。补充恢复/重复池检查覆盖这些边界。

服务端覆盖费用/退款、所有者、友军、半血喷柱、地裂时序、肩撞骨裂/障碍、三波恢复、原生增益、熔岩禁止扩散、自然/死亡清理、替换方块与 SavedData 恢复。客户端图标、粒子、地裂外观及玩家位移没有人工确认。

`verifyDistributions` 不包含灾变素材、源码、依赖或测试类；MIT 已打包。固定 Core 受版本控制文件保持干净；测试服务已退出。

## 交付

工作树 `build/libs/`：

- `cataclysm-nouveau-0.1.0-dev.6-mc1.21.1-neoforge.jar`
  SHA-256 `2fac9b90eeda5cb0b94b9986005ba09af64de211d3ea42cd0826ee59240e3ed0`
- `cataclysm-nouveau-0.1.0-dev.6-sources.jar`
  SHA-256 `f5d6560c73ffabedc38ec27b46799a1d5c4e14e66eb066f828b1db1f63068472`

协调者本地快进合入 main，打 `v0.1.0-dev.6`。部署至 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`；旧 dev.5 备份在 `.codex-backups/cataclysm-nouveau/pre-dev.6/`。不替换 Core，不推送远程。共同实例的客户端仍需用户重启验证。
