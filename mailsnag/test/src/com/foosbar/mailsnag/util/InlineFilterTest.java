package com.foosbar.mailsnag.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InlineFilterTest {

	private String sample1 = "<META NAME=\"GENERATOR\" CONTENT=\"GtkHTML/3.32.1\">\n</HEAD>\n<BODY>\n<IMG SRC=\"cid:1292510711.3937.0.camel@Liam.home\" ALIGN=\"bottom\" BORDER=\"0\">\n</BODY>\n</HTML>";
	private String sample2 = "<META NAME=\"GENERATOR\" CONTENT=\"GtkHTML/3.32.1\">\n</HEAD>\n<BODY>\n</BODY>\n</HTML>";
	private String sample3 = "<META NAME=\"GENERATOR\" CONTENT=\"GtkHTML/3.32.1\">\n</HEAD>\n<BODY>\n<img src=\'cid:1292510711.3937.0.camel@Liam.home\' ALIGN=\"bottom\" BORDER=\"0\">\n</BODY>\n</HTML>";
	private String sample4 = "<META NAME=\"GENERATOR\" CONTENT=\"GtkHTML/3.32.1\">\n</HEAD>\n<BODY>\n<img src=cid:1292510711.3937.0.camel@Liam.home ALIGN=\"bottom\" BORDER=\"0\">\n</BODY>\n</HTML>";
	private String sample5 = "<META NAME=\"GENERATOR\" CONTENT=\"GtkHTML/3.32.1\">\n</HEAD>\n<BODY>\n<img src=cid:1292510711.3937.0.camel@Liam.home>\n</BODY>\n</HTML>";
	private String sample6 = "<META NAME=\"GENERATOR\" CONTENT=\"GtkHTML/3.32.1\">\n</HEAD>\n<BODY>\n<IMG ALIGN=\"bottom\" BORDER=\"0\" SRC=\"cid:1292510711.3937.0.camel@Liam.home\">\n</BODY>\n</HTML>";
	
	private Pattern srcPattern;
	private int groupNumber = 6;
	
	@Before
	public void setUp() throws Exception {
		srcPattern = Pattern.compile("<(IMG)(.+?)(SRC\\s*?=\\s*?)([\"'\\s]{0,1})(cid:(.*?))([\"'\\s>]+)(.+?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindSrc1() {
		Matcher m1 = srcPattern.matcher(sample1);
		Assert.assertTrue(m1.find());
/*
		System.out.println("Match found");
		for(int x=0; x<=m1.groupCount(); x++) {
			System.out.println("  "+x+". " + m1.group(x));
		}
*/
		Assert.assertTrue(m1.group(groupNumber).equals("1292510711.3937.0.camel@Liam.home"));
	}

	@Test
	public void testFindSrc2() {
		
		Matcher m2 = srcPattern.matcher(sample2);
		Assert.assertFalse(m2.find());
	}

	@Test
	public void testFindSrc3() {
		Matcher m3 = srcPattern.matcher(sample3);
		Assert.assertTrue(m3.find());
		Assert.assertTrue(m3.group(groupNumber).equals("1292510711.3937.0.camel@Liam.home"));

	}

	@Test
	public void testFindSrc4() {
		Matcher m4 = srcPattern.matcher(sample4);
		Assert.assertTrue(m4.find());
		Assert.assertTrue(m4.group(groupNumber).equals("1292510711.3937.0.camel@Liam.home"));

	}

	@Test
	public void testFindSrc5() {
		Matcher m5 = srcPattern.matcher(sample5);
		Assert.assertTrue(m5.find());
		Assert.assertTrue(m5.group(groupNumber).equals("1292510711.3937.0.camel@Liam.home"));

	}

	@Test
	public void testFindSrc6() {
		Matcher m6 = srcPattern.matcher(sample6);
		Assert.assertTrue(m6.find());
		Assert.assertTrue(m6.group(groupNumber).equals("1292510711.3937.0.camel@Liam.home"));

	}

}
