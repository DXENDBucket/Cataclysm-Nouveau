# 虚空漩涡客户端诊断（dev.25）

## 状态与范围

用户确认 dev.24 仍仅闪现，服务器牵引与结束爆炸继续工作。原生 Boss 漩涡尚未测试。此版本是采证版本，不宣称已解决显示问题。

- 新增本地 CLIENT 配置 `cataclysm_nouveau-client.toml`，`diagnostics.vortex` 默认 false；仅在授权的万法归一实例设 true。
- 客户端每个世界最多采集八个漩涡，单次记录最多1300tick；退出世界释放引用。快照按有限时间点记录，不逐帧输出。
- 记录实际 ClientLevel 实体、寿命、删除原因、managed/noCulling、物理/可见框、相机距离、实际 renderer、绘制/quad调用数，以及原生 renderer 实际选择的贴图和资源包。
- 原生客户端 renderer 的探针只在 Mixin 配置 client 列表加载；不开开关时不扫描实体、不输出日志。探针不取消或替换原生绘制。
- 可选 EntityCulling 标志仅反射读取，无优化模组硬依赖。普通 Boss 漩涡也能记录，以便对照。
- 不改漩涡位置、生命期、粒子或优化模组配置；不改 Core。

## 排查证据与下一步

当前实例确实加载dev.24；Iris日志显示没有启用shaderpack。实际Cataclysm3.33原生idle贴图不是透明空图；资源复制不是解决路径。

实际EntityCulling1.11.3的渲染和tick跳过均尊重entity.noCulling，需用客户端日志确认标记真正到达。Chloride1.8.1另有独立实体距离过滤，不读该标记；当前横向64格、纵向32格，entityLimit512代表不限数量。尚无证据其距离过滤就是本次原因。

用户重启后近距离施放一次，保持视线约15秒，再读latest.log的 `[Vortex diagnostic]`：

1. 实体提前removed且life异常：查客户端寿命/移除/同步。
2. 实体继续、render/arc计数停止：查裁剪和renderer分派。
3. 实体继续、render/arc计数持续、idle贴图存在：查透明绘制、缓冲/着色器或GPU优化。

仅在第三种证据成立后做有针对性的优化模组对照，不因安装了多个优化模组便认定某个为原因。

## 构建、版本与部署

- 工作树：`parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`；分支 `planning/scylla-close-harbinger`；父提交 `0ee13d7`；标签 `v0.1.0-dev.25`。
- 固定Core：`9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件保持干净。
- Java21、NeoForge1.21.1；`:build --offline --console=plain --no-daemon`，日志 `build-vortex-diagnostics-dev25.log` 明确BUILD SUCCESSFUL。本次仅诊断代码，未重复56项服务器GameTest、未启动客户端，运行时探针/最终图像待用户验证。
- `verifyDistributions` 同时检查二进制和源码包，无本体素材、代码或依赖打包。
- 协调者串行部署到万法归一，dev.24备份于 `mod-backups/2026-10-12-vortex-diagnostics-dev25`；只新增本地诊断配置。需重启，不关闭用户游戏。
- 仅本地提交与标签，无远程推送，无Forge交付。

## SHA-256

- `cataclysm-nouveau-0.1.0-dev.25-mc1.21.1-neoforge.jar`：`d79891bbc3899b8187ecc6a2a52b6d2300eb78fdae23ca84ccad024e8fae2efe`
- `cataclysm-nouveau-0.1.0-dev.25-sources.jar`：`55a6cee763d5b969cf74fd8967240a19ed10fdc6e1810c472aaf3d210be4ff40`
