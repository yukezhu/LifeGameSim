/* client.c - code for example client program that uses TCP */

#define closesocket close
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>

#define	PROTOPORT	5193		/* default protocol port number */

extern	int		errno;
char	localhost[] =	"localhost";	/* default host name		*/
/*------------------------------------------------------------------------
 * Program:   TCP client
 *
 * Purpose:   allocate a socket, connect to a server, and print all output
 *
 * Syntax:    client [ host [port] ]
 *
 *		 host  - name of a computer on which server is executing
 *		 port  - protocol port number server is using
 *
 * Note:      Both arguments are optional.  If no host name is specified,
 *	      the client uses "localhost"; if no protocol port is
 *	      specified, the client uses the default given by PROTOPORT.
 *
 *------------------------------------------------------------------------
 */
main(argc, argv)
int	argc;
char	*argv[];
{
	struct	hostent	 *ptrh;	 /* pointer to a host table entry	*/
	struct	protoent *ptrp;	 /* pointer to a protocol table entry	*/
	struct	sockaddr_in sad; /* structure to hold an IP address	*/
	int	sd;		 /* socket descriptor			*/
	int	port;		 /* protocol port number		*/
	char	*host;		 /* pointer to host name		*/
	int	n;		 /* number of characters read		*/
	char	buf[4096];	 /* buffer for data from the server	*/

	int	receivecountbit=0;
	int	sendcountbit=0;
	char	inputfile[1024];
	char	outputfile[1024];
	int	ret;
	int   buffsize;
	char readbuff[4096];

	memset((char *)&sad,0,sizeof(sad)); /* clear sockaddr structure	*/
	sad.sin_family = AF_INET;	    /* set family to Internet	*/

	/* Check command-line argument for protocol port and extract	*/
	/* port number if one is specified.  Otherwise, use the default	*/
	/* port value given by constant PROTOPORT			*/



	if (argc >= 2) {
		port = atoi(argv[2]);
		host = argv[1];		/* if host argument specified	*/
	} else {
		host = localhost;
	}

	if (argc == 3) {        /* only contain ip and port number*/
		strcpy(inputfile,"echo.txt");
		strcpy(outputfile,"echoout.txt");
		buffsize = 1440;
	}

	if (argc == 4) {        /* 4 arguments in command line*/
		strcpy(outputfile,"echoout.txt");
		if (isdigit(argv[3][0])) /*add input file*/
		{
			buffsize = atoi(argv[3]);
			strcpy(inputfile,"echo.txt");
		}
		else
		{
			strcpy(inputfile,argv[3]); /*add buff size*/
			buffsize = 1440;		
		}
	}

	if (argc == 5) {        /* 5 arguments in command line*/
		strcpy(inputfile,argv[3]);
		if (isdigit(argv[4][0])) /*add output file*/
		{
			buffsize = atoi(argv[4]);
			strcpy(outputfile,"echoout.txt");
		}
		else
		{
			strcpy(outputfile,argv[4]); /*add buff size*/
			buffsize = 1440;		
		}
	}

	if (argc == 6) {			/* if protocol port specified	*/
		strcpy(inputfile, argv[3]);	/* convert to binary		*/
		strcpy(outputfile, argv[4]);
		buffsize = atoi(argv[5]);
	} 

   if (port > 0)			/* test for legal value		*/
		sad.sin_port = htons((u_short)port);
	else {				/* print error message and exit	*/
		fprintf(stderr,"bad port number %s\n",argv[2]);
		exit(1);
	}
	/* Convert host name to equivalent IP address and copy to sad. */

	ptrh = gethostbyname(host);
	if ( ((char *)ptrh) == NULL ) {
		fprintf(stderr,"invalid host: %s\n", host);
		exit(1);
	}
	memcpy(&sad.sin_addr, ptrh->h_addr, ptrh->h_length);

	/* Map TCP transport protocol name to protocol number. */

	if ( ((int)(ptrp = getprotobyname("tcp"))) == 0) {
		fprintf(stderr, "cannot map \"tcp\" to protocol number");
		exit(1);
	}

	/* Create a socket. */

	sd = socket(PF_INET, SOCK_STREAM, ptrp->p_proto);
	if (sd < 0) {
		fprintf(stderr, "socket creation failed\n");
		exit(1);
	}

	/* Connect the socket to the specified server. */

	if (connect(sd, (struct sockaddr *)&sad, sizeof(sad)) < 0) {
		fprintf(stderr,"connect failed\n");
		exit(1);
	}

	FILE *fp,*fp1;
	char* ch;
	if((fp = fopen(inputfile,"r")) == NULL)
	{
		printf("fail loading,no input file exist.\n");
		exit(1);
	}

	if((fp1 = fopen(outputfile,"w")) == NULL)
	{
		printf("fail writing, output file exist.\n");
		return 0;
	}

	else
	{
		bzero(readbuff,4096);
		while(fgets(readbuff,buffsize+1,fp)) /*the loop terminates when fgets return a null pointer, which occurs when it encouters either an end of file or an error.*/
		{
			ret = send(sd,readbuff,strlen(readbuff),0);/*used to transmit a message to another socket*/
			sendcountbit = receivecountbit + ret; /*The receive bit plus the send readbuff*/
			bzero(buf,4096);  /*set up the TCP server*/
			n = recv(sd, buf, sizeof(buf), 0); /*read incoming data from the remote side using the recv()*/
			receivecountbit = receivecountbit + n;    /*The receive bit plus buff size*/
			fprintf(fp1,"%s",buf);
			bzero(readbuff,4096);
		}
		fclose(fp);
		fclose(fp1); 
	}

	printf("Send: %d bits.\n",sendcountbit*8);
	printf("Receive: %d bits.\n",receivecountbit*8);

	/* Close the socket. */

	closesocket(sd);

	/* Terminate the client program gracefully. */

	exit(0);
}

