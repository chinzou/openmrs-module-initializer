package org.openmrs.module.initializer.api.c;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.module.initializer.api.CsvLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component("initializer.conceptAttributeLineProcessor")
public class ConceptAttributeLineProcessor extends ConceptLineProcessor {
	
	public static String ATTRIBUTE_SEPARATOR = "|";
	
	protected static String HEADER_ATTRIBUTE = "attribute";
	
	@Autowired
	public ConceptAttributeLineProcessor(@Qualifier("conceptService") ConceptService conceptService) {
		super(conceptService);
	}
	
	/**
	 * @param headerLine
	 * @return A Map of Attribute names or/and UUIDs
	 * @throws IllegalArgumentException
	 */
	private static Map<String, String> getAttributeHeadersMap(String[] headerLine) {
		
		Map<String, String> attributes = new HashMap<String, String>();
		
		for (String attributeHeader : headerLine) {
			String[] parts = StringUtils.split(attributeHeader, ATTRIBUTE_SEPARATOR);
			if (parts.length == 2 && parts[0].trim().equals(HEADER_ATTRIBUTE)) {
				String attribute = parts[1].trim();
				if (attributes.containsValue(attribute)) {
					throw new IllegalArgumentException(
					        "The CSV header line cannot contains twice the same attribute: '" + attribute + "'");
				}
				if (attribute.isEmpty()) {
					throw new IllegalArgumentException("The attribute name or Uuid cannot be empty.");
				}
				attributes.put(attributeHeader, attribute);
			}
		}
		return attributes;
	}
	
	@Override
	protected Concept fill(Concept concept, CsvLine line) throws IllegalArgumentException {
		
		Map<String, String> conceptAttributesMap = getAttributeHeadersMap(line.getHeaderLine());
		
		List<ConceptAttributeType> conceptAttributeTypes = conceptService.getAllConceptAttributeTypes();
		
		// Filtering out non existing Attribute types
		Map<String, String> filteredAttributes = conceptAttributesMap.entrySet().stream().filter(map -> {
			for (ConceptAttributeType ca : conceptAttributeTypes) {
				if (ca.getName().equals(map.getValue()) || ca.getUuid().equals(map.getValue()))
					return true;
			}
			return false;
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Set<ConceptAttribute> cas = new HashSet<>();

		filteredAttributes.forEach((attHeader, attribute) -> {
			ConceptAttribute ca = new ConceptAttribute();
			ConceptAttributeType cat = conceptService.getConceptAttributeTypeByUuid(attribute);
			if (cat == null) {
				cat = conceptService.getConceptAttributeTypeByName(attribute);
			}
			ca.setAttributeType(cat);
			cat.getDatatypeClassname();
			ca.setValue(line.get(attHeader));
			cas.add(ca);
		});

		concept.setAttributes(cas);
		return concept;
	}
}
