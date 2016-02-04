#练习项目，简易记事本

#####本分支的图片存储：只保存其路径，然后SpanText显示为图片，图标的也如此，至于内存溢出，用工具类解决。
#####另外也有个把图库获取的图片存数据库的demo，请转[picDb分支](https://github.com/hanyuguang/Notepad/tree/pidDb)

####listview的item间隔：  
listview不设置背景，item的背景设置跟整个页面的背景不一样。  
```java
// listview settings
android:divider="#00000000"  
android:dividerHeight="10dp"  
```

####点listview的item响应checkbox：  
```java
// checkbox settings
android:focusable="false"  
android:checked="false"  
```
再设置listview的onItemClick()    


![Index](https://cloud.githubusercontent.com/assets/14596166/12810880/e864d76a-cb63-11e5-858e-c37e0df85ffa.png)  

![edit](https://cloud.githubusercontent.com/assets/14596166/12810943/54726b7a-cb64-11e5-9b31-f70766117e22.png)  
