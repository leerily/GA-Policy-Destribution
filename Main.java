package test.LB;

import org.cloudbus.cloudsim.Cloudlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Main
{
	private static List<Cloudlet> cloudletList = new ArrayList<Cloudlet>(); 
	private static List<Vm> vmlist= new ArrayList<Vm>();
	
	private static List<String[]> cloudletArgs = new ArrayList();
	private static List<String[]> vmArgs = new ArrayList();
	
	private static int brokerId = -1;
	private static int userId = 0;
	private static List<Cloudlet> printList;//the flag of printint or not.
	
	
	private static int iterationNum = Args.iterationNum;
	
	public static boolean isGAscheduleApplied = Args.isGAscheduleApplied;
	
	public static void main(String[] args)
	{
		String cloudletFilePath="data\\cloudlet.txt";
		String vmFilePath = "data\\vm.txt";
		
		createVm(vmFilePath);
		createTasks(cloudletFilePath);
		
		if( isGAscheduleApplied )
		{
			runSimulation_GA();
		}
		else
		{
			List<Cloudlet> list = runSim((int[])null);
			printCloudletList(list);
		}
	}


	private static void runSimulation_GA() {
		/*ArrayList<int[]> iniPop = Classify.applyClassifyScheduling(cloudletList, vmlist);*/
		ArrayList<int[]> pop=initPopsRandomly(Args.taskNum, Args.VmNum, Args.popSize);
		Log.printLine("the initial size of population is:" + pop.size());
		pop = (ArrayList<int[]>)applyGAScheduling(pop);
		int[] bestSchedule = findBestSchedule(pop);
		printList = runSim(bestSchedule);
		printCloudletList(printList);
		Log.printLine("GA finished........");
	}
	
	private static ArrayList<int[]> initPopsRandomly(int taskNum,int vmNum,int popsize)
	{
		ArrayList<int[]> schedules=new ArrayList<int[]>();
		for(int i=0;i<popsize;i++)
		{
			//data structure for saving a schedule：array,index of array are cloudlet id,content of array are vm id.
			int[] schedule=new int[taskNum];
			for(int j=0;j<taskNum;j++)
			{
				schedule[j]=new Random().nextInt(vmNum);
			}
			schedules.add(schedule);
		}
		return schedules;
	}

	
	
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try
		{
			broker = new DatacenterBroker("Broker");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return broker;
	}


	private static Datacenter createDatacenter(String name) {
		//TODO: the datacenter
		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		double mips = Args.maxMips_Vm * Args.VmNum;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = Args.VmNum * Args.maxRam_Vm; // host memory (MB)
		long storage = Args.VmNum * Args.maxSize_Vm; // host storage
		long bw = Args.VmNum * Args.maxBw_Vm;

		hostList.add(
			new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
			)
		); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}


	//create Vms list
	@SuppressWarnings("unused")
	private static void createVm(String vmFilePath) {
		int vmNumber = 0;
		vmlist = new ArrayList<Vm>();
		if(vmArgs.size() == 0 ) {
			try {
				ArrayList<String> list = new ArrayList();
				BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(vmFilePath)));
				String tempData;
				String[] tempVMArray;
				while((tempData = br.readLine()) != null) {
					//tempVMArray, String int[6], represent id, mips, numOfPes, ram, bw, size
					tempVMArray = tempData.split("\t");
				vmArgs.add(tempVMArray);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		String vmm = "testGA";
		for(int i=0; i<vmArgs.size(); i++){
/*				Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			public Vm(
					int id,
					int userId,
					double mips,
					int numberOfPes,
					int ram,
					long bw,
					long size,
					String vmm,
					CloudletScheduler cloudletScheduler)*/
			Log.printLine("--------------creating the vm...--------------\n" + "the vm args:" + Arrays.toString(vmArgs.get(i)));
			Vm tempVm= new Vm( 
					Integer.parseInt(vmArgs.get(i)[0]),	//int id
					brokerId, //int userId
					Double.parseDouble(vmArgs.get(i)[1]),//double mips
					Integer.parseInt(vmArgs.get(i)[2]), //int numberOfPes
					Integer.parseInt(vmArgs.get(i)[3]), //int ram
					(long)Double.parseDouble(vmArgs.get(i)[4]),//long bw
					(long)Double.parseDouble(vmArgs.get(i)[5]),//long size
					vmm, 
					new CloudletSchedulerSpaceShared());
			if(tempVm == null) {
				Log.printLine("create the vm failed..." );
			}else {
				Log.printLine("create the vm succeccfuly!" );
			}
			Log.printLine("--------------finish creating vms--------------");
			
			vmlist.add(tempVm);
			vmNumber++;
		}
//		Args.taskNum = VmNumber;
		Log.printLine("intialize the vmlist successfully, the number of vm is:" + vmNumber);
	}

	
	//create cloudlets list
	private static void createTasks(String cloudletFilePath) {
		int taskNumber = 0;
		cloudletList = new ArrayList<Cloudlet>();
		Log.printLine("reset the clouletlist:" + (cloudletList.size() == 0));
		Log.printLine("the cache of cloudlet args is" + " " + (cloudletArgs.size()==0));
		if(cloudletArgs.size() == 0) {		
			try {
			BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(cloudletFilePath)));
			String tempData;
			String[] tempTaskArray;
			Cloudlet task;
			
			int pesNumber = 1;
			UtilizationModel utilizationModel = new UtilizationModelFull();			
			while((tempData = br.readLine()) != null) {
				//tempTaskArray, length is 5
				tempTaskArray =  tempData.split("\t");
				cloudletArgs.add(tempTaskArray);
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();	
		for(int i=0; i<cloudletArgs.size(); i++) {
			/*				public Cloudlet(
			final int cloudletId,
			final long cloudletLength,
			final int pesNumber,
			final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw)*/
			Cloudlet task=new Cloudlet(
			Integer.parseInt(cloudletArgs.get(i)[0]), 	//int cloudletId
			(long) Double.parseDouble(cloudletArgs.get(i)[1]),	//long cloudletLength
			pesNumber,	//pesNumber, we make all the task one CPU.
			(long) Double.parseDouble(cloudletArgs.get(i)[2]),	//long cloudletFileSize
			(long) Double.parseDouble(cloudletArgs.get(i)[3]),	//long cloudletOutputSize
			utilizationModel, 
			utilizationModel,
			utilizationModel);
			task.setUserId(brokerId);
			taskNumber++;
			cloudletList.add(task);	
		}
		
//		Args.taskNum = taskNumber;
		Log.printLine("intialize the cloudletlist successfully, the number of cloudlet is:" + taskNumber);
	}

	
	public static ArrayList<int[]> applyGAScheduling(ArrayList<int[]> pop) {
		HashMap<Integer,double[]> segmentForEach=calcSelectionProbs(pop);
		ArrayList<int[]> children=new ArrayList<int[]>();
		ArrayList<int[]> tempParents=new ArrayList<int[]>();
		
		{
			Log.printLine("the segment rate of pop is:");
			Log.printLine("pop-index"+ "\t" + "beginRate" + "\t" +"endRate");
			for(int i=0; i<segmentForEach.size(); i++) {
				Log.printLine(i + "\t" + segmentForEach.get(i)[0] + "\t" + segmentForEach.get(i)[1]);
			}
		}
		
		while(children.size()<pop.size())
		{	
			//selection phase:select two parents each time.
			for(int i=0;i<2;i++)
			{
				double prob = new Random().nextDouble();
				for (int j = 0; j < pop.size(); j++)
				{
					if (isBetween(prob, segmentForEach.get(j)))
					{
						tempParents.add(pop.get(j));
						break;
					}
				}
			}
			//cross-over phase.
			int[] p1,p2,p1temp,p2temp;
			p1= tempParents.get(tempParents.size() - 2).clone();
			p1temp= tempParents.get(tempParents.size() - 2).clone();
			p2 = tempParents.get(tempParents.size() -1).clone();
			p2temp = tempParents.get(tempParents.size() -1).clone();
			
			Log.printLine("in crossover:\n" + "p1:" + Arrays.toString(p1) + "\np2:"
					+ Arrays.toString(p2));
			
			if(new Random().nextDouble()<Args.crossoverFactor)
			{
				int crossPosition = new Random().nextInt(Args.taskNum - 1);
				//cross-over operation
				for (int i = crossPosition + 1; i < Args.taskNum; i++)
				{
					int temp = p1temp[i];
					p1temp[i] = p2temp[i];
					p2temp[i] = temp;
				}
			}
			//choose the children if they are better,else keep parents in next iteration.
			children.add(getFitness(p1temp) < getFitness(p1) ? p1temp : p1);
			children.add(getFitness(p2temp) < getFitness(p2) ? p2temp : p2);	
			
			

			// mutation phase.
			if (new Random().nextDouble() < Args.mutationFactor)
			{
				// mutation operations bellow.
				int maxIndex = children.size() - 1;

				for (int i = maxIndex - 1; i <= maxIndex; i++)
				{
					operateMutation(children.get(i));
				}
			}
		}
		
		iterationNum--;
		return iterationNum > 0 ? applyGAScheduling(children): children;
	}
	

	public static void operateMutation(int []child)
	{
		int mutationIndex = new Random().nextInt(Args.taskNum);
		int newVmId = new Random().nextInt(Args.taskNum);
		while (child[mutationIndex] == newVmId)
		{
			newVmId = new Random().nextInt(Args.VmNum);
		}

		child[mutationIndex] = newVmId;
	}
	
	private static boolean isBetween(double prob,double[]segment)
	{
		if(segment[0]<=prob&&prob<=segment[1])
			return true;
		return false;	
	}
	
	private static HashMap<Integer,double[]> calcSelectionProbs(ArrayList<int[]> parents)
	{
		printPop(parents);
		int size=parents.size();
		double totalFitness=0;	
		ArrayList<Double> fits=new ArrayList<Double>();
		HashMap<Integer,Double> probs=new HashMap<Integer,Double>();
		
		for(int i=0;i<size;i++)
		{
			double fitness=getFitness(parents.get(i));
			fits.add(fitness);
			totalFitness+=fitness;
		}
		
		Log.printLine("the total fitness is:" + totalFitness);
		for(int i=0;i<size;i++)
		{
			probs.put(i,fits.get(i)/totalFitness );
		}
		
		return getSegments(probs);
	}
	
	private static HashMap<Integer,double[]> getSegments(HashMap<Integer,Double> probs)
	{
		HashMap<Integer,double[]> probSegments=new HashMap<Integer,double[]>();
		//probSegments保存每个个体的选择概率的起点、终点，以便选择作为交配元素。
		int size=probs.size();
		double start=0;
		double end=0;
		for(int i=0;i<size;i++)
		{
			end=start+probs.get(i);
			double[]segment=new double[2];
			segment[0]=start;
			segment[1]=end;
			probSegments.put(i, segment);
			start=end;
		}
		
		return probSegments;
	}

	private static double getFitness(int[] schedule)
	{
		double fitness=0;
/*		//key is the vm, and the value is the task list
		HashMap<Integer,ArrayList<Integer>> vmTasks=new HashMap<Integer,ArrayList<Integer>>();	 
		int size=Args.taskNum;
		
		//initialize the vmTasks
		for(int i=0;i<size;i++)
		{
			if(!vmTasks.keySet().contains(schedule[i]))
			{
				ArrayList<Integer> taskList=new ArrayList<Integer>();
				taskList.add(i);
				vmTasks.put(schedule[i],taskList);
			}
			else
			{
				vmTasks.get(schedule[i]).add(i);
			}
		}*/
		
		
		double simTime = 0;	//@Param the time of simulation.
		List<Cloudlet> list  = runSim(schedule);
		for(int i=0; i<list.size(); i++) {
			simTime+=list.get(i).getActualCPUTime();
		}
		fitness = simTime;
		Log.printLine("**********\n"+ "get the fitness..." + "\t status" + (fitness==0)
				+ "the fitness is:" +fitness);
		
		
/*		for(Entry<Integer, ArrayList<Integer>> vmtask:vmTasks.entrySet())
		{
			int length=0;
			for(Integer taskid:vmtask.getValue())
			{
				length+=getCloudletById(taskid).getCloudletLength();
			}
			
			double runtime=length/getVmById(vmtask.getKey()).getMips();
			if (fitness<runtime)
			{
				fitness=runtime;
			}
		}*/
		
		return fitness;
	}
	
	public static void assignResourcesWithSchedule(int []schedule, DatacenterBroker broker)
	{
		for(int i=0;i<schedule.length;i++)
		{
			broker.bindCloudletToVm(i,schedule[i]);
//			getCloudletById(i).setVmId(schedule[i]);
		}
	}
	
	public static Cloudlet getCloudletById(int id)
	{
		for(Cloudlet c:cloudletList)
		{
			if(c.getCloudletId()==id)
				return c;
		}
		return null;
	}
	
	public static List<Cloudlet> runSim(int[] schedule) {
		List<Cloudlet> newList = new ArrayList();
		try
		{
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
		
			CloudSim.init(num_user, calendar, trace_flag);

			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			// #3 step: Create Broker
			DatacenterBroker broker = createBroker();
			brokerId = broker.getId();
			
			setVmCloudletUserId(brokerId);
			// submit vm list to the broker
			vmlist=null;
			cloudletList = null;
			createVm(Args.filePath + "vm.txt");
			createTasks(Args.filePath + "cloudlet.txt");
			broker.submitVmList(vmlist);
			//create cloudlets and submit them.
			broker.submitCloudletList(cloudletList);
			
			if( isGAscheduleApplied ) {//run the cloudsim by GA or not.
				assignResourcesWithSchedule(schedule, broker);
			}
			
			CloudSim.startSimulation();
			CloudSim.stopSimulation();
			newList = broker.getCloudletReceivedList();
			{
				double totaltime=0;
				for(int i=0; i<newList.size(); i++) {
					totaltime += newList.get(i).getActualCPUTime();
				}
				Log.printLine("the total simulation time is:" + totaltime);
			}
		}catch(Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
		return newList;
	}
	
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
						indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}
		double totalTime = 0;
		for(int i=0; i<list.size(); i++) {
			totalTime += list.get(i).getActualCPUTime();
		}
		Log.printLine("the total Time in this schedule is:" + totalTime
				);
		Log.printLine("========== FINISH ==========");

	}
	
	private static void setVmCloudletUserId(int brokerId2) {
		for(int i = 0; i<vmlist.size(); i++) {
			vmlist.get(i).setId(brokerId2);
		}
		for(int i = 0; i<cloudletList.size(); i++) {
			cloudletList.get(i).setUserId(brokerId2);
		}
		
	}


	private static int[] findBestSchedule(ArrayList<int[]> pop)
	{
		double bestFitness=1000000000;
		int bestIndex=0;
		for(int i=0;i<pop.size();i++)
		{
			int []schedule=pop.get(i);
			double fitness=getFitness(schedule);
			if(bestFitness>fitness)
			{
				bestFitness=fitness;
				bestIndex=i;
			}
		}
		return pop.get(bestIndex);
	}

	private static void printPop(ArrayList<int[]> pop) {
		Log.printLine("the number of pop is" + pop.size());
		for(int i=0;i<pop.size();i++) {
			Log.printLine(Arrays.toString(pop.get(i)));
		}
	}
}
