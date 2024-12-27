---
title: 实体类字段脱敏
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-docs.njk
---
# 实体类字段脱敏

脱敏就是现实某些敏感的字段完全暴露数据，但又不能完全消去，保留一部分信息即可判断，常见如姓名、手机、邮箱、用户名、密码等字段。


- DeSensitizeUtils：这个类对实体进行脱敏后返回的是原来的实体对象，它直接在原始对象上进行操作，并对其进行修改。
- SensitizeUtils：而这个类则创建了一个新的对象实例（或集合），并在这个新对象上应用脱敏规则。这意味着原对象保持不变，而返回的是一个结构相同但值被脱敏处理过的新对象。


# 同类开源

- https://gitee.com/strong_sea/sensitive-plus 使用了`MappingJackson2HttpMessageConverter`这点不错，同时也比较全面，还支持日志脱敏，可是代码组织太分散了
- https://github.com/chenqi92/alltobs-desensitization-all
- https://github.com/mingyang66/spring-parent/tree/master/emily-project/oceansky-desensitize 代码简洁清晰
