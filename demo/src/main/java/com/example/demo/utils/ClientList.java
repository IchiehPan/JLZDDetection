package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;


public class ClientList {

	private static ClientList list = null;
    private ClientList() { }
    private List<Client> socketList = new ArrayList<Client>();
	
    public static ClientList GetClientList()
    {
        if (list == null)
            list = new ClientList();
        return list;
    }
    
    
    public void AddClient(Client client)
    {
        this.socketList.add(client);
    }
    
    public void SetSendData(byte[] data)
    {
       
        
        for (int i = 0; i < socketList.size(); i++)
        {
            socketList.get(i).SetData(data);
           
             
        }
    }
    public void SetHeadStramData(byte[] HeadStram)
    {
       
        
        for (int i = 0; i < socketList.size(); i++)
        {
            socketList.get(i).SetheadStream(HeadStram);
           
             
        }
    }
    
}
