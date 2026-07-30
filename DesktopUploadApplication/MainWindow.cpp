#include "MainWindow.h"
#include "Drive.h"
#include <iostream>
#include <filesystem>
#include <vector>
#include <string>
#include "helpers.h"
#include <thread>
#include <atomic>
#include <chrono>
namespace fs = std::filesystem;
void onRipDvd(Gtk::ProgressBar* makemkvProgressBar, Gtk::ProgressBar* handbrakeProgressBar);

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
    auto ripProgressPage = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));
    stack->add(*ripProgressPage, "ripProgressPage", "Rip Progress");


    //setup pages
    setupStartPage(startView);
    setupDrivePickerPage(chooseDriveView);
    setupWaitingForDiscPage(insertDiscView); 
    setupRipProgressPage(ripProgressPage);


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
        loadRipProgressPage();
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
//                             Rip Progress Page
// --------------------------------------------------------------------------


void MainWindow::setupRipProgressPage(Gtk::Box* ripProgressView){

    //makemkv progress bar
    Gtk::Label* barLabel1 = Gtk::make_managed<Gtk::Label>("Rip Progress");
    barLabel1->set_halign(Gtk::Align::ALIGN_START);
    barLabel1->set_valign(Gtk::Align::ALIGN_START);
    ripProgressView->pack_start(*barLabel1);

    Gtk::ProgressBar* progressBar1 = Gtk::make_managed<Gtk::ProgressBar>();
    progressBar1->set_halign(Gtk::Align::ALIGN_FILL);
    progressBar1->set_valign(Gtk::Align::ALIGN_START);
    progressBar1->set_hexpand(true);
    ripProgressView->pack_start(*progressBar1);

    //handbrake progress bar
    Gtk::Label* barLabel2 = Gtk::make_managed<Gtk::Label>("Video Re-encode Progress");
    barLabel2->set_halign(Gtk::Align::ALIGN_START);
    barLabel2->set_valign(Gtk::Align::ALIGN_START);
    ripProgressView->pack_start(*barLabel2);

    Gtk::ProgressBar* progressBar2 = Gtk::make_managed<Gtk::ProgressBar>();
    progressBar2->set_halign(Gtk::Align::ALIGN_FILL);
    progressBar2->set_valign(Gtk::Align::ALIGN_START);
    progressBar2->set_hexpand(true);
    ripProgressView->pack_start(*progressBar2);

    this->makemkvProgressBar = progressBar1;
    this->handbrakeProgressBar = progressBar2;

}


void MainWindow::loadRipProgressPage(){
    this->stack->set_visible_child("ripProgressPage");
    std::thread t(&MainWindow::onRipDvd, this);
    t.detach();
}


// --------------------------------------------------------------------------
//                          Movie Video Options Page
// --------------------------------------------------------------------------


void MainWindow::setupMovieVideoOptionsPage(Gtk::Box* movieVideoOptionsView, std::string vidFile){

    //TODO Video player

    //barLabel1->set_halign(Gtk::Align::ALIGN_START);
    //barLabel1->set_valign(Gtk::Align::ALIGN_START);
    //ripProgressView->pack_start(*barLabel1);

    //Radio button labels
    Gtk::Label* radioLabel = Gtk::make_managed<Gtk::Label>("Choose what type of content this video is");
    movieVideoOptionsView->pack_start(*radioLabel);


    //radio buttons
    Gtk::RadioButton* b1 = Gtk::make_managed<Gtk::RadioButton>("Movie Video");
    Gtk::RadioButton* b2 = Gtk::make_managed<Gtk::RadioButton>("Bonus Content / Ads");
    b2->join_group(*b1);
    movieVideoOptionsView->pack_start(*b1);
    movieVideoOptionsView->pack_start(*b2);


    //bottom page buttons
    Gtk::Button* skipButton = Gtk::make_managed<Gtk::Button>("Skip");
    skipButton->signal_clicked().connect(sigc::mem_fun(*this, &MainWindow::onSkip));
    movieVideoOptionsView->pack_start(*skipButton);
    Gtk::Button* nextButton = Gtk::make_managed<Gtk::Button>("Next");
    nextButton->signal_clicked().connect(sigc::mem_fun(*this, &MainWindow::onNext));
    movieVideoOptionsView->pack_start(*nextButton);
    Gtk::Button* backButton = Gtk::make_managed<Gtk::Button>("Back");
    backButton->signal_clicked().connect(sigc::mem_fun(*this, &MainWindow::onBack));
    movieVideoOptionsView->pack_start(*backButton);

}

