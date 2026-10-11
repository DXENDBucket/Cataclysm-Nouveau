# Cataclysm & Nouveau dev.5 交接

## 范围与基线

- 目标：修复 Ars 书内缺失增强文本、霆浪锚戟图标；接入下界合金巨兽燃光弹。
- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-monstrosity/Cataclysm-Nouveau`，分支 `agent/cataclysm-monstrosity`。
- 附属起始提交：`353df5a662dfe95b792089c3d41d354d68b21487`；交付版本 `0.1.0-dev.5`，提交由 Git 标签 `v0.1.0-dev.5` 定位。
- 固定 Core：`b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16），相邻 `../Ars-Conflux`。受版本控制文件无修改。
- NeoForge 1.21.1；Ars Nouveau 5.13.1；Cataclysm 3.33；Java 21。没有 Forge 版本。

## 实现

- Ars 构造函数生成的增强说明键没有采用旧自定义映射的值。现在直接向实际 `augmentDescriptions` 写入已本地化组件；弹射物替换复用对应魔符说明，海浪持续时间使用共享说明。
- Ceraunus 是 `builtin/entity` 模型且渲染器检查原生物品身份。客户端扩展将锚戟、海浪图标交给原生物品渲染器和原生 ItemStack。
- 燃光弹使用原生 Flare_Bomb_Entity；速度 1、重力 0.025、原生拖尾/旋转/命中事件。命中按原生逻辑生成十字或斜向四臂喷柱，每臂十根，要求附近地面。
- 基础直接伤害 7、每根喷柱伤害 7，增幅同时影响两者；点燃及盟友判定沿用原生。常规发射一次付费，重复载体每次命中付费，接续效果仅父弹命中一次。
- 默认额外 300 Mana、主动冷却 8 秒，可配置。抄写配方为巨兽之眼＋魔源宝石，27 经验。
- 原生普通燃光弹的伤害保持原样。未加入会改地形的熔岩炸弹。未改 Core。

## 验证

在工作树使用 Java 21：

```powershell
./gradlew.bat :runGameTestServer :verifyDistributions --offline --no-daemon --console=plain
```

- 最终日志：`build-dev5-verification-rerun.log`（忽略目录/文件）。13 项必需 GameTest 全通过，8 个魔符中英文本校验通过，二进制和源码包边界检查通过。
- 首次喷柱清理测试失败：范围超过模板实体活动区块，部分喷柱不运行。测试暂时强制加载覆盖区块并在结束后还原；生产代码未因此改变，重跑通过。
- 覆盖原生普通弹行为、费用、飞行、重复载体、真实增强说明映射、喷柱增幅/盟友/延迟伤害/自然清理/冷却。
- 客户端法术书图标、动画和粒子未人工确认；服务端测试不代表客户端画面已确认。
- 固定 Core `git status --porcelain --untracked-files=no` 为空；本次测试 JVM 已退出。

## 交付

工作树 `build/libs/`：

- `cataclysm-nouveau-0.1.0-dev.5-mc1.21.1-neoforge.jar`
  SHA-256：`15cd05289b67e3258340b2665701d4fedeaa7646324b2f1597ad94f82801cbbb`
- `cataclysm-nouveau-0.1.0-dev.5-sources.jar`
  SHA-256：`5b428d04d57590140374f24897c773ccc75421c79261b4cab54052163be97281`

仅运行时引用 Cataclysm 资源与原生行为，不包含其资源或源码实现。发行包保留本项目 MIT 声明。

协调者将本分支快进合入主仓库并串行部署到 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，旧 dev.4 备份至实例 `.codex-backups/cataclysm-nouveau/pre-dev.5/`。不替换 Core；不推送远程。下一步在客户端确认图标及燃光弹视觉，再决定巨兽其他能力范围。
