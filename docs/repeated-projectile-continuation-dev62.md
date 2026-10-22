# dev.62：持续弹体按每次有效命中续接

用户确认：原生无敌帧本身已限制连续伤害，不需要再为同一实体添加弹体终生只续接一次的限制。

移除以下附属自加的目标集合：地震波、海浪、飞旋斧刃、水矛／雷矛、电火花。伤害型弹体以本次原生伤害成功为准，取消伤害和无敌帧失败不执行后续 Ars 编排；再次成功受击则再次执行。原生攻击次数、贯穿预算、自然消失和友军关系继续由原有路径处理。

元素矛原先直接在碰撞回调续接，本轮同时通过既有 ImpactTriggers.ENTITY_DAMAGE 观察原生伤害接受结果，避免去掉去重后在无敌帧期间无伤害续接。纯原生区域／状态与电火花保持原有建立／触发语义。本轮不把每个独立伤害分段都变成一次后续编排。

普通弹体仍在发射时付一次载荷费用；环绕等重复形态仍按触发付费。原生海浪对方块持续抵靠的已有去重与同次回调防重入不属于终生敌人去重，保留。

固定 Core：ed44ebb86bd087f3546a1bc6a315d2692f70262c（0.1.28），只读；现有共享实例 Core 不升级。独立工作树为 parallel-workspaces/cataclysm-repeat-continuation-dev62-20261023/Cataclysm-Nouveau。

## 验证

收拢后仅运行 cataclysm_nouveau_continuation 五项定向检查：现有地震波、旋刃、海浪、元素矛与电火花用例。更新相同目标第二次有效命中的预期，保留无敌帧失败、费用、空蓝停止、友军及清理检查。`runGameTestServer :verifyDistributions -PgameTestNamespaces=cataclysm_nouveau_continuation --offline --console=plain --no-daemon` 首轮 5/5 通过，102 项双语魔符与二进制／源码包发行边界通过。日志：隔离工作树 `dev62-continuation-tests.log`。没有客户端人工视觉复测。

## 产物 SHA-256

- `cataclysm-nouveau-0.1.0-dev.62-mc1.21.1-neoforge.jar`：`6486c1f7d438668aef87b32a1225c91f3c6b55039a5d6adc3de147b8d30f5692`
- `cataclysm-nouveau-0.1.0-dev.62-sources.jar`：`b6c566c69e783e71abc22b3b3f73f848645dbb259ffc32f254729c443e7d5b5c`
