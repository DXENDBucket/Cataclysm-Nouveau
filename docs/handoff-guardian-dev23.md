# 末影守卫首批交付（dev.23）

## 边界和基线

- 用户指定四项：守卫炮弹、虚空漩涡、践踏符文阵、引力重拳。不制作新的行动形态/协议，不扩大至其他动作或Boss防护。
- 工作树parallel-workspaces/cataclysm-scylla-close-harbinger/Cataclysm-Nouveau，planning/scylla-close-harbinger，起点979c51f，标签v0.1.0-dev.23。
- 固定Core 9611f5087e3dd5adad6cd21517bb9563dd9a83dd（0.1.19），tracked文件未改。Java21、NeoForge21.1.250、Ars5.13.1、Cataclysm3.33。只交付NeoForge1.21.1，无远程推送。

## 实现

- 新四项默认Mana45/220/320/160，主动冷却0/300/200/80tick。两项核心能力用护手、另两项用傀儡虚空核心，各加SourceGem和27exp。
- GuardianCasting与SpellGuardian：脱离世界的调用帧，只调用本体StompAttack或指定动画的aiStep；实际执行者/付款/伤害拥有者分离。过滤实际物理目标，重绑近战伤害来源，子符文继承上下文和增强，并有独立会话。
- 炮弹复用既有HarbingerCasting中的通用加速弹体会话与原生装饰tick；新增同步轨迹模式和准备状态访问，不复制原生运动体。默认30tick前方3格布阵，随后加速。特殊Ars轨迹保留本体外观和准备计时、抑制第二套移动与原生发射到期；准备期不会触发续接。
- GuardianVortexMixin只对联动同步标记生效，原生实体仍驱动视觉/寿命。牵引仅服务端执行并同步速度，过滤Core友军；自然坍塌以真实伤害拥有者爆炸，普通清理不爆炸。没有客户端强转ServerLevel路径。Root死亡和移维度直接停止。
- 守卫践踏保留三圈+随机前排及半血三次32/53/62tick；引力重拳保留牵引和29tick原生重击。重拳牵引/重击视觉显式发送本体粒子类型，不依赖未出生Boss的客户端tick。
- 原本dev.22烟云高度/发送路径不改。护手builtin/entity图标复用真实物品渲染身份，不包含本体素材或源码。

## 验证与实际限制

- 编译阶段修正测试夹具使用的不存在MethodOrbit引用，沿用现有EntityOrbitProjectile夹具与原生Projectile解析器。
- build-guardian-dev23-check.log明确All56requiredtestspassed，BUILD SUCCESSFUL；43种魔符中英本地化与verifyDistributions二进制/源码检查通过。
- 五项新增测试：普通炮弹准备/原生位移/原生伤害与漂浮/归属/一次续接/原版隔离；环绕独占运动/原生寿命抑制/友军过滤/两次付款/空蓝清理；漩涡牵引和同步/不伪造持续伤害/结束爆炸归属与友军/不毁地形/死亡清理；践踏完整44/57/70与半血132–210数量/本体独立基准/子特性归属和清理；友军执行重拳/牵引和伤害友军过滤/封顶hp项/实际归属/付款/取消后控制释放。
- 基础独立依赖环境；未重跑全部可选联动矩阵，未启动客户端人工验证。GameTest不能证明炮弹准备动画、漩涡模型与粒子、护手图标等画面已正确。
- 分布说明、费用和触发示例见README末影守卫首批章节与docs/ender-guardian-abilities.md。
- 本次启动的测试/构建进程均已自然结束，不终止用户游戏。协调者部署万法归一mods，dev.22备份mod-backups/2026-10-12-guardian-dev23，Core与现有配置不改，需重启加载。

## SHA-256

- `cataclysm-nouveau-0.1.0-dev.23-mc1.21.1-neoforge.jar`: `c772ed4b180d61cc8a6d90a359f8d8b33c5fe3e268146b94ac2112f8d8cfdc07`
- `cataclysm-nouveau-0.1.0-dev.23-sources.jar`: `92bc283f5d97bfcd46479d4af5b2fb9a95d6139b9d1a4ebbb27334d49b009f1a`
