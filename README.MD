在文本中图片只保存其路径，然后SpanText显示为图片，图标的也如此。至于内存溢出，用工具类解决了

listview的item间隔：
	listview不设置背景，item的背景设置跟整个页面的背景不一样，
	再在listview里设置
	android:divider="#00000000"
	android:dividerHeight="10dp"

点listview的item响应checkbox：
	checkbox设置
        android:focusable="false"
        android:checked="false"
    再设置listview的onItemClick()


//练习项目，请随意fork