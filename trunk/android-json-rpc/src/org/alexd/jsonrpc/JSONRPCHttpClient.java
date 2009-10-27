package org.alexd.jsonrpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of JSON-RPC over HTTP/POST
 */
public class JSONRPCHttpClient extends JSONRPCClient
{
	/*
	 * HttpClient to issue the HTTP/POST request
	 */
	private HttpClient httpClient;
	/*
	 * Service URI
	 */ 
	private String serviceUri;
	
	/**
	 * Construct a JsonRPCClient with the given service uri
	 * @param uri uri of the service
	 */
	public JSONRPCHttpClient(String uri)
	{
		httpClient = new DefaultHttpClient();
		serviceUri = uri;
	}

	protected JSONObject doJSONRequest(JSONObject jsonRequest) throws JSONRPCException
	{
		//Create HTTP/POST request with a JSON entity containing the request
		HttpPost request = new HttpPost(serviceUri);
		HttpEntity entity;
		try
		{
			entity = new JSONEntity(jsonRequest);
		} 
		catch (UnsupportedEncodingException e1) 
		{
			throw new JSONRPCException("Unsupported encoding", e1);
		}
		request.setEntity(entity);
		
		try
		{
			//Execute the request and try to decode the JSON Response
			HttpResponse response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity());
			responseString = responseString.trim();
			JSONObject jsonResponse = new JSONObject(responseString);
			//Check for remote errors
			Object jsonError = jsonResponse.get("error");
			if (!jsonError.equals(null))
			{
				throw new JSONRPCException(jsonError);
			}
			else
			{
				return jsonResponse;
			}
		}
		//Underlying errors are wrapped into a JSONRPCException instance
		catch (ClientProtocolException e) 
		{
			throw new JSONRPCException("HTTP error", e);
		}
		catch (IOException e)
		{
			throw new JSONRPCException("IO error", e);
		}
		catch (JSONException e) 
		{
			throw new JSONRPCException("Invalid JSON response", e);
		}
	}	
}