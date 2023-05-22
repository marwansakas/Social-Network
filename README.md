# Social-Network

Compiling and Running the code instructions:
1. Compiling and Running the Server:
	1. in order to run the server in ThreadPerCLient method please use the following command:
		1. mvn compile
		2. mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="7777"

		notes:
		1. you can replace the port number 7777 with any desired port number.

	2. in order to run the server in Reactor method please use the following command:
		1. mvn compile
		2. mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7777 5"

		notes:
		1. you can replace the port number 7777 with any desired port number.
		2. you can replace the threads number 5 with any desired number of threads.

2. Compiling and Running the Client:
	1. in order to compile the Client use the command:
		1. make clean all
	2. in order to run the Client:
		1. ./bin/echoClient 127.0.0.1 7777
	notes:
		1. when using the run command for the client make sure that the port number you use matches the one the server uses
		2. the first argument is the ip to connect to, so if the server runs on another computer you should change it to the relevant ip

Input assumptions and Instructions:

	1. we assume that when you enter the date it will be in the following format: DD-MM-YYYY

	2. the filtered words are read from the file called filtered_words.txt which is located in the server file.
	in order to add/remove words just edit the file, we assume that each line contains a single word.
	
	3. we assume that the first word of each command which is the desired command to be in capital letter.

Example of the correct format of written commands:
	1. REGISTER command:
		REGISTER <username> <password> <DD-MM-YYYY>
	2. LOGIN command:
		LOGIN <username> <passowrd> <captcha "0 for faliure or 1 for success" >
	3. FOLLOW command:
		FOLLOW <0/1 "0 for follow / 1 for unfollow"> <username> 
	4. POST command:
		POST <message_to_post>
	5. PM command:
		PM <username_to_send_to> <message_to_send>
	6. LOGSTAT command:
		LOGSTAT
	7. STAT command:
		STAT <user1>|<user2>|<user3>...
	8. LOGOUT command: 
		LOGOUT
	9. BLOCK command:
		BLOCK <username_to_block>



