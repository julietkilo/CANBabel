package com.github.canbabel.canio.dbc;

public abstract class AttributeDefinition {

	private final String name;

	public static enum AttrType {
		INT, FLOAT, STRING, ENUM, HEX
	};
	
	public static enum AttrTarget {
		NETWORK, NODE, MESSAGE, SIGNAL
	};

	public final AttrType type;
	public final AttrTarget target;

	protected AttributeDefinition(String name, AttrTarget target, AttrType type) {
		this.name = name;
		this.target = target;
		this.type = type;
	}

	public final String getName() {
		return name;
	}
	
	public final AttrType getType() {
		return type;
	}
	
	public final AttrTarget getTarget() {
		return target;
	}
	
    public static AttributeDefinition.AttrType getAttrTypeFromString(String typestr) {
		if (typestr.equals("ENUM")) {
			return AttrType.ENUM;
		} else if (typestr.equals("INT")) {
			return AttrType.INT;
		} else if (typestr.equals("HEX")) {
			return AttrType.HEX;
		} else if (typestr.equals("FLOAT")) {
			return AttrType.FLOAT;
		} else if (typestr.equals("STRING")) {
			return AttrType.STRING;
		} else {
			throw new RuntimeException("unparsable type");
		}
	}
    
    public static AttributeDefinition.AttrTarget getAttrTargetFromString(String typestr) {
    	if (typestr.startsWith("\"")) {
    		return AttrTarget.NETWORK;
    	} else if (typestr.equals("BO_")) {
			return AttrTarget.MESSAGE;
		} else if (typestr.equals("BU_")) {
			return AttrTarget.NODE;
		} else if (typestr.equals("SG_")) {
			return AttrTarget.SIGNAL;
		} else {
			return null;
		}
	}

}
