# gradeBook

GradeBook is a program that stores assignment data in user-created groups to store and calculate your grades. The easy customizability, custom grade calculation, and [minimal GUI](gradeBookUI.png) makes it a preferable alternative to a simple spreadsheet.

The current implementation reads and writes user data via marshalling and unmarshalling of .xml files via Jabx. The UI is made via windowBuilder and Swing, and data binding is achieved via netbeans. Upon application run, the program reads the current gradebook-jabx.xml file and [prints summary statistics](summaryStatsDemo.png). At the end of each session (and alongside each data edit), the application will write the current object architecture to file and to [system.out](XMLWriteDemo.png).

Currently, in the short term, I am working on creating a properties file that stores the grade definitions (what constitutes an A, etc), and creating a settings dialog to modify these definitions. In the long term, I am working on streamlining UI and implementing a multi-class system featuring multiple class slots. Furthermore, I want to implement group weighting, as well as a "statistics mode" where you can calculate the minimum scores required to achieve a desired grade.

huey 
