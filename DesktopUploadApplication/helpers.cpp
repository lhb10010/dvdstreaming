#include <vector>
#include <string>
#include <iostream>
#include "helpers.h"


std::vector<std::string> splitString(std::string s, char c){

    std::vector<std::string> stringVec = std::vector<std::string>();
    char* currentString = new char[s.length()];

    int j = 0;
    for(int i = 0; i < s.length(); i++){
        if(s.at(i) == c){
            currentString[j] = '\0';
            if(j > 0){
                stringVec.push_back(std::string(currentString));
            }
            j = 0;
        }
        else{
            currentString[j] = s.at(i);
            j++;
        }
    }

    currentString[j] = '\0';
    if(j > 0){
        stringVec.push_back(std::string(currentString));
    }
    delete[] currentString;

    return stringVec;
}


