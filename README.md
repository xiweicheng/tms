# TMS(Teamwork Management System)
> TMS是基于频道模式的团队沟通协作+轻量级任务看板，支持mardown、富文本、在线表格和思维导图的团队博文wiki，i18n国际化翻译管理的响应式web开源团队协作系统。

**代码仓库:**  
- gitee：https://gitee.com/xiweicheng/tms  
- github：https://github.com/xiweicheng/tms  

 **前端代码仓库（代码已经压缩打包到tms仓库中）：** 
- 沟通博文：https://gitee.com/xiweicheng/tms-frontend
- 着陆首页：https://gitee.com/xiweicheng/tms-landing

## 操作手册

- [TMS用户手册（使用指导）](https://gitee.com/xiweicheng/tms/wikis/%E7%9D%80%E9%99%86%E9%A1%B5?sort_id=3692705)

## 如何运行

- [如何在开发工具中运行](https://gitee.com/xiweicheng/tms/wikis/%E5%A6%82%E4%BD%95%E5%9C%A8%E5%BC%80%E5%8F%91%E5%B7%A5%E5%85%B7%E4%B8%AD%E8%BF%90%E8%A1%8C?sort_id=351959)
- [TMS安装部署（传统方式）](https://gitee.com/xiweicheng/tms/wikis/TMS%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%EF%BC%88%E4%BC%A0%E7%BB%9F%E6%96%B9%E5%BC%8F%EF%BC%89?sort_id=21982)
- [TMS安装部署（docker-compose）](https://gitee.com/xiweicheng/tms/wikis/TMS%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%EF%BC%88docker-compose%EF%BC%89?sort_id=21977)
- [TMS安装部署（k8s方式）](https://gitee.com/xiweicheng/tms/wikis/TMS%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%EF%BC%88k8s%E6%96%B9%E5%BC%8F%EF%BC%89?sort_id=3201498)


## Image show

![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/095513_cf21d89f_19723.png "屏幕截图.png")
- 着陆页
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/095934_1f5d8d9f_19723.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/100018_b2efad09_19723.png "屏幕截图.png")
- 沟通
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/095724_988eab53_19723.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/095637_6624a690_19723.png "屏幕截图.png")
- 博文
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/095759_aed0a835_19723.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2021/1016/112533_8bb4e2a0_19723.png "屏幕截图.png")
- i18n国际化翻译
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/100111_088dcdf4_19723.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2020/0524/100201_956e39e6_19723.png "屏幕截图.png")

## GIF show 

**着陆页** 
![着陆页](http://imagizer.imageshack.us/a/img923/7662/AC8F68.gif)

**国际化翻译**  
![国际化翻译](http://imageshack.com/a/img924/7042/t1Lto8.gif) 

**团队沟通**  
![团队沟通](http://imageshack.com/a/img923/9146/CaecMK.gif) 

**团队博文(wiki)**  
![团队博文](http://imageshack.com/a/img922/9337/yhtpMU.gif) 

**移动端响应式设计**  
![移动端响应式设计](http://imageshack.com/a/img923/4710/QkXzJb.gif) 

## 具有以下功能:
- 团队协作沟通功能(类似于[slack](https://slack.com/) [bearychat](https://bearychat.com/))
- 团队博文(wiki) 类似 [精简版confluence](http://baike.baidu.com/link?url=0TtAZuIP9nh31TCEQVSjtgS6-oUt9_M2mgdHu3XBSgF8DZR7u_Yv-XmUK3Yz133kx_2AhlFufEJhHJOgshXJJYst78ahDRto5NsSwWqdMBy) [蚂蚁笔记](https://leanote.com/)
- 国际化翻译管理.

#### 沟通功能（基于websocket实时通讯）
- 频道(组团沟通)（二级话题消息沟通）
- 私聊(一对一)
- markdown语法支持(内容排版不再单调)
- @消息 收藏消息 富文本消息目录
- 频道外链（便于团队将常用链接统一到一处）
- 频道甘特图（方便项目整体规划管理）
- 频道任务看板（可拖拽）
- 频道固定消息
- 日程安排(提醒)
- 待办事项
- 沟通消息标记表情&标签（方便分类过滤检索）
- 剪贴板上传图片, 拖拽文件上传(就是这么便捷)
- 文件上传，从csv、excel导入mardown表格
- 邮件通知、桌面通知、toastr通知(不用担心错过什么)
- 热键支持(鼠标点多了也累不是)
- 自定义皮肤色调
- 自定义频道组（方便批量一次性@一类多个人）
- 更多贴心便捷操作等你发现

#### 团队博文(wiki)
- 博文空间(便于博文组织,权限隔离)
- Markdown、Html富文本、电子表格、思维导图、图表工具多种类型博文创作方式
- 基于博文模板创建（可自由发布私有、公开的模板）
- 博文目录（支持拖拽排序）、标签
- 父子级博文（支持五级父子博文）
- 博文关注,收藏,历史（版本比较、回退）,权限,点赞,分享,开放游客访问
- 博文评论
- 博文多人协作编辑（需开启博文协助权限）
- 导出 pdf、markdown、html、excel、png
- 基于websocket的博文更新实时通知（协作更及时便捷）
- 完整的博文操作变更历史审计和通知消息中心
- 更多贴心便捷操作等你发现


#### 国际化（i18n）翻译管理
**包括以下核心功能模块**
- 翻译项目管理
- 翻译语言管理
- 翻译导入导出
- 翻译管理

#### 其他功能
- 系统设置
- 用户管理

#### 赞助
- ![支付宝转账赞助](https://git.oschina.net/uploads/images/2017/0511/150721_33f53da9_19723.jpeg "支付宝转账赞助") 
- ![微信支付转账赞助](https://git.oschina.net/uploads/images/2017/0511/150742_d8f40866_19723.png "微信支付转账赞助") 
- 或者通过项目提供的赞助入口赞助. 

#### 免责声明

> TMS项目中使用了不少很棒的第三方的开源依赖库，如果计划将本TMS软件用于商业通途，可能部分依赖库涉及到版权授权和付费购买问题，请自行联系并且购买相关依赖库的版本授权License，**对于可能发生的版权纠纷和侵权等问题，TMS软件在此声明将不承担任何法律责任**，在此感谢您对于对于TMS的喜爱和鼓励，谢谢！  


#### 许可(license)
MIT