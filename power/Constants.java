
package org.cloudbus.cloudsim.examples.power;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIntelXeonL5430;

/*
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 *
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 *
 * @author Anton Beloglazov
 * @since Jan 6, 2012
 */

public class Constants {

	public final static boolean ENABLE_OUTPUT = true;
	public final static boolean OUTPUT_CSV    = false;

	public final static double SCHEDULING_INTERVAL = 450;
	//how long the simulation works
	public final static double SIMULATION_LIMIT = 24 * 60 * 60;

	public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
	public final static int CLOUDLET_PES	= 1;

	/*
	 * VM instance types:
	 *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
	 *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
	 *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB

	 *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
	 *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
	 *   We decrease the memory size two times to enable oversubscription
	 *
	 */

public final static int VM_TYPES	= 4; //4 types to correspond to the 4 standard instances of m1.small, m1.large, and m1.xlarge per the ec2instances table
	public final static int[] VM_MIPS	= { 1026, 2052,2052,2052  };
	public final static int[] VM_PES	= { 1, 1, 2, 4};
	public final static int[] VM_RAM	= { 1740, 3840, 7680, 15360};
	public final static int[] VM_BW		= {250000, 250000, 500000, 1000000}; // 250 Mbit/s, its in kb/s
	public final static int VM_SIZE		= 160000; // 160 GB

	/*
	 * Host types:
	 *   Intel Xeon L5430 (1 x [Xeon L5430 2666 MHz, 4 cores], 17GB)
	 */
	
public final static int HOST_TYPES	 = 1; //modelling the 20-computer cluster at the Amazon N. VA center where they are all mostly using Intel Xeon E5430 Processors
	public final static int[] HOST_MIPS	 = { 2565 };  //using the figure of MIPS for the m1.small instance from the reference "Testing the New Linux Benchmark Suite" 
	//with the figure from "A Measurement Study of Server Utilization in Public Clouds" stating that the m1.small instance has a virtual core equivalent to roughly
	//40% single physical core's capacity, where for all 4 core's this would be 1/.4*4=10 times a m1.small virtuall cores capacity
	//where a m1.small instance has one core and 1 EC2 compute unit (ECU)
	public final static int[] HOST_PES	 = { 4 };
	public final static int[] HOST_RAM	 = { 17408 }; //where 1gb corresponds to 1024MB, and the memory per EC2 unit is 1.7 GB per the reference I/O Performance of Virtualized Cloud Environments
	public final static int HOST_BW		 = 2500; // 2.5 Gbit/s
	public final static int HOST_STORAGE = 16000000; // 1.6 TB

	public final static PowerModel[] HOST_POWER = {
		new PowerModelSpecPowerIntelXeonL5430()	
	};

}

/*package org.cloudbus.cloudsim.examples.power;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
public class Constants {

	public final static boolean ENABLE_OUTPUT = true;
	public final static boolean OUTPUT_CSV    = false;

	public final static double SCHEDULING_INTERVAL = 300;
	public final static double SIMULATION_LIMIT = 24 * 60 * 60;

	public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
	public final static int CLOUDLET_PES	= 1;

	/*
	 * VM instance types:
	 *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
	 *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
	 *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB
	 *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
	 *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
	 *   We decrease the memory size two times to enable oversubscription
	 *
	 */
/*	public final static int VM_TYPES	= 4;
	public final static int[] VM_MIPS	= { 2500, 2000, 1000, 500 };
	public final static int[] VM_PES	= { 1, 1, 1, 1 };
	public final static int[] VM_RAM	= { 870,  1740, 1740, 613 };
	public final static int VM_BW		= 100000; // 100 Mbit/s
	public final static int VM_SIZE		= 2500; // 2.5 GB
*/
	/*
	 * Host types:
	 *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
	 *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
	 *   We increase the memory size to enable over-subscription (x4)
	 */
/*	public final static int HOST_TYPES	 = 2;
	public final static int[] HOST_MIPS	 = { 1860, 2660 };
	public final static int[] HOST_PES	 = { 2, 2 };
	public final static int[] HOST_RAM	 = { 4096, 4096 };
	public final static int HOST_BW		 = 1000000; // 1 Gbit/s
	public final static int HOST_STORAGE = 1000000; // 1 GB

	public final static PowerModel[] HOST_POWER = {
		new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
		new PowerModelSpecPowerHpProLiantMl110G5Xeon3075()
	};

}*/