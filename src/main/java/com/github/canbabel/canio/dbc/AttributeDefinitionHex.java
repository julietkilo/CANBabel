package com.github.canbabel.canio.dbc;

class AttributeDefinitionHex extends AttributeDefinition {
	private final int min;
	private final int max;
	private int def;

	public AttributeDefinitionHex(String name, AttrTarget target, int min, int max)
	{
		super(name, target, AttrType.HEX);
		this.min = min;
		this.max = min;
		def = 0;
	}

	void setDefault(int def)
	{
		this.def = def;
	}
	
	int getDefault()
	{
		return def;
	}

	int getMax()
	{
		return max;
	}
	
	int getMin()
	{
		return min;
	}
}