# dev.9 巨兽配方分配交接

- 工作树：`parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`；基线附属 `ac03399333d89475afd8b48ccd7bfe995156e345`。
- 固定 Core `b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16）；副本未改，测试实例 Core 0.1.17 保持原样。
- 两种犄角配方：撼地裂波和环形焰柱。
- 八种电池配方：燃光弹、熔岩炸弹、巨兽震地、巨兽肩撞、熔岩汲取、骇人之恶、骨裂、熔岩行走。
- 每个配方另消耗魔源宝石 1、经验 27；产出各魔符 1。图标、施法代码及原生 Boss 掉落未改。
- `build-dev9-package.log`：verifyDistributions 检查 binary/sources 均通过。逐项检查打包配方及全部图标与 dev.8 字节相同。配方调整不重跑 GameTest。
- 本地提交及标签 v0.1.0-dev.9；部署“万法归一”，不推送。

## SHA256

- `cataclysm-nouveau-0.1.0-dev.9-mc1.21.1-neoforge.jar`：`e13fc2059b0d2840706194cc25c8dbf7912f48bcdf121654cd3fa670221cbe71`
- `cataclysm-nouveau-0.1.0-dev.9-sources.jar`：`255f0f421be69f4b005a3cea92d79d6a9cd90c5c31bd27e17db7d3396457015b`
