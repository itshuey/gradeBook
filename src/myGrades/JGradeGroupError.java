package myGrades;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// Add the line like in the jframe to make the OK button prettier if possible

public class JGradeGroupError extends JDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			JGradeGroupError dialog = new JGradeGroupError();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public JGradeGroupError() {
		setBounds(100, 100, 326, 112);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{314, 0};
		gbl_contentPanel.rowHeights = new int[]{71, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblCantEdit = new JLabel("The \"All\" folder is not editable!");
			lblCantEdit.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
			GridBagConstraints gbc_lblCantEdit = new GridBagConstraints();
			gbc_lblCantEdit.fill = GridBagConstraints.VERTICAL;
			gbc_lblCantEdit.gridx = 0;
			gbc_lblCantEdit.gridy = 0;
			contentPanel.add(lblCantEdit, gbc_lblCantEdit);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(":~)");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
}
