package org.openmrs.module.initializer.api.c;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptDatatype;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.module.initializer.DomainBaseModuleContextSensitiveTest;
import org.openmrs.module.initializer.api.CsvLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Set;

public class ConceptAttributeLineProcessorTest extends DomainBaseModuleContextSensitiveTest {

	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;

	@Before
	public void setup() throws Exception {
		executeDataSet("testdata/test-concepts.xml");
		executeDataSet("testdata/test-concepts-attribute.xml");
	}
	
	@Test
	public void fill_ShouldParseBooleanAttributes() throws Exception {
		
		// Setup
		String[] headerLine = { "Data type", "attribute|Test" };
		String[] line = { "Numeric", "true" };
		
		ConceptAttributeLineProcessor calp = new ConceptAttributeLineProcessor(cs);
		
		Concept c = calp.fill(new Concept(), new CsvLine(headerLine, line));

		Set<ConceptAttribute> attributes = c.getAttributes();

		attributes.forEach((conceptAttribute) -> {
			Assert.assertTrue(conceptAttribute.getValue().equals(true));
		});

	}
}
