# dev.10 炼狱锻锤配方交接

- 工作树：`parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`；基线附属 `b2a89bbc57c69d82ecd17a1b69459f8d8e266c4e`。
- 固定 Core `b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16），副本未改；实例 Core 0.1.17 不替换。
- 唯一锤子配方：巨兽震地，用炼狱锻锤＋魔源宝石，经验 27。其他配方为两种犄角、七种电池。
- 震地图标引用 infernal_forge 原生模型；父模型是 builtin/entity，接入真实原生 ItemStack 渲染委托，其他战锚图标通过同一辅助方法保持原身份。
- 未修改施法与伤害实现；仍为 Boss 版震地。
- `build-dev10-package.log`：编译、双语文本及 binary/sources 资源边界检查通过。逐项检查打包配方与图标引用；不重跑服务端 GameTest。
- 未启动客户端，锤子图标实际外观仍需人工确认。
- 本地提交及标签 v0.1.0-dev.10；协调者部署“万法归一”，不推送。

## SHA256

- `cataclysm-nouveau-0.1.0-dev.10-mc1.21.1-neoforge.jar`：`50c408d65728b74bd87a9c3063c9992f0d0ac72530b0d759da134a792750fe59`
- `cataclysm-nouveau-0.1.0-dev.10-sources.jar`：`180bf5384241b1686c5b5dc9e45bcc20e0b52de3ae00575d4bd119c5232e3a8d`
