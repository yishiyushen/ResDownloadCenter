package com.noahedu.resdownloadcenter.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PressMap {

	private static Map<String, Integer> pressMap = new HashMap<String, Integer>();

	static {
		pressMap.put("长春出版社", 0);
		pressMap.put("北京出版社", 0);
		pressMap.put("河北教育出版社", 0);
		pressMap.put("上海科学技术出版社", 0);
		pressMap.put("青岛出版社", 0);
		pressMap.put("山东教育出版社", 0);
		pressMap.put("人民教育出版社", 0);
		pressMap.put("济南出版社", 0);
		pressMap.put("华东师范大学出版社", 0);
		pressMap.put("上海教育出版社", 0);
		pressMap.put("外语教学与研究出版社", 0);
		pressMap.put("山东人民出版社", 0);
		pressMap.put("译林出版社", 0);
		pressMap.put("牛津大学出版社", 0);
		pressMap.put("岳麓书社", 0);
		pressMap.put("山东科学技术出版社", 0);
		pressMap.put("人民出版社", 0);
		pressMap.put("北京师范大学出版社", 0);
		pressMap.put("北京出版社", 0);
		pressMap.put("中国地图出版社", 0);
		pressMap.put("湖南教育出版社", 0);
		pressMap.put("语文出版社", 0);
		pressMap.put("广东教育出版社", 0);
		pressMap.put("江苏教育出版社", 0);
		pressMap.put("教育科学出版社", 0);
		pressMap.put("北京出版社", 0);
		pressMap.put("北京教育出版社", 0);
		pressMap.put("江苏科学技术出版社", 0);
		pressMap.put("译林出版社", 0);
		pressMap.put("湖北教育出版社", 0);
		pressMap.put("四川教育出版社", 0);
		pressMap.put("浙江教育出版社", 0);
		pressMap.put("上海外语教育出版社", 0);
		pressMap.put("四川人民出版社",0);
		pressMap.put("西南师范大学出版社",0);
	}

	public static Map<String, Integer> getMap() {
		return pressMap;
	}

	public static String[] getPresses() {
		Set<String> set = pressMap.keySet();
		String[] strArray = new String[set.size()];
		return set.toArray(strArray);
	}
}
