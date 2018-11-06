package com.github.canbabel.canio.dbc;

import org.junit.*;

import com.github.canbabel.canio.kcd.Bus;
import com.github.canbabel.canio.kcd.Message;
import com.github.canbabel.canio.kcd.NetworkDefinition;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

public class CANFDTest {

    DbcReader dr = null;

    public CANFDTest() {
    }

    @Before
    public void setUp() {
        dr = new DbcReader();
        URL url = Thread.currentThread().getContextClassLoader().getResource("canfdtest.dbc");
        File testFile = new File(url.getPath());
        dr.parseFile(testFile, System.out);
    }

    /**
     * @author nautsch
     *
     *         the idea here is to put a comment into the message of the test dbc,
     *         which describes the attributes of the message. this way, we can
     *         check, if the message was parsed correctly.
     */
    class messageflags {
        private boolean brs = false;
        private boolean fd = false;
        private int len = 0;
        private boolean ext = false;

        public messageflags(String in) {
            if (in != null && !in.isEmpty()) {
                String[] flags = in.split(":");
                if (flags.length != 4) {
                    throw new RuntimeException("number of flags in comment is wrong");
                }

                // System.out.println("Ext: " + flags[0] + " fd: " + flags[1] + " brs: " + flags[2] + " len: " + flags[3]);

                ext = flags[0].equals("ext");
                fd = flags[1].equals("fd");
                brs = flags[2].equals("brs");
                len = Integer.parseInt(flags[3]);
            }
        }

        public boolean getbrs() {
            return brs;
        }

        public boolean getfd() {
            return fd;
        }

        public int getlen() {
            return len;
        }

        public boolean getext() {
            return ext;
        }
    }

    @Test
    public void testflags() {
        NetworkDefinition network = dr.getNetwork();

        for (Bus b : network.getBus()) {
            for (Message m : b.getMessage()) {
                String notes = m.getNotes();
                if (notes != null && !notes.isEmpty()) {

                    messageflags flags = new messageflags(notes);

                    assertTrue(flags.getbrs() == m.isBitrateswitch());
                    assertTrue(flags.getfd() == m.isFd());
                    assertTrue(flags.getlen() == Integer.parseInt(m.getLength()));
                    if (m.getFormat() == "extended") {
                        assertTrue(flags.getext());
                    } else {
                        assertFalse(flags.getext());
                    }
                }
            }
        }

    }
}
