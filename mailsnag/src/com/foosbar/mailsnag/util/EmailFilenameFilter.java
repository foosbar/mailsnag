/**
 * 
 */
package com.foosbar.mailsnag.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author kevin
 *
 */
public class EmailFilenameFilter implements FilenameFilter {

	public EmailFilenameFilter() {
	}

	public boolean accept(File dir, String filename) {
		if(filename == null)
			return false;
		
		return filename.endsWith(".eml");
	}

}
