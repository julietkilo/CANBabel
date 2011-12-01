package com.github.canbabel.canio.dbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

/**
 * Reads industry widespread CAN database (*.dbc) format.
 *
 * @author julietkilo
 *
 */
public class DbcReader {

	private static final String MAJOR_VERSION = "0";
	private static final String MINOR_VERSION = "4";

	final private static String[] KEYWORDS = { "VERSION ", "NS_ : ", "BS_:",
			"BU_: ", "BO_ ", "SG_ ", "BO_TX_BU_ ", "CM_ ", "CM_ BO_ ",
			"CM_ SG_ ", "BA_DEF_ ", "BA_DEF_ BU_ ", "BA_DEF_REL_ BU_SG_REL_ ",
			"BA_DEF_ SG_ ", "BA_DEF_DEF_ ", "BA_DEF_DEF_REL_ ", "BA_ ", "VAL_ " };

	private static final String NOT_DEFINED = "Vector__XXX";
	private static final String DOC_CONTENT = "Converted with CANBabel (https://github.com/julietkilo/CANBabel)";
	private boolean isReadable;
	private Collection<String> nodes = new ArrayList<String>();
	private JAXBContext context = null;
	private ObjectFactory factory = null;
	private NetworkDefinition network = null;
	private Document document = null;
	private Bus bus = null;
	private Marshaller marshaller = null;
	private Signal signal = null;
	private Value value = null;
	private MuxGroup muxgroup = null;
	private TreeMap<String, String> muxed = new TreeMap<String, String>();

