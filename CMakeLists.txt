cmake_minimum_required (VERSION 3.31)

project (chess)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

# Include sub-projects.
add_subdirectory (src/client)
add_subdirectory (src/server)
add_subdirectory (tests)