void MainWindow::loadMovieVideoOptionsPage(int currentPos){

    if(currentPos >= this->movieVideoOptionsPages.size()){
        currentPos = this->movieVideoOptionsPages.size() - 1;
    }
    else if(currentPos < 0){
        currentPos = 0;
    }
    this->stack->set_visible_child(this->movieVideoOptionsPages.at(currentPos));

}


void MainWindow::onNext(){

    //TODO save selected options

    this->onSkip();

}


void MainWindow::onBack(){
    if(this->currentPos >= 1){
        this->currentPos--;
        loadMovieVideoOptionsPage(currentPos);
    }
}

void MainWindow::onSkip(){
    if(currentPos >= this->movieVideoOptionsPages.size()){
        if(this->selectedMovieVideo == nullptr){
            //errorDialog();
        }
        else{
            //TODO load next
        }
    }
    else{
        this->currentPos++;
        loadMovieVideoOptionsPage(currentPos);
    }
}

void errorDialog(){


}



// --------------------------------------------------------------------------
//                          Movie Details Page
// --------------------------------------------------------------------------

/*
void setupMovieDetailsPage(Gtk::Box* movieDetailsView){

    //title
    //genre

    Gtk::Label* movieTitleEntryLabel = Gtk::make_managed<Gtk::Label>("Enter Movie Title");
    Gtk::Entry* movieTitle = Gtk::make_managed<Gtk::Entry>();
    //barLabel1->set_halign(Gtk::Align::ALIGN_START);
    //barLabel1->set_valign(Gtk::Align::ALIGN_START);
    //ripProgressView->pack_start(*barLabel1);


    Gtk::Label* movieTitleEntryLabel = Gtk::make_managed<Gtk::Label>("Enter Movie Genre");
    Gtk::Entry* movieTitle = Gtk::make_managed<Gtk::Entry>();

    Gtk::RadioButton* b1 = Gtk::make_managed<Gtk::RadioButton>();

    Gtk::RadioButton* b2 = Gtk::make_managed<Gtk::RadioButton>();

    b2->join_group(*b1);


    Gtk::Button* skipButton = Gtk::make_managed<Gtk::Button>();
    Gtk::Button* nextButton = Gtk::make_managed<Gtk::Button>();
    Gtk::Button* backButton = Gtk::make_managed<Gtk::Button>();


}*/


//void MainWindow::loadMovieVideoOptionsPage(int currentPos){

//}


// --------------------------------------------------------------------------
//                                      RIP
// --------------------------------------------------------------------------


void setProgressBar(Gtk::ProgressBar* bar, double percent){
    Glib::signal_idle().connect([bar, percent]() {
        bar->set_fraction(percent);
        return false;
    });
}


//TODO make this handle handbrake's stupid ass carriage return chars
void handbrakeProcessThread(std::atomic<int>* progress, std::string command, int taskCount){

    command = command + " 2>&1";
    int bufferSize = 2048;
    double lastPercent = 0.0;
    FILE* pipe = popen(command.c_str(), "r");
    char buffer[bufferSize];
    char ch;
    int i = 0;
    int addedPercent = 0;
    while((ch = fgetc(pipe)) != EOF){

        if(ch == '\r' || ch == '\n'){

            buffer[i] = '\0';
            std::string s = std::string(buffer);
            std::cout << "s: " << s << "\n";


            //TODO clean up
            std::vector<std::string> vec = splitString(s, 'k');
            if(vec.size() > 0 && vec.at(0) == "Encoding: tas"){
                double percent = std::stod(splitString(s, ' ').at(5)) * 100;
                //std::cout << "int: " << int((percent - lastPercent) / taskCount) << "\n";
                progress->fetch_add(int((percent - lastPercent) / taskCount));
                addedPercent += int((percent - lastPercent) / taskCount);
                lastPercent = percent;
            }
            i = 0;
        }
        else if(i < bufferSize - 1){
            buffer[i] = ch;
            i++;
        }

    }

    std::cout << "last percent: " << lastPercent << "\n";
    std::cout << "taskCount: " << taskCount << "\n";
    std::cout << "int((10000.0 - lastPercent) / taskCount): " << int((10000.0 - lastPercent) / taskCount) << "\n";
    progress->fetch_add(int((10000.0 - lastPercent) / taskCount));
    addedPercent += int((10000.0 - lastPercent) / taskCount);
    std::cout << "thread done: " << addedPercent << "\n";
}


bool checkAllThreadsJoinable(std::vector<std::thread>& threads){

    for(std::thread& t : threads){
        if(!t.joinable()){
            return false;
        }
    }
    return true;

}


