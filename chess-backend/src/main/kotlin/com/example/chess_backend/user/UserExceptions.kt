package com.example.chess_backend.user

class DuplicateNameException(name: String) :
    RuntimeException("Name '$name' already exists")

class DuplicateEmailException(email: String) :
    RuntimeException("Email '$email' already exists")

class UserAlreadyRegisteredException(auth0Id: String) :
    RuntimeException("User with auth0Id '$auth0Id' is already registered")

class UserNotFoundException : RuntimeException {
    constructor(id: Int) : super("User not found with id: $id")
    constructor(auth0Id: String) : super("User authenticated but not found in database (auth0Id: $auth0Id). Please contact support.")
}