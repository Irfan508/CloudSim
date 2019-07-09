/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import org.apache.commons.math3.stat.clustering.Cluster;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

//import com.sun.corba.se.pept.broker.Broker;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, submission of cloudlets to VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

	/** The list of VMs submitted to be managed by the broker. */
	protected List<? extends Vm> vmList;

	/** The list of VMs created by the broker. */
	protected List<? extends Vm> vmsCreatedList;

	/** The list of cloudlet submitted to the broker. 
         * @see #submitCloudletList(java.util.List) 
         */
	protected List<? extends Cloudlet> cloudletList;

	/** The list of submitted cloudlets. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The list of received cloudlet. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The number of submitted cloudlets. */
	protected int cloudletsSubmitted;

	/** The number of requests to create VM. */
	protected int vmsRequested;

	/** The number of acknowledges (ACKs) sent in response to
         * VM creation requests. */
	protected int vmsAcks;

	/** The number of destroyed VMs. */
	protected int vmsDestroyed;

	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

	/** The list of datacenters where was requested to place VMs. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map, where each key is a VM id
         * and each value is the datacenter id whwere the VM is placed. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics map where each key
         * is a datacenter id and each value is its characteristics.. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	protected double Q;
	protected double alpha;
	protected double beta;
	protected double gamma;
	protected double rho;
	protected int m;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by {@link SimEntity} class)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}
	
	public DatacenterBroker(String name, int m, double Q, double alpha, double beta, double gamma, double rho) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		this.m = m;
		this.Q = Q;
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.rho = rho;
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
         * 
         * @todo The name of the method is confused with the {@link #submitCloudlets()},
         * that in fact submit cloudlets to VMs. The term "submit" is being used
         * ambiguously. The method {@link #submitCloudlets()} would be named "sendCloudletsToVMs"
         * 
         * The method {@link #submitVmList(java.util.List)} may have
         * be checked too.
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a Datacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloud Resource List received with ",
				getDatacenterIdsList().size(), " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
					" has been created in Datacenter #", datacenterId, ", Host #",
					VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
					" failed in Datacenter #", datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ", cloudlet.getCloudletId(),
				" received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
         * @todo to ensure the method will be overridden, it should be defined 
         * as abstract in a super class from where new brokers have to be extended.
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): ", "Error - an event is null.");
			return;
		}

		Log.printConcatLine(getName(), ".processOtherEvent(): Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the submitted virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen Datacenter
	 * @pre $none
	 * @post $none
         * @see #submitVmList(java.util.List) 
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
         * @see #submitCloudletList(java.util.List) 
	 */
	protected void submitCloudlets() {		//cloudlets are mapped to the VMs in RR fashion
//		submitCloudletsACO();
		
//		System.out.println("No of VMs: "+vmsCreatedList.size()+" No of cloudlets: "+cloudletList.size());
//		if(vmsCreatedList.size()==cloudletList.size()) {
//			System.out.println("***************No of cloudlets and vm are same.. SO running hungarian algorithm in submitCloudlets()");
//			submitCloudletsHungarianAlgo();		//it will bind the cloudlets with VMs using bindCloudlettoVm()
//		}
		
//		submitCloudletsKhalidCluster();
		
//		submitCloudletsKhalidRandomCluster();
		
//		submitCloudletsKhalidRandom();

//		submitCloudletsKhalidRL();
	
//		submitCloudletsKhalidRandomRL();
		
//		submitCloudletsKhalidFCFS();
		
//		submitCloudletsKhalidFCFSRandomCluster();
		
		submitCloudletsSaturation(4);
		
//		System.out.println("*********************Inside our function******************************************************************");
//		submitCloudletsMinMin();
		
//		submitCloudletsMaxMin();

		int vmIndex = 0;
			List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
			for (Cloudlet cloudlet : getCloudletList()) {
				Vm vm;
				// if user didn't bind this cloudlet and it has not been executed yet
				if (cloudlet.getVmId() == -1) {
					vm = getVmsCreatedList().get(vmIndex);
				} else { // submit to the specific vm
					vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
					if (vm == null) { // vm was not created
						if(!Log.isDisabled()) {				    
						    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
								cloudlet.getCloudletId(), ": bound VM not available");
						}
						continue;
					}
				}

				if (!Log.isDisabled()) {
				    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
						cloudlet.getCloudletId(), " to VM #", vm.getId());
				}
				
				cloudlet.setVmId(vm.getId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
				cloudletsSubmitted++;
				vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
				getCloudletSubmittedList().add(cloudlet);
				successfullySubmitted.add(cloudlet);
			}

			// remove submitted cloudlets from waiting list
			getCloudletList().removeAll(successfullySubmitted);			
	}
	
	protected void submitCloudletsSaturation(int vmId) {
		int i;
		Random rand=new Random();
		
		for(i=0;i<1000;i++) {		//bind first 1000 cloudlets with vmId and rest in random fashion
			bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmId);
		}
		for(i=1000;i<cloudletList.size();i++) {
			bindCloudletToVm(cloudletList.get(i).getCloudletId(), rand.nextInt(vmList.size()));
		}
		
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<cloudletList.size();i++) {
				JobCount[cloudletList.get(i).getVmId()]++;
				if(i==(cloudletList.size()-1))
					continue;
				else {
					if(cloudletList.get(i).getVmId()!=cloudletList.get(i+1).getVmId()) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);	
	}
	
	protected void submitCloudletsKhalidFCFS() {
		int i,j;
		int limit=cloudletList.size()/vmList.size();		//number of cloudlets on each VM		
		
		for(i=0;i<vmList.size();i++) {
			for(j=0;j<limit;j++) {
				bindCloudletToVm(cloudletList.get((i*limit)+j).getCloudletId(), i);
			}
		}		
		
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int clusterCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<cloudletList.size();i++) {
				JobCount[cloudletList.get(i).getVmId()]++;
				if(i==(cloudletList.size()-1))
					continue;
				else {
					if(cloudletList.get(i).getVmId()!=cloudletList.get(i+1).getVmId()) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]+" and clusters on Fog Device "+ i +" = "+clusterCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);	
	}
	
	protected void submitCloudletsKhalidFCFSRandomCluster() {
		Random rand=new Random();
		int total=0;
		int i=0,j=0;
		int clusterSize[]=new int[200];
		int noOfClusters=0;
		long lengthOfCluster[]=new long[200];
		int result[]=new int[200];
		int index, value;
		int clusterRange[]=new int[200];
		
		
		while(total!=5000) {
			noOfClusters++;
			int val=rand.nextInt(100);
			val++;		//to avoid zero size cluster
			System.out.println("random number generated = "+val+" and total = "+total);
			if((total+val)<5000) {
				total+=val;
				clusterSize[i]=val;
			}
			else {
				clusterSize[i]=5000-total;
				total=5000;
			}
			i++;
		}
		noOfClusters=i;
		System.out.println("Number of clusters is: "+noOfClusters);
		
		int sum=0;
		clusterRange[0]=0;
		for(i=0;i<noOfClusters;i++) {
			lengthOfCluster[i]=clusterSize[i]*10000;
			sum=sum+clusterSize[i];
			clusterRange[(i+1)]=sum;
		}
		
		int clusterCount[]=new int[vmList.size()];
		int vmId=0;
		for(i=0;i<noOfClusters;i++) {
			for(j=clusterRange[i];j<clusterRange[i+1];j++) {
				bindCloudletToVm(cloudletList.get(j).getCloudletId(), vmId);
			}
			clusterCount[vmId]++;
			vmId=(vmId+1)%vmList.size();
		}		
	
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<cloudletList.size();i++) {
				JobCount[cloudletList.get(i).getVmId()]++;
				if(i==(cloudletList.size()-1))
					continue;
				else {
					if(cloudletList.get(i).getVmId()!=cloudletList.get(i+1).getVmId()) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]+" and clusters on Fog Device "+ i +" = "+clusterCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);					
		
	}
	
	protected void submitCloudletsKhalidRandom() {
		int i;
		int noOfVMs=vmList.size();
		Random rand=new Random();
		
		for(i=0;i<cloudletList.size();i++) {
			bindCloudletToVm(cloudletList.get(i).getCloudletId(), rand.nextInt(noOfVMs));
		}		
		
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int clusterCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<cloudletList.size();i++) {
				JobCount[cloudletList.get(i).getVmId()]++;
				if(i==(cloudletList.size()-1))
					continue;
				else {
					if(cloudletList.get(i).getVmId()!=cloudletList.get(i+1).getVmId()) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]+" and clusters on Fog Device "+ i +" = "+clusterCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);				
		
	}
	
	protected int findMinimum(double times[]) {
		int i,index=-1;
		double min=999999;
		
		for(i=0;i<vmList.size();i++) {
			if(min>times[i]) {
				min=times[i];
				index=i;
			}
		}
		return index;
	}
	
	protected void submitCloudletsKhalidRL() {
		double times[]=new double[vmList.size()];
		times[0]=64.52;
		times[1]=48.7;
		times[2]=21.87;
		times[3]=40.42;
		times[4]=90.94;
		int i,j,min;
		
		for(i=0;i<cloudletList.size();i++) {
			min=findMinimum(times);
			bindCloudletToVm(cloudletList.get(i).getCloudletId(), min);
			times[min]+=cloudletList.get(i).getCloudletLength()/vmList.get(min).getMips();
		}
		
		
		
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int clusterCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<cloudletList.size();i++) {
				JobCount[cloudletList.get(i).getVmId()]++;
				if(i==(cloudletList.size()-1))
					continue;
				else {
					if(cloudletList.get(i).getVmId()!=cloudletList.get(i+1).getVmId()) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]+" and clusters on Fog Device "+ i +" = "+clusterCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);	
		

	}
	
	protected void submitCloudletsKhalidRandomRL() {
		Random rand=new Random();
		int total=0;
		int i=0,j=0;
		int clusterSize[]=new int[200];
		int noOfClusters=0;
		long lengthOfCluster[]=new long[200];
		int result[]=new int[200];
		int clusterRange[]=new int[200];
		
		double times[]=new double[vmList.size()];
		times[0]=64.52;
		times[1]=48.7;
		times[2]=21.87;
		times[3]=40.42;
		times[4]=90.94;
		int min;
		
		
		while(total!=5000) {
			noOfClusters++;
			int val=rand.nextInt(100);
			val++;		//to avoid zero size cluster
			System.out.println("random number generated = "+val+" and total = "+total);
			if((total+val)<5000) {
				total+=val;
				clusterSize[i]=val;
				
			}
			else {
				clusterSize[i]=5000-total;
				total=5000;
			}
			i++;
		}
		noOfClusters=i;
		System.out.println("Number of clusters is: "+noOfClusters);
		
		int sum=0;
		clusterRange[0]=0;
		for(i=0;i<noOfClusters;i++) {
			lengthOfCluster[i]=clusterSize[i]*10000;
			sum=sum+clusterSize[i];
			clusterRange[(i+1)]=sum;
		}
		
		int JobCount[]=new int[vmList.size()];
		int clusterCount[]=new int[vmList.size()];		
		int exMin=0;
		int ComCost=0;

		for(i=0;i<noOfClusters;i++) {
			min=findMinimum(times);

			clusterCount[min]++;
			for(j=clusterRange[i];j<clusterRange[i+1];j++) {
				bindCloudletToVm(cloudletList.get(j).getCloudletId(), min);
				times[min]+=cloudletList.get(j).getCloudletLength()/vmList.get(min).getMips();
				JobCount[min]++;
			}
			if(i==0) {
				exMin=min;
			}
			else {
				if(exMin!=min)
					ComCost++;
			}
			exMin=min;
		}
				
	
//		//for khalid program
//		int JobCount[]=new int[vmList.size()];
//		int clusterCount[]=new int[vmList.size()];
//		int ComCost=0;
//		for(i=0;i<noOfClusters;i++) {
//				JobCount[result[i]]+=clusterSize[i];
//				clusterCount[result[i]]++;
//				if(i==(noOfClusters-1))
//					continue;
//				else {
//					if(result[i]!=result[i+1]) {
//						ComCost++;
//					}
//				}
//		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]+" and clusters on Fog Device "+ i +" = "+clusterCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);		
	} 
	
	protected void submitCloudletsKhalidRandomCluster() {
		Random rand=new Random();
		int total=0;
		int i=0,j=0;
		int clusterSize[]=new int[200];
		int noOfClusters=0;
		long lengthOfCluster[]=new long[200];
		int result[]=new int[200];
		int index, value;
		int clusterRange[]=new int[200];
		
		
		while(total!=5000) {
			noOfClusters++;
			int val=rand.nextInt(100);
			val++;		//to avoid zero size cluster
			System.out.println("random number generated = "+val+" and total = "+total);
			if((total+val)<5000) {
				total+=val;
				clusterSize[i]=val;
				
			}
			else {
				clusterSize[i]=5000-total;
				total=5000;
			}
			i++;
		}
		noOfClusters=i;
		System.out.println("Number of clusters is: "+noOfClusters);
		
		int sum=0;
		clusterRange[0]=0;
		for(i=0;i<noOfClusters;i++) {
			lengthOfCluster[i]=clusterSize[i]*10000;
			sum=sum+clusterSize[i];
			clusterRange[(i+1)]=sum;
		}
		
		int row=noOfClusters;
		int col=vmList.size();
		double[][] costMatrix=new double[noOfClusters][vmsCreatedList.size()];
		double[][] costMatrixTemp=new double[noOfClusters][vmsCreatedList.size()];
		for(i=0;i<noOfClusters;i++) {		//row is for cloudlets
			for(j=0;j<vmsCreatedList.size();j++) {	//column is for VMs
				costMatrix[i][j]=(int) (lengthOfCluster[i]/vmList.get(j).getMips());
				costMatrixTemp[i][j]=(int) (lengthOfCluster[i]/vmList.get(j).getMips());
			}
		}	
		
//		System.out.println("Cost matrix before implementation of the algo is:");
//		displayCostMatrix(costMatrix);
		
		for(i=0;i<row;i++) {
			value=(int) costMatrix[i][0];			//store first value
			index=0;
			for(j=0;j<col;j++) {
				if(value>costMatrix[i][j]) {
					value=(int) costMatrix[i][j];
					index=j;
				}	
			}	//this loops finds the smallest value in each row and its corresponding index	
			result[i]=index;
			value=(int)costMatrixTemp[i][index];
//			costMatrix[i][index]=9999;
			for(int k=0;k<row;k++) {
				if(k==i)
					continue;
				costMatrix[k][index]+=value;
			}
//			System.out.println("value of i: "+i);
//			displayCostMatrix(costMatrix);
		}
		
		System.out.println("Final resultant matrix is: ");
		for(i=0;i<noOfClusters;i++) {
			for(j=clusterRange[i];j<clusterRange[(i+1)];j++) {
				bindCloudletToVm(cloudletList.get(j).getCloudletId(), result[i]);
				System.out.println("Cloudlet "+cloudletList.get(j).getCloudletId()+" has been bound to VM "+result[i]);
				//System.out.println("  "+result[i]+"("+costMatrix[i][result[i]]+")\t");
			}			
		}
	
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int clusterCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<noOfClusters;i++) {
				JobCount[result[i]]+=clusterSize[i];
				clusterCount[result[i]]++;
				if(i==(noOfClusters-1))
					continue;
				else {
					if(result[i]!=result[i+1]) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]+" and clusters on Fog Device "+ i +" = "+clusterCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);				
		
	}
	
	protected void submitCloudletsKhalidCluster() {		//cloudlets are mapped to the VMs in RR fashion
				
		int totalCloudlets=getCloudletList().size();
//		int totalDevices=getVmList().size();
		int clusterSize=100;
		int noOfClusters=totalCloudlets/clusterSize;
		double lengthOfCluster=cloudletList.get(0).getCloudletLength()*clusterSize;
		int i,j;
		int index,value;
		int result[]=new int[noOfClusters];
		int row=noOfClusters;
		int col=getVmList().size();
		
		double[][] costMatrix=new double[noOfClusters][vmsCreatedList.size()];
		double[][] costMatrixTemp=new double[noOfClusters][vmsCreatedList.size()];
		for(i=0;i<noOfClusters;i++) {		//row is for cloudlets
			for(j=0;j<vmsCreatedList.size();j++) {	//column is for VMs
				costMatrix[i][j]=(int) (lengthOfCluster/vmList.get(j).getMips());
				costMatrixTemp[i][j]=(int) (lengthOfCluster/vmList.get(j).getMips());
			}
		}	
		
//		System.out.println("Cost matrix before implementation of the algo is:");
//		displayCostMatrix(costMatrix);
		
		for(i=0;i<row;i++) {
			value=(int) costMatrix[i][0];			//store first value
			index=0;
			for(j=0;j<col;j++) {
				if(value>costMatrix[i][j]) {
					value=(int) costMatrix[i][j];
					index=j;
				}	
			}	//this loops finds the smallest value in each row and its corresponding index	
			result[i]=index;
			value=(int)costMatrixTemp[i][index];
//			costMatrix[i][index]=9999;
			for(int k=0;k<row;k++) {
				if(k==i)
					continue;
				costMatrix[k][index]+=value;
			}
//			System.out.println("value of i: "+i);
//			displayCostMatrix(costMatrix);
		}
		
		System.out.println("Final resultant matrix is: ");
		for(i=0;i<noOfClusters;i++) {
			for(j=0;j<clusterSize;j++) {
				bindCloudletToVm(cloudletList.get(i*clusterSize+j).getCloudletId(), result[i]);
				System.out.println("Cloudlet "+cloudletList.get(i*clusterSize+j).getCloudletId()+" has been bound to VM "+result[i]);
				//System.out.println("  "+result[i]+"("+costMatrix[i][result[i]]+")\t");
			}			
		}
	
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<noOfClusters;i++) {
				JobCount[result[i]]+=clusterSize;
				if(i==(noOfClusters-1))
					continue;
				else {
					if(result[i]!=result[i+1]) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);				
	}
	
	
	
	//ACO funtion from github
	//does not require the remaining portion of the original submitCloudlets()
	protected void submitCloudletsACO() {
		// int vmIndex = 0;
		List<Cloudlet> clList = getCloudletList();
		List<Vm> vm_list = getVmsCreatedList();
		// Random r = new Random();
		// int m = (vm_list.size()/4 + r.nextInt(vm_list.size()/2+1))%vm_list.size();

		LBACO lbaco1 = new LBACO(m,Q,alpha,beta,gamma,rho);
		Map<Integer, Integer> allocated=null;
		try {
			allocated = lbaco1.implement(clList,vm_list,100);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i=0;i<clList.size();i++) {
			Cloudlet cloudlet = clList.get(i);
			Vm vm = vm_list.get(allocated.get(i));
			// if user didn't bind this cloudlet and it has not been executed yet
			// if (cloudlet.getVmId() == -1) {
			// 	vm = getVmsCreatedList().get(vmIndex);
			// } else { // submit to the specific vm
			// 	vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			// 	if (vm == null) { // vm was not created
			// 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
			// 				+ cloudlet.getCloudletId() + ": bount VM not available");
			// 		continue;
			// 	}
			// }

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			// vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}
	
	//Display the contents of the cost Matrix
	protected void displayCostMatrix(double costMatrix[][])
	{
		System.out.println("\nContents of the CostMatrix are");
		for(int i=0;i<cloudletList.size();i++)
		{
			for(int j=0;j<vmsCreatedList.size();j++)
			{
				System.out.print(costMatrix[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	
	protected void submitCloudletsMinMin() {	
		int row,col,i,j;
		int index, value;
		int result[]=new int[cloudletList.size()];
		
		System.out.println("No of VMs: "+vmsCreatedList.size()+" No of cloudlets: "+cloudletList.size());
		row=cloudletList.size();
		col=vmsCreatedList.size();
		
//		//cloudletList.sort(c);
//		System.out.print("Cloudlets before sorting:\t");
//		for(i=0;i<cloudletList.size();i++)
//			System.out.print(cloudletList.get(i).getCloudletId()+"\t");
		
		//sort the cloudlets on the basis of their total length
		Collections.sort(cloudletList, new Comparator<Cloudlet>() {
			@Override
			public int compare(Cloudlet o1, Cloudlet o2) {
				// TODO Auto-generated method stub
				return (int)(o1.getCloudletTotalLength()-o2.getCloudletTotalLength());
			}
		});

//		System.out.print("\nCloudlets after sorting:\t");
//		for(i=0;i<cloudletList.size();i++)
//			System.out.print(cloudletList.get(i).getCloudletId()+"\t");
//		System.out.println();

		double[][] costMatrix=new double[cloudletList.size()][vmsCreatedList.size()];
		double[][] costMatrixTemp=new double[cloudletList.size()][vmsCreatedList.size()];
		for(i=0;i<cloudletList.size();i++) {		//row is for cloudlets
			for(j=0;j<vmsCreatedList.size();j++) {	//column is for VMs
				costMatrix[i][j]=(int) (cloudletList.get(i).getCloudletLength()/vmList.get(j).getMips());
				costMatrixTemp[i][j]=(int) (cloudletList.get(i).getCloudletLength()/vmList.get(j).getMips());
			}
		}	
		
//		System.out.println("Cost matrix before implementation of the algo is:");
//		displayCostMatrix(costMatrix);
		
		for(i=0;i<row;i++) {
			value=(int) costMatrix[i][0];			//store first value
			index=0;
			for(j=0;j<col;j++) {
				if(value>costMatrix[i][j]) {
					value=(int) costMatrix[i][j];
					index=j;
				}	
			}	//this loops finds the smallest value in each row and its corresponding index	
			result[i]=index;
			value=(int)costMatrixTemp[i][index];
//			costMatrix[i][index]=9999;
			for(int k=0;k<row;k++) {
				if(k==i)
					continue;
				costMatrix[k][index]+=value;
			}
//			System.out.println("value of i: "+i);
//			displayCostMatrix(costMatrix);
		}
		
		System.out.println("Final resultant matrix is: ");
		for(i=0;i<cloudletList.size();i++) {
			bindCloudletToVm(cloudletList.get(i).getCloudletId(), result[i]);
			System.out.println("Cloudlet "+cloudletList.get(i).getCloudletId()+" has been bound to VM "+result[i]);
			//System.out.println("  "+result[i]+"("+costMatrix[i][result[i]]+")\t");
		}

		
		//for khalid program
		int JobCount[]=new int[vmList.size()];
		int ComCost=0;
		for(i=0;i<cloudletList.size();i++) {
				JobCount[result[i]]++;
				if(i==(cloudletList.size()-1))
					continue;
				else {
					if(result[i]!=result[i+1]) {
						ComCost++;
					}
				}
		}
		System.out.println("Count of Jobs on each machine");
		for(i=0;i<JobCount.length;i++) {
			System.out.println("Jobs on Fog Device "+i+" = "+JobCount[i]);
		}
		System.out.println("Cost of communication is: "+ComCost);
	}
	
	
	protected void submitCloudletsMaxMin() {		//remaining part of the main function (submitcloudlets) should be executed
		int row,col,i,j;
		int index, value;
		int result[]=new int[cloudletList.size()];
		
		System.out.println("No of VMs: "+vmsCreatedList.size()+" No of cloudlets: "+cloudletList.size());
		row=cloudletList.size();
		col=vmsCreatedList.size();
		
		//sort the cloudlets on the basis of their total length
		Collections.sort(cloudletList, new Comparator<Cloudlet>() {
			@Override
			public int compare(Cloudlet o1, Cloudlet o2) {
				// TODO Auto-generated method stub
				return (int)(o2.getCloudletTotalLength()-o1.getCloudletTotalLength());
			}
		});
		
		double[][] costMatrix=new double[cloudletList.size()][vmsCreatedList.size()];
		double[][] costMatrixTemp=new double[cloudletList.size()][vmsCreatedList.size()];
		for(i=0;i<cloudletList.size();i++) {		//row is for cloudlets
			for(j=0;j<vmsCreatedList.size();j++) {	//column is for VMs
				costMatrix[i][j]=(int) (cloudletList.get(i).getCloudletLength()/vmList.get(j).getMips());
				costMatrixTemp[i][j]=(int) (cloudletList.get(i).getCloudletLength()/vmList.get(j).getMips());
			}
		}	
		
		System.out.println("Cost matrix before implementation of the algo is:");
		displayCostMatrix(costMatrix);
		
		for(i=0;i<row;i++) {
			value=(int) costMatrix[i][0];			//store first value
			index=0;
			for(j=0;j<col;j++) {
				if(value>costMatrix[i][j]) {
					value=(int) costMatrix[i][j];
					index=j;
				}	
			}	//this loops finds the smallest value in each row and its corresponding index	
			result[i]=index;
			value=(int)costMatrixTemp[i][index];
//			costMatrix[i][index]=9999;
			for(int k=0;k<row;k++) {
				if(k==i)
					continue;
				costMatrix[k][index]+=value;
			}
//			displayCostMatrix(costMatrix);
		}
		
		System.out.println("Final resultant matrix is: ");
		for(i=0;i<cloudletList.size();i++) {
			bindCloudletToVm(cloudletList.get(i).getCloudletId(), result[i]);
			System.out.println("Cloudlet "+cloudletList.get(i).getCloudletId()+" has been bound to VM "+result[i]);
		}
	}
	
//	protected void submitCloudletsHungarianAlgo() {		//use vmsCreatedList.size()	instead of vmList.size()
//		int i,j;
//		double cost=0;
//		/** Cost matrix for hungarian algo*/
//		double[][] costMatrix=new double[cloudletList.size()][cloudletList.size()];
//		System.out.println("***********************Cost matrix is:");
//		for(i=0;i<cloudletList.size();i++) {				//row is for cloudlet
//			for(j=0;j<vmList.size();j++) {		//column is for VM
//				costMatrix[i][j]=(int) (cloudletList.get(i).getCloudletLength()/vmList.get(j).getMips());
////				System.out.print("cloudlet id: "+i+" cloudlet length: "+getCloudletList().get(i).getCloudletLength()+" VM id: "+j+" VM mips: "+vmList.get(j).getMips());
////				System.out.println("  costMatrix: "+costMatrix[i][j]);
//			}
////			System.out.println();
//		}
//		//display costMatrix values
//		System.out.println("Final matrix is: ");
//		for(i=0;i<cloudletList.size();i++) {
//			for(j=0;j<vmList.size();j++) {
//				System.out.print(costMatrix[i][j]+"\t");			
//			}
//			System.out.println();
//		}
//		HungarianAlgorithm algo=new HungarianAlgorithm(costMatrix);
//		int result[]=algo.execute();		//returns VM no for each cloudlet
//		System.out.println("Resultant matrix is: ");
//		for(i=0;i<vmList.size();i++) {
//			bindCloudletToVm(cloudletList.get(i).getCloudletId(), result[i]);
//			System.out.print("Cloudlet "+cloudletList.get(i).getCloudletId()+" has been bound to VM "+result[i]);
//			System.out.println("  "+result[i]+"("+costMatrix[i][result[i]]+")\t");
//			cost=cost+costMatrix[i][result[i]];
//		}
//		System.out.println("\nTotal cost is: "+cost);
//		
//	}
//	

	//rank based function where the cloudlets are sorted first and then mapped sequentially
	//larger cloudlets assume higher priority
	protected void submitCloudletsPriority() {		//I have created this function
		int vmIndex = 0;
		List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
		
		//sort the cloudlets on the basis of their total length
		Collections.sort(cloudletList, new Comparator<Cloudlet>() {
			@Override
			public int compare(Cloudlet o1, Cloudlet o2) {
				// TODO Auto-generated method stub
				return (int)(o1.getCloudletTotalLength()-o2.getCloudletTotalLength());
			}
		});
		
//		//sort the VMs on the basis of their total mips
//		Collections.sort(vmList,new Comparator<Vm>() {
//			@Override
//			public int compare(Vm o1, Vm o2) {
//				// TODO Auto-generated method stub
//				return (int) (o1.getCurrentRequestedTotalMips()-o2.getCurrentRequestedTotalMips());
//			}
//		});
		
//		for(Vm vm: getVmList()) {
//			System.out.println("VM id:"+vm.getId()+" mips: "+vm.getMips());
//		}
		
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
//				vm = getVmsCreatedList().get(vmIndex);
				vm = getVmList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					if(!Log.isDisabled()) {				    
					    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
							cloudlet.getCloudletId(), ": bount VM not available");
					}
					continue;
				}
			}

			if (!Log.isDisabled()) {
			    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
					cloudlet.getCloudletId(), " to VM #", vm.getId());
			}
			
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
			successfullySubmitted.add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);
	}
	
	
	//rank based function where the cloudlets and VMs are sorted first and then mapped sequentially
	protected void submitCloudletsModified() {		//I have created this function
		int vmIndex = 0;
		List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
		
		//sort the cloudlets on the basis of their total length
		Collections.sort(cloudletList, new Comparator<Cloudlet>() {
			@Override
			public int compare(Cloudlet o1, Cloudlet o2) {
				// TODO Auto-generated method stub
				return (int)(o1.getCloudletTotalLength()-o2.getCloudletTotalLength());
			}
		});
		
		//sort the VMs on the basis of their total mips
		Collections.sort(vmList,new Comparator<Vm>() {
			@Override
			public int compare(Vm o1, Vm o2) {
				// TODO Auto-generated method stub
				return (int) (o1.getCurrentRequestedTotalMips()-o2.getCurrentRequestedTotalMips());
			}
		});
		
//		for(Vm vm: getVmList()) {
//			System.out.println("VM id:"+vm.getId()+" mips: "+vm.getMips());
//		}
		
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
//				vm = getVmsCreatedList().get(vmIndex);
				vm = getVmList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					if(!Log.isDisabled()) {				    
					    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
							cloudlet.getCloudletId(), ": bount VM not available");
					}
					continue;
				}
			}

			if (!Log.isDisabled()) {
			    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
					cloudlet.getCloudletId(), " to VM #", vm.getId());
			}
			
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
			successfullySubmitted.add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);
	}
	/**
	 * Destroy all virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printConcatLine(CloudSim.clock(), ": " + getName(), ": Destroying VM #", vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	@Override
	public void shutdownEntity() {
		Log.printConcatLine(getName(), " is shutting down...");
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment the number of acknowledges (ACKs) sent in response
         * to requests of VM creation.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

}
