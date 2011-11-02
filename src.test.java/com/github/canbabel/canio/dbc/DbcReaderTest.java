package com.github.canbabel.canio.dbc;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



public class DbcReaderTest {

	private static String sample = "src.test.resources/geheim.dbc";
	private static DbcReader reader;
	
    /**
     * @throws java.lang.Exception
     *             Methods with the annotation 'BeforeClass' are executed o$
     *             before the first of the series of tests. External resour$
     *             that are used by all tests should be initialized here.
     */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		reader = new DbcReader(new File(sample));
	
	}
	

	/**
     * @throws java.lang.Exception
     *             Methods with the annotation 'Before' are executed before
     *             every test. The test object should be brought to the ini$
     *             state all tests assume it to be in.
     */
	@Before
	public void setUp() throws Exception {
	}
	
    /**
     * @throws java.lang.Exception
     *             Methods with the annotation 'After' are executed after e$
     *             test.
     */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testIsReadable() throws Exception {
		assertTrue("Sample is not readable", reader.isReadable());
	}

	@Test
	public void testGetNodes() throws Exception {
		
		java.util.List<String> list = reader.getNodes();
		
        Iterator<String> iterator = list.iterator();
        //TODO Assert rule here
        
        System.out.println(list.size() + " nodes:");
        while (iterator.hasNext()) {
            System.out.print((String) iterator.next() + "; ");
        }
        System.out.println();
	}
	
	@Test
	public void testGetMessageIdentifier() throws Exception {
		//TODO Testcode here
		assertTrue("Put in testcode here",false);
	}
}
