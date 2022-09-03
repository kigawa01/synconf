package net.kigawa.synconf;

import net.kigawa.kutil.log.log.KLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class SocketConnector
{
    private final ServerSocket serverSocket;
    private final KLogger logger;
    private final ExecutorService executorService;

    public SocketConnector(int port, KLogger logger, ExecutorService executorService) throws IOException
    {
        this.logger = logger;
        this.executorService = executorService;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new IOException(e);
        }

        executorService.execute(this::waitConnect);
        logger.info("listen on " + port);
    }

    private void waitConnect()
    {
        logger.info("listen socket");
        try {
            Socket socket = serverSocket.accept();

            readSocket(socket);
        } catch (IOException e) {
            logger.warning(e);
            Synconf.getInstance().end();
        }

        if (Synconf.getInstance().isEnd()) {
            logger.info("close socket");
            return;
        }

        waitConnect();
    }

    private void readSocket(Socket socket)
    {
        try {
            var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            readLine(reader, writer);

        } catch (IOException e) {
            logger.warning(e);
            Synconf.getInstance().end();
        }

        try {
            socket.close();
        } catch (IOException e) {
            logger.warning(e);
        }
    }

    private void readLine(BufferedReader reader, BufferedWriter writer)
    {
        try {
            String line = reader.readLine();
            if (line == null) return;

            switch (line) {
                case "end" -> Synconf.getInstance().end();
                default -> {
                }
            }

        } catch (IOException e) {
            logger.warning(e);
        }

        if (Synconf.getInstance().isEnd()) return;
        readLine(reader, writer);
    }

    public void end()
    {
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.warning(e);
        }
        logger.info("port closed");
    }
}
