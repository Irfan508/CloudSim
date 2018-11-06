package org.cloudbus.cloudsim.container.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.container.core.ContainerHost;

public final class CircularHostList implements Iterable<ContainerHost> {

    private final List<ContainerHost> _list = new LinkedList<ContainerHost>();
    private int ini;

    public CircularHostList(List<? extends ContainerHost> hosts) {
        this._list.addAll(hosts);
    }

    public boolean add(ContainerHost host) {
        return this._list.add(host);
    }

    public boolean remove(ContainerHost host2Remove) {
        return this._list.remove(host2Remove);
    }

    public ContainerHost next() {
    	ContainerHost host = null;

        if (!_list.isEmpty()) {
            int index = (this.ini++ % this._list.size());
            host = this._list.get(index);
        }

        return host;
    }

    @Override
    public Iterator<ContainerHost> iterator() {
        return get().iterator();
    }

    public List<ContainerHost> get() {
        return Collections.unmodifiableList(this._list);
    }

    public ContainerHost getWithMinimumNumberOfPesEquals(int numberOfPes) {
        List<ContainerHost> hosts = this.orderedAscByAvailablePes().get();

        for (int i = 0; i < hosts.size(); i++) {
            if (hosts.get(i).getNumberOfFreePes() >= numberOfPes) {
                return hosts.get(i);
            }
        }
        return null;
    }

    public int size() {
        return this._list.size();
    }

    public CircularHostList orderedAscByAvailablePes() {
        List<ContainerHost> list = new ArrayList<ContainerHost>(this._list);

        Collections.sort(list, new Comparator<ContainerHost>() {

            @Override
            public int compare(ContainerHost o1, ContainerHost o2) {
                return Integer.valueOf(o1.getNumberOfFreePes()).compareTo(
                        o2.getNumberOfFreePes());
            }
        });
        return new CircularHostList(list);
    }
}
