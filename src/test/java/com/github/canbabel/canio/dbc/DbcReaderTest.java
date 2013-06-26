/**
 *  CANBabel - Translator for Controller Area Network description formats
 *  Copyright (C) 2011 julietkilo and Jan-Niklas Meier
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.github.canbabel.canio.dbc;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class DbcReaderTest {

    private DbcReader dr = null;
    private File testFile = null;

    
    public DbcReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        dr = new DbcReader();
        URL url = Thread.currentThread().getContextClassLoader().getResource("read_in_test.dbc");
        testFile = new File(url.getPath());        
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSplitString() {
        String[] result = DbcReader.splitString("56|8@1+ (1,0) [0|255] \"km/h\" Motor Brake Gearbox");

        assertEquals(12, result.length);

        assertEquals(result[0], "56");
        assertEquals(result[1], "8");
        assertEquals(result[2], "1");
        assertEquals(result[3], "+");
        assertEquals(result[4], "1");
        assertEquals(result[5], "0");
        assertEquals(result[6], "0");
        assertEquals(result[7], "255");
        assertEquals(result[8], "km/h");
        assertEquals(result[9], "Motor");
        assertEquals(result[10], "Brake");
        assertEquals(result[11], "Gearbox");
    }

    @Test
    public void testSplitString2() {
        String[] result = DbcReader.splitString("0|8@1+ (1,0) [0|0] \"!#$%&'()*+,-./0123456789:foobar\" Vector__XXX");

        assertEquals(10, result.length);

        assertEquals("0", result[0]);
        assertEquals("8", result[1]);
        assertEquals("1", result[2]);
        assertEquals("+", result[3]);
        assertEquals("1", result[4]);
        assertEquals("0", result[5]);
        assertEquals("0", result[6]);
        assertEquals("0", result[7]);
        assertEquals("!#$%&'()*+,-./0123456789:foobar", result[8]);
        assertEquals("Vector__XXX", result[9]);
    }

    @Test
    public void testFindTestFileRessource() {

        assertTrue(testFile != null); 
    }
    /*
    @Test
    public void testGetNodes() {
        
        assertTrue(dr.parseFile(testFile, null));

    }
    */
    
}
