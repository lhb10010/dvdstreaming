#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <gtkmm.h>
#include "Drive.h"


class MainWindow : public Gtk::Window{

    public:
        MainWindow();
        virtual ~MainWindow() = default;
        void loadChooseDriveView();
        void loadWaitingForDiscPage();


        //constants
        static const int MOVIE = 0;
        static const int SERIES = 1;

    private:

        
        Gtk::Stack* stack;
        Drive* selectedDrive = nullptr;
        int mediaType = MOVIE;


        //start page 
        void setupStartPage(Gtk::Box* startView);


        //drive picker page
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


        //disk waiting page
        void setupWaitingForDiscPage(Gtk::Box* insertDiscView);

        //rip progress page
        void setupRipProgressPage(Gtk::Box* insertDiscView);
        void loadRipProgressPage();
        Gtk::ProgressBar* makemkvProgressBar;
        Gtk::ProgressBar* handbrakeProgressBar;
        void onRipDvd();

        //movie video options pages
        void setupMovieVideoOptionsPage(Gtk::Box* movieVideoOptionsView, std::string vidFile);
        void loadMovieVideoOptionsPage(int currentPos);
        void onNext();
        void onBack();
        void onSkip();
        int currentPos = 0;
        std::vector<std::string> movieVideoOptionsPages;
        std::string* selectedMovieVideo = nullptr;
        std::vector<std::string> selectedSpecials = std::vector<std::string>();
        

    //window
    Gtk::VBox m_vbox;

    //menu bar on every page
    Gtk::MenuBar m_menubar;

    //start page
    Gtk::Button m_ripDvdButton;



};


#endif