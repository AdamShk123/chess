enable_testing()

find_package(GTest CONFIG REQUIRED)

add_executable (tests "test.cpp")

target_link_libraries(tests PRIVATE GTest::gtest GTest::gtest_main)

include(GoogleTest)
gtest_discover_tests(tests)