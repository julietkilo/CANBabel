# CANBabel

## Overview

CANBabel is a conversion tool for CAN database files. It was created because most CAN databases are stored in proprietary formats that are not documented and can not be used for free.
The open source CAN analysis tool [**Kayak**](https://github.com/dschanoeh/Kayak/ "Kayak is an application for CAN bus diagnosis and monitoring") introduced a new and well documented XML based format: KCD (file suffix .kcd). KCD is the acronym for Kayak CAN definition.
Initially **CANBabel** supported only conversion from the .dbc format to the .kcd format but the goal is to provide a tool that supports many formats.

## Supported formats
Currently the following formats are supported (Read/Write):

* Kayak CAN Definition (.kcd) (Read/Write)
* Vector (.dbc) (Read)

## Build instructions
You will need a current Java JDK and Maven to build **CANBabel**. After cloning the repository just type
	$ mvn clean install
and maven will fetch all dependencies and build **CANBabel**. You can also just add the repository in your favorite Java IDE as a new _maven project_.

## KCD Example

    <NetworkDefinition xmlns="http://kayak.2codeornot2code.org/1.0">
        <Document name="kcdexample.dbc" date="Tue Aug 18 09:27:40 CEST 2015">Converted with CANBabel (https://github.com/julietkilo/CANBabel)</Document>
        <Node id="15" name="ECU_Clima"/>
        <Bus name="Comfort" baudrate="125000">
            <Message id="0x21C" name="Temperature">
                <Producer>
	            <NodeRef id="15"/>
                </Producer>
                <Signal name="InsideTempC" offset="0" length="12">
                    <Value type="signed" unit="Cel"/>
                </Signal>
                <Signal name="OutsideTempC" offset="12" length="12">
                    <Value type="signed" unit="Cel"/>
                </Signal>
            </Message>
        </Bus>
    </NetworkDefinition>
