/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

//Code copied from example 7


package org.cloudbus.cloudsim.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
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

/**
 * An example showing how to pause and resume the simulation,
 * and create simulation entities (a DatacenterBroker in this example)
 * dynamically.
 */
public class MyExample {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	private static List<Vm> createVM(int userId, int vms, int idShift) {
		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		int mips = 100;				//250 for all my expts
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		Vm[] vm = new Vm[vms];

		//for hungarian algorithm
    	Scanner scanner = null;
		try {
			scanner = new Scanner(new File("D://VMs.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int [] new_arr = new int [200];
    	int j = 0;
    	while(scanner.hasNextInt())
    	{
    	     new_arr[j] = scanner.nextInt();
    	     new_arr[j]=new_arr[j]%vms;
//    	     System.out.print(new_arr[j]+"\t");
    	     j++;
    	}
    	scanner.close();


//		int new_arr[]= {1,3,5,7,9,8,6,4,2,0};		//for all my experiments
		for(int i=0;i<vms;i++){
//			vm[i] = new Vm(idShift + i, userId, mips+(new_arr[i%10]*5), pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
			vm[i] = new Vm(idShift + i, userId, mips+(new_arr[i]*5), pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
			list.add(vm[i]);
			System.out.println("VM id: "+vm[i].getId()+" mips "+vm[i].getCurrentRequestedTotalMips());
		}

		return list;
	}


	private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//for hungarian algorithm
    	Scanner scanner = null;
		try {
			scanner = new Scanner(new File("D://CLoudlets.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int [] arr = new int [200];
    	int j = 0;
    	while(scanner.hasNextInt())
    	{
    	     arr[j] = scanner.nextInt();
    	     arr[j]=arr[j]%cloudlets;
 //   	     System.out.print(arr[j]+"\t");
    	     j++;
    	}
    	scanner.close();

		
//		int arr[]= {1,3,6,8,0,2,7,9,4,5};		//for all my experiments
//	int arr[]= {21,8,0,25,4,18,10,14};
		//cloudlet parameters
		long length = 10000;			//40000 for all my expts
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
//			cloudlet[i] = new Cloudlet(idShift + i, length+(arr[i%8]*5000), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet[i] = new Cloudlet(idShift + i, length+(arr[i]*1000), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);

			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
			System.out.println("Cloudlet id: "+cloudlet[i].getCloudletId()+ " total length: "+cloudlet[i].getCloudletTotalLength());
		}

		return list;
	}


	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample7...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			//Third step: Create Broker
			DatacenterBroker broker = createBroker("Broker_0");
			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
			vmlist = createVM(brokerId, 200, 0); //creating 5 vms
			cloudletList = createCloudlet(brokerId, 200, 0); // creating 10 cloudlets
 
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine("CloudSimExample7 finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 20000;		//1000 for all my experiments

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		//Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 100000; //host memory (MB) 16384		5000 for all my experiments
		long storage = 1000000; //host storage
		int bw = 1000000;			//10000 for all my experiments

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); // This is our first machine

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // Second machine
		
//		hostId++;
//
//		hostList.add(
//    			new Host(
//    				hostId,
//    				new RamProvisionerSimple(ram),
//    				new BwProvisionerSimple(bw),
//    				storage,
//    				peList2,
//    				new VmSchedulerTimeShared(peList2)
//    			)
//    		); // Third machine

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(String name){

		DatacenterBroker broker = null;
		try {
			//broker = new DatacenterBroker(name);
			
			//m is no of ants
			//q is no of iterations
			//rho is rate of evaporation
			//alpha is rate of phermone deposition/importance of phermone
			//beta is importance of dist bw cities
			
			
			//broker=new DatacenterBroker(name, m, Q, alpha, beta, gamma, rho);
			broker = new DatacenterBroker(name, 10, 5, 5, 1, 75, 0.5);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		double finishtime=0;
		Cloudlet cloudlet;
		double avgTime=0;
		double stdDev=0;		//standard deviation
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
						avgTime+=(cloudlet.getFinishTime()-cloudlet.getExecStartTime());
						finishtime=cloudlet.getFinishTime();
			}
		}
		avgTime/=size;
		Log.printLine("Average execution time is: "+avgTime);
		
		//for standard deviation
		for(int i=0;i<size;i++) {
			cloudlet=list.get(i);
			stdDev+=Math.pow(avgTime-(cloudlet.getFinishTime()-cloudlet.getExecStartTime()), 2);			
		}
		stdDev/=size;
		stdDev=Math.sqrt(stdDev);
		
		Log.printLine("Output: "+finishtime/size+" seconds/cloudlet");
		Log.printLine("Throughput: "+1/(finishtime/size)+" seconds/cloudlet");
		Log.printLine("Standard deviation is: "+stdDev);

	}
}
