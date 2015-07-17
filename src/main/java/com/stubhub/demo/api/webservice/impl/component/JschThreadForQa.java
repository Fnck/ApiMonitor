package com.stubhub.demo.api.webservice.impl.component;

import java.net.UnknownHostException;

import com.stubhub.demo.api.monitor.util.JschUtil;

public class JschThreadForQa implements Runnable {

	@Override
	public void run() {
		try {
			JschUtil.syncQaShapeList();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
