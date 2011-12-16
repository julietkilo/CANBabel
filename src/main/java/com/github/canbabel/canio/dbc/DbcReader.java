package com.github.canbabel.canio.dbc;

import com.github.canbabel.canio.kcd.BasicLabelType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.github.canbabel.canio.kcd.Bus;
import com.github.canbabel.canio.kcd.Document;
import com.github.canbabel.canio.kcd.Label;
import com.github.canbabel.canio.kcd.LabelSet;
import com.github.canbabel.canio.kcd.Message;
import com.github.canbabel.canio.kcd.Multiplex;
import com.github.canbabel.canio.kcd.MuxGroup;
import com.github.canbabel.canio.kcd.NetworkDefinition;
import com.github.canbabel.canio.kcd.Node;
import com.github.canbabel.canio.kcd.NodeRef;
import com.github.canbabel.canio.kcd.ObjectFactory;
import com.github.canbabel.canio.kcd.Producer;
import com.github.canbabel.canio.kcd.Signal;
import com.github.canbabel.canio.kcd.Value;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Reads industry widespread CAN database (*.dbc) format.
 *
 * @author julietkilo
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class DbcReader {

	private static final String MAJOR_VERSION = "0";
	private static final String MINOR_VERSION = "4";

	final private static String[] KEYWORDS = { "VERSION ", "NS_ : ", "BS_:",
			"BU_: ", "BO_ ", "SG_ ", "BO_TX_BU_ ", "CM_ ", "CM_ BO_ ",
			"CM_ SG_ ", "BA_DEF_ ", "BA_DEF_ BU_ ", "BA_DEF_REL_ BU_SG_REL_ ",
			"BA_DEF_ SG_ ", "BA_DEF_DEF_ ", "BA_DEF_DEF_REL_ ", "BA_ ", "VAL_ ",
                        "VAL_TABLE_ ", "SIG_VALTYPE_ "};

	private static final String NOT_DEFINED = "Vector__XXX";
	private static final String DOC_CONTENT = "Converted with CANBabel (https://github.com/julietkilo/CANBabel)";
	private boolean isReadable;
	private Collection<String> nodes = new ArrayList<String>();
	private ObjectFactory factory = null;
	private NetworkDefinition network = null;
	private Document document = null;
	private Bus bus = null;
	private Signal signal = null;
	private Map<Long, Set<Signal>> muxed = new TreeMap<Long, Set<Signal>>();
        private Set<LabelDescription> labels = new HashSet<LabelDescription>();
        private Set<SignalComment> signalComments = new HashSet<SignalComment>();
        private String version = "";

        private PrintWriter logWriter;

        private class LabelDescription {

            private long id;
            private String signalName;
            private Set<Label> labels;

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

        };

        private class SignalComment {

            private long id;
            private String signalName;
            private String comment;

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

        };

	public boolean parseFile(File file, OutputStream logStream) {
            logWriter = new PrintWriter(logStream);
            factory = new ObjectFactory();
            network = (NetworkDefinition) (factory.createNetworkDefinition());
            network.setVersion(MAJOR_VERSION + "." + MINOR_VERSION);
            document = (Document) (factory.createDocument());
                            document.setContent(DOC_CONTENT);
                            document.setName(file.getName());
                            Date now = Calendar.getInstance().getTime();
                            document.setDate(now.toString());
            network.setDocument(document);

            bus = (Bus) (factory.createBus());
            bus.setName("Private");


            if ((file.canRead() && file.exists())) {
                this.setReadable(true);
            }

            StringBuffer contents = new StringBuffer();
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ASCII"));
                String text = null;
                boolean isFirstLine = true;

                while ((text = reader.readLine()) != null) {
                    if (startsWithKeyword(text) && !isFirstLine) {
                        processLine(contents);
                        contents.delete(0, contents.length());
                    }
                    contents.append(text);
                    isFirstLine = false;
                }
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
                    return false;
                }
            }

            document.setVersion(version);
            /*
             * File has been completely parsed. Now the labels can be added
             * to the corresponding signals.
             */
            for(LabelDescription description : labels) {
                List<Message> messages = bus.getMessage();

                /* Find ID */
                for(Message message : messages) {
                    if(Long.parseLong(message.getId().substring(2),16) == description.getId()) {
                        List<Signal> signals = message.getSignal();
                        /* Find signal name */
                        for(Signal signal : signals) {
                            if(signal.getName().equals(description.getSignalName())) {
                                LabelSet set = new LabelSet();
                                List<BasicLabelType> labellist = set.getLabelOrLabelGroup();
                                labellist.addAll(description.getLabels());
                                signal.setLabelSet(set);
                            }
                        }
                    }
                }
            }

            /*
             * File has been completely parsed. Now the signal comments can be added
             * to the corresponding signals.
             */
            for(SignalComment comment : signalComments) {
                List<Message> messages = bus.getMessage();

                /* Find ID */
                for(Message message : messages) {
                    if(Long.parseLong(message.getId().substring(2),16) == comment.getId()) {
                        List<Signal> signals = message.getSignal();
                        /* Find signal name */
                        for(Signal signal : signals) {
                            if(signal.getName().equals(comment.getSignalName())) {
                                signal.setNotes(comment.getComment());
                            }
                        }
                    }
                }
            }

            return true;
        }

	/**
	 * Produces a file in KCD format.
	 *
	 * @param file
	 *            File to save.
	 * @return True, if operation successful.
	 */
	 public boolean writeKcdFile(File file, boolean prettyPrint, boolean gzip) {
            Writer w = null;
            try {
                JAXBContext context = JAXBContext.newInstance(new Class[]{com.github.canbabel.canio.kcd.NetworkDefinition.class});
                Marshaller marshaller = context.createMarshaller();

                if(prettyPrint)
                    marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

                if(gzip) {
                    FileOutputStream fo = new FileOutputStream(file);
                    GZIPOutputStream stream = new GZIPOutputStream(fo);
                    w = new OutputStreamWriter(stream);
                } else {
                    w = new FileWriter(file);
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
                w.close();
                } catch (Exception e) {
                    return false;
                }
            }

            return true;
        }

	public boolean isReadable() {
		return this.isReadable;
	}

	/**
	 * Returns the available network nodes as set.
	 *
	 * @return
	 */
	public List<String> getNodes() {
		return Collections.unmodifiableList((List<String>) nodes);
	}

	/**
	 * Returns true, if a line from the input file starts with a keyword from
	 * the list of
	 * <p>
	 * KEYWORDS
	 * </p>
	 * .
	 *
	 * @param line
	 *            String to check for Keyword
	 * @return true, if line starts with a keyword.
	 */
	private static boolean startsWithKeyword(String line) {
		boolean retval = false;
		line.trim();
		for (int i = 0; i < KEYWORDS.length; i++) {
			if (line.startsWith(KEYWORDS[i]))
				retval = true;
		}
		return retval;
	}

	/**
	 * Several lines of a DBC-File, which begins with a keyword
	 * will be sorted here for further processing.
	 *
	 * @param line Related parts of a dbc-file will be passed over to the
	 * suitable handling method.
	 */
	private void processLine(StringBuffer line) {

		if (Pattern.matches("BO_.?\\d+.*", line)) {
			parseMessageDefinition(line);
                } else if (Pattern.matches("VAL_TABLE_.*", line)) {

		} else if (Pattern.matches("VAL_.*", line)) {
			parseValueDescription(line);
		} else if (Pattern.matches("BA_.+\".*", line)) {
			parseAttribute(line);
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
		} else {
			logWriter.write("Line does not match:'" + line + "'\n");
		}

	}

	private void parseVersion(StringBuffer line) {
            String[] splitted =  splitString(line.toString());
            version = splitted[1];
	}

	private static void parseBitTimingSection(StringBuffer line) {
		//System.out.println("Bit timing section: " + line.toString());
	}

	private static void parseNewSymbols(StringBuffer line) {
		//System.out.println("New symbol entries: " + line.toString());
	}

	/**
	 * Handling method for network node starting by a line that begins with BU_.
	 *
	 * @param line line from dbc-file to handle.
	 */
	private void parseNetworkNode(StringBuffer line) {
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

		// System.out.println("Network Node: " + line.toString());
	}

	/**
	 * Handling method for message transmitter starting by a line that begins with BO_TX_BU_.
	 *
	 * @param line line from dbc-file to handle.
	 *
	 */
	private static void parseMessageTransmitter(StringBuffer line) {
		//System.out.println("Message transmitter: " + line.toString());

	}

	/**
	 * Handling method for message transmitter starting by a line that begins with CM_.
	 *
	 * @param line line from dbc-file to handle.
	 */
	private static void parseComment(StringBuffer line) {
		// System.out.println("Comment: " + line.toString());
	}

	/**
	 * Handling method for attributes starting by a line that begins with BA_.
	 *
	 * @param line line from dbc-file to handle.
	 */
	private static void parseAttribute(StringBuffer line) {
		// System.out.println("Attribute: " + line.toString());

	}

	/**
	 * Handling method for message definition starting by a line that begins with BO_ {decimal}.
	 *
	 * @param line passed over buffer of the line (starting with BO_ including
	 * all corresponding signals.
	 */
	private void parseMessageDefinition(StringBuffer line) {

		// reset signal context with each new message;
		signal = null;
		muxed = new TreeMap<Long, Set<Signal>>();

		// BO_ 1984 Messagename: 8 Producername
		// System.out.println("Message Definition: " + line.toString());

		// remove BO_
		line.replace(0, 4, "");
		line.trimToSize();
		String[] lineArray = line.toString().split("\\s+SG_\\s+");
		//System.out.println("Message: " + lineArray[0]);

		String[] messageArray = lineArray[0].split("\\s+");
		Message message = (Message) factory.createMessage();
		int messageIdDecimal = getCanIdFromString(messageArray[0]);

		message.setId("0x" + Integer.toString(messageIdDecimal,16).toUpperCase() );
		if (isExtendedFrameFormat(messageArray[0]))
			message.setFormat("extended");


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

			//System.out.println("Signal: " + lineArray[i]);
			parseSignal(message, lineArray[i]);
			// Signal signal = (Signal) factory.createSignal();

		}

                /* Check if we have to add a multiplex definition to the last
                 * message.
                 */
                if(muxed != null && muxed.size() > 0) {
                    if(message.getMultiplex().size() == 1) {
                        Multiplex mul = message.getMultiplex().get(0);
                        List<MuxGroup> muxgroups = mul.getMuxGroup();

                        for(Long i : muxed.keySet()) {
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

		// System.out.println("Signalliste hat " + composite.size() +
		// "Einträge");

		bus.getMessage().add(message);
	}

	private int getCanIdFromString(String canIdStr){

		long canIdLong = Long.valueOf(canIdStr).longValue();
		int canId = (int) canIdLong & 0x1FFFFFFF;
		return canId;
	}

	private boolean isExtendedFrameFormat(String canIdStr){

		long canIdLong = Long.valueOf(canIdStr).longValue();
		return ((canIdLong >>> 31 & 1) == 1) ? true : false;
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
	private void parseSignal(Message message, String line) {

		// Split signalname and mux coding from rest of line
		String[] lineArray = line.split(":");
		String signalName = lineArray[0].toString().trim();
		// Check if this is a multiplex
		if (Pattern.compile("\\w+\\s+\\w+").matcher(lineArray[0]).find()) {
			/* line is multiplexer or multiplexed signal */

			if (signalName.endsWith("M")) {
				/* signal type is multiplexor */
				/* FIN_MUX M : 0|2@1+ (1,0) [0|255] "" Motor */
				//System.out.println("###Multiplexor: " + lineArray[0]);
				Multiplex mux = (Multiplex) factory.createMultiplex();
				mux.setName(signalName.replace(" M", "").trim());

                                signal = (Signal) factory.createSignal();

                                String[] splitted = splitString(lineArray[1]);

                                if (splitted != null) {
                                        mux.setOffset(Integer.parseInt(splitted[0]));

                                        // Omit length == "1" (default)
                                        if (!splitted[1].equals("1"))
                                                mux.setLength(Integer.parseInt(splitted[1]));

                                        // find big endian signals, little is default
                                        if (splitted[2].equals("0"))
                                                mux.setEndianess("big");

                                        /*
                                         * TODO: Signed / unsigned is currenty ignored for
                                         * multiplex values.
                                         */
                                }

				message.getMultiplex().add(mux);
			} else {
				/* signal type is multiplex */
				/* Signal: FIN17 m2 : 43|8@1+ (1,0) [0|255] "" YBOX,CO2,Clima */

				//System.out.println("###Multiplex: "
				//		+ signalName);

                                signal = (Signal) factory.createSignal();

                                signal.setName(lineArray[0].split(" ")[0]);

                                /* Parse multiplex count */
                                String countstring = lineArray[0].trim();
                                for(int i=countstring.length()-1;i>0;i--) {
                                    if(countstring.charAt(i) == 'm') {
                                        countstring = countstring.substring(i+1);
                                        break;
                                    }
                                }
                                long muxcount = Long.parseLong(countstring);

                                String[] splitted = splitString(lineArray[1]);

                                if (splitted != null) {
                                        signal.setOffset(Integer.parseInt(splitted[0]));

                                        // Omit length == "1" (default)
                                        if (!splitted[1].equals("1"))
                                                signal.setLength(Integer.parseInt(splitted[1]));

                                        // find big endian signals, little is default
                                        if (splitted[2].equals("0"))
                                                signal.setEndianess("big");

                                        Value value = (Value) factory.createValue();

                                        if("-".equals(splitted[3])) {
                                            value.setType("signed");
                                        } else {
                                            value.setType("unsigned");
                                        }

                                        value.setSlope(Double.valueOf(splitted[4]));
                                        value.setIntercept(Double.valueOf(splitted[5]));

                                        if(!"".equals(splitted[8])) {
                                            value.setUnit(splitted[8]);
                                        }
                                }

                                /* Do we have a signal list for muxcount? */
                                Set<Signal> signalSet = muxed.get(muxcount);
                                if(signalSet == null) {
                                    signalSet = new HashSet<Signal>();
                                    muxed.put(muxcount, signalSet);
                                }

                                signalSet.add(signal);
			}

		} else {
			/* signal type is plain */
			parsePlainSignal(message, signalName, lineArray[1].toString().trim());
		}
		/* printMuxed(); */
	}


	/**
	 * Parses a plain signal that is not a multiplexor or muxed signal.
	 *
	 * @param message message object where the signal line belongs to and shall
	 *            append to.
	 * @param line signal line String to parse
	 */
	private void parsePlainSignal(Message message, String signalName, String line) {
		/* line e.g. "39|16@0+ (0.01,0) [0|655.35] "Km/h" ECU3" */

		//** Debug *//
		//System.out.println("@@@Signalname::" + signalName + "Line:" + line);

		signal = (Signal) factory.createSignal();
		// signal.setName(lineArray[0].replaceAll("\\w+", ""));

		String[] splitted = null;

		splitted = splitString(line);

		signal.setName(signalName);
		if (splitted != null) {
			signal.setOffset(Integer.parseInt(splitted[0]));

			// Omit length == "1" (default)
			if (!splitted[1].equals("1"))
				signal.setLength(Integer.parseInt(splitted[1]));

			// find big endian signals, little is default
			if (splitted[2].equals("0"))
				signal.setEndianess("big");

                        Value value = (Value) factory.createValue();

                        if("-".equals(splitted[3])) {
                            value.setType("signed");
                        } else {
                            value.setType("unsigned");
                        }

			Double slope = Double.valueOf(splitted[4]);
			Double intercept = Double.valueOf(splitted[5]);
			// Omit default slope = 1.0
			if (slope != 1.0)
				value.setSlope((double) slope);

			// Omit default intercept = 0.0
			if (intercept != 0.0)
				value.setIntercept((double) intercept);

                        if(!"".equals(splitted[8])) {
                            value.setUnit(splitted[8]);
                        }

			// Omit empty value elements
			if ((intercept != 0.0) || (slope != 1.0) ||
                                !"1".equals(value.getUnit()) ||
                                !"unsigned".equals(value.getType())){
				signal.setValue(value);
			}

		}
		message.getSignal().add(signal);

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
		return (c == '[' || c == ']' || c == '(' || c == ')' || c == '|'
				|| c == ',' || c == '@' || c == ' ');
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
		return (c == '+' || c == '-');
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
		return (c == '"');
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
            ArrayList<String> elements = new ArrayList<String>(10);
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
                    if(s.charAt(i-2) == '@') {
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

    private void parseValueDescription(StringBuffer line) {
        /* line e.g. "VAL_ 1234 signalname 1 "on" 2 "off" ;" */

        String[] splitted =  splitString(line.toString());

        long id = Long.valueOf(splitted[1]);
        String signalName = splitted[2];

        Set<Label> labelSet = new HashSet<Label>();


        for(int i=3;i<(splitted.length-1);i+=2) {
            Label label = new Label();

            label.setName(splitted[i+1]);
            label.setValue(BigInteger.valueOf(Long.parseLong(splitted[i])));

            labelSet.add(label);
        }

        LabelDescription description = new LabelDescription();
        description.setId(id);
        description.setSignalName(signalName);
        description.setLabels(labelSet);

        labels.add(description);
    }

    private void parseSignalComment(StringBuffer line) {
        /* line e.g. "CM_ SG_ 1234 signalname 1 "comment";" */

        String[] splitted =  splitString(line.toString());

        SignalComment comment = new SignalComment();

        comment.setId(Long.valueOf(splitted[2]));
        comment.setSignalName(splitted[3]);
        comment.setComment(splitted[4]);


        signalComments.add(comment);
    }
}
