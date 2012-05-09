/*******************************************************************************
 * Copyright (c) 2010-2011 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.views;

import java.text.Collator;
import java.util.Date;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.foosbar.mailsnag.model.Message;

/**
 * Sorter is responsible for ordering messages based on the selected column. It
 * also has the responsibility of determining if the column should be sorted in
 * ascending or descending order.
 * 
 * Default is descending order for newly selected columns.
 * 
 * @author Kevin Kelley
 */
public class MessageSorter extends ViewerSorter {

	private String columnName;

	private SortDirection direction;

	public MessageSorter() {
		direction = SortDirection.DESCENDING;
		columnName = "receiving";
	}

	public void setColumnName(String columnName) {
		direction = columnName.equalsIgnoreCase(this.columnName) ? direction
				.flip() : SortDirection.DESCENDING;
				this.columnName = columnName;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		if (columnName == null) {
			return 0;
		}

		Message m1 = (Message) e1;
		Message m2 = (Message) e2;

		int result = 0;

		if (MessagesView.COL_SUBJECT.equals(columnName)) {
			result = Collator.getInstance().compare(m1.getSubject(),
					m2.getSubject());
		} else if (MessagesView.COL_FROM.equals(columnName)) {
			result = Collator.getInstance().compare(m1.getFrom(), m2.getFrom());
		} else if (MessagesView.COL_TO.equals(columnName)) {
			result = Collator.getInstance().compare(m1.getTo(), m2.getTo());
		} else if (MessagesView.COL_CC.equals(columnName)) {
			result = Collator.getInstance().compare(m1.getTo(), m2.getTo());
		} else if (MessagesView.COL_RECEIVED.equals(columnName)) {
			result = compareDates(m1.getReceived(), m2.getReceived());
		}
		// If descending order, flip the direction
		return direction == SortDirection.DESCENDING ? -result : result;
	}

	public int compareDates(Date d1, Date d2) {
		if (d1 == null) {
			return -1;
		}
		if (d2 == null) {
			return 1;
		}
		return d1.compareTo(d2);
	}

	public enum SortDirection {
		ASCENDING,
		DESCENDING;

		public SortDirection flip() {
			if (this == ASCENDING) {
				return DESCENDING;
			} else {
				return ASCENDING;
			}
		}
	}
}