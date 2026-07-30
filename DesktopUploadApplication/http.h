#ifndef HTTP_H
#define HTTP_H

#include <string>

class http{

    public:

        http(std::string ip, int port);
        std::string requestCreateMovie();
        std::string requestCreateVideo();


    private:

        std::string ip;
        int port;
        int createSocket();


};


#endif