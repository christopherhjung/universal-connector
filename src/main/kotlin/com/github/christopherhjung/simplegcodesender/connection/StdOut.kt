package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream

class StdOut : StaticStreamConnection(InputStream.nullInputStream(), System.out )

