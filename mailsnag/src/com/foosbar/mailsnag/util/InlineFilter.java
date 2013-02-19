/*******************************************************************************
 * Copyright (c) 2010-2013 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.Message.Attachment;

public class InlineFilter {

	private static final Pattern SRC_PATTERN = Pattern
			.compile(
					"<(IMG)(.+?)(SRC\\s*?=\\s*?)([\"'\\s]{0,1})(cid:(.*?))([\"'\\s>]+)(.+?)>",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	public static String filter(Message message, String content, String path) {

		Matcher m = SRC_PATTERN.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {

			String contentId = m.group(6);
			Attachment a = message.getAttachments().get(contentId);

			if (a == null) {
				continue;
			}

			m.appendReplacement(sb, "<$1$2$3$4" + path + "/" + contentId + "/"
					+ a.getName() + "$7$8>");

		}
		m.appendTail(sb);
		return sb.toString();
	}
}