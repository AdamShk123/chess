#include <csignal>
#include "main.hpp"

int main(int argc, char** argv)
{
    if(argc != 3)
    {
        std::cerr << "Incorrect number of command-line arguments. Should be exactly 2." << std::endl;
        return 1;
    }

    std::string address = argv[1];
    std::string port = argv[2];

    if(!validateAddress(address))
    {
        std::cerr << "Invalid Address!" << std::endl;
        return 1;
    }

    if(!validatePort(port))
    {
        std::cerr << "Invalid Port!" << std::endl;
        return 1;
    }

    std::cout << "Starting server..." << std::endl;

    int status;
    addrinfo hints{};
    addrinfo *serverInfo = nullptr;

    std::memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    status = getaddrinfo(address.c_str(), port.c_str(), &hints, &serverInfo);

    if(status != 0)
    {
        std::cerr << "getaddrinfo error: " << gai_strerror(status) << std::endl;
        return 1;
    }

    addrinfo* curr = serverInfo;
    int socketFD;

    while(curr != nullptr)
    {
        socketFD = socket(curr->ai_family, curr->ai_socktype, curr->ai_protocol);

        if(socketFD == -1)
        {
            std::cerr << "socket error: " << std::strerror(errno) << std::endl;
            curr = curr->ai_next;
            continue;
        }

        status = connect(socketFD, curr->ai_addr, curr->ai_addrlen);

        if(status == -1)
        {
            close(socketFD);
            std::cerr << "connect error: " << std::strerror(errno) << std::endl;
            curr = curr->ai_next;
            continue;
        }

        break;
    }

    if(curr == nullptr)
    {
        std::cerr << "client: failed to connect!" << std::endl;
//        return 1;
    }

//    std::cout << "client: connecting to..." << std::endl;
//
//    int numbytes;
//    char buf[MAXDATASIZE];
//
//    numbytes = recv(socketFD, buf, MAXDATASIZE-1, 0);
//    if (numbytes == -1)
//    {
//        std::cerr << "recv error" << std::endl;
//        return 1;
//    }
//    buf[numbytes] = '\0';
//    std::cout << "client: received " << buf << std::endl;
//    close(socketFD);
//
//    freeaddrinfo(serverInfo);

    auto game = Game::Game();

    game.run();

    return 0;
}

void *get_in_addr(struct sockaddr *sa)
{
    return sa->sa_family == AF_INET
           ? (void *) &(((struct sockaddr_in*)sa)->sin_addr)
           : (void *) &(((struct sockaddr_in6*)sa)->sin6_addr);
}

bool validateAddress(const std::string& address)
{
    if(address == "localhost") return true;

    const std::regex pattern("^[a-zA-Z0-9-]{1,63}\\.[a-zA-Z]{2,6}$|^localhost$");

    return std::regex_match(address, pattern);
}

bool validatePort(const std::string& port)
{
    try
    {
        int converted = std::stoi(port);
        if(converted < 1024) return false;
    }
    catch (std::invalid_argument const& ex)
    {
        std::cout << "std::invalid_argument::what(): " << ex.what() << '\n';
        return false;
    }
    catch (std::out_of_range const& ex)
    {
        std::cout << "std::out_of_range::what(): " << ex.what() << '\n';
        return false;
    }

    for(char ch : port)
    {
        if(ch < 0x30 || ch > 0x39)
        {
            return false;
        }
    }

    return true;
}