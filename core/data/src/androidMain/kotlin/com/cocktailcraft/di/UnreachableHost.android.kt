package com.cocktailcraft.di

import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

// OkHttp surfaces DNS failures and refused/unroutable connections as these
internal actual fun isUnreachableHostError(cause: Throwable): Boolean =
    cause is UnknownHostException || cause is ConnectException || cause is NoRouteToHostException
