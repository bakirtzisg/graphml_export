/*
    Authors: Georgios Bakirtzis (bakirtzisg@ieee.org) & Brandon Simon (simonbj@vcu.edu)

    Status: Stable
    
    This plugin outputs SysML IBD & Requirements diagrams in the GraphML format using the MagicDraw™ OpenAPI.
    
    Requirements: MagicDraw™ OpenAPI, Java 8.
*/

package net.bakirtzis.exporter

import com.nomagic.actions.ActionsCategory
import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.plugins.Plugin


class GraphMLExportPlugin extends Plugin
{
	void init()
	{
		try
		{
			ActionsConfiguratorsManager manager = ActionsConfiguratorsManager.getInstance()
			manager.addMainMenuConfigurator(new MainMenuConfigurator(getSubMenuActions()))
			Application.getInstance().getGUILog().log("[GraphML Export] Plugin loaded properly.");
			
			
		}
		catch (Exception e)
		{
			Application.getInstance().getGUILog().log(
					"[GraphML Export] Could not instantiate plugin: " + e.toString())
		}
		
	}
	
	/*
		Adds menu items to access a given action, in this case CallFromMenu_Old
	 */
	
	def NMAction getSubMenuActions()
	{
		ActionsCategory category = new ActionsCategory(null, null)
		
		// makes sub-menu item - no need for now only one action
		//category.setNested(false)
		
		// this calls the action from the sub-menu if invoked
		category.addAction(new CallFromMenu("graphml_export", "Export"))
		
		return category
	}
	
	/*
	  This plugin does not have any close specific actions.

	  @return always true
	 */
	
	boolean close()
	{
		return true
	}
	
	/*
	  Always supported for now.

	  @return always true
	 */
	
	boolean isSupported()
	{
		return true
	}
}