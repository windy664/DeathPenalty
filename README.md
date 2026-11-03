<div align="center">

<img src="assets/banner.svg" alt="DeathPenalty" width="100%">

# DeathPenalty

**死亡惩罚** —— 每次死亡削减最大生命值上限，吃金苹果恢复；给生存服加一层真实的代价。

![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper%20%2F%20Mohist-62bda8?style=flat-square&logo=minecraft&logoColor=white)
![Minecraft](https://img.shields.io/badge/MC-1.20.x-8a9a5b?style=flat-square)
![Java](https://img.shields.io/badge/Java-17-f89820?style=flat-square&logo=openjdk&logoColor=white)

[功能](#-功能) · [命令](#-命令) · [配置](#️-配置)

</div>

## ✨ 功能

- **生命上限削减**：每次死亡按 `health-decrease-per-death` 扣减玩家最大生命值（属性级修改）
- **金苹果恢复**：吃金苹果按 `health-restore-per-golden-apple` 回复生命上限
- **上限保护**：`health-max` 限制最高生命值，防止无限叠
- **Mohist 兼容**：`mohist: true` 针对混合端的重生行为做适配
- **管理命令**：`/swcf` 系列支持重载配置、切换 debug、调整玩家生命上限

## 📜 命令

| 命令 | 说明 |
|------|------|
| `/swcf reload` | 重载配置与玩家数据 |
| `/swcf debug` | 切换调试模式 |
| `/swcf setmaxhealth <数值>` | 设置生命上限 |

## ⚙️ 配置

```yaml
health-decrease-per-death: 2.0     # 每次死亡扣 1 颗心
health-restore-per-golden-apple: 10.0
health-max: 40.0
prefix: "§7§l[§c§l死亡惩罚§7§l] "
mohist: true
debug: false
```

## 🔨 构建

```bash
./gradlew build
```
