ShareSDK 1.0
========
The support platforms:<br>
1:qq<br> 2:qzone<br>3:wechat<br>4:wechatmoments<br>5:sinaweibo<br>6:tencentweibo<br>7:renren<br>8:douban<br>
<br><br>
How to use:
========
(1) Copy the extracted ShareSDK into your workspace of Eclipse;<br>
(2) Add dependency of your project to ShareSDK;<br>
(3) Copy the "ShareSDK/asserts/platforms.xml" to "project/asserts";<br>
(4) Config the platforms.xml,the explain of cofig params:<br>
id:String field,you must not change this field;<br>
appid,appkey,appsecret:String field,these field got from third platform;<br>
redirecturl,redirecturls:String field,redirecturl has higher priority,it's the callback url when oauth;<br>
authorizeurl:String field,the url of getting the oauth code;<br>
accesstokenurl:String field,the url of getting the access token;<br>
scope:String field,it determines the accessibility of open api;<br>
showpriority:the importance of a platform<br>

