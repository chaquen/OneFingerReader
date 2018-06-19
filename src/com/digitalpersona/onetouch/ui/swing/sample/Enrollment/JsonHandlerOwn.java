package com.digitalpersona.onetouch.ui.swing.sample.Enrollment;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
/**
 *
 * @author Adrian
 */
public class JsonHandlerOwn implements ResponseHandler<JSONObject>{

    public JSONObject handleResponse(final HttpResponse response) {
			int status = response.getStatusLine().getStatusCode();
			JSONObject returnData = new JSONObject();
			JSONParser parser = new JSONParser();
			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				try {
					
					if(null == entity){
						returnData.put("status_code", "1");
						returnData.put("error_message", "null Data Found");
					} else {
						returnData = (JSONObject) parser.parse(EntityUtils.toString(entity));
					}
				} catch (ParseException | IOException | org.json.simple.parser.ParseException e) {
					returnData.put("status_code", "1");
					returnData.put("error_message", e.getMessage());
				}
			} else {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Unexpected response status: " + status);
			}
			
			return returnData;
	}
    
}
