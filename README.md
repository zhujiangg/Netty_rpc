# 用netty手写rpc框架

#### 介绍
使用netty手写rpc框架

#### 软件架构
![输入图片说明](https://images.gitee.com/uploads/images/2021/0516/130649_f82f9eff_8044183.png "屏幕截图.png")


#### 安装教程

下载maven导入依赖
开启注册中心 
直接启动测试类

#### 执行流程
借用Guide的一张图

![输入图片说明](https://images.gitee.com/uploads/images/2021/0516/130825_6cab1cf2_8044183.png "屏幕截图.png")

具体执行流程

1.
![输入图片说明](https://images.gitee.com/uploads/images/2021/0516/131232_624ffa54_8044183.png "屏幕截图.png")

2.所以我们客户端需要构建和服务器端能接收的通用请求

我们本地写和服务器端一样的接口  然后用代理模式 发送请求给服务器端 服务器端返回结果响应


![执行流程](https://images.gitee.com/uploads/images/2021/0516/134406_67d5cf06_8044183.png "屏幕截图.png")


整合了注册中心和自定义注解 自动注册服务

