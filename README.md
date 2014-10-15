ShareSDK 1.0
========
The support platforms:<br>
1:qq<br> 2:qzone<br>3:wechat<br>4:wechatmoments<br>5:sinaweibo<br>6:tencentweibo<br>7:renren<br>8:douban<br>
How to use:
========
(1) Copy the extracted ShareSDK into your workspace of Eclipse;<br><br>
(2) Add dependency of your project to ShareSDK;<br><br>
(3) Copy the "ShareSDK/asserts/platforms.xml" to "project/asserts";<br><br>
(4) Config the platforms.xml,the explaination of cofig params:<br><br>
id: String field,you must not change this field;<br><br>
appid,appkey,appsecret: String field,these field got from third platform;<br><br>
redirecturl,redirecturls: String field,redirecturl has higher priority,it's the callback url when oauth;<br><br>
authorizeurl: String field,the url of getting the oauth code;<br><br>
accesstokenurl: String field,the url of getting the access token;<br><br>
scope: String field,it determines the accessibility of open api;<br><br>
showpriority:int field,the importance of a platform<br><br>
(5) Config "project/AndroidManifest.xml",you can find the detail configuration in "ShareSDKSample/AndroidManifest.xml".
