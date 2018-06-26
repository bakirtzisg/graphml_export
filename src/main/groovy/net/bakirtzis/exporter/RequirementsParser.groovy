package net.bakirtzis.exporter

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.ProjectUtilities
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil


class RequirementsParser extends Parser
{
	private def log = Application.instance.GUILog
	private def edges = []; // def containing all the edge data
//	private Map<String, BaseNode> nodeMap; // map storing all the nodes
	
	private List<ReqNode> nodes = new ArrayList<>();
	
	
	class ReqNode
	{
		String name;
		String text;
		String id;
		String type;
		def parts;
		
		ReqNode(String name)
		{
			this.name = name;
			this.type = "N/A"
			this.id = null;
			this.text = null;
		}
	}
	
	/**
	 *  parses all the necessary information from a diagram
	 */
	void parse(DiagramPresentationElement diagramElement)
	{
		
		log.log("[GraphML Export]: Parsing " + diagramElement.name + "...")
		// search for nodes first because the data parsing requires them
		diagramElement.getUsedModelElements(false).each {
			
			if (!ProjectUtilities.isFromStandardProfile(it) && it instanceof NamedElement)
			{
				// Check the properties of each element to determine if they're an attribute for a node
				if (StereotypesHelper.hasStereotype(it, "Requirement"))
				{
					
					// get the requirement description
					Object textValue = StereotypesHelper.getStereotypePropertyFirst(it, "Requirement", "Text");
					Object idValue = StereotypesHelper.getStereotypePropertyFirst(it, "Requirement", "Id");
					
					ReqNode node = new ReqNode(it.name);
					node.text = textValue.toString();
					node.id = idValue.toString();
					node.type = "Requirement"
					
					nodes << node;
					
				}
				else if (StereotypesHelper.hasStereotype(it, "Block"))
				{
					ReqNode node = new ReqNode(it.name);
					node.type = "Device"
					nodes << node;
					
				}
				else if (it instanceof Diagram)
				{
					ReqNode node = new ReqNode(it.name);
					node.type = "Diagram"
					nodes << node;
				}
				else if (it instanceof Relationship)
				{
					List<Element> e = it.relatedElement.asList();
					
					edges << [source: ((NamedElement) e.get(0)).name,
					          target: ((NamedElement) e.get(1)).name,
					          label : "requirement"];
					
				}
			}
			
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
				graph(id: "requirements", edgedefault: "undirected") {
					
					
					mkp.comment("Nodes")
					
					// add each node that was found
					for (ReqNode n : nodes)
					{
						node(id: n.name) {
							// add the values for the required attributes
							data(key: "type", n.type)
							if (n.text != null)
							{
								data(key: "text", n.text)
							}
							if (n.id != null)
							{
								data(key: "id", n.id)
							}
						}
					}
					
					mkp.comment("Edges")
					
					// add each edge that was found
					
					edges.each { edge(it) }
				}
			}
		}
		
		file.newWriter().withWriter { w ->
			w << XmlUtil.serialize(binder.toString());
		}
		log.log("[GraphML Export]: Done.")
	}
}
