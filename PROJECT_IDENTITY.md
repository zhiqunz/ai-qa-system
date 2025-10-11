# 项目身份标识

## 项目类型
**领域驱动设计 (Domain-Driven Design, DDD) 项目**

## 架构分层
本项目严格遵循领域驱动设计的分层架构：

### 📋 分层结构
```
backend-services/
├── api-gateway/          # API网关层
├── qa-service/          # 问答服务 - 限界上下文
│   └── src/main/java/com/ai/qa/service/
│       ├── api/                 # 接口层 (Interface Layer)
│       │   ├── controller/      # REST控制器
│       │   ├── dto/            # 数据传输对象
│       │   └── exception/      # 异常处理
│       ├── application/        # 应用层 (Application Layer)
│       │   ├── dto/            # 应用DTO
│       │   └── service/        # 应用服务
│       ├── domain/             # 领域层 (Domain Layer) - 核心！
│       │   ├── model/          # 领域模型 (实体、值对象)
│       │   ├── repo/           # 领域仓库接口
│       │   └── service/        # 领域服务
│       └── infrastructure/     # 基础设施层 (Infrastructure Layer)
│           ├── feign/          # 外部服务调用
│           └── persistence/    # 持久化实现
└── user-service/        # 用户服务 - 另一个限界上下文
```

## 🎯 DDD 核心概念体现

### 1. 限界上下文 (Bounded Context)
- **问答上下文 (QA Context)**: `qa-service`
- **用户上下文 (User Context)**: `user-service`

### 2. 领域层核心地位
- 领域模型 (`domain/model/`) 包含业务核心逻辑
- 领域服务 (`domain/service/`) 处理复杂业务规则
- 领域仓库接口 (`domain/repo/`) 定义数据访问契约

### 3. 分层依赖规则
- 接口层 → 应用层 → 领域层 ← 基础设施层
- 领域层不依赖任何其他层，保持纯粹性

### 4. 统一语言 (Ubiquitous Language)
- QAHistory (问答历史)
- QARAG (问答RAG模型)
- 领域术语在代码中保持一致

## 🔧 技术栈
- **后端**: Java 17 + Spring Boot + Maven
- **前端**: Next.js + TypeScript
- **架构**: 微服务 + DDD

## 📋 开发规范
1. 所有业务逻辑必须放在领域层
2. 应用层只负责流程编排，不包含业务规则
3. 基础设施层实现领域层定义的技术细节
4. 接口层只处理HTTP请求和响应格式

## 🚀 记住
这是一个**领域驱动设计**项目，核心是：
- **业务优先** - 领域模型反映真实业务
- **边界清晰** - 各层职责分明
- **统一语言** - 代码与业务语言保持一致