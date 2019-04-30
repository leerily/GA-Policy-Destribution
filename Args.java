package test.LB;

import java.util.HashMap;

//the parameter of GA

public class Args {
	public static boolean isGAscheduleApplied = true;
	
	public static int VmNum = 10;	//the number of vms.
	public static int taskNum = 2000;	//the number of tasks/cloudlets.
	
	
	public static int popSize = 10; //the population size.
	public static float loadBalanceFactor;	//the standard factor of load banlance.
	public static float crossoverFactor = (float) 0.8;	//the crossover chance.
	public static float mutationFactor = (float) 0.01;	//the mutation chance.
	
	public static int minMutationLength = 2;	//the minimum of mutation length.
	public static int maxMutationLength = 5;	//the maximum of mutation length
	
	public static int iterationNum = 2000;	//the number of iteration.
	
	
/*	@SuppressWarnings("unchecked")
	public static HashMap<String, Float > wTask = new HashMap() {	//the weights of task/cloudlet
		{
			wTask.put("fileSize", (float) 0.25);
			wTask.put("fileExcuteSize", (float) 0.5);
			wTask.put("fileOutputSize", (float) 0.25);
		}
	};
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, Float > wVm = new HashMap() {	//the weights of vm
		{
			wVm.put("mips", (float) 0.2);
			wVm.put("numOfPes", (float) 0.2);
			wVm.put("ram", (float) 0.2);			
			wVm.put("bw", (float) 0.2);
			wVm.put("size", (float) 0.2);
		}
	};
	
	public static HashMap<String, Float > wHost;	//the weights of host
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, Float > wFitness = new HashMap(){	//the weight of fitness
		{
			wFitness.put("time", (float)0.5);
			wFitness.put("urgency", (float)0);
			wFitness.put("banlance", (float)0.5);
		}
	};*/
	
	
	/*the parameters in outputing vm*/
	public static double maxMips_Vm = 1000;
	public static int rangeMips_Vm = 500;
	
	public static int maxNumOfPe_Vm = 1;
	public static int rangeNumOfPe_Vm = 0;
	
	public static int maxRam_Vm = 10000;
	public static int rangeRam_Vm = 5000;
	
	public static long maxBw_Vm = 2048;
	public static int rangeBw_Vm = 1024;
	
	public static long maxSize_Vm = 10000;
	public static int rangeSize_Vm = 5000;
	
	/*the parameters in outputing cloudlet*/
	public static long maxCloudletLength = 50000;
	public static int rangeCloudletLength = 25000;
	
	public static long maxCloudletFileSize = 300;
	public static int rangeCloudletFileSize = 100;
	
	public static long maxCloudletOutputSize = 300;
	public static int rangeCloudletOutputSize = 100;
	
	public static String filePath = "data\\";
	

	
}
