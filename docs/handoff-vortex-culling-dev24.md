# 虚空漩涡显示修正（dev.24）

- 用户看到黑色图像仅闪现，但确认之后仍有牵引或结束爆炸。服务端实体继续工作，问题集中在客户端显示。
- 实际Cataclysm3.33 renderer绘制6×6平面及上方下降粒子；物理实体框2.5×0.5。测试实例EntityCulling1.11.3启用tickCulling，实际EntityRendererInter使用noCulling并读取getBoundingBoxForCulling。
- GuardianVortexMixin新增接收同步标记回调，令联动实例noCulling=true；可见框为水平±3、上方6格，与物理框独立。原生Renderer/粒子/坐标/寿命/伤害/牵引范围都不改，无新素材或可选模组硬依赖。
- 新增验证原生实体默认规则、同步接收安装保护、原生寿命仍同步、标记取消后复原，以及物理框仍2.5宽。未人工启动客户端，具体裁剪是否就是用户端最终原因仍需重启后的画面确认。
- 首次build-vortex-culling-dev24.log中漩涡检查通过，旧环形焰柱到期检查失败（两枚实际年龄15/13，90tick窗口内部分强制区块尚未及时开始tick）。不改旧业务和断言，复跑build-vortex-culling-dev24-recheck.log明确All56requiredtestspassed、BUILD SUCCESSFUL，二进制/源码verifyDistributions通过。
- 工作树parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau，planning/scylla-close-harbinger，起点125d3d6，标签v0.1.0-dev.24。
- 固定Core 9611f5087e3dd5adad6cd21517bb9563dd9a83dd（0.1.19），tracked干净。NeoForge1.21.1，无Forge交付/远程推送。
- 部署万法归一mods，dev.23备份mod-backups/2026-10-12-vortex-culling-dev24。Core和配置不改，需重启。测试/构建进程自然结束，没有关闭用户游戏。

## SHA-256

- `cataclysm-nouveau-0.1.0-dev.24-mc1.21.1-neoforge.jar`: `9c09b953c0c22a8e14ad804a3a7a4781d6a1ca90e6712fff2507685aea307bf5`
- `cataclysm-nouveau-0.1.0-dev.24-sources.jar`: `146cee3848cbb44aa59a3c996bb818d3e141874b11d8530b867b89fcd0350487`
