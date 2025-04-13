/**
 *  CANBabel - Translator for Controller Area Network description formats
 *  Copyright (C) 2011-2025 julietkilo and Jan-Niklas Meier
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

import com.github.canbabel.canio.ui.MainFrame;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.Assert.*;

public class CLITest {

    private String pdbc;
    private String pkcd;

    public CLITest() {
    }

    @Before
    public void setUp() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("CLI.dbc");
        if (url == null) throw new IllegalStateException("Resource CLI.dbc not found");
        pdbc = url.getPath();
        pkcd = pdbc.replace("dbc", "kcd");
    }

    @Test
    public void cliIncorrectParameterCountTest() {
        PrintStream recover = System.out;
        ByteArrayOutputStream sysOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(sysOutContent));

        try {
            MainFrame.main(new String[]{"incorrect parameter count"});
        } catch (Exception ignored) {
            // IGNORE
        } finally {
            System.setOut(recover);
        }
        assertEquals("Usage: CANBabel.jar [dbc-in  kcd-out]", sysOutContent.toString().trim());
    }

    @Test
    public void commandLineInterfaceTest() {
        try {
            File fkcd = new File(pkcd);
            assertTrue(fkcd.delete());
            assertFalse(fkcd.exists());
            MainFrame.main(new String[]{pdbc, pkcd});
            assertTrue(fkcd.exists());
            if (!fkcd.exists()) {
                fail("CLI didn't create kcd file");
            }
        } catch (Exception e) {
            fail("main shouldn't throw exception");
        }
    }
}

