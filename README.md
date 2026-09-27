# GTQuality — GregTech 6 体验，重新定义

> **你与一台好机器之间，只差一个 GTQuality。**
> 为 GregTech 6 打造的“手感补完计划”：更聪明的模具、更听话的漏斗、更顺手的流体、更清爽的界面。
> 装上它，你会怀疑原版 GT6 到底是怎么忍过来的。

GTQuality 是基于 **Minecraft 1.7.10 Forge** 与 **GT New Horizons 构建框架**的轻量增强 Mod——不加一条新配方、不破坏任何平衡，只把 GT6 里那些“就差一点”的地方，一次性做到位。

---

## ✨ 为什么你需要它

### 🎨 1. 凿子雕刻模具，一个界面全搞定
还在对着模具右键一下、猜一下、再右键一下地试形状？**现在：手持凿子 → 蹲下 → 右键模具**，专属选择界面直接弹出。

- **分页式形状选择器**：所有可用雕刻类型一目了然，8 个一页，`<` `>` 自由翻页，页码实时显示
- **智能排序**：你上次用过的形状永远排在第一位，普通锭形排在 `Raw` 半成品之前，找东西不用再翻
- **一键清空**：中间的「清空」按钮随时把模具恢复为“未雕刻”状态
- **当前状态可见**：界面顶部实时显示“当前: xxx”，改之前先看清楚
- **记忆你的习惯**：上次选的形状会随玩家数据一起保存，下次打开直接置顶
- **安全校验**：只在模具为空、手持凿子、距离 8 格以内时生效——服务端逐条把关，别想作弊
- **新手提示**：模具方块的物品 ToolTip 会自动加上一行灰字教程，拿到手就知道怎么用

### 🪣 2. 会“看形状”的智能漏斗
GT6 的漏斗明明是梯形，碰撞箱却是一整块方砖？**GTQuality 给漏斗换上了真正贴合模型的碰撞箱。**

- 精确射线检测：漏斗身、锥形收口、朝向出口管，**指哪点哪**
- 高亮框只框住你真正指到的那一部分，视觉反馈精准到像素
- 贴了 Cover 的漏斗？**Cover 自身的碰撞盒也会一并参与检测**，覆盖件不再“看不见摸不着”
- **普通漏斗与排队漏斗（Queue Hopper）同时支持**，注册表级无侵入替换，不改存档、不改 ID

### 💧 3. GUI 流体直通：光标上的容器，直接灌进机器
不用再把流体容器塞进槽里等它自己慢慢转——**打开机器 GUI，手里拿着桶 / 单元 / 任意流体容器，直接点击流体槽位：**

- **左键 / 右键点击**：存入或取出一次的量（自动判断该存还是该取）
- **Shift + 点击**：整组一口气处理完，批量操作爽快到底
- 完整支持 **Forge `IFluidContainerItem` 可变流体容器**与 GT6 的单元/桶体系
- 自动计算可接受量，**不会溢出、不会凭空造液**，容器与槽位逐一结算
- 操作完成后立刻同步背包与机器状态，**零掉帧、零幽灵物品**

### 🔧 4. 打通“被堵住”的机器交互
机器正面被方块顶住就完全不能点了？那是 GT6 的遮挡检查在作怪。GTQuality 提供配置开关：

- `allowObstructedInteraction`（默认开启）：**禁用 GT6 的遮挡检查**，被相邻方块挡住的机器面照样右键交互
- 紧凑布线、贴墙机组、密集产线——**再也不用为了点一下机器预留一个空气格**

### 📊 5. GTM 风格工具状态条
还在辨认 GT6 那一小坨耐久与充能图标？**换上现代感十足的渐变进度条。**

- **耐久度**：绿色渐变条（`0x147c00 → 0x73ff59`），越短越危险
- **EU 充电量**：蓝色渐变条（`0x0065b2 → 0xd9eeff`），电量一眼见底
- **危急警报**：数值低于阈值时进度条**自动变红**，想忽略都难
- 平滑着色（smooth shading）+ 半透明底槽，物品栏、GUI 内全部生效
- 纯客户端渲染，服务端无需安装，**不影响任何存档与联机**
- 检测到 DuraDisplay（modid `duradisplay`）时自动停用此渲染器，避免显示冲突

### 📖 6. NEI 深度整合
- **全机器催化剂注册**：遍历 GT6 所有 `RecipeMap`，把每台机器注册为对应配方表的 **Recipe Catalyst**——在 NEI 里按下 R/J，立刻只看“这台机器能做什么”
- **高级工作台一键填充修复**：为 `MultiTileEntityAdvancedCraftingTable` 注册专属 Overlay Handler，正确识别其 0–20 号槽位，**配方一键摆放不再报错**

### 🌲 7. 树脂袋接上漏斗，采集终于能自动化

树脂袋里已经攒了树脂，还得隔一会儿手动掏一次？**让漏斗来接班。**

