package net.bakirtzis.exporter

import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement


abstract class Parser
{
	abstract void parse(DiagramPresentationElement dpe);
	abstract void export(File file);
}
