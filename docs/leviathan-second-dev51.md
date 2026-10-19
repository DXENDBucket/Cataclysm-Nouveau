# dev.51：利维坦第二批

2026-10-19。新增维度裂隙、潮汐触手、深渊水雷场与深渊轰炸阵；首批四项的费用和配方保持不变。没有加入抓取、成对传送门或更多身体动作。

## 行为与获取

| 魔符 | 默认额外 Mana | 主动冷却 | 抄写材料 |
| --- | ---: | ---: | --- |
| 维度裂隙 | 650 | 20 秒 | 潮汐利爪 ×1 |
| 潮汐触手 | 140 | 5 秒 | 潮汐利爪碎片 ×1 |
| 深渊水雷场 | 900 | 12 秒 | 潮汐利爪碎片 ×1 |
| 深渊轰炸阵 | 1200 | 20 秒 | 潮汐利爪碎片 ×1 |

四项均为效果魔符，每种另需魔源宝石 ×1、27 经验。全部费用和主动冷却进入现有配置。利维坦八项累计两把整爪加六碎片，三次固定掉落可以学齐。图标只运行时引用灾变资源，武器/卵继续委托真实物品渲染器；系列排序仍集中在一起。

### 维度裂隙

直接运行本体 `Dimensional_Rift_Entity`：落点生成 stage 1、基准 lifespan 600 的裂隙，按原动作生成后第 38/70/102 tick 的相对时间升阶，最高 4。只修改本次实例，不扫描并升级其他人的裂隙。保留成长声音/震动、分阶段外观、原生牵引、10 点接触伤害、关闭声音与收缩后的威力 4 爆炸。

最后 100 tick 进入原生每 40 tick 降一阶的关闭过程，stage 0 爆炸后删除；满阶收尾可能延续到约 640 tick，不按 600 强行截断。时效修饰改变基准 lifespan，缩短到提前进入关闭时不强制继续成长。会话额外预留收尾时间。

联动实例直接跳过原生 `31³` 方块扫描和掀地，也不删除附近其他能力产生的碎块。服务器统一牵引，标记运动同步；客户端保持原生显示，但阶段/寿命由服务器同步，不再自行额外推进。伤害、爆炸与牵引均保护 Core 友军，并排除友方已绑定的攻击实体。正常 Boss 裂隙保持原生行为。

### 潮汐触手

调用原生利爪 `launchTendonsAt`，选中 16 格内可见的敌方生物，从真实施法者伸出；方块、友军、无效/过远目标失败退款。沿用原生单链门槛、连锁候选、视线检查、段数限制、伸缩模型/音效、3 点基础伤害与深渊诅咒。增强在每段实际伤害上应用一次，链条生成不再次收费或复制 Ars 后续效果。

子段继承同一上下文和链管理器。原生 Creator UUID 用于连线与附件，伤害来源独立保留真实 damageOwner。收回和取消只管理本链段与本链附件；另一条链/原生武器的记录不被旧链清空。

实际 3.33 的 `TidalTentacleUtil.setLastTentacle` 只写 `hasTentacle`，但其 `getLastTendon` 还要读 UUID/实体 ID。联动在初次和每次换段时补齐原生附件这两个字段；自然结束/取消时按 UUID 所属清理。修改仅针对本附属调用/管理的链，未全局改动本体武器。原生候选与重复命中规则保留，不另造一套连锁算法。

### 两种阵列

未注册到世界的 `SpellLeviathan` 只作为原生 Goal 调用帧，不 tick Boss AI、不承担伤害归属。两种阵列由本体私有布阵方法计算位置、碰撞/地面及 warmup。先在不插入世界的作用域检查首批候选；完全没有有效候选则启动失败退款。同一随机种子恢复后执行第一批，避免预检查额外改变布阵随机结果。延迟中目标/地形变化仍可能让实际数量减少。

- 水雷场：保留第 31 tick 尝试 35 枚、XYZ 高斯散布、每枚 `2 × 序号` 的启动延迟。实体目标仍可随实际第一批时的位置取中心，固定落点使用不出生的标记。时效修饰作用于每枚的存续。
- 轰炸阵：第 56 tick 尝试九个随机门户和第一个目标门户，第 76/96 tick 再瞄准目标；目标移动时后两次读取其当前位置。保留原生向下最多约 50 格的地面搜索，不能在无地面时强补门户。时效修饰每个子光束，门户预留收尾余量。

候选落点未加载时在读方块/碰撞前拒绝，不主动加载随机远端。原生 helper 的出生口接入上下文、真实 owner 与首批管理标记。阵列整体支付一次，门户子光束也不重复付费。会话覆盖最后一批及全部延迟/寿命；根失效、禁用、卸载或结束时清理所有本阵列父实体和子光束。

## 验证与限制

隔离工作树 `parallel-workspaces/cataclysm-leviathan-dev51-20261019/Cataclysm-Nouveau`，分支 `feature/leviathan-second-dev51`；起点 `df6732425c1095ea466ce358af06e986fcd440b1`。固定 Core `ed44ebb86bd087f3546a1bc6a315d2692f70262c` / 0.1.28，受版本控制文件保持干净。本体仍为 Cataclysm 1.21.1-3.33，SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。

Java 21，首次编译修正了缺少 ArmorStand import。首轮 7 项新增检查加首批 8 项回归共 14/15 通过；失败点为触手附件实体字段缺失。修正后只重跑涉及本批的 7 项，全部通过；此前 8 项首批回归已通过，未重复完整旧矩阵。

```powershell
./gradlew.bat :runGameTestServer :verifyDistributions --offline --console=plain '-PgameTestNamespaces=cataclysm_nouveau_leviathan_second,cataclysm_nouveau_leviathan'
./gradlew.bat :runGameTestServer :verifyDistributions --offline --console=plain -PgameTestNamespaces=cataclysm_nouveau_leviathan_second
```

日志 `build-dev51-tests.log` / `build-dev51-tests-r2.log`；最终 79 种魔符双语校验、二进制及源码 `verifyDistributions` 通过。GameTest JVM 自然退出。客户端未启动；原生连线、裂隙分阶段显示、音效/震动与阵列画面尚待人工验收，不能把服务端通过当作画面通过。未单独重跑 NEG/Hex 共存。

产物在工作树 `build/libs/`：

- 二进制 `cataclysm-nouveau-0.1.0-dev.51-mc1.21.1-neoforge.jar`，SHA-256 `4ae6e9d515e01b3bb266c20fa0fef075c7d0b802f868adb40973b0c410711817`。
- 源码 `cataclysm-nouveau-0.1.0-dev.51-sources.jar`，SHA-256 `fc29e1f04226a21313d5aa676f9150e68050c3f8366d2896e3a95b6d313a810f`。

实现提交 `4397b5c`，已快进主分支并标记 `v0.1.0-dev.51`。协调者已部署到 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，只替换本附属 dev.50；落地 JAR 哈希与上述一致，其他 **173 个文件**逐一 SHA-256 不变，Core 0.1.27 保持不变。

旧 JAR 及校验清单位于实例 `codex-backups/cataclysm-dev51-20261019/`；主仓库 `build/libs/` 已同步发行包和源码包。没有修改“万途归零”实例，没有推送或上传 CurseForge。
