package com.foosbar.mailsnag.model;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.foosbar.mailsnag.util.MessageStore;

public class MessageTest {

	private Message message1;
	
	@Before
	public void setUp() throws Exception {
		message1 = new Message();
		message1.setCc("dev@foos-bar.com");
		message1.setFilename(MessageStore.getRandomFilename());
		message1.setFrom("dev@foos-bar.com");
		//message1.set
	}

	@After
	public void tearDown() throws Exception {
		message1 = null;
	}

	@Test
	public void testAddAttachment() {
		Assert.assertTrue(true);
		//fail("Not yet implemented");
	}

	@Test
	public void testGetAttachmentDir() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttachments() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCc() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFilename() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFrom() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetId() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetReceived() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSubject() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTo() {
		fail("Not yet implemented");
	}

	@Test
	public void testHasAttachments() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetAttachments() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCc() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFilename() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFrom() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetId() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetReceived() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSubject() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTo() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsUnread() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUnread() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsObject() {
		fail("Not yet implemented");
	}

}
