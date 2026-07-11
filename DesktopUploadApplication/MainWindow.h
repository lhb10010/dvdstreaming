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

    private:

        void onRipDvd();
        Gtk::Stack* stack;
        Drive* selectedDrive = nullptr;


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
        

    //window
    Gtk::VBox m_vbox;

    //menu bar on every page
    Gtk::MenuBar m_menubar;

    //start page
    Gtk::Button m_ripDvdButton;



};


#endif