# dev.7 状态魔符交接

- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`；分支 `agent/cataclysm-monstrosity-full`。
- 基线附属：`0168e87ea073bf02e39d58d4e5f5a9bcd43d9016`；固定 Core：`b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16），副本受版本控制文件未改。
- NeoForge 1.21.1，Ars 5.13.1、Cataclysm 3.33；未实现 Forge 附属。
- 接入骨裂及熔岩行走；无震退。获取：恶兽犄角／熔岩能量单元，各搭配魔源宝石。
- 骨裂复用原生效果及属性减益；熔岩行走是本附属同步临时效果，默认 30 秒，范围 2～1200 tick。友军限制、潜行下潜、浮起、熔岩与持续燃烧保护都随效果失效。
- 状态图标引用原版抗火 sprite；物品模型引用灾变，包内不含灾变媒体或源码。
- 修正共享弹射物持续时间提示误写海浪；不修改共享 Core 的只读副本。
- `build-dev7-check.log`：26 项 GameTest 全部通过、17 魔符两语言检查、二进制及源码包 `verifyDistributions` 通过。
- 客户端图标／熔岩行走手感未人工验证。
- 另一个明确以 Core 为目标的工作树处理大列表分页边界（Conflux 0.1.17）；不将该修复写进本附属。

## 产物 SHA256

- `cataclysm-nouveau-0.1.0-dev.7-mc1.21.1-neoforge.jar`：`96ecfcc4221f3d03d05fe03f4ab5f82651f1211de169e1b70ac7a6020b70beb9`
- `cataclysm-nouveau-0.1.0-dev.7-sources.jar`：`e9b11e1352ec1c258319d9387b730e390b9e990803d711f628ffea38b36c6e20`
