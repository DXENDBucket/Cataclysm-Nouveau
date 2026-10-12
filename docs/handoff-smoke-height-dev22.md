# 烟云高度回退（dev.22）

- 用户确认dev.21烟云已显示；要求去掉0.2格抬高，并指出此前墙面命中同样没有显示。
- 只将粒子生成高度恢复为原生cloud.getY()，保留服务端原版粒子包传输、发送频率与客户端防重复发射。数值能力、半径、生命周期、伤害判定不改。
- 工作树parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau，分支planning/scylla-close-harbinger，起点c117335，标签v0.1.0-dev.22。
- 固定Core 9611f5087e3dd5adad6cd21517bb9563dd9a83dd（0.1.19），tracked文件未改。仅NeoForge1.21.1；无推送。
- 验证：一次:compileGameTestJava :build --offline --console=plain --no-daemon成功（build-smoke-height-dev22.log），更新对应高度断言并编译，发行/源码包verifyDistributions通过。此次不重复运行GameTests；dev.21有51项通过记录。dev.21画面由用户确认，dev.22没有新增人工客户端验证。
- 协调者部署万法归一mods；dev.21备份mod-backups/2026-10-12-smoke-height-dev22；Core和配置不改。需要重启游戏加载本次改动。构建进程自然结束。

## SHA-256

- `cataclysm-nouveau-0.1.0-dev.22-mc1.21.1-neoforge.jar`: `c1bc4d01e8df91af80ec2296b073b9ddb632d77d3db0150daa9c887d75c286a5`
- `cataclysm-nouveau-0.1.0-dev.22-sources.jar`: `950771ddf5d5dc52fd6880d03a8cda12ae0f217253bda11b3b55d2fe6497f1f0`
