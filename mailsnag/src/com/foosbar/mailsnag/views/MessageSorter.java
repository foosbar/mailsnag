/**
 * 
 */
package com.foosbar.mailsnag.views;

import java.text.Collator;

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
		
		if(MessagesView.COL_SUBJECT.equals(columnName))
			result = Collator.getInstance().compare( m1.getSubject(), m2.getSubject() );
		
		else if(MessagesView.COL_FROM.equals(columnName))
			result = Collator.getInstance().compare( m1.getFrom(), m2.getFrom() );
		
		else if(MessagesView.COL_TO.equals(columnName))
			result = Collator.getInstance().compare( m1.getTo(), m2.getTo() );
		
		else if(MessagesView.COL_CC.equals(columnName))
			result = Collator.getInstance().compare( m1.getTo(), m2.getTo() );

		else if(MessagesView.COL_RECEIVED.equals(columnName))
			result = m1.getReceived().compareTo(m2.getReceived());
		
		// If descending order, flip the direction
		return (direction == DESCENDING) ? 
			-result: 
				result;
	}
}