Project 1 README
For your infomation, this project was done by using a windows computer. It is better to use windows os to test it.

To compile: 
1st option: you can use Eclipse. Copy and paste my code into the OSU project template, it should works fine. 

2nd option: command prompt
	        1) go to cmd or terminal
	        2) type "javac ChatServer.java"
	        3) type "javac ChatClient.java"
	        4) type "java ChatClient" to run the client file / "java ChatClient &" for more clients

Controls:
Enter: send message
Commands (type the command in the text box):
"-online" : to check the online user 
"-quit": to quit the client program (remember not to use the exit button ("X") on the GUI window, only type "-quit" for going offline)
Button: "SEND" button: send message 
	"CLEAR" button: clear the message window

Note: For BufferWriter, "next line" string is "\r\n" for window and  "\n" for mac. 
You might running into problem when using mac computer.

The program can only run on one device, professor said turn off the firewall would fix the issue. 
However, it still doesn't work on the another devices.
	      