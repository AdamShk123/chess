package com.example.chess_backend.match

class MatchNotFoundException(id: Int) :
    RuntimeException("Match not found with id: $id")