#ifndef SPL3CPP_ECHOENCODERDECODER_H
#define SPL3CPP_ECHOENCODERDECODER_H

#include <string>
#include <vector>
#include <iostream>
#include <codecvt>
#include <locale>
#include <mutex>
#include "connectionHandler.h"

using namespace std;

class EchoEncoderDecoder {
public:
    EchoEncoderDecoder(ConnectionHandler &handler);
    //encode
    bool encode(string toEncode);
    //decode
    void decode(char* toDecode);
    void operator()();
private:
    bool sendOpcode(short opcode);
    short commandToShort(string str);
    ConnectionHandler &handler;
};


#endif //SPL3CPP_ECHOENCODERDECODER_H
