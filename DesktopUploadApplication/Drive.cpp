#include "Drive.h"
#include <string>
#include "helpers.h"
#include <iostream>

int CAN_READ_CD = 0;
int CAN_READ_DVD = 1;
int CAN_READ_BD = 2;
int NO_READ = -1;
std::vector<Drive*> Drive::loadedDrives;


std::string executeCommand(std::string command){
    char buffer[1024];
    std::string result = "";
    FILE* pipe = popen(command.c_str(), "r");
    if(!pipe){
        return "";
    }

    try{
        while(fgets(buffer, sizeof buffer, pipe) != nullptr){
            result += buffer;
        }
    }
    catch(const std::exception& e){
        pclose(pipe);
        return "";
    }
    pclose(pipe);
    return result;
}


Drive::Drive(std::string drivePath){
    this->path = drivePath;
    loadCapabilities();
}


Drive::~Drive(){

}


void Drive::loadCapabilities(){

    std::string command = "udevadm info --query=property --name=" + this->path;
    std::string result = executeCommand(command);
    auto lines = splitString(result, '\n');
    int cap = NO_READ;

    for(auto line : lines){
        if(line == "ID_CDROM_CD=1" && cap < 0){
            cap = CAN_READ_CD;
        }
        if(line == "ID_CDROM_DVD=1" && cap < 1){
            cap = CAN_READ_DVD;
        }
        if(line == "ID_CDROM_BD=1"){
            cap = CAN_READ_BD;
            break;
        }
    }

    this->capabilities = cap;

}

bool Drive::canReadCD(){
    if(this->capabilities >= 0){
        return true;
    }
    return false;
}


bool Drive::canReadDVD(){
    if(this->capabilities >= 1){
        return true;
    }
    return false;
}


bool Drive::canReadBD(){
    if(this->capabilities == 2){
        return true;
    }
    return false;
}



void Drive::loadName(){

    /*
    //CINFO:2,0,"Spider-Man 2 (Special Edition)"

    std::string command = "sudo makemkvcon info -r dev:" + this->path;
    std::string result = executeCommand(command);
    auto lines = result | std::views::split('\n');

    for(auto line : lines){
        auto sides = line | std::views::split("");

    }
        */
}


std::string Drive::getPath(){
    return this->path;
}


std::vector<Drive*> Drive::loadAllDrives(){


    //Drive vector
    std::vector<Drive*> drives = std::vector<Drive*>();


    //setup and execute lsblk
    std::string command = "lsblk -n -o NAME -p -i";
    std::string result = executeCommand(command);

    //loop each result
    auto lines = splitString(result, '\n');
    for(std::string line : lines){
        std::string match = "/dev/sr";
        if(line.length() < match.length()){
            continue;
        }
        bool matches = true;
        for(int i = 0; i < match.length(); i++){
            if(line.at(i) != match.at(i)){
                matches = false;
                break;
            }
        }
        if(matches){
            Drive* d = new Drive(line);
            drives.push_back(d);
        }
    }


    loadedDrives = drives;
    return drives;
}


Drive* Drive::getExistingDriveByPath(std::string path){
    for(Drive* d : loadedDrives){
        if(d->path == path){
            return d;
        }
    }
    return nullptr;
}


std::string Drive::getCapabilitiesAsString(){
    if(this->capabilities == 2){
        return "Blu-Ray";
    }
    else if(this->capabilities == 1){
        return "DVD";
    }
    else if(this->capabilities == 0){
        return "CD";
    }
    return "Unknown";
}


bool Drive::hasDisk(){
    return false; //TODO make real
}


std::vector<Drive*> Drive::getDriveList(){
    return loadedDrives;
}

