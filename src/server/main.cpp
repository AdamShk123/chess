#include "main.hpp"

int main(int argc, char** argv)
{
    for(int i = 0; i < argc; i++)
    {
        std::cout << argv[i] << std::endl;
    }

    std::cout << "Starting server..." << std::endl;

    int status;
    addrinfo hints{};
    addrinfo *serverInfo = nullptr;

    std::memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    status = getaddrinfo(nullptr, PORT, &hints, &serverInfo);

    if(status != 0)
    {
        std::cerr << "getaddrinfo error: " << gai_strerror(status) << std::endl;
        return 1;
    }

    char ipstr[INET6_ADDRSTRLEN];
    addrinfo* curr = serverInfo;
    int socketFD;
    int yes = 1;

    while(curr != nullptr)
    {
        socketFD = socket(curr->ai_family, curr->ai_socktype, curr->ai_protocol);

        if(socketFD == -1)
        {
            std::cerr << "socket error: " << std::strerror(errno) << std::endl;
            curr = curr->ai_next;
            continue;
        }

        status = setsockopt(socketFD, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

        if(status == -1)
        {
            std::cerr << "setsockopt error: " << std::strerror(errno) << std::endl;
            return 1;
        }

        status = bind(socketFD, curr->ai_addr, curr->ai_addrlen);

        if(status == -1)
        {
            std::cerr << "Bind error: " << std::strerror(errno) << std::endl;
            curr = curr->ai_next;
            continue;
        }

        break;
    }

    if(curr == nullptr)
    {
        std::cerr << "failed to bind!" << std::endl;
        return 1;
    }

    std::cout << "opened socket..." << std::endl;

    status = listen(socketFD, BACKLOG);

    if(status == -1)
    {
        std::cerr << "listen error: " << std::strerror(errno) << std::endl;
        return 1;
    }

    std::cout << "waiting for new connections..." << std::endl;

    socklen_t sin_size;
    sockaddr_storage their_addr;
    int newFD;

    std::vector<int> connections{};

    while(true)
    {
        sin_size = sizeof(their_addr);

        newFD = accept(socketFD, (struct sockaddr *)&their_addr, &sin_size);

        if(newFD == -1)
        {
            std::cout << "server: failed to accept" << std::endl;
            continue;
        }

        inet_ntop(their_addr.ss_family, get_in_addr((sockaddr *)&their_addr), ipstr, sizeof(ipstr));
        std::cout << "server: got connection from " << ipstr << std::endl;

        if(connections.size() == 2)
        {
            int pid = fork();

            if(pid == -1)
            {
                std::cerr << "server: fork failed" << std::endl;
                return 1;
            }
            else if(pid == 0)
            {
                char* args[3];
                args[0] = strdup("../match/match");
                args[1] = nullptr;
                execvp(args[0], args);
            }
            else
            {

            }
        }
        else
        {
            connections.push_back(newFD);
        }

//        if (!fork()) { // this is the child process
//            close(socketFD); // child doesn't need the listener
//
//            if (send(newFD, "Hello, world!", 13, 0) == -1)
//            {
//                std::cerr << "send error" << std::endl;
//            }
//
//            close(newFD);
//
//            return 0;
//        }
//        close(newFD); // parent doesn't need this
    }

    freeaddrinfo(serverInfo);

    return 0;
}

void *get_in_addr(struct sockaddr *sa)
{
    return sa->sa_family == AF_INET
           ? (void *) &(((struct sockaddr_in*)sa)->sin_addr)
           : (void *) &(((struct sockaddr_in6*)sa)->sin6_addr);
}