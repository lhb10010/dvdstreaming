#include "MainWindow.h"
#include <gtkmm.h>

int main(int argc, char* argv[]) {
    Gtk::Main kit(argc, argv);
    MainWindow window;
    window.show();
    Gtk::Main::run(window);
    return 0;
}