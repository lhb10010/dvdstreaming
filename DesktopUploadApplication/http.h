#ifndef HTTP_H
#define HTTP_H

#include <string>

class http{

    public:

        http(std::string ip, int port);
        std::string requestCreateMovie();
        std::string requestCreateVideo();
        int sendCreateMovieRequest(std::string title, std::string genre, unsigned char* imageData, int imageDataLen);
        int uploadVideo(std::string vidFilePath);


    private:

        std::string ip;
        int port;
        int createSocket();





};


#endif