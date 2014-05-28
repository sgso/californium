/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and re-implementation
 *    Dominique Im Obersteg - parsers and initial implementation
 *    Daniel Pauli - parsers and initial implementation
 *    Kai Hudalla - logging
 ******************************************************************************/
package org.eclipse.californium.core.test;

import java.util.LinkedList;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.DiscoveryResource;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.core.server.resources.ResourceBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ResourceAttributesTest {

	private Resource root;
	
	@Before
	public void setup() {
		try {
			System.out.println("\nStart "+getClass().getSimpleName());
			EndpointManager.clear();
			
			root = new ResourceBase("");
			Resource sensors = new ResourceBase("sensors");
			Resource temp = new ResourceBase("temp");
			Resource light = new ResourceBase("light");
			root.add(sensors);
			sensors.add(temp);
			sensors.add(light);
			
			sensors.getAttributes().setTitle("Sensor Index");
			temp.getAttributes().addResourceType("temperature-c");
			temp.getAttributes().addInterfaceDescription("sensor");
			temp.getAttributes().addAttribute("foo");
			temp.getAttributes().addAttribute("bar", "one");
			temp.getAttributes().addAttribute("bar", "two");
			light.getAttributes().addResourceType("light-lux");
			light.getAttributes().addInterfaceDescription("sensor");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Test
	public void testDiscovery() {
		DiscoveryResource discovery = new DiscoveryResource(root);
		String serialized = discovery.discoverTree(root, new LinkedList<String>());
		System.out.println(serialized);
		Assert.assertEquals(serialized,
				"</sensors>;title=\"Sensor Index\"," +
				"</sensors/light>;if=\"sensor\";rt=\"light-lux\"," +
				"</sensors/temp>;if=\"sensor\";foo;rt=\"temperature-c\";bar=\"one two\""
				);
	}
	
	@Test
	public void testDiscoveryFiltering() {
		Request request = Request.newGet();
		request.setURI("/.well-known/core?rt=light-lux");
		
		DiscoveryResource discovery = new DiscoveryResource(root);
		String serialized = discovery.discoverTree(root, request.getOptions().getURIQueries());
		System.out.println(serialized);
		Assert.assertEquals(serialized, 
				"</sensors/light>;if=\"sensor\";rt=\"light-lux\""
				);
	}
	
}