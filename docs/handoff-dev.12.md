# dev.12：按 Boss／来源系列排列魔符

- 版本：0.1.0-dev.12，MC 1.21.1 NeoForge；Cataclysm 3.33。
- 固定 Core：`b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16），受版本控制文件无改动；人工实例继续使用 0.1.17。
- 修改范围：Ability 来源系列、GlyphCatalog 展示键／列表排序、版本及说明。伤害、获取、资源结算和 Ars 类别未变化。

来源系列归属集中在 Ability。GlyphCatalog 将同一个键用于 Core GlyphPresentation 和创造标签页列表。保留 Ars 类别与“普通增强在前、弹射物替换在后”，各类别内按来源系列、能力声明顺序排列。

- 下界合金巨兽效果：烈焰喷柱、巨兽震地、撼地裂波、环形焰柱、巨兽肩撞、熔岩汲取、骇人之恶、骨裂、熔岩行走。
- 斯库拉效果：雷击柱、风暴巨蛇、闪电风暴、雷电场、放射雷暴。雷击柱按风暴精华获取归入这一系列，不意味着它等同于斯库拉原生落雷。
- 巨兽弹射物替换：燃光弹、熔岩炸弹。
- 斯库拉弹射物替换：战锚、海浪、沧波棱刺、鸣雷槊。
- 紫水晶蟹、狮子鱼、末影傀儡能力保留各自来源系列。

## 验证与产物

`:build --offline --console=plain --no-daemon` 通过，含编译、本地化及发行／源码包资源检查。未重跑战斗 GameTest；客户端排列未人工打开验证。

- 发行 JAR：`build/libs/cataclysm-nouveau-0.1.0-dev.12-mc1.21.1-neoforge.jar`
  - SHA256 `6d1c192253fdb8db0459fc196c3cea8ace472793079426ed2268b04cbf38d3c0`
- 源码 JAR：`build/libs/cataclysm-nouveau-0.1.0-dev.12-sources.jar`
  - SHA256 `df7fe4b408acfa6ecc05c5aaafa83d3ffbcebf8b765afd6eec8cd13784944d91`

由协调者串行更新“万法归一”，dev.11 备份到 `.codex-backups/cataclysm-nouveau/pre-dev.12`，核对复制后哈希；本地提交与版本标签，不推送远程。
