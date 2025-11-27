package zekusan;

import java.io.IOException;

import zekusan.app.systems.RequestHandler;
import zekusan.app.systems.SocketSystem;

public class App 
{
    public static void main( String[] args )
    {
//        SocketSystem socket = new SocketSystem();
//        
//        if (!socket.init()) {
//        	return;
//        }
//        
//        while (socket.isOnline()){
//        	try {
//        		socket.listen();
//        	} catch (IOException e) {
//        		e.printStackTrace();
//        	}
//        	
//        	String request = socket.getRequest();
//        	
//        }
    	RequestHandler.process("login|{prova}");
    	
    }
}
