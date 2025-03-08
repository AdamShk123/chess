find_package(SDL3 CONFIG REQUIRED)

add_executable(client main.cpp)

target_link_libraries(client PRIVATE SDL3::SDL3)

target_compile_options(client PRIVATE -Wall -Wextra -Wpedantic -Werror)

target_include_directories(client PRIVATE client)