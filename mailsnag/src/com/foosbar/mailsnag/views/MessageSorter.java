/**
 * 
 */
package com.foosbar.mailsnag.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.foosbar.mailsnag.model.Message;

/**
 * @author Kevin Kelley
 *
 */
public class MessageSorter extends ViewerSorter {

	private static final int DESCENDING = 1;

	private String columnName;
	private int direction;

	public MessageSorter() {
		direction = DESCENDING;
		columnName = "receiving";
	}

	public void setColumnName(String columnName) {
		direction = 
			(columnName.equalsIgnoreCase(this.columnName)) ?
				direction = 1 - direction : 
					DESCENDING;

		this.columnName = columnName;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		if(columnName == null)
			return 0;
		
		Message m1 = (Message) e1;
		Message m2 = (Message) e2;

		int result = 0;
		
		if(columnName.equals(MessagesView.COL_SUBJECT))
			result = m1.getSubject().compareTo(m2.getSubject());
		
		else if(columnName.equals(MessagesView.COL_FROM))
			result = m1.getFrom().compareTo(m2.getFrom());
		
		else if(columnName.equals(MessagesView.COL_TO))
			result = m1.getTo().compareTo(m2.getTo());
		
		else if(columnName.equals(MessagesView.COL_CC))
			result = m1.getTo().compareTo(m2.getTo());

		else if(columnName.equals(MessagesView.COL_RECEIVED))
			result = m1.getReceived().compareTo(m2.getReceived());
		
		// If descending order, flip the direction
		return (direction == DESCENDING) ? 
			-result: 
				result;
	}
}