#include "MainWindow.h"
#include "Drive.h"
#include <iostream>
#include <filesystem>
#include <vector>
#include <string>
namespace fs = std::filesystem;

MainWindow::MainWindow()
: m_vbox(false, 10) // false = not homogeneous, 10 = spacing
{
    set_title("DVD Ripper");
    set_default_size(1000, 1000);

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
    this->stack = Gtk::manage(new Gtk::Stack());


    //create pages
    auto startView = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));
    stack->add(*startView, "startPage", "Start Page");
    auto chooseDriveView = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));
    stack->add(*chooseDriveView, "drivePickerPage", "Drive Chooser Page");
    auto insertDiscView = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));
    stack->add(*insertDiscView, "insertDiscPage", "Insert Disc Page");


    //setup pages
    setupStartPage(startView);
    setupDrivePickerPage(chooseDriveView);
    setupWaitingForDiscPage(insertDiscView); 


    m_vbox.pack_start(m_menubar, Gtk::PACK_SHRINK);
    m_vbox.pack_start(*stack, Gtk::PACK_EXPAND_WIDGET);

    add(m_vbox);
    show_all_children(); 

    this->stack->set_visible_child("startPage");
}


// --------------------------------------------------------------------------
//                                Start Page
// --------------------------------------------------------------------------


void MainWindow::setupStartPage(Gtk::Box* startView){
    Gtk::Button* nextPageButton = Gtk::make_managed<Gtk::Button>("Start Ripping Discs");
    nextPageButton->set_halign(Gtk::Align::ALIGN_CENTER);
    nextPageButton->set_valign(Gtk::Align::ALIGN_CENTER);
    startView->pack_start(*nextPageButton);
    nextPageButton->signal_clicked().connect(sigc::mem_fun(*this, &MainWindow::loadChooseDriveView));
}


// --------------------------------------------------------------------------
//                              Driver Picker Page
// --------------------------------------------------------------------------


