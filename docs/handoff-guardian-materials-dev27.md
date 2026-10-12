# 守卫素材获取门槛（dev.27）

- 用户要求护手拆九枚，并将较强的核心配方转为残片。此处“傀儡掉的虚空符文”按实际掉落物虚空核心解释。
- 守卫者护手×1无序分解为虚空残片×9。虚空漩涡、守卫践踏、引力重拳各用1枚残片；单独虚空符文、守卫炮弹仍用傀儡掉落的虚空核心。每项仍另需魔源宝石×1、27抄写经验。四项守卫能力合计3残片＋1核心，余6残片可供后续能力；没有新能力或伤害修改。
- 工作树 `parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau`，分支 `planning/scylla-close-harbinger`，父提交 `a1be713`，标签 `v0.1.0-dev.27`。Core固定 `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），tracked干净。
- JSON解析、产量和五种配方材料分流断言通过。`:build --offline --console=plain --no-daemon` 成功，日志 `build-guardian-materials-dev27.log`，binary/sources verifyDistributions通过。仅配方改动，未重复服务器GameTest，未人工启动客户端。
- 协调者串行部署到万法归一，旧dev.26备份 `mod-backups/2026-10-12-guardian-materials-dev27`。不修改Core、配置或用户游戏进程；重启生效。本地提交/标签，无远程推送，无Forge交付。

## SHA-256

- `cataclysm-nouveau-0.1.0-dev.27-mc1.21.1-neoforge.jar`：`9220913b35fa030d21b380e7b8066b25ef7994a57e237f634922237fa227d3e5`
- `cataclysm-nouveau-0.1.0-dev.27-sources.jar`：`fc8eab89ada7a485145e8cf1f50aa8a51119b3fdfe9224cc4e382e8bf6c5d999`
