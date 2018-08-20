package myGrades.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * @author huey_
 * 
 */

@XmlRootElement(namespace = "myGrades.model")
public class GradeGroups extends AbstractModelObject {

    // XmLElementWrapper generates a wrapper element around XML representation
	// temp disable
    // @XmlElementWrapper(name = "class")
    // XmlElement sets the name of the entities
	private List<GradeGroup> m_groups = new ArrayList<GradeGroup>();
	private String m_classGrade = "";
	
	// Getters and Setters for XML
	public List<GradeGroup> getGroups() {
		return m_groups;
	}
	
	public void setGroups(List<GradeGroup> groups) {
		List<GradeGroup> oldValue = m_groups;
		m_groups = groups;
		firePropertyChange("groups", oldValue, m_groups);
		update();
	}
	
	// groupGrade
	public String getClassGrade() {
		return m_classGrade;
	}
	
	public void setClassGrade() {
		String oldValue = m_classGrade;
		m_classGrade = m_groups.get(0).getgroupGrade();
		firePropertyChange("classGrade", oldValue, m_classGrade);
	}
	
	public void addGroup(GradeGroup group) {
		List<GradeGroup> oldValue = m_groups;
		m_groups = new ArrayList<GradeGroup>(m_groups);
		m_groups.add(group);
		firePropertyChange("groups", oldValue, m_groups);
		update();
	}

	public void removeGroup(GradeGroup group) {
		List<GradeGroup> oldValue = m_groups;
		m_groups = new ArrayList<GradeGroup>(m_groups);
		m_groups.remove(group);
		firePropertyChange("groups", oldValue, m_groups);
		update();
	}
	
	
	public void update() {
		setClassGrade();
	}
	
	/**
	 * Unfortunately, unmarshalling the .xml file destroys the true linkage between the groups and (all)
	 * This is an attempt to fix the problem by relinking the nodes in all
	 * 
	 * Potential bugs:
	 * Because I am iterating through, if two assignments have the same attributes (pass off as .equal), their position can
	 * possible be switched. This is only noticeable if the assignments are in different groups, but this seems to be unlikely.
	 * 
	 * The only possible occasion for this being a problem is if people name solely based on number within groups. Maybe add a tooltip?
	 * 
	 * Alternate solution: Add position field to assignment on window close based on current order in all
	 * 
	 */
	public void relink() {
		GradeGroup original = m_groups.get(0);
		// MAKING A DUPLICATE COPY
		GradeGroup copy = new GradeGroup("All");
		for (Assignment a : original.getAssignments()) {
			copy.directAdd(new Assignment(a.getName(),a.getfullScore(),a.getmyScore(),a.getPercent(),a.getGrade()));
		}
		original.clear();
		
		// can optimize this for space later
		// i don't want to add a searched bool onto the assignments themselves
		int allsize = copy.getAssignmentCount();
		int groupcount = m_groups.size();
		boolean found[][] = new boolean[groupcount][allsize];
		
		// iterate through elements in all
		for (int i = 0; i < allsize; i++) {
			boolean currentNode = false;
			Assignment a = copy.getAssignments().get(i);
			// search through all elements
			for (int groupIndex = 1; groupIndex < groupcount; groupIndex++) {
				GradeGroup current = m_groups.get(groupIndex);
				for (int inner = 0; inner < current.getAssignmentCount(); inner++) {
					Assignment search = current.getAssignments().get(inner);
					boolean id = found[groupIndex][inner];
					if (a.equals(search) && !id && !currentNode) {
						original.addAssignment(search);
						currentNode = !currentNode;
						id = !id;
					}
						
				}
						
			}	
					
		}		
		original.update();
	}

}