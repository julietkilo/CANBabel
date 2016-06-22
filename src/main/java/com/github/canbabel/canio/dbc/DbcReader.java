/**
 *  CANBabel - Translator for Controller Area Network description formats
 *  Copyright (C) 2011-2015 julietkilo and Jan-Niklas Meier
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

import com.github.canbabel.canio.kcd.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Reads industry widespread CAN database (*.dbc) format.
 *
 * @author julietkilo
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class DbcReader {

    private static final String[] KEYWORDS =
        { "VERSION ", "NS_ : ", "BS_:", "BU_: ", "BO_ ", "BO_TX_BU_ ",
        "CM_ ", "CM_ BO_ ", "CM_ EV_ ", "CM_ SG_ ", "BA_DEF_ ", "BA_DEF_ BU_ ",
        "BA_DEF_REL_ BU_SG_REL_ ", "BA_DEF_ SG_ ",  "BA_ ", "EV_ ", "VAL_ ",
        "BA_DEF_DEF_ ", "BA_DEF_DEF_REL_ ", "VAL_TABLE_ ", "SIG_VALTYPE_ "};
    private static final String NOT_DEFINED = "Vector__XXX";
    private static final String ORPHANED_SIGNALS = "VECTOR__INDEPENDENT_SIG_MSG";
    private static final String DOC_CONTENT = "Converted with CANBabel (https://github.com/julietkilo/CANBabel)";
    private static final String UTF8 = "UTF-8";
    private boolean isReadable;
    private Collection<String> nodes = new ArrayList<String>();
    private ObjectFactory factory = null;
    private NetworkDefinition network = null;
    private Document document = null;
    private Bus bus;
    private Map<Long, Set<Signal>> muxed = new TreeMap<Long, Set<Signal>>();
    private final Set<LabelDescription> labels = new HashSet<LabelDescription>();
    private final Set<SignalComment> signalComments = new HashSet<SignalComment>();
    private String version = "";
    private PrintWriter logWriter;

    public DbcReader() {
        this.bus = null;
    }

    private static class LabelDescription {

        private long id;
        private String signalName;
        private Set<Label> labels;
        private boolean extended;

        public boolean isExtended() {
            return extended;
        }

        public void setExtended(boolean extended) {
            this.extended = extended;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Set<Label> getLabels() {
            return labels;
        }

        public void setLabels(Set<Label> labels) {
            this.labels = labels;
        }

        public String getSignalName() {
            return signalName;
        }

        public void setSignalName(String signalName) {
            this.signalName = signalName;
        }
    }

    private static class SignalComment {

        private long id;
        private String signalName;
        private String comment;
        private boolean extended;

        public boolean isExtended() {
            return extended;
        }

        public void setExtended(boolean extended) {
            this.extended = extended;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getSignalName() {
            return signalName;
        }

        public void setSignalName(String signalName) {
            this.signalName = signalName;
        }
    }

    /**
     * Find a single CAN message from the list of messages
     * @param messages List of message objects
     * @param id CAN identifier of the message to find
     * @param e True, if the message to find is of extended frame format
     * @return Message object found, null otherwise
     */
    private static Message findMessage(List<Message> messages, long id, boolean e) {
        for (Message message : messages){
            boolean extended = "extended".equals(message.getFormat());
            if (Long.parseLong(message.getId().substring(2), 16) == id
                    && extended == e) {
                    return message;
            }
        }
        return null;
    }

    /**
     * Find a single CAN signal from list of CAN messages.
     * @param messages List of messages object
     * @param id Identifier of CAN message to find
     * @param e True, if CAN message to find is extended frame format
     * @param name Name of signal to find
     * @return Signal object found, null otherwise
     */
    private static Signal findSignal(List<Message> messages, long id, boolean e, String name) {
        Message message;
        message = findMessage(messages, id, e);
        List<Signal> signals;

        if (message != null){
            signals = message.getSignal();
        } else if (id == 0) {
            /* orphaned signal found */
            return null;
        } else {
            /* valid signal found but message not defined */
            return null;
        }
    
                   
        /* Find signal name */
        for (Signal signal : signals) {
            if (signal.getName().equals(name)) {
                return signal;
            }
        }

        for (Multiplex multiplex : message.getMultiplex()) {
            for (MuxGroup group : multiplex.getMuxGroup()) {
                for (Signal signal : group.getSignal()) {
                    if (signal.getName().equals(name)) {
                        return signal;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Read in given CAN database file (*.dbc)
     * @param file CAN database filehandle to read.
     * @param logStream OutputStream to write out stack traces
     * @return true, if file has been successfully read.
     */
    public boolean parseFile(File file, OutputStream logStream) {
        try {
            logWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(logStream,"ISO-8859-1")), true);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DbcReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        factory = new ObjectFactory();
        network = (NetworkDefinition) (factory.createNetworkDefinition());
        document = (Document) (factory.createDocument());
        document.setContent(DOC_CONTENT);
        document.setName(file.getName());
        Date now = Calendar.getInstance().getTime();
        document.setDate(now.toString());
        network.setDocument(document);

        bus = (Bus) (factory.createBus());
        /** TODO Allow customized bus names */
        bus.setName("Private");


        if ( file.canRead() && file.exists() ) {
            this.setReadable(true);
        }

        StringBuilder contents = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
            String text;
            boolean isFirstLine = true;

            while ((text = reader.readLine()) != null) {
                if (startsWithKeyword(text) && !isFirstLine) {
                    processLine(contents);
                    contents.delete(0, contents.length());
                }
                contents.append(text);
                isFirstLine = false;
            }
			// since there is no last keyword to trigger the parsing of the last block, we just parse it.
			processLine(contents);
            network.getBus().add(bus);
        } catch (FileNotFoundException e) {
            e.printStackTrace(logWriter);
            return false;
        } catch (IOException e) {
            e.printStackTrace(logWriter);
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace(logWriter);
            }
        }

        /*
         * File has been completely parsed. Now the labels can be added
         * to the corresponding signals.
         */
        for (LabelDescription description : labels) {
            List<Message> messages = bus.getMessage();

            LabelSet set = new LabelSet();
            List<BasicLabelType> labellist = set.getLabelOrLabelGroup();
            labellist.addAll(description.getLabels());

            Signal signal;
            signal = findSignal(messages, description.getId(), description.isExtended(), description.getSignalName());

            if (signal != null) {
                signal.setLabelSet(set);
            }
        }

        /*
         * File has been completely parsed. Now the signal comments can be added
         * to the corresponding signals.
         */
        for (SignalComment comment : signalComments) {
            List<Message> messages = bus.getMessage();

            /* Find ID */
            Signal signal = findSignal(messages, comment.getId(), comment.isExtended(), comment.getSignalName());
            if (signal != null) {
                signal.setNotes(comment.getComment());
            }
        }

        return true;
    }

    /**
     * Produces a file in KCD format.
     *
     * @param file File to save.
     * @param prettyPrint True, to format for human reading.
     * @param gzip True, to compress output file.
     * @return True, if operation successful.
     */
    public boolean writeKcdFile(File file, boolean prettyPrint, boolean gzip) {
        Writer w = null;
        try {
            JAXBContext context = JAXBContext.newInstance(new Class[]{com.github.canbabel.canio.kcd.NetworkDefinition.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, UTF8);

            if (prettyPrint) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            }

            if (gzip) {
                FileOutputStream fo = new FileOutputStream(file);
                GZIPOutputStream stream = new GZIPOutputStream(fo);
                w = new OutputStreamWriter(stream, UTF8);
            } else {
                FileOutputStream fo = new FileOutputStream(file);
                w = new OutputStreamWriter(fo, UTF8);
            }
            marshaller.marshal(network, w);
        } catch (JAXBException jxbe) {
            jxbe.printStackTrace(logWriter);
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace(logWriter);
            return false;
        } finally {
            try {
                if (w != null)  w.close();
            } catch (IOException e) {
                e.printStackTrace(logWriter);
            }
        }

        return true;
    }

    /**
     * Returns true, if a line from the input file starts with a keyword from
     * the list of <p>KEYWORDS</p>.
     *
     * @param line
     *            String to check for Keyword
     * @return true, if line starts with a keyword.
     */
    private static boolean startsWithKeyword(String line) {
        for (String keyword : KEYWORDS) {
            if (line.startsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Several lines of a DBC-File, which begins with a keyword
     * will be sorted here for further processing.
     *
     * @param line Related parts of a dbc-file will be passed over to the
     * suitable handling method.
     */
    private void processLine(StringBuilder line) {

        if (Pattern.matches("BO_.?\\d+.*", line)) {
            parseMessageDefinition(line);
        } else if (Pattern.matches("VAL_TABLE_.*", line)) {
        } else if (Pattern.matches("VAL_.?\\d+.*", line)) {
            try {
                parseValueDescription(line);
            } catch (Exception e) {
                System.err.println(line);
            }
        } else if (Pattern.matches("BA_DEF_.+\".*", line)) {
        } else if (Pattern.matches("BA_\\s+\".*", line)) {
            try {
                parseAttribute(line);
            } catch (Exception e) {
                System.err.println(line + e.getMessage());
            }
        } else if (Pattern.matches("CM_ SG_.*", line)) {
            parseSignalComment(line);
        } else if (Pattern.matches("CM.*", line)) {
            parseComment(line);
        } else if (Pattern.matches("BO_TX_BU_.*", line)) {
            parseMessageTransmitter(line);
        } else if (Pattern.matches("BU_.*", line)) {
            parseNetworkNode(line);
        } else if (Pattern.matches("NS_.?:.*", line)) {
            parseNewSymbols(line);
        } else if (Pattern.matches("BS_.*", line)) {
            parseBitTimingSection(line);
        } else if (Pattern.matches("VERSION.*", line)) {
            parseVersion(line);
        } else if (Pattern.matches("EV_.*", line)) {
            parseEnvironmentVariable(line);
        } else if (Pattern.matches("VAL_.?\\w+.*", line)) {
            parseEnvironmentVariableDescription(line);
        } else {
            logWriter.write("Line does not match:'" + line + "'\n");
        }

    }

    private void parseVersion(StringBuilder line) {
        String[] splitted = splitString(line.toString());
        version = splitted[1];
    }

    private static void parseBitTimingSection(StringBuilder line) {
        // ignore
    }

    private static void parseNewSymbols(StringBuilder line) {
        // ignore
    }

    /**
     * Handling method for network node starting by a line that begins with BU_.
     *
     * @param line line from dbc-file to handle.
     */
    private void parseNetworkNode(StringBuilder line) {
        line.replace(0, 5, "");
        line.trimToSize();
        String[] lineArray = line.toString().split("\\s+");

        nodes = Arrays.asList(lineArray);

        for (String nodeString : nodes) {
            Node node = (Node) factory.createNode();
            node.setId(nodeString);
            node.setName(nodeString);
            network.getNode().add(node);
        }
    }

    /**
     * Handling method for message transmitter starting by a line that begins with BO_TX_BU_.
     *
     * @param line line from dbc-file to handle.
     *
     */
    private static void parseMessageTransmitter(StringBuilder line) {
        // ignore
    }

    /**
     * Handling method for message transmitter starting by a line that begins with CM_.
     *
     * @param line line from dbc-file to handle.
     */
    private static void parseComment(StringBuilder line) {
        // ignore
    }

    /**
     * Handling method for attributes starting by a line that begins with BA_.
     *
     * @param line line from dbc-file to handle.
     */
    private void parseAttribute(StringBuilder line) {
        
        /* Find message with given id in GenMsgCycleTime and attach to message node */
         if (Pattern.matches("BA_\\s+\"GenMsgCycleTime.*", line)) {
             String[] splitted = splitString(line.toString());
             if (splitted != null) {
                List<Message> messages = bus.getMessage();
                Message message =  findMessage(messages, getCanIdFromString(splitted[3]), isExtendedFrameFormat(splitted[3]) );
                Float fval = Float.valueOf(splitted[4].substring(0, splitted[4].length()-1));
                Integer ival = Math.round(fval);
                // Omit default interval = 0
                if (ival != 0) {
                    message.setInterval(ival);
                }
             }
         }
    }

    /**
     * Handling method for message definition starting by a line that begins with BO_ {decimal}.
     *
     * @param line passed over buffer of the line (starting with BO_ including
     * all corresponding signals e.g.
     * BO_ 2684354547 ExtMsgBig2: 8 Bob SG_ TestSigBigDouble1 : 7|64@0- (2,0) [0|0] "" Vector__XXX
     */
    private void parseMessageDefinition(StringBuilder line) {

        muxed = new TreeMap<Long, Set<Signal>>();

        // BO_ 1984 Messagename: 8 Producername

        // remove BO_
        line.replace(0, 4, "");
        line.trimToSize();
        String[] lineArray = line.toString().split("\\s*SG_\\s+");

        String[] messageArray = lineArray[0].split("\\s+");
        Message message = (Message) factory.createMessage();
        int messageIdDecimal = getCanIdFromString(messageArray[0]);

        message.setId("0x" + Integer.toString(messageIdDecimal, 16).toUpperCase());
        if (isExtendedFrameFormat(messageArray[0])) {
            message.setFormat("extended");
        }

        message.setName(messageArray[1].replace(":", ""));
        message.setLength(messageArray[2]);
        if (!messageArray[3].contains(NOT_DEFINED)) {
            Producer producer = (Producer) factory.createProducer();
            NodeRef ref = (NodeRef) factory.createNodeRef();
            ref.setId(messageArray[3]);
            producer.getNodeRef().add(ref);
            message.setProducer(producer);
        }
        for (int i = 1; i < lineArray.length; i++) {
            parseSignal(message, lineArray[i]);
        }

        /* Check if we have to add a multiplex definition to the last
         * message.
         */
        if (muxed != null && muxed.size() > 0) {
            if (message.getMultiplex().size() == 1) {
                Multiplex mul = message.getMultiplex().get(0);
                List<MuxGroup> muxgroups = mul.getMuxGroup();

                for (Long i : muxed.keySet()) {
                    MuxGroup group = new MuxGroup();
                    group.setCount(i);
                    group.getSignal().addAll(muxed.get(i));
                    muxgroups.add(group);
                }
            }

        } else {
            /* Make sure there is no empty multiplex in the message */
            message.getMultiplex().clear();
        }

        /* Skip messages with signals that have not defined a parent message */
        if (!message.getName().contains(ORPHANED_SIGNALS)) {
            bus.getMessage().add(message);
        }
    }

    /**
     * Handling method for environment variable starting by a line that begins with EV_.
     *
     * @param line line from dbc-file to handle.
     *
     */
    private void parseEnvironmentVariable(StringBuilder line) {
         // ignore
    }

    /**
     * Handling method for environment variable starting by a line that begins with VAL_ {string}.
     *
     * @param line line from dbc-file to handle.
     *
     */
    private void parseEnvironmentVariableDescription(StringBuilder line) {
        // ignore
    }

    public static int getCanIdFromString(String canIdStr) {

        long canIdLong = Long.valueOf(canIdStr);
        int canId = (int) canIdLong & 0x1FFFFFFF;
        return canId;
    }

    public static boolean isExtendedFrameFormat(String canIdStr) {

        long canIdLong = Long.valueOf(canIdStr);
        return ((canIdLong >>> 31 & 1) == 1);
    }

    private enum SignalType {

        MULTIPLEXOR, MULTIPLEX, PLAIN
    };

    /**
     * Parses a the part of a signal line that is same for plain, multiplexor
     * or muxed signal.
     *
     * @param message message object where the signal line belongs to and shall
     *            append to.
     * @param signalName name of the signal as parsed before the line string begins
     * @param type signal type that is one of multiplexor, multiplex or plain signal.
     * @param line signal line String to parse  e.g. "39|16@0+ (0.01,0) [0|655.35] "Km/h" ECU3"
     */
    private Signal parseSignalLine(Message message, String signalName, SignalType type, String line) {
        Value value = null;

        Signal tSignal = (Signal) factory.createSignal();

        tSignal.setName(signalName);
        String[] splitted = splitString(line);
        String[] sConsumers = Arrays.copyOfRange(splitted, 9, splitted.length);

        if (splitted != null) {
            int offset = Integer.parseInt(splitted[0]);
            int length = Integer.parseInt(splitted[1]);
            boolean isBigEndian = "0".equals(splitted[2]);

            // Omit length == "1" (default)
            if (length > 1) {
                tSignal.setLength(length);
            }

            if (isBigEndian && length > 1) {
                // big endian signal and signal length greater than 1
                tSignal.setOffset(bigEndianLeastSignificantBitOffset(offset, length));
            } else {
                // little endian OR signal length == 1
                tSignal.setOffset(offset);
            }

            if (isBigEndian) {
                tSignal.setEndianess("big");
            }

            double slope = Double.valueOf(splitted[4]);
            double intercept = Double.valueOf(splitted[5]);
            double min = Double.valueOf(splitted[6]);
            double max = Double.valueOf(splitted[7]);

            if (sConsumers.length > 0) {
                Consumer consumer = (Consumer) factory.createConsumer();
                for (String sConsumer : sConsumers) {
                    NodeRef ref = (NodeRef) factory.createNodeRef();
                    consumer.getNodeRef().add(ref);
                    ref.setId(sConsumer);
                }
                tSignal.setConsumer(consumer);
            }

            if ((intercept != 0.0)
                    || (slope != 1.0)
                    || !"".equals(splitted[8])
                    || "-".equals(splitted[3])
                    || (min != 0.0) || (max != 1.0)) {

                value = (Value) factory.createValue();

                if ("-".equals(splitted[3])) {
                    value.setType("signed");
                }

                // Omit default slope
                if (slope != 1.0) {
                    value.setSlope(slope);
                }

                // Omit default intercept = 0.0
                if (intercept != 0.0) {
                    value.setIntercept(intercept);
                }

                // Omit empty units
                if (!"".equals(splitted[8])) {
                    value.setUnit(splitted[8]);
                }

                // Omit default min = 0.0
                if (min != 0.0) {
                    value.setMin(min);
                }

                // Omit default max = 1.0
                if (max != 1.0) {
                    value.setMax(max);
                }
            // End value part
            }
        // End line split
        }

        if (type == SignalType.MULTIPLEXOR) {

            Multiplex mux = (Multiplex) factory.createMultiplex();
            mux.setName(tSignal.getName());
            mux.setOffset(tSignal.getOffset());
            mux.setConsumer(tSignal.getConsumer());
            if (tSignal.getLength() != 1) {
                mux.setLength(tSignal.getLength());
            }
            if ("big".equals(tSignal.getEndianess())) {
                mux.setEndianess(tSignal.getEndianess());
            }
            mux.setValue(value);
            message.getMultiplex().add(mux);
            return null;

        } else {
            Signal signal = (Signal) factory.createSignal();
            signal.setName(tSignal.getName());
            signal.setOffset(tSignal.getOffset());
            signal.setConsumer(tSignal.getConsumer());
            if (tSignal.getLength() != 1) {
                signal.setLength(tSignal.getLength());
            }
            if ("big".equals(tSignal.getEndianess())) {
                signal.setEndianess(tSignal.getEndianess());
            }
            signal.setValue(value);
            if (type == SignalType.PLAIN){
                // Prevent from adding MULTIPLEX signals twice
                message.getSignal().add(signal);
            }
            return signal;
        }
    }

    /**
     * Parses a dbc file signal line without the SG_ header. Parses also signal
     * lines with multiplexed signals (e.g. m2) and multiplexors (M).
     *
     * @param message
     *            message object where the signal line belongs to and shall
     *            append to.
     * @param line
     *            signal line String to parse
     */
    protected void parseSignal(Message message, String line) {

        // Split signalname and mux coding from rest of line
        String[] lineArray = line.split(":");
        String signalName = lineArray[0].trim();
        // Check if this is a multiplex
        if (Pattern.compile("\\w+\\s+\\w+").matcher(lineArray[0]).find()) {
            /* line is multiplexer or multiplexed signal */

            if (signalName.endsWith("M")) {
                /* signal type is multiplexor */
                /* FIN_MUX M : 0|2@1+ (1,0) [0|255] "" Motor */
                // Remove multiplex coding ' M' from name "Muxname M"
                parseSignalLine(message, signalName.substring(0, signalName.length() - 2), SignalType.MULTIPLEXOR, lineArray[1]);

            } else {
                /* signal type is multiplex */
                /* Signal: FIN17 m2 : 43|8@1+ (1,0) [0|255] "" YBOX,CO2,Clima */

                /* Parse multiplex count */
                String countstring = lineArray[0].trim();
                for (int i = countstring.length() - 1; i > 0; i--) {
                    if (countstring.charAt(i) == 'm') {
                        countstring = countstring.substring(i + 1);
                        break;
                    }
                }
                long muxcount = Long.parseLong(countstring);

                Signal signal = parseSignalLine(message, lineArray[0].split(" ")[0], SignalType.MULTIPLEX, lineArray[1]);


                /* Do we have a signal list for muxcount? */
                Set<Signal> signalSet = muxed.get(muxcount);
                if (signalSet == null) {
                    signalSet = new HashSet<Signal>();
                    muxed.put(muxcount, signalSet);
                }

                signalSet.add(signal);
            }

        } else {
            /* signal type is plain */
            parseSignalLine(message, signalName, SignalType.PLAIN, lineArray[1].trim());
        }

    }

    /**
     * Check for character classes. Returns true if the checked character is a
     * devider.
     *
     * @param c
     *            Character to check
     * @return True, if the character is a devider.
     */
    private static boolean isDivider(char c) {
        return c == '[' || c == ']' || c == '(' || c == ')' || c == '|'
                || c == ',' || c == '@' || c == ' ';
    }

    /**
     * Check for character classes. Returns true if the checked character is a
     * symbol.
     *
     * @param c
     *            Character to check
     * @return True, if the character is a symbol.
     */
    private static boolean isSymbol(char c) {
        return c == '+' || c == '-';
    }

    /**
     * Check for character classes. Returns true if the checked character is a
     * quotation.
     *
     * @param c
     *            Character to check
     * @return True, if the character is a quotation.
     */
    private static boolean isQuote(char c) {
        return c == '"';
    }

    /**
     * Method to split a signal string in fields. A typical string looks like
     *
     * 56|8@1+ (1,0) [0|255] "km/h" Motor Brake Gearbox
     *
     * Returned array looks like
     *
     * {"56","8","1","1","0","0","255",""km/h"","Motor","Brake","Gearbox"}
     *
     * @param s
     *            String to split in fields
     * @return String array containing the seperated value elements in ascending
     *         order.
     */
    protected static String[] splitString(String s) {
        List<String> elements = new ArrayList<String>(10);
        String element = "";
        boolean inString = false;

        for (int i = 0; i < s.length(); i++) {

            /* Dividers in strings are ignored */
            if (!inString && isDivider(s.charAt(i))) {
                if (!"".equals(element)) {
                    elements.add(element);
                }
                element = "";
                /*
                 * Inside a string + and - are ignored, outside they are
                 * valid elements.
                 */
            } else if (!inString && isSymbol(s.charAt(i))) {
                /* Signed unsigned character */
                if (s.charAt(i - 2) == '@') {
                    elements.add(element);
                    element = "" + s.charAt(i);
                    /*
                     * Otherwise symbol is either part of an exponential
                     * or a negative number
                     */
                } else {
                    element += s.charAt(i);
                }
            } else if (isQuote(s.charAt(i))) {
                if (inString) {
                    elements.add(element);
                    element = "";
                    inString = false;
                } else {
                    inString = true;
                }
                /* Default: add to element */
            } else {
                element += s.charAt(i);
            }
        }

        if (!"".equals(element)) {
            elements.add(element);
        }
        return elements.toArray(new String[elements.size()]);
    }

    private void setReadable(boolean isReadable) {
        this.isReadable = isReadable;
    }

    /**
     * Handling method for value description starting by a line that begins with VAL_ {integer}.
     *
     * @param line line from dbc-file to handle e.g. "VAL_ 1234 signalname 1 "on" 2 "off" ;".
     *
     */
    private void parseValueDescription(StringBuilder line) {

        String[] splitted = splitString(line.toString());

        LabelDescription description = new LabelDescription();
        description.setExtended(isExtendedFrameFormat(splitted[1]));
        description.setId(getCanIdFromString(splitted[1]));
        description.setSignalName(splitted[2]);
        Set<Label> labelSet = new TreeSet<Label>(new LabelComparator());

        for (int i = 3; i < (splitted.length - 1); i += 2) {
            Label label = new Label();

            label.setName(splitted[i + 1]);
            label.setValue(int32ToBigInt(new BigInteger(splitted[i])));
            labelSet.add(label);
        }

        description.setLabels(labelSet);

        labels.add(description);
    }

    /**
     * Handling method for signal comments starting by a line that begins with CM_ SG_ {integer}.
     * @param line line from dbc-file to handle e.g. "CM_ SG_ 1234 signalname 1 "comment";".
     */
    private void parseSignalComment(StringBuilder line) {

        String[] splitted = splitString(line.toString());
        SignalComment comment = new SignalComment();
        comment.setExtended(isExtendedFrameFormat(splitted[2]));
        comment.setId(getCanIdFromString(splitted[2]));
        comment.setSignalName(splitted[3]);
        comment.setComment(splitted[4]);

        signalComments.add(comment);
    }

    /**
     * Correct C long int values bigger than 2^31-1 to a BigInteger representation.
     * @param big BigInteger of C long int
     * @return Corrected value as BigInteger representation
     */
    public static BigInteger int32ToBigInt(BigInteger big){
        if ( big.signum() != -1 ){
            return big;
        } else {
            // negative because C long int value exceeds 2^31-1
            return big.add(BigInteger.valueOf(4294967296L));
        }
    }

    /**
     * Calculates the least significant bit offset for big endian byte order
     * @param msb Most significant bit offset
     * @param length signal length in bit
     * @return lsb Least significant bit offset
     */
    public static int bigEndianLeastSignificantBitOffset(int msb, int length){
        int lsb;
        int pos;
        int cpos;
        int bytes;
        pos = 7 - (msb % 8) + (length - 1);
        if (pos < 8){
            /* msb pass a byte order */
            lsb = msb - length + 1;
        } else {
            cpos = 7 - (pos % 8);
            bytes = pos / 8;
            lsb = cpos + (bytes * 8) + (msb/8) * 8;
        }

        return lsb;
    }
}
