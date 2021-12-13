# 北京地铁预约出行 - 定时预约系统

#### 介绍

每天早上九点会检查第二天是否为工作日  
如果为工作日中午十二点自动进行预约操作  
access_token有效期大概在一周
由于官网的刷新token接口不对，只能检查token有效期进行提醒

#### 使用

##### 1.修改预约站点
默认抢票点为：昌平线-沙河站
可以在TaskMetro.java中进行修改

##### 2.修改抢票时间段及个人账号access_token
打开application.properties
对应修改即可，注意格式

#### 3.修改src/main/resources/mail.setting文件
加了邮件提醒防止token过期忘记更换，预约成功与否都会发邮件
注：邮箱需开启SMTP服务

##### 4.执行就完事了