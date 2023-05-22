#ifndef SPL3CPP_INPUTOUTPUTHANDLER_H
#define SPL3CPP_INPUTOUTPUTHANDLER_H

#include "connectionHandler.h"
#include "EchoEncoderDecoder.h"
#include <mutex>


class InputOutputHandler{
public:
    InputOutputHandler(ConnectionHandler &_handler);
    void operator()();
private:
    EchoEncoderDecoder encdec;
    ConnectionHandler &handler;
};

#endif //SPL3CPP_INPUTOUTPUTHANDLER_H
