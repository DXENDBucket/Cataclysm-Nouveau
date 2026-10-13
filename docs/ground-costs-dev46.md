# 地面魔符费用定值 dev.46

2026-10-13，按用户指定调整独立虚空符文为20 Mana、幻影戟为30 Mana，两者无主动冷却。伤害、怒气、攻击范围、原生时序与伤害窗口保持dev.44原生实现；强化仍每份额外10 Mana。Ars形态和其他魔符费用另算。

沿用`mana.void_rune=20`、`mana.phantom_halberd=30`、`cooldowns.ticks.phantom_halberd=0`。虚空符文冷却本来就是0。修改模组默认，并由协调者备份后同步万法归一三项旧合法配置值，其余键保持原样。

本轮只有默认数值变化，不重复战斗GameTest；Java21编译、jar、sourcesJar、verifyDistributions通过，60个魔符双语键核对通过。dev.45已验证的原生伤害、逐次扣费、退款、拥有者、友军与清理测试没有新增行为改动。未做本轮客户端人工验证。Core固定`08c307f3238b9b9aa7fd628b9771cd287160448d`（0.1.24），受版本控制文件保持只读；NeoForge1.21.1，原生Cataclysm3.33。

## 产物SHA-256

- `cataclysm-nouveau-0.1.0-dev.46-mc1.21.1-neoforge.jar`：`f91a069ab649f4d0b54647c00c4167943d6e5ddfaeddcac53edaf9ac2d9bf1dc`
- `cataclysm-nouveau-0.1.0-dev.46-sources.jar`：`49bee1f825fc6a989795335842d8dd5fb9f38764af3924c7a7764d8b2f552d27`

实现提交`427abcc`已快进合入main，标记`v0.1.0-dev.46`，未推送。已部署到万法归一并核对JAR哈希、20／30 Mana和两个零冷却配置。dev.45及旧配置备份于实例`codex-backups/cataclysm-dev46`，Core和其他附属未更换。
