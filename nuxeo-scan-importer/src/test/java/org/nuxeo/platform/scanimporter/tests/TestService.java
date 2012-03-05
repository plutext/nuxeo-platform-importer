package org.nuxeo.platform.scanimporter.tests;

import org.nuxeo.ecm.platform.scanimporter.service.ImporterConfig;
import org.nuxeo.ecm.platform.scanimporter.service.ScanFileMappingDescriptor;
import org.nuxeo.ecm.platform.scanimporter.service.ScannedFileMapperComponent;
import org.nuxeo.ecm.platform.scanimporter.service.ScannedFileMapperService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

public class TestService extends NXRuntimeTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployContrib("org.nuxeo.ecm.platform.scanimporter", "OSGI-INF/importerservice-framework.xml");
    }

    public void testServiceLookup() {
        ScannedFileMapperService sfms = Framework.getLocalService(ScannedFileMapperService.class);
        assertNotNull(sfms);
    }

    public void testServiceContrib() throws Exception {

        ScannedFileMapperService sfms = Framework.getLocalService(ScannedFileMapperService.class);
        ScanFileMappingDescriptor desc =  ((ScannedFileMapperComponent)sfms).getMappingDesc();

        assertNull(desc);
        deployContrib("org.nuxeo.ecm.platform.scanimporter.test", "OSGI-INF/importerservice-test-contrib0.xml");

        desc =  ((ScannedFileMapperComponent)sfms).getMappingDesc();
        assertNotNull(desc);

        assertEquals(2,desc.getFieldMappings().size());
        assertEquals(1,desc.getBlobMappings().size());

        assertEquals("xpath1",desc.getFieldMappings().get(0).getSourceXPath());
        assertEquals("attr1",desc.getFieldMappings().get(0).getSourceAttribute());
        assertEquals("string",desc.getFieldMappings().get(0).getTargetType());
        assertEquals("dc:title",desc.getFieldMappings().get(0).getTargetXPath());;


        assertEquals("xpath3",desc.getBlobMappings().get(0).getSourceXPath());
        assertEquals("filePath1",desc.getBlobMappings().get(0).getSourcePathAttribute());
        assertEquals("fileName1",desc.getBlobMappings().get(0).getSourceFilenameAttribute());
        assertEquals("file:content",desc.getBlobMappings().get(0).getTargetXPath());

    }

    public void testServiceConfig() throws Exception {

        ScannedFileMapperService sfms = Framework.getLocalService(ScannedFileMapperService.class);

        deployContrib("org.nuxeo.ecm.platform.scanimporter.test", "OSGI-INF/importerservice-test-config0.xml");

        ImporterConfig config = sfms.getImporterConfig();

        assertNotNull(config);

        assertEquals("/tmp/somefolder",config.getSourcePath());
        assertEquals("/tmp/processed",config.getProcessedPath());
        assertEquals(new Integer(2),config.getNbThreads());
        assertEquals(new Integer(5),config.getBatchSize());
        assertEquals("/default-domain/import",config.getTargetPath());

    }


}