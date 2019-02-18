/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-Present by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.appc.titanium.socketio;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Manager;
import io.socket.client.Socket;

@Kroll.module(name = "TiSocketio", id = "ti.socketio")
public class TiSocketioModule extends KrollModule
{
	// Standard Debugging variables
	private static final String LCAT = "TiSocketioModule";
	private static final boolean DBG = TiConfig.LOGD;

	private HashMap<Manager, SocketManagerProxy> managerCache;

	public TiSocketioModule()
	{
		super();

		this.managerCache = new HashMap<Manager, SocketManagerProxy>();
	}

	// JS Methods

	@Kroll.method
	public SocketClientProxy connect(String uri, @Kroll.argument(optional=true) HashMap jsOptions) throws URISyntaxException
	{
		boolean autoConnect = jsOptions != null ? TiConvert.toBoolean(jsOptions, "autoConnect", true) : true;
		Options options = this.convertOptions(jsOptions);
		Socket socket = IO.socket(uri, options);
		if (autoConnect) {
			socket.connect();
		}
		SocketManagerProxy managerProxy;
		if (this.managerCache.containsKey(socket.io())) {
			managerProxy = this.managerCache.get(socket.io());
		} else {
			managerProxy = new SocketManagerProxy(socket.io(), autoConnect);
			this.managerCache.put(socket.io(), managerProxy);
		}
		SocketClientProxy socketProxy = new SocketClientProxy(socket, managerProxy);

		return socketProxy;
	}

	@Kroll.method
	public SocketManagerProxy Manager(String uri, @Kroll.argument(optional=true) HashMap jsOptions) throws URISyntaxException
	{
		Options options = this.convertOptions(jsOptions);
		Manager manager = new Manager(new URI(uri), options);
		boolean autoConnect = jsOptions != null ? TiConvert.toBoolean(jsOptions, "autoConnect", true) : true;
		return new SocketManagerProxy(manager, autoConnect);
	}

	// Private methods

	private Options convertOptions(HashMap<String, Object> options)
	{
			Options socketOptions = new Options();
			if (options == null) {
					return socketOptions;
			}

			HashMap<String, Object> jsOptions = new HashMap<String, Object>(options);

			jsOptions.remove("autoConnect");
			jsOptions.remove("parser");
			jsOptions.remove("decoder");
			jsOptions.remove("encoder");
			jsOptions.remove("transportOptions");

			socketOptions = this.createSocketOptions(jsOptions);

			return socketOptions;
	}

	private Options createSocketOptions(HashMap<String, Object> jsOptions)
	{
			Options socketOptions = new Options();
			Class<? extends Options> socketOptionsClass = socketOptions.getClass();
			for (Map.Entry<String, Object> entry : jsOptions.entrySet()) {
					String optionName = entry.getKey();
					Object optionValue = entry.getValue();

					if (optionName.equals("query") && optionValue instanceof HashMap) {
							optionValue = this.buildQueryString((HashMap<?, ?>)optionValue);
					}
					if (optionName.equals("transports") && optionValue.getClass().isArray()) {
						optionValue = Arrays.copyOf((Object[])optionValue, ((Object[])optionValue).length, String[].class);
					}

					try {
							Field propertyField = socketOptionsClass.getField(optionName);
							propertyField.set(socketOptions, optionValue);
					} catch (Exception e) {
							if (e instanceof NoSuchFieldException) {
									Log.w(LCAT, String.format("There is no option named \"%s\".", optionName));
							} else if (e instanceof IllegalArgumentException) {
									Log.w(LCAT, String.format("Invalid value for option \"%s\".", optionName));
							} else {
									Log.w(LCAT, String.format("Error while trying to set socket option %s. Error: %s", optionName, e.getMessage()));
							}
					}
			}

			return socketOptions;
	}

	private String buildQueryString(HashMap<?, ?> queryParams)
	{
			StringBuilder queryStringBuilder = new StringBuilder("");
			for (Map.Entry<?, ?> entry : queryParams.entrySet()) {
					if (queryStringBuilder.length() > 0) {
							queryStringBuilder.append("&");
					}
					String encodedParamName = this.urlEncode(entry.getKey().toString());
					String encodedParamValue = this.urlEncode(entry.getValue().toString());
					queryStringBuilder.append(String.format("%s=%s", encodedParamName, encodedParamValue));
			}

			return queryStringBuilder.toString();
	}

	private String urlEncode(String urlPart) {
			try {
					return URLEncoder.encode(urlPart, "UTF-8");
			} catch (UnsupportedEncodingException e) {
					return urlPart;
			}
	}
}
