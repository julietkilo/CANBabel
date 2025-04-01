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
import com.github.canbabel.canio.ui.SchemaValidator;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class DbcReadFileTest {

    @RunWith(Parameterized.class)
    public static class DbcReadWithParameterizedFile {

        private final String filename;

        public DbcReadWithParameterizedFile(String filename) {
            this.filename = filename;
        }

        @Parameterized.Parameters(name="Test - {0}")
        public static Collection<String> files() {
            Collection<String> paths = new ArrayList<>();
            Path resources = Paths.get("./src/test/resources");

            try {
                Files
                        .list(resources)
                        .filter(resource -> resource.toString().endsWith(".dbc"))
                        .forEach(resource -> paths.add(resource.toString()));
            } catch (IOException e) {
                System.err.println("Error reading resource files: " + e.getMessage());
            }

            return paths;
        }

        @Test
        public void readAndValidateTest(){
            File fdbc = new File(filename);
            DbcReader reader = new DbcReader();

            String kcd = filename.replaceAll("dbc", "kcd");
            File fkcd = new File(kcd);

            if (reader.parseFile(fdbc, System.out)) {
                reader.writeKcdFile(fkcd, true, false);

                StreamSource source = new StreamSource(fkcd);
                SchemaValidator validator = new SchemaValidator(System.out);

                assertTrue(validator.validate(source));
            }
        }

    }
}
