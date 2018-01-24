package com.hedvig.memberservice.externalApi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class SanctionList {

	static String listURL = "https://webgate.ec.europa.eu/europeaid/fsd/fsf/public/files/dtdFullSanctionsList/content?token=dG9rZW4tMjAxNw";
	
	public static void main(String args[]){
		System.out.println(SanctionList.isOnSanctionList("john ardelius"));
	}
	
	public static Boolean isOnSanctionList(String fullName){
		
		try {
			URL url = new URL(listURL);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = null;
			//StringBuilder sb = new StringBuilder();
			String line;
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				if(line.contains("LASTNAME") && line.length()>11){ // Exclude <LASTNAME/> tags
					//System.out.println(line);
					String lastName = line.substring(line.indexOf(">")+1, line.indexOf("<", line.indexOf("<") + 1));
					String firstName = "";
					line = br.readLine();
					//System.out.println(line);
					if(line!=null && line.length()>13){
						firstName = line.substring(line.indexOf(">")+1, line.indexOf("<", line.indexOf("<") + 1));
						String name = (firstName.toLowerCase() + " " + lastName.toLowerCase());
						if(name.equals(fullName.toLowerCase())){
							return true;
						}
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
