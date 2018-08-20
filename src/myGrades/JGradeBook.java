package myGrades;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import myGrades.model.Assignment;
import myGrades.model.GradeGroup;
import myGrades.model.GradeGroups;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.jdesktop.swingbinding.JComboBoxBinding;

import java.awt.event.WindowEvent;

import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.ObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


/*************************************************************
				  ~ CURRENLTY WORKING ON: ~
 **************************************************************/

/////////////////////////// FLAGS //////////////////////////////
// FLAG 1: Currently, editing assignment scores updates group totals on enter press.
// This may be better than having it live update (current implementation one key behind).

// FLAG 2: Jframe delete group dialog needed


/////////////////////////// TO-DO //////////////////////////////
// Make one way of tracking updates, whether through actionEvent or documentListener
// Set up settings (display style? weights?)
// Handle floats in input scores? Half-points, showing decimal if necessary
// Drag and drop the assignments / Groups
// More JFrames
// Fix ComboBox binding- m_names might be necessary??


///////////////////////// DESIGN Q's ///////////////////////////
// Better to have an editable all folder? or force all new assignments in all into a misc. folder? 
//		Might be annoying?
// Should I auto-delete groups if there are no more assignments in the group? 
//		Probably not? There's a delete group already
// How should I handle groups

/**
 * @author huey_
 * 
 * Shouts to WindowBuilder tutorials
 * v.02  
 */

public class JGradeBook extends JFrame {
	
	// Data elements
	private static final long serialVersionUID = 1L;
	private GradeGroups m_groups = new GradeGroups();
	// Consider removing, currently used for drop-down
	// Might be necessary for proper data binding
	// Needs support in Groups class to be viable
	private List<String> m_names = new ArrayList<String>();
	
	////////////// J-Items //////////////
	
	// Left Panel
	private JSplitPane m_contentPane;
	private JList m_groupList;
	private JPanel groupToolbar;
	private JButton m_newGroupButton;
	private JButton m_editGroupButton;
	private JButton m_deleteGroupButton;
	
	// Right Panel
	// Top section
	private JButton m_newAssignmentButton;
	private JButton m_deleteAssignmentButton;
	private JButton m_classSettingsButton;
	private JLabel lblClassGrade;
	private JTextField m_classGradeTF;
	private JTable m_AssignmentTable;
	private JScrollPane scrollPane;
	private JPanel panel;
	
	// Assignment Details
	// Some JLabels created in constructor
	// (lblName, lblScore, etc.)
	private JTextField m_nameTF;
	private JTextField m_scoreTF;
	private JTextField m_totalScoreTF;
	private JTextField m_gradeTF;
	private JTextField m_percentTF;
	private JLabel lblGroup;
	private JLabel lblPercent;
	private JLabel lblDivider;

	
	// Group Details & Labels
	private JComboBox m_groupCB;
	private JTextField m_groupScoreTF;
	private JTextField m_groupTotalScoreTF;
	private JTextField m_groupPercentTF;
	private JTextField m_groupGradeTF;
	private JLabel lblClassScore;
	private JLabel lblGroupDivider;
	private JLabel lblGroupPercent;
	private JLabel lblGroupGrade;

	// File Name
    private static final String GRADEBOOK_XML = "./gradebook-jaxb.xml";

    /**
     * Main method of the GradeBook class.
     * 
     * Creates new gradeBook, which initializes values from the stored .xml file.
     * Sets frame as visible, and autoSelects the (All) group.
     */
	public static void main(String[] args) throws JAXBException, IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JGradeBook frame = new JGradeBook();
					frame.setVisible(true);
					
