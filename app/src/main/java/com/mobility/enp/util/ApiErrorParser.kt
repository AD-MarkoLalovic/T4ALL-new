package com.mobility.enp.util

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.mobility.enp.data.model.ApiErrorResponse
import okhttp3.ResponseBody

object ApiErrorParser {
    private val gson = Gson()

    fun parseErrorResponse(
        errorCode: Int,
        errorBody: ResponseBody,
        defaultMessage: String
    ): ApiErrorResponse {
        val errorBodyString = try {
            errorBody.string()
        } catch (e: Exception) {
            return ApiErrorResponse(code = errorCode, message = defaultMessage, errors = null)
        }
        return try {
            val parsed = gson.fromJson(errorBodyString, ApiErrorResponse::class.java)
                ?: return ApiErrorResponse(code = errorCode, message = defaultMessage, errors = null)
            val parsedWithCode = parsed.copy(code = errorCode)
            val processedErrors = when (val errs = parsedWithCode.errors) {
                is Map<*, *> -> errs.values
                    .flatMap { it as? List<*> ?: emptyList() }
                    .mapNotNull { it?.toString() }
                is List<*> -> errs
                else -> null
            }
            parsedWithCode.copy(errors = processedErrors)
        } catch (e: JsonParseException) {
            ApiErrorResponse(code = errorCode, message = defaultMessage, errors = null)
        }
    }
}