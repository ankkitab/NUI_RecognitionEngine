package myDollar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class pDollar {
	
	PointCloud pclouds;
	String eventFiles;
	
	public pDollar	(String eFile)
	{
		this.eventFiles=eFile; //Initialize event file
		pclouds=null;
		
	}
	
	public void callOrigRecognizer()
	{
		PointCloudLibrary pcLib = PointCloudLibrary.getDemoLibrary();
        PointCloudMatchResult result = pcLib.originalRecognize(pclouds);
        System.out.println(result.getName());
	}
	
	public void cloudPointAccepter()
	{
		try
		{
			FileReader fr= new FileReader(eventFiles);
			BufferedReader br= new BufferedReader(fr);
			String stream=br.readLine();
			
			int i=1;
			while(br!=null && stream != null) 
			{
                ArrayList<PointCloudPoint> pcList = new ArrayList<PointCloudPoint>();
                while (stream.equals("RECOGNIZE") == false) 
                {

                    if (stream.equals("MOUSEDOWN"))
                    {
                        stream = br.readLine();
                        continue;
                    }
                    if (stream.equals("MOUSEUP")) 
                    {
                        stream = br.readLine();
                        i++;
                        continue;
                    }
                    String[] stg = stream.split(",");
                    
                    PointCloudPoint pCPoint = new PointCloudPoint(Double.parseDouble(stg[0]), Double.parseDouble(stg[1]), i);
                    pcList.add(pCPoint);
                    stream = br.readLine();
                }
			
                pclouds = new PointCloud("Point Cloud Gesture", pcList);
                this.callOrigRecognizer();
                stream=br.readLine();
			}	
			br.close();
		}
		catch (IOException e)
		{
			System.out.println("File not found!!");
		}
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String dir = System.getProperty("user.dir");
		System.out.println(dir);
		if (args[0]!=null)
		{
			if (args[0].equals("-t")) //gesture Template input
			{
				String gestureFile= args[1];
				try{
					File oldFile = new File(dir+"\\"+gestureFile);
	
					if (oldFile.renameTo(new File(dir+"\\gestureFiles\\"+ oldFile.getName()))) 
					{
					      System.out.println("The file was moved successfully to the new folder");
				    }
				    else 
				    {
				          System.out.println("The File was not moved.");
				    }
				   } 
				catch (Exception e) 
				   {
				            e.printStackTrace();
				    }
			 }
				


			else if (args[0].equals("-r")) //delete all Templates
			{
				 String[] allFiles;
				 File file = new File(dir+"\\gestureFiles\\");
	               
	                if(file.isDirectory())
	                {
						allFiles = file.list();
						for (int i=0; i<allFiles.length; i++)
						{
							File myFile = new File(file, allFiles[i]);
							myFile.delete();
						}
						System.out.println("All files deleted!!");
					}
	                else
	                {
	                	System.out.println("Directory doesn't exist!!");
	                }
			}
			else //eventstream input here
			{
				String eventFiles=dir+"\\eventfiles\\"+args[0];
                pDollar pD = new pDollar(eventFiles);
                pD.cloudPointAccepter();
			}
		}
		
		else
			System.out.println("Please input the arguments!!");
	}

}
