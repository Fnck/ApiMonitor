package com.stubhub.demo.api.monitor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stubhub.demo.api.monitor.entity.JarInfo;
import com.stubhub.demo.util.DiffLineByLine;

public class CompareXSD {

	public static void main(String[] args) throws MalformedURLException {
		//File f1 = new File("C:\\firstClass.xsd");
		//File f2 = new File("C:\\secondClass.xsd");
		//DiffLineByLine df = new DiffLineByLine();
		//System.out.println(df.diffFilesLineByLine(f1.getPath(), f2.getPath()));
		String s = "abcdef";
		String m = "abcfed";
		String z = "def";
		String d = "fed";
		int i1 = s.compareTo(m);
		int i2 = z.compareTo(d);
		System.out.println(i1 == i2);
		String mstr = "hello:";
		char tmp = '.';
		int i = 0;
		while(tmp != ':'){			
			tmp = mstr.charAt(i);
			System.out.println("Got it once");
			i++;
		}
	}
	
	
}
