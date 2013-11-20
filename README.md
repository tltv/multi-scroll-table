MultiScrollTable Vaadin component
=================================

Vaadin Table component with a support of multiple scroll contents. A single Vaadin container can be divided into a several ScrollContents that shares a same vertical scroll bar and have individual horizontal scroll bar.

Key features:
* multiple scroll contents for one container data source ("frozen columns")
* lazy loaded rows
* hierarchical headers
* hierarchical rows (expand/collapse)


TODO
----

List of incomplete features:

* Client & Server
	* refactor all legacy stuff (UIDL) away and use shared states and RPC for the communication.
	* floating rows (that can be used as a footer for example)
	* fixed column widths, expand ratios
	* theme
	
* Client side 
	* hierarchical row structure
	* improve layout management and support for scaling
	  
	
------------

* [Vaadin 7 (7.x.x)](http://vaadin.com)



License
-------

Apache License, Version 2.0

