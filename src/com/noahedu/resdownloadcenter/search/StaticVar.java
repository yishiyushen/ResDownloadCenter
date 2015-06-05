package com.noahedu.resdownloadcenter.search;

public class StaticVar {
		
	public static String ALL_CONDITION = "条件不限";
	
	public static String ALL_SUBJECT = "全部科目";
	public static String CHINESE_SUBJECT = "语文";
	public static String ENGLISH_SUBJECT = "英语";
	public static String MATH_SUBJECT = "数学";
	public static String PHYSICAL_SUBJECT = "物理";
	public static String CHEMISTRY_SUBJECT= "化学";
	public static String HISTORY_SUBJECT = "历史";
	public static String POLITICS_SUBJECT = "政治";
	public static String GEOGRAPHY_SUBJECT = "地理";
	public static String BIOLOGY_SUBJECT = "生物";
	
	public static String ALL_GRADE = "所有年级";
	public static String GRADE_ONE = "一年级";
	public static String GRADE_TWO = "二年级";
	public static String GRADE_THREE = "三年级";
	public static String GRADE_FOUR = "四年级";
	public static String GRADE_FIVE = "五年级";
	public static String GRADE_SIX = "六年级";
	public static String GRADE_SEVEN = "七年级";
	public static String GRADE_EIGHT = "八年级";
	public static String GRADE_NIGHT = "九年级";
	public static String GRADE_HIGH_ONE = "高一";
	public static String GRADE_HIGH_TWO = "高二";
	public static String GRADE_HIGH_THREE = "高三";
	
	public static String SCHOOL_TERM_UP = "上册";
	public static String SCHOOL_TERM_DOWN = "下册";
	public static String SCHOOL_TERM_ALL = "全一册";
	
	public static String ALL_PUBLISHER ="全部出版社";
	
	public static final int GET_NET_DATA_FAILED = 0x1001;
	
	public static final String BOOK_COVER_IMG_DIR = "bitmap";
	
	/**获取功能目录接口*/
	public static final String CONTENT_MENU_URL = "http://192.168.71.213:8082/udlcenter/catalogue/apcatalogs";
	/**条件筛选接口*/
	public static final String FILTER_MENU_URL = "http://192.168.71.213:8082/udlcenter/catalogue/getSourceParam.Ajax";
	/**从搜索引擎里搜索资源接口*/
	public static final String SEARCH_RESOURCE_URL = "http://192.168.71.213:8082/udlcenter/catalogue/searchsource";
	/**获取获取同步课件的关联课件接口*/
	public static final String ASSOCIATE_RESOURCE_URL = "http://192.168.71.213:8082/udlcenter/catalogue/getrelsourcebysourid";
	/**获取目录接口*/
	public static final String GET_BOOK_SV_DIR = "http://192.168.71.213:8082/udlcenter/catalogue/courseware/getucatalog";
}
