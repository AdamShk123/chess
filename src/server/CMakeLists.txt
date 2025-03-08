add_executable(server main.cpp)

target_compile_options(server PRIVATE -Wall -Wextra -Wpedantic -Werror)

target_include_directories(server PRIVATE server)