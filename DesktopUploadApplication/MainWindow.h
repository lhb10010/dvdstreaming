#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <gtkmm.h>
#include "Drive.h"
#include "StartPage.h"
#include "DrivePickerPage.h"
#include "MovieVideoOptionsPage.h"
#include "RipProgressPage.h"
#include "WaitingForDiscPage.h"
#include "Movie.h"


class MainWindow : public Gtk::Window{

    public:

        //contructors and deconstructors
        MainWindow();
        virtual ~MainWindow() = default;


        //getters and setters
        Gtk::Stack* getStack();
        Drive* getSelectedDrive();
        void setSelectedDrive(Drive* drive);
        Movie* getMovie();
        void setMovie(Movie* m);
        void setMovieVideoOptionsPagesVector(std::vector<MovieVideoOptionsPage*> v);
        void addVideoToExtrasList(std::string vidPath);
        void removeVideoFromExtrasList(std::string vidPath);
        void setMovieVideo(std::string vidPath);
        std::string getMovieVideo();


        //load pages
        void loadDrivePickerPage();
        void loadWaitingForDiscPage();
        void loadStartPage();
        void loadRipProgressPage();


        //constants
        static const int MOVIE = 0;
        static const int SERIES = 1;

    private:


        //page objects
        StartPage* startPage;
        DrivePickerPage* pickerPage;
        WaitingForDiscPage* waitingPage;
        RipProgressPage* progressPage;
        std::vector<MovieVideoOptionsPage*> movieVideoOptionsPages; 


        //GTK members
        Gtk::Stack* stack;
        Gtk::VBox m_vbox;
        Gtk::MenuBar m_menubar;


        //other Data
        Drive* selectedDrive = nullptr;
        int mediaType = MOVIE;
        std::string movieVideo;
        std::vector<std::string> dvdExtrasVideoPaths = std::vector<std::string>();
        Movie* targetMovie;
        


        //start page 
        //void setupStartPage(Gtk::Box* startView);


        //drive picker page
        /*
        void setupDrivePickerPage(Gtk::Box* chooseDriveView);
        struct DrivePickerTableColumns : Gtk::TreeModel::ColumnRecord{
            Gtk::TreeModelColumn<std::string> driveName;
            Gtk::TreeModelColumn<bool> hasDisk;
            Gtk::TreeModelColumn<std::string> diskName;
            Gtk::TreeModelColumn<std::string> driveCapabilities;
            DrivePickerTableColumns(){
                add(driveName);
                add(hasDisk);
                add(diskName);
                add(driveCapabilities);
                }
        };
        Glib::RefPtr<Gtk::ListStore> pickerTableModel;
        Glib::RefPtr<Gtk::TreeSelection> tableSelection;
        void checkIfDiscInDriveAndLoadNextPage();
        */


        //disk waiting page
        //void setupWaitingForDiscPage(Gtk::Box* insertDiscView);


        //rip progress page

        //movie video options pages
        /*
        void setupMovieVideoOptionsPage(Gtk::Box* movieVideoOptionsView, std::string vidFile);
        void loadMovieVideoOptionsPage(int currentPos);
        void onNext();
        void onBack();
        void onSkip();
        int currentPos = 0;
        std::vector<std::string> movieVideoOptionsPages;
        */
        std::string* selectedMovieVideo = nullptr;
        std::vector<std::string> selectedSpecials = std::vector<std::string>();
        

    //window
    

    //menu bar on every page
    

    //start page
    //Gtk::Button m_ripDvdButton;



};


#endif