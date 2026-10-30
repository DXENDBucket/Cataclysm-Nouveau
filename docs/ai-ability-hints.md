# AI 能力提示（0.1.4）

102 种魔符通过 Conflux `GlyphAbilities` 声明能力类型与接收方式，供 Ars Instinct 等消费者判断编排用途。声明位于共享 `AbilityHints`，两平台使用同一份穷举分类；新增 Ability 必须补齐分类才能编译。不新增与 Instinct 的运行依赖，不自动让任何原生 Boss 开始用法术书。

## 接收者与执行者

| 类别 | 示例 | AI 选择 |
| --- | --- | --- |
| 弹射物替换 | 海浪、幻影箭、激光弹 | 敌人；不因后续接增益而变成友军法术 |
| 敌人位置上的效果 | 雷电场、幻影戟、虚空符文 | 敌人所在位置；原生支持时也可自身中心或固定位置 |
| 让接收者执行攻击 | 旋转挥锚、戟刃横斩、灰烬吐息 | 自身或友军作为执行者；也声明已有的固定位置能力 |
| 仅敌方实体 | 骨裂、深渊标记、潮汐触手 | 敌方生物，不声明空位置 |
| 增益 | 骇人之恶、水面行走、熔岩行走、诅咒怒气 | 自身或友军 |
| 恢复 | 熔岩汲取 | 恢复原施法者；选点只改变搜寻熔岩的中心，不视为给友军治疗 |

类型包含攻击、治疗、防御、控制、机动、辅助，并按实际行为补充范围或持续引导提示。这里只描述已有语义，不替代原生目标权限、法力、吟唱、冷却、动作占用或地面检查。

`GlyphConditions` 只限制 AI 的候选选择：已有的骇人之恶／水面行走／熔岩行走不会重复施放；怒气五层时跳过，消耗后重新可选。玩家手动施放、刷新持续时间和增强状态仍按原有规则执行。

没有物种或 Boss 来源限制。由深渊试炼、整合包或其他模组接入 Instinct 的生物，可以使用其他 Boss 系列的魔符。是否装备工具、如何启用控制器、法力池和原生 AI 的调度仍由接入方决定。提示不会替未知形态新增 AI，也不会让缺失的地面、熔岩、有效目标等条件自动满足。

## 数据包调整

Conflux 允许用下列位置覆盖某个魔符的完整提示集合：

```text
data/cataclysm_nouveau/conflux/glyph_abilities/glyph_anchor_spin.json
```

```json
{
  "tags": [
    "ars_conflux:attack", "ars_conflux:area",
    "ars_conflux:receiver_self", "ars_conflux:receiver_ally", "ars_conflux:receiver_position"
  ]
}
```

`{"tags": []}` 可让 AI 不识别这一项，删除覆盖则恢复代码默认。对接收方式的覆盖应尊重已有原生行为；加一个标签不能赋予不受支持的施法能力。

## 验证与构建

基线 Core `8512636481d1a33aa432f3aea1fef391223682ee`（0.1.45），其受版本控制文件不属于本次修改。NeoForge 原模组为 Cataclysm 3.33、Ars Nouveau 5.13.1；Forge 为 Cataclysm 3.31、Ars Nouveau 4.12.7。

可选定向测试使用已构建的 Ars Instinct 0.1.1 作为测试依赖；不打包进发行 JAR：

```powershell
./gradlew.bat :runGameTestServer :jar :sourcesJar :verifyDistributions `
  -PwithInstinctTests=true -PgameTestNamespaces=cataclysm_nouveau_instinct `
  -PgameTestHeap=1000m --offline --no-daemon --max-workers=1
```

非相邻工程可用 `-PinstinctJar=<实际 JAR>`。测试覆盖实际注册、弹射物优先分类、敌人选点与友军执行选择、触摸距离、原生增益应用后的 AI 跳过，以及怒气上限和消耗后的再次可选。Forge 编译同一声明并执行发行包／生产 Mixin 校验。未进行客户端画面或所有 102 项实战组合的人工验证。

本批结果：NeoForge 三项定向 GameTest 全部通过；两版发行包与源码包检查通过；Forge 111 个 Mixin 的生产映射检查无失败。结果、产物 SHA-256 与部署记录见 [验证记录](ai-ability-hints-validation.json)。首次测试的失败来自测试依赖未进入运行路径及测试直接调用了错误的原生入口，修正测试接线后通过；没有因此修改原生施法行为。
