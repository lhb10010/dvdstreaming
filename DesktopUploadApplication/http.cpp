#include "http.h"
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <cstring>


//constructor
http::http(std::string ip, int port){
    this->ip = ip;
    this->port = port;
}

int http::createSocket(){

    int s = socket(AF_INET, SOCK_STREAM, 0);
    if(s == -1){
        return -1;
    }

    //setup server info struct
    struct sockaddr_in serverStruct;
    serverStruct.sin_family = AF_INET;
    serverStruct.sin_port = htons(this->port);


    //resolve to IP
    struct hostent* serverIp = gethostbyname(this->ip.c_str());
    if(serverIp == nullptr){
        return -1;
    }
    memcpy(&serverStruct.sin_addr.s_addr, serverIp->h_addr, serverIp->h_length);


    int c = connect(s, (struct sockaddr*)&serverStruct, sizeof(serverStruct));
    if(c == -1){
        return -1;
    }

    return s;

}