/*
 * Copyright 2020 Oliver Siegmar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siegmar.fastcsv.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class CsvReaderHeaderTest {

    private final CsvReaderBuilder crb = CsvReader.builder();

    @Test
    public void nullInput() {
        assertThrows(NullPointerException.class, () -> parse(findBugsSafeNullInput()));
    }

    private static String findBugsSafeNullInput() {
        return null;
    }

    @Test
    public void empty() {
        final NamedCsvReader parse = parse("");
        assertEquals(Collections.emptyList(), parse.getHeader());
        final Iterator<NamedCsvRow> it = parse.iterator();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void onlyHeader() {
        final NamedCsvReader csv = parse("foo,bar\n");
        assertEquals(Arrays.asList("foo", "bar"), csv.getHeader());
        assertFalse(csv.iterator().hasNext());
        assertThrows(NoSuchElementException.class, () -> csv.iterator().next());
    }

    @Test
    public void getFieldByName() {
        assertEquals(Optional.of("bar"), readSingleRow("foo\nbar").getField("foo"));
    }

    @Test
    public void getHeader() {
        assertEquals(Collections.singletonList("foo"), parse("foo\nbar").getHeader());
        assertEquals(Arrays.asList("foo", "bar"), parse("foo,bar\n1,2").getHeader());
    }

    @Test
    public void getHeaderEmptyRows() {
        final NamedCsvReader csv = parse("foo,bar");
        assertEquals(Arrays.asList("foo", "bar"), csv.getHeader());
        final Iterator<NamedCsvRow> it = csv.iterator();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void getHeaderWithoutNextRowCall() {
        assertEquals(Collections.singletonList("foo"), parse("foo\n").getHeader());
    }

    // Request field by name, but column name doesn't exist
    @Test
    public void getNonExistingFieldByName() {
        assertEquals(Optional.empty(), readSingleRow("foo\nfaz").getField("bar"));
    }

    @Test
    public void toStringWithHeader() {
        final NamedCsvRow csvRow = readSingleRow("headerA,headerB,headerC\nfieldA,fieldB\n");
        assertEquals(
            "NamedCsvRow[headerMap={headerA=0, headerB=1, headerC=2}, "
                + "row=IndexedCsvRow[originalLineNumber=2, fields=[fieldA, fieldB]]]",
            csvRow.toString());

        assertEquals("{headerA=fieldA, headerB=fieldB, headerC=null}",
            csvRow.getFieldMap().toString());
    }

    @Test
    public void fieldMap() {
        final Iterator<NamedCsvRow> it = parse("headerA,headerB,headerC\n"
            + "fieldA,fieldB\n"
            + ",fieldB2,fieldC2,fieldD2\n")
            .iterator();

        assertEquals("{headerA=fieldA, headerB=fieldB, headerC=null}",
            it.next().getFieldMap().toString());

        assertEquals("{headerA=, headerB=fieldB2, headerC=fieldC2}",
            it.next().getFieldMap().toString());
    }

    // test helpers

    private NamedCsvReader parse(final String data) {
        return crb.build(new StringReader(data)).withHeader();
    }

    private NamedCsvRow readSingleRow(final String data) {
        final List<NamedCsvRow> lists = readAll(data);
        assertEquals(1, lists.size());
        return lists.get(0);
    }

    private List<NamedCsvRow> readAll(final String data) {
        return parse(data)
            .stream()
            .collect(Collectors.toList());
    }

}
