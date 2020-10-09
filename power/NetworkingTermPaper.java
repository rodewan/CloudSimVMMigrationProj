package org.cloudbus.cloudsim.examples.power;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
//we need the space-shared cloudlet scheduler
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationLocalRegression;
import org.cloudbus.cloudsim.power.TermPaperVmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumMigrationTime;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;



/**
 * An example of a power aware data center. In this example the placement of VMs
 * is continuously adapted using VM migration in order to minimize the number
 * of physical nodes in use.
 * The CPU utilization of each host is kept over the specified under-utilization threshold.
 */
public class NetworkingTermPaper {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	public static long length = 824904; // the cloudlet length - 402s/99s on 2052 MIPS, 804s/198s on 1026 MIPS, targeting
	//for 13.97% and 3.44% overall, which match the peak daily highs and lows for average use % of CPU utilization usage
	//per the graph digitized in the term paper, according to the paper "A Measurement Study of Server Utilization in Public Clouds" by Huan Liu

	/** The vm list. */
	private static List<PowerVm> vmList;

	private static double hostsNumber = 20;
	private static double vmsNumber = 20;
	private static double cloudletsNumber = 20;       

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
                 
		Log.printLine("Starting the running of the Networking Term Paper Project with "+length+ " MIPS, ");

		try {
			// First step: Initialize the CloudSim package. 
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace GridSim events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
 		    PowerDatacenter datacenter = createDatacenter(
					"DatacenterTermPaper",
					PowerDatacenter.class);
										
			// Third step: Create Broker
			PowerDatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create twenty virtual machines
			vmList = createVms(brokerId);

			// We then submit the vm list of twenty machines to the broker
			broker.submitVmList(vmList);

			// Fifth step: Create twenty cloudlets
			cloudletList = createCloudletList(brokerId);

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			double lastClock = CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			printCloudletList(newList);


			Log.printLine();
			Log.printLine(String.format("Total simulation time: %.2f sec", lastClock));
			Log.printLine(String.format("Number of VM migrations: %d", datacenter.getMigrationCount()));
			Log.printLine();

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("An unwanted error has occurred");
		}

