图文混排：
    拍照的图片保存到SD卡，然后文本框保存其图片路径，通过正则匹配图片在文本中出现的地方，再DecodeFile加到文本里

    【图库获取的图片存到数据库，同样正则匹配，But存数据库在图片大的时候各种内存溢出。。】

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