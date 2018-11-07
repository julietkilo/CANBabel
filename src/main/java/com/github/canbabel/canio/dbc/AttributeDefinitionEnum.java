package com.github.canbabel.canio.dbc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class AttributeDefinitionEnum extends AttributeDefinition {
	private Map<Long, String> enumval_int;
	private Map<String, Long> enumval_str;
	private long def;

	public AttributeDefinitionEnum(String name, AttrTarget target, List<String> values)
	{
		super(name, target, AttrType.ENUM);
		enumval_int = new HashMap<Long, String>();
		enumval_str = new HashMap<String, Long>();
		long cnt = 0;
		for (Iterator<String> it = values.iterator(); it.hasNext();) {
			String aname = it.next();
			enumval_int.put(cnt, aname);
			enumval_str.put(aname, cnt);
			++cnt;
		}
	}

	public String getEnum_int(long v)
	{
		return enumval_int.get(v);
	}
	
	public long getEnum_String(String v)
	{
		return enumval_str.get(v);
	}

	public void setDefault(long def)
	{
		this.def = def;
	}
	
	public void setDefault(String defstr)
	{
		this.def = enumval_str.get(defstr);
	}
	
	public String getDefaultAsString()
	{
		return enumval_int.get(def);
	}
	
	public long getDefaultAsInt()
	{
		return def;
	}
}