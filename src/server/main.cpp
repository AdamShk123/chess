#include "main.hpp"

std::atomic<bool> running(true);

auto main(int argc, char** argv) -> int
{
    for(int i = 0; i < argc; i++)
    {
        std::cout << argv[i] << std::endl;
    }

    std::signal(SIGINT, signalHandler);
    std::signal(SIGTERM, signalHandler);

    std::cout << "Starting server..." << std::endl;

    auto addrInfo = loadInfo();

    if(!addrInfo.has_value())
    {
        std::cerr << "getaddrinfo error: " << gai_strerror(addrInfo.error()) << std::endl;
        return 1;
    }

    auto socketFD = createSocket(addrInfo.value());

    if(!socketFD.has_value())
    {
        switch (socketFD.error())
        {
            case CREATE:
                std::cerr << "CREATE: " << strerror(errno) << std::endl;
                return 1;
            case OPTIONS:
                std::cerr << "OPTIONS: " << strerror(errno) << std::endl;
                return 1;
            case BIND:
                std::cerr << "BIND: " << strerror(errno) << std::endl;
                return 1;
            case LISTEN:
                std::cerr << "LISTEN: " << strerror(errno) << std::endl;
                return 1;
        }
    }

    std::cout << "waiting for new connections..." << std::endl;

    char ipstr[INET6_ADDRSTRLEN];

    socklen_t sin_size;
    sockaddr_storage their_addr{};
    int newFD;

    std::vector<pollfd> connections{};

//    std::unordered_map<int,int> socketToMatch{};

//    std::array<std::pair<int,int>,MAX_MATCHES> matchToSockets{};

    pollfd pfd{socketFD.value(), POLLIN, 0};

    while(running)
    {
        if(connections.size() != MAX_CONNECTIONS)
        {
            sin_size = sizeof(their_addr);

            int numEvents = poll(&pfd, 1, TIMEOUT);

            if(numEvents == -1)
            {
                std::cerr << "poll failed!" << std::endl;
                break;
            }
            else if(numEvents == 1)
            {
                if(pfd.revents & (POLLIN | POLLHUP))
                {
                    newFD = accept(socketFD.value(), (struct sockaddr *)&their_addr, &sin_size);

                    if(newFD == -1)
                    {
                        std::cout << "server: failed to accept" << std::endl;
                        continue;
                    }

                    inet_ntop(their_addr.ss_family, get_in_addr((sockaddr *)&their_addr), ipstr, sizeof(ipstr));
                    std::cout << "server: got connection from " << ipstr << std::endl;

                    connections.push_back({newFD, POLLIN, 0});
                }
            }
        }

        int numEvents = poll(connections.data(), connections.size(), TIMEOUT);

        if(numEvents == -1)
        {
            std::cerr << "poll failed!" << std::endl;
            break;
        }

        for(const auto& conn : connections)
        {
            if(conn.revents & (POLLIN | POLLHUP))
            {
                char buf[MAX_DATA_SIZE];

                size_t numBytes = recv(conn.fd, buf, MAX_DATA_SIZE-1, 0);

                std::cout << "bytes: " << numBytes << std::endl;

                if (static_cast<int>(numBytes) == -1)
                {
                    std::cerr << "recv error" << std::endl;
                    close(conn.fd);
                    continue;
                }
                else if(numBytes == 0)
                {
                    std::cout << "connection closed" << std::endl;
                    close(conn.fd);
                    continue;
                }

                buf[numBytes] = '\0';
                std::cout << "client: received " << buf << std::endl;
            }
        }
    }

    std::cout << "server stopped" << std::endl;
    return 0;
}

auto signalHandler(int signal) -> void
{
    if(signal == SIGINT || signal == SIGTERM)
    {
        std::cout << "shutting down server" << std::endl;
        running = false;
    }
}

void *get_in_addr(struct sockaddr *sa)
{
    return sa->sa_family == AF_INET
           ? (void *) &(((struct sockaddr_in*)sa)->sin_addr)
           : (void *) &(((struct sockaddr_in6*)sa)->sin6_addr);
}

auto loadInfo() -> std::expected<std::unique_ptr<addrinfo,AddrInfoDeleter>,loadInfoError>
{
    addrinfo hints{};
    addrinfo *addrInfo = nullptr;

    std::memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    int status = getaddrinfo(nullptr, PORT, &hints, &addrInfo);

    if(status != 0)
    {
        return std::unexpected(static_cast<loadInfoError>(status));
    }

    return std::unique_ptr<addrinfo,AddrInfoDeleter>(addrInfo);
}

auto createSocket(const std::unique_ptr<addrinfo,AddrInfoDeleter>& info) -> std::expected<int,socketError>
{
    int status;
    addrinfo* curr = info.get();
    int socketFD;
    int yes = 1;

    std::optional<socketError> last = std::nullopt;

    while(curr != nullptr)
    {
        socketFD = socket(curr->ai_family, curr->ai_socktype, curr->ai_protocol);

        if(socketFD == -1)
        {
            curr = curr->ai_next;
            last = socketError::CREATE;
            continue;
        }

        status = setsockopt(socketFD, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

        if(status == -1)
        {
            last = socketError::OPTIONS;
            continue;
        }

        status = bind(socketFD, curr->ai_addr, curr->ai_addrlen);

        if(status == -1)
        {
            last = socketError::BIND;
            curr = curr->ai_next;
            continue;
        }

        break;
    }

    if(curr == nullptr)
    {
        return last.has_value() ? std::unexpected(last.value()) : std::unexpected(socketError::CREATE);
    }

    status = listen(socketFD, BACKLOG);

    if(status == -1)
    {
        return std::unexpected(socketError::LISTEN);
    }

    return socketFD;
}