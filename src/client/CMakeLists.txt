find_package(SDL3 CONFIG REQUIRED)
find_package(SDL3_image CONFIG REQUIRED)

add_executable(client main.cpp game.cpp piece.cpp pawn.cpp board.cpp start_scene.cpp waiting_scene.cpp play_button.cpp)

target_link_libraries(client PRIVATE SDL3_image::SDL3_image SDL3::SDL3)

target_compile_options(client PRIVATE -Wall -Wextra -Wpedantic -Werror)

target_include_directories(client PRIVATE client)