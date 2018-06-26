package net.bakirtzis.exporter

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.ProjectUtilities
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil


class IBDParser extends Parser
{
	private def log = Application.instance.GUILog
	private def edges = []; // def containing all the edge data
	private Map<String, BaseNode> nodeMap = new HashMap<>(); // map storing all the nodes
	
	
	class BaseNode
	{
		String name;
		String device;
		String entry;
		def data;
		
		BaseNode(String name)
		{
			this.name = name;
			this.data = [];//new ArrayList();
			
			// set default values since they're required on each node
			this.device = "N/A"
			this.entry = "N/A"
		}
	}
	
	/**
	 * Determines if the provided string is a key name
	 */
	private static String getKeyName(String str)
	{
		switch (str)
		{
			case "Device Name": return "device_name";
			case "Entry Point": return "entry_point";
			case "Operating System": return "os";
			case "Hardware": return "hardware";
			case "Firmware": return "firmware";
			case "Software": return "software";
			case "Communication": return "communication";
		}
		return null;
	}
	
	/**
	 * Parses the nodes from the diagram
	 */
	def parseNode(Element element)
	{
		
		// Check to make sure it's from the standard profile
		// And all elements we're interested in is a NamedElement
		if (!ProjectUtilities.isFromStandardProfile(element) && element instanceof NamedElement)
		{
			// Make sure it's a block and that the name isn't the name of a key
			if (StereotypesHelper.hasStereotype(element, "Block") && getKeyName(element.name) == null)
			{
				// Add the node to the map
				nodeMap.put(element.name, new BaseNode(element.name));
				
				println("[+] Block: " + element.name + ", _diagramOfContext: ")
				
			}
		}
	}
	
	/**
	 * Parses the attributes for each node
	 * (device_name, entry_point, firmware, hardware, os, etc..)
	 */
	def parseAttributes(Element ele)
	{
		if (!ProjectUtilities.isFromStandardProfile(ele) && ele instanceof NamedElement)
		{
			// Check the properties of each element to determine if they're an attribute for a node
			if (StereotypesHelper.hasStereotype(ele, "PartProperty") && ele instanceof TypedElement)
			{
				// make sure its type isn't null
				if (ele.type != null)
				{
					BaseNode baseNode;
					String nodeKey = ele.type.name;
					String key = getKeyName(ele.type.name); // check to see if the name is a key
					if (key != null) // if the name is a key
					{
						// if the element name is a key, then the name we want is the owners
						nodeKey = ((NamedElement) ele.owner).name;
						if (nodeMap.containsKey(nodeKey)) // only work if there is a matching node
						{
							baseNode = nodeMap.get(nodeKey);
							if (key == "entry_point") // set the entry_point attribute
							{
								baseNode.entry = ele.name;
							}
							else
							{
								// add the key-value pair to the node
								baseNode.data << [key: key, value: ele.name]
							}
						}
					}
					else
					{
						
						// if the name is not a key, then the name is the device_name attribute
						if (nodeMap.containsKey(nodeKey))
						{
							baseNode = nodeMap.get(nodeKey);
							baseNode.device = ele.name;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Finds the ports of a block to determine the edges in the model
	 */
	def parseEdges(Element ele)
	{
		
		if (!ProjectUtilities.isFromStandardProfile(ele) && ele instanceof NamedElement)
		{
			
			// Scan the ports to determine the edges
			if (ele instanceof Port)
			{
				
				String conName = null;
				
				// search each connector connected to the port to find one with a name
				ele.end.each {
					Connector con = ((Connector) it._connectorOfEnd)
					
					// If the connector has a name, then that will be the name for all connections on the port
					if (con.name != null && con.name.length() > 0)
					{
						conName = con.name;
					}
				}
				
				// If there is a name, then use this for all edges on connected to this port
				if (conName != null)
				{
					// loop through each onnection connected on the port
					
					ele.end.each {
						List<ConnectorEnd> ends = ((Connector) it._connectorOfEnd).end;
						
						// get the source/target ends
						NamedElement source = (NamedElement) ends[0].role.owner;
						NamedElement target = (NamedElement) ends[1].role.owner;
						
						// add the edge
						edges << [source: source.name, target: target.name, label: ele.name,
						          key   : "communication", value: conName];
					}
				}
			}
		}
		
	}
	
	/**
	 *  parses all the necessary information from a diagram
	 */
	void parse(DiagramPresentationElement diagramElement)
	{
		
		// search for nodes first because the data parsing requires them
		diagramElement.getUsedModelElements(false).each {
			parseNode(it);
		}
		
		// go through each element to parse the attributes and edges
		diagramElement.getUsedModelElements(false).each {
			
			parseAttributes(it);
			parseEdges(it);
		}
		
		
		edges.unique().collect(); // eliminate any duplicate edges
		
	}
	
	/**
	 * Takes all the information and stores it in the specified file as an XML file
	 */
	void export(File file)
	{
		log.log("[GraphML Export]: Exporting graphs...")
		
		// Hardcoded keys
		def keys = []
		keys << [id: "device_name", for: "node", "attr.name": "Device", "attr.type": "string"];
		keys << [id: "entry_point", for: "node", "attr.name": "Entry Points", "attr.type": "string"];
		keys << [id: "os", for: "node", "attr.name": "Operating System", "attr.type": "string"];
		keys << [id: "hardware", for: "node", "attr.name": "Hardware", "attr.type": "string"];
		keys << [id: "firmware", for: "node", "attr.name": "Firmware", "attr.type": "string"];
		keys << [id: "software", for: "node", "attr.name": "Software", "attr.type": "string"];
		keys << [id: "communication", for: "edge", "attr.name": "Communication", "attr.type": "string"];
		
		
		
		def binder = new StreamingMarkupBuilder().bind {
			mkp.xmlDeclaration();
			
			mkp.comment("System Description Graph")
			graphml(
					xmlns: "http://graphml.graphdrawing.org/xmlns",
					"xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
					"xsi:schemaLocation": "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd"
			) {
				
				// add the keys
				mkp.comment("Keys")
				keys.each { key(it) }
				
				mkp.comment("Graph")
				graph(id: "system", edgedefault: "undirected") {
					
					
					mkp.comment("Nodes")
					
					// add each node that was found
					for (BaseNode n : nodeMap.values())
					{
						node(id: n.name) {
							// add the values for the required attributes
							data(key: "device_name", n.device)
							data(key: "entry_point", n.entry)
							
							// add the extra attributes found
							n.data.each {
								data(key: it.key, it.value)
							}
						}
					}
					
					mkp.comment("Edges")
					
					// add each edge that was found
					for (def e : edges)
					{
						edge(source: e.source, target: e.target, label: e.label) {
							data(key: e.key, e.value)
						}
					}
				}
			}
		}
		
		file.newWriter().withWriter { w ->
			w << XmlUtil.serialize(binder);
		}
	}
}
