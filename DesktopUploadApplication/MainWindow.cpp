#include "MainWindow.h"
#include "Drive.h"
#include <iostream>
#include <filesystem>
#include <vector>
#include <string>
namespace fs = std::filesystem;

MainWindow::MainWindow()
: m_vbox(false, 10),  // false = not homogeneous, 10 = spacing
  m_ripDvdButton("Rip DVD Using DVD Drive")
{
    set_title("DVD Ripper");
    set_default_size(400, 300);

    // Build a simple File menu with Quit option
    Gtk::MenuItem* menu_file = Gtk::manage(new Gtk::MenuItem("File"));
    Gtk::Menu* submenu_file = Gtk::manage(new Gtk::Menu());
    Gtk::MenuItem* menu_quit = Gtk::manage(new Gtk::MenuItem("Quit"));


    //menu
    submenu_file->append(*menu_quit);
    menu_file->set_submenu(*submenu_file);
    m_menubar.append(*menu_file);
    menu_quit->signal_activate().connect(sigc::mem_fun(*this, &MainWindow::hide));


    //create stack
    Gtk::Stack* stack = Gtk::manage(new Gtk::Stack());
    this->switcher = Gtk::manage(new Gtk::StackSwitcher());
    this->switcher->set_stack(*stack);


    //create pages
    auto mainView = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));
    auto chooseDriveView = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));


    //start page
    m_ripDvdButton.signal_clicked().connect(sigc::mem_fun(*this, &MainWindow::toChooseDriveView));


    //drive picker page
    Gtk::ListBox* driveListBox = Gtk::manage(new Gtk::ListBox());
    chooseDriveView->pack_start(driveListBox, Gtk::PACK_SHRINK);




    // Layout - GTKmm 3 uses pack_start instead of append
    m_vbox.pack_start(m_menubar, Gtk::PACK_SHRINK);
    m_vbox.pack_start(switcher, Gtk::PACK_SHRINK);

    add(m_vbox);  // GTKmm 3 uses add(), not set_child()

    show_all_children();  // GTKmm 3 needs this
}


void MainWindow::toChooseDriveView(){
    std::vector<Drive> drives = getDriveList();
    for(Drive d : drives){

    }
    this->switcher.set_visible_child("view_name");
    
}


void MainWindow::toChooseDriveView(){
    this->switcher.set_visible_child("view_name");

}

void MainWindow::onRipDvd() {
    std::cout << "Ripping DVD..." << std::endl;
    //"sg cdrom -c \"makemkvcon --robot mkv dev:" + device + " all " + mkvOutputDirectory + "\"",


    //run makemkvcon
    char buffer[256];
    std::string device = "";
    std::string workingDirString = "";
    std::string ripCommand = "sg cdrom -c \"makemkvcon --robot mkv dev:" + device + " all " + workingDirString + "\"";
    FILE* pipe = popen(ripCommand.c_str(), "r");

    if(!pipe){
        //TODO error
    }

    try{
        while(fgets(buffer, sizeof buffer, pipe) != nullptr){
            std::cout << std::string(buffer);
        }

    }
    catch(const std::exception& e){
        pclose(pipe);
        //TODO error
    }
    pclose(pipe);


    //when mkv done run handbrake
    std::vector<std::string> files = std::vector<std::string>();
    fs::directory_entry workingDir{workingDirString};
    int i = 0;
    for(const auto& entry : fs::directory_iterator(workingDirString)){
        std::string handbrakeCommand = "HandBrakeCLI -i " + entry.path().string() + " -o " + std::to_string(i) + ".mp4";
        FILE* pipe = popen(handbrakeCommand.c_str(), "r");
        files.push_back(workingDirString + std::to_string(i) + ".mp4");
        i++;
    }




}