package myGrades.model;

import java.text.DecimalFormat;
import myGrades.model.AbstractModelObject;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author huey_
 * 
 */

@XmlRootElement(name = "assignment")
@XmlType(propOrder = { "name", "fullScore", "myScore", "percent", "grade"})
public class Assignment extends AbstractModelObject {
	// instance variables
	private String asn_name;
	private int asn_myScore;
	private int asn_fullScore;
	private String asn_percent;
	private String asn_grade;
	
	public Assignment() {
		asn_name = "Edit Me!";
	}
	
	public Assignment(String name, int myScore, int fullScore) {
		asn_name = name;
		asn_myScore = myScore;
		asn_fullScore = fullScore;
		asn_percent = calculatePercent();
		asn_grade = this.calculateGrade();
	}
	
	public Assignment(String name, int fullScore, int myScore, String percent, String grade) {
		asn_name = name;
		asn_fullScore = fullScore;
		asn_myScore = myScore;
		asn_percent = percent;
		asn_grade = grade;
	}
	
	/* ------------------- */
	/* SETTERS & GETTERS!! */
	/* --------------------*/
	
	// NAME	
	public String getName() {
		return asn_name;
	}

	public void setName(String name) {
		String oldValue = asn_name;
		asn_name = name;
		firePropertyChange("name", oldValue, asn_name);
	}

	// SCORE
    @XmlElement(name = "score")
	public int getmyScore() {
		return asn_myScore;
	}

	public void setmyScore(int myScore) {
		int oldValue = asn_myScore;
		asn_myScore = myScore;
		firePropertyChange("myScore", oldValue, asn_myScore);
		setPercent(calculatePercent());
		setGrade(calculateGrade());
	}

	// FULLSCORE
	public int getfullScore() {
		return asn_fullScore;
	}

	public void setfullScore(int fullScore) {
		int oldValue = asn_fullScore;
		asn_fullScore = fullScore;
		firePropertyChange("fullScore", oldValue, asn_fullScore);
		setPercent(calculatePercent());
		setGrade(calculateGrade());
	}

	// PERCENT
	public String getPercent() {
		return asn_percent;
	}

	public void setPercent(String percent) {
		String oldValue = asn_percent;
		asn_percent = percent;
		firePropertyChange("percent", oldValue, asn_percent);
	}

	// GRADE
	public String getGrade() {
		return asn_grade;
	}

	public void setGrade(String grade) {
		String oldValue = asn_grade;
		asn_grade = grade;
		firePropertyChange("grade", oldValue, asn_grade);
	}

	
	/* ------------- */
	/* CALCULATORS!! */
	/* --------------*/

	public String calculatePercent() {
		double raw_percent = ((double) this.asn_myScore / (double) this.asn_fullScore) * 100;
		String corrected_percent = new DecimalFormat("#.0").format(raw_percent);
		return corrected_percent;
	}
	
	public String calculateGrade() {	
		if (asn_percent == "∞") {
			return "Error: Invalid Number";
		}
		
		try {
			double score = Double.parseDouble(asn_percent);
		} catch (NumberFormatException e) {
			System.out.println("N/A");
		}
		
		double score = Double.parseDouble(asn_percent);
		
		// READ PROPERTIES FILE
		Properties prop = new Properties();
		InputStream input = null;
		String grade = "";

		try {
			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			if (score > Double.parseDouble(prop.getProperty("A+_range"))) {
				grade = "A+";
			} else if (score > Double.parseDouble(prop.getProperty("A_range"))) {
				grade = "A";
			} else if (score > Double.parseDouble(prop.getProperty("A-_range"))) {
				grade = "A-";
			} else if (score > Double.parseDouble(prop.getProperty("B+_range"))) {
				grade = "B+";
			} else if (score > Double.parseDouble(prop.getProperty("B_range"))) {
				grade = "B";
			} else if (score > Double.parseDouble(prop.getProperty("B-_range"))) {
				grade = "B-";
			} else if (score > Double.parseDouble(prop.getProperty("C_range"))) {
				grade = "C";
			} else if (score >= 0) {
				grade = "F";
			} else {
				grade = "N/A";
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
		return grade;
	}
	
	
	public boolean equals(Assignment a) {
		return asn_name.equals(a.asn_name) && asn_grade.equals(asn_grade) && asn_percent.equals(a.asn_percent)
				&& asn_myScore == a.asn_myScore && asn_fullScore == a.asn_fullScore;
	}

}