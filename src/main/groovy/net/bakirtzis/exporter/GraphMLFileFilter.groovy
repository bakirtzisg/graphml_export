package net.bakirtzis.exporter

import javax.swing.filechooser.FileFilter


class GraphMLFileFilter extends FileFilter
{
	@Override
	boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}
		
		String name = f.getName();
		int idx = name.lastIndexOf('.')
		if (idx > 0)
		{
			String ext = name.substring(idx + 1)
			return ext != null && ext == "graphml"
		}
		
		return false;
	}
	
	@Override
	String getDescription()
	{
		return "GraphML Files"
	}
}
