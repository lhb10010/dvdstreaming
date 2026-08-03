#include "http.h"
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <cstring>
#include <string>
#include "helpers.h"
#include <iostream>


//constructor
http::http(std::string ip, int port){
    this->ip = ip;
    this->port = port;
}


std::string toBase64(unsigned char* data, int len){
    return "data"; //temp
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


bool sendData(int socket, std::string data){
    
    int sent = send(socket, data.c_str(), data.length(), 0);
    if(sent == -1){
        return false;
    }
    return true;
}


/*
std::string recvData(int socket){

    int BUFFER_SIZE = 1024;
    char* buffer = new char[BUFFER_SIZE];

    //parse headers
    int pos = 0;
    for(int i = 0; i < BUFFER_SIZE - 1, i++){
        if(buffer[i] == '\r' && pos = 0){
            pos++;
        }
        else if(buffer[i] == '\n' && pos = 1){
            pos++;
        }
        else if(buffer[i] == '\r' && pos = 2){
            pos++;
        }
        else if(buffer[i] == '\n' && pos = 3){

            char* smallBuffer = new char[i + 2];
            for(int j = 0; j < i + 1; j++){
                smallBuffer[j] = buffer[j];
            }
            smallBuffer[i + 1] = '\0'

        }
        else{
            pos = 0;
        }
    }


}


std::string recvData(int socket){

    int BUFFER_SIZE = 1024;
    char* buffer = new char[BUFFER_SIZE];
    std::string headers = "";
    std::string data;
    int contentLen = 0;
    //parse headers
    int loops = 0;
    int startPos = 0;
    while(true){
        //recv
        std::string s = std::string(buffer);
        headers += s;


        startPos = loops * BUFFER_SIZE;
        if(startPos > 3)
            startPos -= 4;
        loops++;

        int pos = headers.find("\r\n\r\n", startPos);
        if(pos != std::string::npos){
            headers = headers.substr(0, pos);
            std::vector<std::string> headerVec = splitString(headers, '\n');
            for(int i = 0; i < headerVec.size(); i++){
                if(splitString(headerVec.at(i), ':')[0] == "Content-Length"){
                    contentLen = std::stoi(splitString(headerVec.at(i), ':')[1]);
                }
            }

        }
    }
}
    */


std::string recvData(int socket){

    int bytesRecv = 1;
    char* buffer = new char[1024];
    std::string response = "";

    while(bytesRecv > 0){
        bytesRecv = recv(socket, buffer, 1023, 0);
        buffer[bytesRecv] = '\0';
        response += std::string(buffer);
    }
     
    int headerEnd = response.find("\r\n\r\n", 0) + 4;
    return response.substr(headerEnd, (response.length() - headerEnd));
}


int http::sendCreateMovieRequest(std::string title, std::string genre, unsigned char* imageData, int imageDataLen){

    int MAX_ATTEMPTS = 5;


    std::string data = "{\"title\":\"" + title + "\",\"genre\":\"" + genre + "\",\"image\":\"" + toBase64(imageData, imageDataLen) + "\"}";
    std::string headers = "POST /upload/movie HTTP/1.1\r\nHost:" + this->ip + ":" + std::to_string(this->port) + "\r\nConnection: close\r\nContent-Length:" + std::to_string(data.length()) + "\r\nUser-Agent: MediaUploadApp\r\n\r\n"; //content length
    std::string combined = headers + data;

    int s = createSocket();

    bool sent = false;
    int attempts = 0;
    while(!sent){
        if(attempts > MAX_ATTEMPTS){
            return -1;
        }
        sent = sendData(s, combined);
        attempts++;
    }
    
    std::string data = recvData(s);
    std::cout << data << "\n";

    return 1;

}


int http::uploadVideo(std::string vidFilePath){

    

}
