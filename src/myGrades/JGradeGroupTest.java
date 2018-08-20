package myGrades;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import myGrades.model.Assignment;
import myGrades.model.GradeGroup;

class JGradeGroupTest {

	@Test
	void groupsCreatedTest() {
		/**
		JGradeBook frame = new JGradeBook();
		GradeGroup test = frame.getGroups().m_groups.get(0);
		int base = test.getAssignmentCount() + 1;
		test.addAssignment(new Assignment());

		int ans = test.getAssignmentCount();
		assertEquals(base, ans);
		*/
	}
	
	void assignmentEqualsTest() {
		Assignment a1 = new Assignment("test", 5, 5, "100%", "A+");
		Assignment a2 = new Assignment("test", 5, 5, "100%", "A+");
		assertTrue(a1.equals(a2));
	}
	
	
	void assignmentEqualsFailTest() {
		Assignment a1 = new Assignment("test", 5, 5, "100%", "A+");
		Assignment a2 = new Assignment("test", 4, 5, "100%", "A+");
		assertFalse(a1.equals(a2));
	}

}