					// (All) group is always the first group, and cannot be deleted
					// This allows user to immediately see data
					// Move this code if we implement a multi-class enabled system
					frame.m_groupList.setSelectedIndex(0);
					frame.m_groups.update();
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
    /**
     * Helper method that marshals current objects to .xml
     * Essentially saves the data.
     * 
     * Method is called whenever data is changed, and again when the window is closed.
     * This prevents sudden unexpected shutdowns or force-quits from wiping the data.
     */
	private void save() throws JAXBException, IOException {
		
		// create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(GradeGroups.class);
        
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // For debugging- we don't want to have system print on each autosave.
        // m.marshal(m_groups, System.out);

        // Write to file
        m.marshal(m_groups, new File(GRADEBOOK_XML));	
	}
	
	
    /**
     * setInitalValues() reads and unmarshals data from our local .xml file
     * 
     * It relinks the (All) group- more info in method comments.
     * Prints some summary information to ensure file is loaded,
     * 
     * Currently commented-out section is some testing data.
     * To load that data instead of the .xml file, follow the instructions below.
     * Careful! Upon Jframe close, it will overwrite your current save file.
     */
	private void setInitialValues() throws JAXBException, IOException {
		
		// TO LOAD INITIAL TEST DATA, COMMENT OUT THE FOLLOWING CODE UNTIL YOU SEE "STOP" COMMENT!
		
		// create JAXB context 
		JAXBContext context = JAXBContext.newInstance(GradeGroups.class);

		// Unmarshal objects from our .xml file:
        Unmarshaller um = context.createUnmarshaller();
        GradeGroups myGroups = (GradeGroups) um.unmarshal(new FileReader(GRADEBOOK_XML));
        
        // Our marshalling process only saves the names and AssignmentLists of each group. 
        // We need to go through and update the %s and grades.
        for (GradeGroup group : myGroups.getGroups()) {
        	group.update();   	
        }
        // Update class grade. 
        myGroups.update();
        // Now we're good! This is a full dataset. Set local variable
        m_groups = myGroups;
        
        ////////////////////////////// RELINKING //////////////////////////////
        /* Here's why we need to relink our data, and what that means:
         * When we add a new Assignment, that object is added to both the target group and the (All) group.
         * Thus, changing the fields of one automatically changes the other, and the same with deleting.
         * 
         * However, when we marshal and unmarshal the data, this relationship is lost.
         * The (All) group is populated with new Assignments based on its data.
         * relink() recreates the linkage between Assignments in their original group and (All).
         */
        m_groups.relink();
        
        // Printing summary in information
        System.out.println();
        System.out.println("Input from local XML File: ");
        System.out.println("Class grade: " + m_groups.getClassGrade());
        System.out.println();
        
        // In the style of:
        // Group: GroupName (AssignmentCount), Grade: GroupGrade
        List<GradeGroup> groupList = m_groups.getGroups();
        for (GradeGroup group : groupList) {       	
        	System.out.println("Group: " + group.getName() + " (" + group.getAssignmentCount() + "), Grade: " + group.getgroupGrade());
        	for (Assignment a : group.getAssignments()) {
     		
        		// In the style of:
        		// Assignment: AssignmentName    Score: myScore/totalScore, Grade: AssignmentGrade
        		StringBuilder assign = new StringBuilder();
        		assign.append("   Assignment: " + a.getName());
        		// This makes the Scores more-or-less line up after the varying Assignment names
        		// I'm not concerned about extra-long names messing up the formatting, it'll just push that line, no big deal.
        		while (assign.length() < 25) {
        			assign.append(" ");
        		}
        		assign.append(" Score: " + a.getmyScore() + "/" + a.getfullScore() + ", Grade: " + a.getGrade());
        		System.out.println(assign.toString());
        	}
        	System.out.println();
        }	
        
        
        // STOP! NOW UN-COMMENT THE REST OF THIS METHOD!
        
        // Testing Data: 7 Assignments
        /**
        // Initialize groups 1 & 2, populate
		GradeGroup group1 = new GradeGroup("All");
		m_groups.addGroup(group1);
		GradeGroup group2 = new GradeGroup("Homework");
		m_groups.addGroup(group2);
		Assignment a1 = new Assignment("Reading1", 13, 15);
		Assignment a2 = new Assignment("Worksheet", 15, 30);
		Assignment a3 = new Assignment("Pset", 20, 20);
		group2.addAssignment(a1);
		group2.addAssignment(a2);
		group2.addAssignment(a3);
		
		// Initialize group 3, populate
		GradeGroup group3 = new GradeGroup("Quiz");
		m_groups.addGroup(group3);
		Assignment a4 = new Assignment("Quiz 1", 25, 25);
		Assignment a5 = new Assignment("Quiz 2", 20, 22);
		Assignment a6 = new Assignment("Pset", 20, 20);
		group3.addAssignment(a4);
		group3.addAssignment(a5);
		group3.addAssignment(a6);
		
		// Initialize group 4, populate
		GradeGroup group4 = new GradeGroup("Test");
		m_groups.addGroup(group4);
		Assignment a7 = new Assignment("Test 1", 98, 100);
		group4.addAssignment(a7);
		
		// Populate All (linked)
		group1.addAssignment(a1);
		group1.addAssignment(a2);
		group1.addAssignment(a3);
		group1.addAssignment(a4);
		group1.addAssignment(a5);
		group1.addAssignment(a6);
		group1.addAssignment(a7);
		
		// Artifacts
		m_names.add("All");
		m_names.add("Homework");
		m_names.add("Quiz");
		m_names.add("Test");
		m_names.add("Participation");
		m_names.add("Projects");
		m_names.add("Misc.");
		
		*/
	}

	
	/**
     * The constructor! Does most of the heavy lifting.
     * 
     * This method creates and describes the panels: their specifics, appearances,
     * and eventActions. Much of this code is generated by and can be edited within
     * the windowBuilder design tab. 
     */
	public JGradeBook() throws JAXBException, IOException {
		// Load values
		setInitialValues();
		setTitle("GradeBook");
		
		// Last routine save on window close
		// Not worried about force-quits, as data is saved on edit
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent event) {
	            try {
	        		// This is exactly save(), except it writes to system!
	                JAXBContext context = JAXBContext.newInstance(GradeGroups.class);	                
	                Marshaller m = context.createMarshaller();
	                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

	                // Print the output, displays as xml file
	                System.out.println("XML file written:");
	                m.marshal(m_groups, System.out);
	                m.marshal(m_groups, new File(GRADEBOOK_XML));
	                // I'm still a little happy when I see this actually work. Thank you stack overflow <3
	                System.out.println();
	                System.out.println("Save Successful. See you next time!");
	                
				} catch (JAXBException e) {
					e.printStackTrace();
				}
	            // The normal close mechanism after saving
	            dispose();
	            System.exit(0);
	        }
	    });
		
	    // Panel dimensions
		setBounds(100, 100, 965, 744);
		m_contentPane = new JSplitPane();
		m_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(m_contentPane);
		
		// Most of the Panel specs are either self-explantory or used within windowBuilder design
		// I highly recommend using it to edit or view the JElements.
		{
			JPanel leftPanel = new JPanel();
			leftPanel.setBorder(null);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 0, 0 };
			gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
			gridBagLayout.columnWeights = new double[] { 1.0, 1.0E-4 };
			gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };
			leftPanel.setLayout(gridBagLayout);
			m_contentPane.setLeftComponent(leftPanel);
			{
				groupToolbar = new JPanel();
				FlowLayout flowLayout = (FlowLayout) groupToolbar.getLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.insets = new Insets(0, 0, 5, 0);
				gbc.gridx = 0;
				gbc.gridy = 0;
				leftPanel.add(groupToolbar, gbc);
				{
					m_newGroupButton = new JButton("New...");
					// We want to create a new Group
					m_newGroupButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// For some reason, it bugs if an Assignment is selected.
							// This is the quick-fix: deselect Assignment and reset text fields.
							// This doubles to avoid editing a "ghost file" artifact from deleted group
							m_AssignmentTable.clearSelection();
							m_nameTF.setText("");
							m_scoreTF.setText("");
							m_gradeTF.setText("");
							m_percentTF.setText("");
							m_totalScoreTF.setText("");																						                 					                					                           
							m_AssignmentTable.repaint();
							
							// Pops out JFrame to enter new group name.
							// Same JFrame as edit group name!
							GradeGroup group = new GradeGroup("New Group");
							JGradeGroupDialog dialog = new JGradeGroupDialog(m_names, group);
							dialog.setLocationRelativeTo(m_contentPane);
							dialog.setVisible(true);
							m_groups.addGroup(group);
							// We want to select the new group, which is automatically placed at the end
							// Consider adding after currently selected group, or the ability to re-order groups?
							m_groupList.setSelectedIndex(m_groups.getGroups().size()-1);
							m_groupList.repaint();
						}
					});
					groupToolbar.add(m_newGroupButton);
				}
				{
					m_editGroupButton = new JButton("Edit");
					m_editGroupButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// See JGradeGroupDialog for details
							GradeGroup group = m_groups.getGroups().get(m_groupList.getSelectedIndex());
							JGradeGroupDialog dialog = new JGradeGroupDialog(m_names, group);
							dialog.setLocationRelativeTo(m_contentPane);
							dialog.setVisible(true);
						}
					});
					groupToolbar.add(m_editGroupButton);
				}
				{
					m_deleteGroupButton = new JButton("Delete");
					m_deleteGroupButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (!m_AssignmentTable.getSelectionModel().isSelectionEmpty()) {
								m_AssignmentTable.clearSelection();
								// Need to set fields to clear so you can't edit ghost item
								// Could possible simplify following into a helper method but its only needed once or twice
								m_nameTF.setText("");
								m_scoreTF.setText("");
								m_gradeTF.setText("");
								m_percentTF.setText("");
								m_totalScoreTF.setText("");																						                 					                					                           
								m_groupList.repaint();
								m_AssignmentTable.repaint();
							}
							int index = m_groupList.getSelectedIndex();
							
							// Make sure you can't delete (All) folder!
							if (index == 0) {
								// FLAG 2: Jframe error message
								
							} else {
								// After deleting a group, we need to remove those Assignments from (All)
								// This is where relink() was crucial!
								GradeGroup delete = m_groups.getGroups().get(index);
								GradeGroup all = m_groups.getGroups().get(0);
								for (Assignment assign : delete.getAssignments()) {
									all.removeAssignment(assign);
								}
								m_groups.removeGroup(delete);
							}						
							// I played around with different autoselect styles and this felt best:
							// Select the group that was formerly in its place.
							// Or if it was the last one, select the new last.
							if (index <= m_groups.getGroups().size()-1) {
								m_groupList.setSelectedIndex(index);
							} else {
								m_groupList.setSelectedIndex(index-1);
							}
							
							// To be 100% honest, not quite sure what revalidate does but it works..
							m_groupList.revalidate();
							m_groupList.repaint();
							m_AssignmentTable.repaint();
						}
					});
					groupToolbar.add(m_deleteGroupButton);
				}
			}
			{
				m_groupList = new JList();
				m_groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		        m_groupList.addListSelectionListener(new ListSelectionListener() {
		        	// On group change, nullify previous group's assignment selection.
		        	// This is just mop-up work.
		            @Override
		            public void valueChanged(ListSelectionEvent arg0) {
		                if (!arg0.getValueIsAdjusting()) {
		                  m_AssignmentTable.clearSelection();		  
		                  m_AssignmentTable.repaint();
		                  m_nameTF.setText("");
		                  m_scoreTF.setText("");
		                  m_gradeTF.setText("");
		                  m_percentTF.setText("");
		                  m_totalScoreTF.setText("");
		                }
		            }
		        });
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.BOTH;
				gbc.gridx = 0;
				gbc.gridy = 1;
				leftPanel.add(m_groupList, gbc);
			}
		}
		{
			JSplitPane rightPanel = new JSplitPane();
			rightPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
			rightPanel.setBorder(null);
			m_contentPane.setRightComponent(rightPanel);
			{
				JPanel topPanel = new JPanel();
				GridBagLayout gridBagLayout = new GridBagLayout();
				gridBagLayout.columnWidths = new int[] { 215, 0, 0 };
				gridBagLayout.rowHeights = new int[] {41, 426, 10, 0};
				gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
				gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0E-4 };
				topPanel.setLayout(gridBagLayout);
				rightPanel.setLeftComponent(topPanel);
				{
					panel = new JPanel();
					GridBagConstraints gbc_panel = new GridBagConstraints();
					gbc_panel.anchor = GridBagConstraints.WEST;
					gbc_panel.insets = new Insets(0, 0, 5, 5);
					gbc_panel.fill = GridBagConstraints.VERTICAL;
					gbc_panel.gridx = 0;
					gbc_panel.gridy = 0;
					topPanel.add(panel, gbc_panel);
					{
						m_newAssignmentButton = new JButton("New...");
						m_newAssignmentButton.addActionListener(new ActionListener() {
							// Add Assignment object to selected group & all group
							public void actionPerformed(ActionEvent e) {
								Assignment assign = new Assignment();
								GradeGroup all_group = m_groups.getGroups().get(0);	
								
								// need tableIndex for autoSelection of new Assignment later
								// default assumes that we're adding to all
								int tableIndex = all_group.getAssignmentCount();
								all_group.addAssignment(assign);
								
								// Check to see if you're adding to all to avoid double-adding
								int index = m_groupList.getSelectedIndex();		
								if (index == 0){
									// Add to misc folder if there (???), otherwise, create it
									
								} else {
									GradeGroup group = m_groups.getGroups().get(index);
									// update tableIndex if we're not adding to (All)
									tableIndex = group.getAssignmentCount();
									group.addAssignment(assign);
								}
								// Autoselect new
								m_AssignmentTable.setRowSelectionInterval(tableIndex, tableIndex);
								m_groupList.repaint();
								m_AssignmentTable.repaint();
							}
						});
						panel.add(m_newAssignmentButton);
						{
							m_deleteAssignmentButton = new JButton("Delete");
							m_deleteAssignmentButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									// Delete from selected group & all group
									int index = m_groupList.getSelectedIndex();
									GradeGroup group = m_groups.getGroups().get(index);
									GradeGroup all_group = m_groups.getGroups().get(0);
									
									// for autoselecting later
									int a_index = m_AssignmentTable.getSelectedRow();
									Assignment Assignment = group.getAssignments().get(a_index);
									
									// iterate through groups, delete from the one that has the Assignment
									// this is only possible through relink()
									if (index == 0) {
										for (GradeGroup g : m_groups.getGroups()) {
											if (g.getAssignments().contains(Assignment)){
												g.removeAssignment(Assignment);
											}
										}
									} else {
										group.removeAssignment(Assignment);
										all_group.removeAssignment(Assignment);
									}
														
									// This is the process of deleting for unlinked (All). 
									// It's an artifact now but I'll delete it later..
									/**
									
									if (index == 0) {
										all_group.removeAssignment(Assignment);
										for (int i = 1; i < m_groups.getGroups().size()-1; i++) {
											GradeGroup g = m_groups.getGroups().get(i);
											for (Assignment a : g.getAssignments()) {
												if (!found) {
													if (Assignment.equals(a)) {
														g.removeAssignment(a);
														found = true;
													}
												}
											}									
										}	
									} else {
										// Deleting from any other group
										group.removeAssignment(Assignment);
										for (Assignment a: all_group.getAssignments()) {
											if (!found) {
												if (Assignment.equals(a)) {
													all_group.removeAssignment(a);
													found = true;
												}
											}
										}
									}
									*/
									
									
									// AutoSelecting Assignments
									// If group is empty, make no selection
									if (group.getAssignmentCount() == 0) {
										m_AssignmentTable.clearSelection();
										m_nameTF.setText("");
										m_scoreTF.setText("");
										m_gradeTF.setText("");
										m_percentTF.setText("");
										m_totalScoreTF.setText("");	
									// If you deleted the last assignment, select new last
									} else if (group.getAssignmentCount() <= a_index) {
										m_AssignmentTable.setRowSelectionInterval(a_index-1, a_index-1);
									// Otherwise, select new Assignment in that index
									} else {
										m_AssignmentTable.setRowSelectionInterval(a_index, a_index);
									}
									m_AssignmentTable.repaint();
									m_groupList.repaint();
								}
							});
							panel.add(m_deleteAssignmentButton);
							{
								m_classSettingsButton = new JButton("Settings");
								panel.add(m_classSettingsButton);
								// FILL THIS IN
							}
						}
						
					}
				}
				{
					JPanel AssignmentToolbar = new JPanel();
					FlowLayout flowLayout = (FlowLayout) AssignmentToolbar.getLayout();
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.anchor = GridBagConstraints.NORTHEAST;
					gbc.insets = new Insets(0, 0, 5, 0);
					gbc.gridx = 1;
					gbc.gridy = 0;
					topPanel.add(AssignmentToolbar, gbc);
					{
						lblClassGrade = new JLabel("Grade:");
						AssignmentToolbar.add(lblClassGrade);
					}
					{
						m_classGradeTF = new JTextField();
						m_classGradeTF.setEditable(false);
						AssignmentToolbar.add(m_classGradeTF);
						m_classGradeTF.setColumns(10);
					}
				}
				{

					m_AssignmentTable = new JTable();
					m_AssignmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			        m_AssignmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			            @Override
			            public void valueChanged(ListSelectionEvent arg0) {
			                if (!arg0.getValueIsAdjusting()) {
					            
					            m_groups.getGroups().get(m_groupList.getSelectedIndex()).update();
					            m_groups.update();
					            // Autosave Feature
					            try {
									save();
								} catch (JAXBException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
					            // update GUI
						        m_groupScoreTF.repaint();
						        m_groupTotalScoreTF.repaint();
						        m_groupPercentTF.repaint();
						        m_groupGradeTF.repaint();				        
			                }
			            }

				    });
					scrollPane = new JScrollPane(m_AssignmentTable);
					GridBagConstraints gbc_scrollPane = new GridBagConstraints();
					gbc_scrollPane.gridwidth = 2;
					gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
					gbc_scrollPane.fill = GridBagConstraints.BOTH;
					gbc_scrollPane.gridx = 0;
					gbc_scrollPane.gridy = 1;
					topPanel.add(scrollPane, gbc_scrollPane);
				}
			}
			{
				JPanel bottomPanel = new JPanel();
				bottomPanel.setBorder(new MatteBorder(10, 10, 10, 10,
						(Color) null));
				rightPanel.setRightComponent(bottomPanel);
				GridBagLayout gbl_bottomPanel = new GridBagLayout();
				gbl_bottomPanel.columnWidths = new int[]{52, 77, 10, 84, 26, 48, 43, 50};
				gbl_bottomPanel.rowHeights = new int[]{26, 26, 26, 0, 0};
				gbl_bottomPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 2.0, 1.0, 0.0, 1.0};
				gbl_bottomPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				bottomPanel.setLayout(gbl_bottomPanel);
				{
					lblGroup = new JLabel("Group:");
					GridBagConstraints gbc_lblGroup = new GridBagConstraints();
					gbc_lblGroup.anchor = GridBagConstraints.WEST;
					gbc_lblGroup.insets = new Insets(0, 0, 5, 5);
					gbc_lblGroup.gridx = 0;
					gbc_lblGroup.gridy = 0;
					bottomPanel.add(lblGroup, gbc_lblGroup);
				}
				{
					m_groupCB = new JComboBox();
					m_groupCB.setEditable(true);
					GridBagConstraints gbc_m_groupCB = new GridBagConstraints();
					gbc_m_groupCB.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_groupCB.gridwidth = 7;
					gbc_m_groupCB.insets = new Insets(0, 0, 5, 5);
					gbc_m_groupCB.gridx = 1;
					gbc_m_groupCB.gridy = 0;
					bottomPanel.add(m_groupCB, gbc_m_groupCB);
				}
				{
					JLabel lblName = new JLabel("Name:");
					GridBagConstraints gbc_lblName = new GridBagConstraints();
					gbc_lblName.anchor = GridBagConstraints.WEST;
					gbc_lblName.insets = new Insets(0, 0, 5, 5);
					gbc_lblName.gridx = 0;
					gbc_lblName.gridy = 1;
					bottomPanel.add(lblName, gbc_lblName);
				}
				{
					m_nameTF = new JTextField();
					GridBagConstraints gbc_m_nameTF = new GridBagConstraints();
					gbc_m_nameTF.gridwidth = 7;
					gbc_m_nameTF.anchor = GridBagConstraints.NORTH;
					gbc_m_nameTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_nameTF.insets = new Insets(0, 0, 5, 0);
					gbc_m_nameTF.gridx = 1;
					gbc_m_nameTF.gridy = 1;
					bottomPanel.add(m_nameTF, gbc_m_nameTF);
					m_nameTF.setColumns(10);
				}
				{
					JLabel lblScore = new JLabel("Score:");
					GridBagConstraints gbc_lblScore = new GridBagConstraints();
					gbc_lblScore.anchor = GridBagConstraints.WEST;
					gbc_lblScore.insets = new Insets(0, 0, 5, 5);
					gbc_lblScore.gridx = 0;
					gbc_lblScore.gridy = 2;
					bottomPanel.add(lblScore, gbc_lblScore);
				}
				{
					m_scoreTF = new JTextField();
					GridBagConstraints gbc_m_scoreTF = new GridBagConstraints();
					gbc_m_scoreTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_scoreTF.insets = new Insets(0, 0, 5, 5);
					gbc_m_scoreTF.gridx = 1;
					gbc_m_scoreTF.gridy = 2;
					bottomPanel.add(m_scoreTF, gbc_m_scoreTF);
					m_scoreTF.setColumns(10);
	
					// EXPERIMENTAL: TRACKS UPDATES~ 
					// Problem: calculations are always one key behind
					// FLAG 1
					/**
					m_scoreTF.getDocument().addDocumentListener(new DocumentListener() {
						public void changedUpdate(DocumentEvent e) {
							updateGroupData();
						}
						public void insertUpdate(DocumentEvent e) {
							updateGroupData();
						}
						public void removeUpdate(DocumentEvent e) {
							updateGroupData();
						}

						public void updateGroupData() {
							m_groups.m_groups.get(m_groupList.getSelectedIndex()).update();
						    m_groupScoreTF.repaint();
						    m_groupTotalScoreTF.repaint();
						    	m_groupPercentTF.repaint();
						    m_groupGradeTF.repaint();	
						}
					});
					*/

					// OLD CODE: Works on enter press		
					m_scoreTF.addActionListener(new java.awt.event.ActionListener() {
					    public void actionPerformed(java.awt.event.ActionEvent e) {
					    	// Action is defined as enter press
				            m_groups.getGroups().get(m_groupList.getSelectedIndex()).update();
				            m_groups.update();
				            // Autosave feature
				            try {
								save();
							} catch (JAXBException f) {
								f.printStackTrace();
							} catch (IOException f) {
								f.printStackTrace();
							}
					        m_groupScoreTF.repaint();
					        m_groupTotalScoreTF.repaint();
					        m_groupPercentTF.repaint();
					        m_groupGradeTF.repaint();					   	
					    }
					});
					
				}
				{
					lblDivider = new JLabel("/");
					GridBagConstraints gbc_lblDivider = new GridBagConstraints();
					gbc_lblDivider.anchor = GridBagConstraints.EAST;
					gbc_lblDivider.insets = new Insets(0, 0, 5, 5);
					gbc_lblDivider.gridx = 2;
					gbc_lblDivider.gridy = 2;
					bottomPanel.add(lblDivider, gbc_lblDivider);
				}
				{
					m_totalScoreTF = new JTextField();
					GridBagConstraints gbc_m_totalScoreTF = new GridBagConstraints();
					gbc_m_totalScoreTF.insets = new Insets(0, 0, 5, 5);
					gbc_m_totalScoreTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_totalScoreTF.gridx = 3;
					gbc_m_totalScoreTF.gridy = 2;
					bottomPanel.add(m_totalScoreTF, gbc_m_totalScoreTF);
					m_totalScoreTF.setColumns(10);
					// Updating upon changing the textfields
					// Needs testing to find optimal solution
					m_totalScoreTF.getDocument().addDocumentListener(new DocumentListener() {
						public void changedUpdate(DocumentEvent e) {
							updateGroupData();
						}
						public void insertUpdate(DocumentEvent e) {
							updateGroupData();
						}
						public void removeUpdate(DocumentEvent e) {
							updateGroupData();
						}
						
						// Updates basically set group&class %s and grades.
						public void updateGroupData() {
							m_groups.getGroups().get(m_groupList.getSelectedIndex()).update();
							m_groups.update();
				            // autoSave()
				            try {
								save();
							} catch (JAXBException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						    m_groupScoreTF.repaint();
						    m_groupTotalScoreTF.repaint();
						    m_groupPercentTF.repaint();
						    m_groupGradeTF.repaint();	
						}
					});
				}
				{
					lblPercent = new JLabel("%:");
					GridBagConstraints gbc_lblPercent = new GridBagConstraints();
					gbc_lblPercent.anchor = GridBagConstraints.EAST;
					gbc_lblPercent.insets = new Insets(0, 0, 5, 5);
					gbc_lblPercent.gridx = 4;
					gbc_lblPercent.gridy = 2;
					bottomPanel.add(lblPercent, gbc_lblPercent);
				}
				{
					m_percentTF = new JTextField();
					m_percentTF.setEditable(false);
					GridBagConstraints gbc_m_percentTF = new GridBagConstraints();
					gbc_m_percentTF.insets = new Insets(0, 0, 5, 5);
					gbc_m_percentTF.anchor = GridBagConstraints.NORTH;
					gbc_m_percentTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_percentTF.gridx = 5;
					gbc_m_percentTF.gridy = 2;
					bottomPanel.add(m_percentTF, gbc_m_percentTF);
					m_percentTF.setColumns(10);
				}
				{
					JLabel lblGrade = new JLabel("Grade:");
					GridBagConstraints gbc_lblGrade = new GridBagConstraints();
					gbc_lblGrade.anchor = GridBagConstraints.EAST;
					gbc_lblGrade.insets = new Insets(0, 0, 5, 5);
					gbc_lblGrade.gridx = 6;
					gbc_lblGrade.gridy = 2;
					bottomPanel.add(lblGrade, gbc_lblGrade);
				}
				{
					m_gradeTF = new JTextField();
					m_gradeTF.setEditable(false);
					GridBagConstraints gbc_m_gradeTF = new GridBagConstraints();
					gbc_m_gradeTF.insets = new Insets(0, 0, 5, 0);
					gbc_m_gradeTF.anchor = GridBagConstraints.NORTH;
					gbc_m_gradeTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_gradeTF.gridx = 7;
					gbc_m_gradeTF.gridy = 2;
					bottomPanel.add(m_gradeTF, gbc_m_gradeTF);
					m_gradeTF.setColumns(10);
				}
				{
					lblClassScore = new JLabel("Totals:");
					GridBagConstraints gbc_lblClassScore = new GridBagConstraints();
					gbc_lblClassScore.anchor = GridBagConstraints.WEST;
					gbc_lblClassScore.insets = new Insets(0, 0, 0, 5);
					gbc_lblClassScore.gridx = 0;
					gbc_lblClassScore.gridy = 3;
					bottomPanel.add(lblClassScore, gbc_lblClassScore);
				}
				{
					m_groupScoreTF = new JTextField();
					m_groupScoreTF.setEditable(false);
					GridBagConstraints gbc_m_groupScoreTF = new GridBagConstraints();
					gbc_m_groupScoreTF.anchor = GridBagConstraints.NORTH;
					gbc_m_groupScoreTF.insets = new Insets(0, 0, 0, 5);
					gbc_m_groupScoreTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_groupScoreTF.gridx = 1;
					gbc_m_groupScoreTF.gridy = 3;
					bottomPanel.add(m_groupScoreTF, gbc_m_groupScoreTF);
					m_groupScoreTF.setColumns(10);
				}
				{
					lblGroupDivider = new JLabel("/");
					GridBagConstraints gbc_lblGroupDivider = new GridBagConstraints();
					gbc_lblGroupDivider.anchor = GridBagConstraints.EAST;
					gbc_lblGroupDivider.insets = new Insets(0, 0, 0, 5);
					gbc_lblGroupDivider.gridx = 2;
					gbc_lblGroupDivider.gridy = 3;
					bottomPanel.add(lblGroupDivider, gbc_lblGroupDivider);
				}
				{
					m_groupTotalScoreTF = new JTextField();
					m_groupTotalScoreTF.setEditable(false);
					m_groupTotalScoreTF.setColumns(10);
					GridBagConstraints gbc_m_groupTotalScoreTF = new GridBagConstraints();
					gbc_m_groupTotalScoreTF.insets = new Insets(0, 0, 0, 5);
					gbc_m_groupTotalScoreTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_groupTotalScoreTF.gridx = 3;
					gbc_m_groupTotalScoreTF.gridy = 3;
					bottomPanel.add(m_groupTotalScoreTF, gbc_m_groupTotalScoreTF);
				}
				{
					lblGroupPercent = new JLabel("%:");
					GridBagConstraints gbc_lblGroupPercent = new GridBagConstraints();
					gbc_lblGroupPercent.anchor = GridBagConstraints.EAST;
					gbc_lblGroupPercent.insets = new Insets(0, 0, 0, 5);
					gbc_lblGroupPercent.gridx = 4;
					gbc_lblGroupPercent.gridy = 3;
					bottomPanel.add(lblGroupPercent, gbc_lblGroupPercent);
				}
				{
					m_groupPercentTF = new JTextField();
					m_groupPercentTF.setEditable(false);
					m_groupPercentTF.setColumns(10);
					GridBagConstraints gbc_m_groupPercentTF = new GridBagConstraints();
					gbc_m_groupPercentTF.insets = new Insets(0, 0, 0, 5);
					gbc_m_groupPercentTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_groupPercentTF.gridx = 5;
					gbc_m_groupPercentTF.gridy = 3;
					bottomPanel.add(m_groupPercentTF, gbc_m_groupPercentTF);
				}
				{
					lblGroupGrade = new JLabel("Grade:");
					GridBagConstraints gbc_lblGroupGrade = new GridBagConstraints();
					gbc_lblGroupGrade.anchor = GridBagConstraints.EAST;
					gbc_lblGroupGrade.insets = new Insets(0, 0, 0, 5);
					gbc_lblGroupGrade.gridx = 6;
					gbc_lblGroupGrade.gridy = 3;
					bottomPanel.add(lblGroupGrade, gbc_lblGroupGrade);
				}
				{
					m_groupGradeTF = new JTextField();
					m_groupGradeTF.setEditable(false);
					GridBagConstraints gbc_m_groupGradeTF = new GridBagConstraints();
					gbc_m_groupGradeTF.fill = GridBagConstraints.HORIZONTAL;
					gbc_m_groupGradeTF.gridx = 7;
					gbc_m_groupGradeTF.gridy = 3;
					bottomPanel.add(m_groupGradeTF, gbc_m_groupGradeTF);
					m_groupGradeTF.setColumns(10);
				}
			}
		}
		initDataBindings();
	}
	// Bindings done by netBeans!
	protected void initDataBindings() {
		Property gradeGroupsBeanProperty = BeanProperty.create("groups");
		JListBinding jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, m_groups, gradeGroupsBeanProperty, m_groupList);
		//
		Property gradeGroupEvalutionProperty = ELProperty.create("${name} (${assignmentCount})");
		jListBinding.setDetailBinding(gradeGroupEvalutionProperty);
		//
		jListBinding.bind();
		//
		Property jListBeanProperty = BeanProperty.create("selectedElement.assignments");
		JTableBinding jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, m_groupList, jListBeanProperty, m_AssignmentTable);
		//
		Property assignmentBeanProperty = BeanProperty.create("name");
		jTableBinding.addColumnBinding(assignmentBeanProperty).setColumnName("Assignment");
		//
		Property assignmentBeanProperty_1 = BeanProperty.create("myScore");
		jTableBinding.addColumnBinding(assignmentBeanProperty_1).setColumnName("My Score");
		//
		Property assignmentBeanProperty_2 = BeanProperty.create("fullScore");
		jTableBinding.addColumnBinding(assignmentBeanProperty_2).setColumnName("Total Score");
		//
		Property assignmentBeanProperty_3 = BeanProperty.create("percent");
		jTableBinding.addColumnBinding(assignmentBeanProperty_3).setColumnName("%").setEditable(false);
		//
		Property assignmentBeanProperty_4 = BeanProperty.create("grade");
		jTableBinding.addColumnBinding(assignmentBeanProperty_4).setColumnName("Grade").setEditable(false);
		//
		jTableBinding.bind();
		//
		Property jTableBeanProperty = BeanProperty.create("selectedElement.name");
		Property jTextFieldBeanProperty = BeanProperty.create("text");
		AutoBinding autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, m_AssignmentTable, jTableBeanProperty, m_nameTF, jTextFieldBeanProperty, "nameBinding");
		autoBinding.bind();
		//
		Property jTableBeanProperty_1 = BeanProperty.create("selectedElement.myScore");
		Property jTextFieldBeanProperty_1 = BeanProperty.create("text");
		AutoBinding autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, m_AssignmentTable, jTableBeanProperty_1, m_scoreTF, jTextFieldBeanProperty_1, "myScoreBinding");
		autoBinding_1.bind();
		//
		Property jTableBeanProperty_2 = BeanProperty.create("selectedElement.fullScore");
		Property jTextFieldBeanProperty_2 = BeanProperty.create("text");
		AutoBinding autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, m_AssignmentTable, jTableBeanProperty_2, m_totalScoreTF, jTextFieldBeanProperty_2, "totalScoreBinding");
		autoBinding_2.bind();
		//
		Property jTableBeanProperty_3 = BeanProperty.create("selectedElement.percent");
		Property jTextFieldBeanProperty_3 = BeanProperty.create("text");
		AutoBinding autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, m_AssignmentTable, jTableBeanProperty_3, m_percentTF, jTextFieldBeanProperty_3, "percentBinding");
		autoBinding_3.bind();
		//
		Property jTableBeanProperty_4 = BeanProperty.create("selectedElement.grade");
		Property jTextFieldBeanProperty_4 = BeanProperty.create("text");
		AutoBinding autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, m_AssignmentTable, jTableBeanProperty_4, m_gradeTF, jTextFieldBeanProperty_4, "gradeBinding");
		autoBinding_4.bind();
		//
		Property jListBeanProperty_1 = BeanProperty.create("selectedElement.groupScore");
		Property jTextFieldBeanProperty_8 = BeanProperty.create("text");
		AutoBinding autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, m_groupList, jListBeanProperty_1, m_groupScoreTF, jTextFieldBeanProperty_8);
		autoBinding_8.bind();
		//
		Property jListBeanProperty_2 = BeanProperty.create("selectedElement.groupTotalScore");
		Property jTextFieldBeanProperty_9 = BeanProperty.create("text");
		AutoBinding autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, m_groupList, jListBeanProperty_2, m_groupTotalScoreTF, jTextFieldBeanProperty_9);
		autoBinding_9.bind();
		//
		Property jListBeanProperty_3 = BeanProperty.create("selectedElement.groupPercent");
		Property jTextFieldBeanProperty_10 = BeanProperty.create("text");
		AutoBinding autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, m_groupList, jListBeanProperty_3, m_groupPercentTF, jTextFieldBeanProperty_10);
		autoBinding_10.bind();
		//
		Property jListBeanProperty_4 = BeanProperty.create("selectedElement.groupGrade");
		Property jTextFieldBeanProperty_6 = BeanProperty.create("text");
		AutoBinding autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, m_groupList, jListBeanProperty_4, m_groupGradeTF, jTextFieldBeanProperty_6);
		autoBinding_6.bind();
		//
		Property gradeGroupsBeanProperty_1 = BeanProperty.create("classGrade");
		Property jTextFieldBeanProperty_5 = BeanProperty.create("text");
		AutoBinding autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, m_groups, gradeGroupsBeanProperty_1, m_classGradeTF, jTextFieldBeanProperty_5);
		autoBinding_7.bind();
		//
		Property jComboBoxBeanProperty = BeanProperty.create("selectedItem");
		AutoBinding autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, m_groupList, jTableBeanProperty, m_groupCB, jComboBoxBeanProperty);
		autoBinding_5.bind();
	}
}