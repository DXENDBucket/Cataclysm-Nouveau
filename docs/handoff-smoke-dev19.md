# 原生烟云客户端 tick 与死亡激光费用（dev.19）

- 任务：用户未看到凋灵榴弹烟雾；追踪导弹对生物能发射，玩家已确认其原有目标判定正常，不改动。新增要求降低死亡激光费用。
- 工作树 `parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，起点 a6ac5a0；交付标签 v0.1.0-dev.19。
- 固定 Core `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），tracked files干净。不改 Core、不复制灾变素材/代码、不推远程。
- 烟云仍用本体原生实体和客户端粒子。联动生成时设置同步 managedSmoke 标记，在接收 metadata 时令 vanilla noCulling=true。普通烟云不改变。不引用 Entity Culling 类，无新前置。
- 依据：实际 Cataclysm 3.33 Wither_Smoke_Effect_Entity 的粒子在客户端tick；测试包启用 Entity Culling 1.11.3 的 tickCulling；实际 ClientWorldMixin 在排除忽略culling实体之后，会在被裁剪/相机外状态跳过tick，NMSCullingHelper.ignoresCulling 直接检查 Entity.noCulling。这个兼容缺口能解释现象，未人工确认最终客户端画面。
- 死亡激光默认每输出秒60 Mana，基础三秒180；考虑无敌帧限制实际命中，不按每tick检查次数估算DPS。实际百分比伤害、半血、增幅和逐秒结算机制不改。
- 验证：`:runGameTestServer :build --offline --console=plain --no-daemon` 45项通过，含烟云opt-in标记/普通本体隔离、metadata接收后noCulling安装，以及既有伤害/友军/清理。记录 build-smoke-culling-dev19.log。
- 测试后只调整默认费用数据，再跑 `:build`，通过本地化和两种JAR隔离检查；不重复战斗矩阵。记录 build-dev19-final.log。客户端尚未人工验证，无新增可选模组运行矩阵。
- 部署到万法归一；dev.18及旧费用配置备份在 `mod-backups/2026-10-12-smoke-dev19`。现有费用改为60，激光弹冷却维持0。需要重启客户端才能加载新烟云代码；未关闭用户正在运行的游戏。

产物 SHA-256：
- `build/libs/cataclysm-nouveau-0.1.0-dev.19-mc1.21.1-neoforge.jar`: `716ce921dee6d82b7fcce37a1bcd72421843daa37d35bcae0b6c2cf6599dba70`
- `build/libs/cataclysm-nouveau-0.1.0-dev.19-sources.jar`: `00760f5de9a9349696e15d1c9bb43b061da61478f3b4b87c5ff64e0a7dbe8af3`
