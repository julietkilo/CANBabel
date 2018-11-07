package com.github.canbabel.canio.dbc;

class AttributeDefinitionInt extends AttributeDefinition {
	private final long min;
	private final long max;
	private long def;

	public AttributeDefinitionInt(String name, AttrTarget target, long min, long max)
	{
		super(name, target, AttrType.INT);
		this.min = min;
		this.max = min;
		def = 0;
	}

	void setDefault(long def)
	{
		this.def = def;
	}
	
	long getDefault()
	{
		return def;
	}
	
	long getMax()
	{
		return max;
	}
	
	long getMin()
	{
		return min;
	}
}