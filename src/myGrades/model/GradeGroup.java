package myGrades.model;

import java.text.DecimalFormat;
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
@XmlRootElement(name = "group")
public class GradeGroup extends AbstractModelObject {
	
    // XmLElementWrapper generates a wrapper element around XML representation
    // temp disable
	// @XmlElementWrapper(name = "assignments")
    // XmlElement sets the name of the entities
	private List<Assignment> m_Assignments = new ArrayList<Assignment>();
	private String m_name;
	private double m_groupScore;
	private double m_groupTotalScore;
	private String m_groupPercent;
	private String m_groupGrade;
	
	// CONSTRUCTORS

	public GradeGroup() {
	}

	public GradeGroup(String name) {
		m_name = name;
	}

	/* ------------------- */
	/* SETTERS & GETTERS!! */
	/* --------------------*/
	
	public void update() {
		// Let's fix things up real nice
		setGroupScore();
		setGroupTotalScore();
		setGroupPercent();
		setgroupGrade();
	}
	// ASSIGNMENT LIST
	public List<Assignment> getAssignments(){
		return m_Assignments;
	}
	
	public void setAssignments(List<Assignment> assigns) {
		List<Assignment> oldValue = m_Assignments;
		m_Assignments = assigns;
		firePropertyChange("assignments", oldValue, m_Assignments);
		firePropertyChange("assignmentCount", oldValue.size(), m_Assignments.size());
		// Calculate new groupGrade
		m_groupGrade = calculateGrade();
		m_groupScore = calculateGroupScore();
		m_groupTotalScore = calculateGroupTotalScore();
		m_groupPercent = calculateGroupPercent();
	}
	
	// NAME
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		String oldValue = m_name;
		m_name = name;
		firePropertyChange("name", oldValue, m_name);
	}
	
	// groupGrade
	public String getgroupGrade() {
		return m_groupGrade;
	}
	
	public void setgroupGrade() {
		String oldValue = m_groupGrade;
		m_groupGrade = calculateGrade();
		firePropertyChange("groupGrade", oldValue, m_groupGrade);
	}
	
	// GROUPSCORE
	public double getGroupScore() {
		return m_groupScore;
	}
	
	public void setGroupScore() {
		double oldValue = m_groupScore;
		m_groupScore = calculateGroupScore();
		firePropertyChange("groupScore", oldValue, m_groupScore);
	}
	
	// TOTAL GROUPSCORE
	public double getGroupTotalScore() {
		return m_groupTotalScore;
	}
	
	public void setGroupTotalScore() {
		double oldValue = m_groupTotalScore;
		m_groupTotalScore = calculateGroupTotalScore();
		firePropertyChange("groupTotalScore", oldValue, m_groupTotalScore);
	}
	
	// GROUP PERCENT
	public String getGroupPercent() {
		return m_groupPercent;
	}
	
	public void setGroupPercent() {
		String oldValue = m_groupPercent;
		m_groupPercent = calculateGroupPercent();
		firePropertyChange("groupPercent", oldValue, m_groupPercent);
	}

	/* ------------------- */
	/* EDITORS AND ADDERS! */
	/* --------------------*/
	
	public void addAssignment(Assignment Assignment) {
		List<Assignment> oldValue = m_Assignments;
		m_Assignments = new ArrayList<Assignment>(m_Assignments);
		m_Assignments.add(Assignment);
		firePropertyChange("assignments", oldValue, m_Assignments);
		firePropertyChange("assignmentCount", oldValue.size(), m_Assignments.size());
		// Calculate new groupGrade
		m_groupGrade = calculateGrade();
		m_groupScore = calculateGroupScore();
		m_groupTotalScore = calculateGroupTotalScore();
		m_groupPercent = calculateGroupPercent();
	}

	public void removeAssignment(Assignment Assignment) {
		List<Assignment> oldValue = m_Assignments;
		m_Assignments = new ArrayList<Assignment>(m_Assignments);
		m_Assignments.remove(Assignment);
		firePropertyChange("assignments", oldValue, m_Assignments);
		firePropertyChange("assignmentCount", oldValue.size(), m_Assignments.size());
		// Calculate new groupGrade
		m_groupGrade = calculateGrade();
		m_groupScore = calculateGroupScore();
		m_groupTotalScore = calculateGroupTotalScore();
		m_groupPercent = calculateGroupPercent();
	}
	
	public int getAssignmentCount() {
		return m_Assignments.size();
	}
	
	/* ------------- */
	/* CALCULATORS!! */
	/* --------------*/

	public String calculateGrade() {
		int myScores = 0;
		int fullScores = 0;
		for (Assignment a : m_Assignments ) {
			myScores += a.getmyScore();
			fullScores += a.getfullScore();
		}
		return determineGrade((double) myScores * 100 / (double) fullScores);
	}
	
	public double calculateGroupScore() {
		double myScores = 0;
		for (Assignment a : m_Assignments ) {
			myScores += a.getmyScore();
		}
		return Math.floor(myScores);
	}
	
	public double calculateGroupTotalScore() {
		double totalScore = 0;
		for (Assignment a : m_Assignments ) {
			totalScore += a.getfullScore();
		}
		return Math.floor(totalScore);
	}
	
	public String calculateGroupPercent() {
		double raw_percent = ((double) this.m_groupScore / (double) this.m_groupTotalScore) * 100;
		String corrected_percent = new DecimalFormat("#.0").format(raw_percent);
		return corrected_percent;
	}
	
	
	public String determineGrade(double score) {
		if (score > 97.5) {
			return "A+";
		} else if (score > 92.5) {
			return "A";
		} else if (score > 89.5) {
			return "A-";
		} else if (score > 87.5) {
			return "B+";
		} else if (score > 82.5) {
			return "B";
		} else if (score > 79.5) {
			return "B-";
		} else if (score > 69.5) {
			return "C";
		} else {
			return "F";
		}
	}
	
	// METHODS FOR RELINK
	public void directAdd(Assignment a) {
		m_Assignments.add(a);
	}
	
	public void clear() {
		m_Assignments.removeAll(getAssignments());
	}
	
}