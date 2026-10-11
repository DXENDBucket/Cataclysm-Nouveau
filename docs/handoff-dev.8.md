# dev.8 配方交接

- 工作树：`parallel-workspaces/cataclysm-monstrosity-full/Cataclysm-Nouveau`；基线附属 `741bd2e0b4cf88f6634c28fbc81e5bb3aa6b79c3`。
- 固定 Core：`b24f18d72e64a902ff6c966040b0b83ea3cc6226`（0.1.16）；副本受版本控制文件未改。测试实例已用兼容的 Core 0.1.17，不替换。
- 燃光弹在抄写台消耗熔岩动力电池（cataclysm:lava_power_cell）和魔源宝石各 1，经验 27；图标仍为巨兽之眼。
- 已核对 Cataclysm 3.33 中文物品名及巨兽掉落表，电池为直接掉落物。
- 配方修改不涉及施法实现，只执行包装与配方检查，没有重跑 GameTest。`build-dev8-package.log` 的 verifyDistributions 对 binary/sources 均通过。
- 本地提交／标签 v0.1.0-dev.8；协调者部署“万法归一”，不推送。

## SHA256

- `cataclysm-nouveau-0.1.0-dev.8-mc1.21.1-neoforge.jar`：`b3b296c33f0b1f2eaa4173b7c29207f9968b42e44d3efff4f209b4257eb68cae`
- `cataclysm-nouveau-0.1.0-dev.8-sources.jar`：`3dd2b811867a00930e6ab6aa3f20758069d748f308af6d578ed9a37db3b29359`
