package net.bakirtzis.exporter

import com.nomagic.magicdraw.actions.MDAction
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element

import javax.swing.*
import java.awt.event.ActionEvent


public class CallFromMenu extends MDAction
{
	private def log = Application.instance.GUILog
	
	private Parser reqParser;
	private Parser idbParser;
	
	CallFromMenu(String id, String name)
	{
		super(id, name, null, null);
	}
	

	def parse(DiagramPresentationElement e)
	{
		// determine if it's a requirements diagram and parse it as such
		for (Element ele : e.getUsedModelElements(true))
		{
			if (StereotypesHelper.hasStereotype(ele, "Requirement"))
			{
				if (reqParser == null)
				{
					reqParser = new RequirementsParser();
				}
				reqParser.parse(e);
				return;
			}
		}
		
		// if not requirements diagram, parse as an IDB
		if (idbParser == null)
		{
			idbParser = new IBDParser();
		}
		idbParser.parse(e);
	}
	
	// export the xml to file
	def export(File file)
	{
		if (reqParser != null)
		{
			reqParser.export(file);
		}
		else if (idbParser != null)
		{
			idbParser.export(file);
		}
	}
	
	void actionPerformed(ActionEvent e)
	{
		reqParser = null;
		idbParser = null;
		try
		{
			// Create the list of diagrams to be used
			Project project = Application.getInstance().getProject();
			DiagramPresentationElement activeDiagram = project.activeDiagram;
			
			ExportOptionsDialog optionsDialog = new ExportOptionsDialog();
			List<DiagramPresentationElement> diagramList = new ArrayList<>();
			
			// add the active diagram first
			diagramList.add(activeDiagram);
			optionsDialog.addItem(activeDiagram.name + " (active)")
			
			// add the other diagrams
			project.diagrams.each {
				if (!diagramList.contains(it))
				{
					diagramList.add(it)
					optionsDialog.addItem(it.name)
				}
			}
			
			
			
			optionsDialog.updateList();
			
			// This will run once the user presses the "OK" button
			optionsDialog.setOnComplete(new Runnable() {
				
				@Override
				void run()
				{
					
					// goes through each diagram selected and parses the necessary information
					int[] indices = optionsDialog.getSelectedIndices();
					indices.each { parse(diagramList.get(it)) }
					
					// outputs everything to the selected file
					export(optionsDialog.getOutputFile());
					
					// notify that it's done
					JOptionPane.showMessageDialog(null, "Export Complete!", "Notice", JOptionPane.OK_OPTION);
				}
			})
			
			// once the options dialog is set up, display it
			optionsDialog.displayGUI()
			
			
		}
		catch (Exception generalError)
		{
			log.log("[GraphML Export] Error: " + generalError.toString())
			generalError.getStackTrace().each {
				log.log("[GraphML Export]        " + it.toString())
			}
			
		}
	}
}