void MainWindow::setupDrivePickerPage(Gtk::Box* chooseDriveView){


    //setup next page button
    Gtk::Button* nextPageButton = Gtk::make_managed<Gtk::Button>("Next");
    nextPageButton->set_halign(Gtk::Align::ALIGN_END);
    chooseDriveView->pack_start(*nextPageButton);
    nextPageButton->set_sensitive(false);
    nextPageButton->set_tooltip_text("No drive selected. Choose a drive before continuing.");
    nextPageButton->signal_clicked().connect([this](){
        this->checkIfDiscInDriveAndLoadNextPage();
    });


    //setup columns
    DrivePickerTableColumns pickerTableColumns;
    Glib::RefPtr<Gtk::ListStore> pickerTableModel = Gtk::ListStore::create(pickerTableColumns);
    this->pickerTableModel = pickerTableModel;
    Gtk::TreeView* pickerTableWidget = Gtk::make_managed<Gtk::TreeView>();
    pickerTableWidget->set_model(pickerTableModel);

    //setup drive name collumn
    Gtk::CellRendererText* renderDriveName = Gtk::make_managed<Gtk::CellRendererText>();
    Gtk::TreeView::Column* driveName = Gtk::make_managed<Gtk::TreeView::Column>();
    driveName->set_title("Drive Name");
    driveName->pack_start(*renderDriveName, true);
    driveName->add_attribute(renderDriveName->property_text(), pickerTableColumns.driveName);
    //driveName->set_fixed_width(50);
    pickerTableWidget->append_column(*driveName);

    //setup has disk column
    Gtk::CellRendererText* renderHasDisk = Gtk::make_managed<Gtk::CellRendererText>();
    Gtk::TreeView::Column* hasDisk = Gtk::make_managed<Gtk::TreeView::Column>();
    hasDisk->set_title("Has Disk?");
    hasDisk->pack_start(*renderHasDisk, true);
    hasDisk->add_attribute(renderHasDisk->property_text(), pickerTableColumns.hasDisk);
    pickerTableWidget->append_column(*hasDisk);

    //setup disk name collumn
    Gtk::CellRendererText* renderDiskName = Gtk::make_managed<Gtk::CellRendererText>();
    Gtk::TreeView::Column* diskName = Gtk::make_managed<Gtk::TreeView::Column>();
    diskName->set_title("Disk Name");
    diskName->pack_start(*renderDiskName, true);
    diskName->add_attribute(renderDiskName->property_text(), pickerTableColumns.diskName);
    pickerTableWidget->append_column(*diskName);

    //setup drive capabilities collumn
    Gtk::CellRendererText* renderDriveCapabilities = Gtk::make_managed<Gtk::CellRendererText>();
    Gtk::TreeView::Column* driveCapabilities = Gtk::make_managed<Gtk::TreeView::Column>();
    driveCapabilities->set_title("Capabilities");
    driveCapabilities->pack_start(*renderDriveCapabilities, true);
    driveCapabilities->add_attribute(renderDriveCapabilities->property_text(), pickerTableColumns.driveCapabilities);
    pickerTableWidget->append_column(*driveCapabilities);


    //setup selection
    Glib::RefPtr<Gtk::TreeSelection> tableSelection = pickerTableWidget->get_selection();
    this->tableSelection = tableSelection;
    tableSelection->set_mode(Gtk::SELECTION_SINGLE);
    tableSelection->set_select_function(
        [pickerTableModel, &pickerTableColumns]
        (const Glib::RefPtr<Gtk::TreeModel>& model, const Gtk::TreeModel::Path& path, bool path_currently_selected) -> bool {

            return true;

        }
    );
    tableSelection->signal_changed().connect(
        [pickerTableWidget, pickerTableModel, &pickerTableColumns, tableSelection, this, nextPageButton](){
            int selectedCount = tableSelection->count_selected_rows();
            if(selectedCount == 0){
                std::cout << "no selection\n";
                nextPageButton->set_sensitive(false);
                nextPageButton->set_tooltip_text("No drive selected. Choose a drive before continuing.");
            }
            else{
                std::cout << "new selection\n";
                this->selectedDrive = Drive::getExistingDriveByPath("/dev/sr0"); //TODO make
                nextPageButton->set_sensitive(true);
                nextPageButton->set_tooltip_text("Start uploading media in the selected drive.");
            }
        }

        
    );

    chooseDriveView->pack_start(*pickerTableWidget);
    pickerTableWidget->set_hexpand(true);
    pickerTableWidget->set_vexpand(true);


}


void MainWindow::checkIfDiscInDriveAndLoadNextPage(){
    if(this->selectedDrive == nullptr){
        loadChooseDriveView();
    }
    else if(this->selectedDrive->hasDisk()){
        //TODO rip
    }
    else{
        loadWaitingForDiscPage();
    }
}


void MainWindow::loadChooseDriveView(){

    DrivePickerTableColumns pickerTableColumns;
    std::vector<Drive*> drives = Drive::loadAllDrives();

    for(Drive* d : drives){
        Gtk::TreeModel::iterator iter = this->pickerTableModel->append();
        Gtk::TreeModel::Row row = *iter;

        row[pickerTableColumns.driveName] = d->getPath();
        row[pickerTableColumns.driveCapabilities] = d->getCapabilitiesAsString() + " Drive";
        row[pickerTableColumns.hasDisk] = true; //TODO make real
        row[pickerTableColumns.diskName] = "Placeholder name"; 
    }

    this->stack->set_visible_child("drivePickerPage");
    this->tableSelection->unselect_all();

}


// --------------------------------------------------------------------------
//                           Waiting For Disc Page
// --------------------------------------------------------------------------


void MainWindow::setupWaitingForDiscPage(Gtk::Box* insertDiscView){
    Gtk::Label* tempText = Gtk::make_managed<Gtk::Label>("Insert Disc Into Drive");
    tempText->set_halign(Gtk::Align::ALIGN_CENTER);
    tempText->set_valign(Gtk::Align::ALIGN_CENTER);
    insertDiscView->pack_start(*tempText);
}


void MainWindow::loadWaitingForDiscPage(){
    this->stack->set_visible_child("insertDiscPage");
}


// --------------------------------------------------------------------------
//                                      RIP
// --------------------------------------------------------------------------


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