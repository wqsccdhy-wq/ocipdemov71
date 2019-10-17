package com.seeyon.apps.ocip.webservice;

import javax.xml.ws.Endpoint;

import com.seeyon.apps.ocip.webservice.impl.OcipWebServiceImpl;

public class Test {

	public static void main(String[] args) {
		//Endpoint.publish("http://localhost:8089/test", new Function());
		Endpoint.publish("http://127.0.0.1:8089/ocipdemo/services/ocipWebService", new OcipWebServiceImpl());
		System.out.println("Publish Success");
	}

}
