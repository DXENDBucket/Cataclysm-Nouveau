# dev.50 交接：利维坦首批

- 日期：2026-10-19；MC 1.21.1 NeoForge；版本 `0.1.0-dev.50`。
- 工作树：`C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/cataclysm-leviathan-dev50-20261019/Cataclysm-Nouveau`。
- 分支：`feature/leviathan-first-dev50`，起点 `c5985d0990fd4ccd981572e2c445a6c547ecc680`。
- 实现提交：`c9cf7d5`，已快进主分支，并标记 `v0.1.0-dev.50`。
- 固定 Core：`ed44ebb86bd087f3546a1bc6a315d2692f70262c` / 0.1.28；独立构建副本受版本控制文件保持干净。
- 本体：`L_Ender's Cataclysm 1.21.1-3.33.jar`，SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`。
- 行为、费用、边界和两次击杀材料预算：[实现记录](leviathan-first-dev50.md)。

## 实际验证

Java 21，隔离工作树，离线构建。先做一次编译及资源校验。然后执行：

```powershell
./gradlew.bat :runGameTestServer :verifyDistributions --offline --console=plain -PgameTestNamespaces=cataclysm_nouveau_leviathan
```

首轮 6/8 通过，修正射程加载早退后第二轮 **8/8** 通过。最终日志 `build-leviathan-tests-r2.log`，服务器记录 `run/gametest/logs/latest.log`，75 种魔符双语文本校验通过，二进制及源码包均无本体素材/实现。GameTest JVM 已自然关闭，没有本次工作树的残留 Java 进程。

覆盖实际转换、原生 40 tick 追踪、幼年撞墙不续接、恐惧禁疗、友军爆炸保护、环绕单一轨迹和逐次支付/空蓝、原生水雷引爆与到期、光束输出秒收费/无付费尾伤/生命项、友军转向与创造、门户原生第 22 tick 子光束及归属/伤害/时效/清理、Ars 伤害事件/装备一次加成、取消与本体非联动隔离。

未启动客户端，未宣称最终视觉已验收。实际 NEG / Elemental / Hex 组合没有另跑整套矩阵；第一人称渲染修正、晚进入区域的视觉、各种特殊形态画面仍待人工验证。未发布 CurseForge 或推送远程。

## 产物

工作树 `build/libs/`：

- `cataclysm-nouveau-0.1.0-dev.50-mc1.21.1-neoforge.jar`
  - SHA-256 `bb60efc9f90f8703d0b0b3cbb31add3ba4a956bd454297e36cd321f9ce059dcb`
- `cataclysm-nouveau-0.1.0-dev.50-sources.jar`
  - SHA-256 `56a9797da72df74c106d089daeddaedec0b75b806d3d6a049828600fedbf8647`

## 协调者实际部署

已部署至 `C:/Users/57991/Desktop/.minecraft/versions/1.21.1-万法归一/mods`，仅将本附属 dev.49 换为 dev.50，落地 JAR 哈希与上述构建一致。其他 **173 个文件**部署前后逐一 SHA-256 相同，原有 Core 0.1.27 保持不变，没有改写“万途归零”实例。

原 dev.49 备份位于测试实例 `codex-backups/cataclysm-dev50-20261019/`，其中 `deployment.json` 保存新旧哈希及其他文件校验清单。主仓库 `build/libs/` 已同步二进制及源码产物。此次未推送或上传 CurseForge。
