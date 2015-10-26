MailSnag
========

MailSnag is an Eclipse plugin to support development of email generating applications.  The plugin creates a simple smtp server on the port of your choice and will listen for incoming emails.  The emails can be displayed in HTML or Text view, as well as a RAW view in order to inspect the actual output from you application.

Support
=======
The plugin has been verified to work with Eclipse 3.5 (Galileo) through 4.5 (Mars).  There is an optional dependency on Mylyn.  If Mylyn is installed, you can receive popup notifications when a new email arrives.

Installation
============
Two ways to install.  The easiest is to open the Eclipse Marketplace from within Eclipse and search for MailSnag. 
Alternatively, you can install it directly from the update site:

1. From the Eclipse menubar, select: "Help" > "Install New Software..."
2. Click the "Add" button to add a new repository.
3. Enter a Name:  FoosBar
4. Enter a Location: http://foosbar.github.com/update/site.xml
5. Click Ok.

Select "Foos-Bar Development" > "MailSnag" and click "Next >" a few times until it eventually installs.

Restart Eclipse and start spamming yourself!

Getting Around
==============
To use, you need to open the MailSnag Viewer from the Eclipse Menubar: "Window" > "Show View" > "Other...".  Find "MailSnag" from the list and select "Email Messages".

![ScreenShot](http://foosbar.github.com/images/mailsnag-default.png)

You can go directly to MailSnag's preferences by selecting the downward pointing triangle in the upper right side of the view or navigating through the general Eclipse preferences.  That is where you can set the port to listen for incoming emails.  On non-windows systems you will probably have to pick a port number greater than 1024 unless running as root - but why would you do that!

![ScreenShot](http://foosbar.github.com/images/mailsnag-preferences.png)

The option "Enable notification popup when new messages arrive" will only appear if Mylyn Commons is installed.

License
=======
The code for MailSnag is licensed under Eclipse Public License v1.0 - http://www.eclipse.org/legal/epl-v10.html