		Log.printLine("The run is finished!");
	}

	//this method creates the cloudlet list
	//here we create 20 cloudlets to run on a single core occupying 3.44 or 13.97% of the CPU usage per hour
	private static List<Cloudlet> createCloudletList(int brokerId) {
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		
		int pesNumber =1;
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelStochastic(); 
		for (int i = 0; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	/**
	 * Creates the vms.
	 *
	 * @param brokerId the broker id
	 *
	 * @return the list< vm>
	 */
	private static List<PowerVm> createVms(int brokerId) {
		List<PowerVm> vms = new ArrayList<PowerVm>();

		// VM description
		//4 types to correspond to the 4 standard instances of m1.small, m1.large, and m1.xlarge per the ec2instances table
		//int[] mips = { 1026, 2052, 2052, 2052 }; // MIPSRating per processing element from reference "Testing the New Linux Benchmark Suite with Amazon EC2 Instances" used with the data from the ec2instances table
		//where there is 1 ECU instance in m1.small, 2 ECU instances in m1.medium, 4 ECU instances in m1.large, and 8 ECU instances in m1.xlarge
		int[] mips = { 1026, 2052, 2052, 2052}; // MIPSRating per processing element from reference "Testing the New Linux Benchmark Suite with Amazon EC2 Instances" used with the data from the ec2instances table
		int[] pesNumber = {1,1,2,4}; // number of cpus
		int[] ram = {1740,3480,7680,15360}; // vm memory (MB)
		long[] bw = {250,250,500,1000}; // bandwidth
		
		long size = 160000; // image size (MB)
		String vmm = "Xen"; // VMM name
		
		int priority = 1;
		int schedulingInterval = 5; //in seconds, so every 1 second
		
		for (int i = 0; i < vmsNumber; i++) {
			PowerVm vm = new PowerVm(i, brokerId, mips[i % mips.length], pesNumber[i%pesNumber.length], ram[i%ram.length], bw[i%bw.length], size, priority, vmm,  new CloudletSchedulerSpaceShared(), schedulingInterval);
			//PowerVm vm = new PowerVm(i, brokerId, mips[i % mips.length], pesNumber[i%pesNumber.length], ram[i%ram.length], bw[i%bw.length], size, priority, vmm,  new CloudletSchedulerDynamicWorkload(mips[i % mips.length], pesNumber[i % pesNumber.length]), schedulingInterval);
			//test to make sure types are being varied with appropriate mips and PEs
			//Log.printLine("VM "+i+" "+i%mips.length + " total mips per PE " + vm.getMips() + "total PEs " + vm.getNumberOfPes()+ ".");
			vms.add(vm);
		}

		return vms;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 *
	 * @throws Exception the exception
	 */
	private static PowerDatacenter createDatacenter(String name,
			Class<? extends Datacenter> datacenterClass)
			 throws Exception {
		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create an object of HostList to store
		// our machine
		//class of host that stores its CPU utilization history
			List<PowerHostUtilizationHistory> hostList = new ArrayList<PowerHostUtilizationHistory>();
                //Class<? extends Datacenter> datacenterClass;
		for (int i = 0; i < hostsNumber; i++) {
			//there is only one host type in this example, conforming to the Amazon cluster of 20 VMs
			//as reported in "A Measurement Study of Server Utilization in Public Clouds" by Huan Liu
			//therefore the hostType index is set to 0
			int hostType = Constants.HOST_TYPES-1;

			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
				//int id,
				//RamProvisioner ramProvisioner,
				//BwProvisioner bwProvisioner,
				//long storage,
				//List<? extends Pe> peList,
				//VmScheduler vmScheduler,
				//PowerModel powerModel
				peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
			}

			hostList.add(new PowerHostUtilizationHistory(
					i,
					new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
					new BwProvisionerSimple(Constants.HOST_BW),
					Constants.HOST_STORAGE,
					peList,
					new VmSchedulerTimeShared(peList),
					Constants.HOST_POWER[hostType]));// This is our machine
		}
                
		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a Grid resource: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/PowerPe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = .438; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
            String vmAllocationPolicyName="thr";
			String vmSelectionPolicyName= "mu";
			String parameterName="1.2";
                        VmAllocationPolicy vmAllocationPolicy = null;
		PowerVmSelectionPolicy vmSelectionPolicy = null;
		if (!vmSelectionPolicyName.isEmpty()) {
			vmSelectionPolicy = getVmSelectionPolicy(vmSelectionPolicyName);
		}
		if (!vmAllocationPolicyName.isEmpty()) {
			Log.printLine("empty");
		}
		double parameter = 0;
		if (!parameterName.isEmpty()) {
			parameter = Double.valueOf(parameterName);
		}
		
		 
			//PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
			//		hostList,
			//		vmSelectionPolicy,
			//		0.7);
			vmAllocationPolicy = new TermPaperVmAllocationPolicy(
					hostList,
					vmSelectionPolicy,
					0.99);
		 

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
			PowerDatacenter datacenter = null;
		try {
			datacenter = new PowerDatacenter(
					name,
					characteristics,
					vmAllocationPolicy,
					new LinkedList<Storage>(),
					Constants.SCHEDULING_INTERVAL);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	//returns minimumutilization as our chosen vm selection policy for after we have decided to allocate vm's from
	//an under-utilized host, when we need determine which vm's should be migrated
	protected static PowerVmSelectionPolicy getVmSelectionPolicy(String vmSelectionPolicyName) {
		PowerVmSelectionPolicy vmSelectionPolicy = null;
		
		 if (vmSelectionPolicyName.equals("mu")) {
			vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
		} 
		return vmSelectionPolicy;
	}

	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static PowerDatacenterBroker createBroker() {
		PowerDatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "\t";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Resource ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId());

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + "SUCCESS"
					+ indent + indent + cloudlet.getResourceId()
					+ indent + cloudlet.getVmId()
					+ indent + dft.format(cloudlet.getActualCPUTime())
					+ indent + dft.format(cloudlet.getExecStartTime())
					+ indent + indent + dft.format(cloudlet.getFinishTime())+indent +indent + "Processing Cost $ " + ((cloudlet.getFinishTime()-cloudlet.getExecStartTime())/3600*.35)
				);
			}
		}
	}
        

}


