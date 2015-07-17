package com.stubhub.demo.api.monitor.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JschRoleUtil {
	public static String runCommandInRemoteMachine(String hostName, MyUserInfo user, String command) throws Exception{
		JSch jc = new JSch();
		
		Session session = jc.getSession(user.get_userName(), hostName, 22);		
		session.setUserInfo(user);
		session.connect();

		Channel channel=session.openChannel("exec");
		((ChannelExec)channel).setCommand(command);
		channel.setInputStream(null);
		InputStream in=channel.getInputStream();		
		OutputStream out = channel.getOutputStream();
		String consoleError = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr(new PrintStream(baos));
		((ChannelExec)channel).setErrStream(System.err);	

		channel.connect();
		String output = null;

		byte[] tmp=new byte[1024];
		while(true){
			while(in.available()>0){
				int i=in.read(tmp, 0, 1024);
				if(i<0)
					break;
				output += new String(tmp, 0, i);
			}
			if(channel.isClosed()){
				if(channel.getExitStatus() != 0){
					consoleError = baos.toString();
				}
				break;
			}
			try{
				Thread.sleep(1000);
			}
			catch(Exception ex)
			{}
		}
		channel.disconnect();
		String release = "";
		if(consoleError.length() == 0){
			release = output;
		}
		else
		{
			release = consoleError;
		}
		session.disconnect();
		return release;
	}
	
	public static String getShapeReleaseOfRole(String host, String role, MyUserInfo user) throws Exception{
		JSch jc = new JSch();
		final String commonServer = "%s%s001.%s.com";
		String hostStr = String.format(commonServer, host, role, host);
		String command = "ls -l /opt/jboss/server/default/deploy/stubhub-domain/";	
		
		String release = runCommandInRemoteMachine(hostStr, user, command);
		return release;
	}
}
