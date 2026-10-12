# 激光弹默认无冷却（dev.18）

- 用户要求移除激光弹逐发冷却；默认 `[cooldowns.ticks].laser_bolt = 0`，载荷仍为40 Mana。
- 固定 Core `9611f5087e3dd5adad6cd21517bb9563dd9a83dd`（0.1.19），受版本控制文件未改。沿用先驱者独立工作树。
- 核对 Cataclysm 1.21.1-3.33 的 Laser_Gatling：每2 tick发射，20 tick物品冷却只在燃料装填分支。
- 只改默认值，发行模组不迁移或覆盖玩家配置；协调者将万法归一现有配置的20改为0，备份在 `mod-backups/2026-10-12-laser-cooldown-dev18`。
- `:build --offline --console=plain --no-daemon` 通过，包括本地化、二进制和源码包资源隔离检查。本轮不重新跑战斗测试或客户端，dev.17的45项服务端记录留作已有基线。
- 交付标签 `v0.1.0-dev.18`；部署到万法归一，旧dev.17移入上述备份目录。未推远程。

产物 SHA-256：
- `build/libs/cataclysm-nouveau-0.1.0-dev.18-mc1.21.1-neoforge.jar`: `13ef08778ff88874f8bf5b26c7cddc26113b02ce14c5ad735c3846c2f710f581`
- `build/libs/cataclysm-nouveau-0.1.0-dev.18-sources.jar`: `98a9fd3c3b30e7f4a57ffe8606efd839d4ebb810c0b264fba5f6c3cfac55124c`
