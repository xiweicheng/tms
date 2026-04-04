# Java Spring Boot 技能

## 技能描述

该技能提供Java Spring Boot相关的代码生成、问题解决和最佳实践建议，适用于TMS项目的后端开发。

## 核心功能

### 1. 代码生成

#### 1.1 控制器(Controller)生成
- 基于RESTful API设计规范生成控制器代码
- 支持CRUD操作的控制器方法
- 包含适当的异常处理和参数验证

#### 1.2 服务(Service)生成
- 生成业务逻辑服务代码
- 包含事务管理和业务规则实现
- 支持服务层接口和实现分离

#### 1.3 数据访问(Dao/Repository)生成
- 基于JPA生成数据访问代码
- 支持自定义查询和复杂条件查询
- 包含适当的实体关系映射

#### 1.4 实体(Entity)生成
- 生成数据库实体类
- 包含适当的字段定义和注解
- 支持实体间关系定义

#### 1.5 工具类(Util)生成
- 生成通用工具类
- 包含日期处理、加密解密、字符串处理等功能

### 2. 问题解决

#### 2.1 常见错误排查
- 数据库连接问题
- 依赖冲突问题
- 配置错误问题
- 性能问题

#### 2.2 最佳实践建议
- 代码结构优化
- 性能优化
- 安全最佳实践
- 可维护性改进

### 3. 代码审查

#### 3.1 代码质量检查
- 代码风格检查
- 潜在bug检测
- 性能问题识别
- 安全漏洞检测

#### 3.2 代码优化建议
- 重构建议
- 设计模式应用
- 性能优化建议
- 代码可读性改进

## 使用示例

### 示例1: 生成控制器代码

**输入**:
```
生成一个用户控制器，包含CRUD操作
```

**输出**:
```java
package com.lhjz.portal.controller;

import com.lhjz.portal.entity.User;
import com.lhjz.portal.service.UserService;
import com.lhjz.portal.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        user.setId(id);
        return ResponseEntity.ok(userService.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
```

### 示例2: 生成服务层代码

**输入**:
```
生成一个用户服务，包含基本的CRUD操作
```

**输出**:
```java
package com.lhjz.portal.service;

import com.lhjz.portal.entity.User;
import com.lhjz.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findOne(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.delete(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
```

## 注意事项

1. 生成代码时，请确保遵循项目的代码风格和命名规范
2. 生成代码后，请根据实际需求进行适当的调整和修改
3. 对于复杂的业务逻辑，请提供详细的需求描述
4. 如有特定的技术要求或限制，请在输入中明确说明