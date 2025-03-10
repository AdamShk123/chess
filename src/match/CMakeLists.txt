add_executable(match main.cpp)

target_compile_options(match PRIVATE -Wall -Wextra -Wpedantic -Werror)

target_include_directories(match PRIVATE match)