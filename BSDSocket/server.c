/* server.c - code for example server program that uses TCP */
#define closesocket close
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>    /* Unix System Calls */
#include <netinet/in.h> /* INET constants and stuff */

#define	PROTOPORT	5193		/* default protocol port number */
#define	QLEN		6		/* size of request queue	*/

int	visits	    =   0;		/* counts client connections	*/

/*------------------------------------------------------------------------
 * Program:   server
 *
 * Purpose:   allocate a socket and then repeatedly execute the following:
 *		(1) wait for the next connection from a client
 *		(2) send a short message to the client
 *		(3) close the connection
 *		(4) go back to step (1)
 *
 * Syntax:    server [ port ]
 *
 *		 port  - protocol port number to use
 *
 * Note:      The port argument is optional.  If no port is specified,
 *	      the server uses the default given by PROTOPORT.
 *
 *------------------------------------------------------------------------
 */
main(argc, argv)
int	argc;
char	*argv[];
{
	struct	hostent	 *ptrh;	 /* pointer to a host table entry	   */
	struct	sockaddr_in sad;   /* structure to hold server's address	*/
	struct	sockaddr_in cad;   /* structure to hold client's address	*/
	int	ld,udp,sd2,max;	    /* socket descriptors			         */
	fd_set fd;
	int	port;		       		 /* protocol port number		         */
	int	alen;		             /* length of address			         */
	char	buf[1000];	          /* buffer for string the server sends	*/
	pid_t PID;

	memset((char *)&sad,0,sizeof(sad)); /* clear sockaddr structure	*/
	sad.sin_family = AF_INET;	  /* set family to Internet	*/
	sad.sin_addr.s_addr = INADDR_ANY; /* set the local IP address	*/

	/* Check command-line argument for protocol port and extract	*/
	/* port number if one is specified.  Otherwise, use the	default	*/
	/* port value given by constant PROTOPORT			*/

	if (argc > 1) {			/* if argument specified	*/
		port = atoi(argv[1]);	/* convert argument to binary	*/
	} else {
		port = PROTOPORT;	/* use default port number	*/
	}
	if (port > 0)			/* test for illegal value	*/
		sad.sin_port = htons((u_short)port);
	else {				/* print error message and exit	*/
		fprintf(stderr,"bad port number %s\n",argv[1]);
		exit(1);
	}

   /*TCP*/
	/* Map TCP transport protocol name to protocol number */

	/* Create a socket */

	ld = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (ld < 0) {
		fprintf(stderr, "socket creation failed\n");
		exit(1);
	}

	/* Bind a local address to the socket */

	if (bind(ld, (struct sockaddr *)&sad, sizeof(sad)) < 0) {
		fprintf(stderr,"bind failed\n");
		exit(1);
	}

	/* Specify size of request queue */

	if (listen(ld, QLEN) < 0) {
		fprintf(stderr,"listen failed\n");
		exit(1);
	}

   /*UDP*/
	udp = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (udp < 0) {
		fprintf(stderr, "socket creation failed\n");
		exit(1);
	}

	/* Bind a local address to the socket */

	if (bind(udp, (struct sockaddr *)&sad, sizeof(sad)) < 0) {
		fprintf(stderr,"bind failed\n");
		exit(1);
	}

	max = (ld > udp ? ld : udp);
	while(1){

	FD_ZERO(&fd);
	FD_SET(ld,&fd); /* add passive tcp socket */
	FD_SET(udp,&fd);/* add passive udp soceket */

	if (select(max+1,&fd,NULL,NULL,NULL)<0) {
	perror("select problem");
	exit(1);
	}

	//--------------------------if TCP-----------------------//
	if (FD_ISSET(ld,&fd) ) 
   {
		/*accept a connection on a socket*/
		if ( (sd2=accept(ld, (struct sockaddr *)&cad, &alen)) < 0) 
		{
			fprintf(stderr, "accept failed\n");
			exit(1);
		}

		int	receivebit      =0;
		int	sendbit         =0;
		int 	receivesegement =0;
		int	sendsegement    =0;

      /*Use a fork(), can make many clients connecting to server*/
		if((PID=fork()) == 0)
		{
			while (1) {
				char *pointer;
				char socket_buf[1024]; /*set the socke buffer to be 1024*/
				bzero(socket_buf, 1024);/*set up the TCP server*/
				pointer = socket_buf; /*socket buff*/
				int receive = 0,sendM = 0;
				alen= sizeof(struct sockaddr_in);	
				if ((receive = recv(sd2, pointer, 1024, 0)) <= 0) /*receive message from socket. If nothing, break*/
				{
					break;
				}
				receivebit = receivebit + receive;
				receivesegement ++;

				sendM = send(sd2,pointer,strlen(pointer),0); /*tcp send buffer*/
				sendbit = sendbit + sendM;       /*caculate the total bit send to client*/
				sendsegement ++;                 /*segement value increase one*/
		    }
		close(sd2);
		printf("Send: %d bits to client.\n",sendbit*8); /*print the message in command line*/
		printf("Send: %d segements to client.\n",sendsegement);
		printf("Receive: %d bits from client.\n",receivebit*8);
		printf("Receive: %d segements from client.\n",receivesegement);
		}

		close(sd2);
	}
	
	
	/*UDP*/
	if (FD_ISSET(udp,&fd)){
		char *pointer;
		char socket_buf[1024]; /*set the socke buffer to be 1024*/
		bzero(socket_buf, 1024);/*set up the UDP server*/
		pointer = socket_buf;
		int rval=0;

		alen= sizeof(struct sockaddr_in);	
		
		/*read incoming data from the remote side using the recvfrom()*/
		if ((rval = recvfrom(udp, pointer, 1024, 0,(struct sockaddr *)&cad,&alen)) >0){
         /*use udp send to client*/			
			sendto(udp,pointer,strlen(pointer),0,(struct sockaddr *)&cad, alen);			
		}
	}
	}	
}
