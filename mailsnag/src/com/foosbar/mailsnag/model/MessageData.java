/*******************************************************************************
 * Copyright (c) 2010-2012 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.model;

/**
 * Models a message received via SMTP.
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class MessageData {

	private String htmlMessage;
	private String message;
	private String textMessage;

	public MessageData() {
	}

	public String getHtmlMessage() {
		return htmlMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public boolean hasHtmlMessage() {
		return htmlMessage != null;
	}

	public boolean hasTextMessage() {
		return textMessage != null;
	}

	public void setHtmlMessage(String htmlMessage) {
		this.htmlMessage = htmlMessage;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}
}