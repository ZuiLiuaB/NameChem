# NameCrush - AI Name Compatibility Analyzer 💘

> _Find out if your names were meant to be._

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![Java 17](https://img.shields.io/badge/Java-17+-blue)
![GLM-4.5-Flash](https://img.shields.io/badge/AI-GLM--4.5--Flash-purple)

**NameCrush** 是一个基于 AI 的名字亲密度分析服务，利用智谱 AI 的 GLM 大模型，分析两个名字之间的音律、字义、搭配感，并给出“缘分值”评分和幽默点评。适合用于社交小程序、情感测试、情侣互动等场景。

---

## 🌟 功能特性

- ✅ 基于 **GLM-4.5-Flash** 模型分析名字匹配度
- ✅ 输出 0-100 分数 + 幽默/真实风格评价
- ✅ 支持 JSON 格式返回，易于集成
- ✅ 内置错误处理与降级机制
- ✅ 可扩展为 API 服务或小程序后端

---

## 🛠️ 环境要求

- Java 21 或更高版本
- Maven 3.6+
- Spring Boot 3.x
- 智谱 AI 平台账号（[https://www.zhipu.ai](https://www.zhipu.ai)）

---

## 🔑 配置智谱 AI API Key

1. 注册 [智谱 AI 开放平台](https://www.zhipu.ai) 账号
2. 在「个人中心」获取你的 API Key
3. 在 `application.yml` 或 `application.properties` 中配置：

```properties
# application.properties
zhipu.api.key=你的apikey
