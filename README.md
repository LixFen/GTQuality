# GTQuality

GTQuality 是面向 Minecraft 1.7.10、Forge 和 GregTech 6 的辅助模组，提供机器交互、碰撞箱、物品与流体处理，以及 HUD 和 NEI 集成。

## 功能

### 机器与物品交互

- **选择器电路选号**：手持集成电路Shift + 右键，打开 GUI 选择 0–24 号。
- **模具雕刻**：手持凿子潜行右键模具，在界面中选择雕刻形状。
- **漏斗碰撞箱**：为普通漏斗和排队漏斗提供贴合模型的选取与碰撞检测。
- **GUI 流体交互**：在支持的 GT6 机器 GUI 中，允许直接使用流体容器交互。
- **流体管道单向端口**：手持扳手 Shift + 右键 GT6 流体管道的一端，将该端口切换为“只出不进”。
- **创造模式储罐**：在 GTQuality 创造物品栏或 NEI 中获取，无合成配方。
- **遮挡交互**：`allowObstructedInteraction` 开启时，机器面被相邻方块遮挡也可以交互。
- **树脂袋自动提取**：允许漏斗等侧面物品接口从其提取树脂产物。
- **储物形态切换**：手持凿子右键储物桶或存储器，可在该材料形态间切换。

### AE2 联动

新增 **ME 粉末归并存储元件**，作为 AE2 联动。

- **容量**：每个元件容纳 64 种材料，每种最多存储 1,000,000 个标准粉。
- **自动归并**：接受粉、小堆粉、小撮粉、1/72 粉和粉末块，默认按材料量转换为标准粉。
- **形态标记**：将元件放入元件工作台，在标记槽中放入所需粉末形态。例如标记小撮铁粉后，铁材料的主槽、ME 显示和输出均改为小撮铁粉；未标记的铜仍使用标准粉。元件仍可存储其他粉末。同种材料有多个有效标记时，最靠前的槽位生效。安装反相卡后，适用标记白名单机制。
- **ME 网络与余量**：网络只显示和允许提取主槽形态。

### HUD 与界面

- **传感器侧向连线**：红石线从传感器上下左右侧面连接时，补齐连线。通过客户端配置 `sensorWireConnections` 开启。
- **WDMla 集成**：提供 GT6 机器状态、物品内容、配方进度与产物、材料和储罐等信息，也注册 GT6 储存与采掘提示。WDMla 为可选依赖；`wdmlaIntegration` 可关闭全部 GTQuality 的 WDMla 注册。若开启此项，建议移除[OO](https://github.com/exzhawk/OmniOcular)中GT6的脚本。
- **工具状态条**：用GTM进度条显示工具耐久和充能。安装 DuraDisplay 时，此渲染功能不会启用。
- **NEI 集成**：在 NEI Catalyst 支持 GT6 配方 handler；并提供高级工作台的配方填充支持。过滤器的 GUI 支持拖入 NEI 幻影物品。
- **GT6 物品折叠组**：自动将同种机器、容器、管线和多方块部件的不同材料或等级归组.
- **GT6 多方块预览**：安装 BlockRenderer6343 及其依赖后，在 NEI 中对 GT6 多方块控制器查询配方或用途，可查看结构模型和所需部件；对部件查询用途可反查对应结构。
- **GT6 资源生成页面**：在 NEI 中对矿石查询配方或用途，可查看其资源生成页。可通过 `client.neiWorldgenDisplay` 配置关闭此功能。

### 移动与碰撞

- **脚手架移动**：提供额外的上爬或下滑速度，可分别配置；安装 GaiaTweaks 时，上爬速度默认设为 `0`。移除了阻碍移动的两根立柱的碰撞。
- **活板门进气**：关闭的原版活板门可作为固体、液体和流化床燃烧箱的进气位置。预计同样支持Et Futurum Requiem的活板门。
- **小型覆盖板碰撞箱**：修正小型的流体管道和红石线安装部分小型覆盖板后，选取范围错误扩大的问题。

### 生存与防护

- **通用防护服增强**：穿齐四件全防服时提供 20 点护甲（10 格）。增多了全防服耐久。
- **耐热服火焰免疫**：穿齐 GT6 认可的耐热防护装备时，免疫接触火、持续燃烧、岩浆等火焰伤害。

## 配置

配置文件位于 `config/gtquality.cfg`。

| 配置项 | 分类 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `gtmToolBars` | `client` | `true` | 启用工具耐久和充能状态条。 |
| `sensorWireConnections` | `client` | `false` | 补齐 GT6 传感器侧面的红石连线；关闭时保留原版渲染，修改后需重启游戏。 |
| `wdmlaIntegration` | `client` | `true` | 启用 GTQuality 的 WDMla HUD、储存和采掘提示注册。关闭后需重启游戏。 |
| `neiMultiblockPreview` | `client` | `true` | 安装 BlockRenderer6343 时启用 GT6 多方块结构预览。关闭后需重启游戏。 |
| `neiItemGroups` | `client` | `true` | 将 GT6 同类机器的材料、等级变体及材料前缀物品加入 NEI 折叠组，需重启游戏。 |
| `allowObstructedInteraction` | `general` | `true` | 禁用 GT6 的机器遮挡检查。 |
| `guiFluidInteraction` | `general` | `true` | 启用通过机器 GUI 流体槽进行的容器灌注和抽取。 |
| `oneWayFluidPipes` | `general` | `true` | 启用潜行扳手右键设置流体管道端口只出不进；关闭时暂停限制并保留存档设置。 |
| `allowSapBagHopperExtraction` | `general` | `true` | 允许侧面物品接口从 Resin/Sap Bag 提取产物。 |
| `heatHazmatFireImmunity` | `general` | `true` | 穿齐 GT6 认可的耐热防护装备时免疫火焰伤害，包括全防服。 |
| `circuitSelectorGui` | `general` | `true` | 启用手持选择器电路 Shift + 右键选号 GUI 和操作提示。 |
| `universalHazmatEnhancement` | `general` | `true` | 启用全套全防服 20 点护甲及每件 4 倍耐久；关闭后恢复 GT6 原有护甲和耐久上限。 |
| `scaffoldClimbUpSpeed` | `general` | `0.14`；安装 GaiaTweaks 时为 `0` | 最大抬头角度下的额外上移速度，单位为格/tick；`0` 关闭。 |
| `scaffoldClimbDownSpeed` | `general` | `0.15` | 最大低头角度下的额外下移速度，单位为格/tick；`0` 关闭。 |
| `fixAngelicaUnicodeFont` | `client` | `false` | 启动Angelica兼容的FontsFix修复; 未与其余字体mod测试，谨慎开启 |

## 开发与构建

- Minecraft：`1.7.10`
- Forge：`10.13.4.1614`
- GregTech 6：`6.17.06`
- Mod ID：`gtquality`
- 主类：`com.plainston.gtquality.GTQuality`

Windows：

```bat
gradlew.bat build
```

Linux/macOS：

```sh
./gradlew build
```

## 特别鸣谢

感谢 [mordds/GT6WailaCompact](https://github.com/mordds/GT6WailaCompact) 为 GT6 WDMla联动提供参考。
