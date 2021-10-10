cmake_minimum_required(VERSION 3.4.1)

# Add library

add_library(socketwrench SHARED
        src/main/native/junixsocket.c)