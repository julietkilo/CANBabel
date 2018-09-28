package com.github.canbabel.canio.dbc;

class AttributeDefinitionFloat extends AttributeDefinition {
	private final float min;
	private final float max;
	private float def;

	public AttributeDefinitionFloat(String name, AttrTarget target, float min, float max)
	{
		super(name, target, AttrType.FLOAT);
		this.min = min;
		this.max = min;
		def = 0;
	}

	void setDefault(float def)
	{
		this.def = def;
	}
	
	float getDefault()
	{
		return def;
	}

	float getMax()
	{
		return max;
	}
	
	float getMin()
	{
		return min;
	}
}