#include "../include/InputOutputHandler.h"
#include "../include/connectionHandler.h"

using namespace std;

InputOutputHandler::InputOutputHandler(ConnectionHandler &_handler) : encdec(EchoEncoderDecoder(_handler)),
                                                                      handler(_handler) {}

void InputOutputHandler::operator()() {

    while (!handler.shouldTerminate()) {
        while(!handler.shouldLogut()) {
            short buffer_size = 1024;

            char buffer[buffer_size];

            cin.getline(buffer, buffer_size);

            string line(buffer);
            if(line == "LOGOUT") {
                handler.setLogout(true);
            }
            if (!encdec.encode(line)) {
                handler.terminate();
                break;
            }

        }
    }
}