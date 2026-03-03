# 🥇 OmniTab

**OmniTab** is a high-performance, universal TAB plugin for Minecraft servers (Spigot & Paper) supporting versions from **1.7.10 to 1.21.x** with a single JAR.

## ✨ Features
- **Universal Compatibility**: One JAR for all versions (1.7.10 → 1.21.x).
- **Animated Header/Footer**: Dynamic and colorful tablist aesthetics.
- **Smart Sorting**: Permission-based group sorting.
- **Async Operations**: Zero impact on server TPS.
- **Placeholder Support**: Built-in placeholders + PlaceholderAPI integration.
- **RGB/Hex Support**: Modern color support for 1.16+ versions.
- **bStats Integration**: Anonymous metrics for usage tracking.

## 🚀 Installation
1. Download the latest `OmniTab.jar`.
2. Drop it into your `plugins` folder.
3. Restart your server.
4. Configure your styles in `plugins/OmniTab/config.yml`.

## 🛠 Developer API
OmniTab provides a clean API for other plugins to interact with the tablist.
```java
TablistHandler handler = OmniTab.getInstance().getTablistHandler();
// Custom logic here
```

## 📜 License
This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

## 🤝 Contributing
Contributions are welcome! Please read our [Contribution Guidelines](CONTRIBUTING.md).

---
*Created with elite autonomous engineering by OmniTab Dev Team.*