void MainWindow::onRipDvd() {
    std::cout << "Ripping DVD..." << std::endl;
    setProgressBar(makemkvProgressBar, 0.0);
    //sg cdrom -c "makemkvcon mkv dev:/dev/sr0 all temptest --progress=-stdout"

    
    //run makemkvcon
    char buffer[2048];
    std::string device = "/dev/sr0";
    std::string workingDirString = "/home/light/Desktop/dvdstreaming/DesktopUploadApplication/temptest";
    /*
    std::string ripCommand = "sg cdrom -c \"makemkvcon mkv dev:" + device + " all " + workingDirString + " --progress=-stdout\"";
    std::cout << "command: " << ripCommand << "\n";
    FILE* pipe = popen(ripCommand.c_str(), "r");

    if(!pipe){
        //TODO errory
    }

    try{
        int stage = 0;
        double lastPercent = 0.0;
        while(fgets(buffer, sizeof buffer, pipe) != nullptr){

            std::string s = std::string(buffer);
            std::cout << "\"" << s << "\"\n";

            if(splitString(s, '-').at(0) == "Current progress "){

                //std::cout << "here1\n";
                std::string subPercentString = splitString(s, ' ').at(8);
                //std::cout << "here2\n";
                double subPercent = std::stod(splitString(subPercentString, '%').at(0)) / 100.0;
                std::cout << "subPercent: " << subPercent << "\n";
                //std::cout << "here3\n";
                double finalPercent = 0.0;
                if(stage == 1){
                    finalPercent = subPercent * 0.05;
                }
                if(stage == 2){
                    finalPercent = (subPercent * 0.95) + 0.05;
                }

                std::cout << "percent: " << finalPercent << "\n";
                if(lastPercent != finalPercent){
                    setProgressBar(makemkvProgressBar, finalPercent);
                }
                lastPercent = finalPercent;
            }
            else if(s == "Current action: Processing title sets\n"){
                std::cout << "stage1\n";
                stage = 1;
            }
            else if(s == "Current operation: Saving all titles to MKV files\n"){
                std::cout << "stage2\n";
                stage = 2;
            }


            
            Glib::signal_idle().connect([makemkvProgressBar]() {
                makemkvProgressBar->pulse();
                return false;
            });
            
           std::cout << "end1\n";
        }

    }
    catch(const std::exception& e){
        pclose(pipe);
        //TODO error
    }
    pclose(pipe);
    */

    setProgressBar(makemkvProgressBar, 1.0);



    //when mkv done run handbrake


    //HandBrakeCLI -i file.mkv -o 0.mp4




    //create handbrake threads
    std::vector<std::thread> threads = std::vector<std::thread>();
    std::vector<std::string> files = std::vector<std::string>();
    fs::directory_entry workingDir{workingDirString};
    int i = 0;
    std::atomic<int>* progress = new std::atomic<int>(0);
    for(const auto& entry : fs::directory_iterator(workingDirString)){
        std::string handbrakeCommand = "HandBrakeCLI -i \"" + entry.path().string() + "\" -o " + std::to_string(i) + ".mp4 | cat";
        std::thread t(handbrakeProcessThread, progress, handbrakeCommand, 12);
        t.detach();
        threads.push_back(std::move(t));
        files.push_back(workingDirString + std::to_string(i) + ".mp4");
        i++;
    }


    //TODO make its own thread?
    //create video pages for when handbrake completes
    std::vector<std::string> videoPagesStrings = std::vector<std::string>();
    for(int i = 0; i < files.size(); i++){
        std::string pageName = "movieVideoPage" + std::to_string(i);
        videoPagesStrings.push_back(pageName);
    }
    this->movieVideoOptionsPages = videoPagesStrings;
    Glib::signal_idle().connect([this, files]() {
        for(int i = 0; i < this->movieVideoOptionsPages.size(); i++){
            auto vidPage = Gtk::manage(new Gtk::Box(Gtk::ORIENTATION_VERTICAL, 10));
            stack->add(*vidPage, this->movieVideoOptionsPages.at(i), "Video Details Page " + std::to_string(i));
            this->setupMovieVideoOptionsPage(vidPage, files.at(i));
        }
    });

    
    //handle handbrake progress bar. Check if threads are finished.
    int prog = 0;
    while(true){
        std::chrono::_V2::system_clock::time_point time = std::chrono::system_clock::now();
        prog = *progress;
        setProgressBar(handbrakeProgressBar, double(prog) / double(10000));
        if(checkAllThreadsJoinable(threads)){
            break;
        }
        std::cout << "percent" << double(prog) / double(10000) << "\n";
        std::this_thread::sleep_until(time + std::chrono::milliseconds(500));
    }


    //load next page
    std::cout << "done!\n";
    this->loadMovieVideoOptionsPage(0);


}
