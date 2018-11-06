package org.cloudbus.cloudsim.container.resourceAllocators;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.container.core.CircularHostList;
import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.CloudSim;

public class ContainerVmAllocationPolicyRR extends ContainerVmAllocationPolicy {

    private final Map<String, ContainerHost> _vmTable = new HashMap<String, ContainerHost>();
    private final CircularHostList _hosts;

    public ContainerVmAllocationPolicyRR(List<? extends ContainerHost> list) {
        super(list);
        this._hosts = new CircularHostList(list);
    }

    @Override
    public boolean allocateHostForVm(ContainerVm vm) {
    	System.out.println("inside allocateHostForVm function inside RR class");
    	if (this._vmTable.containsKey(vm.getUid())) {
            return true;
        }

        boolean vm_allocated = false;

        ContainerHost host = this._hosts.next();
        if (host != null) {
            vm_allocated = this.allocateHostForVm(vm, host);
        }

        return vm_allocated;
    }

    @Override
    public boolean allocateHostForVm(ContainerVm vm, ContainerHost host) {
        if (host != null && host.containerVmCreate(vm)) {
            _vmTable.put(vm.getUid(), host);
         //   System.out.println(host.getId());
            Log.formatLine("%.4f: VM #" + vm.getUid() + "shas been allocated to the host#" + host.getId()
                    + " datacenter #" + host.getDatacenter().getId() + "(" + host.getDatacenter().getName() + ") #",
                    CloudSim.clock());
            host.getDatacenter().containerVmList.add(vm);
          
        
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends ContainerVm> vmList) {
        return null;
    }

    @Override
    public void deallocateHostForVm(ContainerVm vm) {
    	ContainerHost host = this._vmTable.remove(vm.getUid());

        if (host != null) {
            host.containerVmDestroy(vm);
        }
    }

    @Override
    public ContainerHost getHost(ContainerVm vm) {
        return this._vmTable.get(vm.getUid());
    }

    @Override
    public ContainerHost getHost(int vmId, int userId) {
        return this._vmTable.get(ContainerVm.getUid(userId, vmId));
    }

	@Override
	public void setDatacenter(ContainerDatacenter datacenter) {
		// TODO Auto-generated method stub
		
	}
}


