package com.github.canbabel.canio.dbc;

class Attribute {

	private final AttributeDefinition definition;

	private long intval;
	private float floatval;
	private String stringval;
	
	/**
	 * if this is a message or signal attribute, this references the message (of which the signal is part of) by its id
	 */
	private long message;
	
	/**
	 * if this is a signal attribute, this references the Signal by its name
	 */
	private String signal;
	private String node;

	public Attribute(AttributeDefinition def, long intval)
	{
		if (def.type != AttributeDefinition.AttrType.INT && def.type != AttributeDefinition.AttrType.HEX && def.type != AttributeDefinition.AttrType.ENUM) {
			throw new NumberFormatException("cannot create int attribute from non int/hex attribute definition.");
		}
		definition = def;
		this.intval = intval;
	}

	public Attribute(AttributeDefinition def, float floatval)
	{
		if (def.type != AttributeDefinition.AttrType.FLOAT) {
			throw new NumberFormatException("cannot create float attribute from non float attribute definition.");
		}
		definition = def;
		this.floatval = floatval;
	}

	public Attribute(AttributeDefinition def, String stringval)
	{
		if (def.type != AttributeDefinition.AttrType.STRING) {
			throw new NumberFormatException("cannot create string attribute from non string attribute definition.");
		}
		definition = def;
		this.stringval = stringval;
	}

	long getInt()
	{
		if (definition.type != AttributeDefinition.AttrType.INT && definition.type != AttributeDefinition.AttrType.HEX) {
			throw new NumberFormatException("cannot get int type, if type is not int or hex");
		}
		return intval;
	}

	float getFloat()
	{
		if (definition.type != AttributeDefinition.AttrType.FLOAT) {
			throw new NumberFormatException("cannot get float type, if type is not float");
		}
		return floatval;
	}

	String getString()
	{
		if (definition.type != AttributeDefinition.AttrType.STRING) {
			throw new NumberFormatException("cannot get string type, if type is not string");
		}
		return stringval;
	}

	long getEnumAsInt()
	{
		if (definition.type != AttributeDefinition.AttrType.ENUM) {
			throw new NumberFormatException("cannot get enum type, if type is not enum");
		}
		return intval;
	}

	String getEnumAsString()
	{
		if (definition.type != AttributeDefinition.AttrType.ENUM) {
			throw new NumberFormatException("cannot get enum type, if type is not enum");
		}
		return ((AttributeDefinitionEnum)definition).getEnum_int(intval);
	}
	
	public AttributeDefinition.AttrTarget getTarget()
	{
		return definition.target;
	}
	
	public AttributeDefinition.AttrType getType()
	{
		return definition.type;
	}
	
	public void setMessage(long id)
	{
		this.message = id;
	}
	
	public void setSignal(String signal)
	{
		this.signal = signal;
	}
	
	public void setNode(String node)
	{
		this.node = node;
	}
	
	public long getMessage()
	{
		return message;
	}
	
	public String getSignal()
	{
		return signal;
	}
	
	public String getNode()
	{
		return node;
	}
	
	public String getName()
	{
		return definition.getName();
	}
}