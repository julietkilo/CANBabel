/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.canbabel.canio.dbc;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class DbcReaderTest {

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

}
