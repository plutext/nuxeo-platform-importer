/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger
 */

package org.nuxeo.ecm.platform.importer.properties;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.junit.Test;

import static org.junit.Assert.*;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy({ "org.nuxeo.ecm.core.api" })
public class TestMetadataFile {

    @Inject
    protected CoreSession session;

    protected Calendar calendar = GregorianCalendar.getInstance();

    @Test
    public void generateSimpleMetadataFile() throws Exception {
        DocumentModel testFile = createTestFile();

        MetadataFile mdFile = MetadataFile.createFromDocument(testFile);

        File file = File.createTempFile("mdf", null);
        mdFile.writeTo(file);

        MetadataCollector collector = new MetadataCollector();
        collector.addPropertyFile(file);

        String contextPath = file.getParent();
        Map<String, Serializable> properties = collector.getProperties(contextPath);
        assertEquals(8, properties.size());
        assertEquals("testTitle", properties.get("dc:title"));
        assertEquals("testDescription", properties.get("dc:description"));
        assertEquals("testCoverage", properties.get("dc:coverage"));
        Date date = formatDate((String) properties.get("dc:expired")).getTime();
        DateFormat dateFormat = new SimpleDateFormat(MetadataCollector.DATE_FORMAT);
        assertEquals(dateFormat.format(calendar.getTime()), dateFormat.format(date));
        assertEquals("testIcon", properties.get("common:icon"));
        assertEquals("0", properties.get("uid:major_version"));
        assertEquals("0", properties.get("uid:minor_version"));
        assertEquals("2", properties.get("common:size"));
    }

    @Test
    public void generateOneSchemaMetadataFile() throws Exception {
        DocumentModel testFile = createTestFile();

        MetadataFile mdFile = MetadataFile.createFromSchemas(testFile, Arrays.asList(new String[] { "dublincore" }));

        File file = File.createTempFile("mdf", null);
        mdFile.writeTo(file);

        MetadataCollector collector = new MetadataCollector();
        collector.addPropertyFile(file);

        String contextPath = file.getParent();
        Map<String, Serializable> properties = collector.getProperties(contextPath);
        assertEquals(4, properties.size());
        assertEquals("testTitle", properties.get("dc:title"));
        assertEquals("testDescription", properties.get("dc:description"));
        assertEquals("testCoverage", properties.get("dc:coverage"));
        Date date = formatDate((String) properties.get("dc:expired")).getTime();
        DateFormat dateFormat = new SimpleDateFormat(MetadataCollector.DATE_FORMAT);
        assertEquals(dateFormat.format(calendar.getTime()), dateFormat.format(date));
    }

    @Test
    public void generateOneSchemaWithPropertiesMetadataFile() throws Exception {
        DocumentModel testFile = createTestFile();

        MetadataFile mdFile = MetadataFile.createFromSchemasAndProperties(testFile,
                Arrays.asList(new String[] { "common" }), Arrays.asList(new String[] { "dc:title" }));

        File file = File.createTempFile("mdf", null);
        mdFile.writeTo(file);

        MetadataCollector collector = new MetadataCollector();
        collector.addPropertyFile(file);

        String contextPath = file.getParent();
        Map<String, Serializable> properties = collector.getProperties(contextPath);
        assertEquals(3, properties.size());
        assertEquals("testTitle", properties.get("dc:title"));
        assertEquals("testIcon", properties.get("common:icon"));
        assertEquals("2", properties.get("common:size"));
        assertEquals(2L, testFile.getPropertyValue("common:size"));
    }

    @Test
    public void generatePropertiesMetadataFile() throws Exception {
        DocumentModel testFile = createTestFile();

        MetadataFile mdFile = MetadataFile.createFromProperties(testFile,
                Arrays.asList(new String[] { "dc:title", "common:icon", "dc:description", "common:size" }));

        File file = File.createTempFile("mdf", null);
        mdFile.writeTo(file);

        MetadataCollector collector = new MetadataCollector();
        collector.addPropertyFile(file);

        String contextPath = file.getParent();
        Map<String, Serializable> properties = collector.getProperties(contextPath);
        assertEquals(4, properties.size());
        assertEquals("testTitle", properties.get("dc:title"));
        assertEquals("testIcon", properties.get("common:icon"));
        assertEquals("testDescription", properties.get("dc:description"));
        assertEquals("2", properties.get("common:size"));
    }

    protected DocumentModel createTestFile() {
        DocumentModel file = session.createDocumentModel("/", "testfile", "File");
        file.setPropertyValue("dc:title", "testTitle");
        file.setPropertyValue("dc:description", "testDescription");
        file.setPropertyValue("dc:coverage", "testCoverage");
        file.setPropertyValue("dc:expired", calendar);
        file.setPropertyValue("common:icon", "testIcon");
        file.setPropertyValue("common:size", "2");
        file = session.createDocument(file);
        assertNotNull(file);
        file = session.saveDocument(file);
        session.save();

        return file;
    }

    protected Calendar formatDate(String value) throws ParseException {
        return (Calendar) new DateType().decode(value);
    }

}
