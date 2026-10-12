# 漩涡客户端寿命与守卫素材分解（dev.26）

## 客户端证据与修复

dev.25用户施放后，万法归一latest.log在2026-10-12 18:15:17至18:15:19记录三个漩涡：id991/995/1005均在elapsed=1、age=1、life=300时DISCARDED且registered=false；managed/noCulling=true、invisible=false、EntityCulling两个标志false、相机距离约6.5–8.1格。原生renderer/arc仅调用1–2次，grow_0贴图存在并来自mod/cataclysm。原始诊断保存于忽略文件 `build/vortex-diagnostic-user-dev25.log`。

本附属GuardianVortexMixin的rootAlive未区分端，在客户端读取服务器施法上下文、比较ServerLevel和ClientLevel，遂主动discard。实际MC Entity.equals/hashCode均按实体id；固定Core的NativeSpellDamage.CONTEXTS为WeakHashMap<Entity,CastContext>，单人游戏同id客户端副本可读到服务器绑定。寿命仍300且原生tick尚未减少寿命，与HEAD清理位置一致。

dev.26在GuardianVortexMixin、HarbingerSmokeMixin、GuardianRuneStateMixin的root清理入口先跳过客户端。只有服务器判断根施法者死亡/换世界并清理；保留原生客户端tick、渲染、寿命及服务器到期攻击。烟云仍采用此前用户确认可见的服务器粒子传输。同类Scylla清理已有端守卫，不改。不是裁剪白名单或替换素材修复；没有移除/禁用优化模组。

固定Core不在本任务修改范围，未改动它。后续独立Core任务应审计绑定表的端隔离、实体身份/世界隔离及线程边界；不能将按id的Entity等价关系当作对象身份。

## 获取调整

用户指出单次守卫只掉一件，适合分解成魔符材料。

- 新物品 `cataclysm_nouveau:guardian_void_fragment`，中文“虚空残片”、英文“Void Gauntlet Fragment”。
- 工作台无序配方：守卫者护手×1 → 虚空残片×4，消耗护手。
- 虚空漩涡、引力重拳各用残片×1＋魔源宝石×1，27抄写经验；保留魔符图标。
- 炮弹和践踏各用虚空核心×1＋魔源宝石×1。一次守卫击杀覆盖本批残片并余2枚，傀儡核心仍需另取；保留护手则再取得一只。
- 残片图标运行时引用 `cataclysm:item/void_shard`，未复制素材。真正数据配方由工作台/抄写台/JEI加载，不是信息提示。

## 验证与交付

- 工作树 `parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`；分支 `planning/scylla-close-harbinger`；父提交 `1c4a522`；标签 `v0.1.0-dev.26`。
- 固定Core `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件干净。
- Java21、NeoForge1.21.1；最终 `:build :compileGameTestJava --offline --console=plain --no-daemon` 成功，日志 `build-vortex-client-lifetime-dev26-final.log`。资源JSON、材料计数及运行时引用路径检查通过；verifyDistributions同时检查二进制/源码无本体资源、源码或依赖。
- 用户施放时追加素材需求，首次仅修复构建后补素材，再做最终构建；未重复执行56项服务器测试，未启动客户端。修复后的画面/存续仍待用户重启后确认；不要把编译成功说成画面已验证。
- 协调者串行部署万法归一；旧dev.25备份 `mod-backups/2026-10-12-vortex-lifetime-fragments-dev26`。诊断开关保留true供确认，模组默认false。没有动用户游戏进程。
- 本地提交/标签；无远程推送，无Forge版本交付。

## SHA-256

- `cataclysm-nouveau-0.1.0-dev.26-mc1.21.1-neoforge.jar`：`36986875056d3d9f1d9182d95785153e63ad74da73c1c41a3dcf1b6cbb6db0fa`
- `cataclysm-nouveau-0.1.0-dev.26-sources.jar`：`1f3e97ea16616985edb344d9288088fef2d37b14538f6be07c3df93d8ff4a160`
