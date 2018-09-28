package com.github.canbabel.canio.dbc;

import com.github.canbabel.canio.dbc.AttributeDefinition.AttrType;

class AttributeDefinitionString extends AttributeDefinition {
	private String def;

	public AttributeDefinitionString(String name, AttrTarget target)
	{
		super(name, target, AttrType.STRING);
	}

	void setDefault(String def)
	{
		this.def = def;
	}
}