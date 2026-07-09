#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <gtkmm.h>

class MainWindow : public Gtk::Window{

    public:
        MainWindow();
        virtual ~MainWindow() = default;
        void toChooseDriveView();

    private:

        void onRipDvd();
        auto switcher;

    //window
    Gtk::VBox m_vbox;

    //menu bar on every page
    Gtk::MenuBar m_menubar;

    //start page
    Gtk::Button m_ripDvdButton;



};


#endif