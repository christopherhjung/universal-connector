package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.Connection
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

abstract class FeederFactory<T> {
    protected abstract fun createInputFeeder(connection: Connection, queue: BlockingQueue<T>, context: FeederContext<T>) : InputFeeder<T>

    protected abstract fun createOutputFeeder(connection: Connection, queue: BlockingQueue<T>, context: FeederContext<T>) : OutputFeeder<T>

    fun createFeeder(connection: Connection) : FeederContext<T>{
        val context = FeederContext<T>()

        context.input = createInputFeeder(connection, LinkedBlockingQueue(), context)
        context.output = createOutputFeeder(connection, LinkedBlockingQueue(), context)

        return context
    }
}

class LineFeederFactory : FeederFactory<String>(){
    override fun createInputFeeder(
        connection: Connection,
        queue: BlockingQueue<String>,
        context: FeederContext<String>
    ): InputFeeder<String> {
        return LineInputFeeder(connection, queue, context)
    }

    override fun createOutputFeeder(
        connection: Connection,
        queue: BlockingQueue<String>,
        context: FeederContext<String>
    ): OutputFeeder<String> {
        return LineOutputFeeder(connection, queue, context)
    }
}


class FrameFeederFactory : FeederFactory<ByteArray>(){
    override fun createInputFeeder(
        connection: Connection,
        queue: BlockingQueue<ByteArray>,
        context: FeederContext<ByteArray>
    ): InputFeeder<ByteArray> {
        return FrameInputFeeder(connection, queue, context)
    }

    override fun createOutputFeeder(
        connection: Connection,
        queue: BlockingQueue<ByteArray>,
        context: FeederContext<ByteArray>
    ): OutputFeeder<ByteArray> {
        return FrameOutputFeeder(connection, queue, context)
    }
}
