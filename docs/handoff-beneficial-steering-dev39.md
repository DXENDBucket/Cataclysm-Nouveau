# 友军执行与转向交接（dev.39）

## 范围和基线

用户要求非友军增益命中失败/不扣Mana，体术正确随友军视角转向，自身/友军共用逻辑。工作树 `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-beneficial-steering\Cataclysm-Nouveau`，分支fix/beneficial-steering；只改Cataclysm附属。
固定Core `030880436d93ae104d6e14ba0240b6b1ba698513`（0.1.21），副本 `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-beneficial-steering\Ars-Conflux`，受版本控制文件无变化。NeoForge1.21.1/21.1.250，实际灾变3.33，JDK21；没有Forge移植、Core改动或附属硬依赖。

## 已完成和下一步

详见[dev.39说明](beneficial-steering-dev39.md)。原IgnisActionGeometry推广为ActionGeometry，十五种体术用同一个实际actor授权和方向处理。守卫/肩撞取消敌方→caster回退；火球阵/齐射/死亡激光也采用Core的beneficial锚点。原生Goal启动/推进后校正姿态，先驱者第一帧不再读取付款者，垂直视角的水平fallback使用actor自己的yaw。位置动作继续固定方向。中英文守卫描述已去掉旧回退说法。

下一步由用户在客户端确认友军视角、原生模型/方向与操作手感；没有客户端人工验证。不要再引入自用/友军两条方向实现，权限不能依据damageOwner。原生Ars已支付的起手形态费用不是本次目标失败退款范围。

## 验证证据

全部命令在上述工作树，JAVA_HOME `C:/Program Files/Java/jdk-21`。

- `./gradlew.bat :compileJava :compileGameTestJava --offline --console=plain` 通过，build/steering-compile.log。
- `./gradlew.bat :runGameTestServer :build -PgameTestNamespaces=cataclysm_nouveau_steering --offline --console=plain`：25项，22通过；三项失败为闪现/雷电连段轨迹精确比较及先驱者初始方向。build/steering-validation.log。
- 更正首次姿态，数值轨迹用角度容差比较，并统一焰魔到steer方法后，`-PgameTestNamespaces=cataclysm_nouveau_steering_focused`：6/6通过，build/steering-focused-validation.log。
- 修正垂直视角fallback并补专门检查，`-PgameTestNamespaces=cataclysm_nouveau_vertical`：1/1通过，build/steering-vertical-validation.log；最后这次也执行build及包检查。
- 共26个不同检查；不声称一次完整26/26执行。覆盖15种体术自身/第二名友军玩家的真实姿态轨迹、根付款、取消，23种入口敌方及非生物失败退款与不替换旧动作，攻击锁向的实际伤害/幻影、无效替换、实际落地、原生肩撞/雷电连段、实体/位置并存与位置回归。
- 最终build、二进制包/源码包verifyDistributions及git diff --check通过。没有本体素材/实现源码/依赖打包，专用服务器可启动并测试，无客户端类加载问题。
- Core副本git status --porcelain --untracked-files=no为空；没有其他联动全矩阵或客户端人工画面验证。

## 交付

版本0.1.0-dev.39，代码 `16b341a55209fba4cbf8299827b2de6e64517d10`，本地标签v0.1.0-dev.39。

- `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-beneficial-steering\Cataclysm-Nouveau\build\libs\cataclysm-nouveau-0.1.0-dev.39-mc1.21.1-neoforge.jar`
  SHA-256 `7346174b75c7354636df95beaf8bf4c4db20c3e9c9bd6183205aff38e9aff40e`
- `C:\Users\57991\Documents\MinecraftDev\parallel-workspaces\cataclysm-beneficial-steering\Cataclysm-Nouveau\build\libs\cataclysm-nouveau-0.1.0-dev.39-sources.jar`
  SHA-256 `79586c79cc8872074bf5bfec60a603e285e81a47c61d76f182702b5bdaf46b14`

由根协调者串行部署到万法归一mods，只有一个dev.39，复制哈希一致；旧dev.38保存在实例codex-backups/cat-beneficial-steering-dev39。Core0.1.21、Iron0.17.18与第一人称10%配置保留，没有修改万途归零，没有推送或发布。测试服务已退出，仅结束经所有构建工作目录核对属于本工作区的空闲Gradle daemon。

后续先读AGENTS.md、固定Core共享流程及本记录，核对真实Git状态；普通附属任务不改Core受版本控制文件，独立工作树构建，共享实例由协调者部署。仅新变化或明确疑点补验证。
