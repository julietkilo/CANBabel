package com.github.canbabel.canio.dbc;

import org.junit.*;

import com.github.canbabel.canio.kcd.Bus;
import com.github.canbabel.canio.kcd.Message;
import com.github.canbabel.canio.kcd.NetworkDefinition;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

public class CANFDTest {

	DbcReader dr = null;

	public CANFDTest()
	{
	}
	
	@Before
	public void setUp()
	{
		dr = new DbcReader();
        URL url = Thread.currentThread().getContextClassLoader().getResource("canfdtest.dbc");
        File testFile = new File(url.getPath());
		dr.parseFile(testFile, System.out);
	}

	@Test
	public void testfdflag()
	{
		NetworkDefinition network = dr.getNetwork();
		
		for (Bus b : network.getBus()) {
			for (Message m : b.getMessage()) {
				
				String mname = m.getName();
				
				// the names in the test file are chosen, so that the message name contains a "_FD", if it is an FD message
				if (Pattern.matches(".*_FD_.*", mname)) {
					assertTrue(m.isCanfd());
				} else {
					assertFalse(m.isCanfd());
				}
				
				// additionally the message length is encoded in the name as a decimal suffix _XX
				int len = Integer.parseInt(mname.substring(mname.lastIndexOf('_') + 1));
				assertEquals(len, Integer.parseInt(m.getLength()));
			}
		}
		
	}
}

