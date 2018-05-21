package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class scl {
	private static List<Cloudlet> cloudletList;
	
	public int cloudletId;
	public long length;
	
	scl(int cloudletId,long length)
	{
		this.cloudletId=cloudletId;
		this.length=length;
	}
	public static <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}
	 static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		int length = 40000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		 ArrayList<Integer> ar=new ArrayList<Integer>();
		Cloudlet[] cloudlet = new Cloudlet[cloudlets];
        Random random=new Random();

		for(int i=0;i<cloudlets;i++){
			int j=random.nextInt(10)+1;
			cloudlet[i] = new Cloudlet(idShift + i, length*j, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
			ar.add(length*j);
			System.out.println(i+"  length                "+j+"  id        "+length*j);
			}
		Collections.sort(ar);
       

  	   /* Sorted List*/
  	   System.out.println("After Sorting:");
  	   for(int counter: ar){
  			System.out.println(counter);
  		}
		return list;
	}
	 /**
	  * for (int i = 0; i < arr.length - 1; i++)
	         {
	             int index = i;
	             for (int j = i + 1; j < arr.length; j++)
	                 if (arr[j] < arr[index]) 
	                     index = j;
	       
	             int smallerNumber = arr[index];  
	             arr[index] = arr[i];
	             arr[i] = smallerNumber;
	         }
	         
	  */

	 public List<Cloudlet> sortlist(List<Cloudlet> cloudlet)
	 {
		 
		List<Cloudlet> c1=cloudlet;
		List<Cloudlet> temp;
		
		for(int i=0;i<c1.size()-1;i++){
		int index=i;
				
		for(Cloudlet b:cloudlet){
			//if()
		}}
		return cloudlet;
		 
	 }
	@SuppressWarnings("null")
	public static void main(String[] args){
	List<Cloudlet> a=createCloudlet(1,10,0);
	LinkedList<Cloudlet> t= (LinkedList<Cloudlet>) a;
	Cloudlet temp=null;
	LinkedList<Cloudlet> temp1= new LinkedList<Cloudlet>();
	//System.out.println(a.size());
	long min=1000000;
	int id=0;
	UtilizationModel utilizationModel = new UtilizationModelFull();
	
	for(int i=0;i<10;i++){		
	for(Cloudlet b:t){
		
		if(min>b.getCloudletLength()){
			min=b.getCloudletLength();
		id=b.getCloudletId();
		}
		
	
	}
		
	System.out.println("ID:"+id+"\t\tMinimum:"+min);
	temp = new Cloudlet(id,min,1,300,300,utilizationModel,utilizationModel,utilizationModel);
	System.out.println(t.remove(temp));
	temp1.add(temp);
	}
	System.out.println(temp.getCloudletLength());
	for(Cloudlet d:t)
		System.out.println("ID:"+d.getCloudletId()+"SIZE:\t"+d.getCloudletLength());
	for(Cloudlet d:temp1)
		System.out.println(d.getVmId()+"\t"+d.getCloudletId()+"\t"+d.getCloudletLength()+"\t"+d.getNumberOfPes()+"\t"+d.getCloudletFileSize()+"\t"+d.getCloudletOutputSize()+"\t"+d.getUtilizationOfBw(0));
	}
}
