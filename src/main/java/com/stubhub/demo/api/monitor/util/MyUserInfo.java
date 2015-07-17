package com.stubhub.demo.api.monitor.util;

import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class MyUserInfo implements UIKeyboardInteractive, UserInfo {

	private String _pwd;
	private String _userName;
	
	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		//String m = this.decodeBase64("emVkQDIxNjQyOA==");
		return _pwd;
	}

	public void setPassword(String input){
		this._pwd = input;
	}
	
	private String decodeBase64(String input){
		byte[] p = Base64.decodeBase64(input);
		String result = new String(p);
		return result;
	}
	
	@Override
	public boolean promptPassphrase(String arg0) {
		return false;
	}

	@Override
	public boolean promptPassword(String arg0) {
		return true;
	}

	@Override
	public boolean promptYesNo(String arg0) {
		return true;
	}

	@Override
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	@Override
	public String[] promptKeyboardInteractive(String arg0, String arg1,
			String arg2, String[] arg3, boolean[] arg4) {
		return null;
	}

	public String get_userName() {
		return _userName;
	}

	public void set_userName(String _userName) {
		this._userName = _userName;
	}

}