package com.stubhub.demo.api.monitor.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.geronimo.mail.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.stubhub.demo.api.monitor.entity.ArtifactInfo;

public class JschUtil {
	private static Logger logger = LoggerFactory.getLogger(JschUtil.class);
	
	public static void main(String[] arg){
		String[] roles = {"apx"};
		
		
		MyUserInfo user = new MyUserInfo();
		user.set_userName("bqiao");
		user.setPassword(new String(Base64.decode("cUAyMTY0Mjg=")));
		List<ArtifactInfo> artifactInQA = new ArrayList<ArtifactInfo>();
		for(String role : roles){
			String returnStr;
			try {
				returnStr = JschRoleUtil.getShapeReleaseOfRole("srwd78", role, user);
				for(String line : returnStr.split("\\r?\\n")){
					if(line.indexOf("com.stubhub.domain")>-1){
						String[] tmp = line.split("\\s+");
						String shapePackage = tmp[tmp.length-1];
						ArtifactInfo a = getArtifactByWarString(shapePackage);
						a.setRole(role.toUpperCase());
						a.setPool("srwd78");
						artifactInQA.add(a);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("access {} role in srwd78 got exception!",role);
				continue;
			}
			
		}
		
		for(ArtifactInfo a : artifactInQA){
			logger.info("name:{}, ver:{}, role:{}",a.getArtifactName(), a.getVersion(), a.getRole());
		}
	}

	public static void syncQaShapeList() throws UnknownHostException{
		String[] roles = {"apx",
				"api",
				"brx",
				"byx",
				"cat",
				"ini",
				"mwx",
				"myx",
				"pay",
				"pdf",
				"rec",
				"slx",
				"ilg",
				"stj",
				"dsc",
				"srh",
				"itl",
				"usr",
				"cmx",
				"trk",
				"lgi",
				"rfi"};
		
		
		MyUserInfo user = new MyUserInfo();
		user.set_userName("bqiao");
		user.setPassword(new String(Base64.decode("bmVkQDIxNjQyOA==")));
		List<ArtifactInfo> artifactInQA = new ArrayList<ArtifactInfo>();
		for(String role : roles){
			String returnStr;
			try {
				returnStr = JschRoleUtil.getShapeReleaseOfRole("srwd78", role, user);
				for(String line : returnStr.split("\\r?\\n")){
					if(line.indexOf("com.stubhub.domain")>-1){
						String[] tmp = line.split("\\s+");
						String shapePackage = tmp[tmp.length-1];
						ArtifactInfo a = getArtifactByWarString(shapePackage);
						a.setRole(role.toUpperCase());
						a.setPool("srwd78");
						artifactInQA.add(a);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("access {} role in srwd78 got exception!",role);
				continue;
			}
			
		}
		MongoDBUtil mu = new MongoDBUtil();
		mu.updateQaArtifactList(artifactInQA);
	}
	
	public static String readRemoteFile(String source, String host, MyUserInfo user){
		String content = null;
		try{

			JSch jsch=new JSch();
			Session session=jsch.getSession(user.get_userName(), host, 22);

			// username and password will be given via UserInfo interface.
			
			session.setUserInfo(user);
			session.connect();

			// exec 'scp -f rfile' remotely
			String command="scp -f "+source;
			Channel channel=session.openChannel("exec");;
			((ChannelExec)channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out=channel.getOutputStream();
			InputStream in=channel.getInputStream();

			channel.connect();

			byte[] buf=new byte[1024];

			// send '\0'
			buf[0]=0; out.write(buf, 0, 1); out.flush();

			while(true){
				int c=checkAck(in);
				if(c!='C'){
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize=0L;
				while(true){
					if(in.read(buf, 0, 1)<0){
						// error
						break; 
					}
					if(buf[0]==' ')break;
					filesize=filesize*10L+(long)(buf[0]-'0');
				}

				String file=null;
				for(int i=0;;i++){
					in.read(buf, i, 1);
					if(buf[i]==(byte)0x0a){
						file=new String(buf, 0, i);
						break;
					}
				}

				logger.info("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0]=0; 
				out.write(buf, 0, 1); 
				out.flush();

				// read a content of lfile
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				
				int index;
				while(true){
					if(buf.length < filesize){
						index= buf.length;
					}else{
						index = (int)filesize;
					}
					index = in.read(buf, 0, index);
					if(index < 0){
						break;
					}
					bos.write(buf, 0, index);
					filesize -= index;
					if(filesize == 0l){
						break;
					}
				}
				bos.flush();
				bos.close();
				content = bos.toString();
				bos = null;
				
				if(checkAck(in)!=0){
					System.exit(0);
				}

				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
			}

			session.disconnect();
		}
		catch(Exception e){
			logger.error("Got exception", e);
		}
		return content;
	}
	
	static int checkAck(InputStream in) throws IOException{
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;

		if(b==1 || b==2){
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
			if(b==1){ // error
				logger.error(sb.toString());
			}
			if(b==2){ // fatal error
				logger.error(sb.toString());
			}
		}
		return b;
	}
	
	public static ArtifactInfo getArtifactByWarString(String input){
		ArtifactInfo artifact = new ArtifactInfo();
		artifact.setExtension(input.substring(input.length()-3,input.length()));
		String tmp = input.substring(0, input.length()-4);
		int p = tmp.lastIndexOf("-");
		String packageName = tmp.substring(0, p);
		String version = tmp.substring(p+1, tmp.length());
		artifact.setArtifactName(packageName);
		artifact.setVersion(version);
		return artifact;		
	}
}