- GT6 Resin/Sap Bag 的产物槽现在允许从侧面提取，漏斗等使用侧面物品接口的设备可以自动取走树脂
- **所有方向都能接**：不用为了导出物品反复调整漏斗位置
- `allowSapBagHopperExtraction` 默认开启；想保留原本的手动采集方式，在配置里关闭即可

### 🪜 8. 脚手架攀爬，抬头上、低头下

爬 GT6 脚手架时，方向感可以更直觉一点：**看向哪里，就往哪里走。**

- **抬头并向前移动**：额外向上攀爬，抬头越高，加速越明显
- **低头且不向前移动**：额外向下滑行，低头越多，下降越快
- **视角向前时不触发**；上下速度可分别调整，设为 `0` 就关闭对应方向的加速
- 安装 GaiaTweaks 时，上爬加速默认关闭，避免两边的攀爬手感叠在一起

### 🔥 9. 燃烧箱与活板门，留出进气口

想用活板门做一扇可开合的燃烧箱进气口，却被 GT6 的碰撞与供氧判断卡住？**关闭的原版活板门，现在也能作为进气位置。**

- 对固体、液体与流化床燃烧箱的进气判断生效：关闭的活板门不会因碰撞箱挡住检测，也会被视为有氧气
- 打开活板门时仍按 GT6 原有的碰撞与供氧逻辑判断；其他方块不受影响

### 🧰 10. 细管线上的小 Cover，不再一贴就变整格

红石线贴上红石火把 Cover 后，碰撞箱突然膨胀成一整格？**现在会按管线和 Cover 的实际范围计算。**

- 支持细流体管、普通红石线与绝缘红石线，保留各连接方向伸出的管线范围
- 红石火把、红石中继器与压力阀 Cover 的本体、固定件才会计入碰撞箱，点选时不再凭空碰到一整块空气
- 贴有其他类型 Cover、包覆泡沫或本身为整格尺寸时，继续使用 GT6 原有的碰撞箱

---

## ⚙️ 配置一览

配置文件：`config/gtquality.cfg`

| 配置项 | 分类 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `gtmToolBars` | client | `true` | 用 GTM 风格进度条替换 GT6 的工具充能/耐久图标 |
| `allowObstructedInteraction` | general | `true` | 关闭 GT6 遮挡检查，允许交互被方块挡住的机器面 |
| `guiFluidInteraction` | general | `true` | 允许通过 GUI 槽位，用光标上的流体容器灌注/抽取机器储罐 |
| `allowSapBagHopperExtraction` | general | `true` | 允许料斗等自动化从 GT6 Resin/Sap Bag 提取树脂物品 |
| `scaffoldClimbUpSpeed` | general | `0.14`；安装 GaiaTweaks 时为 `0` | 抬头前进时额外上移量（最大抬头角度时，格/tick）；`0` 关闭加速 |
| `scaffoldClimbDownSpeed` | general | `0.15` | 低头且不前进时额外下移量（最大低头角度时，格/tick）；`0` 关闭加速 |

---

## 📦 安装与构建

```bash
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

- **Mod ID**：`gtquality`
- **Java 包**：`com.plainston.gtquality`
- **入口类**：`com.plainston.gtquality.GTQuality`
- **Minecraft**：`1.7.10`（Forge）
- **必需依赖**：**GregTech 6 `6.17.06`**（开发产物自动从 [GT6 Maven](https://gregtech.overminddl1.com/) 解析）
- **Mixin 运行时依赖**：**UniMixins**
- **可选联动**：[GTNH NotEnoughItems `2.8.144-GTNH`](https://github.com/GTNewHorizons/NotEnoughItems)、[CodeChickenCore `1.4.21`](https://github.com/GTNewHorizons/CodeChickenCore)

> JitPack 支持（`jitpack.yml`），CI 自动构建与打 Tag 发版（GitHub Actions）。

---

## 🗺️ 功能速查

| 提交 | 功能 |
| --- | --- |
| `Auto resin bag & Trapdoor control & Faster scaffold climb speed` | 树脂袋自动导出、燃烧箱活板门进气、脚手架攀爬加速 |
| `Duardisplay compat` | 检测到 DuraDisplay 时停用 GTM 工具状态条，避免显示冲突 |
| `Better redstone wire cover hitbox` | 细管线贴小型 Cover 时的碰撞箱修复 |
| `GUI fluid interaction` | GUI 内流体容器直灌/直抽 |
| `Better hopper` | 漏斗/排队漏斗精确碰撞箱与 Cover 检测 |
| `Better chisel mold & Better bar & Better obstructed interaction` | 凿子模具选择 GUI、GTM 工具状态条、无障碍交互 |
| `GT6 Catalysts` | NEI 全机器催化剂 + 高级工作台配方填充 |
| `init commit` | GTNH 构建脚手架、CI、许可证 |

---

**GTQuality —— 少一点摩擦，多一点心流。**
*装上它，把时间还给真正重要的事：造更多机器。*
