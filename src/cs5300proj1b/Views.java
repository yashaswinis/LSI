package cs5300proj1b;

import java.util.*;

//View implementation for each server
public class Views{
	
	private ArrayList<Server> views= new ArrayList<Server>();
	private int viewsize=5, count;

	public Views()
	{
		//Nothing
	}
	
	public ArrayList<Server> getView()
	{
		return this.views;
	}
	//Insert the server to the view
	public void insert(Server serverId)
	{
		System.out.println("Inside insert for object"+serverId.toString());
		if(!this.views.toString().contains(serverId.toString())){
			System.out.println("Inserted");
			this.views.add(serverId);
		}
	}
	
	//Union of two views
	public ArrayList<Server> union(Views view2)
	{
		ArrayList<Server> tempView= new ArrayList<Server>();
		tempView.addAll(this.views);
		for(int j=0; j<view2.getView().size();j++)
		{
			if(!tempView.toString().contains(view2.getView().get(j).toString()))
			{
				tempView.add(view2.getView().get(j));
			}
		}
		System.out.println("Union of two views is "+ tempView.toString());
		System.out.println("Size after views is "+ tempView.size());
		return tempView;
	}
	
	//Shrink the view to viewsize (5)
	public ArrayList<Server> shrink()
	{
		/*int i=0;
		count=1;
		System.out.println("In shrink");
		if(this.views.size()>viewsize)
		{
			
			Iterator<Server> iter = this.views.iterator();
			while(iter.hasNext())
			{
				if(count>viewsize)
				{
					this.views.remove(i);
				}
				else
				{
					i++;
				}
				count++;
				
			}
		}
		System.out.println("temp view after shrink is "+ this.views);*/
		for (int i = this.views.size() - 1; i >= viewsize; --i){
	        this.views.remove(i);
	     }
		return this.views;	
	}
	
	//Remove the server from the view
	public void remove(Server serverID)
	{
		System.out.println("Inside remove for object"+serverID.toString());
		for(Iterator<Server> itr = this.views.iterator();itr.hasNext();)  
        {  
            Server element = itr.next();  
            if(serverID.toString().equals(element.toString()))  
            {  
            	System.out.println("Removed");
                itr.remove();  
            }  
        } 
		for(Server s:this.views){
			System.out.println("this.view() after remove "+s.toString());
		}
	}	
}