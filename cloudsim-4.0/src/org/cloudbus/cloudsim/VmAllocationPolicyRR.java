/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less PEs in use. It is therefore a Worst Fit policy, allocating VMs into the 
 * host with most available PE.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmAllocationPolicyRR extends VmAllocationPolicy {

	/** The map between each VM and its allocated host.
         * The map key is a VM UID and the value is the allocated host for that VM. */
	private Map<String, Host> vmTable;

	/** The map between each VM and the number of Pes used. 
         * The map key is a VM UID and the value is the number of used Pes for that VM. */
	private Map<String, Integer> usedPes;

	/** The number of free Pes for each host from {@link #getHostList() }. */
	private List<Integer> freePes;

	/**
	 * Creates a new VmAllocationPolicySimple object.
	 * 
	 * @param list the list of hosts
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicyRR(List<? extends Host> list) {
		super(list);

		setFreePes(new ArrayList<Integer>());
		for (Host host : getHostList()) {
			getFreePes().add(host.getNumberOfPes());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedPes(new HashMap<String, Integer>());
	}

	/**
	 * Allocates the host with less PEs in use for a given VM.
	 * 
	 * @param vm {@inheritDoc}
	 * @return {@inheritDoc}
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		return allocateHostForVmRR(vm);		
		
//		int requiredPes = vm.getNumberOfPes();
//		boolean result = false;
//		int tries = 0;
//		
//		List<Integer> freePesTmp = new ArrayList<Integer>();
//		for (Integer freePes : getFreePes()) {		//getFreePes() returns all the free PEs of all hosts
//			freePesTmp.add(freePes);
//		}
//
//		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
//			do {// we still trying until we find a host or until we try all of them
//				int moreFree = Integer.MIN_VALUE;
//				int idx = -1;
//
//				// we want the host with less pes in use
//				for (int i = 0; i < freePesTmp.size(); i++) {
//					if (freePesTmp.get(i) > moreFree) {
//						moreFree = freePesTmp.get(i);
//						idx = i;								//stores the id of the host with more PEs available
//					}
//				}
//
//				Host host = getHostList().get(idx);
//				result = host.vmCreate(vm);
//
//				if (result) { // if vm were succesfully created in the host
//					getVmTable().put(vm.getUid(), host);
//					getUsedPes().put(vm.getUid(), requiredPes);
//					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
//					result = true;
//					break;
//				} else {
//					freePesTmp.set(idx, Integer.MIN_VALUE);
//				}
//				tries++;
//			} while (!result && tries < getFreePes().size());
//
//		}
//
//		return result;
	}

	//my function...implement roundrobin
	//check number of VMs on each host and allocate new VM on the host with least no of VMs
	public boolean allocateHostForVmRR(Vm vm) {
		List<Host> myHostlist=getHostList();
		
		boolean result=false;
		int tries=0;
		int requiredPes = vm.getNumberOfPes();
		int arr[]=new int[myHostlist.size()];
		Arrays.fill(arr, 0);
		
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				int lowest=Integer.MAX_VALUE;	
				int presentVms=Integer.MIN_VALUE;			//number of VMs present on host
				int index=-1;
				
				for(Host hosts:myHostlist) {			//if the hosts have equal VMs then they will be selected in sequential order
						presentVms= hosts.getVmList().size();
						if(lowest>presentVms&&arr[myHostlist.indexOf(hosts)]==0) {
							lowest=presentVms;
//							index=hosts.getId();		//stores the id of the host and not the index
							index=myHostlist.indexOf(hosts);
						}
					}					
					
					Host host = getHostList().get(index);		
					result = host.vmCreate(vm);
							
					if (result) { // if vm were succesfully created in the host
						getVmTable().put(vm.getUid(), host);
						getUsedPes().put(vm.getUid(), requiredPes);
						getFreePes().set(index, getFreePes().get(index) - requiredPes);
						result = true;
						break;
					} else {
//						myHostlist.remove(host);
						arr[index]=1;			//host with arr[index]=1 will not be considered for scheduling
						
					}
					tries++;
				} while (!result && tries < getFreePes().size());
		}
		return result;
	}
	
	//my function...implement greedy algorithm
	//First capable machine found is selected
	public boolean allocateHostForVmGreedy(Vm vm) {		
		int requiredPes = vm.getNumberOfPes();
		boolean result = false;
		int index;
		
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			for(Host hosts:getHostList()) {
				result=hosts.vmCreate(vm);
				if(result) {
					index=getHostList().indexOf(hosts);
					getVmTable().put(vm.getUid(), hosts);
					getUsedPes().put(vm.getUid(), requiredPes);
					getFreePes().set(index, getFreePes().get(index) - requiredPes);
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	//my function...implement Rank Based approach
	//New VM is assigned on a host with more available mips
	public boolean allocateHostForVmRankBased(Vm vm) {
		List<Host> myHostlist=getHostList();		
		boolean result=false;
		int tries=0;
		int requiredPes = vm.getNumberOfPes();
		int arr[]=new int[myHostlist.size()];
		Arrays.fill(arr, 0);
		System.out.println("\nFor VM #"+vm.getId()+" Total mips: "+vm.getCurrentRequestedTotalMips());
		for(Host hosts:getHostList())
			System.out.println("Host #"+hosts.getId()+" mips:"+hosts.getAvailableMips());
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				double lowest=Integer.MIN_VALUE;	
				double presentmips=Integer.MIN_VALUE;			//number of mips present on host
				int index=-1;
				
				for(Host hosts:myHostlist) {			//if the hosts have equal mips then they will be selected in sequential order
						presentmips= hosts.getAvailableMips();
						if(lowest<presentmips&&arr[myHostlist.indexOf(hosts)]==0) {
							lowest=presentmips;
//							index=hosts.getId();		//stores the id of the host and not the index
							index=myHostlist.indexOf(hosts);
						}
					}					
					
					Host host = getHostList().get(index);		
					result = host.vmCreate(vm);
							
					if (result) { // if vm were succesfully created in the host
						getVmTable().put(vm.getUid(), host);
						getUsedPes().put(vm.getUid(), requiredPes);
						getFreePes().set(index, getFreePes().get(index) - requiredPes);
						result = true;
						break;
					} else {
//						myHostlist.remove(host);
						arr[index]=1;			//host with arr[index]=1 will not be considered for scheduling
						
					}
					tries++;
				} while (!result && tries < getFreePes().size());
		}
		return result;
	}
	
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the used pes.
	 * 
	 * @return the used pes
	 */
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	/**
	 * Sets the used pes.
	 * 
	 * @param usedPes the used pes
	 */
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	/**
	 * Gets the free pes.
	 * 
	 * @return the free pes
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

	/**
	 * Sets the free pes.
	 * 
	 * @param freePes the new free pes
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);

			int requiredPes = vm.getNumberOfPes();
			int idx = getHostList().indexOf(host);
			getUsedPes().put(vm.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			System.out.println("inside allocateHostForVM() method");
			return true;
		}

		return false;
	}
}
