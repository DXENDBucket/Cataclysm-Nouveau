# 0.1.1：Forge 1.20.1 移植与图标修复

## 范围与基线

- 同仓库 `common/`＋`forge/`，根目录 `VERSION=0.1.1`，两端保留 102 个魔符。
- Forge：Minecraft 1.20.1、Java 17、Forge 47.4.0、Ars Nouveau 4.12.7、Cataclysm 3.31、Lionfish API 3.0。
- 最初服务端验证使用只读 Core `b46be94b17799573b825a8e4a11870fa3dc857d6`（0.1.29）。该固定副本的受版本控制文件没有修改。
- 用户随后报告双端图标偏暗，因此另开独立 Core 工作树修正通用二维图标绘制（0.1.30）。最终客户端图标预览及回归使用这一工作树，通过 `confluxCheckout` 显式指定。没有在附属的固定 Core 副本上修改源码或静默换基线。

## 平台适配

共享行为经 `forge/generate_platform.py` 转换 Minecraft／Forge／Ars 4 接口，手写平台文件优先。不要修改生成目录。

原生差异包括：触手 Capability、熔岩弹同步字段访问、旧版实体同步数据、客户端顶点格式及包、Ignis 的 vanilla hurting-projectile 父类、在具体 tick 内完成运动与特效的元素矛和诅咒沙暴。沙暴保留 Forge 原生的碰撞／消退状态；普通碰撞在其具体 override 中续接，重复 Ars 载体保持自身寿命和逐次费用。

Forge 的爆炸计算器没有新版实体回调，因此仅对本附属的计算器补入事件过滤与实际伤害挂钩；仍执行 Minecraft 和灾变的原生爆炸体。没有复制原生实现。

Ars 4 没有 Ars 5 的每父魔符增强描述 map，这部分使用 Ars 4 本来的表现；增强兼容、可用性判断和效果规则保留。

## 图标

- 两端通过现有 `GlyphVisuals` API 将自定义三维图标指向真正的灾变物品，让模型、物品身份和原生 GUI 变换一致。
- Forge 在原生 custom-renderer 调用处替换物品身份，保留 ItemRenderer 与 Core 外框流程；不在 GUI 入口提前截断外框。
- 深渊标记使用的实体贴图需显式加入 blocks 图集。共用 `assets/minecraft/atlases/blocks.json` 仅含一条运行时引用，不含任何贴图像素。两端发行审计仅允许这份内容精确匹配的图集元数据，其余非自有资源命名空间依旧拒绝。
- Core 0.1.30 修正带方向光的二维外框；三维中心仍保持其原生光照。

## 验证与边界

- 实际 Forge 服务端加载所有灾变 EntityType、全部魔符注册；所有弹射物替换分别以普通和环绕载体启动、运行原生 tick、清理。
- 怒气隐藏效果衰减、原生触手 Capability、原生爆炸伤害与友军过滤。
- 诅咒沙暴普通原生碰撞及重复载体的成功伤害、归属、接续。
- 两端真实客户端启动并绘制全部 102 个 GUI 图标、12 个 FIXED context 三维图标。预览为可重复启用的 test source，不进入发行 JAR：`-PwithClientProbe=true`。
- 两端二进制／源码归档审计：没有本体素材、实现代码或依赖 JAR，包含 MIT 与第三方说明；Forge 另外检查 refmap、Java 17 及全部 102 个旧版格式配方。
- 没有逐一人工体验所有 Boss 动作、粒子和每种可选 Ars 附属组合；不以服务端通过替代这些视觉／玩法验证。

原始日志、截图和哈希在各工作树忽略的 `build/`、`run/` 与交付目录中。未自动推送、上传 CurseForge 或覆盖共享人工测试实例。
