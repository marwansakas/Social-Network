#include <sstream>
#include "../include/EchoEncoderDecoder.h"

EchoEncoderDecoder::EchoEncoderDecoder(ConnectionHandler &handler):handler(handler){}


bool EchoEncoderDecoder::encode(string toEncode) {
    vector<char> result;
    istringstream split(toEncode);
    vector<string> args;
    for(string s;split>>s;)
        args.push_back(s);
    if(args.size() > 0){
        short opcode = commandToShort(args[0]);
        if(opcode != -1) {
            if(sendOpcode(opcode)){
                for (unsigned i=1; i < args.size(); i++){
                    handler.sendLine(args[i]);
                }
                char endOfLine[] = {';'};
                handler.sendBytes(endOfLine, 1);
                return true;
            } else return false;
        } else {
            cout << "Unknown command" << endl;
        }
    }
    return true;
}
bool EchoEncoderDecoder::sendOpcode(short opcode){
    char opByte[2];
    opByte[0]=((opcode>>8) & 0xff);
    opByte[1]=(opcode & 0xff);
    return handler.sendBytes(opByte,2);
}

short EchoEncoderDecoder::commandToShort(string command) {
    if(command == "REGISTER"){
        return 1;
    }else if (command == "LOGIN") {
        return 2;
    }else if(command == "LOGOUT") {
        return 3;
    }else if(command == "FOLLOW") {
        return 4;
    }else if(command == "POST") {
        return 5;
    }else if (command == "PM") {
        return 6;
    }else if(command == "LOGSTAT") {
        return 7;
    }else if(command == "STAT") {
        return 8;
    }else if (command == "BLOCK") {
        return 12;
    }
    return -1;
}
void EchoEncoderDecoder::operator()() {
    while (!handler.shouldTerminate()) {
        char receive[4];
        if (handler.getBytes(receive,4)) {
            decode(receive);
        }
        else {
            handler.terminate();
        }
    }

}

void EchoEncoderDecoder::decode(char toDecode[]) {
    short res = (short)((toDecode[0] & 0xff) << 8);
    res += (short)(toDecode[1] & 0xff);
    short opcode = (short)((toDecode[2] & 0xff) << 8);
    opcode += (short)(toDecode[3] & 0xff);
    string content;
    handler.getLine(content);

    switch (res) {
        case 9:
            content.pop_back();
            if(opcode == 0)
            cout << "NOTIFICATION PM " << content << endl;
            else cout << "NOTIFICATION Public " << content << endl;
            break;
        case 10:
            content.pop_back();
            cout << "ACK " << opcode << " " << content << endl;
            if (opcode == 3){
                handler.terminate();
            }
            break;
        case 11:
            cout << "ERROR " << opcode << endl;
            if(opcode == 3)
                handler.setLogout(false);
            break;
        default:
            //debug
            //cout << "received: " << res << " and " << opcode << " and "  << endl;
            break;
    }
}