	public boolean parseFile(File file) {
            try {
                context = JAXBContext.newInstance(new Class[]{com.github.canbabel.canio.kcd.NetworkDefinition.class});
                marshaller = context.createMarshaller();

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

            } catch (JAXBException e1) {
                return false;
            }

            try {

                if ((file.canRead() && file.exists())) {
                    this.setReadable(true);
                }

                StringBuffer contents = new StringBuffer();
                BufferedReader reader = null;

                try {
                    reader = new BufferedReader(new FileReader(file));
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
                    return false;
                } catch (IOException e) {
                    return false;
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        return false;
                    }
                }

            } catch (Exception e) {
                return false;
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
	 public boolean writeKcdFile(File file) {
            Writer w = null;
            try {
                w = new FileWriter(file);
                marshaller.marshal(network, w);
            } catch (JAXBException jxbe) {
                return false;
            } catch (IOException ioe) {
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
	private final boolean startsWithKeyword(String line) {
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
		} else if (Pattern.matches("VAL_.*", line)) {
			parseValueDescription(line);
		} else if (Pattern.matches("BA_.+\".*", line)) {
			parseAttribute(line);
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
			System.out.println("NO MATCH: " + line);
		}

	}

	private static void parseVersion(StringBuffer line) {
		System.out.println("Version: " + line.toString());

	}

	private static void parseBitTimingSection(StringBuffer line) {
		System.out.println("Bit timing section: " + line.toString());
	}

	private static void parseNewSymbols(StringBuffer line) {
		System.out.println("New symbol entries: " + line.toString());

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
		System.out.println("Message transmitter: " + line.toString());

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
	 * Handling method for value description starting by a line that begins with VAL_.
	 *
	 * @param line line from dbc-file to handle.
	 */
	private static void parseValueDescription(StringBuffer line) {
		System.out.println("Value Description: " + line.toString());

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
		muxgroup = null;
		muxed.clear();

		// BO_ 1984 Messagename: 8 Producername
		// System.out.println("Message Definition: " + line.toString());

		// remove BO_
		line.replace(0, 4, "");
		line.trimToSize();
		String[] lineArray = line.toString().split("\\s+SG_\\s+");
		// System.out.println("Message: " + lineArray[0]);

		String[] messageArray = lineArray[0].split("\\s+");

		Message message = (Message) factory.createMessage();
		message.setId("0x"
				+ Integer.toHexString(Integer.parseInt(messageArray[0])).toUpperCase());
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

			System.out.println("Signal: " + lineArray[i]);
			parseSignal(message, lineArray[i]);
			// Signal signal = (Signal) factory.createSignal();

		}

		// System.out.println("Signalliste hat " + composite.size() +
		// "Einträge");

		bus.getMessage().add(message);
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

		Multiplex mux = null;
		// Split signalname and mux coding from rest of line
		String[] lineArray = line.split(":");
		String signalName = lineArray[0].toString().trim();
		// Check if this is a multiplex
		if (Pattern.compile("\\w+\\s+\\w+").matcher(lineArray[0]).find()) {
			/* line is multiplexer or multiplexed signal */

			if (signalName.endsWith("M")) {
				/* signal type is multiplexor */
				/* FIN_MUX M : 0|2@1+ (1,0) [0|255] "" Motor */
				System.out.println("###Multiplexor: " + lineArray[0]);
				mux = (Multiplex) factory.createMultiplex();
				mux.setName(signalName.replace(" M", "").trim());
				message.getMultiplex().add(mux);
			} else {
				/* signal type is multiplex */
				/* Signal: FIN17 m2 : 43|8@1+ (1,0) [0|255] "" YBOX,CO2,Clima */

				System.out.println("###Multiplex: "
						+ signalName);
				muxgroup = (MuxGroup) factory.createMuxGroup();
				muxgroup.setCount(0);

				/* key is muxgroup e.g. 3 for all m3 */
				/* value is string of the signal */
				String[] sb = lineArray[0].split("\\s+m");

				System.out.println("line1" + lineArray[0] + lineArray[1]);
				muxed.put(sb[1], lineArray[0] + lineArray[1]);
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
		System.out.println("@@@Signalname::" + signalName + "Line:" + line);
		
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

			value = (Value) factory.createValue();
			Double slope = Double.valueOf(splitted[3]);
			Double intercept = Double.valueOf(splitted[4]);
			// Omit default slope = 1.0
			if (slope != 1.0)
				value.setSlope((double) slope);

			// Omit default intercept = 0.0
			if (intercept != 0.0)
				value.setIntercept((double) intercept);

			// Omit empty value elements
			if ((intercept != 0.0) || (slope != 1.0)){
				signal.setValue(value);
			}
				
		}
		message.getSignal().add(signal);
	
	}
		
	/**
	 * Check for character classes. Returns true if the checked character is a
	 * digit.
	 *
	 * @param c
	 *            Character to check
	 * @return True, if the character is a digit.
	 */
	private boolean isDigit(char c) {
		return ((c <= '9' && c >= '0') || c == '.');
	}

	/**
	 * Check for character classes. Returns true if the checked character is a
	 * devider.
	 *
	 * @param c
	 *            Character to check
	 * @return True, if the character is a devider.
	 */
	private boolean isDevider(char c) {
		return (c == '[' || c == ']' || c == '(' || c == ')' || c == '|'
				|| c == ',' || c == '@');
	}

	/**
	 * Check for character classes. Returns true if the checked character is a
	 * symbol.
	 *
	 * @param c
	 *            Character to check
	 * @return True, if the character is a symbol.
	 */
	private boolean isSymbol(char c) {
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
	private boolean isQuote(char c) {
		return (c == '"');
	}

	/**
	 * Check for character classes. Returns true if the checked character is a
	 * whitespace.
	 *
	 * @param c
	 *            Character to check
	 * @return True, if the character is a whitespace.
	 */
	private boolean isWhitespace(char c) {
		return (c == ' ');
	}

	/**
	 * Check for character classes. Returns true if the checked character is
	 * alphabet char.
	 *
	 * @param c
	 *            Character to check
	 * @return True, if the character is alphabet char.
	 */
	private boolean isAlpha(char c) {
		return (c <= 'Z' && c >= 'A' || c <= 'z' && c >= 'a' || c == '_'
				|| c == '/' || c == '%');
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
	private String[] splitString(String s) {
		int count = 0;
		/** Maximum number of strings in StringArray */
		final int MAX_STRINGS = 50;
		String[] array = new String[MAX_STRINGS];
		String concat = "";

		for (int i = 0; i < s.length(); i++) {

			if (isDigit(s.charAt(i))) {
				concat += s.charAt(i);
			} else if (isDevider(s.charAt(i))) {
				if (concat != "")
					array[count++] = concat;
				concat = "";
			} else if (isWhitespace(s.charAt(i))) {
				// ignore
			} else if (isSymbol(s.charAt(i))) {
				// check if minus sign is part of an exponential number
				if (concat != ""
						&& concat.substring(concat.length() - 1).equals("E")) {
					concat += "-";
				}
			} else if (isAlpha(s.charAt(i))) {
				concat += s.charAt(i);
			} else if (isQuote(s.charAt(i))) {
				if (concat != "")
					array[count++] = concat;
				concat = "";
			} else {
				// a single char is not catched by the if-else
				System.out.println("UNKNOWN CHAR:" + s.charAt(i));
			}

		}

		if (concat != "")
			array[count++] = concat;
		return array;

	}

	private void setReadable(boolean isReadable) {
		this.isReadable = isReadable;
	}

	/** 
	 * Debugging method
	 */
	private void printMuxed(){
		System.out.println("###Inhalt von Muxed:");
		for (Map.Entry<String,String> entry : muxed.entrySet()) {
			String value = entry.getValue();
		    String key = entry.getKey();
		    System.out.println(key + "=" + value);    
		}
	}
}
