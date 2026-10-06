# GTQuality

GTQuality 是面向 Minecraft 1.7.10、Forge 和 GregTech 6 的辅助模组，提供机器交互、碰撞箱、物品与流体处理，以及 HUD 和 NEI 集成。

## 功能

### 机器与物品交互

- **选择器电路选号**：手持集成电路（选择器电路）Shift + 右键空气或方块，打开 GUI 选择 0–24 号；修改手持整组电路，保留数量、NBT 和比较模式，Esc 取消。
- **模具雕刻**：手持凿子潜行右键模具，在界面中选择雕刻形状；也可以清除当前形状。模具物品提示包含操作说明。
- **漏斗碰撞箱**：为普通漏斗和排队漏斗提供贴合模型的选取与碰撞检测，并处理 Cover 的检测。
- **GUI 流体交互**：在支持的 GT6 机器 GUI 中，点击流体槽可以用光标上的流体容器向机器注液或取液。Shift 点击可连续处理光标堆叠中的容器。
- **创造模式储罐**：在 GTQuality 创造物品栏或 NEI 中获取，无合成配方。右键打开 GT GUI，将 NEI 幻影流体或装有流体的容器拖入流体槽选择类型，也可用光标上的流体容器点击选择，不消耗物品。可切换自动输出，设置 `0–2147483647 L/tick` 的输出速率（默认 `1000`）；`0` 停止输出。自动输出默认关闭，开启后向六面相邻流体接口推送；管道抽取和自动输出共享每 tick 的总速率限制。点击 `X` 或空光标右键流体槽清除选择，设置随存档和方块掉落保留。
- **遮挡交互**：`allowObstructedInteraction` 开启时，机器面被相邻方块遮挡也可以交互。
- **树脂袋自动提取**：允许漏斗等侧面物品接口从 Resin/Sap Bag 提取树脂产物。
- **储物形态切换**：手持凿子右键 GT6 物品储物桶或存储器，可在该材料支持的物品形态间切换；未能组成整件的材料保留在余量中。

### HUD 与界面

- **WDMla 集成**：提供 GT6 机器状态、物品内容、配方进度与产物、材料和储罐等信息，也注册 GT6 储存与采掘提示。WDMla 为可选依赖；`wdmlaIntegration` 可关闭全部 GTQuality 的 WDMla 注册。若开启此项，建议移除[OO](https://github.com/exzhawk/OmniOcular)中GT6的脚本。
- **工具状态条**：用GTM进度条显示工具耐久和充能。安装 DuraDisplay 时，此渲染功能不会启用。
- **NEI 集成**：将 GT6 配方机器注册为 Recipe Catalyst，并提供高级工作台的配方填充支持。物品、流体和物品形态过滤器的 GUI 支持拖入 NEI 幻影物品设置过滤条件，不消耗物品；悬停过滤槽可查看提示。流体过滤器可使用 NEI 流体显示物品或装有流体的容器。

### 移动与碰撞

- **脚手架移动**：根据视角和移动方向提供额外的上爬或下滑速度，可分别配置；安装 GaiaTweaks 时，上爬速度默认设为 `0`。移除了阻碍移动的两根立柱的碰撞。
- **活板门进气**：关闭的原版活板门可作为固体、液体和流化床燃烧箱的进气位置。预计同样支持Et Futurum Requiem的活板门。
- **小型 Cover 碰撞箱**：修正小型的流体管道和红石线安装部分小型 Cover 后，选取范围错误扩大的问题。

### 生存与防护

- **通用防护服增强**：穿齐四件全防服时提供 20 点护甲（10 格），实际减伤同步增强；脱下任一件后恢复原有护甲效果。每件最大耐久由 128 提高至 512，旧装备保留已损耗数值及 NBT，无需迁移；例如已损耗 100 的旧装备更新后剩余 412。卸载本模组后上限恢复原值，损耗已超过 128 的装备可能在下次受损时损坏。
- **耐热服火焰免疫**：穿齐 GT6 认可的耐热防护装备时，免疫接触火、持续燃烧、岩浆等火焰伤害。

## 配置

配置文件位于 `config/gtquality.cfg`。GTQuality 在启动时读取配置，修改后需要重启游戏。电路选号和全防服增强的开关应在客户端与服务端保持一致；服务端关闭电路选号时会拒绝选号请求。关闭全防服增强后耐久上限恢复为 128，已有损耗数值保持不变，损耗超过原上限的装备可能在下次受损时损坏。

| 配置项 | 分类 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `gtmToolBars` | `client` | `true` | 启用工具耐久和充能状态条。 |
| `wdmlaIntegration` | `client` | `true` | 启用 GTQuality 的 WDMla HUD、储存和采掘提示注册。关闭后需重启游戏。 |
| `allowObstructedInteraction` | `general` | `true` | 禁用 GT6 的机器遮挡检查。 |
| `guiFluidInteraction` | `general` | `true` | 启用通过机器 GUI 流体槽进行的容器灌注和抽取。 |
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
