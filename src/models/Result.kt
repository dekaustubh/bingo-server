package com.dekaustubh.models

sealed class Result(val result: Boolean)

class Error(val error: String): Result(false)
class Success(val success: String): Result(true)