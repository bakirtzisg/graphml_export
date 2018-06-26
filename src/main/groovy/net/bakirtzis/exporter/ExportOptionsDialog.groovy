package net.bakirtzis.exporter

import com.jidesoft.swing.CheckBoxList

import javax.swing.*
import javax.swing.border.BevelBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.List


public class ExportOptionsDialog implements ActionListener
{
	private final JFrame mainFrame;
	private final CheckBoxList selectionList;
	private final JTextField outputFileField;
	
	private final List<String> listItems;
	
	private Runnable onComplete;
	
	
	public ExportOptionsDialog()
	{
		listItems = new ArrayList<>();
		mainFrame = new JFrame("Export Options");
		
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		//----------
		// Output File
		//----------
		JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
		outputPanel.add(new JLabel("Output File"), BorderLayout.NORTH);
		outputFileField = new JTextField();
		outputFileField.setText(new File(System.getProperty("user.home"), "out.graphml").getAbsolutePath());
		outputPanel.add(outputFileField, BorderLayout.CENTER);
		
		JButton btn_browse = new JButton("...");
		btn_browse.addActionListener(this);
		outputPanel.add(btn_browse, BorderLayout.EAST);
		
		//----------
		// Diagram Selection
		//----------
		JPanel diagramPanel = new JPanel(new BorderLayout(5, 5));
		selectionList = new CheckBoxList();
		
		
		diagramPanel.add(new JLabel("Select the diagrams to export:"), BorderLayout.NORTH);
		
		JScrollPane sp = new JScrollPane(selectionList);
		sp.setPreferredSize(new Dimension(400, 300));
		
		diagramPanel.add(sp, BorderLayout.CENTER);
		
		//----------
		// Buttons
		//----------
		JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonsPane = new JPanel(new GridLayout(1, 2, 5, 5));//
		JButton btn_ok = new JButton("OK");
		JButton btn_cancel = new JButton("Cancel");
		
		btn_ok.addActionListener(this);
		btn_cancel.addActionListener(this);
		
		buttonsPane.add(btn_ok);
		buttonsPane.add(btn_cancel);
		flowPanel.add(buttonsPane);
		
		//----------
		// Connect all components
		//----------
		contentPane.add(outputPanel);
		contentPane.add(diagramPanel);
		contentPane.add(flowPanel);
		
		
		mainFrame.add(contentPane);
		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double px = dim.width / 2 - mainFrame.getSize().width / 2;
		double py = dim.height / 2 - mainFrame.getSize().height / 2;
		mainFrame.setLocation((int) px, (int) py);
	}
	
	public void setOnComplete(Runnable onComplete)
	{
		this.onComplete = onComplete;
	}
	
	public void addItem(String item)
	{
		listItems.add(item);
	}
	
	public void updateList()
	{
		selectionList.setListData(listItems.toArray());
		selectionList.setClickInCheckBoxOnly(false);
		selectionList.setCheckBoxListSelectedIndex(0);
	}
	
	public void displayGUI()
	{
		
		mainFrame.setVisible(true);
		
	}
	
	public int[] getSelectedIndices()
	{
		return selectionList.getCheckBoxListSelectedIndices();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch (e.getActionCommand())
		{
			case "OK": // OK Button
				
				int[] selected = selectionList.getCheckBoxListSelectedIndices();
				if (selected.length > 0)
				{
					mainFrame.dispose(); // dispose the dialog
					if (onComplete != null)
					{
						onComplete.run();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "You must select at least one diagram!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				
				break;
			
			case "Cancel":  // Cancel Button
				mainFrame.dispose(); // dispose the dialog
				break;
			
			case "...": // Browse button for the file selection
				
				browseOutputFile();
				
				break;
			
			default:
				System.out.println("Unknown action: " + e.getActionCommand());
		}
	}
	
	public File getOutputFile()
	{
		return new File(outputFileField.getText());
	}
	
	private void browseOutputFile()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setFileFilter(new GraphMLFileFilter());
		fileChooser.setSelectedFile(new File("out.graphml"));
		
		File file = null;
		
		while (file == null)
		{
			int dialogResult = fileChooser.showSaveDialog(null);
			if (dialogResult == JFileChooser.APPROVE_OPTION)
			{
				file = fileChooser.getSelectedFile();
				if (file.exists())
				{
					int res = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to overwrite " + file.getName(),
							"Confirm File Overwrite",
							JOptionPane.YES_NO_OPTION
					);
					if (res == JOptionPane.NO_OPTION)
					{
						file = null;
					}
				}
			}
			else
			{
				break;
			}
		}
		
		if (file != null)
		{
			outputFileField.setText(file.getAbsolutePath());
		}
	}
	
	
}
