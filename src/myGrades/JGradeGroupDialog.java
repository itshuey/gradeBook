package myGrades;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.SwingBindings;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import myGrades.model.GradeGroup;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.Property;
import javax.swing.JTextField;


// To-Do:
// Remove tampering with (all) group
// Actionevent for enter? So annoying to manually click ok

/**
 * @author huey_
 * 
 */
public class JGradeGroupDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final myGrades.model.GradeGroup m_GradeGroup;
	private final List<String> m_names;
	private JPanel m_contentPane;
	private JButton m_buttonOk;
	private JTextField groupNameTextField;

	public JGradeGroupDialog(List<String> names, myGrades.model.GradeGroup GradeGroup) {
		m_names = names;
		m_GradeGroup = GradeGroup;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setTitle("Grade Group");

		getRootPane().setDefaultButton(m_buttonOk);
	      
		setBounds(100, 100, 432, 126);
		m_contentPane = new JPanel();
		m_contentPane.setBorder(new MatteBorder(5, 5, 5, 5, (Color) null));
		setContentPane(m_contentPane);
		//
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
		m_contentPane.setLayout(gridBagLayout);
		//
		{
			JLabel lblName = new JLabel("Name:");
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.gridx = 0;
			gbc.gridy = 0;
			m_contentPane.add(lblName, gbc);
		}
		{
			m_buttonOk = new JButton("OK");
			m_buttonOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			{
				groupNameTextField = new JTextField();
				GridBagConstraints gbc_groupNameTextField = new GridBagConstraints();
				gbc_groupNameTextField.anchor = GridBagConstraints.SOUTH;
				gbc_groupNameTextField.insets = new Insets(0, 0, 5, 0);
				gbc_groupNameTextField.fill = GridBagConstraints.HORIZONTAL;
				gbc_groupNameTextField.gridx = 1;
				gbc_groupNameTextField.gridy = 0;
				m_contentPane.add(groupNameTextField, gbc_groupNameTextField);
				groupNameTextField.setColumns(10);
			}
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 1;
			gbc.gridy = 2;
			m_contentPane.add(m_buttonOk, gbc);
		}
		initDataBindings();
	}
	protected void initDataBindings() {
		Property gradeGroupBeanProperty_1 = BeanProperty.create("name");
		Property jTextFieldBeanProperty = BeanProperty.create("text");
		AutoBinding autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, m_GradeGroup, gradeGroupBeanProperty_1, groupNameTextField, jTextFieldBeanProperty);
		autoBinding_1.bind();
	}
}