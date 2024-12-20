package com.ajaxjs.util;


import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.ajaxjs.util.StrUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStrUtil {
	final static String str = "中国";

	@Test
	public void testByte2String() {
		assertEquals("abc", byte2String(new byte[] { 97, 98, 99 }));
		assertEquals("abc", byte2String("abc"));
		assertEquals("中国", byte2String(str));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testConcatUrl() {
		assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "/bbb/sds"));
		assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "bbb/sds"));
		assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "bbb/sds"));
		assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "/bbb/sds"));
	}

	@Test
	public void testLeftPad() {
		assertEquals("@@@@@12345", leftPad("12345", 10, "@"));
	}

	@Test
	public void messageFormat() {
		MessageFormat.format("您好{0}，晚上好！您目前余额：{1,number,#.##}元，积分：{2}", "张三", 10.155, 10);
	}

	@Test
	public void testJoin() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");

		System.out.println(join(list, "&"));
	}
}
