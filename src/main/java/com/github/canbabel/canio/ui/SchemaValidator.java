package com.github.canbabel.canio.ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.canbabel.canio.dbc.DbcReader;
import com.github.canbabel.canio.kcd.NetworkDefinition;

public class SchemaValidator {

    private Validator schema_validator = null;

    private PrintWriter logWriter;

    public SchemaValidator(OutputStream logStream) {
        try {
            logWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(logStream, "ISO-8859-1")), true);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DbcReader.class.getName()).log(Level.FINE, null, ex);
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        InputStream resourceAsStream = NetworkDefinition.class.getResourceAsStream("Definition.xsd");

        if (resourceAsStream != null) {

            Source s = new StreamSource(resourceAsStream);
            Schema schema;

            try {
                schema = schemaFactory.newSchema(s);
                schema_validator = schema.newValidator();
            } catch (SAXException ex) {
                ex.printStackTrace(logWriter);
            }

            ErrorHandler handler = new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    exception.printStackTrace(logWriter);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    exception.printStackTrace(logWriter);
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    exception.printStackTrace(logWriter);
                }
            };
            try {
                schema_validator.setErrorHandler(handler);
            } catch (Exception e) {
                e.printStackTrace(logWriter);
            }

        } else {
            // if schema can't be found skip validation part
            logWriter.print("Network definition schema can't be found in jar. Started from commandline?\n");
        }
    }

    public boolean validate(StreamSource source) {
        if (schema_validator != null) {
            try {
                schema_validator.validate(source);
                return true;
            } catch (SAXException ex) {
                ex.printStackTrace(logWriter);
                return false;
            } catch (IOException ex) {
                ex.printStackTrace(logWriter);
                return false;
            }
        } else {
            logWriter.println("Could not validate resulting KCD. Schema not found/usable.");
            return false;
        }
    }
}
