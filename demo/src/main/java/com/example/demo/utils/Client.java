package com.example.demo.utils;

public class Client {



	private int DateType;
	
    private byte[] headStream;
     
    private byte[] sendData;

    private Thread sendThread;
	   
    
    public Client(String acceptIp) {
		
		sendThread = new Thread(new Runnable() {
			
			@SuppressWarnings("unused")
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 while (true) {
					try {
						 System.out.println(sendData.toString());;
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println("====================");
					}
					
					 
				 }
			}
		}) ;
		sendThread.start();
		
	}

	public void SetData(byte[] data) {

		
			sendData = data;
		

	}
	
	public void SetheadStream (byte[] headStream) {

		
		this.headStream = headStream;
	

}
	
}
