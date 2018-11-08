package com.github.canbabel.canio.dbc;

class AttributeDefinitionString extends AttributeDefinition {

    private String def;

	public AttributeDefinitionString(String name, AttrTarget target)
	{
		super(name, target, AttrType.STRING);
		def = ""; // we need to set an empty default here.
	}

	public void setDefault(String def)
	{
	    this.def = def;
	}

	public String getDefault()
	{
	    return def;
	}
}
