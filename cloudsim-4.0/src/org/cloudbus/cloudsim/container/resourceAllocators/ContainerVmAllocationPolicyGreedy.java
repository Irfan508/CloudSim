package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 14/07/15.
 */
public class ContainerVmAllocationPolicyGreedy extends ContainerVmAllocationPolicy {

    /**
     * The vm table.
     */
    private Map<String, ContainerHost> vmTable;

    /**
     * The used pes.
     */
    private Map<String, Integer> usedPes;

    /**
     * The free pes.
     */
    private List<Integer> freePes;

    /**
     * Creates the new VmAllocationPolicySimple object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    public ContainerVmAllocationPolicyGreedy(List<? extends ContainerHost> list) {
        super(list);

        setFreePes(new ArrayList<Integer>());
        for (ContainerHost host : getContainerHostList()) {
            getFreePes().add(host.getNumberOfPes());

        }

        setVmTable(new HashMap<String, ContainerHost>());
        setUsedPes(new HashMap<String, Integer>());
    }

    //my function for greedy allocation of VMs to Hosts
    @Override	
    public boolean allocateHostForVm(ContainerVm containerVm) {
    	System.out.println("inside allocateHostForVm(ContainerVm containerVm) in Greedy");
		int requiredPes = containerVm.getNumberOfPes();
		boolean result = false;
		int index;
		
		if (!getVmTable().containsKey(containerVm.getUid())) { // if this vm was not created
			for(ContainerHost hosts:getContainerHostList()) {
				result=hosts.containerVmCreate(containerVm);
				if(result) {
					index=getContainerHostList().indexOf(hosts);
					getVmTable().put(containerVm.getUid(), hosts);
					getUsedPes().put(containerVm.getUid(), requiredPes);
					getFreePes().set(index, getFreePes().get(index) - requiredPes);
					result = true;
					break;
				}
			}
		}
		return result;
    }

    @Override
    public boolean allocateHostForVm(ContainerVm containerVm, ContainerHost host) {
        if (host.containerVmCreate(containerVm)) { // if vm has been succesfully created in the host
            getVmTable().put(containerVm.getUid(), host);

            int requiredPes = containerVm.getNumberOfPes();
            int idx = getContainerHostList().indexOf(host);
            getUsedPes().put(containerVm.getUid(), requiredPes);
            getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

            Log.formatLine(
                    "%.2f: VM #" + containerVm.getId() + " has been allocated to the host #" + host.getId(),
                    CloudSim.clock());
            return true;
        }

        return false;
    }


    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends ContainerVm> vmList) {
        return null;
    }

    @Override
    public void deallocateHostForVm(ContainerVm containerVm) {
        ContainerHost host = getVmTable().remove(containerVm.getUid());
        int idx = getContainerHostList().indexOf(host);
        int pes = getUsedPes().remove(containerVm.getUid());
        if (host != null) {
            host.containerVmDestroy(containerVm);
            getFreePes().set(idx, getFreePes().get(idx) + pes);
        }
    }

    @Override
    public ContainerHost getHost(ContainerVm containerVm) {
        return getVmTable().get(containerVm.getUid());
    }

    @Override
    public ContainerHost getHost(int vmId, int userId) {
        return getVmTable().get(ContainerVm.getUid(userId, vmId));
    }

    @Override
    public void setDatacenter(ContainerDatacenter datacenter) {

    }


    public Map<String, ContainerHost> getVmTable() {
        return vmTable;
    }

    public void setVmTable(Map<String, ContainerHost> vmTable) {
        this.vmTable = vmTable;
    }

    public Map<String, Integer> getUsedPes() {
        return usedPes;
    }

    public void setUsedPes(Map<String, Integer> usedPes) {
        this.usedPes = usedPes;
    }

    public List<Integer> getFreePes() {
        return freePes;
    }

    public void setFreePes(List<Integer> freePes) {
        this.freePes = freePes;
    }
}
