#ifndef DRIVE_H
#define DRIVE_H
#include <vector>
#include <string>

class Drive{

    public:
        Drive(std::string drivePath);
        ~Drive();
        bool canReadCD();
        bool canReadDVD();
        bool canReadBD();
        std::string getPath();
        std::string getCapabilitiesAsString();
        bool hasDisk();

        //static
        static std::vector<Drive*> getDriveList();
        static std::vector<Drive*> loadAllDrives();
        static Drive* getExistingDriveByPath(std::string path);

    private:
        std::string path;
        int capabilities;
        void loadCapabilities();
        void loadName();

        //static
        static std::vector<Drive*> loadedDrives;

};

#endif