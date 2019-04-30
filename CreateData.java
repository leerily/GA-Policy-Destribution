package test.LB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import org.cloudbus.cloudsim.Log;

public class CreateData {
	public static void main(String[] args) {
		createVMTxt();
		createCloudletTxt();
		Log.printLine("Excute sucessfuly! see the txt in " + Args.filePath);
		Log.printLine("ready to read the txt...");
		
		Log.printLine("reading the vm.txt...");
		try {
			BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(Args.filePath + "vm.txt")));
			String tempData;
			Log.printLine("id" + "\t" + "mips" + "\t" + "numOfPe" + "\t" + "ram"
					+ "\t" + "bw" + "\t" + "size");
			while((tempData = br.readLine()) != null) {
				Log.printLine(tempData);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		Log.printLine("----------------------------------------\n\n\n");
		
		Log.printLine("reading the cloudlet.txt");
		try {
			BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(Args.filePath + "cloudlet.txt")));
			String tempData;
			Log.printLine("id" + "\t" + "cloudletLength" + "\t" + "cloudletFileSize" + "\t"
					 +  "cloudletOutputSize");
			while((tempData = br.readLine()) != null) {
				Log.printLine(tempData);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void createVMTxt(){
		int id=0;
		double mips;
		int numOfPe = 1;
		int ram;
		long bw;
		long size;
		StringBuilder sb=new StringBuilder();
		for(int i=0; i<Args.VmNum; i++) {
			mips = Args.maxMips_Vm - (new Random().nextInt(Args.rangeMips_Vm));
			//numOfPe = Args.maxNumOfPe_Vm - (new Random().nextInt(Args.rangeNumOfPe_Vm));
			ram = Args.maxRam_Vm - (new Random().nextInt(Args.rangeRam_Vm));
			bw = Args.maxBw_Vm - (new Random().nextInt(Args.rangeBw_Vm));
			size = Args.maxSize_Vm - (new Random().nextInt(Args.rangeSize_Vm));
			
			sb.append(id + "\t" + mips + "\t" + numOfPe + "\t" + ram
					+ "\t" + bw + "\t" + size + "\t" + "\n");
			id++;
		}
		
		writeTxt(Args.filePath + "vm.txt", sb.toString());
	}
	
	public static void createCloudletTxt() {
		int id = 0;
		long cloudletLength;
		long cloudletFileSize;
		long cloudletOutputSize;
		StringBuilder sb=new StringBuilder();
		
		for(int i=0; i<Args.taskNum; i++) {
			cloudletLength = Args.maxCloudletLength - (new Random().nextInt(Args.rangeCloudletLength));	
			cloudletFileSize = Args.maxCloudletFileSize - (new Random().nextInt(Args.rangeCloudletFileSize));
			cloudletOutputSize = Args.maxCloudletOutputSize - (new Random().nextInt(Args.rangeCloudletOutputSize));	
			
			sb.append(id + "\t" + cloudletLength + "\t" + cloudletFileSize + "\t"
			 +  cloudletOutputSize + "\t" + "\n");
			id++;
		}
		writeTxt(Args.filePath + "cloudlet.txt", sb.toString());
	}
	
	private static void writeTxt(String filePath, String conent)
	{
		try {
			File file = new File(filePath);
			if(file.exists()) {
				file.delete();
				Log.printLine(filePath + " exists, which has been deleted!");
			}
			Log.printLine(filePath + " doesn't exist, file will be created!");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		BufferedWriter out = null;
		try
		{	
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath , true)));
			out.write(conent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
