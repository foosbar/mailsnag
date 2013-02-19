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
package com.foosbar.mailsnag.views;

import java.text.DateFormat;
import java.util.Locale;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.foosbar.mailsnag.model.Message;

/**
 * Returns the label for each of the columns based on the index for that
 * column.
 * 
 * @author Kevin Kelley
 */
public class MessageLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableFontProvider, ITableColorProvider {

	// Locale Specific Date Formatter
	private static final DateFormat DATE_FORMATTER = DateFormat
			.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT,
					Locale.getDefault());

	/**
	 * Based on the index of each column, return the proper value to populate
	 * the data.
	 */
	public String getColumnText(Object obj, int index) {
		Message message = (Message) obj;
		switch (index) {
		case 0:
			return null;
		case 1:
			return message.getFrom();
		case 2:
			return message.getTo();
		case 3:
			return message.getCc();
		case 4:
			return message.getSubject();
		case 5:
			if (message.getReceived() == null) {
				return "";
			} else {
				return DATE_FORMATTER.format(message.getReceived());
			}
		default:
			throw new RuntimeException("Should not happen");
		}
	}

	/**
	 * Returns an attachment icon for the first column (index == 0) if the
	 * message contains an attachment
	 */
	public Image getColumnImage(Object obj, int index) {
		if (index > 0) {
			return null;
		}

		Message message = (Message) obj;

		if (message.hasAttachments()) {
			return getImage(obj);
		}

		return null;
	}

	@Override
	public Image getImage(Object obj) {
		return Images.ATTACHMENT.createImage();
	}

	/**
	 * Not overriding the default foreground color
	 */
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	/**
	 * Not overriding the default background color
	 */
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	public Font getFont(Object element, int columnIndex) {
		return getFont((Message) element);
	}

	public Font getFont(Message message) {
		if (message.isUnread()) {
			return JFaceResources.getFontRegistry().getBold(
					JFaceResources.DIALOG_FONT);
		} else {
			return null;
		}
	}
}